/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.helper;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author puraner
 */
public class PropertiesHelper {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static PropertiesHelper instance;

    //IDP
    private String authorizationIdpUrl;

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

    public String getAuthorizationIdpUrl()
    {
        return authorizationIdpUrl;
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
            authorizationIdpUrl = config.getProperty("authorizationIdpUrl", "");
            loggingConfigDirectory = config.getProperty("loggingConfigDirectory");

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
