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
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class UpdateConceptValueSetMembershipStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateConceptValueSetMembershipStatusResponseType UpdateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter)
  {
    if (logger.isInfoEnabled())    
      logger.info("====== UpdateConceptValueSetMembershipStatus gestartet ======");    

    UpdateConceptValueSetMembershipStatusResponseType response = new UpdateConceptValueSetMembershipStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Parameter prüfen
    if (validateParameter(parameter, response) == false)    
      return response; // Fehler bei den Parametern
    
    // Login-Informationen auswerten (gilt für jeden Webservice)    
    if (parameter != null){
        if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
            return response;    
        }
    }

    try{
        CodeSystemEntityVersion csev = parameter.getCodeSystemEntityVersion();
        ConceptValueSetMembership cvsm = csev.getConceptValueSetMemberships().iterator().next();
      // Hibernate-Block, Session öffnen
      org.hibernate.Session     hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        ConceptValueSetMembership cvsm_db = null;
        if(cvsm.getId() == null){
        
                ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                        cvsm.getCodeSystemEntityVersion().getVersionId(), cvsm.getValueSetVersion().getVersionId());
                cvsm_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, cvsmId);
        }else{
            cvsm_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, cvsm.getId());
        }
        cvsm_db.setStatus(cvsm.getStatus());
        if(cvsm.getStatusDate() != null){
            cvsm_db.setStatusDate(cvsm.getStatusDate());
        }
        hb_session.update(cvsm_db);
        
        LastChangeHelper.updateLastChangeDate(false, cvsm_db.getId().getValuesetVersionId(),hb_session);
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptValueSetMembershipStatus': " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateConceptValueSetMembershipStatus'-Hibernate: " + e.getLocalizedMessage());
        
        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }
      if (true) {
            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Status erfolgreich geändert.");
        }
      
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptValueSetMembershipStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateConceptValueSetMembershipStatus': " + e.getLocalizedMessage());
      
      e.printStackTrace();
    }
    return response;
  }

  private boolean validateParameter(UpdateConceptValueSetMembershipStatusRequestType Request, UpdateConceptValueSetMembershipStatusResponseType Response)
  {
    boolean erfolg = true;

    
    CodeSystemEntityVersion csev = Request.getCodeSystemEntityVersion();
    if(csev != null){

        if(csev.getConceptValueSetMemberships().size() > 1){

            Response.getReturnInfos().setMessage("Es darf nur genau ein ConceptValueSetMembership beinhaltet sein!");
            erfolg = false;
        }
    }
    else
    {
        Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
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
