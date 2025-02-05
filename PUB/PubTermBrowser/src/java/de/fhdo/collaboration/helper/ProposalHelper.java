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
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.logging.LoggingOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert M�tzner
 */
public class ProposalHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ProposalHelper()
  {
    
  }


  public static Map<Long, Collaborationuser> getAllUsersForProposal(long proposalId)
  {
    Map<Long, Collaborationuser> list = new HashMap<Long, Collaborationuser>();

    DiscussionGroupUserHelper.getInstance().initData(); // vor Session Daten initalisieren

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "select distinct p from Privilege p "
              + " left join p.proposal prop"
              + " left join fetch p.collaborationuser cu"
              + " left join fetch cu.organisation org"
              + " left join fetch p.discussiongroup dg"
              + " where prop.id=" + proposalId;

      if (logger.isDebugEnabled())
        logger.debug("HQL: " + hql);

      org.hibernate.Query q = hb_session.createQuery(hql);
      List<Privilege> liste = q.list();

      for (Privilege priv : liste)
      {
        //logger.debug("Privileg mit ID: " + priv.getId());

        if (priv.getCollaborationuser() != null)
        {
          list.put(priv.getCollaborationuser().getId(), priv.getCollaborationuser());
        }

        if (priv.getDiscussiongroup() != null)
        {
          for (Collaborationuser cu : priv.getDiscussiongroup().getCollaborationusers())
          {
            list.put(cu.getId(), cu);
          }
          /*Discussiongroup dg = DiscussionGroupUserHelper.getInstance().getDiscussionGroup(priv.getDiscussiongroup().getId());
           if (dg != null)
           {
           for (Collaborationuser cu : dg.getCollaborationusers())
           {
           list.put(cu.getId(), cu);
           }

           }*/
        }

      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, ProposalHelper.class);
    }
    finally
    {
      // Session schlie�en
      hb_session.close();
    }

    return list;
  }

 

  public static String getNameFull(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getFirstName() + " " + user.getName()).trim();
      if (user.getOrganisation() != null)
      {
        if (user.getOrganisation().getOrganisationAbbr() != null
                && user.getOrganisation().getOrganisationAbbr().length() > 0)
        {
          s += " (" + user.getOrganisation().getOrganisationAbbr() + ")";
        }
        else
        {
          s += " (" + user.getOrganisation().getOrganisation() + ")";
        }
      }
    }

    return s;
  }
  
  public static String getName(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getFirstName() + " " + user.getName()).trim();
    }

    return s;
  }

  public static String getNameReverseFull(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getName() + ", " + user.getFirstName()).trim();
      if (user.getOrganisation() != null)
      {
        if (user.getOrganisation().getOrganisationAbbr() != null
                && user.getOrganisation().getOrganisationAbbr().length() > 0)
        {
          s += " (" + user.getOrganisation().getOrganisationAbbr() + ")";
        }
        else
        {
          s += " (" + user.getOrganisation().getOrganisation() + ")";
        }
      }
    }

    return s;
  }

  public static String getOrganisation(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      if (user.getOrganisation() != null)
      {
        s += user.getOrganisation().getOrganisation();
      }
    }

    return s;
  }

  public static String getNameReverse(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getName() + ", " + user.getFirstName()).trim();
    }

    return s;
  }

  public static String getNameShort(Collaborationuser user)
  {
    String s = "";

    if (user != null && user.getName() != null)
    {
      s = user.getName().trim();
    }

    return s;
  }

  public static List<Proposalstatuschange> getStatusChangeList(Proposal proposal)
  {
    List<Proposalstatuschange> list = new LinkedList<Proposalstatuschange>();
    if (proposal != null)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        String hql = "from Proposalstatuschange psc "
                + " left join fetch psc.collaborationuser cu"
                + " where proposalId=" + proposal.getId()
                + " order by changeTimestamp ";

        list = hb_session.createQuery(hql).list();

        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
          LoggingOutput.outputException(e, ProposalHelper.class);
      }
      finally
      {hb_session.close();
      }

      // Ersteller hinzuf�gen
      Proposalstatuschange psc = new Proposalstatuschange();
      psc.setProposal(proposal);
      psc.setProposalStatusTo(1); // TODO
      psc.setChangeTimestamp(proposal.getCreated());
      psc.setCollaborationuser(proposal.getCollaborationuser());
      list.add(0, psc);

      if (psc.getCollaborationuser() != null)
        logger.debug("PSC-User: " + psc.getCollaborationuser().getName());
      else
        logger.debug("PSC-User: null");


    }
    else
    {
      logger.warn("[ProposalHelper.java] getStatusChangeList() - Proposal ist null");
    }
    return list;
  }
  
    public static boolean isStatusDiscussion(long StatusId){ 
        return StatusId == 2; // TODO Feste Variable "2" in SysParam schreiben
    }
  
  public static boolean isProposalInDiscussion(Proposal proposal)
  {
    
    if (proposal != null)
    {
      logger.debug("isProposalInDiscussion() mit ID: " + proposal.getId());
      
      /*boolean createSession = session == null;
      Session hb_session = session;
      if(createSession)
        hb_session = HibernateUtil.getSessionFactory().openSession();*/

      logger.debug("Status: " + proposal.getStatus());
      
      if(proposal.getStatus().longValue() == 2)  // TODO Feste Variable "2" in SysParam schreiben
      {
        Date jetzt = new Date();
        
        if(proposal.getValidFrom() != null)
        {
          if(jetzt.before(proposal.getValidFrom()))
          {
            return false;
          }
        }
        if(proposal.getValidTo() != null)
        {
          if(jetzt.after(proposal.getValidTo()))         
            return false;
        }
        
        return true;  // Vorschlag ist in g�ltiger Diskussion
      }
    }
    else
    {
      logger.warn("[ProposalHelper.java] isProposalInDiscussion() - Proposal ist null");
    }
    return false;
  }
}
