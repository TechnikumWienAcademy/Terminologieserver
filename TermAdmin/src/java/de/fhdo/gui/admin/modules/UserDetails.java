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
package de.fhdo.gui.admin.modules;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.communication.Mail;
import de.fhdo.db.HibernateUtil;
import de.fhdo.helper.CollabUserHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.terminologie.ws.idp.userManagement.LoginType;
import de.fhdo.terminologie.ws.idp.userManagement.Status;
import de.fhdo.terminologie.ws.idp.userManagement.TermUser;
import de.fhdo.terminologie.ws.idp.userManagement.SaveTermUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.SaveTermUserResponseType;
import de.fhdo.terminologie.ws.idp.userManagement.UserManagement;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class UserDetails extends Window implements AfterCompose, EventListener<Event>
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private TermUser user;
    //private Map args;
    private boolean newEntry = false;
    private IUpdateModal updateListInterface;
    private Textbox tb_Email, tb_Name;
    private Combobox cb_Benutzername;
    private Row rComboUsername;
    private Row rUsername;
    private Session hb_sessionS;

    public void onEvent(Event event) throws Exception
    {

        if (user == null
                || user.getId() == null)
        {
            return;
        }

        Iterator<Comboitem> it = cb_Benutzername.getItems().iterator();
        while (it.hasNext())
        {
            Comboitem ci = it.next();
            if (user.getId().compareTo(CollabUserHelper.getCollabUsernameIdByName(ci.getLabel())) == 0)
            {
                cb_Benutzername.setSelectedItem(ci);
            }
        }
    }

    public UserDetails()
    {
        Map args = Executions.getCurrent().getArg();
        long userId = 0;
        try
        {
            userId = Long.parseLong(args.get("user_id").toString());
            user = (TermUser) args.get("user");
        }
        catch (Exception ex)
        {
        }

        if (user == null)
        {
            user = new TermUser();
            user.setIsAdmin(true);
            newEntry = true;
        }
    }

    public void afterCompose()
    {
        ((Textbox) getFellow("tb_Benutzername")).setReadonly(!newEntry);
        ((Checkbox) getFellow("cb_aktiv")).setDisabled(newEntry);
        ((Checkbox) getFellow("cb_MailAktiv")).setDisabled(newEntry);
        tb_Email = (Textbox) getFellow("tb_Email");
        tb_Name = (Textbox) getFellow("tb_Name");
        cb_Benutzername = (Combobox) getFellow("cb_Benutzername");
        cb_Benutzername.setModel(CollabUserHelper.getListModelList());
        cb_Benutzername.addEventListener("onInitRenderLater", this);

        rComboUsername = (Row) getFellow("rComboUsername");
        rUsername = (Row) getFellow("rUsername");

        if (newEntry)
        {
            rComboUsername.setVisible(true);
            rUsername.setVisible(false);
        }
        else
        {
            rComboUsername.setVisible(false);
            rUsername.setVisible(true);
        }

        if (cb_Benutzername.getModel().getSize() == 0 && newEntry)
        {

            ((Button) getFellow("b_Ok")).setVisible(false);
            cb_Benutzername.setDisabled(true);
            tb_Email.setDisabled(true);
            tb_Name.setDisabled(true);
            ((Checkbox) getFellow("cb_isAdmin")).setDisabled(true);
        }
    }

    public void onOkClicked()
    {
        // speichern mit Hibernate

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Daten speichern");
            }

            String password = "";

            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try
            {
                if (newEntry)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Neuer Eintrag, fuege hinzu!");
                    }

                    // Passwort erstellen + Activation MD5 erstellen
                    user.setName(cb_Benutzername.getSelectedItem().getLabel());
                    user.setUserName(tb_Name.getText());
                    user.setEmail(tb_Email.getText());
                    user.setActivated(false);
                    user.setEnabled(false);

                    UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

                    SaveTermUserRequestType parameter = new SaveTermUserRequestType();
                    parameter.setLoginType(new LoginType());
                    parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());
                    parameter.setTermUser(user);
                    parameter.setNewEntry(true);

                    SaveTermUserResponseType response = port.saveTermUser(parameter);

                    if (response.getReturnInfos().getStatus().equals(Status.OK))
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }
                    else
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }
                }
                else
                {
                    UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

                    SaveTermUserRequestType parameter = new SaveTermUserRequestType();
                    parameter.setLoginType(new LoginType());
                    parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());
                    parameter.setTermUser(user);
                    parameter.setNewEntry(false);

                    SaveTermUserResponseType response = port.saveTermUser(parameter);

                    if (response.getReturnInfos().getStatus().equals(Status.OK))
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }
                    else
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }
                }

                // Erfolg, Email schicken (!)
                if (newEntry)
                {

                    String s = Mail.sendMailNewUser(user.getName(), password, user.getActivationMd5(), user.getEmail());

                    if (s.length() == 0)
                    {
                        hb_session.getTransaction().commit();
                        Messagebox.show("Benutzer wurde erfolgreich angelegt und Aktivierungs-Email verschickt.");
                    }
                    else
                    {
                        Messagebox.show("Fehler beim Anlegen eines Benutzers: " + s);
                        hb_session.getTransaction().rollback();
                    }
                }
                else
                {
                    hb_session.getTransaction().commit();
                }
                password = "";

            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                logger.error("Fehler in DomainDetails.java (onOkClicked()): " + e.getMessage());
            }

            hb_session.close();

            //hb_session.close();
            this.setVisible(false);
            this.detach();

            if (updateListInterface != null)
            {
                updateListInterface.update(user, !newEntry);
            }

            CollabUserHelper.reloadModel();
            cb_Benutzername.setModel(CollabUserHelper.getListModelList());
        }
        catch (Exception e)
        {
            // Fehlermeldung ausgeben
            logger.error("Fehler in DomainDetails.java: " + e.getMessage());
        }
        finally
        {
            if (hb_sessionS != null)
            {
                hb_sessionS.close();
            }
        }

    }

    public void onCancelClicked()
    {
        this.setVisible(false);
        this.detach();
        if (hb_sessionS != null)
        {
            hb_sessionS.close();
        }
    }

    /**
     * @return the user
     */
    public TermUser getUser()
    {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(TermUser user)
    {
        this.user = user;
    }

    /**
     * @param updateListInterface the updateListInterface to set
     */
    public void setUpdateListInterface(IUpdateModal updateListInterface)
    {
        this.updateListInterface = updateListInterface;
    }

    public void onSelectCombo()
    {

        Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();

        try
        {
            Long userId = CollabUserHelper.getCollabUsernameIdByName(cb_Benutzername.getSelectedItem().getLabel());
            Collaborationuser userL = (Collaborationuser) hb_session_kollab.get(Collaborationuser.class, userId);

            tb_Email.setText(userL.getEmail());
            tb_Name.setText(userL.getFirstName() + " " + userL.getName());
        }
        catch (Exception e)
        {
            logger.error("Fehler in UserDetails.java (onSelectCombo()): " + e.getMessage());
        }
        finally
        {
            hb_session_kollab.close();
        }
    }
}
