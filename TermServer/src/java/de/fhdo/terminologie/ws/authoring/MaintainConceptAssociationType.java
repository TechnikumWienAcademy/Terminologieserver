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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.Property;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class MaintainConceptAssociationType{
    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public MaintainConceptAssociationTypeResponseType MaintainConceptAssociationType(MaintainConceptAssociationTypeRequestType parameter) {
////////// Logger //////////////////////////////////////////////////////////////
        if (logger.isInfoEnabled()) 
            logger.info("====== MaintainConceptAssociationType gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
        MaintainConceptAssociationTypeResponseType response = new MaintainConceptAssociationTypeResponseType();
        response.setReturnInfos(new ReturnType());

////////// Parameter pr�fen ////////////////////////////////////////////////////
        if (validateParameter(parameter, response) == false)
            return response;        

        // Login-Informationen auswerten (gilt f�r jeden Webservice)    
        if (parameter != null){
            if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
                return response;    
        }
        
////////// Der eigentliche Teil  /////////////////////////////////////////////// 
        try {
            // F�r die Statusmeldung ob eine neue VSV angelegt oder die alte ver�ndert wurde
            String sCreateNewVersionMessage = "";
            
            // Hibernate-Block, Session �ffnen
            org.hibernate.Session     hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            
            boolean bNewVersion = parameter.getVersioning().getCreateNewVersion();
            
            CodeSystemEntity        cse_Request  = parameter.getCodeSystemEntity();
            CodeSystemEntity        cse_db       = null;
            CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion)cse_Request.getCodeSystemEntityVersions().toArray()[0];
            CodeSystemEntityVersion csev_New     = null; 
            
            Property                p_Request    = null;
            Property                p_New        = null;
            PropertyVersion         pv_Request   = null;
            PropertyVersion         pv_New       = null;     
            
            Iterator<PropertyVersion> itPV_Request = null;
                        
            try{ // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern      
////////////////// neue Version anlegen oder alte bearbeiten?
                if(bNewVersion){
                    // original CSE aus DB laden
                    cse_db = (CodeSystemEntity)hb_session.load(CodeSystemEntity.class, cse_Request.getId());
                    
                    // neues CodeSystemEntityVersion-Objekt erzeugen
                    csev_New = new CodeSystemEntityVersion(); 
                    
                    // Not NULL werte setzen
                    csev_New.setCodeSystemEntity(cse_db);                         
                    csev_New.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                    csev_New.setStatusDate(new java.util.Date()); 
                    csev_New.setInsertTimestamp(new java.util.Date());
                    
                    csev_New.setPreviousVersionId(cse_db.getCurrentVersionId());
                    
                    // specihern damit eine Id erzeugt wird
                    hb_session.save(csev_New);
                    
                    // Revision setzen, falls angegeben
                    if(csev_Request.getMajorRevision() != null) csev_New.setMajorRevision(csev_Request.getMajorRevision());
                    if(csev_Request.getMinorRevision() != null) csev_New.setMinorRevision(csev_Request.getMinorRevision());                   
                              
                    // AssociationType erzeugen und CSEV hinzuf�gen
                    if(csev_Request.getAssociationTypes().size() > 0){
                        AssociationType aType_Request = (AssociationType)csev_Request.getAssociationTypes().toArray()[0];

                        // lade aus der DB den aType mit der passenden CurrentVersionId bzw versionId von csev. Es wird previousVersionId genutzt, 
                        // weil die neue CSEVersion ja schon durch Hibernate.save eine neue VersionId bekommen hat und die alte Id jetzt die prev.Id ist
                        AssociationType aType_New     = (AssociationType)hb_session.load(AssociationType.class, csev_New.getPreviousVersionId());                       

                        // Eigenschaften setzen
                        if(aType_Request.getForwardName() != null && aType_Request.getForwardName().length() > 0) 
                            aType_New.setForwardName(aType_Request.getForwardName());
                        if(aType_Request.getReverseName() != null && aType_Request.getReverseName().length() > 0) 
                            aType_New.setReverseName(aType_Request.getReverseName());

                        hb_session.save(aType_New);                           
                    } 
                    
                    // Durch alle PropertyVersions vom Request und erzeuge diese
                    itPV_Request = csev_Request.getPropertyVersions().iterator();
                    while(itPV_Request.hasNext()){
                        pv_Request = itPV_Request.next(); 
                   
                        // Property ////////////////////////////////////////////
                        p_Request  = pv_Request.getProperty();
                        p_New = new Property();
                        p_New.setName(p_Request.getName());                        
                        hb_session.save(p_New);
                        
                        // PropertyVersion /////////////////////////////////////
                        pv_New = new PropertyVersion();
                        
                        // Property-Version pflichtangaben f�llen
                        pv_New.setCodeSystemEntityVersion(csev_New);
                        pv_New.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                        pv_New.setStatusDate(new java.util.Date());
                        pv_New.setInsertTimestamp(new java.util.Date());
                        pv_New.setProperty(p_New);
                        pv_New.setName(pv_Request.getName());
                        pv_New.setDescription(pv_Request.getDescription()); 
                        
                        // optionale Felder                    
                        if(pv_Request.getPropertyKindId()   != null)pv_New.setPropertyKindId(pv_Request.getPropertyKindId());
                        if(pv_Request.getContent()          != null)pv_New.setContent(pv_Request.getContent());
                        if(pv_Request.getContentName()      != null)pv_New.setContentName(pv_Request.getContentName());
                        if(pv_Request.getContentMimetype()  != null)pv_New.setContentMimetype(pv_Request.getContentMimetype());
                        if(pv_Request.getContentSize()      != null)pv_New.setContentSize(pv_Request.getContentSize());
                        if(pv_Request.getLanguageId()       != null)pv_New.setLanguageId(pv_Request.getLanguageId());         
                                                
                        // Speichern damit die Id in p_New eingetragen werden kann
                        hb_session.save(pv_New);
                        
                        // CurrentVersionId speichern
                        p_New.setCurrentVersionId(pv_New.getVersionId());
                        hb_session.save(p_New);                       
                    }
                    cse_db.setCurrentVersionId(csev_New.getVersionId());
                    hb_session.save(cse_db);
                }                
                
////////////////// bestehende Version updaten:  der einfachheitshalber wird hier das csev.Objekt, dass ge�ndet werden sollen ebenfalls als csev_New bezeichnet
                else{ 
                    // original CSE aus DB laden                                  
                    cse_db = (CodeSystemEntity)hb_session.load(CodeSystemEntity.class, cse_Request.getId());
                 
                    // Request CSEV, ben�tigt man f�r die versionId
                    csev_Request = (CodeSystemEntityVersion)cse_Request.getCodeSystemEntityVersions().toArray()[0]; 
                    
                    // "neues" CodeSystemEntityVersion-Objekt wird aus der DB geladen. Jetzt k�nnen die Änderungen vorgenommen werden
                    csev_New = (CodeSystemEntityVersion)hb_session.load(CodeSystemEntityVersion.class, csev_Request.getVersionId());
                      
                    // Revision setzen, falls angegeben
                    if(csev_Request.getMajorRevision() != null) csev_New.setMajorRevision(csev_Request.getMajorRevision());
                    if(csev_Request.getMinorRevision() != null) csev_New.setMinorRevision(csev_Request.getMinorRevision());                   
                    
                    // AssociationType erzeugen und CSEV hinzuf�gen
                    if(csev_Request.getAssociationTypes().size() > 0){
                        AssociationType aType_Request = (AssociationType)csev_Request.getAssociationTypes().toArray()[0];

                        // lade aus der DB den aType mit der passenden CurrentVersionId bzw versionId von csev
                        AssociationType aType_New     = (AssociationType)hb_session.load(AssociationType.class, csev_New.getVersionId());                       

                        // Eigenschaften setzen
                        if(aType_Request.getForwardName() != null && aType_Request.getForwardName().length() > 0) 
                            aType_New.setForwardName(aType_Request.getForwardName());
                        if(aType_Request.getReverseName() != null && aType_Request.getReverseName().length() > 0) 
                            aType_New.setReverseName(aType_Request.getReverseName());

                        hb_session.save(aType_New);                           
                    } 
                    
                    // Durch alle PropertyVersions vom Request und erzeuge diese
                    itPV_Request = csev_Request.getPropertyVersions().iterator();
                    while(itPV_Request.hasNext()){
                        pv_Request = itPV_Request.next(); 
                        p_Request = pv_Request.getProperty();
       
                        // PropertyVersion /////////////////////////////////////
                        pv_New = (PropertyVersion)hb_session.load(PropertyVersion.class, pv_Request.getVersionId());

                        // Property //////////////////////////////////////////// 
                        p_New = pv_New.getProperty();//(Property)hb_session.load(Property.class, pv_New.);                       
                        if(p_Request.getName() != null) p_New.setName(p_Request.getName());                        
                        hb_session.save(p_New);
                        
                        // Property-Version optionale angaben                                                                      
                        if(pv_Request.getName()             != null && pv_Request.getName().length() > 0)
                            pv_New.setName(pv_Request.getName());
                        if(pv_Request.getDescription()      != null && pv_Request.getDescription().length() > 0) 
                            pv_New.setDescription(pv_Request.getDescription());  
                        if(pv_Request.getPropertyKindId()   != null)pv_New.setPropertyKindId(pv_Request.getPropertyKindId());
                        if(pv_Request.getContent()          != null)pv_New.setContent(pv_Request.getContent());
                        if(pv_Request.getContentName()      != null)pv_New.setContentName(pv_Request.getContentName());
                        if(pv_Request.getContentMimetype()  != null)pv_New.setContentMimetype(pv_Request.getContentMimetype());
                        if(pv_Request.getContentSize()      != null)pv_New.setContentSize(pv_Request.getContentSize());
                        if(pv_Request.getLanguageId()       != null)pv_New.setLanguageId(pv_Request.getLanguageId());         

                        // Speichern damit die Id in p_New eingetragen werden kann
                        hb_session.save(pv_New);

                        // CurrentVersionId speichern
                        p_New.setCurrentVersionId(pv_New.getVersionId());
                        hb_session.save(p_New);                            
                    }    
                    
                    // Nachdem die neuen Objekte angelegt oder die alten aktualisiert wurden, speichere CSE in DB
                    cse_db.setCurrentVersionId(csev_New.getVersionId());
                    hb_session.save(cse_db);
                }
            } catch (Exception e) {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociationType', Hibernate: " + e.getLocalizedMessage());
                logger.error(response.getReturnInfos().getMessage());
            } finally {
                // Transaktion abschlie�en
                if (csev_New.getVersionId() > 0 && pv_New.getVersionId() > 0 && p_New.getId() > 0) 
                    hb_session.getTransaction().commit();
                else {                    
                    logger.warn("[MaintainConceptAssociationType.java] Änderungen nicht erfolgreich");
                    hb_session.getTransaction().rollback();
                }
                hb_session.close();
            }
            if (csev_New.getVersionId() > 0 && pv_New.getVersionId() > 0 && p_New.getId() > 0) {
                // Status an den Aufrufer weitergeben
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("AssociationType erfolgreich ge�ndert. " + sCreateNewVersionMessage);
            }

        } catch (Exception e) {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociationType': " + e.getLocalizedMessage());           
            logger.error(response.getReturnInfos().getMessage());
        }      
        return response;
    }

    private boolean validateParameter(MaintainConceptAssociationTypeRequestType request, MaintainConceptAssociationTypeResponseType response) {
        boolean          isValid      = false;
        boolean          bNewVersion;
        String           errorMessage = "Unbekannter Fehler";
        CodeSystemEntity cse_Request  = request.getCodeSystemEntity();       
        
        // mit switch kann man sehr einfach die abfragen alle untereinander schreiben, ohne dieses ganze if-else-if-else-if... zeug
        // die abfragen m�ssen nur in der richtigen Reihenfolge stehen 
        abort_validation: // Label m�tig um aus tieferen Schleifen in der Switch-Anweisung komplett rauszuspringen
        switch(1){
            case 1: 
////////////////// Versioning //////////////////////////////////////////////////
                if(request.getVersioning() == null){
                    errorMessage = "Versioning darf nicht NULL sein!"; 
                    break;
                }
                if (request.getVersioning().getCreateNewVersion() == null){
                    errorMessage = "createNewVersion darf nicht NULL sein!";
                    break;
                }
                bNewVersion = request.getVersioning().getCreateNewVersion();
////////////////// CodeSystemEntity ////////////////////////////////////////////
                if (cse_Request == null) {
                    errorMessage = "CodeSystemEntity darf nicht NULL sein!";
                    break;
                }
                // Gibt es eine CodeSystemEntityVersion Liste?
                Set<CodeSystemEntityVersion> csevSet_Request = cse_Request.getCodeSystemEntityVersions();
                if (csevSet_Request == null){
                    errorMessage = "CodeSystemEntityVersion-Liste darf nicht NULL sein!"; 
                    break;
                } 
                // Wenn ja, hat sie genau einen Eintrag?
                if (csevSet_Request.size() != 1) {
                    errorMessage = "CodeSystemEntityVersion-Liste hat " + Integer.toString(csevSet_Request.size()) + " Eintr�ge. Sie muss aber genau einen Eintrag haben!";
                    break;
                } 
                CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion)csevSet_Request.toArray()[0];                   
                // hat VSV eine g�ltige ID?
                if (csev_Request.getVersionId() == null || csev_Request.getVersionId() < 1) {
                    errorMessage = "CodeSystemEntityVersion Id darf nicht NULL oder kleiner 1 sein!";
                    break;
                } 
    ////////////////// AssosiationType (optional) //////////////////////////////
                Set<AssociationType> aTypeSet_Request = csev_Request.getAssociationTypes();
                if(aTypeSet_Request.isEmpty() == false){
                    if(aTypeSet_Request.size() > 1){
                        errorMessage = "Falls AssociationType angegben wurde, darf es nur genau einer sein!";
                        break;
                    }
                    // hole aus dem aTypeSet den einen aType raus
                    AssociationType aType_Request = (AssociationType)aTypeSet_Request.toArray()[0];
                    if(aType_Request == null){
                        errorMessage = "AssociationType konnte nicht aus derm AssociationTypeSet generiert werden!";
                        break;
                    }
                    if(aType_Request.getForwardName() == null || aType_Request.getForwardName().length() == 0){
                        errorMessage = "Falls ein AssociationType angegeben wurde darf ForwardName nicht NULL sein!";
                        break;
                    }
                    if(aType_Request.getReverseName() == null || aType_Request.getReverseName().length() == 0){
                        errorMessage = "Falls ein AssociationType angegeben wurde darf ReverseName nicht NULL sein!";
                        break;
                    }
                }
                
    ////////////////// PropertyVersion /////////////////////////////////////////  
                if(csev_Request.getPropertyVersions() == null){
                    errorMessage = "Das Set PropertyVersions darf nicht NULL sein!";
                    break;
                }
                if(csev_Request.getPropertyVersions().isEmpty()){
                    errorMessage = "Es muss mindestens eine PropertyVersion angegeben werden!";
                    break;
                }
                // durchlaufe alle PropertyVersions und pr�fe, ob alle eine g�ltige Property haben 
                Iterator<PropertyVersion> itPV = csev_Request.getPropertyVersions().iterator();  
                PropertyVersion pV_Request = null;
                while(itPV.hasNext()){
                    pV_Request = itPV.next();
                    // bei neuen Versionen m�ssen noch auf ein paar Dinge geachtet werden:  Name + Description
                    if(bNewVersion){
                        if(pV_Request.getName() == null || pV_Request.getName().length() == 0){
                            errorMessage = "Wenn eine neue PropertyVersion angelegt werden soll, muss ein Name vergeben werden!";
                            break abort_validation;
                        }
                        if(pV_Request.getDescription() == null || pV_Request.getDescription().length() == 0){
                            errorMessage = "Wenn eine neue PropertyVersion angelegt werden soll, muss eine Descripion vergeben werden!";
                            break abort_validation;
                        }
                        if(pV_Request.getProperty() == null){
                            errorMessage = "Wenn eine neue PropertyVersion angelegt werden soll, muss eine Property angegeben werden!";
                            break abort_validation;
                        }
                        if(pV_Request.getProperty().getName() == null || pV_Request.getProperty().getName().length() == 0){
                            errorMessage = "Mindestens eine neue Property hat keinen Namen!";
                            break abort_validation;
                        }                        
                    }
                    else{
                        if(pV_Request.getVersionId() == null || pV_Request.getVersionId() < 1){
                            errorMessage = "Wenn eine bestehende PropertyVersion bearbeitet werden soll, muss eine versionId angegeben werden!";
                            break abort_validation;
                        }                        
                    }
                    
                    // z�hlt f�r neue und alte Versionen
                    if(pV_Request.getProperty() == null || pV_Request.getProperty().getId() == null || pV_Request.getProperty().getId() < 1){
                        errorMessage = "Mindestens eine PropertyVersion hat eine Property ohne Id (oder Id < 1)!";
                        break;
                    }                  
                }      
                // Falls alles OK
                isValid = true;
        }                
       
        if (isValid == false){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage(errorMessage);
        }        
        return isValid;
    }
}