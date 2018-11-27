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

package de.fhdo.terminologie;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Definitions
{
  public static final long LANGUAGE_GERMAN_ID = 33;
  public static final long LANGUAGE_ENGLISH_ID = 38;
  
  public final static String APP_KEY = "TERMSERVER";

    public static String getSwVersion() {
        return "3.2.21";
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
          return true;
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

  public static enum ASSOCIATION_KIND
  {
    ONTOLOGY(1), TAXONOMY(2), CROSS_MAPPING(3), LINK(4);
    private int code;

    private ASSOCIATION_KIND(int c)
    {
      code = c;
    }

    public int getCode()
    {
      return code;
    }
  }

  public static boolean isAssociationKindValid(Integer kind)
  {
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      if (kinds[i].getCode() == kind)
        return true;
    }
    return false;
  }

  public static String readAssociationKinds()
  {
    String s = "";
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      s += "\n" + kinds[i].name() + " (" + kinds[i].getCode() + ")";
    }
    return s;
  }
}
