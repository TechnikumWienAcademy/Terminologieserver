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
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Set;

/**
 * 3.2.20 CHECKED
 * TODO Javadoc
 * @author Mathias Aschhoff
 */
public class UpdateCodeSystemVersionStatus
{

  final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateCodeSystemVersionStatusResponseType UpdateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter)
  {
    logger.info("====== UpdateCodeSystemVersionStatus started ======");
    
    UpdateCodeSystemVersionStatusResponseType response = new UpdateCodeSystemVersionStatusResponseType();
    response.setReturnInfos(new ReturnType());

    // Check login    
    //3.2.17 added second check
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
        return response;
    }

    //Check parameters
    if (validateParameter(parameter, response) == false)
    {
      return response; // Parameter check failed
    }
    
    try
    {
      // Reading code system version from parameters     
      CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

      // Opening hibernate session
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        // Changing status and saving in DB
        CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, csv.getVersionId());
        csv_db.setStatus(csv.getStatus());
        hb_session.update(csv_db);
                
        LastChangeHelper.updateLastChangeDate(true, csv.getVersionId(),hb_session);
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus', Hibernate: " + e.getLocalizedMessage());

            logger.error("Hibernate-error at 'UpdateCodeSystemVersionStatus': " + e.getLocalizedMessage());
      }
      finally
      {
          if(hb_session.isOpen())
            hb_session.close();
      }
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("UpdateCodeSystemVersionStatus erfolgreich");
    }
    catch (Exception e)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus': " + e.getLocalizedMessage());

      logger.error("Error at 'UpdateCodeSystemVersionStatus': " + e.getLocalizedMessage());
    }
    
    return response;
  }

  private boolean validateParameter(UpdateCodeSystemVersionStatusRequestType Request, UpdateCodeSystemVersionStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem == null)
    {
        Response.getReturnInfos().setMessage("CodeSystem darf nicht NULL sein!");
        erfolg = false;
        Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        return erfolg;
    }

    Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
    if (csvSet != null)
    {
      if (csvSet.size() > 1)
      {
            Response.getReturnInfos().setMessage("Die CodeSystem-Version-Liste darf maximal einen Eintrag haben!");
            erfolg = false;
      }
      else if (csvSet.size() == 1)
      {
            CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

        if (csv.getVersionId() == null || csv.getVersionId() == 0)
        {
            Response.getReturnInfos().setMessage("Es muss eine ID (>0) f�r die CodeSystem-Version angegeben werden!");
            erfolg = false;
        }
        if (csv.getStatus() == null)
        {
            Response.getReturnInfos().setMessage("Es muss ein Status f�r die CodeSystem-Version angegeben werden!");
            erfolg = false;
        }
      }
    }
    else
    {
        Response.getReturnInfos().setMessage("Die CodeSystem-Version-Liste muss mindestens einen Eintrag haben!");
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
