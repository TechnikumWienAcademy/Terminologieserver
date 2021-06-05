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
package de.fhdo.gui.header;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class StatusBar extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();
  //private long userID = 0;
  private boolean collaboration = false;

  public void afterCompose()
  {
    org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();
    
    String user = "";

    if (SessionHelper.isCollaborationActive())
    {
      collaboration = true;
      user = SessionHelper.getCollaborationUserName();
    }
    else
    {
      user = SessionHelper.getUserName();
    }
    
    if (user.length() > 0)
    {
      boolean isAdmin = false;
      if (session.getAttribute("is_admin") != null)
        isAdmin = Boolean.parseBoolean(session.getAttribute("is_admin").toString());

      Toolbarbutton tbb = (Toolbarbutton) getFellow("tb_user");
      tbb.setLabel("Benutzer-Details");

      if (isAdmin)
        tbb.setImage("/rsc/img/symbols/user_admin_16x16.png");
      else
        tbb.setImage("/rsc/img/symbols/user_16x16.png");
    }
    
    /*
    ComponentHelper.setVisible("tb_user", user.length() > 0, this);
    ComponentHelper.setVisible("tb_logout", user.length() > 0, this);
    ComponentHelper.setVisible("tb_termadmin", user.length() > 0 && collaboration == false && SessionHelper.isAdmin() == true, this);
    */
    
    ComponentHelper.setVisible("tb_user", user.length() > 0 && collaboration == true, this);
    //ComponentHelper.setVisible("tb_logout", user.length() > 0 && collaboration == true, this);
    ComponentHelper.setVisible("tb_termadmin", false, this);
    
    if(user.length() > 0){
        Toolbarbutton tbb = (Toolbarbutton) this.getFellow("tb_loginInfo");
        tbb.setVisible(true);
        String infoKollab ="";
        String infoTerm ="";
        
        if(collaboration)//show kollab Login info
            infoKollab = "Eingeloggt in Kollaborationsumgebung als: \"" + SessionHelper.getCollaborationUserName() + "\"";
        
        if(SessionHelper.isUserLoggedIn())
            infoTerm ="Eingeloggt in Verwaltungsumgebung als: \"" + SessionHelper.getUserName() + "\"";
            
        if(!infoKollab.equals("") && infoTerm.equals("")){//only kollab
            String t = infoKollab + " | Rolle: " + SessionHelper.getCollaborationUserRole();
            tbb.setLabel(t);
        }else if(infoKollab.equals("") && !infoTerm.equals("")){ //only termadm
            String t = infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRoleFromTermAdmLogin();// No collab Info!!!!
            tbb.setLabel(t);
        }else{ //both
            String t = infoKollab + " | " + infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRole();
            tbb.setLabel(t);
        }
    }
    

    Object pubSessionId = SessionHelper.getSessionObjectByName("pub_session_id");
    Toolbarbutton tbb = (Toolbarbutton) getFellow("tb_pubConnection");
		
		if (SessionHelper.isUserLoggedIn() && SessionHelper.isAdmin()){// && session.getAttribute("pub_collab_session") != null){
			//if((session.getAttribute("pub_collab_session") != null)
       //     && (!session.getAttribute("pub_collab_session").toString().equals("")))
            //&& (pubSessionId != null)
            //&& (!pubSessionId.toString().equals(""))
            //&& (!pubSessionId.toString().equals("0")))
			//{
				tbb.setVisible(true);
				tbb.setLabel("Publikationsplattform: Verbindung hergestellt");
				tbb.setStyle("background-color: green;");
				session.setAttribute("pub_connection", "connected");

			}
			else
			{
				tbb.setVisible(true);
				tbb.setLabel("Publikationsplattform: Verbindung getrennt");
				tbb.setStyle("background-color: red;");
				session.setAttribute("pub_connection", "disconnected");
			}
		//}
    
  }

  public void onCallAdminClicked()
  {
      String path = de.fhdo.db.DBSysParam.getInstance().getStringValue("weblink", null, null);
      path += "/gui/admin/admin.zul";
      Executions.sendRedirect(path);
  }

  public void onUserClicked()
  {
    // User-Details öffnen
    if (collaboration)
    {
      try
      {
        Map map = new HashMap();
        map.put("user_id", SessionHelper.getCollaborationUserID());
        map.put("termuser_id", SessionHelper.getSessionObjectByName("user_id"));

        Window win = (Window) Executions.createComponents(
                "/collaboration/userDetails.zul", null, map);

        //((MasterdataDetails) win).setUpdateInterface(RefThis);

        win.doModal();
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
    }
  }
}
