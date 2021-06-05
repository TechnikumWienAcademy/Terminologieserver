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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CookieHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void setCookie(String name, String value)
  {
    logger.debug("Speicher Cookie: " + name + ", mit Wert: " + value);

    ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(new Cookie(
      name, value));
  }

  public static String getCookie(String name)
  {
    Cookie[] cookies = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getCookies();

    logger.debug("Suche Cookie mit Name: " + name);

    if (cookies != null)
    {
      //logger.debug("Cookies gefunden, Anzahl: " + cookies.length);

      for (Cookie cookie : cookies)
      {
        //logger.debug("Cookie: " + cookie.getName());

        if (cookie.getName().equals(name))
        {
          logger.debug("Cookie '" + name + "' gefunden, Wert: " + cookie.getValue());
          
          return cookie.getValue();
        }
      }
    }
    else 
    {
      
    }

    logger.debug("Kein Cookie fuer '" + name + "' gefunden!");
    return null;
  }

  public static void removeCookie(String name)
  {
    logger.debug("Loesche Cookie: " + name);

    Cookie c = new Cookie(name, "");
    c.setMaxAge(0);

    ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(c);
  }
}
