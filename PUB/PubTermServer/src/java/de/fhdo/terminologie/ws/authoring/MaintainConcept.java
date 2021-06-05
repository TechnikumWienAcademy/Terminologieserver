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
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.Property;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Sven Becker
 */
public class MaintainConcept{
    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public MaintainConceptResponseType MaintainConcept(MaintainConceptRequestType parameter) {
////////// Logger //////////////////////////////////////////////////////////////
        if (logger.isInfoEnabled()) logger.info("====== MaintainConcept gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
        MaintainConceptResponseType response = new MaintainConceptResponseType();
        response.setReturnInfos(new ReturnType());

////////// Parameter prüfen ////////////////////////////////////////////////////
        if (validateParameter(parameter, response) == false)
            return response;        

        // Login-Informationen auswerten (gilt für jeden Webservice)    
        if (parameter != null){
            if(LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
                return response;    
        }
        
////////// Der eigentliche Teil  /////////////////////////////////////////////// 
        try {
            // Für die Statusmeldung ob eine neue VSV angelegt oder die alte verändert wurde
            String  sCreateNewVersionMessage = "";
            boolean bNewVersion              = parameter.getVersioning().getCreateNewVersion();
            
            // Hibernate-Block, Session öffnen
            org.hibernate.Session     hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            
            CodeSystemEntity        cse_Request  = parameter.getCodeSystemEntity();
            Long csvId                           = parameter.getCodeSystemVersionId();
            CodeSystemEntity        cse_db       = null;
            CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion)cse_Request.getCodeSystemEntityVersions().toArray()[0];
            CodeSystemEntityVersion csev_New     = null; 
            
            CodeSystemConcept            csc_Request  = null;
            CodeSystemConcept            csc_New      = null;
            CodeSystemConceptTranslation csct_Request = null;
            CodeSystemConceptTranslation csct_New     = null;
            
            Property                p_Request    = null;
            Property                p_New        = null;
            PropertyVersion         pv_Request   = null;
            PropertyVersion         pv_New       = null;     
            
            Iterator<PropertyVersion>              itPV_Request   = null;
            Iterator<CodeSystemConcept>            itCsc_Request  = null;
            Iterator<CodeSystemConceptTranslation> itCsct_Request = null;
            
            try{ // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern      
                // neue Version anlegen oder alte bearbeiten?
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
                    
                    // speichern damit eine Id erzeugt wird
                    hb_session.save(csev_New);
                    
                    // Revision setzen, falls angegeben
                    if(csev_Request.getMajorRevision() != null) csev_New.setMajorRevision(csev_Request.getMajorRevision());
                    if(csev_Request.getMinorRevision() != null) csev_New.setMinorRevision(csev_Request.getMinorRevision());                   
                    
                    // CodeSystemConcept neu anlegen, falls angegeben
                    if (csev_Request.getCodeSystemConcepts() != null){
                        csc_Request = (CodeSystemConcept)csev_Request.getCodeSystemConcepts().toArray()[0];                   
                        
                        csc_New = new CodeSystemConcept();
                        csc_New.setCodeSystemEntityVersionId(csev_New.getVersionId());
                        
                        csc_New.setCode(csc_Request.getCode());
                        csc_New.setTerm(csc_Request.getTerm());
                        csc_New.setIsPreferred(csc_Request.getIsPreferred());
                        if(csc_Request.getMeaning() != null)
                            csc_New.setMeaning(csc_Request.getMeaning());
                        if(csc_Request.getHints() != null)
                            csc_New.setHints(csc_Request.getHints());
                        if(csc_Request.getDescription()!= null)
                            csc_New.setDescription(csc_Request.getDescription());
                        if(csc_Request.getTermAbbrevation() != null && csc_Request.getTermAbbrevation().length() > 0)
                            csc_New.setTermAbbrevation(csc_Request.getTermAbbrevation());
                       
                        // CSConcept in SB speichern
                        hb_session.save(csc_New);
                        
                        // CodeSystemConceptTranslation(s) anlegen, falls vorhanden
                        if(csc_Request.getCodeSystemConceptTranslations() != null){
                            itCsct_Request = csc_Request.getCodeSystemConceptTranslations().iterator();
                            while(itCsct_Request.hasNext()){
                                csct_Request = itCsct_Request.next(); 

                                csct_New = new CodeSystemConceptTranslation();

                                csct_New.setCodeSystemConcept(csc_New);
                                csct_New.setLanguageId(csct_Request.getLanguageId());
                                csct_New.setTerm(csct_Request.getTerm());
                                if(csct_Request.getTermAbbrevation() != null && csct_Request.getTermAbbrevation().length() > 0)
                                    csct_New.setTermAbbrevation(csct_Request.getTermAbbrevation());
                                
                                // in DB speichern
                                hb_session.save(csct_New);
                            }
                        }                           
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
                        
                        // Property-Version pflichtangaben füllen
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
                
////////////////// bestehende Version updaten:  der einfachheitshalber wird hier das csev.Objekt, dass geändet werden sollen ebenfalls als csev_New bezeichnet
                else{ 
                    // original CSE aus DB laden                                  
                    cse_db = (CodeSystemEntity)hb_session.load(CodeSystemEntity.class, cse_Request.getId());
                 
                    // Request CSEV, benötigt man für die versionId
                    csev_Request = (CodeSystemEntityVersion)cse_Request.getCodeSystemEntityVersions().toArray()[0]; 
                    
                    // "neues" CodeSystemEntityVersion-Objekt wird aus der DB geladen. Jetzt können die Ã„nderungen vorgenommen werden
                    csev_New = (CodeSystemEntityVersion)hb_session.load(CodeSystemEntityVersion.class, csev_Request.getVersionId());
                      
                    // Revision setzen, falls angegeben
                    if(csev_Request.getMajorRevision() != null) csev_New.setMajorRevision(csev_Request.getMajorRevision());
                    if(csev_Request.getMinorRevision() != null) csev_New.setMinorRevision(csev_Request.getMinorRevision());
                    if(csev_Request.getIsLeaf() != null) csev_New.setIsLeaf((csev_Request.getIsLeaf()));
                    
                    // CodeSystemConcept aus DB laden und anpassen
                    if (csev_Request.getCodeSystemConcepts() != null){
                        csc_Request = (CodeSystemConcept)csev_Request.getCodeSystemConcepts().toArray()[0];                   
                        
                        // Aus DB laden
                        csc_New = (CodeSystemConcept)hb_session.load(CodeSystemConcept.class, csc_Request.getCodeSystemEntityVersionId());
                        
                        // Ã„nderungen speichern
                        if(csc_Request.getCode() != null && csc_Request.getCode().length() > 0) 
                            csc_New.setCode(csc_Request.getCode());
                        if(csc_Request.getTerm() != null && csc_Request.getTerm().length() > 0) 
                            csc_New.setTerm(csc_Request.getTerm());
                        if(csc_Request.getIsPreferred() != null)
                            csc_New.setIsPreferred(csc_Request.getIsPreferred());
                        if(csc_Request.getTermAbbrevation() != null)
                            csc_New.setTermAbbrevation(csc_Request.getTermAbbrevation());
                        if(csc_Request.getMeaning() != null)
                            csc_New.setMeaning(csc_Request.getMeaning());
                        if(csc_Request.getHints() != null)
                            csc_New.setHints(csc_Request.getHints());
                        if(csc_Request.getDescription()!= null)
                            csc_New.setDescription(csc_Request.getDescription());
                        // CodeSystemConceptTranslation(s) anlegen, falls vorhanden
                        if(csc_Request.getCodeSystemConceptTranslations() != null){
                            itCsct_Request = csc_Request.getCodeSystemConceptTranslations().iterator();
                            while(itCsct_Request.hasNext()){
                                csct_Request = itCsct_Request.next();
                                
                                //Delete
                                if(csct_Request.getId() != null && 
                                   csct_Request.getLanguageId() < 0 && 
                                   csct_Request.getTerm() == null &&
                                   csct_Request.getTermAbbrevation() == null &&
                                   csct_Request.getDescription() == null){
                                    
                                   CodeSystemConceptTranslation csct_puffer     = (CodeSystemConceptTranslation)hb_session.get(CodeSystemConceptTranslation.class, csct_Request.getId());
                                   hb_session.delete(csct_puffer);
                                }else{

                                    //Update
                                    if(csct_Request.getId() != null){

                                        csct_New     = (CodeSystemConceptTranslation)hb_session.get(CodeSystemConceptTranslation.class, csct_Request.getId());
                                    }else{
                                        //Wenn zusätzlich neues csct => neu anlegen...
                                        csct_New = new CodeSystemConceptTranslation();
                                        csct_New.setCodeSystemConcept(csc_Request);
                                        csct_New.setTerm("");
                                        csct_New.setLanguageId(-1L);
                                        hb_session.save(csct_New);
                                    }    

                                    if(csct_Request.getLanguageId() > 0)
                                        csct_New.setLanguageId(csct_Request.getLanguageId());
                                    if(csct_Request.getTerm() != null && csct_Request.getTerm().isEmpty() == false)
                                        csct_New.setTerm(csct_Request.getTerm());
                                    if(csct_Request.getTermAbbrevation() != null)
                                        csct_New.setTermAbbrevation(csct_Request.getTermAbbrevation());
                                    
                                    hb_session.merge(csct_New);
                                }
                            }
                        }                        
                    }
                    
                    // Durch alle PropertyVersions vom Request und erzeuge diese
                    itPV_Request = csev_Request.getPropertyVersions().iterator();
                    while(itPV_Request.hasNext()){
                        pv_Request = itPV_Request.next(); 
                        p_Request = pv_Request.getProperty();
            
                        // PropertyVersion /////////////////////////////////////
                        pv_New = (PropertyVersion)hb_session.load(PropertyVersion.class, pv_Request.getVersionId());

                        // Property ////////////////////////////////////////////                    
                        p_New = pv_New.getProperty();                        
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
                LastChangeHelper.updateLastChangeDate(true, csvId,hb_session);
            } catch (Exception e) {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'MaintainConcept', Hibernate: " + e.getLocalizedMessage());
                logger.error(response.getReturnInfos().getMessage());
            } finally {
                // Transaktion abschließen
                if (csev_New.getVersionId() > 0 /*&& pv_New.getVersionId() > 0 && p_New.getId() > 0*/) 
                    hb_session.getTransaction().commit();
                else {                    
                    logger.warn("[MaintainConcept.java] Ã„nderungen nicht erfolgreich");
                    hb_session.getTransaction().rollback();
                }
                hb_session.close();
            }
            if (csev_New.getVersionId() > 0 /*&& pv_New.getVersionId() > 0 && p_New.getId() > 0*/) {
                // Status an den Aufrufer weitergeben
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("Concept erfolgreich geändert. " + sCreateNewVersionMessage);
            }

        } catch (Exception e) {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'MaintainConcept': " + e.getLocalizedMessage());           
            logger.error(response.getReturnInfos().getMessage());
        }      
        return response;
    }

    private boolean validateParameter(MaintainConceptRequestType request, MaintainConceptResponseType response) {
        boolean          isValid      = false;
        boolean          bNewVersion;
        String           errorMessage = "Unbekannter Fehler";
        CodeSystemEntity cse_Request  = request.getCodeSystemEntity();       
        
        // mit switch kann man sehr einfach die abfragen alle untereinander schreiben, ohne dieses ganze if-else-if-else-if... zeug
        // die abfragen müssen nur in der richtigen Reihenfolge stehen 
        abort_validation: // Label mötig um aus tieferen if-Schleifen die komplette Switch-Anweisung abzubrechen
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
    ////////////// CodeSystemVersionEntityMembership (optional) ////////////////
                Set<CodeSystemVersionEntityMembership> csvemSet_Request = cse_Request.getCodeSystemVersionEntityMemberships();
                
                if(csvemSet_Request.isEmpty() == false){
                    if(csvemSet_Request.size() > 1){
                        errorMessage = "Falls CodeSystemVersionEntityMembership angegben wurde, darf es nur genau eine sein!";
                        break;
                    }
                    // hole aus dem aTypeSet den einen aType raus
                    CodeSystemVersionEntityMembership csvem_Request = (CodeSystemVersionEntityMembership)csvemSet_Request.toArray()[0];
                    if(csvem_Request == null){
                        errorMessage = "CodeSystemVersionEntityMembership konnte nicht aus derm CodeSystemVersionEntityMembership_Set generiert werden!";
                        break;
                    }
                    if(bNewVersion && csvem_Request.getIsAxis() == null){
                        errorMessage = "Falls ein CodeSystemVersionEntityMembership angegeben wurde darf isAxis nicht NULL sein!";
                        break;
                    }
                    if(bNewVersion && csvem_Request.getIsMainClass() == null){
                        errorMessage = "Falls ein CodeSystemVersionEntityMembership angegeben wurde darf isMainClass nicht NULL sein!";
                        break;
                    }
                }  
    ////////////////// CodeSystemEntityVersion //////////////////////////////////////////                 
                // Gibt es eine CodeSystemEntityVersion Liste?
                Set<CodeSystemEntityVersion> csevSet_Request = cse_Request.getCodeSystemEntityVersions();
                if (csevSet_Request == null){
                    errorMessage = "CodeSystemEntityVersion-Liste darf nicht NULL sein!"; 
                    break;
                } 
                // Wenn ja, hat sie genau einen Eintrag?
                if (csevSet_Request.size() != 1) {
                    errorMessage = "CodeSystemEntityVersion-Liste hat " + Integer.toString(csevSet_Request.size()) + " Einträge. Sie muss aber genau einen Eintrag haben!";
                    break;
                }                
                CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion)csevSet_Request.toArray()[0];                   
                // hat VSV eine gültige ID?
                if (csev_Request.getVersionId() == null || csev_Request.getVersionId() < 1) {
                    errorMessage = "CodeSystemEntityVersion Id darf nicht NULL oder kleiner 1 sein!";
                    break;
                }  
        ////////////// CodeSystemConcept (optional) ////////////////////////////
                Set<CodeSystemConcept> cscSet_Request = csev_Request.getCodeSystemConcepts();
                
                if(cscSet_Request != null && cscSet_Request.isEmpty() == false){
                    if(cscSet_Request.size() > 1){
                        errorMessage = "Falls CodeSystemConcept angegben wurde, darf es nur genau einer sein!";
                        break;
                    }
                    // hole aus dem aTypeSet den einen aType raus
                    CodeSystemConcept csc_Request = (CodeSystemConcept)cscSet_Request.toArray()[0];
                    if(csc_Request == null){
                        errorMessage = "CodeSystemConcept ungültig!";
                        break;
                    }
                    if(bNewVersion && (csc_Request.getCode() == null || csc_Request.getCode().length() == 0)){
                        errorMessage = "Falls ein CodeSystemConcept angegeben wurde darf code nicht NULL sein oder eine Länge von 0 haben!";
                        break;
                    }
                    if(bNewVersion && (csc_Request.getTerm() == null || csc_Request.getTerm().length() == 0)){
                        errorMessage = "Falls ein CodeSystemConcept angegeben wurde darf Term nicht NULL sein oder eine Länge von 0 haben!";
                        break;
                    }
                    if(bNewVersion && (csc_Request.getIsPreferred() == null )){
                        errorMessage = "Falls ein CodeSystemConcept angegeben wurde darf isPreferred nicht NULL sein!";
                        break;
                    }
            ////////////// CodeSystemConceptTranslation (optional) /////////////    
                    Set<CodeSystemConceptTranslation> csctSet_Request = csc_Request.getCodeSystemConceptTranslations();
                    if(csctSet_Request != null && csctSet_Request.isEmpty() == false){                     
                        CodeSystemConceptTranslation csct_Request;
                        Iterator<CodeSystemConceptTranslation> itCsct_Request = csctSet_Request.iterator();                        
                        while(itCsct_Request.hasNext()){
                            csct_Request = itCsct_Request.next();
                            if(bNewVersion){
                                if(csct_Request.getTerm() == null || csct_Request.getTerm().length() == 0){
                                    errorMessage = "Falls eine CodeSystemConceptTranslation angegeben wurde und eine neue Version angelegt werden soll, darf term nicht NULL oder leer sein!";
                                    break;                                    
                                }
                                if(csct_Request.getLanguageId() < 1){
                                    errorMessage = "Falls eine CodeSystemConceptTranslation angegeben wurde und eine neue Version angelegt werden soll, darf languageId nicht NULL oder kleiner 1 sein!";
                                    break;                                    
                                }
                            }
                            else{
                                if(csct_Request.getId() == null || csct_Request.getId() < 1){
                                    errorMessage = "Falls eine CodeSystemConceptTranslation angegeben wurde und keine neue Version angelegt werden soll, darf id nicht NULL oder kleiner 1!";
                                    break;                                    
                                }
                            }                            
                        }
                    }
                }                                                 
                    
        ////////////////// PropertyVersion /////////////////////////////////////  
                // Durchlaufe alle PropertyVersions und prüfe, ob alle eine gültige Property haben
                if(csev_Request.getPropertyVersions() != null && csev_Request.getPropertyVersions().isEmpty() == false){                     
                    Iterator<PropertyVersion> itPV = csev_Request.getPropertyVersions().iterator();  
                    PropertyVersion pV_Request = null;
                    while(itPV.hasNext()){
                        pV_Request = itPV.next();
                        // bei neuen Versionen müssen noch auf ein paar Dinge geachtet werden:  Name + Description
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

                        // zählt für neue und alte Versionen
                        if(pV_Request.getProperty() == null || pV_Request.getProperty().getId() == null || pV_Request.getProperty().getId() < 1){
                            errorMessage = "Mindestens eine PropertyVersion hat eine Property ohne Id (oder Id < 1)!";
                            break;
                        }                  
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