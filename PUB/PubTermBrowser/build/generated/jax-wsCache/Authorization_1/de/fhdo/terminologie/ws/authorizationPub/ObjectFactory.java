
package de.fhdo.terminologie.ws.authorizationPub;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.fhdo.terminologie.ws.authorizationPub package. 
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

    private final static QName _PropertyVersion_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "propertyVersion");
    private final static QName _CodeSystemVersion_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemVersion");
    private final static QName _ValueSetMetadataValue_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "valueSetMetadataValue");
    private final static QName _Logout_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "Logout");
    private final static QName _LicenceType_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "licenceType");
    private final static QName _CodeSystemEntityVersionAssociation_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemEntityVersionAssociation");
    private final static QName _Property_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "property");
    private final static QName _TermUser_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "termUser");
    private final static QName _CodeSystemVersionEntityMembershipId_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemVersionEntityMembershipId");
    private final static QName _ValueSetVersion_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "valueSetVersion");
    private final static QName _CodeSystemVersionEntityMembership_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemVersionEntityMembership");
    private final static QName _CodeSystemMetadataValue_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemMetadataValue");
    private final static QName _Session_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "session");
    private final static QName _MetadataParameter_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "metadataParameter");
    private final static QName _ValueSet_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "valueSet");
    private final static QName _CheckLogin_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "checkLogin");
    private final static QName _CodeSystemEntity_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemEntity");
    private final static QName _CodeSystemConceptTranslation_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemConceptTranslation");
    private final static QName _LicencedUserId_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "licencedUserId");
    private final static QName _ConceptValueSetMembershipId_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "conceptValueSetMembershipId");
    private final static QName _SysParam_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "sysParam");
    private final static QName _Domain_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "domain");
    private final static QName _DomainValue_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "domainValue");
    private final static QName _CodeSystemEntityVersion_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemEntityVersion");
    private final static QName _LogoutResponse_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "LogoutResponse");
    private final static QName _AssociationType_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "associationType");
    private final static QName _ConceptValueSetMembership_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "conceptValueSetMembership");
    private final static QName _LoginResponse_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "LoginResponse");
    private final static QName _CodeSystem_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystem");
    private final static QName _LicencedUser_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "licencedUser");
    private final static QName _CheckLoginResponse_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "checkLoginResponse");
    private final static QName _CodeSystemConcept_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "codeSystemConcept");
    private final static QName _Login_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "Login");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.fhdo.terminologie.ws.authorizationPub
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CheckLoginResponse }
     * 
     */
    public CheckLoginResponse createCheckLoginResponse() {
        return new CheckLoginResponse();
    }

    /**
     * Create an instance of {@link LoginResponse }
     * 
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

    /**
     * Create an instance of {@link MetadataParameter }
     * 
     */
    public MetadataParameter createMetadataParameter() {
        return new MetadataParameter();
    }

    /**
     * Create an instance of {@link ValueSet }
     * 
     */
    public ValueSet createValueSet() {
        return new ValueSet();
    }

    /**
     * Create an instance of {@link CodeSystemVersionEntityMembershipId }
     * 
     */
    public CodeSystemVersionEntityMembershipId createCodeSystemVersionEntityMembershipId() {
        return new CodeSystemVersionEntityMembershipId();
    }

    /**
     * Create an instance of {@link ValueSetVersion }
     * 
     */
    public ValueSetVersion createValueSetVersion() {
        return new ValueSetVersion();
    }

    /**
     * Create an instance of {@link CodeSystemVersionEntityMembership }
     * 
     */
    public CodeSystemVersionEntityMembership createCodeSystemVersionEntityMembership() {
        return new CodeSystemVersionEntityMembership();
    }

    /**
     * Create an instance of {@link CodeSystemMetadataValue }
     * 
     */
    public CodeSystemMetadataValue createCodeSystemMetadataValue() {
        return new CodeSystemMetadataValue();
    }

    /**
     * Create an instance of {@link Session }
     * 
     */
    public Session createSession() {
        return new Session();
    }

    /**
     * Create an instance of {@link ConceptValueSetMembershipId }
     * 
     */
    public ConceptValueSetMembershipId createConceptValueSetMembershipId() {
        return new ConceptValueSetMembershipId();
    }

    /**
     * Create an instance of {@link SysParam }
     * 
     */
    public SysParam createSysParam() {
        return new SysParam();
    }

    /**
     * Create an instance of {@link CodeSystemEntity }
     * 
     */
    public CodeSystemEntity createCodeSystemEntity() {
        return new CodeSystemEntity();
    }

    /**
     * Create an instance of {@link CodeSystemConceptTranslation }
     * 
     */
    public CodeSystemConceptTranslation createCodeSystemConceptTranslation() {
        return new CodeSystemConceptTranslation();
    }

    /**
     * Create an instance of {@link LicencedUserId }
     * 
     */
    public LicencedUserId createLicencedUserId() {
        return new LicencedUserId();
    }

    /**
     * Create an instance of {@link CodeSystemEntityVersionAssociation }
     * 
     */
    public CodeSystemEntityVersionAssociation createCodeSystemEntityVersionAssociation() {
        return new CodeSystemEntityVersionAssociation();
    }

    /**
     * Create an instance of {@link PropertyVersion }
     * 
     */
    public PropertyVersion createPropertyVersion() {
        return new PropertyVersion();
    }

    /**
     * Create an instance of {@link CodeSystemVersion }
     * 
     */
    public CodeSystemVersion createCodeSystemVersion() {
        return new CodeSystemVersion();
    }

    /**
     * Create an instance of {@link ValueSetMetadataValue }
     * 
     */
    public ValueSetMetadataValue createValueSetMetadataValue() {
        return new ValueSetMetadataValue();
    }

    /**
     * Create an instance of {@link LicenceType }
     * 
     */
    public LicenceType createLicenceType() {
        return new LicenceType();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link TermUser }
     * 
     */
    public TermUser createTermUser() {
        return new TermUser();
    }

    /**
     * Create an instance of {@link ConceptValueSetMembership }
     * 
     */
    public ConceptValueSetMembership createConceptValueSetMembership() {
        return new ConceptValueSetMembership();
    }

    /**
     * Create an instance of {@link CodeSystem }
     * 
     */
    public CodeSystem createCodeSystem() {
        return new CodeSystem();
    }

    /**
     * Create an instance of {@link AssociationType }
     * 
     */
    public AssociationType createAssociationType() {
        return new AssociationType();
    }

    /**
     * Create an instance of {@link LicencedUser }
     * 
     */
    public LicencedUser createLicencedUser() {
        return new LicencedUser();
    }

    /**
     * Create an instance of {@link CodeSystemConcept }
     * 
     */
    public CodeSystemConcept createCodeSystemConcept() {
        return new CodeSystemConcept();
    }

    /**
     * Create an instance of {@link Domain }
     * 
     */
    public Domain createDomain() {
        return new Domain();
    }

    /**
     * Create an instance of {@link CodeSystemEntityVersion }
     * 
     */
    public CodeSystemEntityVersion createCodeSystemEntityVersion() {
        return new CodeSystemEntityVersion();
    }

    /**
     * Create an instance of {@link DomainValue }
     * 
     */
    public DomainValue createDomainValue() {
        return new DomainValue();
    }

    /**
     * Create an instance of {@link CheckLogin }
     * 
     */
    public CheckLogin createCheckLogin() {
        return new CheckLogin();
    }

    /**
     * Create an instance of {@link Logout }
     * 
     */
    public Logout createLogout() {
        return new Logout();
    }

    /**
     * Create an instance of {@link LogoutResponse }
     * 
     */
    public LogoutResponse createLogoutResponse() {
        return new LogoutResponse();
    }

    /**
     * Create an instance of {@link Login }
     * 
     */
    public Login createLogin() {
        return new Login();
    }

    /**
     * Create an instance of {@link LoginResponseType }
     * 
     */
    public LoginResponseType createLoginResponseType() {
        return new LoginResponseType();
    }

    /**
     * Create an instance of {@link ReturnType }
     * 
     */
    public ReturnType createReturnType() {
        return new ReturnType();
    }

    /**
     * Create an instance of {@link LoginType }
     * 
     */
    public LoginType createLoginType() {
        return new LoginType();
    }

    /**
     * Create an instance of {@link LogoutRequestType }
     * 
     */
    public LogoutRequestType createLogoutRequestType() {
        return new LogoutRequestType();
    }

    /**
     * Create an instance of {@link LoginRequestType }
     * 
     */
    public LoginRequestType createLoginRequestType() {
        return new LoginRequestType();
    }

    /**
     * Create an instance of {@link LogoutResponseType }
     * 
     */
    public LogoutResponseType createLogoutResponseType() {
        return new LogoutResponseType();
    }

    /**
     * Create an instance of {@link CheckLoginResponse.Return }
     * 
     */
    public CheckLoginResponse.Return createCheckLoginResponseReturn() {
        return new CheckLoginResponse.Return();
    }

    /**
     * Create an instance of {@link LoginResponse.Return }
     * 
     */
    public LoginResponse.Return createLoginResponseReturn() {
        return new LoginResponse.Return();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PropertyVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "propertyVersion")
    public JAXBElement<PropertyVersion> createPropertyVersion(PropertyVersion value) {
        return new JAXBElement<PropertyVersion>(_PropertyVersion_QNAME, PropertyVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemVersion")
    public JAXBElement<CodeSystemVersion> createCodeSystemVersion(CodeSystemVersion value) {
        return new JAXBElement<CodeSystemVersion>(_CodeSystemVersion_QNAME, CodeSystemVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSetMetadataValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "valueSetMetadataValue")
    public JAXBElement<ValueSetMetadataValue> createValueSetMetadataValue(ValueSetMetadataValue value) {
        return new JAXBElement<ValueSetMetadataValue>(_ValueSetMetadataValue_QNAME, ValueSetMetadataValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "Logout")
    public JAXBElement<Logout> createLogout(Logout value) {
        return new JAXBElement<Logout>(_Logout_QNAME, Logout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "licenceType")
    public JAXBElement<LicenceType> createLicenceType(LicenceType value) {
        return new JAXBElement<LicenceType>(_LicenceType_QNAME, LicenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntityVersionAssociation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemEntityVersionAssociation")
    public JAXBElement<CodeSystemEntityVersionAssociation> createCodeSystemEntityVersionAssociation(CodeSystemEntityVersionAssociation value) {
        return new JAXBElement<CodeSystemEntityVersionAssociation>(_CodeSystemEntityVersionAssociation_QNAME, CodeSystemEntityVersionAssociation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Property }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "property")
    public JAXBElement<Property> createProperty(Property value) {
        return new JAXBElement<Property>(_Property_QNAME, Property.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TermUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "termUser")
    public JAXBElement<TermUser> createTermUser(TermUser value) {
        return new JAXBElement<TermUser>(_TermUser_QNAME, TermUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersionEntityMembershipId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemVersionEntityMembershipId")
    public JAXBElement<CodeSystemVersionEntityMembershipId> createCodeSystemVersionEntityMembershipId(CodeSystemVersionEntityMembershipId value) {
        return new JAXBElement<CodeSystemVersionEntityMembershipId>(_CodeSystemVersionEntityMembershipId_QNAME, CodeSystemVersionEntityMembershipId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSetVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "valueSetVersion")
    public JAXBElement<ValueSetVersion> createValueSetVersion(ValueSetVersion value) {
        return new JAXBElement<ValueSetVersion>(_ValueSetVersion_QNAME, ValueSetVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemVersionEntityMembership }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemVersionEntityMembership")
    public JAXBElement<CodeSystemVersionEntityMembership> createCodeSystemVersionEntityMembership(CodeSystemVersionEntityMembership value) {
        return new JAXBElement<CodeSystemVersionEntityMembership>(_CodeSystemVersionEntityMembership_QNAME, CodeSystemVersionEntityMembership.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemMetadataValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemMetadataValue")
    public JAXBElement<CodeSystemMetadataValue> createCodeSystemMetadataValue(CodeSystemMetadataValue value) {
        return new JAXBElement<CodeSystemMetadataValue>(_CodeSystemMetadataValue_QNAME, CodeSystemMetadataValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Session }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "session")
    public JAXBElement<Session> createSession(Session value) {
        return new JAXBElement<Session>(_Session_QNAME, Session.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetadataParameter }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "metadataParameter")
    public JAXBElement<MetadataParameter> createMetadataParameter(MetadataParameter value) {
        return new JAXBElement<MetadataParameter>(_MetadataParameter_QNAME, MetadataParameter.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueSet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "valueSet")
    public JAXBElement<ValueSet> createValueSet(ValueSet value) {
        return new JAXBElement<ValueSet>(_ValueSet_QNAME, ValueSet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckLogin }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "checkLogin")
    public JAXBElement<CheckLogin> createCheckLogin(CheckLogin value) {
        return new JAXBElement<CheckLogin>(_CheckLogin_QNAME, CheckLogin.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntity }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemEntity")
    public JAXBElement<CodeSystemEntity> createCodeSystemEntity(CodeSystemEntity value) {
        return new JAXBElement<CodeSystemEntity>(_CodeSystemEntity_QNAME, CodeSystemEntity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemConceptTranslation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemConceptTranslation")
    public JAXBElement<CodeSystemConceptTranslation> createCodeSystemConceptTranslation(CodeSystemConceptTranslation value) {
        return new JAXBElement<CodeSystemConceptTranslation>(_CodeSystemConceptTranslation_QNAME, CodeSystemConceptTranslation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicencedUserId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "licencedUserId")
    public JAXBElement<LicencedUserId> createLicencedUserId(LicencedUserId value) {
        return new JAXBElement<LicencedUserId>(_LicencedUserId_QNAME, LicencedUserId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConceptValueSetMembershipId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "conceptValueSetMembershipId")
    public JAXBElement<ConceptValueSetMembershipId> createConceptValueSetMembershipId(ConceptValueSetMembershipId value) {
        return new JAXBElement<ConceptValueSetMembershipId>(_ConceptValueSetMembershipId_QNAME, ConceptValueSetMembershipId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SysParam }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "sysParam")
    public JAXBElement<SysParam> createSysParam(SysParam value) {
        return new JAXBElement<SysParam>(_SysParam_QNAME, SysParam.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Domain }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "domain")
    public JAXBElement<Domain> createDomain(Domain value) {
        return new JAXBElement<Domain>(_Domain_QNAME, Domain.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DomainValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "domainValue")
    public JAXBElement<DomainValue> createDomainValue(DomainValue value) {
        return new JAXBElement<DomainValue>(_DomainValue_QNAME, DomainValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemEntityVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemEntityVersion")
    public JAXBElement<CodeSystemEntityVersion> createCodeSystemEntityVersion(CodeSystemEntityVersion value) {
        return new JAXBElement<CodeSystemEntityVersion>(_CodeSystemEntityVersion_QNAME, CodeSystemEntityVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "LogoutResponse")
    public JAXBElement<LogoutResponse> createLogoutResponse(LogoutResponse value) {
        return new JAXBElement<LogoutResponse>(_LogoutResponse_QNAME, LogoutResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssociationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "associationType")
    public JAXBElement<AssociationType> createAssociationType(AssociationType value) {
        return new JAXBElement<AssociationType>(_AssociationType_QNAME, AssociationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConceptValueSetMembership }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "conceptValueSetMembership")
    public JAXBElement<ConceptValueSetMembership> createConceptValueSetMembership(ConceptValueSetMembership value) {
        return new JAXBElement<ConceptValueSetMembership>(_ConceptValueSetMembership_QNAME, ConceptValueSetMembership.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "LoginResponse")
    public JAXBElement<LoginResponse> createLoginResponse(LoginResponse value) {
        return new JAXBElement<LoginResponse>(_LoginResponse_QNAME, LoginResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystem }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystem")
    public JAXBElement<CodeSystem> createCodeSystem(CodeSystem value) {
        return new JAXBElement<CodeSystem>(_CodeSystem_QNAME, CodeSystem.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicencedUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "licencedUser")
    public JAXBElement<LicencedUser> createLicencedUser(LicencedUser value) {
        return new JAXBElement<LicencedUser>(_LicencedUser_QNAME, LicencedUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckLoginResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "checkLoginResponse")
    public JAXBElement<CheckLoginResponse> createCheckLoginResponse(CheckLoginResponse value) {
        return new JAXBElement<CheckLoginResponse>(_CheckLoginResponse_QNAME, CheckLoginResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeSystemConcept }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "codeSystemConcept")
    public JAXBElement<CodeSystemConcept> createCodeSystemConcept(CodeSystemConcept value) {
        return new JAXBElement<CodeSystemConcept>(_CodeSystemConcept_QNAME, CodeSystemConcept.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Login }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorization.ws.terminologie.fhdo.de/", name = "Login")
    public JAXBElement<Login> createLogin(Login value) {
        return new JAXBElement<Login>(_Login_QNAME, Login.class, null, value);
    }

}
