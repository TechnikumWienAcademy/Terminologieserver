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
public class ProposalStatus{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static ProposalStatus instance;
    private Map<Long, Status> statusMap;
    private Map<Long, Statusrel> statusrelMap;

    public static ProposalStatus getInstance(){
        if (instance == null)
            instance = new ProposalStatus();

        return instance;
    }
    
    public ProposalStatus(){
        statusMap = null;
        initData();
    }
  
    public void reloadData(){
        statusMap = null;
        initData();
    }

    private void initData(){
        if (statusMap == null){
            //Loading data
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            try{
              statusMap = new HashMap<>();
              statusrelMap = new HashMap<>();

              String HQL_status_search = "select distinct s from Status s"
                  + " left join fetch s.statusrelsForStatusIdFrom rel"
                  + " left join fetch rel.action";

              List<Status> statusList = hb_session.createQuery(HQL_status_search).list();

              for (Status status : statusList)
                  statusMap.put(status.getId(), status);

              HQL_status_search = "select distinct rel from Statusrel rel"
                  + " left join fetch rel.roles roles"
                  + " left join fetch rel.action";

              List<Statusrel> statusRelList = hb_session.createQuery(HQL_status_search).list();

              for (Statusrel statusRel : statusRelList)
                  statusrelMap.put(statusRel.getId(), statusRel);
            }
            catch (Exception e){
                LOGGER.error("Error [0098]: " + e.getLocalizedMessage());
            }
            finally{
                if(hb_session.isOpen())
                  hb_session.close();
            }
        }
    }

    /**
     * Calls initData() and then gets the status string which fits the status value key.
     * @param status the value key which is used to find the status string.
     * @return the status string fitting the value key.
    */
    public String getStatusStr(long status){
        initData();

        if (statusMap.containsKey(status))
            return statusMap.get(status).getStatus();

        return "";
    }
  
    public Status getStatus(long status){
        initData();

        if (statusMap.containsKey(status))
            return statusMap.get(status);

        return null;
    }

    public Object getHeaderFilter(){
        initData();
    
        try{
            String statusMapStrings[] = new String[statusMap.values().size()];
            int count = 0;
            for(Status status : statusMap.values())
                statusMapStrings[count++] = status.getStatus();
            return statusMapStrings;
        }
        catch (Exception e){
            LOGGER.error("Error [0097]: " + e.getLocalizedMessage());
        }
        return "String";
    }
  
    public Set<Statusrel> getStatusChilds(long status){
        initData();

        if (statusMap.containsKey(status))
            return statusMap.get(status).getStatusrelsForStatusIdFrom();
    
        return new HashSet<>();
    }
  
    public Statusrel getStatusRel(long statusFrom, long statusTo){
        initData();
    
        for(Statusrel rel : statusrelMap.values())
            if(rel.getStatusByStatusIdFrom().getId() == statusFrom &&
            rel.getStatusByStatusIdTo().getId() == statusTo)// Statusänderung
                return rel;
    
        return null;
    }

    /**
     * TODO check welche version stimmt
     * @param statusFrom
     * @param statusTo
     * @return 
     */
    public boolean isStatusChangePossible(long statusFrom, long statusTo){
        initData();
      
        //3.2.26 
        if(getStatusRel(statusFrom,statusTo)!=null)
            return true;
        else
            return false;
        
        /*if(getStatusRel(statusFrom, statusTo)!=null)
            return false;
        else 
            return true;*/
    }
  
    public boolean isUserAllowed(Statusrel rel, long collabUserId){
        initData();
    
        boolean allowed = false;
        LOGGER.debug("isUserAllowed() with userId: " + collabUserId);
    
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        try{
            String HQL = "select distinct r from Role r"
                + " join r.collaborationusers cu"
                + " where cu.id=" + collabUserId;
      
            LOGGER.debug("HQL: " + HQL);
      
            List<Role> roleList = hb_session.createQuery(HQL).list();
      
            for(Role role : roleList){ 
                for(Role roleCompare : rel.getRoles())
                    if(role.getId().equals(roleCompare.getId())){
                        allowed = true;
                        break;
                    }
                if(allowed)
                    break;
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0100]: " + ex.getLocalizedMessage());
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
    
        return allowed;
    }
  
    public long getStatusIDFromString(String text){
        initData();
    
        for(Status status: statusMap.values())
            if(status.getStatus().equals(text))
                return status.getId();
        
        return 0L;
    }
}
