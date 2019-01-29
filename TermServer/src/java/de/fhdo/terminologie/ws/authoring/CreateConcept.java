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
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.Property;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 3.2.26 checked
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConcept{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Calls CreateConcept(parameter,null).
     * @param parameter the parameters which will be handed down
     * @return the return-value of CreateConcept()
     */
    public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter){
        return CreateConcept(parameter, null);
    }

    /**
     * Creates a single concept and stores it in the database.
     * @param parameter the concept to be created
     * @param session the user's session
     * @return info about the execution of the function
     */
    public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter, org.hibernate.Session session){
        LOGGER.info("+++++ CreateConcept started +++++");

        CreateConceptResponseType response = new CreateConceptResponseType();
        response.setReturnInfos(new ReturnType());
    
        //Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- CreateConcept finished (001) -----");
            return response; //Faulty parameters
        }
   
        LoginType paramLogin = null;
        CodeSystem paramCodeSystem = null;
        CodeSystemEntity paramCodeSystemEntity = null;
        List<Property> paramProperty = null;
        //3.2.17 added TODO aus dem gesamten Code entfernen
        boolean loginAlreadyChecked = false;
            
        if (parameter != null){
            paramLogin = parameter.getLogin();
            paramCodeSystem = parameter.getCodeSystem();
            paramCodeSystemEntity = parameter.getCodeSystemEntity();
            paramProperty = parameter.getProperty();
            //3.2.17
            loginAlreadyChecked = parameter.isLoginAlreadyChecked();
        }

        //3.2.17 added last parameter
        CreateConceptOrAssociationType(response, paramLogin, paramCodeSystem, paramCodeSystemEntity, paramProperty, session, loginAlreadyChecked);
    
        LOGGER.info("----- CreateConcept finished (002) -----");
        return response;
    }

    //3.2.17 added boolean loginAlreadyChecked
    public void CreateConceptOrAssociationType(CreateConceptResponseType response,
          LoginType paramLogin, CodeSystem paramCodeSystem,
          CodeSystemEntity paramCodeSystemEntity,
          List<Property> paramProperty,
          org.hibernate.Session session,
          boolean loginAlreadyChecked){
      
        LOGGER.info("+++++ CreateConceptOrAssociationType started +++++");
        boolean createHibernateSession = (session == null);
        
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        //3.2.17 added second check
        if (paramLogin != null && !loginAlreadyChecked){
            loginInfoType = LoginHelper.getInstance().getLoginInfos(paramLogin, session);
            loggedIn = loginInfoType != null;
        }

        //3.2.17 added first check
        if (!loginAlreadyChecked && loggedIn == false){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
            LOGGER.info("----- CreateConceptOrAssociationType finished (001) -----");
            return;
        }

        try{
            
            org.hibernate.Session hb_session;
            if (createHibernateSession){
                hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();
            }
            else
                hb_session = session;

            //Preparing CS and CS-Version for saving
            long codeSystemVersionId = 0;
            long codeSystemEntityVersionId = 0;

            if (paramCodeSystem != null && paramCodeSystem.getCodeSystemVersions() != null && paramCodeSystem.getCodeSystemVersions().size() > 0)
                codeSystemVersionId = ((CodeSystemVersion) paramCodeSystem.getCodeSystemVersions().toArray()[0]).getVersionId();
      

            try{
                // Creating new entity and entity-version
                CodeSystemEntity entity = new CodeSystemEntity();
                hb_session.save(entity);
        
                CodeSystemEntityVersion entityVersion = (CodeSystemEntityVersion) paramCodeSystemEntity.getCodeSystemEntityVersions().toArray()[0];
                
                CodeSystemConcept concept = null;
                if (entityVersion.getCodeSystemConcepts() != null && entityVersion.getCodeSystemConcepts().size() > 0)
                    concept = (CodeSystemConcept) entityVersion.getCodeSystemConcepts().toArray()[0];

                AssociationType assocType = null;
                if (entityVersion.getAssociationTypes() != null && entityVersion.getAssociationTypes().size() > 0)
                    assocType = (AssociationType) entityVersion.getAssociationTypes().toArray()[0];
                
                if(entityVersion.getStatus() == null)
                    entityVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                
                entityVersion.setStatusDate(new Date());
                entityVersion.setInsertTimestamp(new Date());
                entityVersion.setCodeSystemEntity(entity);
                entityVersion.setCodeSystemConcepts(null);
                entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
                entityVersion.setCodeSystemMetadataValues(null);
                entityVersion.setValueSetMetadataValues(null);
                entityVersion.setConceptValueSetMemberships(null);
                entityVersion.setAssociationTypes(null);
                entityVersion.setPropertyVersions(null);
        
                hb_session.save(entityVersion);
        
                //Creating response
                codeSystemEntityVersionId = entityVersion.getVersionId();
                CodeSystemEntityVersion entityVersionReturn = new CodeSystemEntityVersion();
                entityVersionReturn.setVersionId(codeSystemEntityVersionId);
                entityVersionReturn.setStatus(entityVersion.getStatus());

                response.setCodeSystemEntity(new CodeSystemEntity());
                response.getCodeSystemEntity().setId(entity.getId());
                response.getCodeSystemEntity().setCurrentVersionId(entityVersion.getVersionId());
                response.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                response.getCodeSystemEntity().getCodeSystemEntityVersions().add(entityVersionReturn);

                LOGGER.debug("EntityId: " + entity.getId());
                LOGGER.debug("EntityVersionId: " + codeSystemEntityVersionId);

                //Saving currentVersion in the entity
                entity.setCurrentVersionId(entityVersion.getVersionId());
                hb_session.update(entity);
        
                LOGGER.debug("CurrentVersionId: " + entity.getCurrentVersionId());

                //Saving concept with translations
                if (concept != null){
                    concept.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                    concept.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
                    concept.setCodeSystemEntityVersionId(codeSystemEntityVersionId);

                    Iterator<CodeSystemConceptTranslation> itTranslation = concept.getCodeSystemConceptTranslations().iterator();
                    while (itTranslation.hasNext()){
                        CodeSystemConceptTranslation cTranslation = itTranslation.next();
                        cTranslation.setCodeSystemConcept(concept);
                    }

                    hb_session.save(concept);
                }
                
                if (assocType != null){
                    assocType.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                    assocType.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
                    assocType.setCodeSystemEntityVersionId(codeSystemEntityVersionId);
          
                    hb_session.save(assocType);
                }

                //Saving relationship to the vocabulary
                if (codeSystemVersionId > 0){
                    CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();

                    membership.setId(new CodeSystemVersionEntityMembershipId());
                    membership.getId().setCodeSystemEntityId(entity.getId());
                    membership.getId().setCodeSystemVersionId(codeSystemVersionId);

                    membership.setIsAxis(Boolean.FALSE);
                    membership.setIsMainClass(Boolean.FALSE);

                    if (paramCodeSystemEntity.getCodeSystemVersionEntityMemberships() != null && paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().size() > 0){
                        CodeSystemVersionEntityMembership memberRequest = (CodeSystemVersionEntityMembership) paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().toArray()[0];

                        if (memberRequest != null){
                            membership.setIsAxis(memberRequest.getIsAxis());
                            membership.setIsMainClass(memberRequest.getIsMainClass());
                        }
                    }
                    hb_session.save(membership);
                }

                //Saving property
                if (paramProperty != null){
                    Iterator<Property> itProperty = paramProperty.iterator();
                    while (itProperty.hasNext()){
                        Property property = itProperty.next();

                        PropertyVersion lastPropVersion = null;
                        Iterator<PropertyVersion> itPropertyVersion = property.getPropertyVersions().iterator();
                        while (itPropertyVersion.hasNext()){
                            
                            PropertyVersion propertyVersion = itPropertyVersion.next();
                            propertyVersion.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                            propertyVersion.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);

                            propertyVersion.setProperty(property);

                            propertyVersion.setInsertTimestamp(new Date());
                            propertyVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                            propertyVersion.setStatusDate(new Date());

                            lastPropVersion = propertyVersion;
                        }
                        hb_session.save(property);
            
                        //Setting current-Version-ID
                        if (lastPropVersion != null){
                            property.setCurrentVersionId(lastPropVersion.getVersionId());
                            hb_session.update(property);
                        }

                        lastPropVersion = null;
                        itPropertyVersion = property.getPropertyVersions().iterator();
                        while (itPropertyVersion.hasNext()){
                            PropertyVersion propertyVersion = itPropertyVersion.next();

                            if (lastPropVersion != null)
                                propertyVersion.setPreviousVersionId(lastPropVersion.getVersionId());
                                
                            lastPropVersion = propertyVersion;

                            hb_session.update(propertyVersion);
                        }
                    }
                }

                if (paramCodeSystem != null && paramCodeSystem.getId() != null){  
                    //Checking if metadataParameter default values have to be created
                    String HQL_metadataParameter_search = "select distinct mp from MetadataParameter mp";
                        HQL_metadataParameter_search += " join fetch mp.codeSystem cs";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("cs.", "id", paramCodeSystem.getId());

                    //Adding parameters, always connected with AND
                    HQL_metadataParameter_search += parameterHelper.getWhere("");
                    LOGGER.debug("HQL: " + HQL_metadataParameter_search);

                    org.hibernate.Query Q_metadataParameter_search = hb_session.createQuery(HQL_metadataParameter_search);
                    parameterHelper.applyParameter(Q_metadataParameter_search);

                    List<MetadataParameter> metadataParameterList = Q_metadataParameter_search.list();
                    if (!metadataParameterList.isEmpty()){
                        Iterator<MetadataParameter> iter = metadataParameterList.iterator();
                        while (iter.hasNext()){
                            MetadataParameter metadataParameter = (MetadataParameter) iter.next();
                            if(metadataParameter.getCodeSystem() != null && metadataParameter.getCodeSystem().getName() != null){
                                if(!metadataParameter.getCodeSystem().getName().equals("LOINC")){
                                    CodeSystemMetadataValue CSmetadataValue = new CodeSystemMetadataValue();

                                    CSmetadataValue.setParameterValue("");
                                    CSmetadataValue.setMetadataParameter(metadataParameter);
                                    CSmetadataValue.setCodeSystemEntityVersion(entityVersion);
                        
                                    hb_session.save(CSmetadataValue);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e){
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setMessage("Fehler bei 'CreateConcept', Hibernate [0027]: " + e.getLocalizedMessage());
                response.setCodeSystemEntity(null);
                codeSystemEntityVersionId = 0;

                LOGGER.error("Fehler bei 'CreateConcept', Hibernate [0027]: " + e.getLocalizedMessage());
            }
            finally{
                //Completing transaction
                if (createHibernateSession){
                    if (codeSystemEntityVersionId > 0){
                        if(codeSystemVersionId > 0)
                            LastChangeHelper.updateLastChangeDate(true, codeSystemVersionId, hb_session);
                        if(!hb_session.getTransaction().wasCommitted())
                            hb_session.getTransaction().commit();
                    }
                    else{
                        //Changes not successful
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();

                        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                        response.getReturnInfos().setMessage("Konzept konnte nicht erstellt werden!");
                        response.setCodeSystemEntity(null);
                        LOGGER.warn("Create concept failed [0028], codeSystemEntityVersionID: " + codeSystemEntityVersionId);
                    }
                    if(hb_session.isOpen())
                        hb_session.close();
                }
            }
            
            //Creating response
            if (codeSystemEntityVersionId > 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setMessage("Konzept erfolgreich erstellt");
            }

        }
        catch (Exception e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setMessage("Fehler bei 'CreateConcept' [0029] " + e.getLocalizedMessage());
            response.setCodeSystemEntity(null);

            LOGGER.error("Error [0029]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- CreateConceptOrAssociationType finished (002) -----");
    }

    /**
     * Checks the parameters via cross-reference.     *
     * @param Request the parameters to be checked
     * @param Response the response which is returned
     * @return false, if parameters are faulty
    */
    private boolean validateParameter(CreateConceptRequestType Request, CreateConceptResponseType Response){
        boolean erfolg = true;
        CodeSystem codeSystem = Request.getCodeSystem();
        
        if (codeSystem == null){
            erfolg = false;
            Response.getReturnInfos().setMessage("CodeSystem darf nicht null sein!");
        }
        else{
            Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
            if (csvSet != null){
                if (csvSet.size() > 1){
                    erfolg = false;
                    Response.getReturnInfos().setMessage("Die CodeSystem-Version-Liste darf maximal einen Eintrag haben!");
                }
                else if (csvSet.size() == 1){
                    CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

                    if (csv.getVersionId() == null || csv.getVersionId() == 0){
                        erfolg = false;
                        Response.getReturnInfos().setMessage("Es muss eine ID für die CodeSystem-Version angegeben sein, in welcher Sie das Konzept einfügen möchten!");
                    }
                }
            }
        }
        
        if (!erfolg){
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            return erfolg;
        }

        CodeSystemEntity codeSystemEntity = Request.getCodeSystemEntity();
        if (codeSystemEntity == null){
            erfolg = false;
            Response.getReturnInfos().setMessage("CodeSystem-Entity darf nicht NULL sein!");
        }
        else{
            Set<CodeSystemEntityVersion> csevSet = codeSystemEntity.getCodeSystemEntityVersions();
            if (csevSet != null){
                if (csevSet.size() > 1){
                    erfolg = false;
                    Response.getReturnInfos().setMessage("Die CodeSystem-Entity-Version-Liste darf maximal einen Eintrag haben!");
                }
                else if (csevSet.size() == 1){
                    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csevSet.toArray()[0];
                    Set<CodeSystemConcept> conceptSet = csev.getCodeSystemConcepts();
                    if (conceptSet != null && conceptSet.size() == 1){
                        CodeSystemConcept concept = (CodeSystemConcept) conceptSet.toArray()[0];

                        if (concept.getCode() == null || concept.getCode().isEmpty()){
                            Response.getReturnInfos().setMessage("Sie müssen einen Code für das Konzept angeben!");
                            erfolg = false;
                        }
                        else if (concept.getIsPreferred() == null){
                            Response.getReturnInfos().setMessage("Sie müssen 'isPreferred' für das Konzept angeben!");
                            erfolg = false;
                        }
                        if (concept.getCodeSystemConceptTranslations() != null){
                            Iterator<CodeSystemConceptTranslation> itTrans = concept.getCodeSystemConceptTranslations().iterator();

                            while (itTrans.hasNext()){
                                CodeSystemConceptTranslation translation = itTrans.next();
                                if (translation.getTerm() == null){
                                    Response.getReturnInfos().setMessage("Sie müssen einen Term für eine Konzept-Übersetzung angeben!");
                                    erfolg = false;
                                }
                                else if (translation.getLanguageId() == 0){
                                    Response.getReturnInfos().setMessage("Sie müssen eine 'LanguageID' für eine Konzept-Übersetzung angeben!");
                                    erfolg = false;
                                }
                            }
                        }
                    }
                    else{
                        Response.getReturnInfos().setMessage("CodeSystemConcept-Liste darf nicht NULL sein und muss genau 1 Eintrag haben!");
                        erfolg = false;
                    }
                }
            }
            else{
                Response.getReturnInfos().setMessage("CodeSystemEntityVersion darf nicht NULL sein!");
                erfolg = false;
            }
        }
        
        if (!erfolg){
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            return erfolg;
        }

        if (Request.getProperty() != null){
            Iterator<Property> itProp = Request.getProperty().iterator();

            while (itProp.hasNext()){
                Property property = itProp.next();

                if (property.getPropertyVersions() != null){
                    Iterator<PropertyVersion> itPropVersion = property.getPropertyVersions().iterator();

                    while (itPropVersion.hasNext()){
                        PropertyVersion propertyVersion = itPropVersion.next();

                        if (propertyVersion.getName() == null){
                            Response.getReturnInfos().setMessage("Sie müssen einen Namen für eine Property-Version angeben!");
                            erfolg = false;
                        }
                    }
                }
                else{
                    Response.getReturnInfos().setMessage("Bei einer Property muss immer eine Property-Version angegeben sein!");
                    erfolg = false;
                }
            }
        }

        if (!erfolg){
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            return erfolg;
        }
        
        return erfolg;
    }
}
