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
 * 3.2.26 complete check
 * @author Philipp Urbauer
 */
public class RemoveTerminologyOrConcept{
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Calls the RemoveTerminologyOrConcept(parameter, null) function.
     * @param parameter the parameter which is handed over to the called function
     * @return the return value of the called function
     */
    public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter){
        return RemoveTerminologyOrConcept(parameter, null);
    }

    /**
     * Removes a terminology or concept by calling the DeleteTermHelper.
     * @param parameter containing the info about what to delete
     * @param session the hibernate session, from which to delete
     * @return a return response with info about the success or failure of the function
     */
    public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, org.hibernate.Session session){
        LOGGER.info("+++++ RemoveTerminologyOrConcept started +++++");
        
        //Creating return information
        RemoveTerminologyOrConceptResponseType response = new RemoveTerminologyOrConceptResponseType();
        response.setReturnInfos(new ReturnType());

        //Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- RemoveTerminologyOrConcept finished (001) -----");
            return response; //Faulty parameters
        }
    
        //Checking login
        if (parameter != null)
            if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- RemoveTerminologyOrConcept finished (002) -----");
                return response;
            }

        String result;
        Type typeToDelete = parameter.getDeleteInfo().getType();

        //Execute delete
        switch (typeToDelete) {
        case CODE_SYSTEM: result = DeleteTermHelperWS.deleteCS_CSV(true, parameter.getDeleteInfo().getCodeSystem().getId(), null);
        break;
        case CODE_SYSTEM_VERSION: result = DeleteTermHelperWS.deleteCS_CSV(true, parameter.getDeleteInfo().getCodeSystem().getId(), parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
        break;
        case VALUE_SET: result = DeleteTermHelperWS.deleteVS_VSV(true, parameter.getDeleteInfo().getValueSet().getId(), null);
        break;
        case VALUE_SET_VERSION: result = DeleteTermHelperWS.deleteVS_VSV(true, parameter.getDeleteInfo().getValueSet().getId(), parameter.getDeleteInfo().getValueSet().getValueSetVersions().iterator().next().getVersionId());
        break;
        case CODE_SYSTEM_ENTITY_VERSION: result = DeleteTermHelperWS.deleteCSEV(parameter.getDeleteInfo().getCodeSystemEntityVersion().getVersionId(),parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
        break;
        default: result = "";
        break;
        }
    
        if(result.equals("") || result.contains("Ein Fehler ist aufgetreten")){   
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE); 
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setMessage(result);
        }
        else{
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setMessage(result);
        }
    
        LOGGER.info("----- RemoveTerminologyOrConcept finished (003) -----");
        return response;
    }

  /**
   * Checks if all parameters are given to execute the deletion of a terminology or concept.
   * @param Request the parameters which will be checked
   * @param Response whether or not the check was okay
   * @return true if all parameters are okay, false if any are missing
   */
    private boolean validateParameter(RemoveTerminologyOrConceptRequestType Request, RemoveTerminologyOrConceptResponseType Response){
        boolean erfolg = true;
        String sErrorMessage = "";

        if (Request == null){
            erfolg = false;
            sErrorMessage = "Requestparameter darf nicht null sein!";
        }
        else{
            if (Request.getDeleteInfo() == null || Request.getLogin() == null){
                erfolg = false;
                sErrorMessage = "DeleteInfo und Login dürfen nicht null sein!";
            }
            else if(Request.getDeleteInfo().getCodeSystem() == null 
            && Request.getDeleteInfo().getValueSet() == null 
            && Request.getDeleteInfo().getCodeSystemEntityVersion() == null){  
                erfolg = false;
                sErrorMessage = "CodeSystem, ValueSet oder CodeSystemEntityVersion dürfen nicht null sein!";
            }
            else if(Request.getDeleteInfo().getType() == null){
                erfolg = false;
                sErrorMessage = "DeleteType darf nicht null sein.";
            }
        }

        if (!erfolg){
            Response.getReturnInfos().setMessage(sErrorMessage);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        return erfolg;
    }
}
