
package de.fhdo.terminologie.ws.authoring;

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
@WebServiceClient(name = "Authoring", targetNamespace = "http://authoring.ws.terminologie.fhdo.de/", wsdlLocation = "http://localhost:8080/TermServer/Authoring?wsdl")
public class Authoring_Service
    extends Service
{

    private final static URL AUTHORING_WSDL_LOCATION;
    private final static WebServiceException AUTHORING_EXCEPTION;
    private final static QName AUTHORING_QNAME = new QName("http://authoring.ws.terminologie.fhdo.de/", "Authoring");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/TermServer/Authoring?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        AUTHORING_WSDL_LOCATION = url;
        AUTHORING_EXCEPTION = e;
    }

    public Authoring_Service() {
        super(__getWsdlLocation(), AUTHORING_QNAME);
    }

    public Authoring_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    /**
     * 
     * @return
     *     returns Authoring
     */
    @WebEndpoint(name = "AuthoringPort")
    public Authoring getAuthoringPort() {
        return super.getPort(new QName("http://authoring.ws.terminologie.fhdo.de/", "AuthoringPort"), Authoring.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Authoring
     */
    @WebEndpoint(name = "AuthoringPort")
    public Authoring getAuthoringPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://authoring.ws.terminologie.fhdo.de/", "AuthoringPort"), Authoring.class, features);
    }

    private static URL __getWsdlLocation() {
        if (AUTHORING_EXCEPTION!= null) {
            throw AUTHORING_EXCEPTION;
        }
        return AUTHORING_WSDL_LOCATION;
    }

}