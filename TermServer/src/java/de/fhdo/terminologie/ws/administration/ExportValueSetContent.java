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
import de.fhdo.terminologie.ws.administration._export.ExportValueSetCSV;
import de.fhdo.terminologie.ws.administration._export.ExportValueSetSVS;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetContent
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public ExportValueSetContentResponseType ExportValueSetContent(ExportValueSetContentRequestType parameter)
  {
    if (logger.isInfoEnabled())
    {
        logger.info("====== ExportValueSetContent gestartet, ID: " + parameter.getValueSet().getId() + ", Name: " + parameter.getValueSet().getName() + ", Version: " + parameter.getValueSet().getCurrentVersionId() + " ======");
    }

    // Return-Informationen anlegen
    ExportValueSetContentResponseType response = new ExportValueSetContentResponseType();
    response.setReturnInfos(new ReturnType());
    
    if(StaticExportStatus.getActiveSessions() < StaticExportStatus.getMAX_SESSIONS())
    {
        StaticExportStatus.increaseAvtiveSessions();
    }
    else
    {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setMessage("Maximale Anzahl an Export Sessions erreicht. Bitte Versuchen Sie es später wieder.");
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP503);
        return response;
    }

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
        StaticExportStatus.decreaseAvtiveSessions();
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

    if (logger.isDebugEnabled())
        logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    //logged in = true; da sonst Export ohne Anmeldung nicht möglich ist.
    //EXTERMINATUS 3.2.1
    loggedIn = true;
    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
        StaticExportStatus.decreaseAvtiveSessions();
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP403);
        response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
        return response;
    }
    //EXTERMINATUS ENDE

    long formatId = parameter.getExportInfos().getFormatId();

    if (formatId == ExportValueSetContentRequestType.EXPORT_CSV_ID)
    {
        try
        {
            ExportValueSetCSV exportCSV = new ExportValueSetCSV(parameter);

            String s = exportCSV.exportCSV(response);

            if (s.length() == 0)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setCount(exportCSV.getCountExported());
            }
            else
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + s);
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
            response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + e.getLocalizedMessage());

            e.printStackTrace();
        }
    }
    else if(formatId == ExportValueSetContentRequestType.EXPIRT_SVS_ID)
    {
        try
        {
            ExportValueSetSVS exportSVS = new ExportValueSetSVS(parameter);

            String s = exportSVS.exportSVS(response);

            if (s.length() == 0)
            {
              response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
              response.getReturnInfos().setStatus(ReturnType.Status.OK);
              response.getReturnInfos().setCount(exportSVS.getCountExported());
            }
            else
            {
              response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
              response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
              response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
              response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + s);
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
            response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + e.getLocalizedMessage());

            e.printStackTrace();
        }
    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportValueSetContentRequestType.getPossibleFormats());
    }

    StaticExportStatus.decreaseAvtiveSessions();
    return response;

  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   * 
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ExportValueSetContentRequestType parameter, ExportValueSetContentResponseType response)
  {
    // TODO validate für ExportValueSetContent implementieren
    // (siehe Crossreference)
    return true;
  }
}
