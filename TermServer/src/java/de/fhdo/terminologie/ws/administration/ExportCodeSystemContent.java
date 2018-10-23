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
import de.fhdo.terminologie.ws.administration._export.ExportCSV;
import de.fhdo.terminologie.ws.administration._export.ExportClaml;
import de.fhdo.terminologie.ws.administration._export.ExportCodeSystemSVS;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Bernhard Rimatzki
 */
public class ExportCodeSystemContent
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Erstellt eine neue Domäne mit den angegebenen Parametern
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public ExportCodeSystemContentResponseType ExportCodeSystemContent(ExportCodeSystemContentRequestType parameter)
  {
    if (logger.isInfoEnabled())
    {  
        logger.info("====== ExportCodeSystemContent gestartet, ID: " + parameter.getCodeSystem().getCurrentVersionId() + ", Name: " + parameter.getCodeSystem().getName() + ", Version: " + parameter.getCodeSystem().getCurrentVersionId() +  " ======");
    }
    
    // Return-Informationen anlegen
    ExportCodeSystemContentResponseType response = new ExportCodeSystemContentResponseType();
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
    //3.2.17 added check for alreadylogged in
    if (parameter != null && !parameter.getLoginAlreadyChecked() && parameter.getLogin() != null)
    {
        loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
        loggedIn = loginInfoType != null;
    }

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    loggedIn = true;

    if (parameter == null || parameter.getExportInfos() == null || parameter.getExportInfos().getFormatId() == null)
    {
        StaticExportStatus.decreaseAvtiveSessions();
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Request->ExportInfos->formatId darf nicht NULL sein!");
        return response;
    }

    long formatId = parameter.getExportInfos().getFormatId();

    if (formatId == ExportCodeSystemContentRequestType.EXPORT_CLAML_ID)
    {
      try
      {
        ExportClaml exportClaML = new ExportClaml();
        //3.2.17 check if paramter for alreadyloggedin has to be set
        response = exportClaML.export(parameter);

        /*response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
         response.getReturnInfos().setStatus(ReturnType.Status.OK);
         response.getReturnInfos().setMessage("ClaML-Export abgeschlossen.");
         response.getReturnInfos().setCount(exportClaML.getCountImported());*/
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
        response.getReturnInfos().setMessage("Fehler beim ClaML-Export: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_CSV_ID)
    {
      try
      {
        ExportCSV exportCSV = new ExportCSV(parameter);

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
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_SVS_ID)
    {
      try
      {
        ExportCodeSystemSVS exportSVS = new ExportCodeSystemSVS(parameter);

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
      response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportCodeSystemContentRequestType.getPossibleFormats());
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
  private boolean validateParameter(ExportCodeSystemContentRequestType parameter, ExportCodeSystemContentResponseType response)
  {
    String s = "";
    if(parameter.getCodeSystem() == null)
    {
      s = "Es muss ein Codesystem mitgegeben werden.";
    }
    else
    {
      if(parameter.getCodeSystem().getId() == null || parameter.getCodeSystem().getId().longValue() == 0)
      {
        s = "Es muss eine ID für ein Codesystem mitgegeben werden.";
      }
    }

    if(s.length() > 0)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage(s);
      return false;
    }
    

    return true;
  }
}
