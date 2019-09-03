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

import javax.servlet.http.HttpSession;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner
 */
public class SessionHelper{
    private final static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Checks if the getUserID() return-value is greater 0.
     * @return true if the check passed
     */
    public static boolean isUserLoggedIn(){
        return getUserID() > 0;
    }
  
    public static boolean isUserLoggedIn(HttpSession Session){
      return getUserID(Session) > 0;
    }

    /**
     * Checks if the session is an admin session and returns the result.
     * @return true if the user is an admin, otherwise false
     */
    public static boolean isAdmin(){
        Object isAdmin = getSessionAttributeByName("is_admin");

        if (isAdmin == null)
            return false;
        else
            return Boolean.parseBoolean(isAdmin.toString());
    }



  public static long getUserID(HttpSession session)
  {
    //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if(session == null)
    {
      LOGGER.debug("getUserID() - Session ist null");
      return 0;
    }

    LOGGER.debug("getUserID(HttpSession session) mit session-id: " + session.getId());
    /*Enumeration en = session.getAttributeNames();
    while(en.hasMoreElements())
    {
      Object o = en.nextElement();
      logger.debug("Object in Session mit Typ: " + o.getClass().getCanonicalName());
    }*/
    
    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      LOGGER.debug("getUserID() - o ist null");
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
        LOGGER.error("getUserID() - Fehler: " + e.getMessage());
        return 0;
      }
    }
  }

  public static String getUserName()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if(session == null)
    {
      LOGGER.debug("getUserName() - Session ist null");
      return "";
    }

    Object o = session.getAttribute("user_name");

    if (o == null)
    {
      LOGGER.debug("getUserName() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }

    /**
     * Retrieves the current session and then its userID-object. If both of them
     * are not null, the object's toString() result will be cast to a long-value
     * which is then returned.
     * @return either the long-value of the userID or 0.
     */
    public static long getUserID(){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        if(session == null){
            LOGGER.error("Error [0006]: Session is null");
            return 0;
        }

        Object objectUserID = session.getAttribute("user_id");

        if (objectUserID == null){
            LOGGER.error("Error [0007]: UserID object is null");
            return 0;
        }
        else{
            try{
                return Long.parseLong(objectUserID.toString());
            }
            catch (Exception e){
                LOGGER.error("Error [0008]", e);
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
        LOGGER.error("could not get personID: " + e.getMessage());
        return 0;
      }
    }
  }

  public static void setValue(String Name, Object Value)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    session.setAttribute(Name, Value);
    
    LOGGER.debug("SessionHelper.setValue(): " + Name + ", " + Value);
  }

    /**
     * Retrieves the current session and returns the object resulting from the
     * getAttribute(name) call.
     * @param name the name of the attribute
     * @return the object of the attribute
     */
    public static Object getSessionAttributeByName(String name){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        return session.getAttribute(name);
    }
  
  public static String getCollaborationUserRole()
  {
    Object o = getSessionAttributeByName("collaboration_user_role");

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
  
    public static Object getValue(String Name, HttpSession httpSession){
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
