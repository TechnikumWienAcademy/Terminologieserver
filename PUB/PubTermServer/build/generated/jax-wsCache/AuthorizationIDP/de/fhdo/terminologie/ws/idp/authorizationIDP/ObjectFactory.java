
package de.fhdo.terminologie.ws.idp.authorizationIDP;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.fhdo.terminologie.ws.idp.authorizationIDP package. 
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

    private final static QName _GetLoginInfosResponse_QNAME = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "getLoginInfosResponse");
    private final static QName _ChangePasswordResponse_QNAME = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "changePasswordResponse");
    private final static QName _ChangePassword_QNAME = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "changePassword");
    private final static QName _GetLoginInfos_QNAME = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "getLoginInfos");
    private final static QName _TermUser_QNAME = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "termUser");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.fhdo.terminologie.ws.idp.authorizationIDP
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChangePasswordResponse }
     * 
     */
    public ChangePasswordResponse createChangePasswordResponse() {
        return new ChangePasswordResponse();
    }

    /**
     * Create an instance of {@link GetLoginInfosResponse }
     * 
     */
    public GetLoginInfosResponse createGetLoginInfosResponse() {
        return new GetLoginInfosResponse();
    }

    /**
     * Create an instance of {@link TermUser }
     * 
     */
    public TermUser createTermUser() {
        return new TermUser();
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
     * Create an instance of {@link ChangePassword }
     * 
     */
    public ChangePassword createChangePassword() {
        return new ChangePassword();
    }

    /**
     * Create an instance of {@link ChangePasswordResponseType }
     * 
     */
    public ChangePasswordResponseType createChangePasswordResponseType() {
        return new ChangePasswordResponseType();
    }

    /**
     * Create an instance of {@link GetLoginInfos }
     * 
     */
    public GetLoginInfos createGetLoginInfos() {
        return new GetLoginInfos();
    }

    /**
     * Create an instance of {@link LoginRequestType }
     * 
     */
    public LoginRequestType createLoginRequestType() {
        return new LoginRequestType();
    }

    /**
     * Create an instance of {@link ChangePasswordRequestType }
     * 
     */
    public ChangePasswordRequestType createChangePasswordRequestType() {
        return new ChangePasswordRequestType();
    }

    /**
     * Create an instance of {@link ChangePasswordResponse.Return }
     * 
     */
    public ChangePasswordResponse.Return createChangePasswordResponseReturn() {
        return new ChangePasswordResponse.Return();
    }

    /**
     * Create an instance of {@link GetLoginInfosResponse.Return }
     * 
     */
    public GetLoginInfosResponse.Return createGetLoginInfosResponseReturn() {
        return new GetLoginInfosResponse.Return();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetLoginInfosResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorizationIDP.idp.ws.terminologie.fhdo.de/", name = "getLoginInfosResponse")
    public JAXBElement<GetLoginInfosResponse> createGetLoginInfosResponse(GetLoginInfosResponse value) {
        return new JAXBElement<GetLoginInfosResponse>(_GetLoginInfosResponse_QNAME, GetLoginInfosResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangePasswordResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorizationIDP.idp.ws.terminologie.fhdo.de/", name = "changePasswordResponse")
    public JAXBElement<ChangePasswordResponse> createChangePasswordResponse(ChangePasswordResponse value) {
        return new JAXBElement<ChangePasswordResponse>(_ChangePasswordResponse_QNAME, ChangePasswordResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangePassword }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorizationIDP.idp.ws.terminologie.fhdo.de/", name = "changePassword")
    public JAXBElement<ChangePassword> createChangePassword(ChangePassword value) {
        return new JAXBElement<ChangePassword>(_ChangePassword_QNAME, ChangePassword.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetLoginInfos }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorizationIDP.idp.ws.terminologie.fhdo.de/", name = "getLoginInfos")
    public JAXBElement<GetLoginInfos> createGetLoginInfos(GetLoginInfos value) {
        return new JAXBElement<GetLoginInfos>(_GetLoginInfos_QNAME, GetLoginInfos.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TermUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://authorizationIDP.idp.ws.terminologie.fhdo.de/", name = "termUser")
    public JAXBElement<TermUser> createTermUser(TermUser value) {
        return new JAXBElement<TermUser>(_TermUser_QNAME, TermUser.class, null, value);
    }

}
