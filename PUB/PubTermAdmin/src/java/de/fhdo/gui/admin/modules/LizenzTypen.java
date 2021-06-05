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

import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.CodeSystemVersion;
import de.fhdo.db.hibernate.LicenceType;
import de.fhdo.db.hibernate.LicencedUser;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.InputMessageBox;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert M�tzner
 */
public class LizenzTypen extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdate updateInterface;
  GenericList genericList;
  long codeSystemVersionId;

  public LizenzTypen()
  {
    codeSystemVersionId = ArgumentHelper.getWindowArgumentLong("csv_id");
  }

  public void afterCompose()
  {
    initList();
  }

  public void initList()
  {
    logger.debug("F�lle Liste...");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Lizenztyp", 0, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from LicenceType where codeSystemVersionId=" + codeSystemVersionId + " order by typeTxt";

      List<LicenceType> ltList = hb_session.createQuery(hql).list();

      logger.debug("Anzahl: " + ltList.size());

      for (int i = 0; i < ltList.size(); ++i)
      {
        GenericListRowType row = createRow(ltList.get(i));
        dataList.add(row);
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
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
    genericList.setButton_new(true);
    genericList.setButton_edit(true);
    genericList.setButton_delete(true);

    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  private GenericListRowType createRow(LicenceType licenceType)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[1];
    cells[0] = new GenericListCellType(licenceType.getTypeTxt(), false, "");
    //cells[1] = new GenericListCellType(licenceType.getName(), false, "");

    row.setData(licenceType);
    row.setCells(cells);

    return row;
  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdate updateInterface)
  {
    this.updateInterface = updateInterface;
  }

  public void onNewClicked(String id)
  {
    try
    {
      String ltTxt = (String) InputMessageBox.showInput("Lizenztyp:", "Neuer Lizenztyp");
      logger.debug("Neuer Lizenztyp: " + ltTxt);

      if (ltTxt != null && ltTxt.length() > 0)
      {

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try
        {
          LicenceType lt = new LicenceType();
          lt.setCodeSystemVersion(new CodeSystemVersion());
          lt.getCodeSystemVersion().setVersionId(codeSystemVersionId);

          lt.setTypeTxt(ltTxt);

          hb_session.save(lt);

          hb_session.getTransaction().commit();

          genericList.addEntry(createRow(lt));
        }
        catch (Exception e)
        {
          hb_session.getTransaction().rollback();
          initList();
        }
        finally
        {
          hb_session.close();
        }
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  public void onEditClicked(String id, Object data)
  {
    try
    {
      LicenceType lt = (LicenceType) data;

      String ltTxt = (String) InputMessageBox.showInput("Lizenztyp:", "Lizenztyp �ndern", lt.getTypeTxt());
      logger.debug("Lizenztyp: " + ltTxt);

      if (ltTxt != null && ltTxt.length() > 0)
      {
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try
        {
          LicenceType lt_db = (LicenceType)hb_session.get(LicenceType.class, lt.getId());
          lt_db.setTypeTxt(ltTxt);
          hb_session.update(lt_db);

          hb_session.getTransaction().commit();

          genericList.updateEntry(createRow(lt_db));
        }
        catch (Exception e)
        {
          hb_session.getTransaction().rollback();
          initList();
        }
        finally
        {
          hb_session.close();
        }
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof LicenceType)
    {
      LicenceType licenceType = (LicenceType) data;
      logger.debug("LicenceType l�schen: " + licenceType.getId());

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        LicenceType lt_db = (LicenceType) hb_session.get(LicenceType.class, licenceType.getId());

        // Alle zugeh�rigen Verbindungen l�schen
        for (LicencedUser lu : lt_db.getLicencedUsers())
        {
          LicencedUser lu_db = (LicencedUser) hb_session.get(LicencedUser.class, lu.getId());
          lu_db.setLicenceType(null);
          hb_session.update(lu_db);
        }

        // LU l�schen
        lt_db.setLicencedUsers(null);
        hb_session.delete(lt_db);

        hb_session.getTransaction().commit();

        Messagebox.show("Lizenz-Typ wurde erfolgreich gel�scht. Alle Lizenzen mit diesem Typen wurden zu 'generelle Lizenz' ge�ndert.", "Lizenztyp l�schen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show("Fehler beim L�schen eines Lizenz-Typs: " + e.getLocalizedMessage(), "Lizenztyp l�schen", Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }
      finally
      {
        hb_session.close();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
  }

  public void onOkClicked()
  {
    if (updateInterface != null)
      updateInterface.update(null);

    this.setVisible(false);
    this.detach();
  }
}
