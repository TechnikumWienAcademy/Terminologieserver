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
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class ReturnConceptValueSetMembership
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnConceptValueSetMembershipResponseType ReturnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnConceptValueSetMembership gestartet ======");

    // Return-Informationen anlegen
    ReturnConceptValueSetMembershipResponseType response = new ReturnConceptValueSetMembershipResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;

    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }

    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      ConceptValueSetMembership cvsm_db = null;

      try
      {
        ConceptValueSetMembershipId cvsm_id = 
                new ConceptValueSetMembershipId(parameter.getCodeSystemEntityVersion().getVersionId(),
                                                parameter.getValueSetVersion().getVersionId());
        cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsm_id);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptValueSetMembership', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnConceptValueSetMembership', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      if (cvsm_db != null)
      {
        cvsm_db.setCodeSystemEntityVersion(null);
        cvsm_db.setValueSetVersion(null);

        // Liste der Response beifügen
        response.setConceptValueSetMembership(cvsm_db);
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("ConceptValueSetMembership erfolgreich gelesen");
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptValueSetMembership': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnConceptValueSetMembership': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnConceptValueSetMembershipRequestType Request, ReturnConceptValueSetMembershipResponseType Response)
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
    
    if(Request.getCodeSystemEntityVersion() == null || Request.getValueSetVersion() == null){
    
        Response.getReturnInfos().setMessage("CodeSystemEntityVersion und ValueSetVersion darf nicht NULL sein!");
        erfolg = false;
    }else{
    
        if(Request.getCodeSystemEntityVersion().getVersionId() == null || Request.getCodeSystemEntityVersion().getVersionId() == 0){
        
            Response.getReturnInfos().setMessage("CodeSystemEntityVersion.versionId muss korrekt angegeben werden!");
            erfolg = false;
        }
        
        if(Request.getValueSetVersion().getVersionId() == null || Request.getValueSetVersion().getVersionId() == 0){
        
            Response.getReturnInfos().setMessage("ValueSetVersion.versionId muss korrekt angegeben werden!");
            erfolg = false;
        }
    }
    

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
