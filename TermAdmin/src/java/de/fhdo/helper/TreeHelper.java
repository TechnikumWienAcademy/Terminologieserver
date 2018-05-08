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

import java.util.Collection;
import java.util.Iterator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Treeitem;

/**
 *
 * @author Robert Mützner
 */
public class TreeHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void doCollapseExpandAll(Component component, boolean aufklappen)
  {
    if (component == null)
      return;

    try
    {
      if (component instanceof Treeitem)
      {
        Treeitem treeitem = (Treeitem) component;
        if (treeitem != null)
          treeitem.setOpen(aufklappen);
      }
      Collection<?> com = component.getChildren();
      if (com != null)
      {
        for (Iterator<?> iterator = com.iterator(); iterator.hasNext();)
        {
          doCollapseExpandAll((Component) iterator.next(), aufklappen);

        }
      }
    }
    catch (Exception e)
    {
    }
  }

  /*public static void filterTree(String text, Tree tree)
  {
  logger.debug("Filter tree");
  
  text = text.toLowerCase();
  
  doCollapseExpandAll(tree, true);
  
  Iterator<Treeitem> it = tree.getItems().iterator();
  
  while (it.hasNext())
  {
  Treeitem ti = it.next();
  filterTreeitem(text, ti);
  }
  
  }*/

  /*private static int filterTreeitem(String text, Treeitem treeitem)
  {
  boolean isLeaf = true;
  String s = "";
  
  int anzahlSub = 0;
  
  Iterator it = treeitem.getChildren().iterator();
  while (it.hasNext())
  {
  Object o = it.next();
  
  if (o instanceof Treechildren)
  {
  isLeaf = false;
  Treechildren tc = (Treechildren) o;
  
  Iterator<Treeitem> it2 = tc.getChildren().iterator();
  
  while (it2.hasNext())
  {
  Treeitem ti = it2.next();
  anzahlSub += filterTreeitem(text, ti);
  }
  }
  else if (o instanceof Treerow)
  {
  // Wert lesen
  Treerow tr = (Treerow) o;
  
  Iterator itTR = tr.getChildren().iterator();
  while (itTR.hasNext())
  {
  Object trObject = itTR.next();
  
  if (trObject instanceof Treecell)
  {
  Treecell tc = (Treecell) trObject;
  s = tc.getLabel();
  
  if(tc.getChildren().size() > 0)
  {
  Label l = (Label)tc.getChildren().get(0);
  s = l.getValue();
  }
  }
  }
  }
  }
  
  if (isLeaf && s.length() > 0)
  {
  // Blatt-Element, hier muss gefiltert werden
  if(s.toLowerCase().contains(text))
  {
  anzahlSub++;
  treeitem.setVisible(true);
  }
  else
  {
  // Das hier ausblenden
  treeitem.setVisible(false);
  }
  }
  else if(isLeaf == false && s.length() > 0)
  {
  treeitem.setVisible(anzahlSub > 0);
  }
  
  return anzahlSub;
  }*/
}
