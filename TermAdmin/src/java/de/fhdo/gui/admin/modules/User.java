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

import de.fhdo.db.HibernateUtil;
import de.fhdo.helper.CollabUserHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.idp.userManagement.GetTermUserRequestType;
import de.fhdo.terminologie.ws.idp.userManagement.GetUserListResponse;
import de.fhdo.terminologie.ws.idp.userManagement.LoginType;
import de.fhdo.terminologie.ws.idp.userManagement.Status;
import de.fhdo.terminologie.ws.idp.userManagement.TermUser;
import de.fhdo.terminologie.ws.idp.userManagement.UserManagement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class User extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    GenericList genericList;

    public User()
    {
        if (SessionHelper.isAdmin() == false)
        {
            Executions.getCurrent().sendRedirect("/gui/main/main.zul");
        }
    }

    public void afterCompose()
    {
        initList();
    }

    private GenericListRowType createRowFromUser(TermUser user)
    {
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[6];
        cells[0] = new GenericListCellType(user.getName(), false, "");
        cells[1] = new GenericListCellType(user.isEnabled() != null ? user.isEnabled() : false, false, "");
        cells[2] = new GenericListCellType(user.isIsAdmin(), false, "");
        cells[3] = new GenericListCellType(user.getUserName(), false, "");
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
        header.add(new GenericListHeaderType("Administrator", 60, "", true, "boolean", true, true, false, true));
        header.add(new GenericListHeaderType("Name", 250, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Email", 300, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Mail-Aktiviert", 100, "", true, "boolean", true, true, false, true));
        
        UserManagement port = WebServiceUrlHelper.getInstance().getUserManagementServicePort();

        GetTermUserRequestType parameter = new GetTermUserRequestType();
        parameter.setLoginType(new LoginType());
        parameter.getLoginType().setSessionID(SessionHelper.getSessionAttributeByName("session_id").toString());

        GetUserListResponse.Return response = port.getUserList(parameter);

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

        if (response.getReturnInfos().getStatus().equals(Status.OK))
        {
            List<TermUser> personList = response.getUserList();

            for (int i = 0; i < personList.size(); ++i)
            {
                TermUser user = personList.get(i);
                GenericListRowType row = createRowFromUser(user);

                dataList.add(row);
            }
        }

        // Liste initialisieren
        Include inc = (Include) getFellow("incList");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;

        //genericList.setUserDefinedId("1");
        genericList.setListActions(this);
        genericList.setButton_new(false);
        genericList.setButton_edit(true);
        genericList.setButton_delete(false);
        genericList.setListHeader(header);
        genericList.setDataList(dataList);

        //genericList.setDataList(null);
        //genericList.set
    }

    public void onNewClicked(String id)
    {
        logger.debug("onNewClicked(): " + id);

        try
        {
            Window win = (Window) Executions.createComponents(
                    "/gui/admin/modules/userDetails.zul", null, null);

            ((UserDetails) win).setUpdateListInterface(this);
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

        if (data != null && data instanceof TermUser)
        {
            TermUser user = (TermUser) data;

            try
            {
                Map map = new HashMap();
                map.put("user_id", user.getId());
                map.put("user", user);

                Window win = (Window) Executions.createComponents(
                        "/gui/admin/modules/userDetails.zul", null, map);

                ((UserDetails) win).setUpdateListInterface(this);

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
        /*logger.debug("onDeleted()");

        if (data != null && data instanceof de.fhdo.db.hibernate.TermUser)
        {
            de.fhdo.db.hibernate.TermUser user = (de.fhdo.db.hibernate.TermUser) data;
            logger.debug("User löschen: " + user.getName());

            // Person aus der Datenbank löschen
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try
            {
                de.fhdo.db.hibernate.TermUser user_db = (de.fhdo.db.hibernate.TermUser) hb_session.get(de.fhdo.db.hibernate.TermUser.class, user.getId());

                Iterator<de.fhdo.db.hibernate.Session> itSession = user_db.getSessions().iterator();
                while (itSession.hasNext())
                {
                    de.fhdo.db.hibernate.Session obj = itSession.next();
                    hb_session.delete(obj);
                }
                Iterator<de.fhdo.db.hibernate.LicencedUser> itLUser = user_db.getLicencedUsers().iterator();
                while (itLUser.hasNext())
                {
                    de.fhdo.db.hibernate.LicencedUser obj = itLUser.next();
                    hb_session.delete(obj);
                }

                hb_session.delete(user_db);

                hb_session.getTransaction().commit();

                Messagebox.show("Benutzer wurde erfolgreich gelöscht.", "Benutzer löschen", Messagebox.OK, Messagebox.INFORMATION);
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();

                Messagebox.show("Fehler beim Löschen des Benutzers: " + e.getLocalizedMessage(), "Benutzer löschen", Messagebox.OK, Messagebox.EXCLAMATION);
                initList();
            }
            hb_session.close();
            CollabUserHelper.reloadModel();
        }*/
    }

    public void onSelected(String id, Object data)
    {

    }

    public void update(Object o, boolean edited)
    {
        if (o instanceof TermUser)
        {
            // Daten aktualisiert, jetzt dem Model übergeben
            TermUser user = (TermUser) o;

            GenericListRowType row = createRowFromUser(user);

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
