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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ReturnVsConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnVsConceptMetadataResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnVsConceptMetadata
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnVsConceptMetadataResponseType ReturnVsConceptMetadata(ReturnVsConceptMetadataRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnVsConceptMetadata gestartet ======");

    // Return-Informationen anlegen
    ReturnVsConceptMetadataResponseType response = new ReturnVsConceptMetadataResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter pr�fen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt f�r jeden Webservice)
    boolean loggedIn = false;

    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }
    
    List<ValueSetMetadataValue> valueSetMetadataValues = null;
    // Hibernate-Block, Session �ffnen
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    
    try{
        try{
            
            
            

            // Metadata-Values auslesen (performanter)
            // ohne Abfrage w�rde f�r jede Metadata-Value 1 Abfrage ausgef�hrt,
            // so wie jetzt wird 1 Abfrage pro Entity-Version ausgef�hrt
            String hql = "select distinct vsmv from ValueSetMetadataValue vsmv";
            hql += " join fetch vsmv.metadataParameter mp";
            hql += " join vsmv.codeSystemEntityVersion csev";
            
            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("csev.", "versionId", parameter.getCodeSystemEntityVersionId());
            parameterHelper.addParameter("vsmv.", "valuesetVersionId", parameter.getValuesetVersionId());

            // Parameter hinzuf�gen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);


            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);
						//Matthias: set read Only
						q.setReadOnly(true);

            // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
            parameterHelper.applyParameter(q);

            valueSetMetadataValues = q.list();

            if (valueSetMetadataValues != null)
              {
                Iterator<ValueSetMetadataValue> itMV = valueSetMetadataValues.iterator();

                while (itMV.hasNext())
                { 
                  ValueSetMetadataValue mValue = itMV.next();

                  if (mValue.getMetadataParameter() != null)
                  {
                    mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                    mValue.getMetadataParameter().setCodeSystem(null);
                    mValue.getMetadataParameter().setValueSet(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                  }
                }
              }
            
            // Liste der Response beif�gen
            List<ValueSetMetadataValue> l = new ArrayList<ValueSetMetadataValue>();
            for(ValueSetMetadataValue ent:valueSetMetadataValues){
                ValueSetMetadataValue v = new ValueSetMetadataValue();
                v.setId(ent.getId());
                v.setParameterValue(ent.getParameterValue());
                v.setValuesetVersionId(ent.getValuesetVersionId());
                
                v.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                v.getCodeSystemEntityVersion().setVersionId(ent.getCodeSystemEntityVersion().getVersionId());
                
                v.setMetadataParameter(new MetadataParameter());
                v.getMetadataParameter().setId(ent.getMetadataParameter().getId());
                v.getMetadataParameter().setMetadataParameterType(ent.getMetadataParameter().getParamDatatype());
                v.getMetadataParameter().setParamDatatype(ent.getMetadataParameter().getParamDatatype());
                v.getMetadataParameter().setParamName(ent.getMetadataParameter().getParamName());
                
                l.add(v);
            }
            response.setValueSetMetadataValue(l);
            //hb_session.getTransaction().commit();
          }
          catch (Exception e)
          {
            //hb_session.getTransaction().rollback();
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ReturnVsConceptMetadata', Hibernate: " + e.getLocalizedMessage());

            logger.error("Fehler bei 'ReturnVsConceptMetadata', Hibernate: " + e.getLocalizedMessage());
            e.printStackTrace();
          }
          finally
          {
            hb_session.close();
          }

          if (valueSetMetadataValues.isEmpty())
          {
            response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet-Concept wurden kein Metadaten gefunden!");
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          }
          else
          {
            response.getReturnInfos().setCount(1);
            response.getReturnInfos().setMessage("ValueSet-Concept Metadaten erfolgreich gelesen");
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
          }

          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnVsConceptMetadata': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnVsConceptMetadata': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnVsConceptMetadataRequestType Request, ReturnVsConceptMetadataResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getLogin() != null)
    {
      if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
      {
        Response.getReturnInfos().setMessage(
          "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
        erfolg = false;
      }
    }

    if (Request.getCodeSystemEntityVersionId() == null)
    {
      Response.getReturnInfos().setMessage(
        "CodeSystemEntityVersionId darf nicht null sein!");
      erfolg = false;
    }
    if (Request.getValuesetVersionId() == null)
    {
      Response.getReturnInfos().setMessage(
        "ValuesetVersionId darf nicht null sein!");
      erfolg = false;
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
