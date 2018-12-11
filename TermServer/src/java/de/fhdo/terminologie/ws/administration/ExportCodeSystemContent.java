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

import de.fhdo.terminologie.ws.administration._export.ExportCSV;
import de.fhdo.terminologie.ws.administration._export.ExportClaml;
import de.fhdo.terminologie.ws.administration._export.ExportCodeSystemSVS;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Bernhard Rimatzki
 */
public class ExportCodeSystemContent
{

  final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Creates a new domain with the given parameters.
   *
   * @param parameter
   * @return Return value of the webservice
   */
  public ExportCodeSystemContentResponseType ExportCodeSystemContent(ExportCodeSystemContentRequestType parameter)
  {
    if (logger.isInfoEnabled())
    {  
        logger.info("====== ExportCodeSystemContent started, ID: " + parameter.getCodeSystem().getCurrentVersionId() + ", name: " + parameter.getCodeSystem().getName() + ", version: " + parameter.getCodeSystem().getCurrentVersionId() +  " ======");
    }
    
    // Create return information
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

    // Check parameters
    if (validateParameter(parameter, response) == false)
    {
        StaticExportStatus.decreaseAvtiveSessions();
        //response is set during validateParameter
        return response; // Parameter check failed
    }

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
            response = exportClaML.export(parameter);
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
            response.getReturnInfos().setMessage("Fehler beim ClaML-Export: " + e.getLocalizedMessage());
        }
    }
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_CSV_ID)
    {
      try
      {
        ExportCSV exportCSV = new ExportCSV(parameter);
        String exportResponseString = exportCSV.exportCSV(response);

        if (exportResponseString.length() == 0)
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
          response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + exportResponseString);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
        response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + e.getLocalizedMessage());
      }
    }
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_SVS_ID)
    {
      try
      {
        ExportCodeSystemSVS exportSVS = new ExportCodeSystemSVS(parameter);
        String exportResponseString = exportSVS.exportSVS(response);

        if (exportResponseString.length() == 0)
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
            response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + exportResponseString);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
        response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + e.getLocalizedMessage());
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
   * Checks the parameters with the cross-reference.
   * TODO
   * @param Request
   * @param Response
   * @return false, if the parameters are invalid
   */
  private boolean validateParameter(ExportCodeSystemContentRequestType parameter, ExportCodeSystemContentResponseType response)
  {
    String responseString = "";
    if(parameter.getCodeSystem() == null)
    {
      responseString = "Es muss ein Codesystem mitgegeben werden.";
    }
    else
    {
      if(parameter.getCodeSystem().getId() == null || parameter.getCodeSystem().getId() == 0)
      {
        responseString = "Es muss eine ID für ein Codesystem mitgegeben werden.";
      }
    }

    if(responseString.length() > 0)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage(responseString);
      return false;
    }

    return true;
  }
}
