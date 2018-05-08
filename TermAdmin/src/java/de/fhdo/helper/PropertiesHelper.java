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
 * @author Robert
 */
public class PropertiesHelper {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static PropertiesHelper instance;

    /**
     * @return the logger
     */
    public static org.apache.log4j.Logger getLogger()
    {
        return logger;
    }

    /**
     * @param aLogger the logger to set
     */
    public static void setLogger(org.apache.log4j.Logger aLogger)
    {
        logger = aLogger;
    }

    private String server_host;
    private String server_port;
    private boolean server_secure;

    //Kollab URLs
    private String searchUrl;
    private String administrationUrl;
    private String authoringUrl;
    private String conceptAssociationUrl;
    private String authorizationUrl;

    //PUB Urls
    private String searchPubUrl;
    private String authorizationPubUrl;
    private String administrationPubUrl;
    private String authoringPubUrl;
    
    //IDP
    private String userManagementUrl;

    private String login_type;
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

    public String getLoggingConfigDirectory()
    {
        return loggingConfigDirectory;
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
            server_host = config.getProperty("server.host", "127.0.0.1");
            server_port = config.getProperty("server.port", "8080");
            server_secure = getBooleanValue(config.getProperty("server.secure", "false"));

            login_type = config.getProperty("login.type", "elga");
            searchUrl = config.getProperty("searchUrl", "");
            administrationUrl = config.getProperty("administrationUrl", "");
            authoringUrl = config.getProperty("authoringUrl", "");
            conceptAssociationUrl = config.getProperty("conceptAssosciationsUrl", "");
            authorizationUrl = config.getProperty("authorizationUrl", "");

            searchPubUrl = config.getProperty("searchPubUrl", "");
            authorizationPubUrl = config.getProperty("authorizationPubUrl", "");
            administrationPubUrl = config.getProperty("administrationPubUrl", "");
            authoringPubUrl = config.getProperty("authoringPubUrl", "");
            
            userManagementUrl = config.getProperty("userManagementUrl");

            loggingConfigDirectory = config.getProperty("loggingConfigDirectory");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }

    private boolean getBooleanValue(String s)
    {
        if (s == null || s.length() == 0)
        {
            return false;
        }

        try
        {
            return Boolean.parseBoolean(s);
        }
        catch (Exception ex)
        {

        }

        return false;
    }

    /**
     * @return the server_host
     */
    public String getServer_host()
    {
        return server_host;
    }

    /**
     * @param server_host the server_host to set
     */
    public void setServer_host(String server_host)
    {
        this.server_host = server_host;
    }

    /**
     * @return the server_port
     */
    public String getServer_port()
    {
        return server_port;
    }

    /**
     * @param server_port the server_port to set
     */
    public void setServer_port(String server_port)
    {
        this.server_port = server_port;
    }

    /**
     * @return the login_type
     */
    public String getLogin_type()
    {
        return login_type;
    }

    /**
     * @param login_type the login_type to set
     */
    public void setLogin_type(String login_type)
    {
        this.login_type = login_type;
    }

    /**
     * @return the server_secure
     */
    public boolean isServer_secure()
    {
        return server_secure;
    }

    /**
     * @param server_secure the server_secure to set
     */
    public void setServer_secure(boolean server_secure)
    {
        this.server_secure = server_secure;
    }

    public String getUserManagementUrl()
    {
        return userManagementUrl;
    }
}
