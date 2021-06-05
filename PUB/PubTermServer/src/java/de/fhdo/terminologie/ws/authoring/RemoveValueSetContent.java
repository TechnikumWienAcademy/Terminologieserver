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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class RemoveValueSetContent
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public RemoveValueSetContentResponseType RemoveValueSetContent(RemoveValueSetContentRequestType parameter)
  {
    return RemoveValueSetContent(parameter, null);
  }

  /**
   * Entfernt Konzepte aus einem Value Set
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public RemoveValueSetContentResponseType RemoveValueSetContent(RemoveValueSetContentRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== RemoveValueSetContent gestartet ======");

    boolean createHibernateSession = (session == null);

    // Return-Informationen anlegen
    RemoveValueSetContentResponseType response = new RemoveValueSetContentResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter pr�fen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt f�r jeden Webservice)    
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
        return response;
    }

    try
    {
      int count = 0, failure = 0;

      // Hibernate-Block, Session �ffnen
      org.hibernate.Session hb_session = null;
      //org.hibernate.Transaction tx = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
      }
      else
      {
        hb_session = session;
        hb_session.getTransaction().begin();
      }

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        long vsv_id = ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId();
        // Konzepte aus DB l�schen
        for (CodeSystemEntity cse : parameter.getCodeSystemEntity())
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0];
          long csev_id = csev.getVersionId();
          ConceptValueSetMembershipId cvsm_id = new ConceptValueSetMembershipId(csev_id, vsv_id);
          ConceptValueSetMembership cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsm_id);

          if (cvsm_db != null)
          {
            hb_session.delete(cvsm_db);
            count++;
            //Remove ValueSetMetadataValues too
            String hql = "select distinct vsmv from ValueSetMetadataValue vsmv join vsmv.codeSystemEntityVersion csev";
            
            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("csev.", "versionId", csev_id);
            parameterHelper.addParameter("vsmv.", "valuesetVersionId", vsv_id);
            
            // Parameter hinzuf�gen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);
            
            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);
            parameterHelper.applyParameter(q);

            List<ValueSetMetadataValue>  vsmvList= q.list();
            if(!vsmvList.isEmpty()){
                Iterator<ValueSetMetadataValue> iter = vsmvList.iterator();
                while(iter.hasNext()){
                    ValueSetMetadataValue vsmv = (ValueSetMetadataValue)iter.next();
                    hb_session.delete(vsmv);
                }
             }
          }
          else failure++;
        }
        LastChangeHelper.updateLastChangeDate(false, vsv_id,hb_session);
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'RemoveValueSetContents', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'RemoveValueSetContents', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();

        if (createHibernateSession)
          hb_session.getTransaction().rollback();
      }
      finally
      {
        // Transaktion abschlie�en
        if (createHibernateSession)
        {
          hb_session.getTransaction().commit();
          hb_session.close();
        }
      }

      // Antwort zusammenbauen
      response.getReturnInfos().setCount(count);
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      if(count > 0)
        response.getReturnInfos().setMessage(count + " Konzepte erfolgreich aus dem Value Set gel�scht.");
      else response.getReturnInfos().setMessage("Keine Konzepte aus dem Value Set gel�scht.");
      
      if(failure > 0)
      {
        response.getReturnInfos().setMessage(response.getReturnInfos().getMessage() + " " + failure + " Konzept(e) konnten in dem angegeben Value Set nicht gefunden werden.");
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setCount(0);
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'RemoveValueSetContents': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'RemoveValueSetContents': " + e.getLocalizedMessage());
    }

    return response;
  }

  /**
   * Pr�ft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(RemoveValueSetContentRequestType Request, RemoveValueSetContentResponseType Response)
  {
    boolean erfolg = true;
    String sErrorMessage = "";

    if (Request == null)
    {
      sErrorMessage = "Kein Requestparameter angegeben!";
      erfolg = false;
    }
    else
    {
      if (erfolg)
      {
        if (Request.getCodeSystemEntity() == null || Request.getCodeSystemEntity().size() == 0)
        {
          sErrorMessage = "Keine Konzepte angegeben, die aus dem Value Set entfernt werden sollen! Bitte setzen Sie den Parameter 'codeSystemEntity'.";
          erfolg = false;
        }
        else
        {
          for (CodeSystemEntity cse : Request.getCodeSystemEntity())
          {
            if (cse == null || cse.getCodeSystemEntityVersions() == null
                    || cse.getCodeSystemEntityVersions().size() != 1)
            {
              sErrorMessage = "Das CodeSystemEntity mit der ID " + cse.getId() + " beinhaltet keine oder mehrere Versionen! Bitte setzen Sie den Parameter 'codeSystemEntityVersion'.";
              erfolg = false;
            }
            else
            {
              CodeSystemEntityVersion csev = (CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0];
              if (csev.getVersionId() == null || csev.getVersionId() == 0)
              {
                sErrorMessage = "Das CodeSystemEntity mit der ID " + cse.getId() + " beinhaltet eine CodeSystemEntityVersion ohne ID! Bitte geben Sie eine ID bei der CodeSystemEntityVersion mit.";
                erfolg = false;
              }
            }
          }
        }
      }
      if (erfolg)
      {
        if (Request.getValueSet() == null)
        {
          sErrorMessage = "Kein Value Set angegeben! Bitte setzen Sie den Parameter 'valueSet'.";
          erfolg = false;
        }
        else
        {
          if (Request.getValueSet().getValueSetVersions() == null || Request.getValueSet().getValueSetVersions().size() == 0)
          {
            sErrorMessage = "Keine Value Set-Version angegeben! Bitte setzen Sie den Parameter 'valueSetVersion'.";
            erfolg = false;
          }
          else
          {
            for (ValueSetVersion vsv : Request.getValueSet().getValueSetVersions())
            {
              if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
              {
                sErrorMessage = "Die ValueSet-Version beinhaltet keine ID oder die ID ist 0! Bitte setzen Sie den Parameter 'versionId' mit einer ID > 0.";
                erfolg = false;
              }
            }
          }
        }
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setMessage(sErrorMessage);
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;
  }
}
