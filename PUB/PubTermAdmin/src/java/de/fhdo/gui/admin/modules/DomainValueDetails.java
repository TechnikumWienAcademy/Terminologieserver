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

import de.fhdo.db.hibernate.Domain;
import de.fhdo.db.hibernate.DomainValue;
import de.fhdo.db.HibernateUtil;
import de.fhdo.helper.DomainHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class DomainValueDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static Session hb_session = null;
  //private Session hb_session = HibernateUtil.getSessionFactory().openSession();
  //private static Session hb_session;
  private DomainValue domainValue;
  private Map args;
  //private String domainValueID = "";
  private long domainId = 0, domain_value_id = 0;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;

  public DomainValueDetails()
  {
    //hb_session = HibernateSession.getSession();
    //hb_session = HibernateUtil.getSessionFactory().openSession();

    args = Executions.getCurrent().getArg();
    try
    {
      Object o = args.get("domain_id");
      logger.debug("o: " + o.toString());
      domainId = Long.parseLong(o.toString());
      
      domain_value_id = Long.parseLong(args.get("domain_value_id").toString());
    }
    catch (Exception e)
    {
      //e.printStackTrace();
    }
    
    try
    {
      domainValue = (DomainValue) args.get("domain_value");
    }
    catch (Exception e)
    {
      logger.debug("Neuer Domain-Value-Eintrag");
    }
    
    logger.debug("DomainID: " + domainId);

    if (domainValue == null)
    {
      domainValue = new DomainValue();

      newEntry = true;
    }

    logger.debug("domain_value_id: " + domain_value_id);

  }

  public void afterCompose()
  {
    // Verbindung von DomainValue herausbekommen
    String s = "";

    if (domainValue != null && domainValue.getDomainValueId() != null && domainValue.getDomainValueId() > 0)
    {
      List<DomainValue> list = DomainHelper.getInstance().getUpperDomainValues(domainValue.getDomainValueId());

      if (list != null)
      {
        for (int i = 0; i < list.size(); ++i)
        {
          if (s.length() > 0)
            s += ";";

          s += list.get(i).getDomainValueId();

          logger.debug("ueber-ID: " + list.get(i).getDomainValueId());
        }
      }
    }
    
    logger.debug("domain_value_id: " + domain_value_id);
    
    if(domain_value_id > 0)
    {
      if (s.length() > 0)
        s += ";";
      s += "" + domain_value_id;
    }
    
    logger.debug("s: " + s);

    Textbox tb = (Textbox) getFellow("ueberID");
    tb.setValue(s);
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      if(domainValue.getDomainCode() == null || domainValue.getDomainCode().length() == 0)
      {
        Messagebox.show("Geben Sie bitte einen Code an.");
        return;
      }
      if(domainValue.getDomainDisplay() == null || domainValue.getDomainDisplay().length() == 0)
      {
        Messagebox.show("Geben Sie bitte einen Anzeige-Text an.");
        return;
      }
      
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {

        if (newEntry)
        {
          if (logger.isDebugEnabled())
            logger.debug("Neuer Eintrag, fuege der Domain hinzu!");

          Domain domain = (Domain) hb_session.get(Domain.class, domainId);
          domainValue.setDomain(domain);
          

          hb_session.save(domainValue);
        }
        else
        {
          hb_session.merge(domainValue);
        }



      }
      catch (Exception e)
      {
        logger.error("Fehler in DomainValueDetails.java in onOkClicked(): " + e.getMessage());
        e.printStackTrace();
      }


      hb_session.getTransaction().commit();
      hb_session.close();

      // Verbindungen speichern
      Textbox tb = (Textbox) getFellow("ueberID");
      String ueberIDs = tb.getValue();

      if (ueberIDs.length() > 0)
      {
        String[] ids = ueberIDs.split(";");
        for (int i = 0; i < ids.length; ++i)
        {
          long id = 0;
          if (ids[i].length() > 0)
          {
            id = Long.parseLong(ids[i]);
            DomainHelper.getInstance().saveUpperDomainID(domainValue.getDomainValueId(), id);
          }
        }
      }

      //hb_session.close();

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(domainValue, !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in DomainValueDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void onCancelClicked()
  {
    //if (logger.isDebugEnabled())
    //    logger.debug("onCancelClicked()");
    this.setVisible(false);
    this.detach();

  }

  /**
   * @return the domainValue
   */
  public DomainValue getDomainValue()
  {
    return domainValue;
  }

  /**
   * @param domainValue the domainValue to set
   */
  public void setDomainValue(DomainValue domainValue)
  {
    this.domainValue = domainValue;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }
}
