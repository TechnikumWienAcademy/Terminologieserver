
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemMetadataValue;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembershipId;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ConceptValueSetMembershipId;
import types.termserver.fhdo.de.Domain;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.LicenceType;
import types.termserver.fhdo.de.LicencedUser;
import types.termserver.fhdo.de.LicencedUserId;
import types.termserver.fhdo.de.MetadataParameter;
import types.termserver.fhdo.de.Property;
import types.termserver.fhdo.de.PropertyVersion;
import types.termserver.fhdo.de.Session;
import types.termserver.fhdo.de.SysParam;
import types.termserver.fhdo.de.TermUser;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetMetadataValue;
import types.termserver.fhdo.de.ValueSetVersion;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.fhdo.terminologie.ws.conceptassociation package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CodeSystemEntityVersion_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemEntityVersion");
    private final static QName _DomainValue_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "domainValue");
    private final static QName _ReturnConceptAssociationDetails_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ReturnConceptAssociationDetails");
    private final static QName _UpdateConceptAssociationStatus_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "UpdateConceptAssociationStatus");
    private final static QName _Domain_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "domain");
    private final static QName _MaintainConceptAssociationResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "MaintainConceptAssociationResponse");
    private final static QName _CreateConceptAssociationResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "CreateConceptAssociationResponse");
    private final static QName _CodeSystemConcept_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemConcept");
    private final static QName _LicencedUser_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "licencedUser");
    private final static QName _ListConceptAssociationsResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ListConceptAssociationsResponse");
    private final static QName _CodeSystem_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystem");
    private final static QName _MaintainConceptAssociation_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "MaintainConceptAssociation");
    private final static QName _ConceptValueSetMembership_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "conceptValueSetMembership");
    private final static QName _AssociationType_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "associationType");
    private final static QName _Property_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "property");
    private final static QName _TermUser_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "termUser");
    private final static QName _CodeSystemEntityVersionAssociation_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemEntityVersionAssociation");
    private final static QName _CreateConceptAssociation_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "CreateConceptAssociation");
    private final static QName _TraverseConceptToRootResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "TraverseConceptToRootResponse");
    private final static QName _LicenceType_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "licenceType");
    private final static QName _TraverseConceptToRoot_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "TraverseConceptToRoot");
    private final static QName _PropertyVersion_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "propertyVersion");
    private final static QName _ReturnConceptAssociationDetailsResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ReturnConceptAssociationDetailsResponse");
    private final static QName _CodeSystemVersion_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemVersion");
    private final static QName _ValueSetMetadataValue_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "valueSetMetadataValue");
    private final static QName _SysParam_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "sysParam");
    private final static QName _ConceptValueSetMembershipId_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "conceptValueSetMembershipId");
    private final static QName _CodeSystemConceptTranslation_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemConceptTranslation");
    private final static QName _LicencedUserId_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "licencedUserId");
    private final static QName _ListConceptAssociations_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ListConceptAssociations");
    private final static QName _CodeSystemEntity_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemEntity");
    private final static QName _MetadataParameter_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "metadataParameter");
    private final static QName _ValueSet_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "valueSet");
    private final static QName _UpdateConceptAssociationStatusResponse_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "UpdateConceptAssociationStatusResponse");
    private final static QName _UpdateConceptAssociationStatusResponseType_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "updateConceptAssociationStatusResponseType");
    private final static QName _CodeSystemMetadataValue_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemMetadataValue");
    private final static QName _Session_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "session");
    private final static QName _CodeSystemVersionEntityMembershipId_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemVersionEntityMembershipId");
    private final static QName _ValueSetVersion_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "valueSetVersion");
    private final static QName _CodeSystemVersionEntityMembership_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "codeSystemVersionEntityMembership");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.fhdo.terminologie.ws.conceptassociation
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MaintainConceptAssociationResponse }
     * 
     */
    public MaintainConceptAssociationResponse createMaintainConceptAssociationResponse() {
        return new MaintainConceptAssociationResponse();
    }

    /**
     * Create an instance of {@link CreateConceptAssociationResponse }
     * 
     */
    public CreateConceptAssociationResponse createCreateConceptAssociationResponse() {
        return new CreateConceptAssociationResponse();
    }

    /**
     * Create an instance of {@link ListConceptAssociationsResponse }
     * 
     */
    public ListConceptAssociationsResponse createListConceptAssociationsResponse() {
        return new ListConceptAssociationsResponse();
    }

    /**
     * Create an instance of {@link TraverseConceptToRootResponse }
     * 
     */
    public TraverseConceptToRootResponse createTraverseConceptToRootResponse() {
        return new TraverseConceptToRootResponse();
    }

    /**
     * Create an instance of {@link ReturnConceptAssociationDetailsResponse }
     * 
     */
    public ReturnConceptAssociationDetailsResponse createReturnConceptAssociationDetailsResponse() {
        return new ReturnConceptAssociationDetailsResponse();
    }

    /**
     * Create an instance of {@link UpdateConceptAssociationStatusResponse }
     * 
     */
    public UpdateConceptAssociationStatusResponse createUpdateConceptAssociationStatusResponse() {
        return new UpdateConceptAssociationStatusResponse();
    }

    /**
     * Create an instance of {@link UpdateConceptAssociationStatusResponseType }
     * 
     */
    public UpdateConceptAssociationStatusResponseType createUpdateConceptAssociationStatusResponseType() {
        return new UpdateConceptAssociationStatusResponseType();
    }

    /**
     * Create an instance of {@link ListConceptAssociations }
     * 
     */
    public ListConceptAssociations createListConceptAssociations() {
        return new ListConceptAssociations();
    }

    /**
     * Create an instance of {@link CreateConceptAssociation }
     * 
     */
    public CreateConceptAssociation createCreateConceptAssociation() {
        return new CreateConceptAssociation();
    }

    /**
     * Create an instance of {@link TraverseConceptToRootResponseType }
     * 
     */
    public TraverseConceptToRootResponseType createTraverseConceptToRootResponseType() {
        return new TraverseConceptToRootResponseType();
    }

    /**
     * Create an instance of {@link ReturnType }
     * 
     */
    public ReturnType createReturnType() {
        return new ReturnType();
    }

    /**
     * Create an instance of {@link TraverseConceptToRoot }
     * 
     */
    public TraverseConceptToRoot createTraverseConceptToRoot() {
        return new TraverseConceptToRoot();
    }

    /**
     * Create an instance of {@link ReturnConceptAssociationDetailsResponseType }
     * 
     */
    public ReturnConceptAssociationDetailsResponseType createReturnConceptAssociationDetailsResponseType() {
        return new ReturnConceptAssociationDetailsResponseType();
    }

    /**
     * Create an instance of {@link CreateConceptAssociationResponseType }
     * 
     */
    public CreateConceptAssociationResponseType createCreateConceptAssociationResponseType() {
        return new CreateConceptAssociationResponseType();
    }

    /**
     * Create an instance of {@link MaintainConceptAssociation }
     * 
     */
    public MaintainConceptAssociation createMaintainConceptAssociation() {
        return new MaintainConceptAssociation();
    }

    /**
     * Create an instance of {@link ListConceptAssociationsResponseType }
     * 
     */
    public ListConceptAssociationsResponseType createListConceptAssociationsResponseType() {
        return new ListConceptAssociationsResponseType();
    }

    /**
     * Create an instance of {@link ReturnConceptAssociationDetails }
     * 
     */
    public ReturnConceptAssociationDetails createReturnConceptAssociationDetails() {
        return new ReturnConceptAssociationDetails();
    }

    /**
     * Create an instance of {@link UpdateConceptAssociationStatus }
     * 
     */
    public UpdateConceptAssociationStatus createUpdateConceptAssociationStatus() {
        return new UpdateConceptAssociationStatus();
    }

    /**
     * Create an instance of {@link MaintainConceptAssociationResponseType }
     * 
     */
    public MaintainConceptAssociationResponseType createMaintainConceptAssociationResponseType() {
        return new MaintainConceptAssociationResponseType();
    }

    /**
     * Create an instance of {@link ReturnConceptAssociationDetailsRequestType }
     * 
     */
    public ReturnConceptAssociationDetailsRequestType createReturnConceptAssociationDetailsRequestType() {
        return new ReturnConceptAssociationDetailsRequestType();
    }

    /**
     * Create an instance of {@link MaintainConceptAssociationRequestType }
     * 
     */
    public MaintainConceptAssociationRequestType createMaintainConceptAssociationRequestType() {
        return new MaintainConceptAssociationRequestType();
    }

    /**
     * Create an instance of {@link LoginType }
     * 
     */
    public LoginType createLoginType() {
        return new LoginType();
    }

    /**
     * Create an instance of {@link CreateConceptAssociationRequestType }
     * 
     */
    public CreateConceptAssociationRequestType createCreateConceptAssociationRequestType() {
        return new CreateConceptAssociationRequestType();
    }

    /**
     * Create an instance of {@link TraverseConceptToRootRequestType }
     * 
     */
    public TraverseConceptToRootRequestType createTraverseConceptToRootRequestType() {
        return new TraverseConceptToRootRequestType();
    }

    /**
     * Create an instance of {@link ListConceptAssociationsRequestType }
     * 
     */
    public ListConceptAssociationsRequestType createListConceptAssociationsRequestType() {
        return new ListConceptAssociationsRequestType();
    }

    /**
     * Create an instance of {@link UpdateConceptAssociationStatusRequestType }
     * 
     */
    public UpdateConceptAssociationStatusRequestType createUpdateConceptAssociationStatusRequestType() {
        return new UpdateConceptAssociationStatusRequestType();
    }

    /**
     * Create an instance of {@link MaintainConceptAssociationResponse.Return }
     * 
     */
    public MaintainConceptAssociationResponse.Return createMaintainConceptAssociationResponseReturn() {
        return new MaintainConceptAssociationResponse.Return();
    }

    /**
     * Create an instance of {@link CreateConceptAssociationResponse.Return }
     * 
     */
    public CreateConceptAssociationResponse.Return createCreateConceptAssociationResponseReturn() {
        return new CreateConceptAssociationResponse.Return();
    }

    /**
     * Create an instance of {@link ListConceptAssociationsResponse.Return }
     * 
     */
    public ListConceptAssociationsResponse.Return createListConceptAssociationsResponseReturn() {
        return new ListConceptAssociationsResponse.Return();
    }

    /**
     * Create an instance of {@link TraverseConceptToRootResponse.Return }
     * 
     */
    public TraverseConceptToRootResponse.Return createTraverseConceptToRootResponseReturn() {
        return new TraverseConceptToRootResponse.Return();
    }

    /**
     * Create an instance of {@link ReturnConceptAssociationDetailsResponse.Return }
     * 
     */
    public ReturnConceptAssociationDetailsResponse.Return createReturnConceptAssociationDetailsResponseReturn() {
        return new ReturnConceptAssociationDetailsResponse.Return();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntityVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemEntityVersion")
    public JAXBElement<CodeSystemEntityVersion> createCodeSystemEntityVersion(CodeSystemEntityVersion value) {
        return new JAXBElement<CodeSystemEntityVersion>(_CodeSystemEntityVersion_QNAME, CodeSystemEntityVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DomainValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "domainValue")
    public JAXBElement<DomainValue> createDomainValue(DomainValue value) {
        return new JAXBElement<DomainValue>(_DomainValue_QNAME, DomainValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnConceptAssociationDetails }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "ReturnConceptAssociationDetails")
    public JAXBElement<ReturnConceptAssociationDetails> createReturnConceptAssociationDetails(ReturnConceptAssociationDetails value) {
        return new JAXBElement<ReturnConceptAssociationDetails>(_ReturnConceptAssociationDetails_QNAME, ReturnConceptAssociationDetails.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateConceptAssociationStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "UpdateConceptAssociationStatus")
    public JAXBElement<UpdateConceptAssociationStatus> createUpdateConceptAssociationStatus(UpdateConceptAssociationStatus value) {
        return new JAXBElement<UpdateConceptAssociationStatus>(_UpdateConceptAssociationStatus_QNAME, UpdateConceptAssociationStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Domain }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "domain")
    public JAXBElement<Domain> createDomain(Domain value) {
        return new JAXBElement<Domain>(_Domain_QNAME, Domain.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MaintainConceptAssociationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "MaintainConceptAssociationResponse")
    public JAXBElement<MaintainConceptAssociationResponse> createMaintainConceptAssociationResponse(MaintainConceptAssociationResponse value) {
        return new JAXBElement<MaintainConceptAssociationResponse>(_MaintainConceptAssociationResponse_QNAME, MaintainConceptAssociationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateConceptAssociationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "CreateConceptAssociationResponse")
    public JAXBElement<CreateConceptAssociationResponse> createCreateConceptAssociationResponse(CreateConceptAssociationResponse value) {
        return new JAXBElement<CreateConceptAssociationResponse>(_CreateConceptAssociationResponse_QNAME, CreateConceptAssociationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemConcept }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemConcept")
    public JAXBElement<CodeSystemConcept> createCodeSystemConcept(CodeSystemConcept value) {
        return new JAXBElement<CodeSystemConcept>(_CodeSystemConcept_QNAME, CodeSystemConcept.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicencedUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "licencedUser")
    public JAXBElement<LicencedUser> createLicencedUser(LicencedUser value) {
        return new JAXBElement<LicencedUser>(_LicencedUser_QNAME, LicencedUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListConceptAssociationsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "ListConceptAssociationsResponse")
    public JAXBElement<ListConceptAssociationsResponse> createListConceptAssociationsResponse(ListConceptAssociationsResponse value) {
        return new JAXBElement<ListConceptAssociationsResponse>(_ListConceptAssociationsResponse_QNAME, ListConceptAssociationsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystem }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystem")
    public JAXBElement<CodeSystem> createCodeSystem(CodeSystem value) {
        return new JAXBElement<CodeSystem>(_CodeSystem_QNAME, CodeSystem.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MaintainConceptAssociation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "MaintainConceptAssociation")
    public JAXBElement<MaintainConceptAssociation> createMaintainConceptAssociation(MaintainConceptAssociation value) {
        return new JAXBElement<MaintainConceptAssociation>(_MaintainConceptAssociation_QNAME, MaintainConceptAssociation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConceptValueSetMembership }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "conceptValueSetMembership")
    public JAXBElement<ConceptValueSetMembership> createConceptValueSetMembership(ConceptValueSetMembership value) {
        return new JAXBElement<ConceptValueSetMembership>(_ConceptValueSetMembership_QNAME, ConceptValueSetMembership.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssociationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "associationType")
    public JAXBElement<AssociationType> createAssociationType(AssociationType value) {
        return new JAXBElement<AssociationType>(_AssociationType_QNAME, AssociationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Property }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "property")
    public JAXBElement<Property> createProperty(Property value) {
        return new JAXBElement<Property>(_Property_QNAME, Property.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TermUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "termUser")
    public JAXBElement<TermUser> createTermUser(TermUser value) {
        return new JAXBElement<TermUser>(_TermUser_QNAME, TermUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntityVersionAssociation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemEntityVersionAssociation")
    public JAXBElement<CodeSystemEntityVersionAssociation> createCodeSystemEntityVersionAssociation(CodeSystemEntityVersionAssociation value) {
        return new JAXBElement<CodeSystemEntityVersionAssociation>(_CodeSystemEntityVersionAssociation_QNAME, CodeSystemEntityVersionAssociation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateConceptAssociation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "CreateConceptAssociation")
    public JAXBElement<CreateConceptAssociation> createCreateConceptAssociation(CreateConceptAssociation value) {
        return new JAXBElement<CreateConceptAssociation>(_CreateConceptAssociation_QNAME, CreateConceptAssociation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TraverseConceptToRootResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "TraverseConceptToRootResponse")
    public JAXBElement<TraverseConceptToRootResponse> createTraverseConceptToRootResponse(TraverseConceptToRootResponse value) {
        return new JAXBElement<TraverseConceptToRootResponse>(_TraverseConceptToRootResponse_QNAME, TraverseConceptToRootResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "licenceType")
    public JAXBElement<LicenceType> createLicenceType(LicenceType value) {
        return new JAXBElement<LicenceType>(_LicenceType_QNAME, LicenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TraverseConceptToRoot }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "TraverseConceptToRoot")
    public JAXBElement<TraverseConceptToRoot> createTraverseConceptToRoot(TraverseConceptToRoot value) {
        return new JAXBElement<TraverseConceptToRoot>(_TraverseConceptToRoot_QNAME, TraverseConceptToRoot.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PropertyVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "propertyVersion")
    public JAXBElement<PropertyVersion> createPropertyVersion(PropertyVersion value) {
        return new JAXBElement<PropertyVersion>(_PropertyVersion_QNAME, PropertyVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnConceptAssociationDetailsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "ReturnConceptAssociationDetailsResponse")
    public JAXBElement<ReturnConceptAssociationDetailsResponse> createReturnConceptAssociationDetailsResponse(ReturnConceptAssociationDetailsResponse value) {
        return new JAXBElement<ReturnConceptAssociationDetailsResponse>(_ReturnConceptAssociationDetailsResponse_QNAME, ReturnConceptAssociationDetailsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemVersion")
    public JAXBElement<CodeSystemVersion> createCodeSystemVersion(CodeSystemVersion value) {
        return new JAXBElement<CodeSystemVersion>(_CodeSystemVersion_QNAME, CodeSystemVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSetMetadataValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "valueSetMetadataValue")
    public JAXBElement<ValueSetMetadataValue> createValueSetMetadataValue(ValueSetMetadataValue value) {
        return new JAXBElement<ValueSetMetadataValue>(_ValueSetMetadataValue_QNAME, ValueSetMetadataValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SysParam }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "sysParam")
    public JAXBElement<SysParam> createSysParam(SysParam value) {
        return new JAXBElement<SysParam>(_SysParam_QNAME, SysParam.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConceptValueSetMembershipId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "conceptValueSetMembershipId")
    public JAXBElement<ConceptValueSetMembershipId> createConceptValueSetMembershipId(ConceptValueSetMembershipId value) {
        return new JAXBElement<ConceptValueSetMembershipId>(_ConceptValueSetMembershipId_QNAME, ConceptValueSetMembershipId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemConceptTranslation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemConceptTranslation")
    public JAXBElement<CodeSystemConceptTranslation> createCodeSystemConceptTranslation(CodeSystemConceptTranslation value) {
        return new JAXBElement<CodeSystemConceptTranslation>(_CodeSystemConceptTranslation_QNAME, CodeSystemConceptTranslation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicencedUserId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "licencedUserId")
    public JAXBElement<LicencedUserId> createLicencedUserId(LicencedUserId value) {
        return new JAXBElement<LicencedUserId>(_LicencedUserId_QNAME, LicencedUserId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListConceptAssociations }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "ListConceptAssociations")
    public JAXBElement<ListConceptAssociations> createListConceptAssociations(ListConceptAssociations value) {
        return new JAXBElement<ListConceptAssociations>(_ListConceptAssociations_QNAME, ListConceptAssociations.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntity }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemEntity")
    public JAXBElement<CodeSystemEntity> createCodeSystemEntity(CodeSystemEntity value) {
        return new JAXBElement<CodeSystemEntity>(_CodeSystemEntity_QNAME, CodeSystemEntity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetadataParameter }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "metadataParameter")
    public JAXBElement<MetadataParameter> createMetadataParameter(MetadataParameter value) {
        return new JAXBElement<MetadataParameter>(_MetadataParameter_QNAME, MetadataParameter.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "valueSet")
    public JAXBElement<ValueSet> createValueSet(ValueSet value) {
        return new JAXBElement<ValueSet>(_ValueSet_QNAME, ValueSet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateConceptAssociationStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "UpdateConceptAssociationStatusResponse")
    public JAXBElement<UpdateConceptAssociationStatusResponse> createUpdateConceptAssociationStatusResponse(UpdateConceptAssociationStatusResponse value) {
        return new JAXBElement<UpdateConceptAssociationStatusResponse>(_UpdateConceptAssociationStatusResponse_QNAME, UpdateConceptAssociationStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateConceptAssociationStatusResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "updateConceptAssociationStatusResponseType")
    public JAXBElement<UpdateConceptAssociationStatusResponseType> createUpdateConceptAssociationStatusResponseType(UpdateConceptAssociationStatusResponseType value) {
        return new JAXBElement<UpdateConceptAssociationStatusResponseType>(_UpdateConceptAssociationStatusResponseType_QNAME, UpdateConceptAssociationStatusResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemMetadataValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemMetadataValue")
    public JAXBElement<CodeSystemMetadataValue> createCodeSystemMetadataValue(CodeSystemMetadataValue value) {
        return new JAXBElement<CodeSystemMetadataValue>(_CodeSystemMetadataValue_QNAME, CodeSystemMetadataValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Session }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "session")
    public JAXBElement<Session> createSession(Session value) {
        return new JAXBElement<Session>(_Session_QNAME, Session.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersionEntityMembershipId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemVersionEntityMembershipId")
    public JAXBElement<CodeSystemVersionEntityMembershipId> createCodeSystemVersionEntityMembershipId(CodeSystemVersionEntityMembershipId value) {
        return new JAXBElement<CodeSystemVersionEntityMembershipId>(_CodeSystemVersionEntityMembershipId_QNAME, CodeSystemVersionEntityMembershipId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSetVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "valueSetVersion")
    public JAXBElement<ValueSetVersion> createValueSetVersion(ValueSetVersion value) {
        return new JAXBElement<ValueSetVersion>(_ValueSetVersion_QNAME, ValueSetVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersionEntityMembership }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", name = "codeSystemVersionEntityMembership")
    public JAXBElement<CodeSystemVersionEntityMembership> createCodeSystemVersionEntityMembership(CodeSystemVersionEntityMembership value) {
        return new JAXBElement<CodeSystemVersionEntityMembership>(_CodeSystemVersionEntityMembership_QNAME, CodeSystemVersionEntityMembership.class, null, value);
    }

}
