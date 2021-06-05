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
package de.fhdo.terminologie.ws.search.types;

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Die Xml-Annotationen am Anfang bestimmen die Reihenfolge der
 * XML-Elemente in der Response - returnInfos sollten immer zuerst erscheinen
 * 
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@XmlRootElement
@XmlType(name = "", propOrder = { "returnInfos", "valueSet"})
public class ListValueSetsResponseType
{
  private ReturnType returnInfos;
  private List<ValueSet> valueSet;
  
  public ListValueSetsResponseType()
  {
    
  }

  /**
   * @return the returnInfos
   */
  public ReturnType getReturnInfos()
  {
    return returnInfos;
  }

  /**
   * @param returnInfos the returnInfos to set
   */
  public void setReturnInfos(ReturnType returnInfos)
  {
    this.returnInfos = returnInfos;
  }

  /**
   * @return the valueSet
   */
  public List<ValueSet> getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the valueSet to set
   */
  public void setValueSet(List<ValueSet> valueSet)
  {
    this.valueSet = valueSet;
  }
  
}
