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

import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCodeSystemStatus
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ImportCodeSystemStatusResponseType ImportCodeSystemStatus(ImportCodeSystemStatusRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ImportCodeSystem ImportCodeSystemStatus ======");
        }

        // Return-Informationen anlegen
        ImportCodeSystemStatusResponseType response = new ImportCodeSystemStatusResponseType();
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
        }

        if (loggedIn == false)
        {
            // Benutzer muss für diesen Webservice eingeloggt sein
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
            return response;
        }

        // Status wiedergeben
        if (parameter.getImportId() != null)
        {
            ImportStatus status = StaticStatusList.getStatus(parameter.getImportId());
            if (status != null)
            {
                response.setIsRunning(status.exportRunning);
                response.setCurrentIndex(status.importCount);
                response.setTotalCount(status.importTotal);
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
    private boolean validateParameter(ImportCodeSystemStatusRequestType Request,
            ImportCodeSystemStatusResponseType Response)
    {
        boolean erfolg = true;

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

        if (Request.getImportId() == null)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            Response.getReturnInfos().setMessage("Import Id ist unbekannt. (null)");
            erfolg = false;
        }

        return erfolg;
    }
}
