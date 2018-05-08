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
package de.fhdo.tree;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeRowType
{
  private Object data;
  private GenericTreeCellType []cells;
  private String color;
  
  private List<GenericTreeRowType> childRows;
  
  public GenericTreeRowType()
  {
    childRows = new LinkedList<GenericTreeRowType>();
  }

  /**
   * @return the data
   */
  public Object getData()
  {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Object data)
  {
    this.data = data;
  }

  /**
   * @return the cells
   */
  public GenericTreeCellType[] getCells()
  {
    return cells;
  }

  /**
   * @param cells the cells to set
   */
  public void setCells(GenericTreeCellType[] cells)
  {
    this.cells = cells;
  }

  /*public int compareTo(Object o)
  {
    if(o instanceof GenericListRowType)
    {
      GenericListRowType o2 = (GenericListRowType)o;
      
      
    }
    
    return 0;
  }*/

  /**
   * @return the color
   */
  public String getColor()
  {
    return color;
  }

  /**
   * @param color the color to set
   */
  public void setColor(String color)
  {
    this.color = color;
  }

  /**
   * @return the childRows
   */
  public List<GenericTreeRowType> getChildRows()
  {
    return childRows;
  }

  /**
   * @param childRows the childRows to set
   */
  public void setChildRows(List<GenericTreeRowType> childRows)
  {
    this.childRows = childRows;
  }
}
