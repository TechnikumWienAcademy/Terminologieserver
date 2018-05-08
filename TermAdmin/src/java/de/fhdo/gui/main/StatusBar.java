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
package de.fhdo.gui.main;

import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.SessionHelper;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class StatusBar  extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();

  //private long userID = 0;
  private long personID = 0;

  public void afterCompose()
  {
    org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();
    
    String user = "";
    if(session.getAttribute("user_name") != null)
      user = session.getAttribute("user_name").toString();
    
    boolean isAdmin = false;
    if(session.getAttribute("is_admin") != null)
      isAdmin = Boolean.parseBoolean(session.getAttribute("is_admin").toString());

    if(session.getAttribute("person_id") != null)
      personID = Long.parseLong(session.getAttribute("person_id").toString());

    logger.debug("[StatusBar] PersonID: " + personID);
    logger.debug("[StatusBar] user_name: " + user);

    Toolbarbutton tbbU = (Toolbarbutton)getFellow("tb_user");
    tbbU.setLabel("Benutzer-Details");

    if(isAdmin)
      tbbU.setImage("/rsc/img/symbols/user_admin_16x16.png");
    else tbbU.setImage("/rsc/img/symbols/user_16x16.png");
    
    if(user.length() > 0){
        Toolbarbutton tbb = (Toolbarbutton) this.getFellow("tb_loginInfo");
        tbb.setVisible(true);
        String infoTerm ="";
        
        if(SessionHelper.isUserLoggedIn())
            infoTerm ="Eingeloggt in Verwaltungsumgebung als: \"" + SessionHelper.getUserName() + "\"";

        String t = infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRole();
        tbb.setLabel(t);
    } 
  }

  public void onLogoutClicked()
  {
    //LoginHelper.getInstance().logout();
		org.zkoss.zk.ui.Session session = Sessions.getCurrent();
		session.removeAttribute("user_id");
		session.removeAttribute("user_name");
		session.removeAttribute("session_id");
		session.removeAttribute("collaboration_user_id");
		session.removeAttribute("collaboration_user_name");
		session.removeAttribute("collaboration_user_role");
	
    Executions.sendRedirect("../../../TermBrowser/gui/admin/logout.zul");
  }
  
  public void onCallBrowserClicked()
  {
      Executions.sendRedirect("../../../TermBrowser/gui/main/main.zul");
  }

  public void onUserClicked()
  {
    // User-Details öffnen
    /*try
    {

      Map map = new HashMap();
      map.put("person_id", personID);

      Window win = (Window) Executions.createComponents(
        "/gui/main/masterdata/masterdataDetails.zul", null, map);

      //((MasterdataDetails) win).setUpdateInterface(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Ã–ffnen der Masterdaten: " + ex.getLocalizedMessage());
      ex.printStackTrace();
      //logger.error("Fehler in Klasse '" + DiagnosisDetails.class.getName()
      //  + "': " + ex.getMessage());
    }*/
   
    // Passwort ändern
    try
    {
      logger.debug("Erstelle Fenster...");

      Map map = new HashMap();
      map.put("user_id", SessionHelper.getUserID());
      
      Window win = (Window) Executions.createComponents(
        "/gui/main/masterdata/userDetails.zul", null, map);

      //((PasswordDetails) win).setUpdateListInterface(this);

      logger.debug("Ã–ffne Fenster...");
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + this.getClass().getName()
        + "': " + ex.getMessage());
    }
  }
}
