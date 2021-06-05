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
package de.fhdo.list;

/**

 @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class GenericListRowType implements IDoubleClick
{

	public void onDoubleClick(Object o) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
  private Object data;
  private GenericListCellType []cells;
  private String color;

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
  public GenericListCellType[] getCells()
  {
    return cells;
  }

  /**
   * @param cells the cells to set
   */
  public void setCells(GenericListCellType[] cells)
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
}
