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

import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner
 */
public class SessionHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();

  public static boolean isUserLoggedIn()
  {
    return getUserID() > 0;
  }
  
  public static boolean isUserLoggedIn(HttpSession Session)
  {
    return getUserID(Session) > 0;
  }

  public static boolean isAdmin()
  {
    Object o = getValue("is_admin");

    if (o == null)
    {
      return false;
    }
    else
    {
      return Boolean.parseBoolean(o.toString());
    }
  }





  /*public static void checkUserLoggedIn()
  {
    boolean login = isUserLoggedIn();

    if (login == false)
    {
      Clients.showBusy("Nicht eingelogged...");

      LoginHelper.getInstance().reset();
      
      //session.setAttribute("user_id", 0);
      Executions.sendRedirect("/index.zul");
    }

  }*/



  public static long getUserID(HttpSession session)
  {
    //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if(session == null)
    {
      logger.debug("getUserID() - Session ist null");
      return 0;
    }

    logger.debug("getUserID(HttpSession session) mit session-id: " + session.getId());
    /*Enumeration en = session.getAttributeNames();
    while(en.hasMoreElements())
    {
      Object o = en.nextElement();
      logger.debug("Object in Session mit Typ: " + o.getClass().getCanonicalName());
    }*/
    
    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      logger.debug("getUserID() - o ist null");
      return 0;
    }
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("getUserID() - Fehler: " + e.getMessage());
        return 0;
      }
    }
  }

  public static String getUserName()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if(session == null)
    {
      logger.debug("getUserName() - Session ist null");
      return "";
    }

    Object o = session.getAttribute("user_name");

    if (o == null)
    {
      logger.debug("getUserName() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }

  public static long getUserID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if(session == null)
    {
      logger.debug("getUserID() - Session ist null");
      return 0;
    }

    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      logger.debug("getUserID() - o ist null");
      return 0;
    }
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("getUserID() - Fehler: " + e.getMessage());
        return 0;
      }
    }
  }

  public static long getPersonID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    Object o = session.getAttribute("person_id");

    if (o == null)
      return 0;
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("could not get personID: " + e.getMessage());
        return 0;
      }
    }
  }

  public static void setValue(String Name, Object Value)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    session.setAttribute(Name, Value);
    
    logger.debug("SessionHelper.setValue(): " + Name + ", " + Value);
  }

  public static Object getValue(String Name)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    return session.getAttribute(Name);
  }
  
  public static String getCollaborationUserRole()
  {
    Object o = getValue("collaboration_user_role");

    if (o == null)
    {
      //logger.debug("getUserName() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }
  
  public static long getCollaborationUserID()
  {
    return getCollaborationUserID(null);
  }
	
  public static long getCollaborationUserID(HttpSession httpSession)
  {
    //return 5l; 
		if (httpSession == null){
			
			org.zkoss.zk.ui.Session session = Sessions.getCurrent();
			Object o = session.getAttribute("collaboration_user_id");
			return Long.parseLong(o.toString());
		}

    Object o = getValue("collaboration_user_id", httpSession);

    try
    {
      return Long.parseLong(o.toString());
    }
    catch (Exception e)
    {
      //logger.error("getCollaborationUserID() - Fehler: " + e.getMessage() + ", Objekt: " + o);
      return 0;
    }
  }
  
  public static Object getValue(String Name, HttpSession httpSession)
  {
    if (httpSession != null)
    {
      return httpSession.getAttribute(Name);
    }

    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
      return session.getAttribute(Name);
    else
      return null;
  }
}
