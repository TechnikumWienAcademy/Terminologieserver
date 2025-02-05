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
import de.fhdo.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.db.hibernate.ConceptValueSetMembership;
import de.fhdo.db.hibernate.MetadataParameter;
import de.fhdo.db.hibernate.ValueSet;
import de.fhdo.db.hibernate.ValueSetMetadataValue;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert M�tzner
 */
public class MetadatenVS extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  GenericList genericListVocs;
  GenericList genericListMetadata;
  ValueSet selectedValueSet;
  MetadataParameter selectedMetadataParameter;
  Label labelInfoChange;
  Label labelInfoParamName;
  Textbox tbParamName;

  public MetadatenVS()
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
            
                if(at.getClassname().equals("ValueSet")){
                
                    ValueSet vs = (ValueSet)hb_session.get(ValueSet.class, at.getClassId());
                    GenericListRowType row = createRowFromValueSet(vs);
                    dataList.add(row);
                }
            }
            Collections.sort(dataList,new ComparatorRowTypeName(true));
        }else{
        
            String hql = "from ValueSet order by name";
            List<ValueSet> csList = hb_session.createQuery(hql).list();

            for (int i = 0; i < csList.size(); ++i)
            {
              ValueSet vs = csList.get(i);
              GenericListRowType row = createRowFromValueSet(vs);

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
  
  public void fillMetadataList()
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
        hql += " join fetch mp.valueSet vs";
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("vs.", "id", selectedValueSet.getId());

        // Parameter hinzuf�gen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        hql += " order by mp.paramName";
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
        parameterHelper.applyParameter(q);
        
        List<MetadataParameter> mpList = q.list();
        
        /*if(mpList.isEmpty()){
            ((Label) getFellow("labelStatus")).setValue("Keine Metadaten Parameter zu dieser Liste vorhanden!");
        }*/

        for (int i = 0; i < mpList.size(); ++i)
        {
          MetadataParameter mp = mpList.get(i);
          GenericListRowType row = createRowFromMetadataParameter(mp);

          dataList.add(row);
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

  private GenericListRowType createRowFromValueSet(ValueSet vs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(vs.getId(), false, "");
    cells[1] = new GenericListCellType(vs.getName(), false, "");

    row.setData(vs);
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
        
      if (data instanceof ValueSet)
      {
        selectedValueSet = (ValueSet) data;
        logger.debug("Selected ValueSet: " + selectedValueSet.getName());
        fillMetadataList();
        
        showStatus();
      }
      else if(data instanceof MetadataParameter)
      {
          selectedMetadataParameter = (MetadataParameter) data;
          logger.debug("Selected Metadata Parameter: " + selectedMetadataParameter.getParamName());
          
          showStatus();
      }
      else{
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
            map.put("valueset_id", selectedValueSet.getId());
            map.put("MetadatenVS",this);

            Window win = (Window) Executions.createComponents(
                    "/gui/admin/modules/metadatenDetailsVS.zul", null, map);

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
            map.put("valueset_id", selectedValueSet.getId());
            map.put("mp", mp);
            map.put("MetadatenVS",this);

            Window win = (Window) Executions.createComponents(
                    "/gui/admin/modules/metadatenDetailsVS.zul", null, map);

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
          MetadataParameter mp = (MetadataParameter)data;
          MetadataParameter mp_db = (MetadataParameter)hb_session.get(MetadataParameter.class,mp.getId());
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

    if (selectedValueSet == null)
    {
      s = "\nBitte w�hlen Sie ein Codesystem aus.";
    }
    
    if(selectedValueSet != null){
        s = "\nJetzt k�nnen Sie einen Metadaten Parameter ausw�hlen.";
    }
    if(selectedMetadataParameter != null){
        s= "\nMetadaten Parameter ausgew�hlt."; 
    }*/

    //((Label) getFellow("labelStatus")).setValue(s);
  }

  public void afterCompose()
  {

    fillVocabularyList();
    showStatus();
  }
}
