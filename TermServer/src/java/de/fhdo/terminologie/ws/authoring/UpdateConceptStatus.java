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
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Set;

/**
 * TODO Javadoc
 * 3.2.20 Checked
 * @author Mathias Aschhoff
 */
public class UpdateConceptStatus
{

  final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateConceptStatusResponseType UpdateConceptStatus(UpdateConceptStatusRequestType parameter)
  {
    if (logger.isInfoEnabled())    
      logger.info("====== UpdateConceptStatus started ======");    

    UpdateConceptStatusResponseType response = new UpdateConceptStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Checking parameters
    if (validateParameter(parameter, response) == false)    
      return response; // Parameter check failed
    
    // Checking login   
    if (parameter != null){
        if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
            return response;    
        }
    }

    try{
      CodeSystemEntity        cse  = parameter.getCodeSystemEntity();
      Long csvId = parameter.getCodeSystemVersionId();
      CodeSystemEntityVersion csev = (CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0];

      // Opening hibernate session
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        CodeSystemEntityVersion csev_db = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, csev.getVersionId());
        csev_db.setStatus(csev.getStatus());        
        hb_session.update(csev_db);
        
        LastChangeHelper.updateLastChangeDate(true, csvId,hb_session);
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
          if(!hb_session.getTransaction().wasRolledBack())
             hb_session.getTransaction().rollback();
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + e.getLocalizedMessage());

        logger.error("Error at 'UpdateConceptStatus'-Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
          if(hb_session.isOpen())
            hb_session.close();
      }
      if (true) {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Status erfolgreich geändert.");
        }
      
    }
    catch (Exception e)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + e.getLocalizedMessage());

      logger.error("Error at 'UpdateConceptStatus': " + e.getLocalizedMessage());
    }
    return response;
  }

  private boolean validateParameter(UpdateConceptStatusRequestType Request, UpdateConceptStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystemEntity cse = Request.getCodeSystemEntity();
    if (cse == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntity darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      Set<CodeSystemEntityVersion> csevSet = cse.getCodeSystemEntityVersions();
      if (csevSet != null)
      {
        if (csevSet.size() > 1)
        {
          Response.getReturnInfos().setMessage("Die CodeSystemEntity-Version-Liste darf maximal einen Eintrag haben!");
          erfolg = false;
        }
        else if (csevSet.size() == 1)
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csevSet.toArray()[0];

          if (csev.getVersionId() == null || csev.getVersionId() == 0)
          {
            Response.getReturnInfos().setMessage(
              "Es muss eine ID für die CodeSystemEntity-Version angegeben werden!");
            erfolg = false;
          }
        }

      }
      else
      {
        Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
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
