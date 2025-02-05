
package de.fhdo.terminologie.ws.authoring;

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
@WebService(name = "Authoring", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/")
@XmlSeeAlso({
    de.fhdo.terminologie.ws.authoring.ObjectFactory.class,
    types.termserver.fhdo.de.ObjectFactory.class
})
public interface Authoring {


    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse.Return
     */
    @WebMethod(operationName = "UpdateCodeSystemVersionStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "UpdateCodeSystemVersionStatus", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatus")
    @ResponseWrapper(localName = "UpdateCodeSystemVersionStatusResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse")
    public de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        UpdateCodeSystemVersionStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse.Return
     */
    @WebMethod(operationName = "CreateValueSetContent")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateValueSetContent", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSetContent")
    @ResponseWrapper(localName = "CreateValueSetContentResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse")
    public de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse.Return createValueSetContent(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateValueSetContentRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType
     */
    @WebMethod(operationName = "RemoveTerminologyOrConcept")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "RemoveTerminologyOrConcept", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConcept")
    @ResponseWrapper(localName = "RemoveTerminologyOrConceptResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponse")
    public RemoveTerminologyOrConceptResponseType removeTerminologyOrConcept(
        @WebParam(name = "parameter", targetNamespace = "")
        RemoveTerminologyOrConceptRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse.Return
     */
    @WebMethod(operationName = "CreateConceptAssociationType")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateConceptAssociationType", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType")
    @ResponseWrapper(localName = "CreateConceptAssociationTypeResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse")
    public de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse.Return createConceptAssociationType(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateConceptAssociationTypeRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.RemoveValueSetContentResponseType
     */
    @WebMethod(operationName = "RemoveValueSetContent")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "RemoveValueSetContent", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.RemoveValueSetContent")
    @ResponseWrapper(localName = "RemoveValueSetContentResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.RemoveValueSetContentResponse")
    public RemoveValueSetContentResponseType removeValueSetContent(
        @WebParam(name = "parameter", targetNamespace = "")
        RemoveValueSetContentRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse.Return
     */
    @WebMethod(operationName = "MaintainCodeSystemVersion")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainCodeSystemVersion", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersion")
    @ResponseWrapper(localName = "MaintainCodeSystemVersionResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainCodeSystemVersionRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse.Return
     */
    @WebMethod(operationName = "UpdateConceptStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "UpdateConceptStatus", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateConceptStatus")
    @ResponseWrapper(localName = "UpdateConceptStatusResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse")
    public de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse.Return updateConceptStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        UpdateConceptStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse.Return
     */
    @WebMethod(operationName = "UpdateValueSetStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "UpdateValueSetStatus", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateValueSetStatus")
    @ResponseWrapper(localName = "UpdateValueSetStatusResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse")
    public de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse.Return updateValueSetStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        UpdateValueSetStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse.Return
     */
    @WebMethod(operationName = "MaintainConceptAssociationType")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainConceptAssociationType", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationType")
    @ResponseWrapper(localName = "MaintainConceptAssociationTypeResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse.Return maintainConceptAssociationType(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainConceptAssociationTypeRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateValueSetConceptMetadataValueResponse.Return
     */
    @WebMethod(operationName = "CreateValueSetConceptMetadataValue")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateValueSetConceptMetadataValue", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSetConceptMetadataValue")
    @ResponseWrapper(localName = "CreateValueSetConceptMetadataValueResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSetConceptMetadataValueResponse")
    public de.fhdo.terminologie.ws.authoring.CreateValueSetConceptMetadataValueResponse.Return createValueSetConceptMetadataValue(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateValueSetConceptMetadataValueRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValueResponse.Return
     */
    @WebMethod(operationName = "MaintainValueSetConceptMetadataValue")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainValueSetConceptMetadataValue", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValue")
    @ResponseWrapper(localName = "MaintainValueSetConceptMetadataValueResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValueResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValueResponse.Return maintainValueSetConceptMetadataValue(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainValueSetConceptMetadataValueRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse.Return
     */
    @WebMethod(operationName = "UpdateConceptValueSetMembershipStatus")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "UpdateConceptValueSetMembershipStatus", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatus")
    @ResponseWrapper(localName = "UpdateConceptValueSetMembershipStatusResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse")
    public de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(
        @WebParam(name = "parameter", targetNamespace = "")
        UpdateConceptValueSetMembershipStatusRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.DeleteValueSetConceptMetadataValueResponse.Return
     */
    @WebMethod(operationName = "DeleteValueSetConceptMetadataValue")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "DeleteValueSetConceptMetadataValue", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.DeleteValueSetConceptMetadataValue")
    @ResponseWrapper(localName = "DeleteValueSetConceptMetadataValueResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.DeleteValueSetConceptMetadataValueResponse")
    public de.fhdo.terminologie.ws.authoring.DeleteValueSetConceptMetadataValueResponse.Return deleteValueSetConceptMetadataValue(
        @WebParam(name = "parameter", targetNamespace = "")
        DeleteValueSetConceptMetadataValueRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse.Return
     */
    @WebMethod(operationName = "MaintainConceptValueSetMembership")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainConceptValueSetMembership", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembership")
    @ResponseWrapper(localName = "MaintainConceptValueSetMembershipResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainConceptValueSetMembershipRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValueResponse.Return
     */
    @WebMethod(operationName = "MaintainCodeSystemConceptMetadataValue")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainCodeSystemConceptMetadataValue", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValue")
    @ResponseWrapper(localName = "MaintainCodeSystemConceptMetadataValueResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValueResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValueResponse.Return maintainCodeSystemConceptMetadataValue(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainCodeSystemConceptMetadataValueRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType
     */
    @WebMethod(operationName = "MaintainConcept")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainConcept", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConcept")
    @ResponseWrapper(localName = "MaintainConceptResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainConceptResponse")
    public MaintainConceptResponseType maintainConcept(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainConceptRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateConceptResponse.Return
     */
    @WebMethod(operationName = "CreateConcept")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateConcept", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateConcept")
    @ResponseWrapper(localName = "CreateConceptResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateConceptResponse")
    public de.fhdo.terminologie.ws.authoring.CreateConceptResponse.Return createConcept(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateConceptRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse.Return
     */
    @WebMethod(operationName = "MaintainValueSet")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "MaintainValueSet", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainValueSet")
    @ResponseWrapper(localName = "MaintainValueSetResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse")
    public de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse.Return maintainValueSet(
        @WebParam(name = "parameter", targetNamespace = "")
        MaintainValueSetRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateValueSetResponse.Return
     */
    @WebMethod(operationName = "CreateValueSet")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateValueSet", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSet")
    @ResponseWrapper(localName = "CreateValueSetResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateValueSetResponse")
    public de.fhdo.terminologie.ws.authoring.CreateValueSetResponse.Return createValueSet(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateValueSetRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse.Return
     */
    @WebMethod(operationName = "CreateCodeSystem")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "CreateCodeSystem", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateCodeSystem")
    @ResponseWrapper(localName = "CreateCodeSystemResponse", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse")
    public de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse.Return createCodeSystem(
        @WebParam(name = "parameter", targetNamespace = "")
        CreateCodeSystemRequestType parameter);

}
