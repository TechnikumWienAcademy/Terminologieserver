/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.idp.userManagement.Collaborationuser;
import de.fhdo.terminologie.ws.idp.userManagement.DeleteUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.DeleteUserResponseType;
import de.fhdo.terminologie.ws.idp.userManagement.GetCollaborationUserListResponse;
import de.fhdo.terminologie.ws.idp.userManagement.GetCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.LoginType;
import de.fhdo.terminologie.ws.idp.userManagement.Role;
import de.fhdo.terminologie.ws.idp.userManagement.Status;
import de.fhdo.terminologie.ws.idp.userManagement.TermUser;
import de.fhdo.terminologie.ws.idp.userManagement.UserManagement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Benutzer extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    GenericList genericList;

    public Benutzer()
    {

    }

    public void afterCompose()
    {
        initList();
    }

    private GenericListRowType createRowFromBenutzer(Collaborationuser user)
    {
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[6];
        cells[0] = new GenericListCellType(user.getUsername(), false, "");
        cells[1] = new GenericListCellType(user.isEnabled() != null ? user.isEnabled() : false, false, "");
        cells[2] = new GenericListCellType(user.getName(), false, "");
        cells[3] = new GenericListCellType(user.getFirstName(), false, "");
        cells[4] = new GenericListCellType(user.getEmail(), false, "");
        cells[5] = new GenericListCellType(user.isActivated() != null ? user.isActivated() : false, false, "");

        row.setData(user);
        row.setCells(cells);

        return row;
    }

    private void initList()
    {
        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Benutzername", 200, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Aktiv", 60, "", true, "boolean", true, true, false, true));
        header.add(new GenericListHeaderType("Name", 150, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Vorname", 150, "", true, "String", true, true, false, false));
        //header.add(new GenericListHeaderType("Admin", 50, "", true, "boolean", true, true, false, true));
        header.add(new GenericListHeaderType("Email", 300, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Mail-Aktiviert", 100, "", true, "boolean", true, true, false, true));

        UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

        GetCollaborationUserRequestType parameter = new GetCollaborationUserRequestType();
        parameter.setLoginType(new LoginType());
        parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());

        GetCollaborationUserListResponse.Return response = port.getCollaborationUserList(parameter);

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

        if (response.getReturnInfos().getStatus().equals(Status.OK))
        {
            List<Collaborationuser> personList = response.getUserList();

            for (int i = 0; i < personList.size(); ++i)
            {
                Collaborationuser user = personList.get(i);
                GenericListRowType row = createRowFromBenutzer(user);

                dataList.add(row);
            }
        }

        // Liste initialisieren
        Include inc = (Include) getFellow("incList");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;

        //genericList.setUserDefinedId("1");
        genericList.setListActions(this);
        genericList.setButton_new(true);
        genericList.setButton_edit(true);
        genericList.setButton_delete(true);
        genericList.setListHeader(header);
        genericList.setDataList(dataList);

        ((Button) genericList.getFellow("buttonDelete")).setVisible(true);
    }

    public void onNewClicked(String id)
    {
        logger.debug("onNewClicked(): " + id);

        try
        {
            Window win = (Window) Executions.createComponents(
                    "/gui/admin/modules/collaboration/benutzerDetails.zul", null, null);

            ((BenutzerDetails) win).setUpdateListInterface(this);
            win.doModal();
        }
        catch (Exception ex)
        {
            logger.debug("Fehler beim Ã–ffnen der UserDetails: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    public void onEditClicked(String id, Object data)
    {
        logger.debug("onEditClicked()");

        if (data != null && data instanceof Collaborationuser)
        {
            Collaborationuser user = (Collaborationuser) data;

            try
            {
                Map map = new HashMap();
                map.put("user_id", user.getId());
                map.put("user", user);

                Window win = (Window) Executions.createComponents(
                        "/gui/admin/modules/collaboration/benutzerDetails.zul", null, map);
                ((BenutzerDetails) win).setUpdateListInterface(this);

                win.doModal();
            }
            catch (Exception ex)
            {
                logger.debug("Fehler beim Ã–ffnen der UserDetails: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }
    }

    public void onDeleted(String id, Object data)
    {
        logger.debug("onDeleted()");
        String termUserName = "";
        if (data != null && data instanceof Collaborationuser)
        {
            Collaborationuser user = (Collaborationuser) data;
            
            if(user.getId() == SessionHelper.getCollaborationUserID())
            {
                Messagebox.show("Eigener Benutzer kann nicht gelöscht werden.", "Bentuzer löschen", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            logger.debug("User deaktivieren: " + user.getName());
            
            UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

            DeleteUserRequestType parameter = new DeleteUserRequestType();
            parameter.setLoginType(new LoginType());
            parameter.getLoginType().setSessionID(SessionHelper.getValue("session_id").toString());
            parameter.setCollaborationUser(user);
            
            if(!user.getRoles().isEmpty())
            {
                Role r = user.getRoles().get(0);
                
                String termuserPostfix = "";
                if(r.getName().equals(CODES.ROLE_ADMIN))
                {
                    termuserPostfix = "_tadm";
                }
                else if(r.getName().equals(CODES.ROLE_INHALTSVERWALTER))
                {
                    termuserPostfix = "_iv";
                }
                else if(r.getName().equals(CODES.ROLE_BENUTZER))
                {
                    termuserPostfix = "_dtn";

                }
                else if(r.getName().equals(CODES.ROLE_REZENSENT))
                {
                    termuserPostfix = "_rzt";
                }
                else 
                {
                    Messagebox.show("Die angegebene Rolle kann nicht verarbeitet werden.");
                    return;
                }
            
                TermUser termUser = new TermUser();
                termUser.setUserName(user.getUsername() + termuserPostfix);
                parameter.setTermuser(termUser);

                DeleteUserResponseType response = port.deleteUser(parameter);
                
                if (response.getReturnInfos().getStatus().equals(Status.OK))
                {
                    Messagebox.show("Benutzer erfolgreich gelöscht!");
                }
                else
                {
                    Messagebox.show("Benuter konnte nicht gelöscht werden. " + response.getReturnInfos().getMessage(), "Benutzer löschen", Messagebox.OK, Messagebox.ERROR);
                }
            }
            else
            {
                Messagebox.show("Rolle zu Benutzer wurde nicht gefunden.", "Benutzer löschen", Messagebox.OK, Messagebox.ERROR);
            }
        }

    }

    public void onSelected(String id, Object data)
    {

    }

    public void update(Object o, boolean edited)
    {
        if (o instanceof Collaborationuser)
        {
            // Daten aktualisiert, jetzt dem Model übergeben
            Collaborationuser user = (Collaborationuser) o;

            GenericListRowType row = createRowFromBenutzer(user);

            if (edited)
            {
                // Hier wird die neue Zeile erstellt und der Liste übergeben
                // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
                genericList.updateEntry(row);
            }
            else
            {
                genericList.addEntry(row);
            }
        }
    }
}
