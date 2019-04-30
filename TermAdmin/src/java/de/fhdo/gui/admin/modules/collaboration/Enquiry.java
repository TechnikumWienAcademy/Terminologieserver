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
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * @author Philipp Urbauer
 */
public class Enquiry extends Window implements AfterCompose, IGenericListActions, IUpdateModal{
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private GenericList genericList;

    @Override
    public void afterCompose(){
      initList();
    }
  
    private GenericListRowType createRowFromEnquiry(de.fhdo.collaboration.db.classes.Enquiry enquiry){
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[5];
        cells[0] = new GenericListCellType(enquiry.getCollaborationuser().getFirstName(), false, "");
        cells[1] = new GenericListCellType(enquiry.getCollaborationuser().getName(), false, "");
        cells[2] = new GenericListCellType(enquiry.getCollaborationuser().getOrganisation().getOrganisation(), false, "");
        cells[3] = new GenericListCellType(enquiry.getRequestType(), false, "");
        String status;
        if(!enquiry.getClosedFlag())
            status = "Offen";
        else
            status = "Geschlossen";
        cells[4] = new GenericListCellType(status, false, "");

        row.setData(enquiry);
        row.setCells(cells);

        return row;
    }

    private void initList(){
        List<GenericListHeaderType> header = new LinkedList<>();
    
        header.add(new GenericListHeaderType("Vorname", 150, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Nachname", 250, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Organisation", 270, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Anfrage Typ", 450, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Status", 100, "", true, "String", true, true, false, false));
    
        // Daten laden
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        List<GenericListRowType> rowList = new LinkedList<>();
        try{
            String HQL_enquiry_search = "from Enquiry order by requestType";
            List<de.fhdo.collaboration.db.classes.Enquiry> enquiryList = hb_session.createQuery(HQL_enquiry_search).list();

            for (int i = 0; i < enquiryList.size(); ++i){
                de.fhdo.collaboration.db.classes.Enquiry enquiry = enquiryList.get(i);
                GenericListRowType row = createRowFromEnquiry(enquiry);

                rowList.add(row);
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0136]", e);
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }

        //Initializing list
        Include inc = (Include) getFellow("incList");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;

        genericList.setListActions(this);
        genericList.setButton_new(true);
        genericList.setButton_edit(true);
        genericList.setButton_delete(true);
        genericList.setListHeader(header);
        genericList.setDataList(rowList);
    
        ((Button)genericList.getFellow("buttonNew")).setVisible(false);
    }
  
    @Override
    public void onNewClicked(String id){
        LOGGER.debug("CLICKED UNonNewClicked(): + " + id);
    }

    @Override
    public void onEditClicked(String id, Object data){
        if (data != null && data instanceof de.fhdo.collaboration.db.classes.Enquiry){
            de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) data;

            try{
                Map map = new HashMap();
                map.put("enquiry_id", enquiry.getId());


                Window win = (Window) Executions.createComponents("/gui/admin/modules/collaboration/anfrageDetails.zul", null, map);
                ((EnquiryDetails) win).setUpdateListInterface(this);

                win.doModal();
            }
            catch (Exception ex){
                LOGGER.error("Error [0096", ex);
            }
        }
    }

    @Override
    public void onDeleted(String id, Object data){
        if (data != null && data instanceof de.fhdo.collaboration.db.classes.Enquiry){
            de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) data;
            LOGGER.debug("Deleting enquiry with ID " + enquiry.getId());

            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try{
                de.fhdo.collaboration.db.classes.Enquiry enquiry_db = (de.fhdo.collaboration.db.classes.Enquiry) hb_session.get(de.fhdo.collaboration.db.classes.Enquiry.class, enquiry.getId());
                hb_session.delete(enquiry_db);

                if(enquiry_db.getCollaborationuser().getHidden()){
                  hb_session.delete(enquiry_db.getCollaborationuser());
                  hb_session.delete(enquiry_db.getCollaborationuser().getOrganisation());
                }
        
                if(enquiry_db.getCollaborationuserExtPerson() != null && enquiry_db.getCollaborationuserExtPerson().getHidden()){
                    hb_session.delete(enquiry_db.getCollaborationuserExtPerson());
                    
                    if(enquiry_db.getCollaborationuserExtPerson().getOrganisation() != null)
                        hb_session.delete(enquiry_db.getCollaborationuserExtPerson().getOrganisation());
                }
       
                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();

                Messagebox.show("Anfrage wurde erfolgreich gelöscht.", "Anfrage löschen", Messagebox.OK, Messagebox.INFORMATION);
            }
            catch (Exception e){
                LOGGER.error("Error [0097]", e);
                Messagebox.show("Fehler beim Löschen der Anfrage: " + e.getLocalizedMessage(), "Anfrage löschen", Messagebox.OK, Messagebox.EXCLAMATION);
                if(!hb_session.getTransaction().wasRolledBack())
                    hb_session.getTransaction().rollback();
                this.initList();
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
    }

    @Override
    public void onSelected(String id, Object data){}

    @Override
    public void update(Object updateObject, boolean edited){
        if (updateObject instanceof de.fhdo.collaboration.db.classes.Enquiry){
            // Data updated, passing it to modell
            de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) updateObject;

            GenericListRowType row = createRowFromEnquiry(enquiry);

            if(edited)
                genericList.updateEntry(row);
            else
            genericList.addEntry(row);
        }
    }
}