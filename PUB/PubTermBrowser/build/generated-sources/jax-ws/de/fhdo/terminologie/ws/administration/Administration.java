
package de.fhdo.terminologie.ws.administration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.1
 * 
 */
@WebService(name = "Administration", targetNamespace = "http://administration.ws.terminologie.fhdo.de/")
@XmlSeeAlso({
    de.fhdo.terminologie.ws.administration.ObjectFactory.class,
    types.termserver.fhdo.de.ObjectFactory.class
})
public interface Administration {


    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ActualProceedingsResponseType
     */
    @WebMethod(operationName = "ActualProceedings")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ActualProceedings", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ActualProceedings")
    @ResponseWrapper(localName = "ActualProceedingsResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ActualProceedingsResponse")
    public ActualProceedingsResponseType actualProceedings(
        @WebParam(name = "parameter", targetNamespace = "")
        ActualProceedingsRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse.Return
     */
    @WebMethod(operationName = "ImportCodeSystemStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ImportCodeSystemStatus", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemStatus")
    @ResponseWrapper(localName = "ImportCodeSystemStatusResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse")
    public de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse.Return importCodeSystemStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportCodeSystemStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelResponseType
     */
    @WebMethod(operationName = "ImportCodeSystemCancel")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ImportCodeSystemCancel", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemCancel")
    @ResponseWrapper(localName = "ImportCodeSystemCancelResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelResponse")
    public ImportCodeSystemCancelResponseType importCodeSystemCancel(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportCodeSystemCancelRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.CreateDomainResponse.Return
     */
    @WebMethod(operationName = "CreateDomain")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateDomain", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.CreateDomain")
    @ResponseWrapper(localName = "CreateDomainResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.CreateDomainResponse")
    public de.fhdo.terminologie.ws.administration.CreateDomainResponse.Return createDomain(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateDomainRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ImportValueSetStatusResponse.Return
     */
    @WebMethod(operationName = "ImportValueSetStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ImportValueSetStatus", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSetStatus")
    @ResponseWrapper(localName = "ImportValueSetStatusResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSetStatusResponse")
    public de.fhdo.terminologie.ws.administration.ImportValueSetStatusResponse.Return importValueSetStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportValueSetStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse.Return
     */
    @WebMethod(operationName = "ExportValueSetContent")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ExportValueSetContent", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ExportValueSetContent")
    @ResponseWrapper(localName = "ExportValueSetContentResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse")
    public de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse.Return exportValueSetContent(
        @WebParam(name = "parameter", targetNamespace = "")
        ExportValueSetContentRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return
     */
    @WebMethod(operationName = "ImportCodeSystem")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ImportCodeSystem", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystem")
    @ResponseWrapper(localName = "ImportCodeSystemResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse")
    public de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return importCodeSystem(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportCodeSystemRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse.Return
     */
    @WebMethod(operationName = "ExportCodeSystemContent")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ExportCodeSystemContent", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ExportCodeSystemContent")
    @ResponseWrapper(localName = "ExportCodeSystemContentResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse")
    public de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse.Return exportCodeSystemContent(
        @WebParam(name = "parameter", targetNamespace = "")
        ExportCodeSystemContentRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.ImportValueSetResponse.Return
     */
    @WebMethod(operationName = "ImportValueSet")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ImportValueSet", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSet")
    @ResponseWrapper(localName = "ImportValueSetResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSetResponse")
    public de.fhdo.terminologie.ws.administration.ImportValueSetResponse.Return importValueSet(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportValueSetRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.administration.MaintainDomainResponse.Return
     */
    @WebMethod(operationName = "MaintainDomain")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainDomain", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.MaintainDomain")
    @ResponseWrapper(localName = "MaintainDomainResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.MaintainDomainResponse")
    public de.fhdo.terminologie.ws.administration.MaintainDomainResponse.Return maintainDomain(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainDomainRequestType parameter);

    /**
     * 
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "isImportValueSetPubRunning", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.IsImportValueSetPubRunning")
    @ResponseWrapper(localName = "isImportValueSetPubRunningResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.IsImportValueSetPubRunningResponse")
    public boolean isImportValueSetPubRunning();

    /**
     * 
     * @return
     *     returns de.fhdo.terminologie.ws.administration.GetImportValueSetPubResponseResponse.Return
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getImportValueSetPubResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.GetImportValueSetPubResponse")
    @ResponseWrapper(localName = "getImportValueSetPubResponseResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.GetImportValueSetPubResponseResponse")
    public de.fhdo.terminologie.ws.administration.GetImportValueSetPubResponseResponse.Return getImportValueSetPubResponse();

    /**
     * 
     * @param parameter
     */
    @WebMethod(operationName = "ImportCodeSystemPub")
    @RequestWrapper(localName = "ImportCodeSystemPub", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemPub")
    @ResponseWrapper(localName = "ImportCodeSystemPubResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportCodeSystemPubResponse")
    public void importCodeSystemPub(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportCodeSystemRequestType parameter);

    /**
     * 
     * @return
     *     returns de.fhdo.terminologie.ws.administration.GetPubImportResponseResponse.Return
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getPubImportResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.GetPubImportResponse")
    @ResponseWrapper(localName = "getPubImportResponseResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.GetPubImportResponseResponse")
    public de.fhdo.terminologie.ws.administration.GetPubImportResponseResponse.Return getPubImportResponse();

    /**
     * 
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "checkImportRunning", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.CheckImportRunning")
    @ResponseWrapper(localName = "checkImportRunningResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.CheckImportRunningResponse")
    public boolean checkImportRunning();

    /**
     * 
     * @param parameter
     */
    @WebMethod(operationName = "ImportValueSetPub")
    @RequestWrapper(localName = "ImportValueSetPub", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSetPub")
    @ResponseWrapper(localName = "ImportValueSetPubResponse", targetNamespace = "http://administration.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.administration.ImportValueSetPubResponse")
    public void importValueSetPub(
        @WebParam(name = "parameter", targetNamespace = "")
        ImportValueSetRequestType parameter);

}
