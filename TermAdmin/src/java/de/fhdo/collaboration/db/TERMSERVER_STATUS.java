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
package de.fhdo.collaboration.db;

/**
 *
 * @author Robert Mützner
 */
public enum TERMSERVER_STATUS
{
  INACTIVE(0, "inaktiv"),
  PUBLIC(1, "öffentlich"),
  DELETED(2, "gelöscht");
  
  private final int id;
  private final String bezeichnung;

  private TERMSERVER_STATUS(int ID, String Bezeichnung)
  {
    this.id = ID;
    this.bezeichnung = Bezeichnung;
  }

  public int id()
  {
    return id;
  }

  public String bezeichnung()
  {
    return bezeichnung;
  }

  public static TERMSERVER_STATUS get(int ID)
  {
    TERMSERVER_STATUS[] values = TERMSERVER_STATUS.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].id() == ID)
        return values[i];
    }
    return TERMSERVER_STATUS.INACTIVE;
  }
  
  public static String getString(int ID)
  {
    TERMSERVER_STATUS ct = get(ID);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}
