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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._import.ImportCSSVSNew;
import de.fhdo.terminologie.ws.administration._import.ImportCSV_ELGA;
import de.fhdo.terminologie.ws.administration._import.ImportClamlNew;
import de.fhdo.terminologie.ws.administration._import.ImportICDBMGATNew;
import de.fhdo.terminologie.ws.administration._import.ImportKAL;
import de.fhdo.terminologie.ws.administration._import.ImportKBV;
import de.fhdo.terminologie.ws.administration._import.ImportLOINC;
import de.fhdo.terminologie.ws.administration._import.ImportLOINCNew;
import de.fhdo.terminologie.ws.administration._import.ImportLeiKatAt;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 * 3.2.26 checked
 * @author Stefan Puraner
 */
public class ImportCodeSystemNew{
    
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Validates the parameters, checks the login and then calls the import class, which corresponds to the import format.
     * @param parameter the import data
     * @return info about the import
     */
    public ImportCodeSystemResponseType ImportCodeSystem(ImportCodeSystemRequestType parameter){
        LOGGER.info("+++++ ImportCodeSystemNew gestartet +++++");

        //Creating return information
        ImportCodeSystemResponseType response = new ImportCodeSystemResponseType();
        response.setReturnInfos(new ReturnType());

        //Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- ImportCodeSystemNew finished (001) -----");
            return response; //Faulty parameters
        }

        //Check login
        boolean loggedIn = false;
        LoginInfoType loginInfoType;
        //3.2.17 added second check
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;

            if (loggedIn && !loginInfoType.getTermUser().isIsAdmin())
                    loggedIn = false;
        }

        if (loggedIn == false){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
            LOGGER.info("----- ImportCodeSystemNew finished (002) -----");
            return response;
        }

        long formatID = parameter.getImportInfos().getFormatId();
        
        if (formatID == ImportCodeSystemRequestType.IMPORT_CLAML_ID){
            importClaml(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_CSV_ID){
            importCSV(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_LOINC_ID){
            importLoinc(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID){
            importLoincAssociations(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID){
            importKBV(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_SVS_ID){
            importSVS(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_LeiKat_ID){
            importLeiKatAt(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_KAL_ID){
            importKAL(parameter, response);
        }
        else if (formatID == ImportCodeSystemRequestType.IMPORT_ICD_BMG_ID){
            importIcd(parameter, response);
        }
        else{
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatID + "\n" + ImportCodeSystemRequestType.getPossibleFormats());
        }

        if(parameter.getImportId() != null){
            if (StaticStatusList.getStatus(parameter.getImportId()) != null)
                StaticStatusList.getStatus(parameter.getImportId()).importRunning = false;
        }
        else{
            if (StaticStatusList.getStatus(0L) != null)
                StaticStatusList.getStatus(0L).importRunning = false;
        }
        
        LOGGER.info("----- ImportCodeSystemNew finished (003) -----");
        return response;
    }

    /**
     * Checks the parameters via cross-reference.     *
     * @param Request the parameters to be checked
     * @param Response info about the overall progress of the import
     * @return false if one of the parameters is missing or invalid
     */
    private boolean validateParameter(ImportCodeSystemRequestType Request, ImportCodeSystemResponseType Response){
        boolean erfolg = true;

        if (Request.getImportInfos() == null){
            erfolg = false;
            Response.getReturnInfos().setMessage("ImportInfos darf nicht null sein!");
        }
        else if (Request.getImportInfos().getFormatId() == null || Request.getImportInfos().getFormatId() == 0){
            erfolg = false;
            Response.getReturnInfos().setMessage("Import-Format darf nicht null sein!");
        }
        else{
            long formatID = Request.getImportInfos().getFormatId();
            if(formatID != ImportCodeSystemRequestType.IMPORT_CLAML_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_CSV_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_ICD_BMG_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_KAL_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_LOINC_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_LeiKat_ID &&
                formatID != ImportCodeSystemRequestType.IMPORT_SVS_ID){
                    erfolg = false;
                    Response.getReturnInfos().setMessage("Es muss ein gültiges Import-Format angegeben sein.");
            }
        }
        
        if (Request.getLogin() == null || Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0){
            erfolg = false;
            Response.getReturnInfos().setMessage("Login darf nicht null sein und es muss eine Session-ID angegeben sein!");
        }

        if (!erfolg){
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        }

        return erfolg;
    }

    /**
     * Calls ImportClamlNew to import the code system.
     * @param parameter the import data for ImportClamlNew
     * @param response info about the execution of the function
     */
    private void importClaml(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importClaml started +++++");
        try{
            //TODO Klasse prüfen
            ImportClamlNew importClamlNew = new ImportClamlNew();
            importClamlNew.setImportData(parameter);
            importClamlNew.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel()){
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Import abgebrochen.");
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.setCodeSystem(importClamlNew.getCodeSystem());
                if(status != null){
                    response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert.");
                    response.getReturnInfos().setCount(status.getImportCount());
                }
                else{
                    response.getReturnInfos().setMessage("Import abgeschlossen. Anzahl der importierten Konzepte unbekannt.");
                    response.getReturnInfos().setCount(-1);
                }
            }
        }
        catch (ImportException e){
            LOGGER.error("Error [0003]", e);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0003]: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException e){
            LOGGER.error("Error [0004]", e);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0004]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importClaml finished (001) -----");
    }

    /**
     * Calls ImportCSV_ELGA to import the code system.
     * @param parameter the import data for ImportCSV_ELGA
     * @param response infos about the execution of the function
     */
    private void importCSV(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importCSV started +++++");
        try{
            //TODO Klasse prüfen
            ImportCSV_ELGA importCSV = new ImportCSV_ELGA(parameter);

            String responseInfo = importCSV.importCSV(response);
            int countImported = importCSV.getCountImported();
            
            if (responseInfo.length() == 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setCount(countImported);
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Fehler beim Import [0005]: " + responseInfo);
            }
        }
        catch (Exception e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0006]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importCSV finished (001) -----");
    }
    
    /**
     * Calls ImportLoinc to import Loinc associations.
     * @param parameter the import data for ImportLoinc
     * @param response infos about the execution of the function
     */
    private void importLoincAssociations(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importLoincAssociations started +++++");
        try{
            //TODO Klasse prüfen, importLOINC_Associations in ImportLOINCNew umbauen
            ImportLOINC importLOINC = new ImportLOINC(parameter);
            String responseInfo = importLOINC.importLOINC_Associations(response);

            if (responseInfo.length() == 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setCount(importLOINC.getCountImported());
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Fehler beim Import [0008]: " + responseInfo);
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0009]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importLoincAssociations finished (001) -----");
    }

    /**
     * Calls ImportCSSVSNew to import the SVS data.
     * @param parameter the import data
     * @param response infos about the import
     */
    private void importSVS(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importSVS started +++++");
        try{
            //TODO Klasse prüfen
            ImportCSSVSNew importCSSVSNew = new ImportCSSVSNew();
            importCSSVSNew.setImportData(parameter);
            importCSSVSNew.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel()){
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Import abgebrochen.");
            }
            else{
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importCSSVSNew.getCountFehler() + " Fehler | " + importCSSVSNew.getSortMessage());
                response.setCodeSystem(importCSSVSNew.getCodeSystem());
                response.getReturnInfos().setCount(status.getImportCount());
            }
        }
        catch (ImportException e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0012]: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException ex){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0013]: " + ex.getLocalizedMessage());
        }
        
        LOGGER.info("----- importSVS finished (001) -----");
    }

    /**
     * Calls ImportICDBMGATNew to import the code system.
     * @param parameter the import data
     * @param response infos about the import
     */
    private void importIcd(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importIcd started +++++");
        try{
            //TODO Klasse prüfen
            ImportICDBMGATNew importICDBMGAT = new ImportICDBMGATNew();
            importICDBMGAT.setImportData(parameter);
            importICDBMGAT.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel()){
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Import abgebrochen.");
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importICDBMGAT.getCountFehler() + " Fehler");
                response.setCodeSystem(importICDBMGAT.getCodeSystem());
                response.getReturnInfos().setCount(status.getImportCount());
            }
        }
        catch (ImportException e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0016]: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException ex){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0017]: " + ex.getLocalizedMessage());
        }
        
        LOGGER.info("----- importIcd finished (001) -----");
    }

    /**
     * Calls ImportLOINCNew to import the LOINC.
     * @param parameter the import data for ImportLOINCNew
     * @param response infos about the execution of the function
     */
    private void importLoinc(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response){
        LOGGER.info("+++++ importLoinc started +++++");
        try{
            //TODO Klasse prüfen
            ImportLOINCNew importLOINC = new ImportLOINCNew();
            importLOINC.setImportData(parameter);
            importLOINC.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel()){
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Import abgebrochen.");
            }
            else{
                if (parameter.getImportInfos().getOrder()){
                    if (importLOINC.getCountFehler() == 0){
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                        response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + status.getImportCount() + " Konzept(en). " + importLOINC.getNewCount() + " Konzepte wurden neu importiert. " + importLOINC.getCountFehler() + " Fehler");
                        response.setCodeSystem(importLOINC.getCodeSystem());
                        response.getReturnInfos().setCount(status.getImportCount());
                    }
                    else{
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                        response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + status.getImportCount() + " Konzept(en). " + importLOINC.getNewCount() + " Konzepte wurden neu importiert. " + importLOINC.getCountFehler() + " Fehler; " + "Daten auf Grund der Fehler nicht in der Datenbank gespeichert.");
                        response.setCodeSystem(importLOINC.getCodeSystem());
                        response.getReturnInfos().setCount(status.getImportCount());
                    }
                }
                else{
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importLOINC.getCountFehler() + " Fehler");
                    response.setCodeSystem(importLOINC.getCodeSystem());
                    response.getReturnInfos().setCount(status.getImportCount());                    
                }
            }
        }
        catch (ImportException e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0006]: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0007]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importLoinc finished (001) -----");
    }

    /**
     * Calls ImportKBV to import keytabs.
     * @param parameter the import data for ImportKBV
     * @param response infos about the execution of the function
     */
    private void importKBV(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response) {
        LOGGER.info("+++++ importKBV started +++++");
        try{
            //TODO Klasse prüfen
            ImportKBV importKBV = new ImportKBV(parameter);
            String responseInfos = importKBV.importXML(response);

            if (responseInfos.length() == 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setCount(importKBV.getCountImported());
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Fehler beim Import [0010]: " + responseInfos);
            }
        }
        catch (Exception e){
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0011]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importKBV finished (001) -----");
    }

    /**
     * Calls ImportLeiKatAt to import the code system.
     * @param parameter the import data
     * @param response infos about the import
     */
    private void importLeiKatAt(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response) {
        LOGGER.info("+++++ importLeiKatAt started +++++");
        try{
            //TODO Klasse prüfen
            ImportLeiKatAt importLeiKatAt = new ImportLeiKatAt(parameter);
            String responseInfos = importLeiKatAt.importLeiKatAt(response);

            if (responseInfos.length() == 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setCount(importLeiKatAt.getCountImported());
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Fehler beim Import [0014]: " + responseInfos);
            }
        }
        catch (Exception e){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import [0015]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importLeiKatAt finished (001) -----");
    }

    private void importKAL(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response) {
        LOGGER.info("+++++ importKAL started +++++");
        try{
            //TODO Klasse prüfen
            ImportKAL importKAL = new ImportKAL(parameter);
            String responseInfo = importKAL.importKAL(response);

            if (responseInfo.length() == 0){
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setCount(importKAL.getCountImported());
            }
            else{
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setMessage("Fehler beim Import [0018]: " + responseInfo);
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Fehler beim Import [0019]: " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- importKAL finished (001) -----");
    }
}