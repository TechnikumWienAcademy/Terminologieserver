/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.ws.idp.authorizationIDP.AuthorizationIDP;
import de.fhdo.terminologie.ws.idp.authorizationIDP.AuthorizationIDP_Service;
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
    
    //IDP
    private AuthorizationIDP_Service authorizationIdpService;
    
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
        //IDP
        this.setAuthorizationIdpService();
    }
    
    //IDP
    private void setAuthorizationIdpService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAuthorizationIdpUrl();
            
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "AuthorizationIDP");
            
            this.authorizationIdpService = new AuthorizationIDP_Service(newEndpoint, qname);
            logger.info("TermServer: Authorization IDP Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("malforemd URL: "+ ex.getMessage());
            this.authorizationIdpService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermServer: Could not create WebService Authorization IDP: "+ wsEx.getMessage());
            this.authorizationIdpService = null;
        }
    }
    
    //IDP
    public AuthorizationIDP getAuthorizationIdpServicePort()
    {
        if(authorizationIdpService == null)
        {
            this.setAuthorizationIdpService();
        }
        return authorizationIdpService.getAuthorizationIDPPort();
    }
    
}
