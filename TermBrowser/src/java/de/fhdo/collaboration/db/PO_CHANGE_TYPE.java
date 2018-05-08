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
public enum PO_CHANGE_TYPE
{
  NONE(0, ""),
  NEW(1, "Neu"),
  CHANGED(2, "Ändern"),
  DELETED(3, "Löschen");
  
  private final int id;
  private final String bezeichnung;

  private PO_CHANGE_TYPE(int ID, String Bezeichnung)
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

  public static PO_CHANGE_TYPE get(int ID)
  {
    PO_CHANGE_TYPE[] values = PO_CHANGE_TYPE.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].id() == ID)
        return values[i];
    }
    return PO_CHANGE_TYPE.NONE;
  }
  
  public static String getString(int ID)
  {
    PO_CHANGE_TYPE ct = get(ID);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}
