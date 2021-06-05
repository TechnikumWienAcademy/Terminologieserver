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
package de.fhdo.gui;

/**
 *
 * @author Robert Mützner
 */
import de.fhdo.dortmund.DortmundHelper;
import de.fhdo.helper.CookieHelper;
import de.fhdo.helper.DES;
import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import java.util.ResourceBundle;
import java.util.UUID;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

public class Login extends Window implements org.zkoss.zk.ui.ext.AfterCompose, IUpdate
{

  private static final long serialVersionUID = 1L;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private boolean captchaShown = false;
  private String username;
  private int loginCount = 0;
  private boolean activated = false;
  long standardId = 0;
  private Label labelVersion;

  public void backToTermBrowser()
  {
    Executions.sendRedirect("../../TermBrowser/gui/main/main.zul");
  }

  public Login()
  {
    logger.debug("Login()");

    username = "";

    // Cookies lesen
    String user = CookieHelper.getCookie("username");

    if (user != null && user.length() > 0)
      username = user;

    //if (DortmundHelper.getInstance().isFhDortmund())
    if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("fhdo"))
    {
      if (SessionHelper.getUserID() > 0 || DortmundHelper.tryLogin())
      {
        Clients.showBusy("Login erfolgreich\n\nTermBrowser wird geladen...");
        Executions.sendRedirect("/gui/admin/admin.zul");
        return;
      }
    }

    // Parameter lesen
    String logout = "" + (String) Executions.getCurrent().getParameter("do");

    if (logout.equals("logout"))
      LoginHelper.getInstance().logout();

    String activationMD5 = "" + (String) Executions.getCurrent().getParameter("reg");

    if (activationMD5.length() > 0 && activationMD5.equals("null") == false)
    {
      // Benutzer jetzt aktivieren(!)
      activated = LoginHelper.getInstance().activate(activationMD5);
    }

    String userParam = "" + (String) Executions.getCurrent().getParameter("user");

    if (userParam.length() > 0 && userParam.equals("null") == false)
      username = userParam;

    //Smtp smtp = new Smtp();
    //smtp.sendMail("robert@muetzner.de", "Robert Mützner", "TODO-Anwendung", "Hallo Robert Mützner, das ist ein Test!");
    //createUser();
  }

  public String getVersion()
  {
    ResourceBundle rb = ResourceBundle.getBundle("version");
    //return rb.getString("application.version") + "." + rb.getString("application.buildnumber");
    return rb.getString("application.version");
  }

  /*private void createUser()
   {
   Session hb_session = HibernateUtil.getSessionFactory().openSession();
   org.hibernate.Transaction tx = hb_session.beginTransaction();

   // 1. Person anlegen
   // 2. User anlegen
   // 3. Aktivierungsmail schicken

   if (logger.isDebugEnabled())
   logger.debug("Neuen Benutzer speichern");
   TermUser user = new TermUser();
   user.setIsAdmin(true);
   user.setEnabled(false);
   user.setActivated(false);
   user.setPassw("");
   user.setName("muetzner");
   //user.setUser(_zclass)

   String md5 = MD5.getMD5(new java.util.Date().toString() + user.getUser());
   user.setActivationMd5(md5);

   String neuesPW = Password.generateRandomPassword(8);
   String salt = Password.generateRandomSalt();
   user.setPass(Password.getSaltedPassword(neuesPW, salt, user.getUser()));
   user.setSalt(salt);

   Person person = new Person();
   person.setName("Mützner");
   person.setEmail("robert.muetzner@fh-dortmund.de");

   //person.setUsers(new HashSet<User>());
   //person.getUsers().add(user);
   //hb_session.save(person);

   user.setPersons(new HashSet<Person>());
   user.getPersons().add(person);
   //user.setPerson(person);
   hb_session.save(user);

   if (logger.isDebugEnabled())
   logger.debug("Neue Person-ID: " + person.getId());

   String mailResponse = Mail.sendMailNewUser(person.getName(), person.getName(), neuesPW,
   md5, person.getEmail());

   logger.debug("Mail: " + mailResponse);


   tx.commit();
   HibernateUtil.getSessionFactory().close();
   }*/
  public void afterCompose()
  {
    
    labelVersion = (Label) getFellow("labelVersion");
    labelVersion.setValue(de.fhdo.db.DBSysParam.instance().getStringValue("systemType", null, null) + " Verwaltungsumgebung");
      
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

    //if (DortmundHelper.getInstance().isFhDortmund())
    if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("fhdo"))
    {
      // Eigene Buttons (Image) für die FH Dortmund (Login Server)
      ((Button) getFellow("loginButton")).setImage("/rsc/img/symbols/LoginServer_24x24.png");
    }

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
   * Im Anmeldefenster wurde "Return" gedrückt
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
   * Führt einen Loginversuch mit den Werten aus den Textfeldern durch. Gibt
   * eine Fehlermeldung bei nicht korrekten Anmeldedaten aus.
   *
   */
  public void login()
  {
    // Daten aus dem Formular auslesen
    Textbox tbUser = (Textbox) getFellow("tfUser");
    Textbox tbPass = (Textbox) getFellow("tfPassword");

    if (logger.isDebugEnabled())
      logger.debug("Login wird durchgefuehrt...");

    if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("userpass"))
    {
      LoginHelper.getInstance().reset();

      // Login-Daten ueberpruefen
      boolean loginCorrect = LoginHelper.getInstance().loginUserPass(tbUser.getValue(), tbPass.getValue());
      
      if (logger.isDebugEnabled())
        logger.debug("loginCorrect: " + loginCorrect);

      if (loginCorrect)
      {
        // Hauptseite aufrufen
        CookieHelper.removeCookie("show_captcha");
        CookieHelper.setCookie("username", username);

        Clients.showBusy("Login erfolgreich\n\nTermAdmin wird geladen...");

        Executions.sendRedirect("/gui/admin/admin.zul");
      }
      else
      {
        // Login falsch
        loginCount++;

        if (loginCount > 4)
        {
          CookieHelper.setCookie("show_captcha", "1");
          showCaptcha();
        }

        showRow("warningRow", true);

        Label label = (Label) getFellow("temp");
        label.setValue("Versuch " + loginCount);
      }
    }
    /*else if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("fhdo"))
    {
      // FH Dortmund verwendet eigenen Login
      try
      {
        de.fhdo.dortmund.LoginHelper.getInstance().reset();

        // Login-Daten ueberpruefen
        boolean loginCorrect = de.fhdo.dortmund.LoginHelper.getInstance().login(tbUser.getValue(), tbPass.getValue());

        if (logger.isDebugEnabled())
          logger.debug("loginCorrect: " + loginCorrect);

        if (loginCorrect)
        {
          // Hauptseite aufrufen
          CookieHelper.removeCookie("show_captcha");
          CookieHelper.setCookie("username", username);

          Clients.showBusy("Login erfolgreich\n\nTermAdmin wird geladen...");
          Executions.sendRedirect("/gui/admin/admin.zul");
          //Executions.getCurrent().sendRedirect("../../TermBrowser/gui/main/main.zul?" + "p1=" + userAndPseudEnc);
        }
        else
        {
          // Login falsch
          loginCount++;

          if (loginCount > 2)
          {
            CookieHelper.setCookie("show_captcha", "1");
            showCaptcha();
          }

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
    }*/
    else
    {
      try
      {
        LoginHelper.getInstance().reset();
        boolean termconsumer = false;

        // Login-Daten ueberpruefen
        //AT: gen. pseudonym
        String pseudonym = UUID.randomUUID().toString();

        boolean loginCorrect = false;
        boolean pseudonymStored = LoginHelper.getInstance().storePseudonym(tbUser.getValue(), pseudonym);
        String res = LoginHelper.getInstance().login(tbUser.getValue(), tbPass.getValue());

        String[] splitted = res.split(";");
        if (splitted[0].equals("true"))
        {
          loginCorrect = true;
        }
        else if (splitted[0].equals("false"))
        {
          loginCorrect = false;
        }
        else
        {
          termconsumer = true;
        }
        
        if (logger.isDebugEnabled())
        {
          logger.debug("loginCorrect: " + loginCorrect);
          logger.debug("pseudonymStored: " + pseudonymStored);
        }

        if (loginCorrect)
        {
          String userAndPseudEnc = DES.encrypt(tbUser.getValue() + ";" + pseudonym + ";" + splitted[1] + ";" + splitted[2] + ";" + splitted[3] + ";" + splitted[4] + ";"+ splitted[5]);

          // Hauptseite aufrufen
          CookieHelper.removeCookie("show_captcha");
          CookieHelper.setCookie("username", username);

          Clients.showBusy("Login erfolgreich\n\nTermBrowser wird geladen...");

          //Adapted AT:ZK POST??      
          Executions.getCurrent().sendRedirect("../../TermBrowser/gui/main/main.zul?" + "p1=" + userAndPseudEnc);
        }
        else
        {
          if (!termconsumer)
          {
            // Login falsch
            loginCount++;

            if (loginCount > 4)
            {
              CookieHelper.setCookie("show_captcha", "1");
              showCaptcha();
            }

            showRow("warningRow", true);

            Label label = (Label) getFellow("temp");
            label.setValue("Versuch " + loginCount);
          }
          else
          {

            Label label = (Label) getFellow("temp");
            label.setValue("Login mit user termconsumer nicht erlaubt!");
          }
        }

      }
      catch (Exception e)
      {
        logger.error(e.getLocalizedMessage());
        e.printStackTrace();
      }
    }

  }

  /**
   * Sendet das Passwort an die angegebene Email-Adresse
   */
  public void sendPassword() throws InterruptedException
  {
    if (Messagebox.show("Möchten Sie ein neues, zufällig generiertes Passwort an Ihre Email-Adresse geschickt bekommen?",
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
    SessionHelper.setValue("captcha_correct", false);
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
