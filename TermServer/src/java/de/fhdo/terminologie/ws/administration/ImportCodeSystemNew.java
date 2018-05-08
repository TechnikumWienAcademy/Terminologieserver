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
import de.fhdo.dortmund.DortmundHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._import.ImportCSSVSNew;
import de.fhdo.terminologie.ws.administration._import.ImportCSV_ELGA;
import de.fhdo.terminologie.ws.administration._import.ImportCSV_FHDo;
import de.fhdo.terminologie.ws.administration._import.ImportClamlNew;
import de.fhdo.terminologie.ws.administration._import.ImportICDBMGAT;
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
 *
 * @author puraner
 */
public class ImportCodeSystemNew
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ImportCodeSystemResponseType ImportCodeSystem(ImportCodeSystemRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ImportCodeSystem gestartet ======");
        }

        // Return-Informationen anlegen
        ImportCodeSystemResponseType response = new ImportCodeSystemResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter prüfen
        if (validateParameter(parameter, response) == false)
        {
            return response; // Fehler bei den Parametern
        }

        // Login-Informationen auswerten (gilt für jeden Webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;

            if (loggedIn)
            {

                if (loginInfoType.getTermUser().isIsAdmin())
                {
                    loggedIn = true;
                }
                else
                {
                    loggedIn = false;
                }
            }
        }

        logger.debug("Eingeloggt: " + loggedIn);

        if (loggedIn == false)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
            return response;
        }

        long formatId = parameter.getImportInfos().getFormatId();

        if (formatId == ImportCodeSystemRequestType.IMPORT_CLAML_ID)
        {
            importClaml(parameter, response);
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_CSV_ID)
        {
            importCsv(parameter, response);
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_ID)
        {
            importLoinc(parameter, response);
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID)
        {
            try
            {
                ImportLOINC importLOINC = new ImportLOINC(parameter);

                String s = importLOINC.importLOINC_Associations(response);

                if (s.length() == 0)
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(importLOINC.getCountImported());
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler beim Import: " + s);
                }
                logger.warn("LOINC-Associations Import-Ende");
            }
            catch (Exception e)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

                e.printStackTrace();
            }
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID)
        {
            try
            {
                //StaticStatus.importRunning = true;
                ImportKBV importKBV = new ImportKBV(parameter);

                String s = importKBV.importXML(response);

                if (s.length() == 0)
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(importKBV.getCountImported());
                    //response.getReturnInfos().setMessage("Import abgeschlossen.");
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler beim Import: " + s);
                }
            }
            catch (Exception e)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

                e.printStackTrace();
            }
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_SVS_ID)
        {
            importSvs(parameter, response);
        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_LeiKat_ID)
        {

            try
            {
                ImportLeiKatAt importLeiKatAt = new ImportLeiKatAt(parameter);
                String s = importLeiKatAt.importLeiKatAt(response);

                if (s.length() == 0)
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(importLeiKatAt.getCountImported());
                    //response.getReturnInfos().setMessage("Import abgeschlossen.");
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler beim Import: " + s);
                }
            }
            catch (Exception e)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

                e.printStackTrace();
            }

        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_KAL_ID)
        {

            try
            {
                ImportKAL importKAL = new ImportKAL(parameter);
                String s = importKAL.importKAL(response);

                if (s.length() == 0)
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(importKAL.getCountImported());
                    //response.getReturnInfos().setMessage("Import abgeschlossen.");
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Fehler beim Import: " + s);
                }
            }
            catch (Exception e)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

                e.printStackTrace();
            }

        }
        else if (formatId == ImportCodeSystemRequestType.IMPORT_ICD_BMG_ID)
        {
            importIcd(parameter, response);
        }
        else
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ImportCodeSystemRequestType.getPossibleFormats());
        }

        if (parameter.getImportId() != null)
        {
            if (StaticStatusList.getStatus(parameter.getImportId()) != null)
            {
                StaticStatusList.getStatus(parameter.getImportId()).importRunning = false;
            }
        }
        else
        {
            if (StaticStatusList.getStatus(0L) != null)
            {
                StaticStatusList.getStatus(0L).importRunning = false;
            }
        }
        return response;
    }

    /**
     * Prüft die Parameter anhand der Cross-Reference
     *
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
     */
    private boolean validateParameter(ImportCodeSystemRequestType Request,
            ImportCodeSystemResponseType Response)
    {
        boolean erfolg = true;

        if (Request.getImportInfos() == null)
        {
            Response.getReturnInfos().setMessage("ImportInfos darf nicht NULL sein!");
            erfolg = false;
        }
        else
        {
            /*if (Request.getImportInfos().getFilecontent() == null)
       {
       Response.getReturnInfos().setMessage("Sie müssen eine Datei anhängen (filecontent)!");
       erfolg = false;
       }
       else*/ if (Request.getImportInfos().getFormatId() == null || Request.getImportInfos().getFormatId() == 0)
            {
                // TODO auf gültiges Format prüfen
                Response.getReturnInfos().setMessage("Sie müssen ein Import-Format angeben!");
                erfolg = false;
            }
        }

        if (Request.getLogin() == null || Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
        {
            Response.getReturnInfos().setMessage("Login darf nicht NULL sein und es muss eine Session-ID angegeben sein!");
            erfolg = false;
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }

    private void importClaml(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
    {
        try
        {
            ImportClamlNew importClamlNew = new ImportClamlNew();
            importClamlNew.setImportData(parameter);

            importClamlNew.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel())
            {
                response.getReturnInfos().setMessage("Import abgebrochen.");
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
            else
            {
                response.setCodeSystem(importClamlNew.getCodeSystem());
                response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert.");
                response.getReturnInfos().setCount(status.getImportCount());
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            }
        }
        catch (ImportException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
    }

    private void importCsv(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
    {
        try
        {
            String s = "";
            int countImported = 0;

            if (DortmundHelper.getInstance().isFhDortmund())
            {
                ImportCSV_FHDo importCSV = new ImportCSV_FHDo(parameter);

                s = importCSV.importCSV(response);
                countImported = importCSV.getCountImported();
            }
            else
            {
                ImportCSV_ELGA importCSV = new ImportCSV_ELGA(parameter);

                s = importCSV.importCSV(response);
                countImported = importCSV.getCountImported();
            }

            if (s.length() == 0)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setCount(countImported);
            }
            else
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler beim Import: " + s);
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

            e.printStackTrace();
        }
    }

    private void importSvs(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
    {
        try
        {
            ImportCSSVSNew importCSSVSNew = new ImportCSSVSNew();
            importCSSVSNew.setImportData(parameter);

            importCSSVSNew.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel())
            {
                response.getReturnInfos().setMessage("Import abgebrochen.");
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
            else
            {
                response.setCodeSystem(importCSSVSNew.getCodeSystem());
                response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importCSSVSNew.getCountFehler() + " Fehler | " + importCSSVSNew.getSortMessage());
                response.getReturnInfos().setCount(status.getImportCount());
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            }
        }
        catch (ImportException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException ex)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + ex.getLocalizedMessage());
        }
    }

    private void importIcd(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
    {
        try
        {
            ImportICDBMGATNew importICDBMGAT = new ImportICDBMGATNew();
            importICDBMGAT.setImportData(parameter);

            importICDBMGAT.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel())
            {
                response.getReturnInfos().setMessage("Import abgebrochen.");
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
            else
            {
                response.setCodeSystem(importICDBMGAT.getCodeSystem());
                response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importICDBMGAT.getCountFehler() + " Fehler");
                response.getReturnInfos().setCount(status.getImportCount());
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            }
        }
        catch (ImportException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException ex)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + ex.getLocalizedMessage());
        }
    }

    private void importLoinc(ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
    {
        try
        {
            ImportLOINCNew importLOINC = new ImportLOINCNew();

            importLOINC.setImportData(parameter);
            importLOINC.startImport();

            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

            if (status != null && status.isCancel())
            {
                response.getReturnInfos().setMessage("Import abgebrochen.");
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
            else
            {
                if (parameter.getImportInfos().getOrder())
                {
                    if (importLOINC.getCountFehler() == 0)
                    {
                        response.setCodeSystem(importLOINC.getCodeSystem());
                        response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + status.getImportCount() + " Konzept(en). " + importLOINC.getNewCount() + " Konzepte wurden neu importiert. " + importLOINC.getCountFehler() + " Fehler");
                        response.getReturnInfos().setCount(status.getImportCount());
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    }
                    else
                    {
                        response.setCodeSystem(importLOINC.getCodeSystem());
                        response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + status.getImportCount() + " Konzept(en). " + importLOINC.getNewCount() + " Konzepte wurden neu importiert. " + importLOINC.getCountFehler() + " Fehler; " + "Daten auf Grund der Fehler nicht in der Datenbank gespeichert.");
                        response.getReturnInfos().setCount(status.getImportCount());
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    }
                }
                else
                {
                    response.setCodeSystem(importLOINC.getCodeSystem());
                    response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount() + " Konzept(e) importiert, " + importLOINC.getCountFehler() + " Fehler");
                    response.getReturnInfos().setCount(status.getImportCount());
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                }
            }
        }
        catch (ImportException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
        catch (ImportParameterValidationException e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
        }
    }
}
