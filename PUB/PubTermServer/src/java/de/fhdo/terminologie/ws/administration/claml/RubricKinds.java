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
public class RubricKinds
{
  public static enum RUBRICKINDS
  {
    preferred("preferred", "The attribute kind='preferred' defines a specific unique term that identifies the meaning of a class."), 
    inclusion("inclusion", "The attribute kind='inclusion' shall be used for additional terms that can be used within a class."), 
    exclusion("exclusion", "The attribute kind='exclusion' shall be used for terms that are excluded from a class."),
    coding_hint("coding_hint", "Coding instructions"), 
    definition("definition", "Otherwise unspecified texts added to rubrics. Should be used for a descriptive phrase for a given concept in a healthcare classification system."), 
    note("note", "General remark"), 
    text("text", "e.g. a text for a Modifier"), 
    title("title", "A title for a text rubric"), 
    introduction("introduction", "A long text at the beginning of a chapter."),
    footnote("footnote", "As in the printed versions of ICD."), 
    etiology("etiology", "The basic cause or underlying disease process is assigned a code marked with a dagger (â€ )."), 
    manifestation("manifestation", "The clinical manifestation is marked with an asterisk (*).");
    
    private String code, noteAttr;

    private RUBRICKINDS(String c, String s)
    {
      code = c;
      noteAttr = s;
    }

    public String getCode()
    {
      return code;
    }

    /*public static boolean isStatusCodeValid(Integer StatusCode)
    {
      RUBRICKINDS[] codes = RUBRICKINDS.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode().equals(StatusCode))
          return true;
      }
      return false;
    }*/

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

    /**
     * @return the noteAttr
     */
    public String getNoteAttr()
    {
      return noteAttr;
    }
  }

  
  
}
