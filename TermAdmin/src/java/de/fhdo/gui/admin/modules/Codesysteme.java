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

import de.fhdo.db.Definitions;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.DomainValue;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeCellType;
import de.fhdo.tree.GenericTreeHeaderType;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IUpdateData;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;

/**
 *
 * @author Robert M�tzner
 */
public class Codesysteme extends Window implements AfterCompose, IGenericListActions, IUpdateData
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  private GenericTree genericTree;
  private long selectedCodesystemId;

  public Codesysteme()
  {
    selectedCodesystemId = 0;
    
    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }
  }

  private GenericListRowType createRowFromCodesystem(de.fhdo.db.hibernate.CodeSystem codeSystem)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[1];
    cells[0] = new GenericListCellType(codeSystem.getName(), false, "");

    row.setData(codeSystem);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from CodeSystem order by name";
      List<de.fhdo.db.hibernate.CodeSystem> csList = hb_session.createQuery(hql).list();

      for (int i = 0; i < csList.size(); ++i)
      {
        de.fhdo.db.hibernate.CodeSystem cs = csList.get(i);
        GenericListRowType row = createRowFromCodesystem(cs);

        dataList.add(row);
      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
        //hb_session.getTransaction().rollback();
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);



    //genericList.setDataList(null);
    //genericList.set


  }

  private GenericTreeRowType createTreeRowFromDomainValue(de.fhdo.db.hibernate.DomainValue domainValue, boolean Zugeordnet, long CodesystemId)
  {
    GenericTreeRowType row = new GenericTreeRowType();

    GenericTreeCellType[] cells = new GenericTreeCellType[3];
    //cells[0] = new GenericTreeCellType(domainValue.getDomainValueId(), false, "");
    cells[0] = new GenericTreeCellType(Zugeordnet, false, "");
    cells[1] = new GenericTreeCellType(domainValue.getDomainCode(), false, "");
    cells[2] = new GenericTreeCellType(domainValue.getDomainDisplay(), false, "");
    //cells[3] = new GenericTreeCellType(domainValue.getOrderNo(), false, "");
    //cells[4] = new GenericTreeCellType(domainValue.getImageFile(), false, "");

    row.setData(domainValue);
    row.setCells(cells);

    if (domainValue.getDomainValuesForDomainValueId2() != null)
    {
      Iterator<DomainValue> dvIt = domainValue.getDomainValuesForDomainValueId2().iterator();

      while (dvIt.hasNext())
      {
        DomainValue dvChild = dvIt.next();

        boolean zugeordnet = false;
        if (dvChild.getCodeSystems() != null)
        {
          Iterator<de.fhdo.db.hibernate.CodeSystem> it = dvChild.getCodeSystems().iterator();
          while (it.hasNext())
          {
            de.fhdo.db.hibernate.CodeSystem cs = it.next();
            if (cs.getId().longValue() == CodesystemId)
            {
              zugeordnet = true;
              break;
            }
          }

        }

        row.getChildRows().add(createTreeRowFromDomainValue(dvChild, zugeordnet, CodesystemId));
      }
    }

    return row;
  }

  private void initDomainValueTree(long DomainID, long CodeSystemId)
  {
    logger.debug("initDomainValueTree: " + DomainID);
    selectedCodesystemId = CodeSystemId;

    // Header
    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType("Zugeordnet", 100, "", true, "Bool", true, false, true));
    header.add(new GenericTreeHeaderType("Code", 140, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType("Anzeige-Text", 400, "", true, "String", false, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();
    try
    {
      String hql = "from DomainValue where domainId=" + DomainID + " order by orderNo,domainDisplay";
      List<de.fhdo.db.hibernate.DomainValue> dvList = hb_session.createQuery(hql).list();

      for (int i = 0; i < dvList.size(); ++i)
      {
        de.fhdo.db.hibernate.DomainValue domainValue = dvList.get(i);

        if (domainValue.getDomainValuesForDomainValueId1() == null
                || domainValue.getDomainValuesForDomainValueId1().size() == 0)
        {
          // Nur root-Elemente auf oberster Ebene
          boolean zugeordnet = false;
          if (domainValue.getCodeSystems() != null)
          {
            Iterator<de.fhdo.db.hibernate.CodeSystem> it = domainValue.getCodeSystems().iterator();
            while (it.hasNext())
            {
              de.fhdo.db.hibernate.CodeSystem cs = it.next();
              if (cs.getId().longValue() == CodeSystemId)
              {
                zugeordnet = true;
                break;
              }
            }

          }
          GenericTreeRowType row = createTreeRowFromDomainValue(domainValue, zugeordnet, CodeSystemId);

          dataList.add(row);
          //logger.debug("DV: " + domainValue.getDomainCode());
        }
        //else
        //  logger.debug("kein Child DV: " + domainValue.getDomainCode() + ", " + domainValue.getDomainValuesForDomainValueId1().size());



      }

      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initDomainValueList(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
        hb_session.close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incTree");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");

    genericTree = (GenericTree) winGenericTree;
    //genericListValues.setUserDefinedId("2");

    //genericTree.setTreeActions(this);
    genericTree.setUpdateDataListener(this);
    genericTree.setButton_new(false);
    genericTree.setButton_edit(false);
    genericTree.setButton_delete(false);
    genericTree.setListHeader(header);
    genericTree.setDataList(dataList);


  }

  public void afterCompose()
  {
    initList();
    //initDomainValueTree(Definitions.DOMAINID_CODESYSTEM_TAXONOMY);
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
    // Details ausw�hlen
    if(data != null)
      logger.debug("onSelected: " + data.getClass().getCanonicalName());
    else logger.debug("onSelected: null");
    
    if (data != null && data instanceof de.fhdo.db.hibernate.CodeSystem)
    {
      //de.fhdo.db.hibernate.CodeSystem cs = (de.fhdo.db.hibernate.CodeSystem) ((GenericListRowType)data).getData();
      de.fhdo.db.hibernate.CodeSystem cs = (de.fhdo.db.hibernate.CodeSystem) data;
      initDomainValueTree(Definitions.DOMAINID_CODESYSTEM_TAXONOMY, cs.getId());
    }
  }

  public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row)
  {
    logger.debug("onCellUpdated, index: " + cellIndex + ", data: " + data);
    logger.debug("selectedCodesystemId: " + selectedCodesystemId);
    
    Boolean zugeordnet = (Boolean)data;
    
    // DB aktualisieren
    DomainValue dv = (DomainValue) row.getData();
    logger.debug("dvid: " + dv.getDomainValueId());
    
    
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      //String hql = "from CodeSystem order by name";
      DomainValue dv_db =  (DomainValue) hb_session.get(DomainValue.class, dv.getDomainValueId());
      
      boolean existiert = false;
      
      if(dv_db.getCodeSystems() != null)
      {
        Iterator<de.fhdo.db.hibernate.CodeSystem> it = dv_db.getCodeSystems().iterator();
        while(it.hasNext())
        {
          de.fhdo.db.hibernate.CodeSystem cs_db = it.next();
          if(cs_db.getId().longValue() == selectedCodesystemId)
          {
            existiert = true;
            
            // bereits vorhanden
            if(zugeordnet == false)
            {
              // TODO Verbindung l�schen
              logger.debug("Verbindung wird entfernt...");
              dv_db.getCodeSystems().remove(cs_db);
              hb_session.update(dv_db);
              hb_session.getTransaction().commit();
            }
            
            break;
          }
        }
      }
      
      if(existiert == false && zugeordnet)
      {
        // Verbindung hinzuf�gen
        logger.debug("Verbindung wird hinzugef�gt...");
        if(dv_db.getCodeSystems() == null)
          dv_db.setCodeSystems(new HashSet<de.fhdo.db.hibernate.CodeSystem>());
        
        //de.fhdo.db.hibernate.CodeSystem insertCS = new de.fhdo.db.hibernate.CodeSystem();
        //insertCS.setId(selectedCodesystemId);
        de.fhdo.db.hibernate.CodeSystem insertCS = (de.fhdo.db.hibernate.CodeSystem) hb_session.get(de.fhdo.db.hibernate.CodeSystem.class, selectedCodesystemId);
        dv_db.getCodeSystems().add(insertCS);
        
        hb_session.save(dv_db);
        
        hb_session.getTransaction().commit();
      }
      
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
}
