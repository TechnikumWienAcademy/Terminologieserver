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
 * V 3.3 OK
 * @author Bernhard Rimatzki
 */
public class ExportCodeSystemContent{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Checks the export parameters and calls the export function, which suits the export format ID given in the parameters.
     * @param parameter the export parameters.
     * @return a response with info about the export.
    */
    public ExportCodeSystemContentResponseType ExportCodeSystemContent(ExportCodeSystemContentRequestType parameter){
        LOGGER.info("+++++ ExportCodeSystemContent started +++++");
    
        ExportCodeSystemContentResponseType response = new ExportCodeSystemContentResponseType();
        response.setReturnInfos(new ReturnType());
    
        if(StaticExportStatus.getActiveSessions() < StaticExportStatus.getMAX_SESSIONS())
            StaticExportStatus.increaseActiveSessions();
        else{
            response.getReturnInfos().setMessage("Maximale Anzahl an Export Sessions erreicht. Bitte Versuchen Sie es später wieder.");
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP503);
            LOGGER.info("----- ExportCodeSystemContent finished (001) -----");
            return response;
        }

        //Check parameters
        if (validateParameter(parameter, response) == false){
            StaticExportStatus.decreaseActiveSessions();
            LOGGER.info("----- ExportCodeSystemContent finished (002) -----");
            return response; // Parameter check failed
        }

        if (parameter == null || parameter.getExportInfos() == null || parameter.getExportInfos().getFormatId() == null){
            StaticExportStatus.decreaseActiveSessions();
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Request -> ExportInfos -> Format-ID darf nicht null sein.");
            LOGGER.info("----- ExportCodeSystemContent finished (003) -----");
            return response;
        }

        long formatId = parameter.getExportInfos().getFormatId();

        if (formatId == ExportCodeSystemContentRequestType.EXPORT_CLAML_ID){
            try{
                ExportClaml exportClaML = new ExportClaml();
                response = exportClaML.exportClaml(parameter);
            }
            catch (Exception e){
                LOGGER.error("Error [0059]: " + e.getLocalizedMessage(), e);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim CLAML-Export: " + e.getLocalizedMessage());
            }
        }
        else if (formatId == ExportCodeSystemContentRequestType.EXPORT_CSV_ID){
            try{
                //TODO check class
                ExportCSV exportCSV = new ExportCSV(parameter);
                String exportResponseString = exportCSV.exportCSV(response);

                if (exportResponseString.length() == 0){
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(exportCSV.getCountExported());
                }
                else{
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                    response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + exportResponseString);
                }
            }
            catch (Exception e){
                LOGGER.error("Error [0060]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + e.getLocalizedMessage());
            }
        }
        else if (formatId == ExportCodeSystemContentRequestType.EXPORT_SVS_ID){
            try{
                //TODO check class
                ExportCodeSystemSVS exportSVS = new ExportCodeSystemSVS(parameter);
                String exportResponseString = exportSVS.exportSVS(response);

                if (exportResponseString.length() == 0){
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setCount(exportSVS.getCountExported());
                }
                else{
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                    response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + exportResponseString);
                }
            }
            catch (Exception e){
                LOGGER.error("Error [0061]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + e.getLocalizedMessage());
            }
        }
        else{
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportCodeSystemContentRequestType.getPossibleFormats());
        }
        StaticExportStatus.decreaseActiveSessions();
        
        LOGGER.info("----- ExportCodeSystemContent finished (004) -----");
        return response;
    }

    /**
     * Checks if the parameters are valid.
     * @param Request the parameters to be checked.
     * @param Response the container for the return infos.
     * @return false, if the parameters are invalid.
    */
    private boolean validateParameter(ExportCodeSystemContentRequestType parameter, ExportCodeSystemContentResponseType response){
        String responseString = "";
        boolean passed = true;
        
        if(parameter.getCodeSystem() == null){
            responseString = "Es muss ein Codesytem angegeben werden.";
            passed = false;
        }
        else
            if(parameter.getCodeSystem().getId() == null || parameter.getCodeSystem().getId() == 0){
                responseString = "Es muss eine ID für ein Codesystem angegeben werden.";
                passed = false;
            }
        
        if(!passed){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage(responseString);
        }

        return passed;
    }
}