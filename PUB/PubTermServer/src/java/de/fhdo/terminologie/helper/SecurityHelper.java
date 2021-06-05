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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.ws.types.LoginType;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SecurityHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static void applyIPAdress(LoginType login, WebServiceContext webServiceContext)
  {
    if (login != null)
    {
      login.setIp(getIp(webServiceContext));
      logger.debug("IP: " + login.getIp());
    }
  }
  
  /**
   * Diese Methode gibt die Client IP Adresse zurück.
   * Beim Aufruf eines WebServices wird diese über den WebServiceContext mitgegeben.
   *
   * @param webServiceContext
   * @return
   */
  public static String getIp(WebServiceContext wsc)
  {
    try
    {
      //if (logger.isDebugEnabled())
      //  logger.debug("Get IP Adress");

      MessageContext msgCtxt = wsc.getMessageContext();
      HttpServletRequest ht_request = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
      String clientIP = ht_request.getRemoteAddr();
      
      /*if (logger.isInfoEnabled())
      {
        logger.info("Zugriffszeitpunkt: " + new java.util.Date() + " | Client IP: " + clientIP);
      }*/
      return clientIP;
    }
    catch (Exception e)
    {
      /*if (logger.isDebugEnabled())
      {
        logger.debug("[Security] Fehler beim ermitteln der Clien IP: " + e.getMessage());
      }*/
      return "";
    }

  }
  
}
