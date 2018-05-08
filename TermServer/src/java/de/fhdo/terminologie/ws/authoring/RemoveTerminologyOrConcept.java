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

import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.types.DeleteInfo.Type;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class RemoveTerminologyOrConcept
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter)
  {
    return RemoveTerminologyOrConcept(parameter, null);
  }

  /**
   * Entfernt Konzepte aus einem Value Set
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== RemoveEntity gestartet ======");
    
    // Return-Informationen anlegen
    RemoveTerminologyOrConceptResponseType response = new RemoveTerminologyOrConceptResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)    
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
        return response;
    }

    String result = "";
    Type t = parameter.getDeleteInfo().getType();

    switch (t) {
        case CODE_SYSTEM:
                 
                 result = DeleteTermHelperWS.deleteCS_CSV(true, parameter.getDeleteInfo().getCodeSystem().getId(), null);
                 break;
        case CODE_SYSTEM_VERSION:
                 result = DeleteTermHelperWS.deleteCS_CSV(true, parameter.getDeleteInfo().getCodeSystem().getId(), parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
                 break;
        case VALUE_SET:
                 result = DeleteTermHelperWS.deleteVS_VSV(true, parameter.getDeleteInfo().getValueSet().getId(), null);
                 break;
        case VALUE_SET_VERSION:
                 result = DeleteTermHelperWS.deleteVS_VSV(true, parameter.getDeleteInfo().getValueSet().getId(), parameter.getDeleteInfo().getValueSet().getValueSetVersions().iterator().next().getVersionId());
                 break;
        case CODE_SYSTEM_ENTITY_VERSION:
                 result = DeleteTermHelperWS.deleteCSEV(parameter.getDeleteInfo().getCodeSystemEntityVersion().getVersionId(),parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
                 break;
        default: 
                 result = "";
                 break;
    }
    
    if(result.equals("") || result.contains("An Error occured:")){ //Error
    
        response.getReturnInfos().setMessage(result);
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    
    }else{//Success
    
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage(result);
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
  private boolean validateParameter(RemoveTerminologyOrConceptRequestType Request, RemoveTerminologyOrConceptResponseType Response)
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
        if (Request.getDeleteInfo() == null || Request.getLogin() == null)
        {
          sErrorMessage = "Weder DeleteInfo noch Login dürfen null sein!";
          erfolg = false;
        }
        else
        {
          if(erfolg){
              if(Request.getDeleteInfo().getCodeSystem() == null && 
                 Request.getDeleteInfo().getValueSet() == null && 
                 Request.getDeleteInfo().getCodeSystemEntityVersion() == null){
                  
                  sErrorMessage = "Entweder CodeSystem, ValueSet oder CodeSystemEntityVersion dürfen nicht null sein!";
                  erfolg = false;
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
