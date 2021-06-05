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
import de.fhdo.Definitions;
import de.fhdo.db.DBSysParamCollab;
import de.fhdo.helper.AssertionHelper;
import de.fhdo.helper.LoginHelperTest;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SAMLHelper;
import de.fhdo.interfaces.IUpdate;
import java.util.ResourceBundle;
import org.apache.log4j.Level;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

public abstract class AbstractLogin extends Window implements org.zkoss.zk.ui.ext.AfterCompose, IUpdate
{

    private static final long serialVersionUID = 1L;
    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    //private boolean captchaShown = false;
    long standardId = 0;
    private Label labelVersion;
    private final PropertiesHelper ph;
    private final LoginHelperTest lh;
    private final String userSessAttr;
    private final String assertionSessAttr;

    public AbstractLogin(PropertiesHelper ph, LoginHelperTest lh, String userSessAttr, String assertionSessAttr)
    {
        this.ph = ph;
        this.lh = lh;
        this.userSessAttr = userSessAttr;
        this.assertionSessAttr = assertionSessAttr;
    }

    public void backToTermBrowser()
    {
        String weblink = DBSysParamCollab.instance().getStringValue("weblink", null, null);
        Executions.sendRedirect(weblink);
    }

    public String getVersion()
    {
        ResourceBundle rb = ResourceBundle.getBundle("version");
        return rb.getString("application.version");

    }

    @Override
    public void afterCompose()
    {
        labelVersion = (Label) getFellow("labelVersion");
        labelVersion.setValue("Verwaltungsbereich");

        // Fokus auf Eingabefeld setzen
        Session session = Sessions.getCurrent();
        String username = (String) session.getAttribute("username");
        Textbox tb;
        if (username != null)
        {
            tb = (Textbox) getFellow("tfPassword");
        }
        else
        {
            tb = (Textbox) getFellow("tfUser");
        }

        if (tb != null)
        {
            tb.setFocus(true);
        }

        String s_captcha = (String) Sessions.getCurrent().getAttribute("show_captcha");
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
        {
            logger.debug("Enter gedrueckt!");
        }
        Button b = (Button) getFellow("loginButton");
        b.setDisabled(true);
        login();
        b.setDisabled(false);
    }

    /**
     * Führt einen Loginversuch mit den Werten aus den Textfeldern durch. Gibt
     * eine Fehlermeldung bei nicht korrekten Anmeldedaten aus. *
     */
    public void login()
    {
        // Daten aus dem Formular auslesen
        Textbox tbUser = (Textbox) getFellow("tfUser");
        Textbox tbPass = (Textbox) getFellow("tfPassword");

        if (logger.isDebugEnabled())
        {
            logger.debug("Login wird durchgefuehrt...");
        }

        lh.reset();
        // AbstractLogin-Daten ueberpruefen
        boolean loginCorrect = lh.loginUserPass(tbUser.getValue(), tbPass.getValue());
        logger.info("Login correct: " + loginCorrect);
        Session s = Sessions.getCurrent();
        if (loginCorrect)
        {
            lh.sendUserBack();
        }
        else
        {
            // AbstractLogin falsch
            Integer i = (Integer) s.getAttribute("loginCount");
            i = i == null ? 1 : i + 1;
            s.setAttribute("loginCount", i);

            if (i > 4)
            {
                s.setAttribute("show_captcha", "1");
                showCaptcha();
            }

            showRow("warningRow", true);

            Label label = (Label) getFellow("temp");
            label.setValue("Versuch " + i);
        }
    }

    /**
     * Sendet das Passwort an die angegebene Email-Adresse
     */
    public void sendPassword() throws InterruptedException
    {
        logger.info("send password");
        logger.info((String) Sessions.getCurrent().getAttribute(userSessAttr));
        logger.info(((Textbox) getFellow("tfUser")).getValue());
        Messagebox.show("Möchten Sie ein neues, zufällig generiertes Passwort an Ihre Email-Adresse geschickt bekommen?",
                "Neues Passwort", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>()
                {
            @Override
            public void onEvent(Event t) throws Exception
            {
                logger.info(t.getName());
                if(t.getName().equals(Messagebox.ON_OK))
                {
                    Textbox tbUser = (Textbox) getFellow("tfUser");
                    logger.info("message box yes");
                    logger.setLevel(Level.DEBUG);
                    String username = tbUser.getValue();
                    logger.info(username);
                    if(!username.equals(""))
                    {
                        boolean erfolg = lh.resendPassword(true, username);

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
                    else
                    {
                        Messagebox.show("Bitte geben Sie einen Benutzer an.",
                                    "Neues Passwort", Messagebox.OK, Messagebox.EXCLAMATION);
                    }
                }
            }
        });
    }

    private void showRow(String RowID, boolean Visible)
    {
        Row row = (Row) getFellow(RowID);
        row.setVisible(Visible);
    }

    private void showCaptcha()
    {
        Sessions.getCurrent().setAttribute("captcha_correct", false);
    }

    @Override
    public void update(Object o)
    {
        login();
    }

}
