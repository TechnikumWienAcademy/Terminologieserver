/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.communication.Mail;
import de.fhdo.helper.CollabUserHelper;
import de.fhdo.helper.CollabUserRoleHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericListRowType;
import de.fhdo.terminologie.ws.idp.userManagement.Collaborationuser;
import de.fhdo.terminologie.ws.idp.userManagement.GetTermUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.GetUserListResponse;
import de.fhdo.terminologie.ws.idp.userManagement.LoginType;
import de.fhdo.terminologie.ws.idp.userManagement.Organisation;
import de.fhdo.terminologie.ws.idp.userManagement.Role;
import de.fhdo.terminologie.ws.idp.userManagement.SaveCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.SaveCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.userManagement.Status;
import de.fhdo.terminologie.ws.idp.userManagement.TermUser;
import de.fhdo.terminologie.ws.idp.userManagement.UserManagement;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class BenutzerDetails extends Window implements AfterCompose, EventListener<Event>
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private Collaborationuser user;
    private TermUser userTerm;
    //private Map args;
    private boolean newEntry = false;
    private IUpdateModal updateListInterface;
    private Combobox cbUserRole;

    public BenutzerDetails()
    {
        Map args = Executions.getCurrent().getArg();
        long userId = 0;
        try
        {
            userId = Long.parseLong(args.get("user_id").toString());
            user = (Collaborationuser) args.get("user");
        }
        catch (Exception ex)
        {
        }

        if (userId > 0)
        {
            UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

            GetTermUserRequestType parameter = new GetTermUserRequestType();
            parameter.setLoginType(new LoginType());
            parameter.getLoginType().setSessionID(SessionHelper.getSessionAttributeByName("session_id").toString());

            GetUserListResponse.Return response = port.getUserList(parameter);

            List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

            if (response.getReturnInfos().getStatus().equals(Status.OK))
            {
                List<de.fhdo.terminologie.ws.idp.userManagement.TermUser> personList = response.getUserList();

                for (int i = 0; i < personList.size(); ++i)
                {
                    de.fhdo.terminologie.ws.idp.userManagement.TermUser termUser = personList.get(i);
                    if(termUser.getName().equals(user.getUsername()+"_tadm"))
                    {
                        userTerm = termUser;
                    }
                }
            }
        }

        if (user == null)
        {
            user = new Collaborationuser();
            user.setEnabled(true);
            user.setHidden(false);
            user.setDeleted(false);
            user.setSendMail(true);
            user.getRoles().clear();
            user.getRoles().add(new Role());
            user.setOrganisation(new Organisation());

            //TermUser
            userTerm = new TermUser();
            userTerm.setIsAdmin(true);

            newEntry = true;
        }
    }

    public void afterCompose()
    {
        ((Textbox) getFellow("tb_Benutzername")).setReadonly(!newEntry);
        ((Checkbox) getFellow("cb_aktiv")).setChecked(!newEntry);
        ((Checkbox) getFellow("cb_aktiv")).setDisabled(newEntry);
        cbUserRole = (Combobox) getFellow("cb_UserRole");
        cbUserRole.setModel(CollabUserRoleHelper.getListModelList());
        cbUserRole.addEventListener("onInitRenderLater", this);
    }

    public void onEvent(Event event) throws Exception
    {

        if (user == null
                || user.getRoles() == null
                || user.getRoles().isEmpty() || user.getRoles().iterator().next() == null
                || user.getRoles().iterator().next().getId() == null)
        {
            return;
        }

        Iterator<Comboitem> it = cbUserRole.getItems().iterator();
        while (it.hasNext())
        {
            Comboitem ci = it.next();
            if (user.getRoles().iterator().next().getId().compareTo(CollabUserRoleHelper.getCollabUserRoleIdByName(ci.getLabel())) == 0)
            {
                cbUserRole.setSelectedItem(ci);
            }
        }
    }

    public void onOkClicked()
    {
        String mailResponse = "";
        String neuesPW = Password.generateRandomPassword(8);
        String benutzerName = "";
        String activationMD5 = "";
        String email = "";

        // speichern mit Hibernate
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Daten speichern");
            }

            try
            {
                if (newEntry)
                {
                    // Pflichtfelder prüfen
                    if (user.getUsername() == null || user.getUsername().length() == 0
                            || user.getEmail() == null || user.getEmail().length() == 0 || user.getEmail().contains("@") == false
                            || cbUserRole.getSelectedItem() == null || user.getOrganisation().getOrganisation() == null || user.getOrganisation().getOrganisation().equals(""))
                    {
                        Messagebox.show("Sie müssen einen Benutzernamen, eine gültige Email-Adresse, Benutzerrolle und Organisation angeben.");
                        return;
                    }
                    
                    if(user.getUsername().contains("_"))
                    {
                        Messagebox.show("Der Benutername darf das Zeichen '_' (Unterstrich) nicht beinhalten.");
                        return;
                    }
                    
                    String usernamePostfix = "";
                    if(cbUserRole.getSelectedItem().getLabel().equals("Terminologieadministrator"))
                    {
                        usernamePostfix = "_tadm";
                        userTerm.setIsAdmin(true);
                    }
                    else if(cbUserRole.getSelectedItem().getLabel().equals("Inhaltsverwalter"))
                    {
                        usernamePostfix = "_iv";
                        userTerm.setIsAdmin(false);
                    }
                    else if(cbUserRole.getSelectedItem().getLabel().equals("Diskussionsteilnehmer"))
                    {
                        usernamePostfix = "_dtn";
                        userTerm.setIsAdmin(false);
                    }
                    else if(cbUserRole.getSelectedItem().getLabel().equals("Rezensent"))
                    {
                        usernamePostfix = "_rzt";
                        userTerm.setIsAdmin(false);
                    }
                    else 
                    {
                        Messagebox.show("Die angegebene Rolle kann nicht verarbeitet werden.");
                        return;
                    }
                    
                    userTerm.setEnabled(false);
                    userTerm.setActivated(false);
                    userTerm.setActivated(user.isActivated());
                    userTerm.setUserName(user.getFirstName() + " " + user.getName());
                    
                    user.setFirstName(((Textbox) getFellow("tb_vorname")).getValue());
                    user.setName(((Textbox) getFellow("tb_nachname")).getValue());
                    user.setEmail(((Textbox) getFellow("tb_Email")).getValue());
                    user.setUsername(((Textbox) getFellow("tb_Benutzername")).getValue());
                    user.setActivated(false);
                    user.setEnabled(false);
                    
                    userTerm.setEmail(user.getEmail());
                    userTerm.setName(user.getUsername() + usernamePostfix);
                    user.setSendMail(((Checkbox) getFellow("cb_sendMail")).isChecked());
                    
                    UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

                    SaveCollaborationUserRequestType parameter = new SaveCollaborationUserRequestType();
                    parameter.setLoginType(new LoginType());
                    parameter.getLoginType().setSessionID(SessionHelper.getSessionAttributeByName("session_id").toString());
                    parameter.setTermuser(userTerm);
                    parameter.setUser(user);
                    parameter.setNewEntry(true);
                    parameter.setRole(new Role());
                    parameter.getRole().setId(CollabUserRoleHelper.getCollabUserRoleIdByName(cbUserRole.getSelectedItem().getLabel()));

                    SaveCollaborationUserResponseType response = port.saveCollaborationUser(parameter);
                    
                    if(response.getReturnInfos().getStatus().equals(Status.OK))
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                        this.setVisible(false);
                        this.detach();

                        if (updateListInterface != null)
                        {
                            updateListInterface.update(user, !newEntry);
                        }
                        CollabUserHelper.reloadModel();
                    }
                    else
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }

                    
                }
                else
                {
                    
                    userTerm.setEnabled(user.isEnabled());
                    userTerm.setActivated(user.isActivated());
                    userTerm.setUserName(user.getFirstName() + " " + user.getName());
                    user.setFirstName(((Textbox) getFellow("tb_vorname")).getValue());
                    user.setName(((Textbox) getFellow("tb_nachname")).getValue());
                    user.setEmail(((Textbox) getFellow("tb_Email")).getValue());
                    userTerm.setEmail(user.getEmail());
                    
                    UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

                    SaveCollaborationUserRequestType parameter = new SaveCollaborationUserRequestType();
                    parameter.setLoginType(new LoginType());
                    parameter.getLoginType().setSessionID(SessionHelper.getSessionAttributeByName("session_id").toString());
                    parameter.setTermuser(userTerm);
                    parameter.setUser(user);
                    parameter.setNewEntry(false);

                    SaveCollaborationUserResponseType response = port.saveCollaborationUser(parameter);
                    
                    if(response.getReturnInfos().getStatus().equals(Status.OK))
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                        this.setVisible(false);
                        this.detach();

                        if (updateListInterface != null)
                        {
                            updateListInterface.update(user, !newEntry);
                        }
                        CollabUserHelper.reloadModel();
                    }
                    else
                    {
                        Messagebox.show(response.getReturnInfos().getMessage());
                    }
                    
                }

            }
            catch (Exception e)
            {
                logger.error("Fehler in BenutzerDetails.java (onOkClicked()): " + e.getMessage());
            }
        }
        catch (Exception e)
        {
            // Fehlermeldung ausgeben
            logger.error("Fehler in BenutzerDetails.java: " + e.getMessage());
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
}
