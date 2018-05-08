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
package de.fhdo.collaboration.desktop;

import de.fhdo.collaboration.helper.CODES;
import de.fhdo.helper.SessionHelper;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class MainCollab extends Window implements AfterCompose {

	private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

	public MainCollab() {
		/*if(SessionHelper.isAdmin() == false)
		 {
		 Executions.getCurrent().sendRedirect("../../TermBrowser/gui/main/main.zul");
		 }*/
	}

	public void onNavigationSelect(SelectEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("onNavigationSelect()");
		}

		logger.debug("class: " + event.getReference().getClass().getCanonicalName());
		Tab tab = (Tab) event.getReference();
		tabSelected(tab.getId());
	}

	private void tabSelected(String ID) {
		if (ID == null || ID.length() == 0) {
			return;
		}

		if (ID.equals("tabProposal")) {
			includePage("incProposal", "/collaboration/desktop/desktop.zul");
		} else if (ID.equals("tabDiscGroup")) {
			includePage("incDiscGroup", "/collaboration/discgroup/discGroupMain.zul");
		} else if (ID.equals("tabPublication") && SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)) {
			setTabVisibility("tabPublication", true);
			includePage("incPublication", "/collaboration/publication/publicationMain.zul");
		} else {
			logger.debug("ID nicht bekannt: " + ID);
		}
		if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) && (!ID.equals("tabPublication"))) {
			setTabVisibility("tabPublication", true);
		}
		
		SessionHelper.setValue("collab_tabid", ID);
	}

	private void includePage(String ID, String Page) {
		try {
			Include inc = (Include) getFellow(ID);
			inc.setSrc(null);

			logger.debug("includePage: " + ID + ", Page: " + Page);
			inc.setSrc(Page);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void afterCompose() {
		String id = "";

		Object o = SessionHelper.getValue("collab_tabid");
		if (o != null) {
			id = o.toString();
		}

		if (id != null && id.length() > 0) {
			logger.debug("Goto Page: " + id);
			try {
				Tabbox tb = (Tabbox) getFellow("tabboxNavigation");
				//Tabpanel panel = (Tabpanel) getFellow("tabboxNavigation");
				Tab tab = (Tab) getFellow(id);
				int index = tab.getIndex();
				logger.debug("Index: " + index);

				tb.setSelectedIndex(index);

				tabSelected(id);
			} catch (Exception e) {
				tabSelected("tabProposal");
				logger.warn(e.getMessage());
			}
		} else {
			if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)) {
				Tab tab = (Tab) getFellow("tabPublication");
				setTabVisibility(tab.getId(), true);
				tabSelected(tab.getId());
				tab.setSelected(true);
			} else {
				tabSelected("tabProposal");
				Tab tab = (Tab) getFellow("tabProposal");
				tab.setSelected(true);
			}
		}
	}
	
	private void setTabVisibility(String tabId, boolean b)
	{
		Tab tab = (Tab) getFellow(tabId);
		tab.setVisible(b);
	}
}
