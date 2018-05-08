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
package de.fhdo.gui.main.modules;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.models.TreeNode;
import de.fhdo.terminologie.ws.search.GlobalSearchResultEntry;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;

/**
 *
 * @author Becker
 */
public class PopupGlobalSearch extends Window implements IGenericListActions, IUpdateModal, AfterCompose
{

  GenericList genericList;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Integer mode = -1;
  private Label l_status;
	private Button bDetailsCSV;
	
	private GlobalSearchResultEntry selectedEntry;

  public void afterCompose()
  {
    mode = (Integer) Executions.getCurrent().getArg().get("EditMode");
    initList();
  }

  private void initList()
  {
    logger.debug("Desktop(): initList()");
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    if (mode == 97)
    {
      header.add(new GenericListHeaderType("Begriff", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Code", 225, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("CodeSystem", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("CodeSystemVersion", 200, "", true, "String", true, true, false, false));
    }
    else
    { //mode == 98

      header.add(new GenericListHeaderType("Begriff", 225, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Code", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("ValueSet", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("ValueSetVersion", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Quelle", 400, "", true, "String", true, true, false, false));
    }
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    // Liste initialisieren
    l_status = (Label) getFellow("l_status");
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
    
    genericList.getListbox().setMold("paging");
    genericList.getListbox().setPageSize(20);
  }

  public void onSearchClicked()
  {

    initList();

    ListGloballySearchedConceptsRequestType parameter = new ListGloballySearchedConceptsRequestType();
    ListGloballySearchedConceptsResponse.Return response = null;
    List<GlobalSearchResultEntry> gsreList = null;

    if (mode == 97)
    {  //CS Global Search
      parameter.setCodeSystemConceptSearch(true);
    }

    if (mode == 98)
    {  //VS Global Search
      parameter.setCodeSystemConceptSearch(false);
    }

    if (SessionHelper.isCollaborationActive())
    {
      // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
      parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
      parameter.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
    }
    else if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
      parameter.getLogin().setSessionID(SessionHelper.getSessionId());
    }

    Textbox tbTerm = (Textbox) getFellow("tbTerm");
    Textbox tbCode = (Textbox) getFellow("tbCode");

    if (tbTerm.getText() != null)
    {
      parameter.setTerm(tbTerm.getText());
    }
    else
    {
      parameter.setTerm("");
    }

    if (tbCode.getText() != null)
    {
      parameter.setCode(tbCode.getText());
    }
    else
    {
      parameter.setCode("");
    }

    response = WebServiceHelper.listGloballySearchedConcepts(parameter);
    if (response != null)
    {
      gsreList = response.getGlobalSearchResultEntry();

      if (response.getReturnInfos().getStatus().equals(Status.OK))
      {
        if (gsreList.isEmpty())
        {
          l_status.setValue("Ihre Suche ergab keine Resultate!");
        }
        else
        {
          l_status.setValue("");
        }
        for (GlobalSearchResultEntry entry : gsreList)
        {
          GenericListRowType row = createMyTermRow(entry);
          genericList.addEntry(row);
					
					
        }
			
      }
    }
  }

  private GenericListRowType createMyTermRow(Object obj)
  {
    GenericListRowType row = new GenericListRowType();
		
    GenericListCellType[] cells = null;

    if (obj instanceof GlobalSearchResultEntry)
    {

      if (((GlobalSearchResultEntry) obj).isCodeSystemEntry())
      {
        cells = new GenericListCellType[4];
        cells[0] = new GenericListCellType(((GlobalSearchResultEntry) obj).getTerm(), false, "");
        cells[1] = new GenericListCellType(((GlobalSearchResultEntry) obj).getCode(), false, "");
        cells[2] = new GenericListCellType(((GlobalSearchResultEntry) obj).getCodeSystemName(), false, "");
        cells[3] = new GenericListCellType(((GlobalSearchResultEntry) obj).getCodeSystemVersionName(), false, "");
      }
      else
      {
        cells = new GenericListCellType[5];
        cells[0] = new GenericListCellType(((GlobalSearchResultEntry) obj).getTerm(), false, "");
        cells[1] = new GenericListCellType(((GlobalSearchResultEntry) obj).getCode(), false, "");
        cells[2] = new GenericListCellType(((GlobalSearchResultEntry) obj).getValueSetName(), false, "");
        cells[3] = new GenericListCellType(((GlobalSearchResultEntry) obj).getValueSetVersionName(), false, "");
        cells[4] = new GenericListCellType(((GlobalSearchResultEntry) obj).getSourceCodeSystemInfo(), false, "");
      }
    }

    row.setData(obj);
    row.setCells(cells);

    return row;
  }
	
	public void showPopupConcept(int editMode){
		
		
		//TreeNode tnSelected = (TreeNode) tree.getSelectedItem().getValue();
		//CodeSystemEntityVersion csevSelected = (CodeSystemEntityVersion) tnSelected.getData();

		Map<String, Object> data = new HashMap<String, Object>();
		//data.put("EditMode", editMode);  // PopupWindow.EDITMODE_
		//data.put("ContentMode", 1);       // 1=CS, 2=VS
		//data.put("Tree", tree);
		//data.put("Id", id);
		//data.put("VersionId", versionId);
		//data.put("TreeNode", tnSelected);
		//data.put("CSEV", csevSelected);
		data.put("EditMode", editMode);  // PopupWindow.EDITMODE_
		data.put("ContentMode", 1); 
		//data.put("CSEV", selectedEntry.getCsevId());
		//data.put("VersionId", selectedEntry.getCsvId());
		//data.put("Id", selectedEntry.getCsId());

		try
		{
			
			ReturnConceptDetailsRequestType parameter   = new ReturnConceptDetailsRequestType();        
			parameter.setCodeSystemEntity(new CodeSystemEntity());
			parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
			CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
			//for CS
			if (mode == 97){
				
				csev.setVersionId(selectedEntry.getCsevId());
				parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
				//CSE aus CSEV entfernen, sonst inf,loop
				csev.setCodeSystemEntity(null);
			}
			//for VS
			if (mode == 98){
				csev.setVersionId(selectedEntry.getCvsmId().getCodeSystemEntityVersionId());
				parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
				//CSE aus CSEV entfernen, sonst inf,loop
				csev.setCodeSystemEntity(null);
			}
			
			
			
			if(SessionHelper.isUserLoggedIn()){

					de.fhdo.terminologie.ws.search.LoginType loginSearch = new de.fhdo.terminologie.ws.search.LoginType();
					loginSearch.setSessionID(SessionHelper.getSessionId());
					parameter.setLogin(loginSearch);
			}

			ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(parameter);

			// keine csev zurueckgekommen (wegen moeglicher Fehler beim WS)
			if(response.getCodeSystemEntity() == null)
					return;
			
			data.put("CSE", response.getCodeSystemEntity());
			
			
			final Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", null, data);
			w.doOverlapped();
		}
		catch (Exception e)
		{
			Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
		}
	}

  public void onNewClicked(String id)
  {

  }

  public void onEditClicked(String id, Object data)
  {

  }

  public void onDeleted(String id, Object data)
  {

  }

  public void onSelected(String id, Object data)
  {
		selectedEntry = (GlobalSearchResultEntry) data;
		
		bDetailsCSV = (Button) getFellow("bDetails");
		bDetailsCSV.setDisabled(false);
		boolean stop = true;
  }

  public void update(Object o, boolean edited)
  {

  }
}
