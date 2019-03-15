/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.terminologie.ws.administration.Administration;
import de.fhdo.terminologie.ws.administration.Administration_Service;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.Authorization_Service;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations_Service;
import de.fhdo.terminologie.ws.idp.authorizationidp.AuthorizationIDP;
import de.fhdo.terminologie.ws.idp.authorizationidp.AuthorizationIDP_Service;
import de.fhdo.terminologie.ws.idp.usermanagement.UserManagement;
import de.fhdo.terminologie.ws.idp.usermanagement.UserManagement_Service;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.MTOMFeature;

/**
 *
 * @author puraner
 */
public class WebServiceUrlHelper
{
    private static WebServiceUrlHelper instance;
    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    //Collab WS
    private Authoring_Service authoringService;
    private Search_Service searchService;
    private Administration_Service administrationService;
    private ConceptAssociations_Service conceptAssociationsService;
    private Authorization_Service authorizationService;
    //private SSo_Service ssoService;
    
    //Pub WS
    private de.fhdo.terminologie.ws.searchPub.Search_Service searchPubService;
    private de.fhdo.terminologie.ws.authorizationPub.Authorization_Service authorizationPubService;
    private de.fhdo.terminologie.ws.administrationPub.Administration_Service administrationPubService;
    private de.fhdo.terminologie.ws.authoringPub.Authoring_Service authoringPubService;
    
    //IDP
    private AuthorizationIDP_Service authorizationIdpService;
    private UserManagement_Service userManagementService;
    
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
        this.setAuthoringService();
        this.setSearchService();
        this.setAdministrationService();
        this.setConceptAssociationsService();
        this.setAuhtorizationService();
        //this.setSsoService();
        
        //Pub
        this.setSearchPubService();
        this.setAuthorizationPubService();
        this.setAdministrationPubService();
        this.setAuthoringPubService();
        
        //IDP
        this.setAuthorizationIdpService();
        this.setUserManagementService();
    }

    private void setAuthoringService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAuthoringUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authoring.ws.terminologie.fhdo.de/", "Authoring");
            
            this.authoringService = new Authoring_Service(newEndpoint, qname);
            logger.info("TermBrowser: Authoring Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.authoringService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Authoring: "+ wsEx.getMessage());
            this.authoringService = null;
        }
    }
    
    private void setSearchService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getSearchUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://search.ws.terminologie.fhdo.de/", "Search");
            
            this.searchService = new Search_Service(newEndpoint, qname);
            logger.info("TermBrowser: Search Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.searchService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Search: "+ wsEx.getMessage());
            this.searchService = null;
        }
    }
    
    private void setAdministrationService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAdministrationUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://administration.ws.terminologie.fhdo.de/", "Administration");
            
            this.administrationService = new Administration_Service(newEndpoint, qname);
            logger.info("TermBrowser: Administration Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.administrationService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Administration: "+ wsEx.getMessage());
            this.administrationService = null;
        }
    }
    
    private void setConceptAssociationsService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getConceptAssociationUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociations");
            
            this.conceptAssociationsService = new ConceptAssociations_Service(newEndpoint, qname);
            logger.info("TermBrowser: Concept Association Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.conceptAssociationsService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Concept Association: "+ wsEx.getMessage());
            this.conceptAssociationsService = null;
        }
    }
    
    private void setAuhtorizationService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAuthorizationUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization");
            
            this.authorizationService = new Authorization_Service(newEndpoint, qname);
            logger.info("TermBrowser: Authorization Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.authorizationService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Authorization: "+ wsEx.getMessage());
            this.authorizationService = null;
        }
    }
    /*
    private void setSsoService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getSsoUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://sso.ws.terminologie.fhdo.de/", "SSo");
            
            this.ssoService = new SSo_Service(newEndpoint, qname);
            logger.info("TermBrowser: SSO Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.ssoService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService SSO: "+ wsEx.getMessage());
            this.ssoService = null;
        }
    }*/
    
    //PUB
    private void setSearchPubService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getSearchPubUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://search.ws.terminologie.fhdo.de/", "Search");
            
            this.searchPubService = new de.fhdo.terminologie.ws.searchPub.Search_Service(newEndpoint, qname);
            logger.info("TermBrowser: Search Pub Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.searchPubService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Search Pub: "+ wsEx.getMessage());
            this.searchPubService = null;
        }
    }
    
    private void setAuthorizationPubService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAuthorizationPubUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization");
            
            this.authorizationPubService = new de.fhdo.terminologie.ws.authorizationPub.Authorization_Service(newEndpoint, qname);
            logger.info("TermBrowser: Authorization Pub Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.authorizationPubService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Authorization Pub: "+ wsEx.getMessage());
            this.authorizationPubService = null;
        }
    }
    
    private void setAdministrationPubService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAdministrationPubUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://administration.ws.terminologie.fhdo.de/", "Administration");
            
            this.administrationPubService = new de.fhdo.terminologie.ws.administrationPub.Administration_Service(newEndpoint, qname);
            logger.info("TermBrowser: Administration Pub Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.administrationPubService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Administration Pub: "+ wsEx.getMessage());
            this.administrationPubService = null;
        }
    }
    
    private void setAuthoringPubService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getAuthoringPubUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authoring.ws.terminologie.fhdo.de/", "Authoring");
            
            this.authoringPubService = new de.fhdo.terminologie.ws.authoringPub.Authoring_Service(newEndpoint, qname);
            logger.info("TermBrowser: Authoring Pub Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.authoringPubService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Authoring Pub: "+ wsEx.getMessage());
            this.authoringPubService = null;
        }
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
            logger.info("TermBrowser: Authorization IDP Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.authorizationIdpService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService Authorization IPD: "+ wsEx.getMessage());
            this.authorizationIdpService = null;
        }
    }
    
    private void setUserManagementService()
    {
        try
        {
            String url = PropertiesHelper.getInstance().getUserManagementUrl();
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "UserManagement");
            
            this.userManagementService = new UserManagement_Service(newEndpoint, qname);
            
            logger.info("TermBrowser: UserManagement Service with URL " + url + " created.");
        }
        catch (MalformedURLException ex)
        {
            logger.error("TermBrowser: malforemd URL: "+ ex.getMessage());
            this.userManagementService = null;
        }
        catch (javax.xml.ws.WebServiceException wsEx)
        {
            logger.error("TermBrowser: Could not create WebService UserManagement: "+ wsEx.getMessage());
            this.userManagementService = null;
        }
    }
    
    
    public Authoring getAuthoringServicePort()
    {
        if(authoringService == null)
        {
            this.setAuthoringService();
        }
        return authoringService.getAuthoringPort();
    }

    public Search getSearchServicePort()
    {
        if(searchService == null)
        {
            this.setSearchService();
        }
        return searchService.getSearchPort();
    }
    
    public Administration getAdministrationServicePort()
    {
        if(administrationService == null)
        {
            this.setAdministrationService();
        }
        return administrationService.getAdministrationPort();
    }
    
    public Administration getAdministrationServicePort(MTOMFeature mtom)
    {
        if(administrationService == null)
        {
            this.setAdministrationService();
        }
        return administrationService.getAdministrationPort(mtom);
    }
    
    public ConceptAssociations getConceptAssociationServicePort()
    {
        if(conceptAssociationsService == null)
        {
            this.setConceptAssociationsService();
        }
        return conceptAssociationsService.getConceptAssociationsPort();
    }
    
    public Authorization getAuthorizationServicePort()
    {
        if(authorizationService == null)
        {
            this.setAuhtorizationService();
        }
        return authorizationService.getAuthorizationPort();
    }
    
    /*
    public SSo getSsoServicePort()
    {
        if(ssoService == null)
        {
            this.setSsoService();
        }
        return ssoService.getSSoPort();
    }*/
    
    //PUB Services
    public de.fhdo.terminologie.ws.searchPub.Search getSearchPubServicePort()
    {
        if(searchPubService == null)
        {
            this.setSearchPubService();
        }
        return searchPubService.getSearchPort();
    }
    
    public de.fhdo.terminologie.ws.authorizationPub.Authorization getAuthorizationPubServicePort()
    {
        if(authorizationPubService == null)
        {
            //EXTERMINATUS 3.2.2
            //this.setAuthoringPubService();
            this.setAuthorizationPubService();
        }
        return authorizationPubService.getAuthorizationPort();
    }
    
    public de.fhdo.terminologie.ws.administrationPub.Administration getAdministrationPubServicePort()
    {
        if(administrationPubService == null)
        {
            this.setAdministrationPubService();
        }
        return administrationPubService.getAdministrationPort();
    }
    
    public de.fhdo.terminologie.ws.administrationPub.Administration getAdministrationPubServicePort(MTOMFeature mtom)
    {
        if(administrationPubService == null)
        {
            this.setAdministrationPubService();
        }
        return administrationPubService.getAdministrationPort(mtom);
    }
    
    public de.fhdo.terminologie.ws.authoringPub.Authoring getAuthoringPubServicePort()
    {
        if(authoringPubService == null)
        {
            this.setAuthoringPubService();
        }
        return authoringPubService.getAuthoringPort();
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
    
    public UserManagement getUserManagementServicePort()
    {
        if( userManagementService == null)
        {
            this.setUserManagementService();
        }
        return userManagementService.getUserManagementPort();
    }
    
}
