/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author puraner
 */
public class PropertiesHelper
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static PropertiesHelper instance;

    //Kollab URLs
    private String searchUrl;
    private String administrationUrl;
    private String authoringUrl;
    private String conceptAssociationUrl;
    private String authorizationUrl;
    private String ssoUrl;

    //PUB Urls
    private String searchPubUrl;
    private String authorizationPubUrl;
    private String administrationPubUrl;
    private String authoringPubUrl;

    //IDP
    private String authorizationIdpUrl;
    private String userManagementUrl;

    private String collaborationUser;
    private String collaborationPassword;
    private String loggingConfigDirectory;

    public static PropertiesHelper getInstance()
    {
        if (instance == null)
        {
            instance = new PropertiesHelper();
        }

        return instance;
    }

    public PropertiesHelper()
    {
        loadData();
    }

    public String getSearchUrl()
    {
        return searchUrl;
    }

    public String getSearchPubUrl()
    {
        return searchPubUrl;
    }

    public String getAdministrationUrl()
    {
        return administrationUrl;
    }

    public String getAuthoringUrl()
    {
        return authoringUrl;
    }

    public String getConceptAssociationUrl()
    {
        return conceptAssociationUrl;
    }

    public String getAuthorizationUrl()
    {
        return authorizationUrl;
    }

    public String getAuthorizationPubUrl()
    {
        return authorizationPubUrl;
    }

    public String getAdministrationPubUrl()
    {
        return administrationPubUrl;
    }

    public String getAuthoringPubUrl()
    {
        return authoringPubUrl;
    }

    public String getSsoUrl()
    {
        return ssoUrl;
    }

    public String getAuthorizationIdpUrl()
    {
        return authorizationIdpUrl;
    }

    public String getCollaborationUser()
    {
        return collaborationUser;
    }

    public String getCollaborationPassword()
    {
        return collaborationPassword;
    }

    public String getLoggingConfigDirectory()
    {
        return loggingConfigDirectory;
    }

    public String getUserManagementUrl()
    {
        return userManagementUrl;
    }

    private void loadData()
    {
        //logger cannot be used because its not initialized yet
        System.out.println("Load properties...");

        Properties config = new Properties();
        try
        {

            String filename = de.fhdo.db.DBSysParam.instance().getStringValue("termserverProperties", null, null);

            //logger cannot be used because its not initialized yet
            System.out.println("filename: " + filename);

            config.load(new FileInputStream(filename));

            // load properties
            searchUrl = config.getProperty("searchUrl", "");
            ssoUrl = config.getProperty("ssoUrl", "");
            conceptAssociationUrl = config.getProperty("conceptAssosciationsUrl", "");
            authoringUrl = config.getProperty("authoringUrl", "");
            administrationUrl = config.getProperty("administrationUrl", "");
            authorizationUrl = config.getProperty("authorizationUrl", "");

            searchPubUrl = config.getProperty("searchPubUrl", "");
            authoringPubUrl = config.getProperty("authoringPubUrl", "");
            authorizationPubUrl = config.getProperty("authorizationPubUrl", "");
            administrationPubUrl = config.getProperty("administrationPubUrl", "");

            authorizationIdpUrl = config.getProperty("authorizationIdpUrl", "");

            collaborationUser = config.getProperty("collaborationUser", "");
            collaborationPassword = config.getProperty("collaborationPassword", "");

            loggingConfigDirectory = config.getProperty("loggingConfigDirectory");

            userManagementUrl = config.getProperty("userManagementUrl");

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
