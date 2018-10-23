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

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._import.ImportVSCSV;
import de.fhdo.terminologie.ws.administration._import.ImportVSSVSNew;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportValueSetNew
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ImportValueSetResponseType ImportValueSet(ImportValueSetRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ImportValueSet gestartet ======");
        }

        // Return-Informationen anlegen
        ImportValueSetResponseType response = new ImportValueSetResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter prüfen
        if (validateParameter(parameter, response) == false)
        {
            return response; // Fehler bei den Parametern
        }

        // Login-Informationen auswerten (gilt für jeden Webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        //3.2.17 added loginAlreadyChecked
        if(parameter != null){
            if(parameter.isLoginAlreadyChecked()){
                loggedIn = true;
            }
            else if(parameter.getLogin() != null){
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
        }

        logger.debug("Eingeloggt: " + loggedIn);

        if (loggedIn == false)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
            return response;
        }

        try
        {

            long formatId = parameter.getImportInfos().getFormatId();

            if (formatId == ImportValueSetRequestType.IMPORT_CSV_ID)
            {
                //ImportClaml importClaml = new ImportClaml(parameter);
                // TODO Value-Set CSV importieren
                ImportVSCSV import_vs = new ImportVSCSV(parameter);
                import_vs.importCSV(response);
                
            }
            else if (formatId == ImportValueSetRequestType.IMPORT_SVS_ID)
            {
                ImportVSSVSNew importVS_SVS = new ImportVSSVSNew();
                importVS_SVS.setImportData(parameter);
                importVS_SVS.startImport();
                
                ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());

                if (status != null && status.isCancel())
                {
                    response.getReturnInfos().setMessage("Import abgebrochen.");
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                }
                else
                {
                    response.setValueSet(importVS_SVS.getValueset());
                    response.getReturnInfos().setMessage("Import abgeschlossen. " + status.getImportCount()+ " Konzept(e) dem Value Set hinzugefügt.\n");
                    response.getReturnInfos().setCount(status.getImportCount());
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                }
            }
            else
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ImportValueSetRequestType.getPossibleFormats());
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
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
    private boolean validateParameter(ImportValueSetRequestType Request,
            ImportValueSetResponseType Response)
    {
        boolean erfolg = true;

        if (Request.getImportInfos() == null)
        {
            Response.getReturnInfos().setMessage("ImportInfos darf nicht NULL sein!");
            erfolg = false;
        }

        if (Request.getLogin() == null || Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
        {
            Response.getReturnInfos().setMessage("Login darf nicht NULL sein und es muss eine Session-ID angegeben sein!");
            erfolg = false;
        }

        if (Request.getValueSet() == null)
        {
            Response.getReturnInfos().setMessage("ValueSet darf nicht NULL!");
            erfolg = false;
        }
        else
        {
            ValueSet vs = Request.getValueSet();

            if ((vs.getId() == null || vs.getId() == 0) && (vs.getName() == null || vs.getName().length() == 0))
            {
                Response.getReturnInfos().setMessage("Falls die Value Set-ID 0 ist, müssen Sie einen Namen für das Value Set angeben, damit ein neues angelegt werden kann. Geben Sie also entweder eine Value Set-ID an, damit die Einträge in ein vorhandenes Value Set importiert werden oder geben Sie einen Namen an, damit ein neues erstellt wird.");
                erfolg = false;
            }
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }
}
