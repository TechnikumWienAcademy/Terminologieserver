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
public class UpdateConceptValueSetMembershipStatus{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public UpdateConceptValueSetMembershipStatusResponseType UpdateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter){
        LOGGER.info("+++++ UpdateConceptValueSetMembershipStatus started +++++");    

        UpdateConceptValueSetMembershipStatusResponseType response = new UpdateConceptValueSetMembershipStatusResponseType();
        response.setReturnInfos(new ReturnType());

        // Check parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- UpdateConceptValueSetMembershipStatus finished (001) -----");
            return response; //Faulty parameters
        }
    
        // Check login (does every webservice)
        if (parameter != null)
            if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false){
                LOGGER.info("----- UpdateConceptValueSetMembershipStatus finished (002) -----");
                return response;    
            }

        if(parameter != null)
            try{
                CodeSystemEntityVersion CSEV = parameter.getCodeSystemEntityVersion();
                ConceptValueSetMembership CVSM = CSEV.getConceptValueSetMemberships().iterator().next();
      
                org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try{
                    ConceptValueSetMembership CVSM_db;
                    if(CVSM.getId() == null){
                        ConceptValueSetMembershipId CVSM_id = new ConceptValueSetMembershipId(
                        CVSM.getCodeSystemEntityVersion().getVersionId(), CVSM.getValueSetVersion().getVersionId());
                        CVSM_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, CVSM_id);
                    }
                    else
                        CVSM_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, CVSM.getId());
                    
                    CVSM_db.setStatus(CVSM.getStatus());
                    if(CVSM.getStatusDate() != null)
                        CVSM_db.setStatusDate(CVSM.getStatusDate());
                    
                    hb_session.update(CVSM_db);
        
                    LastChangeHelper.updateLastChangeDate(false, CVSM_db.getId().getValuesetVersionId(),hb_session);
                    
                    if(!hb_session.getTransaction().wasCommitted())
                        hb_session.getTransaction().commit();
                }
                catch (Exception ex){
                    LOGGER.error("Error [0130]", ex);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptValueSetMembershipStatus': " + ex.getLocalizedMessage());
                    
                    try{
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                    }
                    catch(Exception e){
                        LOGGER.error("Error [0131]: Rollback failed.", e);
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
                LOGGER.error("Error [0132]", ex);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptValueSetMembershipStatus': " + ex.getLocalizedMessage());
            }
        
        LOGGER.info("----- UpdateConceptValueSetMembershipStatus finished (003) -----");
        return response;
    }

    private boolean validateParameter(UpdateConceptValueSetMembershipStatusRequestType Request, UpdateConceptValueSetMembershipStatusResponseType Response){
        boolean passed = true;

        CodeSystemEntityVersion CSEV = Request.getCodeSystemEntityVersion();
        if(CSEV != null)
            if(CSEV.getConceptValueSetMemberships().size() > 1){
                Response.getReturnInfos().setMessage("Es darf nur genau ein ConceptValueSetMembership beinhaltet sein!");
                passed = false;
            }
        else{
            Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
            passed = false;
        }
    
        if (!passed){
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return passed;
    }
}