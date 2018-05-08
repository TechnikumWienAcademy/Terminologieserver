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

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeCellType
{
  private Object data;
  private boolean showLabel;
  private String label;
  private String style;

  public GenericTreeCellType(Object data, boolean showLabel, String label)
  {
    this.data = data;
    this.showLabel = showLabel;
    this.label = label;
  }
  
  public GenericTreeCellType(Object data, boolean showLabel, String label, String style)
  {
    this.data = data;
    this.showLabel = showLabel;
    this.label = label;
    this.style = style;
  }
  
  public Object getDisplayData()
  {
    if(showLabel)
      return label;
    else return data;
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
   * @return the showLabel
   */
  public boolean isShowLabel()
  {
    return showLabel;
  }

  /**
   * @param showLabel the showLabel to set
   */
  public void setShowLabel(boolean showLabel)
  {
    this.showLabel = showLabel;
  }

  /**
   * @return the label
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label)
  {
    this.label = label;
  }

  /**
   * @return the style
   */
  public String getStyle()
  {
    return style;
  }

  /**
   * @param style the style to set
   */
  public void setStyle(String style)
  {
    this.style = style;
  }
}
