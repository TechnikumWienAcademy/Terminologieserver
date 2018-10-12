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
package de.fhdo.helper;

import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.models.TreeNode;
import java.util.Collection;
import java.util.Iterator;
import org.zkoss.zk.ui.Component;;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner, Sven Becker
 */
public class TreeHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void filterTree(String text, Tree tree)
  {
    logger.debug("Filter tree: " + tree.getId());
    text = text.toLowerCase();
    doCollapseExpandAll(tree, true);
    Iterator<Treeitem> it = tree.getItems().iterator();
    while (it.hasNext())
    {
      Treeitem ti = it.next();
      if(ti!=null && ti.getValue()!=null && ((TreeNode)ti.getValue()).getData()!=null)
      {
        Object data = ((TreeNode)ti.getValue()).getData();
        if(data instanceof CodeSystemVersion){     
            String s = ((CodeSystemVersion)data).getCodeSystem().getName() + " : " + ((CodeSystemVersion)data).getName();
            ti.setVisible(s.toLowerCase().contains(text));
            //ti.setVisible(((CodeSystemVersion)data).getName().toLowerCase().contains(text));
        }
        else if (data instanceof ValueSetVersion){
            String s = ((ValueSetVersion)data).getValueSet().getName() + " : " + ((ValueSetVersion)data).getVersionId();
            ti.setVisible(s.toLowerCase().contains(text));
        }
    }
      //filterTreeitem(text, ti);  // alte Version, bei der nach namen der Versionen gefiltert wurde
    }
  }
  
  public static void doCollapseExpandAll(Component component, boolean aufklappen){
      doCollapseExpandAll(component, aufklappen, null);
  }
  
  public static void doCollapseExpandAll(Component component, boolean aufklappen, ContentConcepts window)
  {
    if (component instanceof Treeitem)
    {
        
      Treeitem treeitem = (Treeitem) component;
      
      // replace dummy with real children
      if(aufklappen == true && window != null && ((TreeNode)treeitem.getValue()).getChildren().isEmpty() == false){
          TreeNode dummy = (TreeNode)((TreeNode)treeitem.getValue()).getChildren().get(0);
          if(dummy.getData() instanceof String)  {
              window.openNode((TreeNode)treeitem.getValue(), false); 
              window.updateModel(true);
          }          
      }
      treeitem.setOpen(aufklappen);     
    }
    Collection<?> com = component.getChildren();
    if (com != null)
    {
      for (Iterator<?> iterator = com.iterator(); iterator.hasNext();)
      {          
        doCollapseExpandAll((Component) iterator.next(), aufklappen, window);
      }
    }
  }
}


// Für Filterung nach Namen der Versionen, wird vorraussichtlich nicht mehr benötigt

//private static int filterTreeitem(String text, Treeitem treeitem)
//  {
//    boolean isLeaf = true;
//    String s = "";
//
//    int anzahlSub = 0;
//
//    Iterator it = treeitem.getChildren().iterator();
//    while (it.hasNext())
//    {
//      Object o = it.next();
//
//      if (o instanceof Treechildren)
//      {
//        isLeaf = false;
//        Treechildren tc = (Treechildren) o;
//
//        Iterator<Treeitem> it2 = tc.getChildren().iterator();
//        
//        while (it2.hasNext())
//        {
//          Treeitem ti = it2.next();
//          anzahlSub += filterTreeitem(text, ti);
//        }
//      }
//      else if (o instanceof Treerow)
//      {
//        // Wert lesen
//
//        Treerow tr = (Treerow) o;
//        //logger.debug("Treerow erkannt, Childs: " + tr.getChildren().size());
//        Iterator itTR = tr.getChildren().iterator();
//        while (itTR.hasNext())
//        {
//          Object trObject = itTR.next();
//          //logger.debug("Object: " + trObject.getClass().getCanonicalName());
//          if (trObject instanceof Treecell)
//          {
//            Treecell tc = (Treecell) trObject;
//            s = tc.getLabel();
//
//            if(tc.getChildren().size() > 0)
//            {
//              Label l = (Label)tc.getChildren().get(0);
//              s = l.getValue();
//            }
//          }
//        }
//      }
//    }
//
//    if (isLeaf && s.length() > 0)
//    {
//      // Blatt-Element, hier muss gefiltert werden
//      if(s.toLowerCase().contains(text))
//      {
//        anzahlSub++;
//        treeitem.setVisible(true);
//      }
//      else
//      {
//        // Das hier ausblenden
//        treeitem.setVisible(false);
//      }
//    }
//    else if(isLeaf == false && s.length() > 0)
//    {
//      treeitem.setVisible(anzahlSub > 0);
//    }
//
//    return anzahlSub;
//  }
//
//  private static String getTextFromData(Object data)
//  {
//    if (data instanceof CodeSystem)
//    {
//      return ((CodeSystem) data).getName();
//    }
//    else if (data instanceof CodeSystemVersion)
//    {
//      return ((CodeSystemVersion) data).getName();
//    }
//
//    return "";
//  }