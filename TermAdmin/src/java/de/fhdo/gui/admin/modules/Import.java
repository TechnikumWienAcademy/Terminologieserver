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

import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Import extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tabbox tb;
  public Import()
  {
    
  }
  
  public void afterCompose()
  {
    String id = "";
    tb = (Tabbox) getFellow("tabboxNavigation");
    Object o = SessionHelper.getValue("termadmin_import_tabid");
    if (o != null)
      id = o.toString();

    if (id != null && id.length() > 0)
    {
      logger.debug("Goto Page: " + id);
      try
      {
        Tab tab = (Tab) getFellow(id);
        int index = tab.getIndex();
        logger.debug("Index: " + index);

        tb.setSelectedIndex(index);

        tabSelected(id);
      }
      catch (Exception e)
      {
        tabSelected("tabCLAML");
        logger.warn(e.getMessage());
      }
    }
    else
      tabSelected("tabCLAML");
    
    Tabs tabs = tb.getTabs();
    List<Component> tabList = tabs.getChildren();
    
    if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) 
            || SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
    
        for(Component c:tabList){
        
            if(c.getId().equals("tabLOINC"))
                c.setVisible(true);
            
            if(c.getId().equals("tabLEIKAT"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabICDBMG"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabKBV"))
                c.setVisible(true);
        }
    }
  }
  
  
  public void onNavigationSelect(SelectEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("onNavigationSelect()");

    logger.debug("class: " + event.getReference().getClass().getCanonicalName());
    Tab tab = (Tab) event.getReference();
    tabSelected(tab.getId());
  }
  
  private void tabSelected(String ID)
  {
    if (ID == null || ID.length() == 0)
      return;

    if (ID.equals("tabCLAML"))
    {
      includePage("incCLAML", "/gui/admin/modules/importClaML.zul");
    }
    else if (ID.equals("tabCS_CSV"))
    {
      includePage("incCS_CSV", "/gui/admin/modules/importCSV.zul");
    }
    else if (ID.equals("tabVS_CSV"))
    {
      includePage("incVS_CSV", "/gui/admin/modules/importVS_CSV.zul");
    }
    else if (ID.equals("tabKBV"))
    {
      includePage("incKBV", "/gui/admin/modules/importKBV.zul");
    }
    else if (ID.equals("tabLOINC"))
    {
      includePage("incLOINC", "/gui/admin/modules/importLOINC.zul");
    }
    else if (ID.equals("tabCS_SVS"))
    {
      includePage("incCS_SVS", "/gui/admin/modules/importCS_SVS.zul");
    }
    else if (ID.equals("tabVS_SVS"))
    {
      includePage("incVS_SVS", "/gui/admin/modules/importVS_SVS.zul");
    }
    else if (ID.equals("tabLEIKAT"))
    {
      includePage("incLEIKAT", "/gui/admin/modules/importLeiKatCSV.zul");
    }
    else if (ID.equals("tabICDBMG"))
    {
      includePage("incICDBMG", "/gui/admin/modules/importICDAT.zul");
    }
    else logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("termadmin_import_tabid", ID);
  }

  private void includePage(String ID, String Page)
  {
    try
    {
      Include inc = (Include) getFellow(ID);
      inc.setSrc(null);

      logger.debug("includePage: " + ID + ", Page: " + Page);
      inc.setSrc(Page);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
