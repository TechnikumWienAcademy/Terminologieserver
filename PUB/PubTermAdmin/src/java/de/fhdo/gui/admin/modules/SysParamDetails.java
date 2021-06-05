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
import de.fhdo.helper.DomainHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class SysParamDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal iUpdateListener;
  private de.fhdo.db.hibernate.SysParam sysParam;
  boolean newEntry = false;
  private List<DomainValue> validityDomainList;
  //private DomainValue selectedValidityDomain;
  
  private List<DomainValue> modifyLevelList;
  //private DomainValue selectedModifyLevel;
  private Session hb_sessionS;

  public SysParamDetails()
  {
    try
    {
      // Domain-Listen laden
      validityDomainList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_VALIDITYDOMAIN);
      modifyLevelList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_VALIDITYDOMAIN);
      
      Map args = Executions.getCurrent().getArg();

      long paramId = 0;

      try
      {
        paramId = Long.parseLong(args.get("sysparam_id").toString());
        logger.debug("SysParam-ID: " + paramId);
      }
      catch (Exception e)
      {
        logger.debug("Parameter 'sysparam_id' nicht gefunden");
      }

      logger.debug("SysParamDetails() - Konstruktor");

      if (paramId > 0)
      {
        newEntry = false;

        hb_sessionS = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();

        //person = PersonHelper.getInstance().getCurrentPatient();
        sysParam = (de.fhdo.db.hibernate.SysParam) hb_sessionS.get(de.fhdo.db.hibernate.SysParam.class, paramId);

        
      }
      else
      {
        // Neuer Eintrag
        newEntry = true;

        sysParam = new de.fhdo.db.hibernate.SysParam();
        
        sysParam.setDomainValueByModifyLevel(DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN));
        sysParam.setDomainValueByValidityDomain(DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN));
        //selectedValidityDomain = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN);
        //selectedModifyLevel = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN);
      }

    }
    catch (Exception e)
    {
      logger.error("Fehler im Konstruktor: " + e.getMessage());
    }
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");
      
      //sysParam.setDomainValueByModifyLevel(selectedModifyLevel);
      //attachment.getAttachment().setTechnicalTypeCd(selectedTechnicalType.getDomainCode());
      

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      try
      {
        

        if (newEntry)
        {
          hb_session.save(sysParam);
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug("Daten aktualisieren");
          
          hb_session.merge(sysParam);
        }

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
          logger.error("Fehler in onOkClicked() bei hibernate: " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      this.setVisible(false);

      if (iUpdateListener != null)
      {
        iUpdateListener.update(sysParam, !newEntry);
      }

      this.detach();
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in onOkClicked(): " + e.getMessage());
      e.printStackTrace();
      if(hb_sessionS != null)
      hb_sessionS.close();
    }
    if(hb_sessionS != null)
    hb_sessionS.close();
    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void afterCompose()
  {

    //Listbox contactListBox = (Listbox) getFellow("lbCommunication");
    //contactListBox.setModel(communicationListModel);
    //contactListBox.setItemRenderer(communicationRenderer);

    

    /*tb = (Textbox)getFellow("tb_Email");
     tb.setVisible(newEntry);*/

    //row = (Row) getFellow("row_kontakt");
    //row.setVisible(!newEntry);

    //row = (Row) getFellow("row_Email");
    //row.setVisible(newEntry);


    //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
    if(hb_sessionS != null)
        hb_sessionS.close();
    //Executions.getCurrent().setAttribute("doctor_controller", null);
  }

  /**
   * @return the iUpdateListener
   */
  public IUpdateModal getiUpdateListener()
  {
    return iUpdateListener;
  }

  /**
   * @param iUpdateListener the iUpdateListener to set
   */
  public void setiUpdateListener(IUpdateModal iUpdateListener)
  {
    this.iUpdateListener = iUpdateListener;
  }

 

  /**
   * @return the sysParam
   */
  public de.fhdo.db.hibernate.SysParam getSysParam()
  {
    return sysParam;
  }

  /**
   * @param sysParam the sysParam to set
   */
  public void setSysParam(de.fhdo.db.hibernate.SysParam sysParam)
  {
    this.sysParam = sysParam;
  }

  /**
   * @return the validityDomainList
   */
  public List<DomainValue> getValidityDomainList()
  {
    return validityDomainList;
  }

  /**
   * @param validityDomainList the validityDomainList to set
   */
  public void setValidityDomainList(List<DomainValue> validityDomainList)
  {
    this.validityDomainList = validityDomainList;
  }

  /**
   * @return the modifyLevelList
   */
  public List<DomainValue> getModifyLevelList()
  {
    return modifyLevelList;
  }

  /**
   * @param modifyLevelList the modifyLevelList to set
   */
  public void setModifyLevelList(List<DomainValue> modifyLevelList)
  {
    this.modifyLevelList = modifyLevelList;
  }
}
