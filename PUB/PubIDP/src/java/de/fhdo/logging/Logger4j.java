/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.logging;

import de.fhdo.Definitions;
import de.fhdo.db.DBSysParamAdmin;
import de.fhdo.helper.PropertiesHelperAdmin;
import java.io.File;
import org.apache.log4j.*;

/**
 * Logger-Klasse. Einfach initialisieren mit "getInstance().getLogger()".
 *
 * @author Robert Muetzner
 */
public class Logger4j {

    /**
     * Logging-Konfigurationsdatei
     */
    private static final String LOG4J_CONFIG_FILE = "termserver_log4j.config.xml";

    /**
     * Der Logger
     */
    private Logger logger = null;

    /**
     * Singleton-Instanz
     */
    private static Logger4j instance = null;

    public static Logger4j getInstance()
    {
        if (instance == null)
        {
            instance = new Logger4j();
        }
        return instance;
    }

    /**
     * Konstruktor. Privat wg. Singleton
     *
     */
    private Logger4j()
    {
        initLogger();
    }

    public Logger getLogger()
    {
        return logger;
    }

    /**
     * Initialisiert den Logger. Liest die Eigenschaften aus der
     * XML-Configdatei. Wenn diese nicht vorhanden ist, werden
     * Standardeinstellungen benutzt.
     *
     */
    private void initLogger()
    {
        try
        {

            /**
             * Configuration
             */
            String directory = PropertiesHelperAdmin.getInstance().getLoggingConfigDirectory();
            String sFile = directory + LOG4J_CONFIG_FILE;
            System.setProperty("log4j.configurationFile", sFile);

            System.out.println("Logger wird mit der Konfigurationsdatei aus " + sFile + " erstellt.");
            
            File file = new File(sFile);

            if (file.exists() == false)
            {
                // Standard-Logger benutzen (keine Konfigurationsdatei gefunden)
                logger = Logger.getRootLogger();
                // Layout & Appender
                SimpleLayout layout = new SimpleLayout();
                ConsoleAppender consoleAppender = new ConsoleAppender(layout);

                // ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:        
                String logLevel = DBSysParamAdmin.instance().getStringValue("logLevel", null, null);
                if (logLevel.equals(""))
                {
                    logLevel = "2";
                }

                if (logLevel.equals("0"))
                {
                    logger.setLevel(Level.ERROR);
                    consoleAppender.setThreshold(Level.ERROR);
                }
                else if (logLevel.equals("1"))
                {
                    logger.setLevel(Level.WARN);
                    consoleAppender.setThreshold(Level.WARN);
                }
                else if (logLevel.equals("2"))
                {
                    logger.setLevel(Level.INFO);
                    consoleAppender.setThreshold(Level.INFO);
                }
                else if (logLevel.equals("3"))
                {
                    logger.setLevel(Level.DEBUG);
                    consoleAppender.setThreshold(Level.DEBUG);
                }
                else if (logLevel.equals("4"))
                {
                    logger.setLevel(Level.TRACE);
                    consoleAppender.setThreshold(Level.TRACE);
                }

                logger.addAppender(consoleAppender);

                // Meldung ausgeben
                logger.info("Konfigurationsdatei '" + (sFile)
                        + "'nicht gefunden, benutze Standardeigenschaften!");
            }
            else
            {
                System.out.println("Logger mit Konfiguration aus der Konfigurationsdatei verwenden...");
                // Konfiguration aus der Konfigurationsdatei verwenden

                logger = Logger.getLogger("idp");
                logger.info("Logger mit Eigenschaften aus '" + (sFile) + "' erfolgreich initialisiert!");
                logger.info("IDP Version " + Definitions.getSwVersion());
                logger.info("TermAdmin Version " + Definitions.getSwVersion());
                System.out.println("IDP: Level: " + logger.getLevel());
            }

        }
        catch (Exception ex)
        {
            System.err.println("Fehler bei Logger-Initialisierung: " + ex.getMessage());
        }
    }
}
