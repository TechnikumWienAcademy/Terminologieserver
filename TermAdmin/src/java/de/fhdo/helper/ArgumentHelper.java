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

import java.util.Map;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner
 */
public class ArgumentHelper
{
  
  public static Object getWindowArgument(String argName)
  {
    try
    {
      Map args = Executions.getCurrent().getArg();

      if (args.get(argName) != null)
        return args.get(argName);
    }
    catch (Exception e)
    {
      //LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return null;
  }
  
  public static long getWindowArgumentLong(String argName)
  {
    long l = 0;
    try
    {
      Object o = getWindowArgument(argName);

      if (o != null)
        l = (Long) o;
    }
    catch (Exception e)
    {
      //LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return l;
  }
  
}
