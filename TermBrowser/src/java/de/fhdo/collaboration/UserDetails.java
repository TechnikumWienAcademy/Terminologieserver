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

import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.idp.usermanagement.GetCollaborationUserListResponse;
import de.fhdo.terminologie.ws.idp.usermanagement.GetCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.usermanagement.GetTermUserRequestType;
import de.fhdo.terminologie.ws.idp.usermanagement.GetUserListResponse;
import de.fhdo.terminologie.ws.idp.usermanagement.LoginType;
import de.fhdo.terminologie.ws.idp.usermanagement.SaveCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.usermanagement.SaveCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.usermanagement.Status;
import de.fhdo.terminologie.ws.idp.usermanagement.UserManagement;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.idp.termserver.fhdo.de.Collaborationuser;
import types.idp.termserver.fhdo.de.TermUser;

/**
 *
 * @author Robert Mützner
 */
public class UserDetails extends Window implements AfterCompose, IUpdate
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private Collaborationuser user;
    private TermUser userTerm;

    //private Map args;
    private IUpdateModal updateListInterface;

    public UserDetails()
    {
        Map args = Executions.getCurrent().getArg();
        long userId = 0;
        long termuserId = 0;
        try
        {
            userId = Long.parseLong(args.get("user_id").toString());
            termuserId = Long.parseLong(args.get("termuser_id").toString());
            //user = (TermUser) args.get("user");
        }
        catch (Exception ex)
        {
        }

        if (userId > 0 && termuserId > 0)
        {
            UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

            GetCollaborationUserRequestType parameter = new GetCollaborationUserRequestType();
            parameter.setLoginType(new LoginType());
            parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());

            GetCollaborationUserListResponse.Return response = port.getCollaborationUserList(parameter);

            if (response.getReturnInfos().getStatus().equals(Status.OK))
            {
                List<Collaborationuser> personList = response.getUserList();

                for (int i = 0; i < personList.size(); ++i)
                {
                    if (personList.get(i).getId() == userId)
                    {
                        user = personList.get(i);
                    }
                }
            }

            GetTermUserRequestType parameter_term = new GetTermUserRequestType();
            parameter_term.setLoginType(new LoginType());
            parameter_term.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());

            GetUserListResponse.Return response_term = port.getUserList(parameter_term);

            if (response_term.getReturnInfos().getStatus().equals(Status.OK))
            {
                List<TermUser> personList = response_term.getUserList();

                for (int i = 0; i < personList.size(); ++i)
                {
                    if (personList.get(i).getId() == termuserId)
                    {
                        userTerm = personList.get(i);
                    }
                }
            }
        }

        if (user == null || userTerm == null)
        {
            Messagebox.show("Benutzer nicht vorhanden!", "Achtung", Messagebox.OK, Messagebox.INFORMATION);
            this.setVisible(false);
            this.detach();
        }
    }

    public void afterCompose()
    {

    }

    public void changePassword()
    {
        try
        {
            logger.debug("erstelle Fenster...");

            Window win = (Window) Executions.createComponents(
                    "/collaboration/passwordDialog.zul", null, null);

            ((PasswordDetails) win).setUpdateListInterface(this);

            logger.debug("öffne Fenster...");
            win.doModal();
        }
        catch (Exception ex)
        {
            logger.error("Fehler in Klasse '" + this.getClass().getName()
                    + "': " + ex.getMessage());
        }
    }

    public void onOkClicked()
    {

        // speichern mit Hibernate
        try
        {
            UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

            SaveCollaborationUserRequestType parameter = new SaveCollaborationUserRequestType();
            parameter.setLoginType(new LoginType());
            parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());
            userTerm.setEmail(user.getEmail());
            parameter.setTermuser(userTerm);
            parameter.setUser(user);
            parameter.setNewEntry(false);

            SaveCollaborationUserResponseType response = port.saveCollaborationUser(parameter);

            if (response.getReturnInfos().getStatus().equals(Status.OK))
            {
                Messagebox.show("Benutzer erfolgreich aktualisiert.");
            }
            else
            {
                Messagebox.show(response.getReturnInfos().getMessage());
            }

            this.setVisible(false);
            this.detach();

            if (updateListInterface != null)
            {
                updateListInterface.update(user, true);
            }

        }
        catch (Exception e)
        {
            // Fehlermeldung ausgeben
            LoggingOutput.outputException(e, this);

        }
    }

    public void onCancelClicked()
    {
        this.setVisible(false);
        this.detach();
    }

    /**
     * @return the user
     */
    public Collaborationuser getUser()
    {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(Collaborationuser user)
    {
        this.user = user;
    }

    public TermUser getUserTerm()
    {
        return userTerm;
    }

    public void setUserTerm(TermUser userTerm)
    {
        this.userTerm = userTerm;
    }

    /**
     * @param updateListInterface the updateListInterface to set
     */
    public void setUpdateListInterface(IUpdateModal updateListInterface)
    {
        this.updateListInterface = updateListInterface;
    }

    public void update(Object o)
    {
        if (o instanceof UserPackage)
        {
            user.setPassword((((UserPackage) o).getUser()).getPassword());
            //userTerm.setPassw((((UserPackage)o).getUserTerm()).getPassw());
            user.setSalt((((UserPackage) o).getUser()).getSalt());
            //userTerm.setSalt((((UserPackage)o).getUserTerm()).getSalt());
        }

    }
}
