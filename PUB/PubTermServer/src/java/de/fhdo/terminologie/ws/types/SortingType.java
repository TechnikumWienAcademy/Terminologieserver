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
public class SortingType
{
  public enum SortType { ALPHABETICALLY, ORDER_NR};
  public enum SortByField { CODE, TERM};
  public enum SortDirection { ASCENDING, DESCENDING};
  
  
  private SortType sortType;
  private SortByField sortBy;
  private SortDirection sortDirection;
  

  /**
   * @return the sortType
   */
  public SortType getSortType()
  {
    return sortType;
  }

  /**
   * @param sortType the sortType to set
   */
  public void setSortType(SortType sortType)
  {
    this.sortType = sortType;
  }

  /**
   * @return the sortBy
   */
  public SortByField getSortBy()
  {
    return sortBy;
  }

  /**
   * @param sortBy the sortBy to set
   */
  public void setSortBy(SortByField sortBy)
  {
    this.sortBy = sortBy;
  }

  /**
   * @return the sortDirection
   */
  public SortDirection getSortDirection()
  {
    return sortDirection;
  }

  /**
   * @param sortDirection the sortDirection to set
   */
  public void setSortDirection(SortDirection sortDirection)
  {
    this.sortDirection = sortDirection;
  }
    
  
  
  
}
