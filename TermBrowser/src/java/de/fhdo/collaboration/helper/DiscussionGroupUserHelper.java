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
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class DiscussionGroupUserHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // SINGLETON-MUSTER
  private static DiscussionGroupUserHelper instance;

  public static DiscussionGroupUserHelper getInstance()
  {
    if (instance == null)
      instance = new DiscussionGroupUserHelper();

    return instance;
  }
  // KLASSE
  private Map<Long, Discussiongroup> discussionGroupMap;
  //private Map<Long, Collaborationuser> userMap;

  public DiscussionGroupUserHelper()
  {
  }
  
  public int countUsersInDiscussionGroup(long DiscussionGroupId)
  {
    initData();
    
    if(discussionGroupMap != null)
    {
      if(discussionGroupMap.containsKey(DiscussionGroupId))
      {
        Discussiongroup dg = discussionGroupMap.get(DiscussionGroupId);
        return dg.getCollaborationusers().size();
      }
    }
    
    return 0;
  }
  
  public Discussiongroup getDiscussionGroup(long DiscussionGroupId)
  {
    initData();
    
    if(discussionGroupMap != null)
    {
      if(discussionGroupMap.containsKey(DiscussionGroupId))
      {
        return discussionGroupMap.get(DiscussionGroupId);
      }
    }
    
    return null;
  }
  

  public void reloadData()
  {
    discussionGroupMap = null;
  }

  public void initData()
  {
    if (discussionGroupMap == null)
    {
      discussionGroupMap = new HashMap<Long, Discussiongroup>();
      //userMap = new HashMap<Long, Collaborationuser>();

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        String hql = "from Discussiongroup dg "
                + " left join fetch dg.collaborationusers cu "
                + " left join fetch cu.organisation org";

        List<Discussiongroup> list = hb_session.createQuery(hql).list();
        if (list != null && list.size() > 0)
        {
          for (Discussiongroup dg : list)
          {
            discussionGroupMap.put(dg.getId(), dg);
          }
        }
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
          //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

    }

  }
}
