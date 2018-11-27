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

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainResponseType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetStatusRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetStatusResponseType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainResponseType;
import java.io.IOException;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernhard Rimatzki
 */
@WebService(serviceName = "Administration")
public class Administration
{
    //3.2.20
    @Resource
    public boolean importRunning;
    
  private static Logger logger = Logger4j.getInstance().getLogger();
    
  //3.2.20
  @WebMethod(operationName = "checkImportRunning")
  public boolean checkImportRunning(){
      return importRunning;
  }
  
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.
  @Resource
  private WebServiceContext webServiceContext;
  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateDomain")
  public CreateDomainResponseType CreateDomain(@WebParam(name = "parameter") CreateDomainRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    CreateDomain cd = new CreateDomain();
    return cd.CreateDomain(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainDomain")
  public MaintainDomainResponseType MaintainDomain(@WebParam(name = "parameter") MaintainDomainRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    MaintainDomain md = new MaintainDomain();
    return md.MaintainDomain(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystem")
  public ImportCodeSystemResponseType ImportCodeSystem(@WebParam(name = "parameter") ImportCodeSystemRequestType parameter)
  {
      //3.2.20 next line
      importRunning = true;
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    
       //preparation for new and more stable imports
      ImportCodeSystemNew nics = new ImportCodeSystemNew();
      ImportCodeSystemResponseType response = nics.ImportCodeSystem(parameter);
      //3.2.20 next line
      importRunning = false;
      return response;
    
    /*
    ImportCodeSystem ics = new ImportCodeSystem();
    return ics.ImportCodeSystem(parameter);
      */
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystemStatus")
  public ImportCodeSystemStatusResponseType ImportCodeSystemStatus(@WebParam(name = "parameter") ImportCodeSystemStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    ImportCodeSystemStatus ics = new ImportCodeSystemStatus();
    return ics.ImportCodeSystemStatus(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ExportCodeSystemContent")
  public ExportCodeSystemContentResponseType ExportCodeSystemContent(@WebParam(name = "parameter") ExportCodeSystemContentRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    ExportCodeSystemContent ecsc = new ExportCodeSystemContent();
    ExportCodeSystemContentResponseType returnValue = ecsc.ExportCodeSystemContent(parameter);
    
    try{
        MessageContext context = webServiceContext.getMessageContext();
        HttpServletResponse response = (HttpServletResponse)context.get(MessageContext.SERVLET_RESPONSE);
        if(ecsc.ExportCodeSystemContent(parameter).getReturnInfos().getHttpStatus()!=null)
            switch(ecsc.ExportCodeSystemContent(parameter).getReturnInfos().getHttpStatus()){
                case HTTP403: {
                    response.sendError(403, "403 Forbidden");
                    logger.warn("403 Forbidden");
                }
                break;
                case HTTP409: {
                    response.sendError(409, "409 Conflict");
                    logger.warn("409 Conflict");
                }
                break;
                case HTTP500: {
                    response.sendError(500, "500 Internal Server Error");
                    logger.warn("500 Internal Server Error");
                }
                break;
                case HTTP503: {
                    response.sendError(503, "503 Service Unavailable");
                    logger.warn("503 Service Unavailable");
                }
                break;
                default:;
            }        
    }
    catch(IOException e){
       logger.error("Error sending HTTP-Status.");
    }
    
    return returnValue;
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ExportValueSetContent")
  public ExportValueSetContentResponseType ExportValueSetContent(@WebParam(name = "parameter") ExportValueSetContentRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    ExportValueSetContent evsc = new ExportValueSetContent();
    ExportValueSetContentResponseType returnValue = evsc.ExportValueSetContent(parameter);
    
    try{
        MessageContext context = webServiceContext.getMessageContext();
        HttpServletResponse response = (HttpServletResponse)context.get(MessageContext.SERVLET_RESPONSE);
        if(evsc.ExportValueSetContent(parameter).getReturnInfos().getHttpStatus()!=null)
            switch(evsc.ExportValueSetContent(parameter).getReturnInfos().getHttpStatus()){
                case HTTP403: {
                    response.sendError(403, "403 Forbidden");
                    logger.warn("403 Forbidden");
                }
                break;
                case HTTP409: {
                    response.sendError(409, "409 Conflict");
                    logger.warn("409 Conflict");
                }
                break;
                case HTTP500: {
                    response.sendError(500, "500 Internal Server Error");
                    logger.warn("500 Internal Server Error");
                }
                break;
                case HTTP503: {
                    response.sendError(503, "503 Service Unavailable");
                    logger.warn("503 Service Unavailable");
                }
                break;
                default:;
            }        
    }
    catch(IOException e){
       logger.error("Error sending HTTP-Status.");
    }
    return returnValue;
  }

  /**
   * Web service operation
   */
  /*@WebMethod(operationName = "CheckImportStatus")
  public CheckImportStatusResponseType CheckImportStatus()
  {
    CheckImportStatusResponseType response = new CheckImportStatusResponseType();
    response.setReturnInfos(new ReturnType());

    if (ImportClaml.isRunning)
    {
      response.setPercentComplete(ImportClaml.percentageComplete);
      response.setCurrentTask(ImportClaml.currentTask);
      
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Status gelesen");
    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Der Import läuft zur Zeit nicht.");
    }
    return response;
  }*/

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystemCancel")
  public ImportCodeSystemCancelResponseType ImportCodeSystemCancel(@WebParam(name = "parameter") ImportCodeSystemCancelRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    ImportCodeSystemCancel ics = new ImportCodeSystemCancel();
    return ics.ImportCodeSystemCancel(parameter);
  }
  
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportValueSet")
  public ImportValueSetResponseType ImportValueSet(@WebParam(name = "parameter") ImportValueSetRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
    ImportValueSetNew nics = new ImportValueSetNew();
    return nics.ImportValueSet(parameter);
    
    /*
    ImportValueSet ics = new ImportValueSet();
    return ics.ImportValueSet(parameter);
    */
  }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "ActualProceedings")
    public ActualProceedingsResponseType ActualProceedings(@WebParam(name = "parameter") ActualProceedingsRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
        ActualProceedings rap = new ActualProceedings();
        return rap.ActualProceedings(parameter);
    }
    
    /**
   * Web service operation
     * @return 
   */
  @WebMethod(operationName = "ImportValueSetStatus")
  public ImportValueSetStatusResponseType ImportValueSetStatus(@WebParam(name = "parameter") ImportValueSetStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    
      ImportValueSetStatus ivs = new ImportValueSetStatus();
    return ivs.ImportValueSetStatus(parameter);
  }
}
