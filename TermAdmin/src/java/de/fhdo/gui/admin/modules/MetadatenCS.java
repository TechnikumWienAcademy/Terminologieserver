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

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.db.hibernate.MetadataParameter;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import de.fhdo.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert M�tzner
 */
public class MetadatenCS extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  GenericList genericListVocs;
  GenericList genericListMetadata;
  CodeSystem selectedCodeSystem;
  MetadataParameter selectedMetadataParameter;
  

  public MetadatenCS()
  {
  }

  private void fillVocabularyList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        
          if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
            
            ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();
         
            for(AssignedTerm at:myTerms){
            
                if(at.getClassname().equals("CodeSystem")){
                
                    CodeSystem cs = (CodeSystem)hb_session.get(CodeSystem.class, at.getClassId());
                    GenericListRowType row = createRowFromCodesystem(cs);
                    dataList.add(row);
                }
            }
            Collections.sort(dataList,new ComparatorRowTypeName(true));
        }else{

            String hql = "from CodeSystem order by name";
            List<CodeSystem> csList = hb_session.createQuery(hql).list();

            for (int i = 0; i < csList.size(); ++i)
            {
              CodeSystem cs = csList.get(i);
              GenericListRowType row = createRowFromCodesystem(cs);

              dataList.add(row);
            }
        }
        //tx.commit();
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      //Vokabular Liste
      Include inc = (Include) getFellow("incVocList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListVocs = (GenericList) winGenericList;
      //genericListVocs.setId("0");

      genericListVocs.setListActions(this);
      genericListVocs.setButton_new(false);
      genericListVocs.setButton_edit(false);
      genericListVocs.setButton_delete(false);
      genericListVocs.setListHeader(header);
      genericListVocs.setDataList(dataList);
    }
    catch (Exception ex)
    {
      logger.error("Fehler in MetadatenCS.java: " + ex.getMessage());
    }
  }

  private void fillMetadataList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        String hql = "from MetadataParameter mp";
        hql += " join fetch mp.codeSystem cs";
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("cs.", "id", selectedCodeSystem.getId());

        // Parameter hinzuf�gen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        hql += " order by mp.paramName";
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
        parameterHelper.applyParameter(q);

        List<MetadataParameter> mpList = q.list();

        /*if (mpList.size() == 0)
        {
          ((Label) getFellow("labelStatus")).setValue("Keine Metadaten Parameter zu dieser Liste vorhanden!");
        }*/

        for (int i = 0; i < mpList.size(); ++i)
        {
          MetadataParameter mp = mpList.get(i);
          GenericListRowType row = createRowFromMetadataParameter(mp);

          dataList.add(row);
        }
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      //Metadaten Liste
      Include incMeta = (Include) getFellow("incMetadataList");
      Window winGenericListMeta = (Window) incMeta.getFellow("winGenericList");
      genericListMetadata = (GenericList) winGenericListMeta;
      genericListMetadata.setListId("metadataList");

      genericListMetadata.setListActions(this);
      genericListMetadata.setButton_new(true);
      genericListMetadata.setButton_edit(true);
      genericListMetadata.setButton_delete(true);
      genericListMetadata.setListHeader(header);
      genericListMetadata.setDataList(dataList);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }

  private GenericListRowType createRowFromMetadataParameter(MetadataParameter mp)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(mp.getId(), false, "");
    cells[1] = new GenericListCellType(mp.getParamName(), false, "");

    row.setData(mp);
    row.setCells(cells);

    return row;
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
    {

      if (data instanceof CodeSystem)
      {
        selectedCodeSystem = (CodeSystem) data;
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());
        fillMetadataList();
        
        showStatus();
      }
      else if (data instanceof MetadataParameter)
      {
        selectedMetadataParameter = (MetadataParameter) data;
        logger.debug("Selected Metadata Parameter: " + selectedMetadataParameter.getParamName());

        showStatus();
      }
      else
      {
        logger.debug("data: " + data.getClass().getCanonicalName());
      }
    }
  }

  public void onNewClicked(String id)
  {
    try
    {
      if (id != null && id.equals("metadataList"))
      {
        Map map = new HashMap();
        map.put("codesystem_id", selectedCodeSystem.getId());
          
        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/metadatenDetails.zul", null, map);

        ((MetadatenDetails) win).setUpdateListInterface(this);
        win.doModal();
      }
      
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der MetadatenDetails: " + ex.getLocalizedMessage());
    }
    
  }

  public void onEditClicked(String id, Object data)
  {
    try
    {
      if (id != null && id.equals("metadataList"))
      {
        MetadataParameter mp = (MetadataParameter) data ;
        
        Map map = new HashMap();
        map.put("codesystem_id", selectedCodeSystem.getId());
        map.put("mp", mp);
          
        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/metadatenDetails.zul", null, map);

        ((MetadatenDetails) win).setUpdateListInterface(this);
        win.doModal();
      }
      
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der MetadatenDetails: " + ex.getLocalizedMessage());
    }
  }

  public void onDeleted(String id, Object data)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    try
    {
      MetadataParameter mp = (MetadataParameter) data;
      MetadataParameter mp_db = (MetadataParameter) hb_session.get(MetadataParameter.class, mp.getId());
      hb_session.delete(mp_db);

      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }

  private void showStatus()
  {
    /*String s = "";

    if (selectedCodeSystem == null)
    {
      s = "\nBitte w�hlen Sie ein Codesystem aus.";
    }

    if (selectedCodeSystem != null)
    {
      s = "\nJetzt k�nnen Sie einen Metadaten Parameter ausw�hlen.";
    }
    if (selectedMetadataParameter != null)
    {
      s = "\nMetadaten Parameter ausgew�hlt.";
    }*/

    //((Label) getFellow("labelStatus")).setValue(s);
  }

  public void afterCompose()
  {
    fillVocabularyList();
    showStatus();
  }

  public void update(Object o, boolean edited)
  {
    if(o != null && o instanceof MetadataParameter)
    {
      GenericListRowType row = createRowFromMetadataParameter((MetadataParameter)o);
      if(edited)
        genericListMetadata.updateEntry(row);
      else genericListMetadata.addEntry(row);
    }
  }
}
