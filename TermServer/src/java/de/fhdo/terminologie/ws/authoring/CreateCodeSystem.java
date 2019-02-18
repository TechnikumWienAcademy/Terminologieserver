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
public class CreateCodeSystem{
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
     * @param session
    * @return the response of the webservice call
    */
    public CreateCodeSystemResponseType CreateCodeSystem(CreateCodeSystemRequestType parameter, org.hibernate.Session session){
        LOGGER.info("+++++ CreateCodeSystem started +++++");

        // Creating return information
        CreateCodeSystemResponseType response = new CreateCodeSystemResponseType();
        response.setReturnInfos(new ReturnType());

        // Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- CreateCodeSystem finished (001) -----");
            return response; // Faulty parameters
        }
        
        // Check login (Has to be done for every webservice)
        if (parameter != null)
            if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- CreateCodeSystem finished (002) -----");
                return response; // Login failed
            }

        CodeSystem cs_return = null;
        CodeSystemVersion csv_return = null;
        org.hibernate.Session hb_session = null;
        boolean hibernateSessionCreated = false;
        try{
            // The returned code-system and code-system version contain only the
            // code-system-ID, code-system-currentVersionID and the code-system-version-versionID.
            // Other information like name, source, etc are not returned.
            cs_return = new CodeSystem();
            csv_return = new CodeSystemVersion();

            // Opening hibernate session
            hb_session = null;

            if (session == null){
                hibernateSessionCreated = true;
                hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();
            }
            else
                hb_session = session;
            
            // Preparing code-system and code-system-version for saving
            CodeSystem CS;
            if(parameter!=null)
                CS = parameter.getCodeSystem();
            else{
                //Creates a null response through validateParameter TODO durch eigene NULL response methode ersetzen
                LOGGER.info("----- CreateCodeSystem finished (004) -----");
                this.validateParameter(null, response);
                return response;
            }
            CodeSystemVersion CSversion = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

            // Adjusting code-system
            CS.setInsertTimestamp(new java.util.Date());
            CS.setCurrentVersionId(null);

            // Adjusting code-system-version
            CSversion.setCodeSystemVersionEntityMemberships(null);
            CSversion.setLicencedUsers(null);
            CSversion.setInsertTimestamp(new java.util.Date());
      
            if(CSversion.getStatus() == null)
                CSversion.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
            CSversion.setStatusDate(new java.util.Date());

            if (CSversion.getUnderLicence() == null)
                CSversion.setUnderLicence(false);

            CSversion.setPreviousVersionId(null);

            // LicenceTypes
            Set<LicenceType> licenceTypes = CSversion.getLicenceTypes();
            CSversion.setLicenceTypes(null);

            // Saving code-system in the database
            // Checking whether the code-system exists (meaning it is a new version) or not
            CodeSystem CSdatabase = null;
            if (CS.getId() != null && CS.getId() > 0){
                // Code-system exists
                CSdatabase = (CodeSystem) hb_session.get(CodeSystem.class, CS.getId());

                // Saving previous version
                if (CSdatabase != null)
                  CSversion.setPreviousVersionId(CSdatabase.getCurrentVersionId());
            }
            // Code-system does not exist
            if (CSdatabase == null){
                CS.setCodeSystemVersions(null);

                // Saving code-system in database so that it gets an ID
                hb_session.save(CS);

                // Code-system-version gets a code-system with the new ID
                CSversion.setCodeSystem(new CodeSystem());
                CSversion.getCodeSystem().setId(CS.getId());

                // Saving code-system-version in database so that it gets an ID
                hb_session.save(CSversion);

                // Setting current-version of the code-system and saving again
                CS.setCurrentVersionId(CSversion.getVersionId());
                hb_session.update(CS);

                // Setting response (with new ID)
                cs_return.setId(CS.getId());
                cs_return.setCurrentVersionId(CS.getCurrentVersionId());
                csv_return.setVersionId(CSversion.getVersionId());
            }
            else{ // Code-system exists, creating version
                CSversion.setCodeSystem(new CodeSystem());
                CSversion.getCodeSystem().setId(CSdatabase.getId());
                CSversion.setValidityRange(238l);
          
                // Saving code-system-version in database to get a new ID
                hb_session.save(CSversion);

                // Updating code-system with currentVersion and saving
                CSdatabase.setCurrentVersionId(CSversion.getVersionId());
                hb_session.update(CSdatabase);

                // Setting response (with new ID)
                cs_return.setId(CSdatabase.getId());
                cs_return.setCurrentVersionId(CSdatabase.getCurrentVersionId());
                csv_return.setVersionId(CSversion.getVersionId());
                csv_return.setName(CSversion.getName());
            }

            // Saving LicenceTypes
            if (licenceTypes != null && csv_return.getVersionId() > 0){
                Iterator<LicenceType> licenceTypeIterator = licenceTypes.iterator();
                while (licenceTypeIterator.hasNext()){
                    LicenceType licenceType = licenceTypeIterator.next();
                    licenceType.setCodeSystemVersion(new CodeSystemVersion());
                    licenceType.getCodeSystemVersion().setVersionId(csv_return.getVersionId());
                    licenceType.setLicencedUsers(null);
                    hb_session.save(licenceType);
                }
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0090]: " + e.getLocalizedMessage());
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'CreateCodeSystems', Hibernate: " + e.getLocalizedMessage());
        }
        finally{
            // Closing transaction
            if(hb_session != null){
                if(cs_return != null && csv_return != null && cs_return.getId() > 0 && csv_return.getVersionId() > 0){
                    if(!hb_session.getTransaction().wasCommitted())
                        hb_session.getTransaction().commit();
                }
                else{
                    if(cs_return != null && csv_return != null)
                        LOGGER.error("Error [0091]: Changes not successful, cs_return.id: " + cs_return.getId() + ", csv_return.versionId: " + csv_return.getVersionId());
                    else if(cs_return != null)
                        LOGGER.error("Error [0092]: Changes not successful, cs_return.id: " + cs_return.getId() + ", csv_return: null");
                    else if(csv_return != null)
                        LOGGER.error("Error [0093]: Changes not successful, cs_return: null, csv_return.versionId: " + csv_return.getVersionId());
                    
                    try{
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                    }
                    catch(Exception e){
                        LOGGER.error("Error [0094]: " + e.getLocalizedMessage());
                    }
                }
                if(hibernateSessionCreated && hb_session.isOpen())
                    hb_session.close();
            }
        }

        // Creating response
        if(cs_return!=null){
            cs_return.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
            cs_return.getCodeSystemVersions().add(csv_return);
            if(parameter!=null)
                cs_return.setName(parameter.getCodeSystem().getName());
            if(parameter!=null)
                cs_return.setAutoRelease(parameter.getCodeSystem().getAutoRelease());
        }
        response.setCodeSystem(cs_return);

        if (cs_return != null && cs_return.getId() != null && cs_return.getId() > 0 && csv_return != null && csv_return.getVersionId() != null && csv_return.getVersionId() > 0){
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
        boolean passed = true;
        String errorMessage = "";

        if (Response == null){
            errorMessage = "Kein Responseparameter vorhanden!";
            passed = false;
        }
        if (Request == null){
            errorMessage = "Kein Requestparameter angegeben!";
            passed = false;
        }

        if (passed){
            CodeSystem CS = Request.getCodeSystem();
            if (CS == null){
                errorMessage = "CodeSystem darf nicht NULL sein!";
                passed = false;
            }
            else{
                if (CS.getName() == null || CS.getName().isEmpty()){
                    errorMessage = "Es muss ein Name für das CodeSystem angegeben werden!";
                    passed = false;
                }
                Set<CodeSystemVersion> csvSet = CS.getCodeSystemVersions();
                if (csvSet == null || csvSet.isEmpty() || csvSet.size() > 1){
                    errorMessage = "Es muss genau eine CodeSystem-Version angegeben werden!";
                    passed = false;
                }
                else{
                    CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];
                    if (csv == null){
                        errorMessage = "CodeSystem-Version fehlerhaft!";
                        passed = false;
                    }
                    else{
                        if (csv.getName() == null || csv.getName().isEmpty()){
                            errorMessage = "Es muss ein Name für die CodeSystem-Version angegeben sein!";
                            passed = false;
                        }
                        Set<LicenceType> ltSet = csv.getLicenceTypes();
                        if (ltSet != null){
                            Iterator<LicenceType> itLt = ltSet.iterator();
                            LicenceType lt;
                            while (itLt.hasNext()){
                                if (itLt.next().getTypeTxt().isEmpty()){
                                    errorMessage = "Zumindest ein Lizenztyp hat keinen Typtext!";
                                    passed = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!passed && Response!=null){
            Response.getReturnInfos().setMessage(errorMessage);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        LOGGER.info("----- validateParameter finished (001) -----");
        return passed;
    }
}