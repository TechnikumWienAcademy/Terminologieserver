/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Philipp Urbauer
 */
public class DeleteValueSetConceptMetadataValue
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public DeleteValueSetConceptMetadataValueResponseType DeleteValueSetConceptMetadataValue(DeleteValueSetConceptMetadataValueRequestType parameter){
    
    return DeleteValueSetConceptMetadataValue(parameter, null);
  }
  
  public DeleteValueSetConceptMetadataValueResponseType DeleteValueSetConceptMetadataValue(DeleteValueSetConceptMetadataValueRequestType parameter, org.hibernate.Session session)
  {
      
    if (logger.isInfoEnabled())
      logger.info("====== DeleteValueSetConceptMetadataValue gestartet ======");

      boolean createHibernateSession = (session == null);
      logger.debug("createHibernateSession: " + createHibernateSession);

      // Return-Informationen anlegen
      DeleteValueSetConceptMetadataValueResponseType response = new DeleteValueSetConceptMetadataValueResponseType();
      response.setReturnInfos(new ReturnType());

      // Parameter prüfen
      if (validateParameter(parameter, response) == false)
      {
        logger.debug("Parameter falsch");
        return response; // Fehler bei den Parametern
      }

      // Login-Informationen auswerten (gilt für jeden Webservice)    
      if (parameter != null)
      {
        if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true, session) == false)
        {
          logger.warn("Nicht eingeloggt!");
          return response;
        }
      }

      try
      {
        // Hibernate-Block, Session öffnen
        org.hibernate.Session hb_session = null;
        org.hibernate.Transaction tx = null;

        if (createHibernateSession)
        {
          hb_session = HibernateUtil.getSessionFactory().openSession();
          hb_session.getTransaction().begin();
        }
        else
        {
          hb_session = session;
          //hb_session.getTransaction().begin();
        }

        // Die Rückgabevariablen erzeugen
        List<ValueSetMetadataValue> valueSetMetadataValues = null;
        
        // Zum Speichern vorbereiten          
        ValueSetMetadataValue vsmv = parameter.getValueSetMetadataValue();  // bereits geprüft, ob null  

        try
        {   // 1. try-catch-Block zum Abfangen von Hibernate-Fehlern        
            // Neues ValueSetMetadataValue in der DB speichern. Dabei bekommt das Javaobjekt auch gleichzeitig eine ID zugewiesen
            hb_session.delete(vsmv);

            // Metadata-Values auslesen (performanter)
            // ohne Abfrage würde für jede Metadata-Value 1 Abfrage ausgeführt,
            // so wie jetzt wird 1 Abfrage pro Entity-Version ausgeführt
            String hql = "select distinct vsmv from ValueSetMetadataValue vsmv";
            hql += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";
            
            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            //parameterHelper.addParameter("vsmv.", "codeSystemEntityVersionId", vsmv.getCodeSystemEntityVersionId());
            parameterHelper.addParameter("csev.", "versionId", vsmv.getCodeSystemEntityVersion().getVersionId());
            parameterHelper.addParameter("vsmv.", "valuesetVersionId", vsmv.getValuesetVersionId());

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(q);

            valueSetMetadataValues = q.list();

            
            LastChangeHelper.updateLastChangeDate(false, vsmv.getValuesetVersionId(),hb_session);
            hb_session.getTransaction().commit();
            if (valueSetMetadataValues != null)
              {
                Iterator<ValueSetMetadataValue> itMV = valueSetMetadataValues.iterator();

                while (itMV.hasNext())
                { 
                  ValueSetMetadataValue mValue = itMV.next();

                  if (mValue.getMetadataParameter() != null)
                  {
                    mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                    mValue.getMetadataParameter().setCodeSystem(null);
                  }
                }
              }
            
            // Liste der Response beifügen
            response.setValueSetMetadataValue(valueSetMetadataValues);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("ValueSetConceptMetadataValue erfolgreich gelöscht");
        }
        catch (Exception e)
        {
          hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler bei 'DeleteValueSetConceptMetadataValue', Hibernate: " + e.getLocalizedMessage());
          logger.error(response.getReturnInfos().getMessage());
          e.printStackTrace();
        }
        finally
        {
					if (createHibernateSession){
						hb_session.close();
					}
            
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'DeleteValueSetConceptMetadataValue': " + e.getLocalizedMessage());// + " ; Mögliche Ursache: Verwendete Id(" + Long.toString(parameter.getValueSet().getId()) +  ") nicht in Datenbank.");           
        logger.error(response.getReturnInfos().getMessage());
        e.printStackTrace();
      }
      return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   * 
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(DeleteValueSetConceptMetadataValueRequestType Request,DeleteValueSetConceptMetadataValueResponseType Response)
  {
    boolean erfolg = true;

    ValueSetMetadataValue valueSetMetadataValue = Request.getValueSetMetadataValue();
    if (valueSetMetadataValue == null)
    {
      Response.getReturnInfos().setMessage("ValueSetMetadataValue darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      MetadataParameter metadataParameter = valueSetMetadataValue.getMetadataParameter();
      if (metadataParameter == null){

          Response.getReturnInfos().setMessage("MetadataParameter darf nicht NULL sein!");
          erfolg = false;
      }
    }
    
    if (erfolg == false){
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    
    return erfolg;
  }
}
