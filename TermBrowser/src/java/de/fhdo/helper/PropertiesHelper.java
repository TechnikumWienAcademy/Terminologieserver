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
public class PropertiesHelper{

    private static PropertiesHelper instance;

    //Kollab URLs
    private String searchUrl;
    private String administrationUrl;
    private String authoringUrl;
    private String conceptAssociationUrl;
    private String authorizationUrl;
    private String ssoUrl;

    //PUB URLs
    private String searchPubUrl;
    private String authorizationPubUrl;
    private String administrationPubUrl;
    private String authoringPubUrl;

    //IDP URLs
    private String authorizationIdpUrl;
    private String userManagementUrl;

    //OTHER
    private String collaborationUser;
    private String collaborationPassword;
    private String loggingConfigDirectory;

    /**
     * Returns the instance of the PropertiesHelper if it is not null, otherwise
     * a new instance will be instantiated before it is returned.
     * @return the instance of the PropertiesHelper
     */
    public static PropertiesHelper getInstance(){
        if (instance == null)
            instance = new PropertiesHelper();

        return instance;
    }

    /**
     * Calls the loadData() method.
     */
    public PropertiesHelper(){
        loadData();
    }

    /**
     * Retrieves the path to the termserverProperties file from the database
     * and loads the file, setting the properties afterwards.
     */
    private void loadData(){
        //The logger cannot be used because its not initialized yet
        System.out.println("+++++ PropertiesHelper.loadData() started +++++");

        Properties properties = new Properties();
        try{
            String filename = de.fhdo.db.DBSysParam.getInstance().getStringValue("termserverProperties", null, null);
            System.out.println("Properties-path, read from the sys_param table in the database: " + filename);

            properties.load(new FileInputStream(filename));

            //Setting properties
            searchUrl = properties.getProperty("searchUrl", "");
            ssoUrl = properties.getProperty("ssoUrl", "");
            conceptAssociationUrl = properties.getProperty("conceptAssosciationsUrl", "");
            authoringUrl = properties.getProperty("authoringUrl", "");
            administrationUrl = properties.getProperty("administrationUrl", "");
            authorizationUrl = properties.getProperty("authorizationUrl", "");

            searchPubUrl = properties.getProperty("searchPubUrl", "");
            authoringPubUrl = properties.getProperty("authoringPubUrl", "");
            authorizationPubUrl = properties.getProperty("authorizationPubUrl", "");
            administrationPubUrl = properties.getProperty("administrationPubUrl", "");

            authorizationIdpUrl = properties.getProperty("authorizationIdpUrl", "");

            collaborationUser = properties.getProperty("collaborationUser", "");
            collaborationPassword = properties.getProperty("collaborationPassword", "");

            loggingConfigDirectory = properties.getProperty("loggingConfigDirectory");

            userManagementUrl = properties.getProperty("userManagementUrl");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    //BASIC GETTERS
    public String getSearchUrl(){
        return searchUrl;
    }

    public String getSearchPubUrl(){
        return searchPubUrl;
    }

    public String getAdministrationUrl(){
        return administrationUrl;
    }

    public String getAuthoringUrl(){
        return authoringUrl;
    }

    public String getConceptAssociationUrl(){
        return conceptAssociationUrl;
    }

    public String getAuthorizationUrl(){
        return authorizationUrl;
    }

    public String getAuthorizationPubUrl(){
        return authorizationPubUrl;
    }

    public String getAdministrationPubUrl(){
        return administrationPubUrl;
    }

    public String getAuthoringPubUrl(){
        return authoringPubUrl;
    }

    public String getSsoUrl(){
        return ssoUrl;
    }

    public String getAuthorizationIdpUrl(){
        return authorizationIdpUrl;
    }

    public String getCollaborationUser(){
        return collaborationUser;
    }

    public String getCollaborationPassword(){
        return collaborationPassword;
    }

    public String getLoggingConfigDirectory(){
        return loggingConfigDirectory;
    }

    public String getUserManagementUrl(){
        return userManagementUrl;
    }
}