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

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner, Sven Becker
 */
public class SessionHelper
{

  final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static Properties properties = null;

  public String getProperty(String property)
  {
    if (loadPropertyFile())
      return properties.getProperty(property);
    else
      return "";
  }

  private static boolean loadPropertyFile()
  {
    if (properties == null)
    {
      try
      {
        properties = new Properties();
        InputStream in = SessionHelper.class.getResourceAsStream("termBrowserSettings.properties");
        properties.load(in);
        in.close();
      }
      catch (IOException e)
      {
        System.out.println("TermBrowser: Could not open Config file. Reason: \n\n" + e.getMessage());
        return false;
      }
    }
    return true;
  }

  public static String getHostUrl()
  {
    if (loadPropertyFile())
      return properties.getProperty("urlHost");
    else
      return "";
  }

  public static String getServiceName()
  {
    if (loadPropertyFile())
      return properties.getProperty("urlHostServiceName");
    else
      return "";
  }

  public static boolean isUserLoggedIn()
  {
    String s = getSessionId();
    if (s != null && s.length() > 0)
    {
      return true;
    }
    return false;
  }

  public static boolean isAdmin()
  {
    Object o = getSessionObjectByName("is_admin");

    if (o == null)
    {
      return false;
    }
    else
    {
      return Boolean.parseBoolean(o.toString());
    }
  }

  public static long getUserID(HttpSession session)
  {
    //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session == null)
    {
      LOGGER.debug("getUserID() - Session ist null");
      return 0;
    }

    LOGGER.debug("getUserID(HttpSession session) mit session-id: " + session.getId());
    Enumeration en = session.getAttributeNames();
    while (en.hasMoreElements())
    {
      Object o = en.nextElement();
      LOGGER.debug("Object in Session mit Typ: " + o.getClass().getCanonicalName());
    }

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

  public static String getServerName()
  {
    return Sessions.getCurrent().getServerName();
  }

    public static String getUserName(){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        if (session == null)
            return "";

        Object userName = session.getAttribute("user_name");

        if (userName == null)
            return "";
        else
            return userName.toString();
    }
  
    /**
     * Retrieves the collaboration user name and returns it as a string, if it 
     * is not null.
     * @return the collaboration user name as a string.
     */
    public static String getCollaborationUserName(){
        Object collaborationUserName = getSessionObjectByName("collaboration_user_name");

        if (collaborationUserName == null){
            LOGGER.error("Error [0002]: The collaboration user name is null");
            return "";
        }
        else
            return collaborationUserName.toString();
    }
  
  public static String getCollaborationUserRole()
  {
    Object o = getSessionObjectByName("collaboration_user_role");

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

  public static long getUserID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if (session == null)
    {
      //logger.debug("getUserID() - Session ist null");
      return 0;
    }

    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      //logger.debug("getUserID() - o ist null");
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

  public static long getPersonID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    Object o = session.getAttribute("person_id");

    if (o == null)
    {
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
        LOGGER.error("could not get personID: " + e.getMessage());
        return 0;
      }
    }
  }

    /**
     * Calls Sessions.getCurrent(), if this session is not null and the
     * sessionID is also not null, this ID is returned as a string.
     * @return the sessionID as a string
     */
    public static String getSessionId(){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        if (session == null){
            LOGGER.error("Error [0001]: Current session is null");
            return "";
        }

        Object sessionID = session.getAttribute("session_id");

        if (sessionID == null){
            LOGGER.error("Error [0000]: The current session's ID is null");
            return "";
        }
        else
            return sessionID.toString();
    }

  public static boolean isCollaborationLoggedIn(HttpSession httpSession)
  {
    long id = getCollaborationUserID(httpSession);
    return id > 0;
  }

  public static long getCollaborationUserID()
  {
    return getCollaborationUserID(null);
  }
  public static long getCollaborationUserID(HttpSession httpSession)
  {
    //return 5l; 

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

  public static void setValue(String Name, Object Value)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
      session.setAttribute(Name, Value);
  }

    /**
     * Retrieves the current session and returns the attribut which has the
     * same name as the given parameter if the session is not null.
     * @param name the name of the returned attribute
     * @return the value of session.getAttribute(name) or null
     */
    public static Object getSessionObjectByName(String name){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        
        if (session != null)
            return session.getAttribute(name);
        else
            return null;
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

  /**
   * Ändert den Status der Kollaboration
   *
   * @return false: Kollaboration ist deaktiviert, true: aktiviert
   */
  public static boolean switchCollaboration()
  {

    boolean active = false;
    Object o = getSessionObjectByName("CollaborationActive");
    if (o != null)
    {
      active = (Boolean) o;
    }

    LOGGER.debug("switchCollaboration(), aktuell: " + active);

    active = !active; // Zustand tauschen

    setValue("CollaborationActive", active);
    LOGGER.debug("neu: " + active);

    return active;
  }

  public static boolean isCollaborationActive()
  {
    return isCollaborationActive(null);
  }

  public static boolean isCollaborationActive(HttpSession httpSession)
  {
    boolean active = false;
    Object o = getValue("CollaborationActive", httpSession);
    if (o != null)
    {
      active = (Boolean) o;
    }
    if(active)
    {
      return getCollaborationUserID(httpSession) > 0;
    }
    return false;
  }
  
  public static boolean isCollaborationFlag()
  {
    return isCollaborationActive(null);
  }

  public static boolean isCollaborationFlag(HttpSession httpSession)
  {
    boolean active = false;
    Object o = getValue("CollaborationActive", httpSession);
    if (o != null)
    {
      active = (Boolean) o;
    }
    return active;
  }

//// Neue Settings für UserAccounts //////////////////////////////////////////
//    
//    // Anzeigen von Crossmapping?
//    public static boolean isShowCrossmapping() {
//        Object o = getValue("is_ShowCrossmapping");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
//    // Anzeigen von Verknüpften Konzepten?
//    public static boolean isShowLinkedConcepts() {
//        Object o = getValue("is_ShowLinkedConcepts");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
  // Soll beim Anklicken eines CS/VS sofort die aktuelle Version geladen werden?
  public static boolean isLoadCurrentVersion()
  {
    Object o = getSessionObjectByName("is_LoadCurrentVersion");

    if (o == null)
      return false;
    else
      return Boolean.parseBoolean(o.toString());
  }
  
  public static String getCollaborationUserRoleFromTermAdmLogin(){
  
        String role ="";
        //Nur die Benutzer hohlen welche noch nicht zur Termserver DB "zugeordnet" sind!!!
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();
       
        String hqlC = "select distinct cu from Collaborationuser cu where cu.hidden=false AND deleted=0";

        try{

            List<Collaborationuser> userListC = hb_session.createQuery(hqlC).list();

            for(Collaborationuser cu:userListC){

                if((cu.getUsername() + "_tadm").equals(SessionHelper.getUserName())){
                    role = cu.getRoles().iterator().next().getName();
                    break;
                }                
            }

        }catch(Exception e){
          LOGGER.error("[Fehler bei CollabUserHelper.java createCollabUserTable(): " + e.getMessage());
        }
        finally
        {
          hb_session.close();
        } 
        return role;
  }
  
//    
//    // Soll beim Anklicken eines CS/VS sofort die aktuelle Version geladen werden?
//    public static boolean isShowPreferredLanguage() {
//        Object o = getValue("is_ShowPreferredLanguage");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
//    // Bevorzugte Sprache
//    public static int preferredLanguage() {
//        Object o = getValue("is_preferredLanguage");
//
//        if (o == null) 
//            return -1;
//        else 
//            return Integer.valueOf(o.toString());        
//    }
  /*public static boolean isUserLoggedIn(HttpSession Session)
   {
   String s = getSessionId(Session);
   if(s != null && s.length() > 0)
   return true;
   return false;
   //return getUserID(Session) > 0;
   }*/

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
}