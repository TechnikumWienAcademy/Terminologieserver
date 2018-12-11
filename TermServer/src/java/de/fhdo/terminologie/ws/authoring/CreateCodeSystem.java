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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateCodeSystem
{
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Calls CreateCodeSystem(parameter, null).
     * @param parameter the parameter for the subsequent call
     * @return the response of the subsequent call
     */
    public CreateCodeSystemResponseType CreateCodeSystem(CreateCodeSystemRequestType parameter){
        return CreateCodeSystem(parameter, null);
    }

    /**
    * Creates a new code-system with the given parameters.
    * @param parameter the parameter for the webservice call
    * @return the response of the webservice call
    */
    public CreateCodeSystemResponseType CreateCodeSystem(CreateCodeSystemRequestType parameter, org.hibernate.Session session){
        LOGGER.info("+++++ CreateCodeSystem started +++++");

        boolean createHibernateSession = (session == null);

        // Creating return information
        CreateCodeSystemResponseType response = new CreateCodeSystemResponseType();
        response.setReturnInfos(new ReturnType());

        // Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- CreateCodeSystem finished (001) -----");
            return response; // Faulty parameters
        }
        
        // Check login (Has to be done for every webservice)    
        //  3.2.17 added second check
        if (parameter != null && !parameter.isLoginAlreadyChecked())
            if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- CreateCodeSystem finished (002) -----");
                return response; // Login failed
            }

        CodeSystem cs_return = null;
        CodeSystemVersion csv_return = null;
        org.hibernate.Session hb_session = null;
        try{
            // The returned code-system and code-system version contain only the
            // code-system-ID, code-system-currentVersionID and the code-system-version-versionID.
            // Other information like name, source, etc are not returned.
            cs_return = new CodeSystem();
            csv_return = new CodeSystemVersion();

            // Opening hibernate session
            hb_session = null;

            if (createHibernateSession){
              hb_session = HibernateUtil.getSessionFactory().openSession();
              hb_session.getTransaction().begin();
            }
            else
              hb_session = session;
            
            // Preparing code-system and code-system-version for saving
            CodeSystem cs_parameter = parameter.getCodeSystem();
            CodeSystemVersion csv_parameter = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

            // Adjusting code-system
            cs_parameter.setInsertTimestamp(new java.util.Date());
            cs_parameter.setCurrentVersionId(null);

            // Adjusting code-system-version
            csv_parameter.setCodeSystemVersionEntityMemberships(null);
            csv_parameter.setLicencedUsers(null);
            csv_parameter.setInsertTimestamp(new java.util.Date());
      
            if(csv_parameter.getStatus() == null)
              csv_parameter.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
            csv_parameter.setStatusDate(new java.util.Date());

            if (csv_parameter.getUnderLicence() == null)
              csv_parameter.setUnderLicence(false);

            csv_parameter.setPreviousVersionId(null);

            // LicenceTypes
            Set<LicenceType> licenceTypes = csv_parameter.getLicenceTypes();
            csv_parameter.setLicenceTypes(null);

            // Saving code-system in the database
            // Checking whether the code-system exists (meaning it is a new version) or not
            CodeSystem cs_db = null;
            if (cs_parameter.getId() != null && cs_parameter.getId() > 0){
                // Code-system exists
                cs_db = (CodeSystem) hb_session.get(CodeSystem.class, cs_parameter.getId());

                // Saving previous version
                if (cs_db != null)
                  csv_parameter.setPreviousVersionId(cs_db.getCurrentVersionId());
            }
            // Code-system does not exist
            if (cs_db == null){
                cs_parameter.setCodeSystemVersions(null);

                // Saving code-system in database so that it gets an ID
                hb_session.save(cs_parameter);

                // Code-system-version gets a code-system with the new ID
                csv_parameter.setCodeSystem(new CodeSystem());
                csv_parameter.getCodeSystem().setId(cs_parameter.getId());

                // Saving code-system-version in database so that it gets an ID
                hb_session.save(csv_parameter);

                // Setting current-version of the code-system and saving again
                cs_parameter.setCurrentVersionId(csv_parameter.getVersionId());
                hb_session.update(cs_parameter);

                // Setting response (with new ID)
                cs_return.setId(cs_parameter.getId());
                cs_return.setCurrentVersionId(cs_parameter.getCurrentVersionId());
                csv_return.setVersionId(csv_parameter.getVersionId());
            }
            else{ // Code-system exists, creating version
                csv_parameter.setCodeSystem(new CodeSystem());
                csv_parameter.getCodeSystem().setId(cs_db.getId());
                csv_parameter.setValidityRange(238l);
          
                // Saving code-system-version in database to get a new ID
                hb_session.save(csv_parameter);

                // Updating code-system with currentVersion and saving
                cs_db.setCurrentVersionId(csv_parameter.getVersionId());
                hb_session.update(cs_db);

                // Setting response (with new ID)
                cs_return.setId(cs_db.getId());
                cs_return.setCurrentVersionId(cs_db.getCurrentVersionId());
                csv_return.setVersionId(csv_parameter.getVersionId());
                csv_return.setName(csv_parameter.getName());
            }

            // Saving LicenceTypes
            if (licenceTypes != null && csv_return.getVersionId() > 0){
                Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
                while (itLicenceType.hasNext()){
                    LicenceType lt = itLicenceType.next();
                    lt.setCodeSystemVersion(new CodeSystemVersion());
                    lt.getCodeSystemVersion().setVersionId(csv_return.getVersionId());
                    lt.setLicencedUsers(null);
                    hb_session.save(lt);
                }
            }
        }
        catch (Exception e){
            // Forwarding error message to the caller
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'CreateCodeSystems', Hibernate: " + e.getLocalizedMessage());

            LOGGER.error("Error at 'CreateCodeSystems', Hibernate: " + e.getLocalizedMessage());
        }
        finally{
            // Closing transaction
            if (createHibernateSession){
                if (cs_return.getId() > 0 && csv_return.getVersionId() > 0 && !hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
                else{
                    LOGGER.warn("Changes not successful, cs_return.id: "
                    + cs_return.getId() + ", csv_return.versionId: " + csv_return.getVersionId());

                    if(!hb_session.getTransaction().wasRolledBack())
                        hb_session.getTransaction().rollback();
                }
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }

        // Creating response
        cs_return.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
        cs_return.getCodeSystemVersions().add(csv_return);
        cs_return.setName(parameter.getCodeSystem().getName());
        cs_return.setAutoRelease(parameter.getCodeSystem().getAutoRelease());
        response.setCodeSystem(cs_return);

        if (cs_return.getId() != null && cs_return.getId() > 0 && csv_return.getVersionId() != null && csv_return.getVersionId() > 0){
            // Forwarding status to the caller
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("CodeSystem erfolgreich erstellt");
        }
        LOGGER.info("----- CreateCodeSystem finished (003) -----");
        return response;
    }

    /**
     * Checks the parameters via cross-reference.
     * @param Request
     * @param Response
     * @return true if the parameters are okay, else false
     */
    private boolean validateParameter(CreateCodeSystemRequestType Request, CreateCodeSystemResponseType Response){
        LOGGER.info("+++++ validateParameter started +++++");
        boolean erfolg = true;
        String sErrorMessage = "";

        if (Response == null){
            sErrorMessage = "Kein Responseparameter vorhanden!";
            erfolg = false;
        }
        if (Request == null){
            sErrorMessage = "Kein Requestparameter angegeben!";
            erfolg = false;
        }

        if (erfolg){
            CodeSystem cs = Request.getCodeSystem();
            if (cs == null){
                sErrorMessage = "CodeSystem darf nicht NULL sein!";
                erfolg = false;
            }
            else{
                if (cs.getName() == null || cs.getName().isEmpty()){
                    sErrorMessage = "Es muss ein Name für das CodeSystem angegeben werden!";
                    erfolg = false;
                }
                Set<CodeSystemVersion> csvSet = cs.getCodeSystemVersions();
                if (csvSet == null || csvSet.isEmpty() || csvSet.size() > 1){
                    sErrorMessage = "Es muss genau eine CodeSystem-Version angegeben werden!";
                    erfolg = false;
                }
                else{
                    CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];
                    if (csv == null){
                        sErrorMessage = "CodeSystem-Version fehlerhaft!";
                        erfolg = false;
                    }
                    else{
                        if (csv.getName() == null || csv.getName().isEmpty()){
                            sErrorMessage = "Es muss ein Name für die CodeSystem-Version angegeben sein!";
                            erfolg = false;
                        }
                        Set<LicenceType> ltSet = csv.getLicenceTypes();
                        if (ltSet != null){
                            Iterator<LicenceType> itLt = ltSet.iterator();
                            LicenceType lt;
                            while (itLt.hasNext()){
                                if (itLt.next().getTypeTxt().isEmpty()){
                                    sErrorMessage = "Zumindest ein Lizenztyp hat keinen Typtext!";
                                    erfolg = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (erfolg == false){
            Response.getReturnInfos().setMessage(sErrorMessage);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        LOGGER.info("----- validateParameter finished (001) -----");
        return erfolg;
    }
}