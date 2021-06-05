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
 * @author Mathias Aschhoff
 */
public class UpdateConceptStatus{

    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public UpdateConceptStatusResponseType UpdateConceptStatus(UpdateConceptStatusRequestType parameter){
        LOGGER.info("+++++ UpdateConceptStatus started +++++");

        UpdateConceptStatusResponseType response = new UpdateConceptStatusResponseType();
        response.setReturnInfos(new ReturnType());

        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- UpdateConceptStatus finished (001) -----");
            return response; // Faulty parameters
        }
    
        // Checking login   
        if (parameter != null){
            if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- UpdateConceptStatus finished (002) -----");
                return response;    
            }

            try{
                CodeSystemEntity CSentity  = parameter.getCodeSystemEntity();
                Long CSVid = parameter.getCodeSystemVersionId();
                CodeSystemEntityVersion CSentityVersion = (CodeSystemEntityVersion) CSentity.getCodeSystemEntityVersions().toArray()[0];

                org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try{
                    CodeSystemEntityVersion CSEV_db = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, CSentityVersion.getVersionId());
                    CSEV_db.setStatus(CSentityVersion.getStatus());        
                    
                    hb_session.update(CSEV_db);
                    LastChangeHelper.updateLastChangeDate(true, CSVid,hb_session);
                    if(!hb_session.getTransaction().wasCommitted())
                        hb_session.getTransaction().commit();
                }
                catch (Exception ex){
                    LOGGER.error("Error [0124]", ex);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + ex.getLocalizedMessage());
                    try{
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                    }
                    catch(Exception e){
                        LOGGER.error("Error [0125]: Rollback failed.", e);
                    }
                }
                finally{
                    if(hb_session.isOpen())
                      hb_session.close();
                }
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("Status erfolgreich geändert.");
            }
            catch (Exception ex){
                LOGGER.error("Error [0126]", ex);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + ex.getLocalizedMessage());
            }
        }
        LOGGER.info("----- UpdateConceptStatus finished (003) -----");
    
        return response;
    }

    private boolean validateParameter(UpdateConceptStatusRequestType Request, UpdateConceptStatusResponseType Response){
        boolean passed = true;

        CodeSystemEntity CSentity = Request.getCodeSystemEntity();
        if (CSentity == null){
            Response.getReturnInfos().setMessage("CodeSystemEntity darf nicht NULL sein!");
            passed = false;
        }
        else{
            Set<CodeSystemEntityVersion> CSEVset = CSentity.getCodeSystemEntityVersions();
            if (CSEVset != null){
                if (CSEVset.size() > 1){
                    Response.getReturnInfos().setMessage("Die CodeSystemEntity-Version-Liste darf maximal einen Eintrag haben.");
                    passed = false;
                }
                else if (CSEVset.size() == 1){
                    CodeSystemEntityVersion CSEV = (CodeSystemEntityVersion) CSEVset.toArray()[0];

                    if (CSEV.getVersionId() == null || CSEV.getVersionId() == 0){
                        Response.getReturnInfos().setMessage("Es muss eine ID für die CodeSystemEntity-Version angegeben werden.");
                        passed = false;
                    }
                }
            }
            else{
                Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
                passed = false;
            }
        }

        if (!passed){
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return passed;
    }
}