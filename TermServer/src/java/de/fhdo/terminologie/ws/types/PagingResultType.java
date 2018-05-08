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

 @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class PagingResultType
{
  private int pageIndex, maxPageSize, count;
  private String pageSize;
  private String message;

  /**
   * @return the pageSize
   */
  public String getPageSize()
  {
    return pageSize;
  }

  /**
   * @param pageSize the pageSize to set
   */
  public void setPageSize(String pageSize)
  {
    this.pageSize = pageSize;
  }

  /**
   * @return the pageIndex
   */
  public int getPageIndex()
  {
    return pageIndex;
  }

  /**
   * @param pageIndex the pageIndex to set
   */
  public void setPageIndex(int pageIndex)
  {
    this.pageIndex = pageIndex;
  }

  /**
   * @return the maxPageSize
   */
  public int getMaxPageSize()
  {
    return maxPageSize;
  }

  /**
   * @param maxPageSize the maxPageSize to set
   */
  public void setMaxPageSize(int maxPageSize)
  {
    this.maxPageSize = maxPageSize;
  }

  /**
   * @return the message
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * @return the count
   */
  public int getCount()
  {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(int count)
  {
    this.count = count;
  }
}
