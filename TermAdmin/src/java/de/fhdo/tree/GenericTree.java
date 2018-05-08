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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.South;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class GenericTree extends Window implements IDoubleClick, IUpdateData
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<GenericTreeRowType> dataList;
  private List<GenericTreeHeaderType> listHeader;
  private GenericTreeItemRenderer treeitemRenderer;
  private TreeModel treeModel;
  //private ListModelList listModelFilter;
  private IGenericTreeActions treeActions;
  private boolean button_new;
  private boolean button_edit;
  private boolean button_delete;
  //private GenericTreeItemRenderer listitemRenderer;
  //private IGenericTreeActions treeActions;
  private IUpdateData updateDataListener;
  //private int lastSelectedIndex;
  private Treeitem lastSelectedTreeitem;
  private boolean oneFilter;
  private List<Button> customButtonList;
  private String listId;

  public GenericTree()
  {
    listHeader = new LinkedList<GenericTreeHeaderType>();
    //lastSelectedIndex = -1;
    oneFilter = false;
    customButtonList = new LinkedList<Button>();
  }

  public void onTreeSelected(SelectEvent event)
  {
    Object o = getSelection();
    showButtonMode(o);

    if (treeActions != null)
    {
      treeActions.onTreeSelected(listId, o);
    }
  }

  private void showButtonMode()
  {
    Object o = getSelection();
    showButtonMode(o);
  }

  private void showButtonMode(Object o)
  {
    try
    {
      boolean disabled = (o == null);

      ((Button) getFellow("buttonEdit")).setDisabled(disabled);
      ((Button) getFellow("buttonDelete")).setDisabled(disabled);

      ((Button) getFellow("buttonNew")).setDisabled(false);

      for (int i = 0; i < customButtonList.size(); ++i)
      {
        customButtonList.get(i).setDisabled(disabled);
      }
    }
    catch (Exception e)
    {
      logger.error("Fehler in showButtonMode(): " + e.getLocalizedMessage());
    }
  }

  public Object getSelection()
  {
    //logger.debug("getSelection()");

    Object object = null;

    try
    {
      Tree tree = (Tree) getFellow("generictree");

      Treeitem ti = tree.getSelectedItem();
      if (ti != null)
      {
        object = ti.getValue();
      }
      lastSelectedTreeitem = ti;
    }
    catch (Exception ex)
    {
      logger.warn("[GenericList.java] getSelection-Fehler: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }

    return object;
  }

  /**
   * @return the dataList
   */
  public List<GenericTreeRowType> getDataList()
  {
    return dataList;
  }

  /**
   * @param dataList the dataList to set
   */
  public void setDataList(List<GenericTreeRowType> dataList)
  {
    this.dataList = dataList;
    createTreeModel();
  }

  private List<TreeNode> createTreeNodeList(List<GenericTreeRowType> dataList)
  {
    List<TreeNode> list = new ArrayList<TreeNode>();

    for (int i = 0; i < dataList.size(); ++i)
    {
      GenericTreeRowType row = dataList.get(i);
      if (row.getChildRows().size() == 0)
      {
        //logger.debug("addLeaf: " + ((GenericTreeCellType)row.getCells()[0]).getData());
        list.add(new DefaultTreeNode(row));
      }
      else
      {
        //logger.debug("addTree: " + ((GenericTreeCellType)row.getCells()[0]).getData());
        list.add(new DefaultTreeNode(row, createTreeNodeList(row.getChildRows())));
      }
      //TreeNode treeNode = new DefaultTreeNode()
      //list.add(new DefaultTreeNode(item, ));

    }

    return list;
  }

  private void createTreeModel()
  {
    logger.debug("createTreeModel()");

    /*
     List<TreeNode> dataList = new LinkedList<TreeNode>();
     List<TreeNode> subList = new LinkedList<TreeNode>();
     subList.add(new DefaultTreeNode(createRow(new Organisation("FB 1", "", true, null))));
     subList.add(new DefaultTreeNode(createRow(new Organisation("FB 2", "", true, null))));
     //TreeNode tn1 = new DefaultTreeNode(tn, subList);
    
     GenericTreeRowType row = createRow(new Organisation("FH Dortmund", "Dortmund", true, new Date(88,4,14)));
     TreeNode tn = new DefaultTreeNode(row, subList);
    
     dataList.add(tn);
     dataList.add(new DefaultTreeNode(createRow(new Organisation("Westfalenstadion", "Dortmund", false, new Date(68,4,14)))));
     */

    

    //TreeNode root = new DefaultTreeNode(root)
    //TreeNode tnRoot = new DefaultTreeNode(null, dataList); 
    TreeNode tnRoot = new DefaultTreeNode(null, createTreeNodeList(dataList));
    //TreeNode tnRoot = new DefaultTreeNode(dataList); 
    treeModel = new DefaultTreeModel(tnRoot);

    Tree tree = (Tree) getFellow("generictree");
    tree.setModel(treeModel);

    if (treeitemRenderer == null)
    {
      treeitemRenderer = new GenericTreeItemRenderer(listHeader);
      treeitemRenderer.setDoubleClickEvent(this);
      treeitemRenderer.setUpdateDataEvent(this);
      tree.setItemRenderer(treeitemRenderer);
    }


    showButtonMode();
    /*

     Listbox lb = (Listbox) getFellow("listbox");
     lb.setModel(listModel);

     if (listitemRenderer == null)
     {
     listitemRenderer = new GenericListItemRenderer(listHeader);
     listitemRenderer.setDoubleClickEvent(this);
     listitemRenderer.setUpdateDataEvent(this);
     lb.setItemRenderer(listitemRenderer);
     }

     showButtonMode();*/
  }

  private void setSouthHeight()
  {
    South south = (South) getFellow("south");

    Div div = (Div) getFellow("divEditButtons");
    if (div.isVisible())
    {
      south.setSize("33px");
      south.setVisible(true);
    }
    else
    {
      south.setSize("0px");
      south.setVisible(false);
    }
    //south
  }

  /**
   * @param button_new the button_new to set
   */
  public void setButton_new(boolean button_new)
  {
    this.button_new = button_new;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonNew");
    button.setVisible(button_new);
  }

  /**
   * @param button_edit the button_edit to set
   */
  public void setButton_edit(boolean button_edit)
  {
    this.button_edit = button_edit;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonEdit");
    button.setVisible(button_edit);
  }

  /**
   * @param button_delete the button_delete to set
   */
  public void setButton_delete(boolean button_delete)
  {
    this.button_delete = button_delete;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonDelete");
    button.setVisible(button_delete);
  }

  /**
   * @param listHeader the listHeader to set
   */
  public void setListHeader(List<GenericTreeHeaderType> listHeader)
  {
    oneFilter = false;
    this.listHeader = listHeader;

    // Header zuweisen
    Treecols treecols = (Treecols) getFellow("treecols");
    
    //Listhead listhead = (Listhead) getFellow("listHeader");
    //listhead.getChildren().clear();
    if (treecols != null)
    {
      treecols.getChildren().clear();
      
      // Bestimmen, ob es einen Filter gibt
      /*for (int i = 0; i < listHeader.size(); ++i)
       {
       if (listHeader.get(i).isShowFilter())
       {
       oneFilter = true;
       break;
       }
       }*/

      for (int i = 0; i < listHeader.size(); ++i)
      {
        listHeader.get(i).setIndex(i);
        final GenericTreeHeaderType head = listHeader.get(i);

        Treecol treecol = new Treecol(head.getName());
        treecol.setImage(head.getImage());
        if (head.getWidth() > 0)
          treecol.setWidth(head.getWidth() + "px");

        //treecol.set
        
        // TODO weitere Filter einfügen
        /*if (head.isShowFilter())
         else*/
        /*{
         // Größe anpassen ohne Filter
         logger.debug("oneFilter: " + oneFilter);
         if (oneFilter)
         {
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         }
         }*/

        treecols.getChildren().add(treecol);
      }
    }

    if (treeitemRenderer != null)
    {
      treeitemRenderer.setListHeader(listHeader);
    }
  }

  public void onNew() throws InterruptedException
  {
    lastSelectedTreeitem = null;

    if (getTreeActions() != null)
    {
      getTreeActions().onTreeNewClicked(listId, null);
    }
  }

  public void onNewSubentry()
  {
    logger.debug("onNewSubentry()");
    if (getTreeActions() != null)
    {
      Object o = getSelection();

      if (o != null)
      {
        getTreeActions().onTreeNewClicked(listId, ((GenericTreeRowType) o).getData());
      }
      else
      {
        Clients.alert("Keine Auswahl! Bitte wählen Sie einen Eintrag aus der Liste aus");
      }

    }
  }

  public void onDoubleClick(Object o)
  {
    try
    {
      onEdit();
    }
    catch (InterruptedException ex)
    {
      logger.error(ex.getLocalizedMessage());
    }
  }

  public void onEdit() throws InterruptedException
  {
    logger.debug("onEdit()");

    if (getTreeActions() != null)
    {
      Object o = getSelection();

      if (o != null)
      {
        getTreeActions().onTreeEditClicked(listId, ((GenericTreeRowType) o).getData());
      }
      else
      {
        Clients.alert("Keine Auswahl! Bitte wählen Sie einen Eintrag aus der Liste aus");
      }
    }
  }

  public void onDelete() throws InterruptedException
  {
    if (getTreeActions() != null)
    {
      try
      {
        // Dialog + Eintrag löschen
        if (logger.isDebugEnabled())
          logger.debug("onDeleteClicked");

        Object o = getSelection();

        if (o != null)
        {
          if (Messagebox.show("Möchten Sie den ausgewählten Eintrag wirklich löschen?", "Löschen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES)
          {
            // Liste dynamisch erneuern
            //Tree tree = (Tree) getFellow("generictree");
            //Treeitem ti = tree.getSelectedItem();

            //Treeitem parent = ti.getParentItem();
            //logger.debug("Anzahl Kinder: " + parent.getChildren().size());
            //ti.detach();

            DefaultTreeNode selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");
            TreeNode parentNode = selectedTreeNode.getParent();
            logger.debug("Anzahl Node-Kinder: " + parentNode.getChildCount());
            if (parentNode.getChildCount() > 1)
            {
              parentNode.remove(selectedTreeNode);
            }
            else
            {
              // Sonderfall (!)
              Tree tree = (Tree) getFellow("generictree");
              Treeitem ti = tree.getSelectedItem();

              ti.detach();
              parentNode.remove(selectedTreeNode);
            }
            
            treeActions.onTreeDeleted(listId, ((GenericTreeRowType) o).getData());
          }
          else
            logger.info("Nein geklickt");
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage());
        
        ex.printStackTrace();
      }
    }
  }

  public void addEntry(GenericTreeRowType row, boolean rootElement)
  {
    logger.debug("addEntry(): " + row.getClass().getCanonicalName() + ", root: " + rootElement);

    DefaultTreeNode selectedTreeNode = null;
    if (rootElement || lastSelectedTreeitem == null)
    {
      selectedTreeNode = (DefaultTreeNode) treeModel.getRoot();
      selectedTreeNode.add(new DefaultTreeNode(row));
    }
    else
    {
      selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");

      if (selectedTreeNode.isLeaf())
      {
        // Besonderer Fall, da es noch kein Subelement gibt
        // hier muss der TreeNode erneut mit Kindern angelegt werden

        // Aufbau ZUL
        // Tree 
        //  -> Treechildren 
        //     -> Treeitem 
        //        -> Treerow
        //           -> Component
        //        -> Treechildren
        //           -> Treeitem
        //              -> Treerow

        // Aufbau Model
        // Root
        // -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        // -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        // -> (Default)TreeNode

        List<TreeNode> children = new LinkedList<TreeNode>();
        children.add(new DefaultTreeNode(row));

        TreeNode node = new DefaultTreeNode(lastSelectedTreeitem.getValue(), children);  // selectedTreeNode

        TreeNode parent = selectedTreeNode.getParent();
        int index = parent.getIndex(selectedTreeNode);
        parent.remove(selectedTreeNode);
        parent.insert(node, index);
      }
      else
      {
        selectedTreeNode.add(new DefaultTreeNode(row));
      }

    }
  }

  public void updateEntry(GenericTreeRowType row)
  {
    logger.debug("updateEntry(): " + row.getClass().getCanonicalName());

    /*DefaultTreeNode selectedTreeNode = null;
     selectedTreeNode = (DefaultTreeNode) treeModel.getRoot();
    
     selectedTreeNode.add(new DefaultTreeNode(row));*
     */

    if (lastSelectedTreeitem != null)
    {
      // Center -> Tree -> Treechildren -> Treeitem
      //logger.debug("Class1: " +lastSelectedTreeitem.getParent().getClass().getCanonicalName());
      //logger.debug("Class2: " +lastSelectedTreeitem.getParent().getParent().getClass().getCanonicalName());
      //logger.debug("Class3: " +lastSelectedTreeitem.getParent().getParent().getParent().getClass().getCanonicalName());

      // lastSelectedTreeitem.getValue() ist de.fhdo.tree.GenericTreeRowType
      DefaultTreeNode selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");
      selectedTreeNode.setData(row);
    }
    else
      logger.debug("lastSelectedTreeitem ist null");
    /*if (lastSelectedIndex >= 0)
     {
     listModel.remove(lastSelectedIndex);
     listModel.add(lastSelectedIndex, row);
     }*/
    //treeModel.

    //getSelection()

  }
  
  public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row)
  {
    if (updateDataListener != null)
      updateDataListener.onCellUpdated(cellIndex, data, row);
  }

  /**
   * @return the treeActions
   */
  public IGenericTreeActions getTreeActions()
  {
    return treeActions;
  }

  /**
   * @param treeActions the treeActions to set
   */
  public void setTreeActions(IGenericTreeActions treeActions)
  {
    this.treeActions = treeActions;
  }

  /**
   * @param updateDataListener the updateDataListener to set
   */
  public void setUpdateDataListener(IUpdateData updateDataListener)
  {
    this.updateDataListener = updateDataListener;
  }
}
