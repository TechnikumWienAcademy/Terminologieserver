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
package de.fhdo.tree;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeItemRenderer implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<GenericTreeHeaderType> listHeader;
  private IDoubleClick doubleClickEvent;
  private IUpdateData updateDataEvent;

  public GenericTreeItemRenderer(List<GenericTreeHeaderType> _listHeader)
  {
    listHeader = _listHeader;


  }

  public void render(Treeitem item, Object tn, int index) throws Exception
  {
    TreeNode treeNode = (TreeNode) tn;
    //Treerow dataRow = new Treerow();
    item.setAttribute("treenode", tn);
    item.setContext("treePopupItem");


    /*boolean updateRow = false;
    
     if(item.getTreerow() != null)
     {
     //item.getChildren().clear(); // geht so nicht, da alle children gelöscht werden
     updateRow = true;
     }*/
    Object data = treeNode.getData();

    if (data instanceof GenericTreeRowType)
    {
      GenericTreeRowType row = (GenericTreeRowType) data;
      renderRow(item, row);
    }
    else
    {
      if (data != null)
        logger.debug("Object-Type nicht gefunden: " + data.getClass().getCanonicalName());
      else
        logger.debug("Object-Type nicht gefunden: null");
    }

    /*Menupopup contextMenu = new Menupopup();
     dataRow.setParent(treeItem);
     treeItem.setValue(treeNode);

     if (data instanceof CodeSystemEntityVersion)
     {
     renderCSEVMouseEvents(dataRow, treeItem, treeNode);
     renderCSEVContextMenu(contextMenu, treeItem, dataRow, data);
     renderCSEVDisplay(dataRow, data, treeItem, treeNode);

     if (draggable)
     dataRow.setDraggable("true");

     // Nur für CS, da es für VS nicht vorgesehen ist, hierarchien aufzubauen
     if (droppable && contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
     dataRow.setDroppable("true");
     }
     else
     {
     logger.debug("Object-Type nicht gefunden (data instanceof CodeSystemEntityVersion == false)");
     }*/
  }

  private void renderRow(Treeitem item, GenericTreeRowType row)
  {
    Treerow treeRow = item.getTreerow();

    if (item.getTreerow() == null)
    {
      // Neuer Renderer
      treeRow = new Treerow();

      item.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
      {
        public void onEvent(Event event) throws Exception
        {
          logger.debug("doubleclicked");

          if (doubleClickEvent != null)
            doubleClickEvent.onDoubleClick(event.getData());
        }
      });
    }
    else
    {
      // Aktualisiert einen Eintrag
      treeRow.getChildren().clear();
    }

    for (int i = 0; i < row.getCells().length; ++i)
    {
      GenericTreeCellType cell = row.getCells()[i];

      treeRow.appendChild(addCell(cell, listHeader.get(i), row));
    }

    item.setValue(row);
    item.appendChild(treeRow);
  }

  private Treecell addCell(GenericTreeCellType data, final GenericTreeHeaderType header, final GenericTreeRowType rowData)
  {
    Treecell cell = null;

    if (data.getData() != null && data.getData() instanceof Treecell)
    {
      cell = (Treecell) data.getData();
    }
    else if (header.isComponent())
    {
      if (data.getData() instanceof Boolean)
      {
        // Checkbox
        cell = new Treecell();
        cell.appendChild(addCheckbox((Boolean) data.getData(), "", "", data, header, rowData));
      }
      else if (data.getData() instanceof Component)
      {
        cell = new Treecell();
        cell.appendChild((Component) data.getData());
      }

      /*if(addComponent(cell, data, header, rowData) == false)
       {
       cell = null;
       }*/
    }
    else if (data.getData() != null)
    {
      if (data.getData() instanceof Date)
      {
        cell = new Treecell();
        cell.appendChild(formatDate((Date) data.getData(), header.getDatatype()));
      }
    }

    if (cell == null)
    {
      cell = new Treecell();
      if (data.getData() != null)
        cell.setLabel(data.getData().toString());

      //updateCellEditing(cell, dataRow, data, header, false, rowData);
    }

    return cell;
  }

  private boolean addComponent(Treecell cell, GenericTreeCellType data,
          GenericTreeHeaderType header, GenericTreeRowType rowData)
  {
    //if (header.isComponent() == false)
    //  return false;

    cell = new Treecell();
    cell.disableClientUpdate(true);
    cell.getChildren().clear();

    //logger.debug("addComponent from Type: " + data.getData().getClass().getCanonicalName());

    Component comp = null;
    /*if (data.getData() instanceof String)
     {
     if (header.getDatatype() instanceof String[])
     {
     comp = addCombobox(data.getData().toString(), "", cell, dataRow, data, header, editing, rowData);
     }
     else
     {
     comp = addTextbox(data.getData().toString(), "", cell, dataRow, data, header, editing, rowData);
     }
     }
     else */
    if (data.getData() instanceof Boolean)
    {
      // Checkbox
      comp = addCheckbox((Boolean) data.getData(), "", "", data, header, rowData);
    }
    else if (data.getData() instanceof Date)
    {
      //comp = addDatebox((Date) data.getData(), "", data, header.isAllowInlineEditing(), cell, dataRow, data, header, editing, rowData);
      comp = formatDate((Date) data.getData(), header.getDatatype());
    }
    else if (data.getData() instanceof Component)
    {
      comp = (Component) data.getData();
    }
    else
    {
      if (data.getData() != null)
        logger.warn("Component nicht gefunden, zeige Label. Comp: " + data.getData().getClass().getCanonicalName());
      else
        logger.warn("Component nicht gefunden, zeige Label. Comp: null");

      return false;
    }


    if (comp != null)
      cell.appendChild(comp);

    cell.disableClientUpdate(false);
    cell.invalidate();

    return true;
  }

  private Component addCheckbox(boolean Checked, String Label, String Tooltip,
          final GenericTreeCellType data,
          final GenericTreeHeaderType header, final GenericTreeRowType rowData)
  {
    //Listcell cell = new Listcell();

    Checkbox item = new Checkbox(Label);
    item.setChecked(Checked);
    item.setAttribute("data", data);
    item.setTooltiptext(Tooltip);

    item.setDisabled(!header.isAllowInlineEditing());

    item.addEventListener(Events.ON_CHECK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("ON_CHANGE: " + Data.getClass().getCanonicalName());
        logger.debug("ON_CHANGE");
        Checkbox cb = (Checkbox) event.getTarget();
        data.setData(cb.isChecked());

        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), cb.isChecked(), rowData);
        }
      }
    });


    return item;


  }

  private Component formatDate(Date datum, Object datatype)
  {
    Label l = new Label();

    try
    {
      SimpleDateFormat sdf;
      if (datatype != null && datatype.toString().equalsIgnoreCase("datetime"))
      {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      }
      else
      {
        sdf = new SimpleDateFormat("dd.MM.yyyy");
      }

      l.setValue(sdf.format(datum));
    }
    catch (Exception e)
    {
    }



    return l;
  }

  /**
   * @param listHeader the listHeader to set
   */
  public void setListHeader(List<GenericTreeHeaderType> listHeader)
  {
    this.listHeader = listHeader;
  }

  /**
   * @param doubleClickEvent the doubleClickEvent to set
   */
  public void setDoubleClickEvent(IDoubleClick doubleClickEvent)
  {
    this.doubleClickEvent = doubleClickEvent;
  }

  /**
   * @param updateDataEvent the updateDataEvent to set
   */
  public void setUpdateDataEvent(IUpdateData updateDataEvent)
  {
    this.updateDataEvent = updateDataEvent;
  }
}
