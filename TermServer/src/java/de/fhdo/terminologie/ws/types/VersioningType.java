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
package de.fhdo.terminologie.ws.types;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class VersioningType
{
  private Boolean createNewVersion;
  private Boolean majorUpdate;
  private Boolean minorUpdate;

  /**
   * @return the createNewVersion
   */
  public Boolean getCreateNewVersion()
  {
    return createNewVersion;
  }

  /**
   * @param createNewVersion the createNewVersion to set
   */
  public void setCreateNewVersion(Boolean createNewVersion)
  {
    this.createNewVersion = createNewVersion;
  }

  /**
   * @return the majorUpdate
   */
  public Boolean getMajorUpdate()
  {
    return majorUpdate;
  }

  /**
   * @param majorUpdate the majorUpdate to set
   */
  public void setMajorUpdate(Boolean majorUpdate)
  {
    this.majorUpdate = majorUpdate;
  }

  /**
   * @return the minorUpdate
   */
  public Boolean getMinorUpdate()
  {
    return minorUpdate;
  }

  /**
   * @param minorUpdate the minorUpdate to set
   */
  public void setMinorUpdate(Boolean minorUpdate)
  {
    this.minorUpdate = minorUpdate;
  }
}
