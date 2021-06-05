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
package de.fhdo.helper;

import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner
 */
public class ParameterHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /*public static Object getObject(String ParameterName)
  {
    try
    {
      Object o = Executions.getCurrent().getAttribute(ParameterName);
      //Executions.getCurrent().getAttribute(ParameterName)
      return o;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }*/
  
    public static Boolean getBoolean(String ParameterName){
        Boolean b = null;
        String s = Executions.getCurrent().getParameter(ParameterName);
        try{                        
            if(s.contains("true") || s.contains("1") || Integer.valueOf(s) > 0)
                b = Boolean.TRUE;
        } catch (Exception e) { }
        
        try{
            if (s.contains("false") || s.contains("0") || Integer.valueOf(s) < 0)
                b = Boolean.FALSE;                                            
        } catch (Exception e) { }

        return b;
    }
  
  public static String getString(String ParameterName)
  {
    String wert = "";
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = o.toString();
    }
    catch (Exception e)
    {
    }

    return wert;
  }

  public static Integer getInteger(String ParameterName)
  {
    Integer wert = 0;
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = Integer.parseInt(o.toString());
    }
    catch (Exception e)
    {
    }

    return wert;
  }

  public static long getLong(String ParameterName)
  {
    long wert = 0;
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = Long.parseLong(o.toString());
    }
    catch (Exception e)
    {
    }

    return wert;
  }
}
