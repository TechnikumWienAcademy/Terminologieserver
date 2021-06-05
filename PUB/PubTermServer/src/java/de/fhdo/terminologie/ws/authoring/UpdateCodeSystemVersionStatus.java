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
 * @author Mathias Aschhoff
 */
public class UpdateCodeSystemVersionStatus{

    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public UpdateCodeSystemVersionStatusResponseType UpdateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter){
        LOGGER.info("+++++ UpdateCodeSystemVersionStatus started +++++");
    
        UpdateCodeSystemVersionStatusResponseType response = new UpdateCodeSystemVersionStatusResponseType();
        response.setReturnInfos(new ReturnType());

        // Check login
        if (parameter != null)
            if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- UpdateCodeSystemVersionStatus finished (001) -----");
                return response;
            }

        //Check parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- UpdateCodeSystemVersionStatus finished (002) -----");
            return response; // Parameter check failed
        }
    
        if(parameter!=null)
            try{
                // Reading code system version from parameters     
                CodeSystemVersion CSversion = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

                org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try{
                    // Changing status and saving in DB
                    CodeSystemVersion CSV_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, CSversion.getVersionId());
                    if(CSV_db != null){
                        CSV_db.setStatus(CSversion.getStatus());
                        hb_session.update(CSV_db);
                    
                        LastChangeHelper.updateLastChangeDate(true, CSversion.getVersionId(), hb_session);
                        if(!hb_session.getTransaction().wasCommitted())
                            hb_session.getTransaction().commit();
                    }
                }
                catch (Exception ex){
                    LOGGER.error("Error [0121]", ex);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus', Hibernate: " + ex.getLocalizedMessage());
                    
                    try{
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                    }
                    catch(Exception e){
                        LOGGER.error("Error [0122]: Rollback failed", e);
                    }
                }
                finally{
                    if(hb_session.isOpen())
                      hb_session.close();
                }
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("UpdateCodeSystemVersionStatus erfolgreich");
            }
            catch (Exception ex){
                LOGGER.error("Error [0123]", ex);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus': " + ex.getLocalizedMessage());
            }
        
        LOGGER.info("----- UpdateCodeSystemVersionStatus finished (003) -----");
        return response;
    }

    private boolean validateParameter(UpdateCodeSystemVersionStatusRequestType Request, UpdateCodeSystemVersionStatusResponseType Response){
        boolean passed = true;

        CodeSystem codeSystem = Request.getCodeSystem();
        if (codeSystem == null){
            Response.getReturnInfos().setMessage("CodeSystem darf nicht null sein.");
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            passed = false;
            return passed;
        }

        Set<CodeSystemVersion> CSversionSet = codeSystem.getCodeSystemVersions();
        if (CSversionSet != null){
            if (CSversionSet.size() > 1){
                Response.getReturnInfos().setMessage("Die CodeSystem-Version-Liste darf maximal einen Eintrag haben.");
                passed = false;
            }
            else if (CSversionSet.size() == 1){
                CodeSystemVersion CSversion = (CodeSystemVersion) CSversionSet.toArray()[0];

                if (CSversion.getVersionId() == null || CSversion.getVersionId() == 0){
                    Response.getReturnInfos().setMessage("Es muss eine ID (>0) für die CodeSystem-Version angegeben werden.");
                    passed = false;
                }
                if (CSversion.getStatus() == null){
                    Response.getReturnInfos().setMessage("Es muss ein Status für die CodeSystem-Version angegeben werden.");
                    passed = false;
                }
            }
        }
        else{
            Response.getReturnInfos().setMessage("Die CodeSystem-Version-Liste muss mindestens einen Eintrag haben.");
            passed = false;
        }

        if (passed == false){
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        return passed;
    }
}