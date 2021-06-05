/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.administration.claml;

/**
 *
 * @author Robert Mützner
 */
public class MetadataDefinition
{
  public static enum METADATA_ATTRIBUTES
  {
    termAbbrevation("TS_ATTRIBUTE_TERMABBREVATION"), 
    meaning("TS_ATTRIBUTE_MEANING"), 
    hints("TS_ATTRIBUTE_HINTS"), 
    minorRevision("TS_ATTRIBUTE_MINORREVISION"), 
    majorRevision("TS_ATTRIBUTE_MAJORREVISION"), 
    status("TS_ATTRIBUTE_STATUS"), 
    statusDate("TS_ATTRIBUTE_STATUSDATE"), 
    isLeaf("TS_ATTRIBUTE_ISLEAF")
    ;
    
    private String claml_name;

    private METADATA_ATTRIBUTES(String c)
    {
      claml_name = c;
    }

    public String getCode()
    {
      return claml_name;
    }
    
    public static boolean isCodeValid(String Code)
    {
      METADATA_ATTRIBUTES[] codes = METADATA_ATTRIBUTES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode().equals(Code))
          return true;
      }
      return false;
    }

    /*public static String readStatusCodes()
    {
      String s = "";
      RUBRICKINDS[] codes = RUBRICKINDS.values();

      for (int i = 0; i < codes.length; ++i)
      {
        s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
      }
      return s;
    }*/


  
  }
}
