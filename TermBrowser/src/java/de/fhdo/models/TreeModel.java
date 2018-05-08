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
package de.fhdo.models;

import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.helper.SessionHelper;
import java.util.Comparator;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeitemComparator;
import org.zkoss.zul.ext.Sortable;

/**
 *
 * @author mathias.aschhoff
 */
public class TreeModel extends DefaultTreeModel implements Sortable
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private DefaultTreeNode _root;
  private ContentCSVSDefault contentCSVSDefault;

  public TreeModel(DefaultTreeNode root)
  {
    super(root, true);
    _root = root;
  }

  public DefaultTreeNode get_root()
  {
    return this._root;
  }

  @Override
  public void sort(Comparator cmpr, final boolean ascending)
  {
    if (contentCSVSDefault != null)
    {
      logger.debug("SORTING!!!");
      
      if (cmpr instanceof TreeitemComparator)
      {
        // Sortierung festlegen
        TreeitemComparator tiComp = (TreeitemComparator) cmpr;
        if (tiComp.getTreecol().getColumnIndex() == 0)
        {
          // Name
          SessionHelper.setValue("SortByField", "term");
        }
        else if (tiComp.getTreecol().getColumnIndex() == 1)
        {
          // Code
          SessionHelper.setValue("SortByField", "code");
        }

        if (ascending)
          SessionHelper.setValue("SortDirection", "ascending");
        else
          SessionHelper.setValue("SortDirection", "descending");

        // Vokabular neu laden und anzeigen
        contentCSVSDefault.loadConceptsBySelectedItem(false, false);
      }
      else
        logger.debug(cmpr.toString());
    }
    else 
//        logger.debug("SORTING, null");   
        try{
            super.sort(cmpr, ascending); // TODO: hier kommt es zu einer Desktop == null Exception beim ersten Laden nach 24h?   
        } catch (Exception e){
            e.printStackTrace();
        }
  }

  /**
   * @param contentCSVSDefault the contentCSVSDefault to set
   */
  public void setContentCSVSDefault(ContentCSVSDefault contentCSVSDefault)
  {
    this.contentCSVSDefault = contentCSVSDefault;
  }
}