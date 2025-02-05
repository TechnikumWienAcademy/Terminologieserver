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
package de.fhdo.collaboration;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.helper.LoginHelper;
import de.fhdo.helper.CookieHelper;
import de.fhdo.helper.SessionHelper;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert M�tzner
 */
public class Login extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static final long serialVersionUID = 1L;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private String username;
  private int loginCount = 0;
  private boolean activated = false;
  private Label labelVersion;

  public Login()
  {
    logger.debug("Login()");
    
    username = "";

    // Cookies lesen
    String user = CookieHelper.getCookie("username_collaboration");

    String activationMD5 = "" + (String) Executions.getCurrent().getParameter("reg");

    if (activationMD5.length() > 0 && activationMD5.equals("null") == false)
    {
      // Benutzer jetzt aktivieren(!)
      activated = de.fhdo.collaboration.helper.LoginHelper.getInstance().activate(activationMD5);
    }
    
    if (user != null && user.length() > 0)
      username = user;

    String userParam = "" + (String) Executions.getCurrent().getParameter("user");
    if (userParam.length() > 0 && userParam.equals("null") == false)
      username = userParam;
  }

  public void backToTermBrowser()
  {
    Executions.sendRedirect("/gui/main/main.zul");
  }

  public void afterCompose()
  {
    
    labelVersion = (Label) getFellow("labelVersion");
    labelVersion.setValue(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("systemType", null, null) + " Kollaborationsumgebung");
    
    // Fokus auf Eingabefeld setzen
    Textbox tb = null;

    if (username.length() > 0)
      tb = (Textbox) getFellow("tfPassword");
    else
      tb = (Textbox) getFellow("tfUser");

    if (tb != null)
      tb.setFocus(true);

    if (activated)
     {
     Row row = (Row) getFellow("activationRow");
     row.setVisible(true);
     }

    String s_captcha = CookieHelper.getCookie("show_captcha");
    if (s_captcha != null && s_captcha.equals("1"))
    {
      showCaptcha();
    }

    loadInfo();

    // Wartemeldung entfernen
    Clients.clearBusy();
  }

  private void loadInfo()
  {
    //String infoText = DBSysParam.instance().getStringValue("info", null, null);
    String infoText = "";

    Label label = (Label) getFellow("infoLabel");
    label.setValue(infoText);
  }

  /**
   * Im Anmeldefenster wurde "Return" gedr�ckt
   *
   * @param event
   */
  public void onKeyPressed(KeyEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("Enter gedrueckt!");

    Button b = (Button) getFellow("loginButton");
    b.setDisabled(true);

    login();

    b.setDisabled(false);
  }

  /**
   * F�hrt einen Loginversuch mit den Werten aus den Textfeldern durch. Gibt
   * eine Fehlermeldung bei nicht korrekten Anmeldedaten aus.
   *
   */
  public void login()
  {
    try
    {
      LoginHelper.getInstance().reset();
      
      /*Object o = SessionHelper.getValue("captcha_correct_collaboration");

      if (o != null && Boolean.parseBoolean(o.toString()) == false)
      {
        // Captcha-Popup ausklappen
        Window win = (Window) Executions.createComponents(
                "/captchaWin.zul", null, null);

        ((CaptchaWin) win).setUpdateInterface(this);
        win.doModal();

        return;
      }*/

      // Daten aus dem Formular auslesen
      Textbox tbUser = (Textbox) getFellow("tfUser");
      Textbox tbPass = (Textbox) getFellow("tfPassword");

      if (logger.isDebugEnabled())
        logger.debug("Login wird durchgefuehrt...");

      // Login-Daten ueberpruefen
      //AT: gen. pseudonym
      //String pseudonym = UUID.randomUUID().toString();

      boolean loginCorrect = LoginHelper.getInstance().login(tbUser.getValue(), tbPass.getValue());
      
      if (logger.isDebugEnabled())
      {
        logger.debug("loginCorrect: " + loginCorrect);
      }

      if (loginCorrect)
      {
        CollaborationSession.getInstance().getSessionID(username);
        // Hauptseite aufrufen
        CookieHelper.removeCookie("show_captcha");
        CookieHelper.setCookie("username_collaboration", username);
        

        Clients.showBusy("Login erfolgreich\n\nKollaborationsumgebung wird geladen...");
        Executions.getCurrent().sendRedirect("/gui/main/main.zul");
      }
      else
      {
        // Login falsch
        loginCount++;

        /*if (loginCount > 4)
        {
          CookieHelper.setCookie("show_captcha", "1");
          showCaptcha();
        }*/

        showRow("warningRow", true);

        Label label = (Label) getFellow("temp");
        label.setValue("Versuch " + loginCount);
      }

    }
    catch (Exception e)
    {
      logger.error(e.getLocalizedMessage());
      e.printStackTrace();
    }

  }

  /**
   * Sendet das Passwort an die angegebene Email-Adresse
   */
  public void sendPassword() throws InterruptedException
  {
    if (Messagebox.show("M�chten Sie ein neues, zuf�llig generiertes Passwort an Ihre Email-Adresse geschickt bekommen?",
            "Neues Passwort", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES)
    {

      boolean erfolg = LoginHelper.resendPassword(true, username);

      if (erfolg)
      {
        Messagebox.show("Neues Passwort erfolgreich verschickt",
                "Neues Passwort", Messagebox.OK, Messagebox.INFORMATION);
      }
      else
      {
        Messagebox.show("Fehler beim Verschicken des neuen Passworts. Bitte wenden Sie sich an den Administrator.",
                "Neues Passwort", Messagebox.OK, Messagebox.EXCLAMATION);
      }
    }
  }

  private void showRow(String RowID, boolean Visible)
  {
    Row row = (Row) getFellow(RowID);
    row.setVisible(Visible);
  }

  private void showCaptcha()
  {
    SessionHelper.setValue("captcha_correct_collaboration", false);
  }

  /**
   * @return the username
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  public void update(Object o)
  {
    login();
  }
}
