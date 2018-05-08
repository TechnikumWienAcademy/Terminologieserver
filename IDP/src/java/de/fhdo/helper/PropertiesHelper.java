/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.db.DBSysParam;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Robert
 */
public abstract class PropertiesHelper {

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

    private String login_type;
    private DBSysParam dbsysparam;

    private String authorizationUrl;

    private String loggingConfigDirectory;

    public PropertiesHelper(DBSysParam dbsysparam)
    {
        this.dbsysparam = dbsysparam;
        loadData();
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

            String filename = dbsysparam.getStringValue("termserverProperties", null, null);

            //logger cannot be used because its not initialized yet
            System.out.println("filename: " + filename);

            config.load(new FileInputStream(filename));

            // load properties
            server_host = config.getProperty("server.host", "127.0.0.1");
            server_port = config.getProperty("server.port", "8080");
            server_secure = getBooleanValue(config.getProperty("server.secure", "false"));

            login_type = config.getProperty("login.type", "elga");

            authorizationUrl = config.getProperty("authorizationUrl", "");

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

    public String getAuthorizationUrl()
    {
        return authorizationUrl;
    }

}
