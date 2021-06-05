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
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.db.hibernate.ValueSet;
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
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class SvAssignment extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  
  public SvAssignment()
  {
    
  }

  public void afterCompose()
  {
    initList();
  }
  
  private GenericListRowType createRowFromSvAssignmentData(SvAssignmentData data)
  {
    GenericListRowType row = new GenericListRowType();
    
    GenericListCellType[] cells = new GenericListCellType[7];
    cells[0] = new GenericListCellType(data.getTermName(), false, "");
    cells[1] = new GenericListCellType(data.getClassname(), false, "");
    cells[2] = new GenericListCellType(data.getUsername(), false, "");
    cells[3] = new GenericListCellType(data.getFirstName(), false, "");
    cells[4] = new GenericListCellType(data.getName(), false, "");
    cells[5] = new GenericListCellType(data.getOrganisation(), false, "");
    cells[6] = new GenericListCellType(data.getAutoRelease(), false, "");

    row.setData(data);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Terminologiename", 350, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Typ", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Benutzername", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Vorname", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Nachname", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Organisation", 200, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("autom. Freigabe", 100, "", true, "Boolean", true, true, false, false));

    // Daten laden
    Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    Session hb_session_term = de.fhdo.db.HibernateUtil.getSessionFactory().openSession();
    //hb_session_term.getTransaction().begin();
    
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      
        //1.GetAll CS
        String hqlCs = "select distinct cs from CodeSystem cs";
        Query qCs = hb_session_term.createQuery(hqlCs);
        List<CodeSystem> csList = qCs.list();
        
        for(CodeSystem cs:csList){
            SvAssignmentData data = new SvAssignmentData();
            data.setClassId(cs.getId());
            data.setClassname("CodeSystem");
            data.setTermName(cs.getName());
            data.setAutoRelease(cs.getAutoRelease());
            
            //Check for userAssignment
            Query q = hb_session_kollab.createQuery("select distinct a from AssignedTerm a " + 
                                                    "join fetch a.collaborationuser u join fetch u.organisation o " + 
                                                    "WHERE a.classId= :p_classId AND a.classname= :p_classname");
            q.setLong("p_classId", cs.getId());
            q.setString("p_classname", "CodeSystem");
            List<AssignedTerm> atList = q.list();
            
            if(atList.size() == 1){
                
                data.setUsername(atList.get(0).getCollaborationuser().getUsername());
                data.setFirstName(atList.get(0).getCollaborationuser().getFirstName());
                data.setName(atList.get(0).getCollaborationuser().getName());
                data.setCollaborationuserId(atList.get(0).getCollaborationuser().getId());
                data.setOrganisation(atList.get(0).getCollaborationuser().getOrganisation().getOrganisation());
                data.setAssignedTermId(atList.get(0).getId());
            }else{
                data.setUsername("-");
                data.setFirstName("-");
                data.setName("-");
                data.setCollaborationuserId(null);
                data.setOrganisation("-");
                data.setAssignedTermId(null);
            }
            GenericListRowType row = createRowFromSvAssignmentData(data);
            dataList.add(row);
        }
        
        //2.GetAll VS
        String hqlVs = "select distinct vs from ValueSet vs";
        Query qVs = hb_session_term.createQuery(hqlVs);
        List<ValueSet> vsList = qVs.list();
        
        for(ValueSet vs:vsList){
            SvAssignmentData data = new SvAssignmentData();
            data.setClassId(vs.getId());
            data.setClassname("ValueSet");
            data.setTermName(vs.getName());
            data.setAutoRelease(vs.getAutoRelease());
            
            //Check for userAssignment
            Query q = hb_session_kollab.createQuery("select distinct a from AssignedTerm a " + 
                                                    "join fetch a.collaborationuser u join fetch u.organisation o " + 
                                                    "WHERE a.classId= :p_classId AND a.classname= :p_classname");
            q.setLong("p_classId", vs.getId());
            q.setString("p_classname", "ValueSet");
            List<AssignedTerm> atList = q.list();
            
            if(atList.size() == 1){
            
                data.setUsername(atList.get(0).getCollaborationuser().getUsername());
                data.setFirstName(atList.get(0).getCollaborationuser().getFirstName());
                data.setName(atList.get(0).getCollaborationuser().getName());
                data.setCollaborationuserId(atList.get(0).getCollaborationuser().getId());
                data.setOrganisation(atList.get(0).getCollaborationuser().getOrganisation().getOrganisation());
                data.setAssignedTermId(atList.get(0).getId());
            }else{
                data.setUsername("-");
                data.setFirstName("-");
                data.setName("-");
                data.setCollaborationuserId(null);
                data.setOrganisation("-");
                data.setAssignedTermId(null);
            }
            GenericListRowType row = createRowFromSvAssignmentData(data);
            dataList.add(row);
        }
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session_kollab.close();
      hb_session_term.close();
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
  }

  public void onEditClicked(String id, Object object)
  {
    logger.debug("onEditClicked()");

    if (object != null && object instanceof SvAssignmentData)
    {
      SvAssignmentData data = (SvAssignmentData) object;

      try
      {
        Map map = new HashMap();
        map.put("svAssignmentData", data);


        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/svassignmentDetails.zul", null, map);
        ((SvAssignmentDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Ã–ffnen der UserDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
    
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof SvAssignmentData)
    {
      // Daten aktualisiert, jetzt dem Model übergeben
      SvAssignmentData data = (SvAssignmentData) o;

      GenericListRowType row = createRowFromSvAssignmentData(data);

      if (edited)
      {
        genericList.updateEntry(row);
      }
      else
      {
        genericList.addEntry(row);
      }
    }
  }

    public void onNewClicked(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onDeleted(String id, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
