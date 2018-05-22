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
package de.fhdo.db;

/**
 *
 * @author Robert Mützner
 */
public class Definitions
{
  // Benötigte Domains
  public final static long DOMAINID_ISO_639_1_LANGUACECODES = 1;
  public final static long DOMAINID_VALIDITYDOMAIN = 2;
  public final static long DOMAINID_DISPLAY_ORDER = 3;
  public final static long DOMAINID_IMPORT_FORMATS = 4;
  public final static long DOMAINID_EXPORT_FORMATS = 5;
  public final static long DOMAINID_CODESYSTEM_TYPES = 6;
  public final static long DOMAINID_METADATAPARAMETER_TYPES = 7;
  public final static long DOMAINID_CODESYSTEM_TAXONOMY = 8;
  
  
  public final static String TECHNICAL_TYPE_PARAMETRIERUNG = "param";
  public final static String TECHNICAL_TYPE_MENU = "menu";
  public final static String TECHNICAL_TYPE_SUBMENU = "submenu";
  public final static String TECHNICAL_TYPE_TOOLMENU = "toolmenu";

  public final static long DISPLAYORDER_ID = 185;
  public final static long DISPLAYORDER_ORDERID = 186;
  public final static long DISPLAYORDER_NAME = 187;
  
  public final static String APP_KEY = "TERMSERVER";
  
  
  /*public final static long TECHNICALTYPE_DOCUMENT = 2196;
  public final static long TECHNICALTYPE_LINK = 2197;
  public final static long TECHNICALTYPE_NOTE = 2198;*/
  
  

  //public final static String TODO_ANWENDUNG_ADRESS = "http://www.ebpg.mi.fh-dortmund.de:8080/toDO/";
  //public final static String TODO_ANWENDUNG_ADRESS = "http://193.25.22.68:8080/toDO/";

    public static String getSwVersion() {
        return "3.2.8";
    }
}
