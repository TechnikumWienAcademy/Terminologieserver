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
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Set;

/**
 *
 * @author Sven Becker
 * 
 * 07.10.11
 * MaintainValueSet dient dazu, den Namen und/oder die Beschreibung eines VS zu
 * ändern. Dabei kann noch entschieben werden, ob die alte VSVersion aktualisiert
 * oder eine neue Version erstellt werden soll. Bei der Aktualisierung einer 
 * bestehenden Version, werden alle Felder, die bei der Eingabe nicht gefüllt 
 * wurden beibehalten und nicht gelöscht. Will man einen Eintrag aus der VSVersion
 * entfernen (z.B. preferredLanguageId) so muss eine neue Version angelegt werden.
 * Bei neuen Versionen werden nur die angegebenen Felder gefüllt, alle anderen 
 * bleiben leer. Evtl wird für die Übernahme von Feldern aus der vorherigen 
 * Version ein Sonderzeichen (z.B. "#") benutzt, um die Übernahme bestimmter
 * Felder zu vereinfachen.
 */
public class MaintainValueSet {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public MaintainValueSetResponseType MaintainValueSet(MaintainValueSetRequestType parameter) {
////////// Logger //////////////////////////////////////////////////////////////
        if (logger.isInfoEnabled()) 
            logger.info("====== MaintainValueSet gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
        MaintainValueSetResponseType response = new MaintainValueSetResponseType();
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
            String sCreateNewVersionMessage = "";
            
            // Hibernate-Block, Session öffnen
            org.hibernate.Session     hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            
            // ValueSet und ValueSetVersion zum Speichern vorbereiten 
            ValueSet        vs     = parameter.getValueSet();
            ValueSetVersion vsv    = (ValueSetVersion)vs.getValueSetVersions().toArray()[0];
            ValueSetVersion vsvNew = new ValueSetVersion();

            // ValueSetVersion anpassen und alle Attribute die nicht teil der Eingabe sind oder noch berechnet werden auf "null" setzen bzw füllen
            vsvNew.setConceptValueSetMemberships(null);           
            vsvNew.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum          
            vsvNew.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
            vsvNew.setStatusDate(new java.util.Date()); // Aktuelles Datum      

            try{ // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern                                             
                // versuche vs aus der DB zu laden und in vs_db, vsv_db zu speichern
                if (vs.getId() != null && vs.getId() > 0) {                    
                    ValueSet vs_db = (ValueSet)hb_session.get(ValueSet.class, vs.getId());
		
                    //Matthias: in the next line previously vs_db.getCurrentVersionId() has been queried. This has been changed.
                    //Dario: Changed back because it caused an error
                    ValueSetVersion vsv_db;
                    if (vsv != null){
                        //vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vsv.getVersionId());//vsv.getVersionId());
                        vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vs_db.getCurrentVersionId());
                    } else {
                        vsv_db = (ValueSetVersion)hb_session.get(ValueSetVersion.class, vs_db.getCurrentVersionId());
                    }
                    
                    // Name soll in der neuen Version geändert werden können
                    if(vs.getName() != null && vs.getName().length() > 0) 
                        vs_db.setName(vs.getName());

                    // Description soll in der neuen Version geändert werden können
                    if(vs.getDescription() != null) 
                        vs_db.setDescription(vs.getDescription());
                    
                    // Description (Englisch) soll in der neuen Version geändert werden können
                    if(vs.getDescriptionEng() != null) 
                        vs_db.setDescriptionEng(vs.getDescriptionEng());
                    
                    // Website soll in der neuen Version geändert werden können
                    if(vs.getWebsite() != null) 
                        vs_db.setWebsite(vs.getWebsite());
										
                    //Matthias added responsibleOrganization
                    if(vs.getResponsibleOrganization() != null){
                        vs_db.setResponsibleOrganization(vs.getResponsibleOrganization());
                    }
                    
                    // Neue Version anlegen:  Diese enthält dann nur die Angaben, die auch gemacht wurden. leere Felder werden nicht aus alten Versionen übernommen
                    if(parameter.getVersioning().getCreateNewVersion()){
                        sCreateNewVersionMessage = "Es wurde eine neue ValueSetVersion angelegt.";
                        vsvNew.setPreviousVersionId(vs_db.getCurrentVersionId());                  
                        vsvNew.setValueSet(new ValueSet());
                        vsvNew.getValueSet().setId(vs_db.getId());
                       
                        // status Date soll angegeben werden können
                        if(vsv.getStatusDate() != null) 
                            vsvNew.setStatusDate(vsv.getStatusDate());        
                        
                        // Status soll angegeben werden können
                        if(vsv.getStatus() != null) 
                            vsvNew.setStatus(vsv.getStatus());        
                        
                        // release Date soll angegeben werden können
                        if(vsv.getReleaseDate() != null) 
                            vsvNew.setReleaseDate(vsv.getReleaseDate());                      

                        // preferred Language setzen falls vorhanden
                        if(vsv.getPreferredLanguageId() != null) 
                            vsvNew.setPreferredLanguageId(vsv.getPreferredLanguageId());
                        
                        // oid setzen falls vorhanden
                        if(vsv.getOid() != null)
                            vsvNew.setOid(vsv.getOid());
                        
                        // Validity Range setzen falls vorhanden
                        if(vsv.getValidityRange() != null) 
                            vsvNew.setValidityRange(vsv.getValidityRange());   
                        
                        // Name setzen falls vorhanden
                        if(vsv.getName()!= null) 
                            vsvNew.setName(vsv.getName()); 
                        
                        // In DB speichern damit vsvNew eine ID bekommt
                        hb_session.save(vsvNew);   
                        
                        // ValueSet mit CurrentVersion aktualisieren und speichern
                        vs_db.setCurrentVersionId(vsvNew.getVersionId());
                    } 
                    // Alte Version überschreiben; Es werden nur die Felder geändert die nicht null sind. Alle anderen Felder bleiben unverändert.
                    else{
                        sCreateNewVersionMessage = "Die aktuelle ValueSetVersion wurde überschrieben.";
                        // release Date soll angegeben werden können
                        if(vsv.getReleaseDate() != null) 
                            vsv_db.setReleaseDate(vsv.getReleaseDate());                         

                        // preferred Language setzen falls vorhanden
                        if(vsv.getPreferredLanguageId() != null) 
                            vsv_db.setPreferredLanguageId(vsv.getPreferredLanguageId());                     
                        
                        // oid setzen falls vorhanden
                        if(vsv.getOid() != null)
                            vsv_db.setOid(vsv.getOid());
                        
                        // oid setzen falls vorhanden
                        if(vsv.getName() != null)
                            vsv_db.setName(vsv.getName());
                        
                        // Validity Range setzen falls vorhanden
                        if(vsv.getValidityRange() != null) 
                            vsv_db.setValidityRange(vsv.getValidityRange()); 
                        
                        // In DB speichern damit vsvNew eine ID bekommt
                        hb_session.update(vsv_db);                     
                        vsvNew = vsv_db;
                        LastChangeHelper.updateLastChangeDate(false, vsvNew.getVersionId(),hb_session);
                    }
                    
                    hb_session.update(vs_db);
                }
            } catch (Exception e) {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'MaintainValueSet', Hibernate: " + e.getLocalizedMessage());

                logger.error(response.getReturnInfos().getMessage());
            } finally {
                // Transaktion abschließen
                if (vs.getId() > 0 && vsvNew.getVersionId() > 0) {
									
                    hb_session.getTransaction().commit();
                } else {
                    // Ã„nderungen nicht erfolgreich
                    logger.warn("[MaintainValueSet.java] Ã„nderungen nicht erfolgreich");
                    hb_session.getTransaction().rollback();
                }
                hb_session.close();
            }
            if (vs.getId() > 0 && vsvNew.getVersionId() > 0) {
                // Status an den Aufrufer weitergeben
                vs.getValueSetVersions().clear();
								//Matthias: set null in order to prefent from error due to lazy fetching
								vsvNew.setConceptValueSetMemberships(null);
								vsvNew.setValueSet(null);
								
                vs.getValueSetVersions().add(vsvNew);
                response.setValueSet(vs);	
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("ValueSet erfolgreich geändert. " + sCreateNewVersionMessage);
            }

        } catch (Exception e) {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'MaintainValueSet': " + e.getLocalizedMessage());           
            logger.error(response.getReturnInfos().getMessage());
        }      
        return response;
    }

    private boolean validateParameter(MaintainValueSetRequestType request, MaintainValueSetResponseType response) {
        boolean  isValid      = true;
        String   errorMessage = "Unbekannter Fehler";
        ValueSet vs           = request.getValueSet();       
        
////////// Versioning //////////////////////////////////////////////////////////
        if (request.getVersioning() == null){
            errorMessage = "Versioning darf nicht NULL sein!";
            isValid = false;
        }
        else{
            if (request.getVersioning().getCreateNewVersion() == null){
                errorMessage = "Es muss angegeben werden, ob eine neue Version erstellt oder die aktuelle überschrieben werden soll!";
                isValid = false;
            }
        }
        
 ///////// ValueSet ////////////////////////////////////////////////////////////
        if (vs == null) {
            errorMessage = "ValueSet darf nicht NULL sein!";
            isValid    = false;
        }
        else {
            // hat VS eine ID?
            if (vs.getId() == null || vs.getId() < 1){
                errorMessage = "Es muss eine ID (>0) für das ValueSet angegeben sein!";
                isValid = false;
            }
            else{
                Set<ValueSetVersion> vsvSet = vs.getValueSetVersions();
                // Gibt es eine ValueSetVersion Liste?
                if (vsvSet == null){
                    errorMessage = "Liste ValueSetVersion darf nicht NULL sein!";
                    isValid = false;    
                }                
                else{
                    // Wenn ja, hat sie genau einen Eintrag?
                    if (vsvSet.size() != 1) {
                        errorMessage = "Die ValueSet-Version-Liste hat " + Integer.toString(vsvSet.size()) + " Einträge. Sie muss aber genau einen Eintrag haben!";
                        isValid = false;
                    } 
                    else{                        
                        ValueSetVersion vsv = (ValueSetVersion)vsvSet.toArray()[0];                            
                        // hat VSV eine gültige ID?
                        if (vsv.getVersionId() == null || vsv.getVersionId() < 1) {
                            errorMessage = "Die ID der ValueSet-Version darf nicht null sein!";
                            isValid = false;         
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