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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Robert Mützner
 */
public class UploadCharsetFinder implements org.zkoss.zk.ui.util.CharsetFinder
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public String getCharset(String string, InputStream in) throws IOException
  {
    //logger.debug("UploadCharsetFinder, getCharset for String: " + string);
    
    if(in != null)
    {
      byte[] bytes = new byte[100];
      in.read(bytes, 0, 99);
      String s = new String(bytes);
      
      //logger.debug("String: " + s);
      
      if(s.contains("encoding=\"ISO-8859-1\""))
      {
        logger.debug("ISO-8859-1 gefunden");
        return "ISO-8859-1";
      }
      
      //<?xml version="1.0" encoding="ISO-8859-1"?>
    }
    
    return "UTF-8";
  }
  
}
