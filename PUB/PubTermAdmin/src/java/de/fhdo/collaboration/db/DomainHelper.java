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
package de.fhdo.collaboration.db;

import de.fhdo.collaboration.db.classes.Domain;
import de.fhdo.collaboration.db.classes.DomainValue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert M�tzner
 */
public class DomainHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // Singleton-Muster
  private static DomainHelper instance;

  public static DomainHelper getInstance()
  {
    if (instance == null)
      instance = new DomainHelper();

    return instance;
  }
  // Klasse
  private Map<Long, Map<String, DomainValue>> domains = null;
  private Map<Long, List<DomainValue>> domainLists = null;
  private Map<Long, DomainValue> defaultValues = null;

  public DomainHelper()
  {
  }

  public void reloadAllDomains()
  {
    domains = null;
    domainLists = null;
    defaultValues = null;
  }

  /**
   * Liest eine DomainValue aus einer Dom�ne mit dem angegebenen Code
   *
   * @param DomainID Die zu lesende DomainID, siehe Definitions.java
   * @param Code Der zu lesende Code
   * @return die DomainValue
   */
  public DomainValue getDomainValue(long DomainID, String Code)
  {
    if (Code == null || Code.length() == 0)
      return null;

    DomainValue dv = null;
    initDomain(DomainID);

    Map<String, DomainValue> map = getDomainMap(DomainID);

    if (map != null)
    {
      dv = map.get(Code);
    }

    if (dv == null && logger.isDebugEnabled())
      logger.debug("DomainValue mit Domain-ID " + DomainID + " und Code '" + Code + "' nicht gefunden!");

    return dv;
  }

  /**
   * Liest eine DomainValue aus einer Dom�ne mit dem angegebenen Code
   *
   * @param DomainValueID Die zu lesende DomainID, siehe Definitions.java
   * @return die DomainValue
   */
  public DomainValue getDomainValue(long DomainValueID)
  {
    if (DomainValueID <= 0)
      return null;

    DomainValue dv = null;
    //initDomain(DomainID);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    dv = (DomainValue) hb_session.get(DomainValue.class, DomainValueID);

    // Transaktion abschlie�en
    
    hb_session.close();

    if (dv == null && logger.isDebugEnabled())
      logger.debug("DomainValue mit ID '" + DomainValueID + "' nicht gefunden!");

    return dv;
  }

  public String getDomainValueDisplayText(long DomainID, String Code)
  {
    if (Code == null || Code.length() == 0)
      return "";

    DomainValue dv = null;
    initDomain(DomainID);

    Map<String, DomainValue> map = getDomainMap(DomainID);

    if (map != null)
    {
      dv = map.get(Code);
    }

    if (dv == null)
    {
      if (logger.isDebugEnabled())
        logger.debug("DomainValue mit Domain-ID " + DomainID + " und Code '" + Code + "' nicht gefunden!");

      return "";
    }

    return dv.getDisplayText();
  }

  /**
   * Liest alle �bergeordneten DomainValues zu einer DomainValue
   *
   */
  public List<DomainValue> getUpperDomainValues(long DomainValueID)
  {
    if (DomainValueID <= 0)
      return null;

    DomainValue dv = null;
    //initDomain(DomainID);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    dv = (DomainValue) hb_session.get(DomainValue.class, DomainValueID);
    if (dv == null && logger.isDebugEnabled())
      logger.debug("DomainValue mit ID '" + DomainValueID + "' nicht gefunden!");

    List<DomainValue> list = null;
    if (dv != null && dv.getDomainValuesForDomainValueIdParent()!= null)
    {
      list = new LinkedList(dv.getDomainValuesForDomainValueIdParent());
    }

    hb_session.close();

    return list;
  }

  /**
   * Liest alle �bergeordneten DomainValues zu einer DomainValue
   *
   */
  public boolean saveUpperDomainID(long DomainValueID, long UpperID)
  {
    if (DomainValueID <= 0)
      return false;

    logger.debug("saveUpperDomainID mit '" + DomainValueID + "' und " + UpperID);

    DomainValue dv = null;
    //initDomain(DomainID);

    DomainValue ueber_dv = getDomainValue(UpperID);

    if (ueber_dv == null)
    {
      logger.debug("DomainValue mit �ber-ID " + UpperID + " existiert nicht");
      return false;
    }

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    dv = (DomainValue) hb_session.get(DomainValue.class, DomainValueID);
    if (dv == null && logger.isDebugEnabled())
      logger.debug("DomainValue mit ID '" + DomainValueID + "' nicht gefunden!");

    //List<DomainValue> list = null;
    if (dv != null)
    {
      boolean gefunden = false;
      if (dv.getDomainValuesForDomainValueIdParent()!= null)
      {
        Iterator<DomainValue> it = dv.getDomainValuesForDomainValueIdParent().iterator();
        while (it.hasNext())
        {
          DomainValue ueber_dv2 = it.next();
          if (ueber_dv2.getId() == UpperID)
          {
            logger.debug("�ber-Domain bereits gefunden, also nicht hinzuf�gen");
            gefunden = true;
          }
        }
      }

      if (gefunden == false)
      {
        logger.debug("�ber-Domain hinzuf�gen...");

        if (dv.getDomainValuesForDomainValueIdParent()== null)
          dv.setDomainValuesForDomainValueIdParent(new HashSet<DomainValue>());

        dv.getDomainValuesForDomainValueIdParent().add(ueber_dv);

        hb_session.merge(dv);
      }
      //list = new LinkedList(dv.getDomainValuesForDomainValueId1());
    }

    // Transaktion abschlie�en
    hb_session.getTransaction().commit();
    hb_session.close();

    return true;
  }

  /**
   * Liest eine Map von DomainValues anhand einer DomainID. Wurde die Liste
   * bereits gelesen, wird die gespeicherte zur�ckgegeben.
   *
   * @param DomainID Die zu lesende DomainID, siehe Definitions.java
   * @return Map von DomainValues, welche zu der Dom�ne geh�ren
   */
  public Map<String, DomainValue> getDomainMap(long DomainID)
  {
    initDomain(DomainID);

    return domains.get(DomainID);
  }

  public List<DomainValue> getDomainList(long DomainID)
  {
    initDomain(DomainID);

    return domainLists.get(DomainID);
  }

  public DomainValue getDefaultValue(long DomainID)
  {
    return defaultValues.get(DomainID);
  }

  public String[] getDomainStringList(long DomainID)
  {
    initDomain(DomainID);

    String[] s = null;
    try
    {
      List<DomainValue> dvList = domainLists.get(DomainID);

      s = new String[dvList.size()];
      
      for(int i=0;i<dvList.size();++i)
      {
        s[i] = dvList.get(i).getDisplayText();
      }

    }
    catch (Exception ex)
    {
      logger.error("[DomainHelper.java] Fehler bei getDomainStringList(): " + ex.getLocalizedMessage());
    }

    return s;
  }

  /**
   * L�scht eine zwischengespeicherte Domain-Liste. Beim n�chsten Aufruf von
   * getDomainMap() wird die Liste erneut geladen!
   *
   * @param DomainID Die zu l�schende DomainID, siehe Definitions.java
   */
  public void reloadDomain(long DomainID)
  {
    if (domains != null)
    {
      if (logger.isDebugEnabled())
        logger.debug("entferne Domain mit ID " + DomainID + " aus dem Cache...");
      domains.remove(DomainID);
      domainLists.remove(DomainID);
    }
  }

  private void initDomain(long DomainID)
  {
    //if (logger.isDebugEnabled())
    //  logger.debug("initDomain() mit ID: " + DomainID);

    if (domains == null)
    {
      // Domain-Map erstellen
      if (logger.isDebugEnabled())
        logger.debug("initDomain(), neue Domain-Map erstellen");

      domains = new HashMap<Long, Map<String, DomainValue>>();
    }
    if (domainLists == null)
    {
      domainLists = new LinkedHashMap<Long, List<DomainValue>>();
    }
    if (defaultValues == null)
      defaultValues = new HashMap<Long, DomainValue>();

    if (domains.containsKey(DomainID) == false)
    {
      // Diese Domain laden
      //if (logger.isDebugEnabled())
      //  logger.debug("initDomain(), lade Domain mit ID " + DomainID);

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      Domain domain = (Domain) hb_session.get(Domain.class, DomainID);

      if (domain != null)
      {
        String hql = "from DomainValue where domain_id=" + DomainID;

        /*if (domain.getDisplayOrder() != null
                && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ID)
        {
          hql += " order by domain_value_id";
        }
        else if (domain.getDisplayOrder() != null
                && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ORDERID)
        {
          hql += " order by order_no";
        }
        else*/
          hql += " order by display_text";

        List list = hb_session.createQuery(hql).list();

        domainLists.put(DomainID, list);

        //domain.getDefaultValue()
        // Eine Map mit allen DomainValues erstellen (DomainCode ist der Key)
        Map<String, DomainValue> map = new HashMap<String, DomainValue>();

        for (int i = 0; i < list.size(); ++i)
        {
          DomainValue dv = (DomainValue) list.get(i);
          map.put(dv.getCode(), dv);
        }

        // Der Map mit allen Domains diese Map hinzuf�gen (DomainID ist der Key)
        domains.put(DomainID, map);

        // Transaktion abschlie�en
        
        hb_session.close();

        if (defaultValues.containsKey(DomainID) == false)
        {
          try
          {
            if (domain.getDomainValueByDefaultValueId() != null)
              //defaultValues.put(DomainID, getDomainValue(Integer.parseInt(domain.getDefaultValue())));
              defaultValues.put(DomainID, domain.getDomainValueByDefaultValueId());
          }
          catch (Exception e)
          {
            logger.error("Fehler beim Lesen einer Default-Value mit ID '" + domain.getId()+ "': " + e.getMessage());
          }
        }
      }
      else
      {
        // Transaktion abschlie�en
        
        hb_session.close();
      }
    }


  }
}
