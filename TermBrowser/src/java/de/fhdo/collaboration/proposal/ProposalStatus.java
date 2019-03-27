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
package de.fhdo.collaboration.proposal;

import de.fhdo.collaboration.db.classes.Status;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class ProposalStatus
{

  private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static ProposalStatus instance;

  public static ProposalStatus getInstance()
  {
    if (instance == null)
      instance = new ProposalStatus();

    return instance;
  }
  // Klasse
  //private List<Status> statusList;
  private Map<Long, Status> statusMap;
  private Map<Long, Statusrel> statusrelMap;

  public ProposalStatus()
  {
    statusMap = null;
    initData();
  }
  
  public void reloadData()
  {
    statusMap = null;
  }

    private void initData(){
        if (statusMap == null){
            // Daten laden
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            try{
                statusMap = new HashMap<Long, Status>();
                statusrelMap = new HashMap<Long, Statusrel>();

                String hql = "select distinct s from Status s"
                    + " left join fetch s.statusrelsForStatusIdFrom rel"
                    + " left join fetch rel.action";
                    //+ " left join fetch rel.statusByStatusIdTo";

                List<Status> statusList = hb_session.createQuery(hql).list();

                for (Status status : statusList)
                    statusMap.put(status.getId(), status);
        
                hql = "select distinct rel from Statusrel rel"
                    + " left join fetch rel.roles roles"
                    + " left join fetch rel.action";

                List<Statusrel> statusrelList = hb_session.createQuery(hql).list();

                for (Statusrel rel : statusrelList)
                    statusrelMap.put(rel.getId(), rel);
            }
            catch (Exception e){
                LOGGER.error("Error [0143]", e);
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
    }

  /**
   * Liest den Status-Text von einem Zahlenwert
   *
   * @param status
   * @return
   */
  public String getStatusStr(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status).getStatus();
    }

    return "";
  }
  
  public Status getStatus(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status);
    }

    return null;
  }

  public Object getHeaderFilter()
  {
    initData();
    try
    {
      String s[] = new String[statusMap.values().size()];
      int count = 0;
      for(Status status : statusMap.values())
      {
        s[count++] = status.getStatus();
      }
      return s;
    }
    catch (Exception e)
    {
    }
    return "String";
  }
  
  public Set<Statusrel> getStatusChilds(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status).getStatusrelsForStatusIdFrom();
    }

    return new HashSet<Statusrel>();
  }
  
  public Statusrel getStatusRel(long statusFrom, long statusTo){
    initData();
    
    for(Statusrel rel : statusrelMap.values())
    {
      if(rel.getStatusByStatusIdFrom().getId().longValue() == statusFrom &&
         rel.getStatusByStatusIdTo().getId().longValue() == statusTo)
      {
        // Statusänderung
        return rel;
      }
    }
    
    return null;
  }
  
  public boolean isStatusChangePossible(long statusFrom, long statusTo)
  {
    initData();
    
    if(getStatusRel(statusFrom, statusTo) == null)
      return false;
    else return true;
  }
  
  public boolean isUserAllowed(Statusrel rel, long collabUserId)
  {
    initData();
    
    boolean erlaubt = false;
    if(LOGGER.isDebugEnabled())
      LOGGER.debug("isUserAllowed() mit userId: " + collabUserId);
    
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "select distinct r from Role r"
              + " join r.collaborationusers cu"
              + " where cu.id=" + collabUserId;
      
      if(LOGGER.isDebugEnabled())
        LOGGER.debug("HQL: " + hql);
      
      List<Role> roleList = hb_session.createQuery(hql).list();
      
      //if(logger.isDebugEnabled())
      //  logger.debug("Anzahl: " + roleList.size());
      
      for(Role role : roleList)
      {
        //if(logger.isDebugEnabled())
        //  logger.debug("Rolle: " + role.getName() + ", id: " + role.getId());
        
        for(Role roleCompare : rel.getRoles())
        {
          //if(logger.isDebugEnabled())
          //  logger.debug("  vergleiche mit Rolle: " + roleCompare.getName() + ", id: " + roleCompare.getId());
          
          if(role.getId().longValue() == roleCompare.getId().longValue())
          {
            // Berechtigung vorhanden
            erlaubt = true;
            break;
          }
        }
        if(erlaubt)
          break;
      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception ex)
    {
      //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(ex, this);
    }
    finally
    {
      // Session schließen
      hb_session.close();
    }
    
    return erlaubt;
  }
  
  public long getStatusIDFromString(String text)
  {
    initData();
    
    for(Status s: statusMap.values())
    {
      if(s.getStatus().equals(text))
      {
        return s.getId();
      }
    }
    return 0L;
  }
}
