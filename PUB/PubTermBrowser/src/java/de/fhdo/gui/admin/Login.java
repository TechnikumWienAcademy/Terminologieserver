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
package de.fhdo.gui.admin;

import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.LoginHelper;
import de.fhdo.models.TreeModelCS;
import de.fhdo.models.TreeModelVS;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Login extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();  
  private boolean erfolg = false;  

  public void loginCheck()
  {
    Button b = (Button) getFellow("loginButton");
    b.setDisabled(true);
    
    // Daten aus dem Formular auslesen
    Textbox tbUser = (Textbox) getFellow("name");
    Textbox tbPass = (Textbox) getFellow("pwd");

    if (logger.isDebugEnabled())
      logger.debug("Login wird durchgefuehrt...");    
    
    // Login-Daten ueberpruefen
    boolean loginCorrect = LoginHelper.getInstance().login(tbUser.getValue(), tbPass.getValue(),false,"bogous","test");
    if(loginCorrect)
    {
      logger.debug("Login correct");
      Clients.showBusy(Labels.getLabel("login.LoginOK") + "\n\n" + Labels.getLabel("login.ReloadTB"));
      
      ComponentHelper.setVisible("warningRow", false, this);
      
      this.setVisible(false);
      this.detach();
      
      TreeModelVS.reloadData(getDesktop()); // neu Laden, da alte Versionen nur nach login angezeigt werden
      TreeModelCS.reloadData(getDesktop()); // neu Laden, da alte Versionen nur nach login angezeigt werden
      
      erfolg = true;      
      //Executions.getCurrent().sendRedirect(null, "_blank");
      //ViewHelper.gotoSrc(null);
    }
    else
    {
      ComponentHelper.setVisible("warningRow", true, this);
      Label lMsg = (Label) getFellow("mesg");
      lMsg.setValue(Labels.getLabel("login.usernameUnknownOrErro"));
      erfolg = false;
    }
    
    b.setDisabled(false);
  }

  /**
   * @return the erfolg
   */
  public boolean isErfolg()
  {
    return erfolg;
  }

    public void afterCompose() {

    }
}
