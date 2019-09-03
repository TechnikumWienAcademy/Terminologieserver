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
public class Terminologie extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tabbox tb;

  public Terminologie()
  {

  }

  public void afterCompose()
  {
    String id = "";

    Object o = SessionHelper.getSessionAttributeByName("termadmin_terminologie_tabid");
    if (o != null)
      id = o.toString();
    tb = (Tabbox) getFellow("tabboxNavigation");
    //Tabpanel panel = (Tabpanel) getFellow("tabboxNavigation");
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
        tabSelected("tabMetaVok");
        logger.warn(e.getMessage());
      }
    }
    else
    {

      tabSelected("tabMetaVok");
    }

    Tabs tabs = tb.getTabs();
    List<Component> tabList = tabs.getChildren();

    // TODO Kollaboration hier rausnehmen bzw. anders abfragen
    //if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
    if(SessionHelper.isAdmin())
    {
      for (Component c : tabList)
      {

        if (c.getId().equals("tabBenutzer"))
          c.setVisible(true);

        if (c.getId().equals("tabLizenzen"))
          c.setVisible(true);

        if (c.getId().equals("tabTaxonomie"))
          c.setVisible(true);

        if (c.getId().equals("tabDomains"))
          c.setVisible(true);

        if (c.getId().equals("tabSysPara"))
          c.setVisible(true);

        if (c.getId().equals("tabDatenbank"))
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
      includePage("incBenutzer", "/gui/admin/modules/user.zul");
    }
    else if (ID.equals("tabLizenzen"))
    {
      includePage("incLizenzen", "/gui/admin/modules/lizenzen.zul");
    }
    else if (ID.equals("tabTaxonomie"))
    {
      includePage("incTaxonomie", "/gui/admin/modules/codesysteme.zul");
    }
    else if (ID.equals("tabMetaVok"))
    {
      includePage("incMetaVok", "/gui/admin/modules/metadatenCS.zul");
    }
    else if (ID.equals("tabMetaVal"))
    {
      includePage("incMetaVal", "/gui/admin/modules/metadatenVS.zul");
    }
    else if (ID.equals("tabDomains"))
    {
      includePage("incDomains", "/gui/admin/modules/domain.zul");
    }
    else if (ID.equals("tabSysPara"))
    {
      includePage("incSysPara", "/gui/admin/modules/sysParam.zul");
    }
    else if (ID.equals("tabRepoTerm"))
    {
      includePage("incRepoTerm", "/gui/admin/modules/reportingTerm.zul");
    }
    else if (ID.equals("tabDatenbank"))
    {
      includePage("incDatenbank", "/gui/admin/modules/datenbank.zul");
    }
    else
      logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("termadmin_terminologie_tabid", ID);
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
