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
 */package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import org.hibernate.HibernateException;

/**
 *
 * @author Robert Mützner
 */
public class CreateConceptAssociation {

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public CreateConceptAssociationResponseType CreateConceptAssociation(CreateConceptAssociationRequestType parameter) {
        return CreateConceptAssociation(parameter, null);
    }

    /**
     * Stellt eine Beziehung zwischen 2 Konzepten her.
     * Die erstegenannte Verbindung ist hier immer die "Left-ID".
     * 
     * @param parameter
     * @param session
     * @return 
     */
    public CreateConceptAssociationResponseType CreateConceptAssociation(CreateConceptAssociationRequestType parameter, org.hibernate.Session session) {
        LOGGER.info("+++++ CreateConceptAssociation started +++++");
        
        //Creating response
        CreateConceptAssociationResponseType response = new CreateConceptAssociationResponseType();
        response.setReturnInfos(new ReturnType());
        
        //Checking parameters
        if (validateParameter(parameter, response) == false) {
            LOGGER.info("----- CreateConceptAssociation finished (001) -----");
            return response; //Faulty parameters
        }

        //Login, does every webservice
        boolean loggedIn = false;
        LoginInfoType loginInfoType;
        if (parameter != null && parameter.getLogin() != null) {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin(), session);
            loggedIn = loginInfoType != null;
        }
  
        // TODO Lizenzen prüfen (?)
        
        if (!loggedIn) {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
            LOGGER.info("----- CreateConceptAssociation finished (002) -----");
            return response;
        }

        if(parameter != null)
            try {
                long associationId = 0;

                org.hibernate.Session hb_session;         
                boolean hibernateSessionCreated = false;
                if (session == null) {
                    hibernateSessionCreated = true;
                    hb_session = HibernateUtil.getSessionFactory().openSession();
                    hb_session.getTransaction().begin();
                }
                else
                    hb_session = session;

                CodeSystemEntityVersionAssociation CSEVassoc = parameter.getCodeSystemEntityVersionAssociation();
                long entityVersionId1 = CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
                long entityVersionId2 = CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
                long associationTypeId = CSEVassoc.getAssociationType().getCodeSystemEntityVersionId();

                //Creating relationships anew, so that ONLY IDs are in them
                CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);

                CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(entityVersionId1);

                CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(entityVersionId2);

                CSEVassoc.setAssociationType(null);
                CSEVassoc.setAssociationType(new AssociationType());
                CSEVassoc.getAssociationType().setCodeSystemEntityVersionId(associationTypeId);

                //Creating other attributes
                CSEVassoc.setLeftId(entityVersionId1);  // TODO ist die 1. für uns immer die "links"?
                CSEVassoc.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                CSEVassoc.setStatusDate(new Date());
                CSEVassoc.setInsertTimestamp(new Date());

                try{
                    //Checking whether associationTypeId is an association or not
                    if (hb_session.get(AssociationType.class, associationTypeId) == null) {
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                        response.getReturnInfos().setMessage("Sie müssen eine gültige ID für ein AssociationType angeben. Das Konzept mit der ID '" + associationTypeId + "' ist kein AssociationType!");
                    } 
                    else {
                        hb_session.save(CSEVassoc);
                        associationId = CSEVassoc.getId();
                    }

                    if (associationId > 0) {
                        if(CSEVassoc.getAssociationKind() == 2 && CSEVassoc.getAssociationType().getCodeSystemEntityVersionId().equals(4L)){
                            //Check parentCSEV isLeaf or children are not shown!
                            CodeSystemEntityVersion CSEVparent = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, entityVersionId1);
                            if(CSEVparent.getIsLeaf()){
                                CSEVparent.setIsLeaf(false);
                                hb_session.update(CSEVparent);
                            }
                        }

                        if(hb_session.getTransaction().isActive() && !hb_session.getTransaction().wasCommitted())
                            hb_session.getTransaction().commit();
                    } 
                    else{
                        LOGGER.warn("Changes not successful");
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                    }
                } 
                catch (Exception e) {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler bei 'CreateConceptAssociation', Hibernate: " + e.getLocalizedMessage());
                    LOGGER.error("Error [0107]", e);
                } 
                finally {
                    if (hibernateSessionCreated && hb_session.isOpen()) 
                        hb_session.close();
                }

                //Building response
                if (associationId > 0) {
                    response.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
                    response.getCodeSystemEntityVersionAssociation().setId(CSEVassoc.getId());

                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Beziehung zwischen 2 Konzepten erfolgreich erstellt");
                }
            } 
            catch (HibernateException e) {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'CreateConceptAssociation': " + e.getLocalizedMessage());
                LOGGER.error("Error [0108]: " + e.getLocalizedMessage(), e);
            }
        
        LOGGER.info("----- CreateConceptAssociation finished (003) -----");
        return response;
    }

    /**
     * Prüft die Parameter anhand der Cross-Reference
     * 
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
     */
    private boolean validateParameter(CreateConceptAssociationRequestType Request,
            CreateConceptAssociationResponseType Response) {
        boolean erfolg = true;

        CodeSystemEntityVersionAssociation association = Request.getCodeSystemEntityVersionAssociation();

        if (association == null) {
            Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein!");
            erfolg = false;
        } else {
            if (association.getAssociationType() == null
                    || association.getAssociationType().getCodeSystemEntityVersionId() == 0) {
                Response.getReturnInfos().setMessage(
                        "Es muss ein AssociationType mit einer ID angegeben sein!");
                erfolg = false;
            } else if (association.getAssociationKind() == null || Definitions.isAssociationKindValid(association.getAssociationKind()) == false) {
                Response.getReturnInfos().setMessage(
                        "Es muss ein Association-Kind angegeben sein, mögliche Werte: " + Definitions.readAssociationKinds());
                erfolg = false;
            } else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() == null
                    || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == null
                    || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == 0) {
                Response.getReturnInfos().setMessage(
                        "Es muss eine ID für die 1. CodeSystemEntityVersion angegeben sein!");
                erfolg = false;
            } else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() == null
                    || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == null
                    || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == 0) {
                Response.getReturnInfos().setMessage(
                        "Es muss eine ID für die 2. CodeSystemEntityVersion angegeben sein!");
                erfolg = false;
            }
        }

        if (erfolg == false) {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }
}
