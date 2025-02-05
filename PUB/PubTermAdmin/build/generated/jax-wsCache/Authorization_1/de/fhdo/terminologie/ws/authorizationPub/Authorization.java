
package de.fhdo.terminologie.ws.authorizationPub;

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
@WebService(name = "Authorization", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Authorization {


    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return
     */
    @WebMethod(operationName = "Login")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "Login", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.Login")
    @ResponseWrapper(localName = "LoginResponse", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.LoginResponse")
    public de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return login(
        @WebParam(name = "parameter", targetNamespace = "")
        LoginRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authorizationPub.LogoutResponseType
     */
    @WebMethod(operationName = "Logout")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "Logout", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.Logout")
    @ResponseWrapper(localName = "LogoutResponse", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.LogoutResponse")
    public LogoutResponseType logout(
        @WebParam(name = "parameter", targetNamespace = "")
        LogoutRequestType parameter);

    /**
     * 
     * @param parameter
     * @return
     *     returns de.fhdo.terminologie.ws.authorizationPub.CheckLoginResponse.Return
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "checkLogin", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.CheckLogin")
    @ResponseWrapper(localName = "checkLoginResponse", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", className = "de.fhdo.terminologie.ws.authorizationPub.CheckLoginResponse")
    public de.fhdo.terminologie.ws.authorizationPub.CheckLoginResponse.Return checkLogin(
        @WebParam(name = "parameter", targetNamespace = "")
        LoginRequestType parameter);

}
