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
package de.fhdo.terminologie.ws.administration.types;

import de.fhdo.terminologie.ws.types.ReturnType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
@XmlRootElement
@XmlType(name = "", propOrder = { "returnInfos", "isRunning", "currentIndex", "totalCount"} )
public class ImportCodeSystemStatusResponseType
{
  private ReturnType returnInfos;
  
  private Boolean isRunning;
  private Integer currentIndex;
  private Integer totalCount;
  
  
  /**
     * @return the returnInfos
     */
    public ReturnType getReturnInfos() {
        return returnInfos;
    }

    /**
     * @param returnInfos the returnInfos to set
     */
    public void setReturnInfos(ReturnType returnInfos) {
        this.returnInfos = returnInfos;
    }

  /**
   * @return the isRunning
   */
  public Boolean getIsRunning()
  {
    return isRunning;
  }

  /**
   * @param isRunning the isRunning to set
   */
  public void setIsRunning(Boolean isRunning)
  {
    this.isRunning = isRunning;
  }

  /**
   * @return the currentIndex
   */
  public Integer getCurrentIndex()
  {
    return currentIndex;
  }

  /**
   * @param currentIndex the currentIndex to set
   */
  public void setCurrentIndex(Integer currentIndex)
  {
    this.currentIndex = currentIndex;
  }

  /**
   * @return the totalCount
   */
  public Integer getTotalCount()
  {
    return totalCount;
  }

  /**
   * @param totalCount the totalCount to set
   */
  public void setTotalCount(Integer totalCount)
  {
    this.totalCount = totalCount;
  }
}
