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

import de.fhdo.db.DBSysParamAdmin;

/**
 *
 * @author Robert Mützner
 */
public class Definitions /* Test */ {

    public final static String ADMIN_SESS_ASSERTION = "admin_assertion";
    public final static String ADMIN_SESS_ID = "admin_id";
//    public final static String ADMIN_USERLOGIN = "admin_userlogin";

    public final static String COLLAB_SESS_ASSERTION = "collab_assertion";
    public final static String COLLAB_SESS_ID = "collab_id";
//    public final static String COLLAB_SESS_USERLOGIN = "collab_userlogin";

    public final static String IDP_NAME = "localhost:8080/IDP/";
    public final static String SESS_REQUEST = "request";
    
    
    public final static String ADMIN_USER_SESS = "admin_user";
    public final static String ADMINCOLLAB_USER_SESS = "admin_collab_user";
    public final static String COLLAB_USER_SESS = "collab_user";
    
    public final static String LOGGED_IN_SITES = "logged_in_sites";
    
    public final static String DEFAULT_PROJECT = "http://localhost:8080/TermBrowser/";

    public static String getSwVersion() {
        return "3.2.9";
    }

    public static enum STATUS_CODES {

        INACTIVE(0), ACTIVE(1), DELETED(2);
        private int code;

        private STATUS_CODES(int c) {
            code = c;
        }

        public int getCode() {
            return code;
        }

        public static boolean isStatusCodeValid(Integer StatusCode) {
            STATUS_CODES[] codes = STATUS_CODES.values();

            for (int i = 0; i < codes.length; ++i) {
                if (codes[i].getCode() == StatusCode) {
                    return true;
                }
            }
            return false;
        }

        public static String readStatusCodes() {
            String s = "";
            STATUS_CODES[] codes = STATUS_CODES.values();

            for (int i = 0; i < codes.length; ++i) {
                s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
            }
            return s;
        }
    }

  //public final static String STD_REPOSITORY_ADRESS = "http://193.25.22.68:8080/StandardsRepository/";
}
