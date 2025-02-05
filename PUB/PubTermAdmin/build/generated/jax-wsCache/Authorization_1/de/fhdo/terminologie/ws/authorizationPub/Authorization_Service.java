
package de.fhdo.terminologie.ws.authorizationPub;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "Authorization", targetNamespace = "http://authorization.ws.terminologie.fhdo.de/", wsdlLocation = "http://localhost:8080/TermServer/Authorization?wsdl")
public class Authorization_Service
    extends Service
{

    private final static URL AUTHORIZATION_WSDL_LOCATION;
    private final static WebServiceException AUTHORIZATION_EXCEPTION;
    private final static QName AUTHORIZATION_QNAME = new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/TermServer/Authorization?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        AUTHORIZATION_WSDL_LOCATION = url;
        AUTHORIZATION_EXCEPTION = e;
    }

    public Authorization_Service() {
        super(__getWsdlLocation(), AUTHORIZATION_QNAME);
    }

    public Authorization_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    /**
     * 
     * @return
     *     returns Authorization
     */
    @WebEndpoint(name = "AuthorizationPort")
    public Authorization getAuthorizationPort() {
        return super.getPort(new QName("http://authorization.ws.terminologie.fhdo.de/", "AuthorizationPort"), Authorization.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Authorization
     */
    @WebEndpoint(name = "AuthorizationPort")
    public Authorization getAuthorizationPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://authorization.ws.terminologie.fhdo.de/", "AuthorizationPort"), Authorization.class, features);
    }

    private static URL __getWsdlLocation() {
        if (AUTHORIZATION_EXCEPTION!= null) {
            throw AUTHORIZATION_EXCEPTION;
        }
        return AUTHORIZATION_WSDL_LOCATION;
    }

}
