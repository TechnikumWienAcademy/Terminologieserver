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

import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.DomainHelper;
import de.fhdo.helper.SessionHelper;
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
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class SysParamCollab extends Window implements AfterCompose, IGenericListActions, IUpdateModal{
    
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private GenericList genericList;

    /**
     * If the user is not an admin he will be redirected to the main-page during
     * the constructor call.
     */
    public SysParamCollab(){
        if (SessionHelper.isAdmin() == false)
            Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }

    /**
     * Generates a row, filling it with the name, validity domain, modifiy level,
     * java datatype, value and description.
     * @param sysParam the sysParam from which the values for the row are taken
     * @return the generated row
     */
    private GenericListRowType createRowFromSysParam(de.fhdo.collaboration.db.classes.SysParam sysParam){
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[6];
        cells[0] = new GenericListCellType(sysParam.getName(), false, "");
        cells[1] = new GenericListCellType(sysParam.getDomainValueByValidityDomain().getDisplayText(), false, "");
        cells[2] = new GenericListCellType(sysParam.getDomainValueByModifyLevel().getDisplayText(), false, "");
        cells[3] = new GenericListCellType(sysParam.getJavaDatatype(), false, "");
        cells[4] = new GenericListCellType(sysParam.getValue(), false, "");
        cells[5] = new GenericListCellType(sysParam.getDescription(), false, "");

        row.setData(sysParam);
        row.setCells(cells);

        return row;
    }

    private void initList(){
        String[] filter = DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_VALIDITYDOMAIN);

        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Name", 230, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Gültigkeitsbereich", 130, "", true, filter, true, true, false, false));
        header.add(new GenericListHeaderType("Modify-Level", 130, "", true, filter, true, true, false, false));
        header.add(new GenericListHeaderType("Datentyp", 80, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Wert", 700, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Beschreibung", 400, "", true, "String", true, true, false, false));
    
    
    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from SysParam p join fetch p.domainValueByModifyLevel join fetch p.domainValueByValidityDomain where p.objectId is null order by p.name";
      List<de.fhdo.collaboration.db.classes.SysParam> paramList = hb_session.createQuery(hql).list();

      for (int i = 0; i < paramList.size(); ++i)
      {
        de.fhdo.collaboration.db.classes.SysParam sysParam = paramList.get(i);
        GenericListRowType row = createRowFromSysParam(sysParam);

        dataList.add(row);
      }
    }
    catch (Exception e)
    {
      LOGGER.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(true);
    genericList.setButton_edit(true);
    genericList.setButton_delete(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  public void afterCompose()
  {
    initList();
  }

  public void onNewClicked(String id)
  {
    LOGGER.debug("onNewClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");

    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/collaboration/sysParamDetails.zul", null, null);

      ((SysParamDetails) win).setiUpdateListener(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      LOGGER.debug("Fehler beim Ã–ffnen der SysParamDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }

  }

  public void onEditClicked(String id, Object data)
  {
    LOGGER.debug("onEditClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");
    if (data != null && data instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) data;
      LOGGER.debug("Parameter: " + sysParam.getName());

      try
      {
        Map map = new HashMap();
        map.put("sysparam_id", sysParam.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/sysParamDetails.zul", null, map);

        ((SysParamDetails) win).setiUpdateListener(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        LOGGER.debug("Fehler beim Ã–ffnen der SysParamDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    LOGGER.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) data;
      LOGGER.debug("Person: " + sysParam.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.collaboration.db.classes.SysParam sysParamDB = (de.fhdo.collaboration.db.classes.SysParam) hb_session.get(de.fhdo.collaboration.db.classes.SysParam.class, sysParam.getId());
        hb_session.delete(sysParamDB);

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        LOGGER.error("[" + this.getClass().getCanonicalName() + "] Fehler beim Löschen eines Eintrags: " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
  }

  public void update(Object o, boolean edited)
  {
    if (o != null && o instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      // Hier wird die neue Zeile erstellt und der Liste übergeben
      // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) o;
      GenericListRowType row = createRowFromSysParam(sysParam);
        
      if (edited)
      {
        // Daten aktualisiert, jetzt dem Model übergeben
        LOGGER.debug("Daten aktualisiert: " + sysParam.getName());
        
        genericList.updateEntry(row);
      }
      else
      {
        genericList.addEntry(row);
      }
    }
  }
}
