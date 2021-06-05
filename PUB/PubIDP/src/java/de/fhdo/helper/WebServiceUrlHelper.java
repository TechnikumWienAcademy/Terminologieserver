/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.Authorization_Service;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 *
 * @author puraner
 */
public class WebServiceUrlHelper
{
    private static WebServiceUrlHelper instance;
    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    //Collab WS
    private Authorization_Service authorizationService;
    
    public static WebServiceUrlHelper getInstance()
    {
        if (instance == null)
        {
            instance = new WebServiceUrlHelper();
        }

        return instance;
    }
    
    private WebServiceUrlHelper()
    {
        //Collab
        this.setAuhtorizationService();
    }
    
    private void setAuhtorizationService()
    {
        try
        {
            String url = PropertiesHelperAdmin.getInstance().getAuthorizationUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization");
            
            this.authorizationService = new Authorization_Service(newEndpoint, qname);
            logger.info("IDP: Authorization Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("malforemd URL: "+ ex.getMessage());
            this.authorizationService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("IDP: Could not create WebService Authorization: "+ wsEx.getMessage());
            this.authorizationService = null;
        }
    }
    
    public Authorization getAuthorizationServicePort()
    {
        if(authorizationService == null)
        {
            this.setAuhtorizationService();
        }
        return authorizationService.getAuthorizationPort();
    }
}
