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
package de.fhdo.terminologie.ws.authoring.types;

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.VersioningType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class MaintainValueSetRequestType
{
  private LoginType login;
  private VersioningType versioning;
  private ValueSet valueSet;

  /**
   * @return the login
   */
  public LoginType getLogin()
  {
    return login;
  }

  /**
   * @param login the login to set
   */
  public void setLogin(LoginType login)
  {
    this.login = login;
  }

  /**
   * @return the versioning
   */
  public VersioningType getVersioning()
  {
    return versioning;
  }

  /**
   * @param versioning the versioning to set
   */
  public void setVersioning(VersioningType versioning)
  {
    this.versioning = versioning;
  }

  /**
   * @return the valueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the valueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }
}
