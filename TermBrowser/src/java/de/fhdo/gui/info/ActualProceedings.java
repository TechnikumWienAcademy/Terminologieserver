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
package de.fhdo.gui.info;

import de.fhdo.helper.WebServiceHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.models.comparators.ComparatorOrderNr;
import de.fhdo.models.comparators.ComparatorProceedings;
import de.fhdo.terminologie.ws.administration.ActualProceeding;
import de.fhdo.terminologie.ws.administration.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.Status;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 *
 * @author Philipp Urbauer
 */
public class ActualProceedings extends Window implements org.zkoss.zk.ui.ext.AfterCompose,IGenericListActions //public class Menu extends GenericAutowireComposer
{
 
    GenericList genericList;
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
    public ActualProceedings()
    {
    }

    public void afterCompose()
    {
        initList();
    }

    
    private void initList(){
        
        logger.debug("ActualProceedings(): initList()");

        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Terminologie", 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Version", 250, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Typ", 100, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Änderung", 100, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Datum der Änderung", 150, "", true, "String", true, true, false, false));

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        
        ActualProceedingsRequestType aprequt = new ActualProceedingsRequestType();
        ActualProceedingsResponseType aprespt = WebServiceHelper.actualProceedings(aprequt);
        
        if(aprespt.getReturnInfos().getStatus() == Status.OK){
            for(ActualProceeding ap:aprespt.getActualProceedings()){

                GenericListRowType row = createMyTermRow(ap);
                dataList.add(row);
            }
        }
        
        // Liste initialisieren
        Include inc = (Include) getFellow("incListActualProceedings");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;
        Collections.sort(dataList, new ComparatorProceedings(false));
        
        genericList.setListActions(this);
        genericList.setButton_new(false);
        genericList.setButton_edit(false);
        genericList.setButton_delete(false);
        genericList.setListHeader(header);
        genericList.setDataList(dataList);
    }

    private GenericListRowType createMyTermRow(Object obj)
    {
        GenericListRowType row = new GenericListRowType();
        GenericListCellType[] cells = new GenericListCellType[5]; //Size
        
        if(obj instanceof ActualProceeding){
           
            cells[0] = new GenericListCellType(((ActualProceeding)obj).getTerminologieName(), false, "");
            cells[1] = new GenericListCellType(((ActualProceeding)obj).getTerminologieVersionName(), false, "");
            cells[2] = new GenericListCellType(((ActualProceeding)obj).getTerminologieType(), false, "");
            cells[3] = new GenericListCellType(((ActualProceeding)obj).getStatus(), false, "");
            cells[4] = new GenericListCellType(((ActualProceeding)obj).getLastChangeDate(), false, "");
        }
        
        row.setData(obj);
        row.setCells(cells);

        return row;
    } 
    
    public void onNewClicked(String id) {
        this.setVisible(false);
        this.detach();
    }

    public void onEditClicked(String id, Object data) {
        
    }

    public void onDeleted(String id, Object data) {
        
    }

    public void onSelected(String id, Object data) {
        
    }
}
