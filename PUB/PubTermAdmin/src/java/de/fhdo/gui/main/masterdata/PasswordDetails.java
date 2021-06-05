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

package de.fhdo.gui.main.masterdata;


import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.helper.Password;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class PasswordDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdate updateListInterface;

  public PasswordDetails()
  {
    //Map args;

    try
    {
      logger.debug("OrganisationPersonDetails() - Konstruktor");
      //args = Executions.getCurrent().getArg();

    }
    catch (Exception e)
    {
      logger.error(e.getLocalizedMessage());
    }

    /*try
    {
    patientID = Long.parseLong(args.get("patientID").toString());
    logger.debug("Patient-ID: " + patientID);*/
  }

  /**
   * Im Anmeldefenster wurde "Return" gedrückt
   *
   * @param event
   */
  public void onOkPressed(KeyEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("Enter gedrueckt!");

    Button b = (Button) getFellow("okButton");
    b.setDisabled(true);

    onOkClicked();

    b.setDisabled(false);
  }

  public void onOkClicked()
  {

    // speichern mit Hibernate
    try
    {
      Textbox tb1 = (Textbox)getFellow("pw1");
      Textbox tb2 = (Textbox)getFellow("pw2");

      if(tb1.getValue().equals(tb2.getValue()) == false)
      {
        Messagebox.show("Das eingegebene Passwort stimmt nicht überein!", "Fehler", Messagebox.OK, Messagebox.ERROR);
        return;
      }


      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      Textbox tb = (Textbox)getFellow("pwAlt");
      //String pwAlt = tb.getText();

      // Altes Passwort überprüfen
      TermUser user_init = (TermUser) hb_session.get(TermUser.class, SessionHelper.getUserID());

      String password = Password.getSaltedPassword(tb.getValue(), user_init.getSalt(), user_init.getName());

      org.hibernate.Query q = hb_session.createQuery("from TermUser where id=" + SessionHelper.getUserID()+ " AND passw='" + password + "'");

      List<TermUser> list = (List<TermUser>) q.list();

      if (list.size() == 1)
      {
        TermUser user = list.get(0);

        // Passwort aktualisieren
        String salt = Password.generateRandomSalt();
        String passwordNeuSalted = Password.getSaltedPassword(tb1.getValue(), salt, user_init.getName());
        user.setSalt(salt);
        user.setPassw(passwordNeuSalted);

        hb_session.update(user);

        if(updateListInterface != null)
          updateListInterface.update(user);
      }
      else
      {
        Messagebox.show("Das alte Passwort ist nicht korrekt!", "Fehler", Messagebox.OK, Messagebox.ERROR);
        hb_session.close();
        return;
      }


      hb_session.getTransaction().commit();
      hb_session.close();

      Messagebox.show("Passwort erfolgreich geändert.", "Passwort ändern", Messagebox.OK, Messagebox.INFORMATION);

      tb1.setValue("");
      tb2.setValue("");
      tb.setValue("");

      this.setVisible(false);

      this.detach();
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in onOkClicked(): " + e.getMessage());
    }

    //de.fhdo.gui.patientrecord.modules.masterdata.PasswordDetails cannot be cast to de.fhdo.gui.patientrecord.modules.masterdata.Mast

    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();

    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }


  public void afterCompose()
  {
    //throw new UnsupportedOperationException("Not supported yet.");

    //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdate updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }


}

