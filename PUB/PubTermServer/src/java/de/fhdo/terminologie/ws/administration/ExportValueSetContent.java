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

import de.fhdo.terminologie.ws.administration._export.ExportValueSetCSV;
import de.fhdo.terminologie.ws.administration._export.ExportValueSetSVS;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner
 */
public class ExportValueSetContent{
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
    public ExportValueSetContentResponseType ExportValueSetContent(ExportValueSetContentRequestType parameter){
        LOGGER.info("+++++ ExportValueSetContent started +++++");
        
        //Creating return information
        ExportValueSetContentResponseType response = new ExportValueSetContentResponseType();
        response.setReturnInfos(new ReturnType());
    
        if(StaticExportStatus.getActiveSessions() < StaticExportStatus.getMAX_SESSIONS())
            StaticExportStatus.increaseActiveSessions();
        else{
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setMessage("Maximale Anzahl an Export Sessions erreicht. Bitte Versuchen Sie es später wieder.");
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP503);
            LOGGER.info("----- ExportValueSetContent finished (001) -----");
            return response;
        }

        //Checking parameters
        if (validateParameter(parameter, response) == false){
            StaticExportStatus.decreaseActiveSessions();
            LOGGER.info("----- ExportValueSetContent finished (002) -----");
            return response; //Faulty parameters
        }

        long formatId = parameter.getExportInfos().getFormatId();

        if (formatId == ExportValueSetContentRequestType.EXPORT_CSV_ID){
            try{
                //TODO check class
                ExportValueSetCSV exportCSV = new ExportValueSetCSV(parameter);
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
                LOGGER.error("Error [0081]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + e.getLocalizedMessage());
            }
        }
        else if(formatId == ExportValueSetContentRequestType.EXPIRT_SVS_ID){
            try{
                //TODO check class
                ExportValueSetSVS exportSVS = new ExportValueSetSVS(parameter);
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
                LOGGER.error("Error [0082]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP500);
                response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + e.getLocalizedMessage());
            }
        }
        else{
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportValueSetContentRequestType.getPossibleFormats());
        }

        StaticExportStatus.decreaseActiveSessions();
        
        LOGGER.info("----- ExportValueSetContent finished (003) -----");
        return response;
    }

    /**
     * Prüft die Parameter anhand der Cross-Reference
     * 
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
    */
    private boolean validateParameter(ExportValueSetContentRequestType parameter, ExportValueSetContentResponseType response){
        //TODO
        return true;
    }
}