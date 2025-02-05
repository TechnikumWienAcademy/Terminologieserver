
package de.fhdo.terminologie.ws.conceptassociation;

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
@WebServiceClient(name = "ConceptAssociations", targetNamespace = "http://conceptAssociation.ws.terminologie.fhdo.de/", wsdlLocation = "http://localhost:8080/TermServer/ConceptAssociations?wsdl")
public class ConceptAssociations_Service
    extends Service
{

    private final static URL CONCEPTASSOCIATIONS_WSDL_LOCATION;
    private final static WebServiceException CONCEPTASSOCIATIONS_EXCEPTION;
    private final static QName CONCEPTASSOCIATIONS_QNAME = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociations");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/TermServer/ConceptAssociations?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        CONCEPTASSOCIATIONS_WSDL_LOCATION = url;
        CONCEPTASSOCIATIONS_EXCEPTION = e;
    }

    public ConceptAssociations_Service() {
        super(__getWsdlLocation(), CONCEPTASSOCIATIONS_QNAME);
    }

    public ConceptAssociations_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    /**
     * 
     * @return
     *     returns ConceptAssociations
     */
    @WebEndpoint(name = "ConceptAssociationsPort")
    public ConceptAssociations getConceptAssociationsPort() {
        return super.getPort(new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociationsPort"), ConceptAssociations.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ConceptAssociations
     */
    @WebEndpoint(name = "ConceptAssociationsPort")
    public ConceptAssociations getConceptAssociationsPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociationsPort"), ConceptAssociations.class, features);
    }

    private static URL __getWsdlLocation() {
        if (CONCEPTASSOCIATIONS_EXCEPTION!= null) {
            throw CONCEPTASSOCIATIONS_EXCEPTION;
        }
        return CONCEPTASSOCIATIONS_WSDL_LOCATION;
    }

}
