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
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.proxy.HibernateProxy;

/**
 *
 * @author Sven Becker
 * 
 * 07.10.11
 * 
 */
public class MaintainCodeSystemVersion {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public MaintainCodeSystemVersionResponseType MaintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter) {
////////// Logger //////////////////////////////////////////////////////////////
        if (logger.isInfoEnabled()) 
            logger.info("====== MaintainCodeSystemVersion gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
        MaintainCodeSystemVersionResponseType response = new MaintainCodeSystemVersionResponseType();
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
            
            // CodeSystem und CodeSystemVersion zum Speichern vorbereiten 
            CodeSystem        cs_Request     = parameter.getCodeSystem();
            CodeSystemVersion csv_Request    = (CodeSystemVersion)cs_Request.getCodeSystemVersions().toArray()[0];
            CodeSystemVersion csvNew         = null;              

            try{ // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern                                             
                // versuche cs aus der DB zu laden und in cs_db, csv_db zu speichern
                if (cs_Request.getId() != null && cs_Request.getId() > 0) {                    
                    CodeSystem        cs_db  = (CodeSystem)hb_session.get(CodeSystem.class, cs_Request.getId());
                    CodeSystemVersion csv_db = (CodeSystemVersion)hb_session.get(CodeSystemVersion.class, csv_Request.getVersionId());
              
                    // Name soll in der neuen Version ge�ndert werden k�nnen
                    if(cs_Request.getName() != null && cs_Request.getName().length() > 0) 
                        cs_db.setName(cs_Request.getName());

                    // Description soll in der neuen Version ge�ndert werden k�nnen
                    if(cs_Request.getDescription() != null) 
                        cs_db.setDescription(cs_Request.getDescription());
                    
                    // DescriptionEng soll in der neuen Version ge�ndert werden k�nnen
                    if(cs_Request.getDescriptionEng() != null) 
                        cs_db.setDescriptionEng(cs_Request.getDescriptionEng());
                    
                    // Website soll in der neuen Version ge�ndert werden k�nnen
                    if(cs_Request.getWebsite() != null) 
                        cs_db.setWebsite(cs_Request.getWebsite());
										
										// Matthias: adding info for incomplete CS
										if(cs_Request.getIncompleteCS() != null){
											cs_db.setIncompleteCS(cs_Request.getIncompleteCS());
										}
										
										//Matthias: adding responsible Organization
										if(cs_Request.getResponsibleOrganization() != null){
											cs_db.setResponsibleOrganization(cs_Request.getResponsibleOrganization());
										}
                    
                    // Neue Version anlegen:  Diese enth�lt dann nur die Angaben, die auch gemacht wurden. leere Felder werden nicht aus alten Versionen �bernommen
                    if(parameter.getVersioning().getCreateNewVersion()){
                        sCreateNewVersionMessage = "Neue CSVersion angelegt.";
                        csvNew = new CodeSystemVersion();
                        
                        // Alte Version auf die aktuelle setzen
                        csvNew.setPreviousVersionId(cs_db.getCurrentVersionId()); 
                        
                        // neues CS anlegen
                        csvNew.setCodeSystem(new CodeSystem());

                        // dem CS die passende ID von dem CS-Objekt aus der DB �bergeben
                        csvNew.getCodeSystem().setId(cs_db.getId());
                        
                        //Zwingende Variable darf nicht NULL sein!
                        csvNew.setValidityRange(238l); // 238 => optional
                        
                        // Ein paar Defaultwerte setzen
                        csvNew.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum          
                        csvNew.setStatus(csv_Request.getStatus());
                        csvNew.setStatusDate(new java.util.Date()); // Aktuelles Datum 
                    }
                    // Alte Version editieren:  csvNew = csv aus DB
                    else{
                        sCreateNewVersionMessage = "CSVersion (" + Long.toString(csv_Request.getVersionId()) + ") �berschrieben.";
                        csvNew = csv_db;
                        LastChangeHelper.updateLastChangeDate(true, csvNew.getVersionId(),hb_session);
                    }                                        
                                           
                    // Name setzen, falls vorhanden
                    if(csv_Request.getName() != null)
                        csvNew.setName(csv_Request.getName());

                    // release Date soll angegeben werden k�nnen
                    if(csv_Request.getReleaseDate() != null) 
                        csvNew.setReleaseDate(csv_Request.getReleaseDate()); 

                    // ExpirationDate setzen, falls vorhanden
                    if(csv_Request.getExpirationDate() != null) 
                        csvNew.setExpirationDate(csv_Request.getExpirationDate()); 

                    // Source setzen, falls vorhanden
                    if(csv_Request.getSource() != null) 
                        csvNew.setSource(csv_Request.getSource()); 

                    // Description setzen, falls vorhanden
                    if(csv_Request.getDescription() != null) 
                        csvNew.setDescription(csv_Request.getDescription()); 

                    // PreferredLanguageId setzen, falls vorhanden
                    if(csv_Request.getPreferredLanguageId() != null) 
                        csvNew.setPreferredLanguageId(csv_Request.getPreferredLanguageId()); 
                    
                    // ValidityRange setzen, falls vorhanden
                    if(csv_Request.getValidityRange() != null) 
                        csvNew.setValidityRange(csv_Request.getValidityRange()); 
                    
                    // Oid setzen falls vorhanden
                    if(csv_Request.getOid() != null) 
                        csvNew.setOid(csv_Request.getOid());  

                    // LicenceHolder setzen falls vorhanden
                    if(csv_Request.getLicenceHolder() != null) 
                        csvNew.setLicenceHolder(csv_Request.getLicenceHolder()); 

                    // UnderLicence setzen falls vorhanden
                    if(csv_Request.getUnderLicence() != null) 
                        csvNew.setUnderLicence(csv_Request.getUnderLicence());

                    // csvNew schon mal in der DB Speichern, damit ggf eine Id vergeben wird die dann gleich bei den licenceTypes ben�tigt wird
                    if(parameter.getVersioning().getCreateNewVersion())
                        hb_session.save(csvNew);                                            
                    
                    // f�r alle angegebenen licenceTypes
                    if(csv_Request.getLicenceTypes() != null && csv_Request.getLicenceTypes().isEmpty() == false){
                        Iterator<LicenceType> itLt_Request = csv_Request.getLicenceTypes().iterator(),
                                              itLt_db;
                        LicenceType lt_Request, 
                                    lt_New = null;                            
                        while(itLt_Request.hasNext()){
                            lt_Request = itLt_Request.next();
                            
                            // Neue CSVersion: neue licenceType-Objekte anlegen, CSV und typeTxt eintragen und speichern 
                            if(parameter.getVersioning().getCreateNewVersion()){
                                lt_New = new LicenceType();
                                lt_New.setTypeTxt(lt_Request.getTypeTxt());
                                lt_New.setCodeSystemVersion(csvNew);
                                hb_session.save(lt_New);
                                csvNew.getLicenceTypes().add(lt_New);
                            }
                            // Alte CSVersion bearbeiten bzw. ihre licenceTypes
                            else{
                                // licenceType aus der DB auslesen, �ber csvNew.getLicenceType() nur m�glich wenn man mit schleife
                                // das ganze set nach der passenden ID durchsucht.  Sollte �ber hb_session.get() schneller gehen oder?
                                LicenceType lt_DB = (LicenceType)hb_session.get(LicenceType.class, lt_Request.getId()); 
                                
                                // die im Request angegebene lt ID ist nicht in der DB, also mache mit n�chstem lt weiter
                                if(lt_DB == null){
                                    logger.debug("licenceType ID " + Long.toString(lt_Request.getId()) + " nicht in der DB vorhanden.");
                                    continue;
                                }
                                if(lt_DB.getCodeSystemVersion().getVersionId() != csv_Request.getVersionId()){
                                    logger.debug("Es wird versucht den licenceType einer anderen CSV zu �ndern!");
                                    continue;                                    
                                }
                                
                                // typeTxt �ndern und speichern
                                lt_DB.setTypeTxt(lt_Request.getTypeTxt());    
                                hb_session.save(lt_DB);                                
                            }                           
                        }    
                    }

                    // In DB speichern damit csvNew eine ID bekommt falls es eine neue Version ist, ansonsten wird das Objekt aktualisiert
                    hb_session.save(csvNew);   
                    
                    if(parameter.getVersioning().getCreateNewVersion()){
                        // CodeSystem mit CurrentVersion aktualisieren und speichern
                        cs_db.setCurrentVersionId(csvNew.getVersionId());                       
                        
                        // TODO: N�tig?  oder reicht es nur die currentVersionId zu speichern?
                        cs_db.getCodeSystemVersions().clear();
                        cs_db.getCodeSystemVersions().add(csvNew);
                    }
                                                          
                    hb_session.update(cs_db);
                }
            } catch (Exception e) {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemVersion', Hibernate: " + e.getLocalizedMessage());

                logger.error(response.getReturnInfos().getMessage());
            } finally {
                // Transaktion abschlie�en
                if (cs_Request.getId() > 0 && csvNew.getVersionId() > 0) {
                    hb_session.getTransaction().commit();
										
										//L�schen von Verkn�pfungen um keine unendlichen XML zu erzeugen
										//csvNew.getCodeSystem().setCodeSystemVersions(null);
										csvNew.setCodeSystemVersionEntityMemberships(null);
										csvNew.setCodeSystem(null);
										csvNew.setLicenceTypes(null);
										csvNew.setLicencedUsers(null);
                } else {
                    // Änderungen nicht erfolgreich
                    logger.warn("[MaintainCodeSystemVersion.java] Änderungen nicht erfolgreich");
                    hb_session.getTransaction().rollback();
                }
                hb_session.close();
            }
            if (cs_Request.getId() > 0 && csvNew.getVersionId() > 0) {
                // Status an den Aufrufer weitergeben
                cs_Request.getCodeSystemVersions().clear();
                cs_Request.getCodeSystemVersions().add(csvNew);
                response.setCodeSystem(cs_Request);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("CodeSystemVersion erfolgreich ge�ndert. " + sCreateNewVersionMessage);
            }

        } catch (Exception e) {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemVersion': " + e.getLocalizedMessage());           
            logger.error(response.getReturnInfos().getMessage());
        }      
        return response;
    }

    private boolean validateParameter(MaintainCodeSystemVersionRequestType request, MaintainCodeSystemVersionResponseType response) {
        boolean    isValid      = true;
        String     errorMessage = "Unbekannter Fehler";
        CodeSystem cs_Request   = request.getCodeSystem();       
        
////////// Versioning //////////////////////////////////////////////////////////
        if (request.getVersioning() == null){
            errorMessage = "Versioning darf nicht NULL sein!";
            isValid = false;
        }
        else{
            if (request.getVersioning().getCreateNewVersion() == null){
                errorMessage = "createNewVersion darf nicht NULL sein!";
                isValid = false;
            }
        }
        
 ///////// CodeSystem ////////////////////////////////////////////////////////////        
        if (cs_Request == null) {
            errorMessage = "CodeSystem darf nicht NULL sein!";
            isValid    = false;
        }
        else {
            // Nur wenn der Name des CS ge�ndet werden soll, ben�tigt man die Id des CS
            if (cs_Request.getName() != null && cs_Request.getName().length() > 0 && (cs_Request.getId() == null || cs_Request.getId() < 1)){
                errorMessage = "Wenn der Name des Vokabulars ge�ndert werden soll, muss die Id des Vokabulars angegeben werden!";
                isValid = false;
            }
            else{
                Set<CodeSystemVersion> vsvSet_Request = cs_Request.getCodeSystemVersions();
                // Gibt es eine CodeSystemVersion Liste?
                if (vsvSet_Request == null){
                    errorMessage = "CodeSystemVersion-Liste darf nicht NULL sein!";
                    isValid = false;    
                }                
                else{
                    // Wenn ja, hat sie genau einen Eintrag?
                    if (vsvSet_Request.size() != 1) {
                        errorMessage = "CodeSystemVersion-Liste hat " + Integer.toString(vsvSet_Request.size()) + " Eintr�ge. Sie muss aber genau einen Eintrag haben!";
                        isValid = false;
                    } 
                    else{  
                        CodeSystemVersion csv_Request = (CodeSystemVersion)vsvSet_Request.toArray()[0]; 
                        
                        // new version
                        if(request.getVersioning().getCreateNewVersion() == true){
                            if(csv_Request.getName() == null || csv_Request.getName().length() <= 0){
                                errorMessage = "Es muss ein Name f�r die neue Version angegeben werden!";
                                isValid = false;   
                            }
                        }
                            
                        // edit version
                        else{
                            if (csv_Request.getVersionId() == null || csv_Request.getVersionId() < 1) {
                                errorMessage = "Die Id der Vokabularversion darf nicht NULL oder kleiner 1 sein!";
                                isValid = false;
                            }
                        }
                        
                        // falls licenceTypes angegeben sind, m�ssen diese auch eine id und typeTxt haben
                        if(csv_Request.getUnderLicence() == null){}else{
                            if(isValid && csv_Request.getUnderLicence()){
                               if(csv_Request.getLicenceTypes().size() > 0){
                                    Iterator<LicenceType> iLicenceType_Request = csv_Request.getLicenceTypes().iterator();
                                    LicenceType licence_Request;
                                    while(iLicenceType_Request.hasNext()){
                                        licence_Request = iLicenceType_Request.next();
                                        if(licence_Request.getId() == null || licence_Request.getId() < 1){
                                            errorMessage = "LicenceType.getId() darf nicht NULL oder kleiner 1 sein!"  + " Size="+Integer.toString(csv_Request.getLicenceTypes().size());
                                            isValid = false;                                    
                                        }
                                        if(licence_Request.getTypeTxt() == null || licence_Request.getTypeTxt().length() == 0 ){
                                            errorMessage = "LicenceType.getTypeTxt() darf nicht NULL sein oder eine L�nge von 0 haben!";
                                            isValid = false;                                    
                                        }                                    
                                    }
                                }                                                                                
                            }
                        }
                    }
                }                                
            }
        }

        if (isValid == false){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage(errorMessage);
        }       
        return isValid;
    }
}