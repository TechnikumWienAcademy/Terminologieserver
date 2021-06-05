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
public class Kollaboration extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tabbox tb;
  public Kollaboration()
  {
    
  }
  
  public void afterCompose()
  {
    String id = "";
    tb = (Tabbox) getFellow("tabboxNavigation");
    Object o = SessionHelper.getSessionAttributeByName("termadmin_kollaboration_tabid");
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
        tabSelected("tabRepKollab"); 
        logger.warn(e.getMessage());
      }
    }
    else{

        tabSelected("tabRepKollab");
    }
    Tabs tabs = tb.getTabs();
    List<Component> tabList = tabs.getChildren();
    
    if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)){
    
        for(Component c:tabList){
        
            if(c.getId().equals("tabBenutzer"))
                c.setVisible(true);
            
            if(c.getId().equals("tabAnfragen"))
                c.setVisible(true);
            
            if(c.getId().equals("tabSvAssignment"))
                c.setVisible(true);
            
            if(c.getId().equals("tabWorkflow"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabDomain"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabSysParam"))
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

    if (ID.equals("tabBenutzer"))
    {
      includePage("incBenutzer", "/gui/admin/modules/collaboration/benutzer.zul");
    }
    else if (ID.equals("tabAnfragen"))
    {
      includePage("incAnfragen", "/gui/admin/modules/collaboration/anfragen.zul");
    }
    else if (ID.equals("tabSvAssignment"))
    {
      includePage("incSvAssignment", "/gui/admin/modules/collaboration/svassignment.zul");
    }
    else if (ID.equals("tabWorkflow"))
    {
      includePage("incWorkflow", "/gui/admin/modules/collaboration/workflow.zul");
    }
    else if (ID.equals("tabDomain"))
    {
      includePage("incDomain", "/gui/admin/modules/collaboration/domain.zul");
    }
    else if (ID.equals("tabSysParam"))
    {
      includePage("incSysParam", "/gui/admin/modules/collaboration/sysParam.zul");
    }
    else if (ID.equals("tabRepKollab"))
    {
      includePage("incRepKollab", "/gui/admin/modules/collaboration/reportingKollab.zul");
    }
    else logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("termadmin_kollaboration_tabid", ID);
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
