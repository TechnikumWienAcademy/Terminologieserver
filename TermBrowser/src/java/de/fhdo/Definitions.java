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
package de.fhdo;

import de.fhdo.collaboration.db.DBSysParam;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Robert Mützner
 */
public class Definitions
{

    public final static String APP_KEY = "TERMSERVER";
    public final static String DOMAIN_ADMIN = "/gui/admin/admin.zul";
    public final static String DOMAIN_MAIN = "/gui/main/main.zul";

    public static String getSwVersion()
    {
        String version = "";
        try
        {
            version = getRbTok("application.version");
        }
        catch (Exception e)
        {
            version = "";
        }

        return version;
    }
    
    public static String getBuildNumber()
    {
        String buildNumber = "";
        try
        {
            buildNumber = getRbTok("application.buildnumber");
        }
        catch (Exception e)
        {
            buildNumber = "";
        }

        return buildNumber;
    }

    private static String getRbTok(String propToken)
    {
        final ResourceBundle rb = ResourceBundle.getBundle("version");

        String msg = "";
        try
        {
            msg = rb.getString(propToken);
        }
        catch (MissingResourceException e)
        {
            System.err.println("Token ".concat(propToken).concat(" not in Propertyfile!"));
        }
        return msg;
    }

    public static enum STATUS_CODES
    {
        INACTIVE(0), ACTIVE(1), DELETED(2);
        private int code;

        private STATUS_CODES(int c)
        {
            code = c;
        }

        public int getCode()
        {
            return code;
        }

        public static boolean isStatusCodeValid(Integer StatusCode)
        {
            STATUS_CODES[] codes = STATUS_CODES.values();

            for (int i = 0; i < codes.length; ++i)
            {
                if (codes[i].getCode() == StatusCode)
                {
                    return true;
                }
            }
            return false;
        }

        public static String readStatusCodes()
        {
            String s = "";
            STATUS_CODES[] codes = STATUS_CODES.values();

            for (int i = 0; i < codes.length; ++i)
            {
                s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
            }
            return s;
        }
    }

    //public final static String STD_REPOSITORY_ADRESS = "http://193.25.22.68:8080/StandardsRepository/";
}
