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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Philipp Urbauer
 */
public class MaintainCodeSystemConceptMetadataValue
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MaintainCodeSystemConceptMetadataValueResponseType MaintainCodeSystemConceptMetadataValue(MaintainCodeSystemConceptMetadataValueRequestType parameter){
    
    return MaintainCodeSystemConceptMetadataValue(parameter, null);
  }
  
  public MaintainCodeSystemConceptMetadataValueResponseType MaintainCodeSystemConceptMetadataValue(MaintainCodeSystemConceptMetadataValueRequestType parameter, org.hibernate.Session session)
  {
      
    if (logger.isInfoEnabled())
      logger.info("====== MaintainCodeSystemConceptMetadataValue gestartet ======");

      boolean createHibernateSession = (session == null);
      logger.debug("createHibernateSession: " + createHibernateSession);

      // Return-Informationen anlegen
      MaintainCodeSystemConceptMetadataValueResponseType response = new MaintainCodeSystemConceptMetadataValueResponseType();
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
        List<CodeSystemMetadataValue> codeSystemMetadataValues = new ArrayList<CodeSystemMetadataValue>();
        
        // Zum Speichern vorbereiten          
        List<CodeSystemMetadataValue> csmvList = parameter.getCodeSystemMetadataValues();  // bereits geprüft, ob null  
        Long csvId = parameter.getCodeSystemVersionId();
        try
        {   
            Iterator<CodeSystemMetadataValue> iterator = csmvList.iterator();
            Long entityVersionId = -1l; // Immer die gleiche
            while(iterator.hasNext()){
            
                CodeSystemMetadataValue csmv = (CodeSystemMetadataValue)iterator.next();
                CodeSystemMetadataValue vsmv_db = (CodeSystemMetadataValue)hb_session.get(CodeSystemMetadataValue.class, csmv.getId());
            
                if(csmv.getParameterValue() != null && !csmv.getParameterValue().equals(""))
                    vsmv_db.setParameterValue(csmv.getParameterValue());

                entityVersionId = vsmv_db.getCodeSystemEntityVersion().getVersionId();
                hb_session.update(vsmv_db);
            }
            
            // Metadata-Values auslesen (performanter)
            // ohne Abfrage würde für jede Metadata-Value 1 Abfrage ausgeführt,
            // so wie jetzt wird 1 Abfrage pro Entity-Version ausgeführt
            String hql = "select distinct csev from CodeSystemEntityVersion csev";
            hql += " join fetch csev.codeSystemMetadataValues csmv";
            hql += " join fetch csmv.metadataParameter mp";
            
            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("csev.", "versionId", entityVersionId);

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(q);

            List<CodeSystemEntityVersion> versionList = q.list();
            Set<CodeSystemMetadataValue> mdValues = versionList.get(0).getCodeSystemMetadataValues();

            Iterator<CodeSystemMetadataValue> iter = mdValues.iterator();
            while(iter.hasNext()){
                CodeSystemMetadataValue v = (CodeSystemMetadataValue)iter.next();
                codeSystemMetadataValues.add(v);
            }
            
            
            LastChangeHelper.updateLastChangeDate(true, csvId,hb_session);
            hb_session.getTransaction().commit();
            if (codeSystemMetadataValues != null)
              {
                Iterator<CodeSystemMetadataValue> itMV = codeSystemMetadataValues.iterator();

                while (itMV.hasNext())
                { 
                  CodeSystemMetadataValue mValue = itMV.next();
                  mValue.getCodeSystemEntityVersion().setCodeSystemMetadataValues(null);
                  mValue.getCodeSystemEntityVersion().setValueSetMetadataValues(null);
                  mValue.getCodeSystemEntityVersion().setAssociationTypes(null);
                  mValue.getCodeSystemEntityVersion().setCodeSystemConcepts(null);
                  mValue.getCodeSystemEntityVersion().setCodeSystemEntity(null);
                  mValue.getCodeSystemEntityVersion().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                  mValue.getCodeSystemEntityVersion().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
                  mValue.getCodeSystemEntityVersion().setConceptValueSetMemberships(null);
                  mValue.getCodeSystemEntityVersion().setPropertyVersions(null);

                  if (mValue.getMetadataParameter() != null)
                  {
                    mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                    mValue.getMetadataParameter().setCodeSystem(null);
                    mValue.getMetadataParameter().setValueSet(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                  }
                }
              }
            
            // Liste der Response beifügen
            response.setCodeSystemMetadataValues(codeSystemMetadataValues);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("CodeSystemConceptMetadataValue erfolgreich bearbeitet");
        }
        catch (Exception e)
        {
          hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemConceptMetadataValue', Hibernate: " + e.getLocalizedMessage());
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
        // Fehlereldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemConceptMetadataValue': " + e.getLocalizedMessage());// + " ; Mögliche Ursache: Verwendete Id(" + Long.toString(parameter.getValueSet().getId()) +  ") nicht in Datenbank.");           
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
  private boolean validateParameter(MaintainCodeSystemConceptMetadataValueRequestType Request,MaintainCodeSystemConceptMetadataValueResponseType Response)
  {
    boolean erfolg = true;

    List<CodeSystemMetadataValue> codeSystemMetadataValues = Request.getCodeSystemMetadataValues();
    if (codeSystemMetadataValues == null || codeSystemMetadataValues.isEmpty())
    {
      Response.getReturnInfos().setMessage("CodeSystemMetadataValue darf nicht NULL/Empty sein!");
      erfolg = false;
    }
    
    if (erfolg == false){
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    
    return erfolg;
  }
}
