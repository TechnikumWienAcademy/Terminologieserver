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
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.helper.SessionHelper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class CollaborationuserHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();


  
  public static List<Long> GetDiscussionGroupIDsForCurrentUser(Session hb_session)
  {
    logger.debug("GetDiscussionGroupIDsForCurrentUser()");
    
    List<Long> retList = new LinkedList<Long>();

    String hql = "select distinct dg from Discussiongroup dg "
            + " left join dg.collaborationusers cu "
            + " where cu.id=" + SessionHelper.getCollaborationUserID();

    logger.debug("HQL: " + hql);
    
    List<Discussiongroup> list = hb_session.createQuery(hql).list();
    if (list != null)
    {
      for (Discussiongroup dg : list)
      {
        logger.debug("DG gefunden mit ID: " + dg.getId());
        retList.add(dg.getId());
      }
    }

    return retList;
  }
  
  public static String ConvertDiscussionGroupListToCommaString(List<Long> list)
  {
    String s = "";
    
    if(list != null)
    {
      for(Long l : list)
      {
        if(s.length() > 0)
          s += ",";
        s += "" + l;
      }
    }
    
    return s;
  }
  
   public static Role getCollaborationuserRoleByName(String role){
   
        Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();
        
        List<Role> roleList = null;
        try{
            String hql = "select distinct r from Role r where r.name=:role";
            Query q = hb_session_kollab.createQuery(hql);
            q.setParameter("role", role);
            roleList = q.list();
            if(roleList.size() == 1){
                return roleList.get(0);
            }
        }catch(Exception e){
          logger.error("[Fehler bei CollabUserRoleHelper.java createCollabUserRoleTable(): " + e.getMessage());
        }
        finally
        {
          hb_session_kollab.close();
        }    
      return null;
   }
}
