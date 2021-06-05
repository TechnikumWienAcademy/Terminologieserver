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
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.db.hibernate.ValueSet;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class SvAssignmentDetails extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private SvAssignmentData svAssignmentData;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  DualListboxUser dlbu;
	
	private boolean isMoreThenOnePersonAssigned = false;

  public SvAssignmentDetails()
  {
    Map args = Executions.getCurrent().getArg();

    svAssignmentData = (SvAssignmentData)args.get("svAssignmentData");
    
    //Prepare Data
    Set<SvAssignmentData> userData = new HashSet<SvAssignmentData>();
    Set<SvAssignmentData> choosenUserData = new HashSet<SvAssignmentData>();
    
    Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    
    try{
        
        if(!newEntry){
            //Load User Info here
            //1.GetGroupMembers
            String hqlTermUser = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.assignedTerms a where cu.enabled=:enabled and a.classId=:classId and a.classname=:classname";
            Query qTermUser = hb_session_kollab.createQuery(hqlTermUser);
            qTermUser.setParameter("enabled", true);
            qTermUser.setParameter("classId", svAssignmentData.getClassId());
            qTermUser.setParameter("classname", svAssignmentData.getClassname());
            List<Collaborationuser> cuList = qTermUser.list();
						
						if (cuList.size() > 1){
							isMoreThenOnePersonAssigned = true;
						}

            for(Collaborationuser cu:cuList){
                SvAssignmentData dgui = new SvAssignmentData(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
                choosenUserData.add(dgui);
            }
        }
        //2.GetNotGroupMembers
        String hqlNotMembers = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.roles r where cu.enabled=:enabled AND deleted=0 ";
               hqlNotMembers += " and r.name in (:nameList)";
        Query qNotMembers = hb_session_kollab.createQuery(hqlNotMembers);
        qNotMembers.setParameter("enabled", true);
        ArrayList<String> nameList = new ArrayList<String>();
        nameList.add(CODES.ROLE_INHALTSVERWALTER);
        nameList.add(CODES.ROLE_ADMIN); //=> Darf sowieso alles sehen
        qNotMembers.setParameterList("nameList", nameList);
        List<Collaborationuser> cuNotMemberList = qNotMembers.list();
        
        for(Collaborationuser cu:cuNotMemberList){
            SvAssignmentData dgui = new SvAssignmentData(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
            boolean member = false;
            
            if(!newEntry){
                for(SvAssignmentData info:choosenUserData){
                    if(dgui.getCollaborationuserId() == info.getCollaborationuserId()){
                        member = true;
                    }
                }
            }
            if(!member){
                userData.add(dgui);
            }
        }
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei DualList preparation(): " + e.getMessage());
    }
    finally
    {
      hb_session_kollab.close();
    }
    
    Executions.getCurrent().setAttribute("userData", userData);
    Executions.getCurrent().setAttribute("choosenUserData", choosenUserData);
  }

  public void afterCompose()
  {
    Include incUser = (Include) getFellow("incListUser");
    Window windowUser = (Window) incUser.getFellow("duallistboxUser");
    dlbu = (DualListboxUser)windowUser.getFellow("dualLBoxUser");
    Checkbox checkbox = (Checkbox) getFellow("cAutoRelease");
    checkbox.setChecked(svAssignmentData.getAutoRelease());
    if(SessionHelper.isAdmin())
    {
      checkbox.setDisabled(false);
    }
  }
  
  public void onOkClicked()
  {
    
    // speichern mit Hibernate
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      
      Session hb_session_term = de.fhdo.db.HibernateUtil.getSessionFactory().openSession();
      hb_session_term.getTransaction().begin();

      try
      {  
        int size = dlbu.getChosenDataList().size();
        if(dlbu.getChosenDataList().isEmpty()){
        
            //Check ob AT vorhanden; Wenn ja delete; Wenn nein ignore
            if(svAssignmentData.getAssignedTermId() == null){  //Vorher kein User
            
                //Do nothing
            }else{
/* TODO							
							//in case of multiple assigend persons
							if(isMoreThenOnePersonAssigned){
								String hqlTermUser = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.assignedTerms a where cu.enabled=:enabled and a.classId=:classId";
								Query qTermUser = hb_session.createQuery(hqlTermUser);
								qTermUser.setParameter("enabled", true);
								qTermUser.setParameter("classId", svAssignmentData.getClassId());
								List<Collaborationuser> cuList = qTermUser.list();
								
								for (Collaborationuser userOld : cuList){
									Long oldPrivId = -1l;
									String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";

									Query q = hb_session.createQuery(hql);
									q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
									q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
									List<Proposal> proposalList = q.list();
									
									for(Proposal prop:proposalList){  
                    
                    for(Privilege priv:prop.getPrivileges()){

                        if(priv.getCollaborationuser().getId().equals(userOld.getId()))
                            oldPrivId = priv.getId();
                    }

                    if((!oldPrivId.equals(-1l)) && !prop.getCollaborationuser().getId().equals(userOld.getId())){

                        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, oldPrivId);
                        priv_db.setCollaborationuser(null);
                        priv_db.setDiscussiongroup(null);
                        priv_db.setProposal(null);
                        userOld.getPrivileges().remove(priv_db);
                        prop.getPrivileges().remove(priv_db);
                        hb_session.delete(priv_db);
                    }
									}
									
									for (AssignedTerm terms : userOld.getAssignedTerms()){
										if (terms.getClassId() == svAssignmentData.getAssignedTermId()){
											boolean stop = true;
										}
									}
								
									a.setCollaborationuser(null);
									hb_session.delete(a);

									svAssignmentData.setAssignedTermId(null);
									svAssignmentData.setCollaborationuserId(null);
									svAssignmentData.setUsername("-");
									svAssignmentData.setFirstName("-");
									svAssignmentData.setName("-");
									svAssignmentData.setOrganisation("-");
							
								}
							}*/
							
                //Update Old
                AssignedTerm a = (AssignedTerm)hb_session.get(AssignedTerm.class, svAssignmentData.getAssignedTermId());
                
                Collaborationuser userOld = (Collaborationuser)hb_session.get(Collaborationuser.class, svAssignmentData.getCollaborationuserId());
                Long oldPrivId = -1l;
                
                String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";

                Query q = hb_session.createQuery(hql);
                q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                List<Proposal> proposalList = q.list();

                for(Proposal prop:proposalList){  
                    
                    for(Privilege priv:prop.getPrivileges()){

                        if(priv.getCollaborationuser().getId().equals(userOld.getId()))
                            oldPrivId = priv.getId();
                    }

                    if((!oldPrivId.equals(-1l)) && !prop.getCollaborationuser().getId().equals(userOld.getId())){

                        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, oldPrivId);
                        priv_db.setCollaborationuser(null);
                        priv_db.setDiscussiongroup(null);
                        priv_db.setProposal(null);
                        userOld.getPrivileges().remove(priv_db);
                        prop.getPrivileges().remove(priv_db);
                        hb_session.delete(priv_db);
                    }
                }
                
                
                
                a.setCollaborationuser(null);
                hb_session.delete(a);
                
                svAssignmentData.setAssignedTermId(null);
                svAssignmentData.setCollaborationuserId(null);
                svAssignmentData.setUsername("-");
                svAssignmentData.setFirstName("-");
                svAssignmentData.setName("-");
                svAssignmentData.setOrganisation("-");
            }
            
        }else{
        
            //Check ob AT vorhanden; Wenn ja update; Wenn nein save
            if(svAssignmentData.getAssignedTermId() == null){  //Vorher kein User
                //Neu von Leer weg
                AssignedTerm at_db = new AssignedTerm();
                AssignedTerm puff_db;
                Object obj = null;
                Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, dlbu.getChosenDataList().iterator().next().getCollaborationuserId());

                if(svAssignmentData.getClassname().equals("CodeSystem")){
                    obj = hb_session_term.get(CodeSystem.class, svAssignmentData.getClassId());
                }
                
                if(svAssignmentData.getClassname().equals("ValueSet")){
                    obj = hb_session_term.get(ValueSet.class, svAssignmentData.getClassId());
                }
                
                if(obj instanceof CodeSystem){

                    //First Check if already exists or other user is connected!hb_sessionhb_session
                    String hqlTermAss = "select distinct at from AssignedTerm at join fetch at.collaborationuser cu where at.classId=:classId and at.classname=:classname";
                    Query qTermAss = hb_session.createQuery(hqlTermAss);
                    qTermAss.setParameter("classname", "CodeSystem");
                    qTermAss.setParameter("classId", ((CodeSystem)obj).getId());

                    List<AssignedTerm> atList = qTermAss.list();

                    if(atList.size() == 1){ 

                        if(atList.get(0).getCollaborationuser().getId() == user.getId()){ //UserId == UserId => do nothing, because no double assignments

                        }else{ //delete old user => add new User

                            //get old user and delete!
                            puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, atList.get(0).getId());
                            hb_session.delete(puff_db);

                            //add new user
                            at_db.setClassId(((CodeSystem)obj).getId());
                            at_db.setClassname("CodeSystem");
                            at_db.setCollaborationuser(user);
                            hb_session.save(at_db);
                        }
                    }else if(atList.isEmpty()){ //add new User

                        //add new user
                        at_db.setClassId(((CodeSystem)obj).getId());
                        at_db.setClassname("CodeSystem");
                        at_db.setCollaborationuser(user);
                        hb_session.save(at_db);

                    }else{ //delete all users => add new User 

                        for(AssignedTerm at:atList){
                            puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, at.getId());
                            hb_session.delete(puff_db);
                        }

                        //add new user
                        at_db.setClassId(((CodeSystem)obj).getId());
                        at_db.setClassname("CodeSystem");
                        at_db.setCollaborationuser(user);
                        hb_session.save(at_db);
                    }

                }else if(obj instanceof ValueSet){

                    //First Check if already exists or other user is connected!hb_sessionhb_session
                    String hqlTermAss = "select distinct at from AssignedTerm at join fetch at.collaborationuser cu where at.classId=:classId and at.classname=:classname";
                    Query qTermAss = hb_session.createQuery(hqlTermAss);
                    qTermAss.setParameter("classname", "ValueSet");
                    qTermAss.setParameter("classId", ((ValueSet)obj).getId());

                    List<AssignedTerm> atList = qTermAss.list();

                    if(atList.size() == 1){ 

                        if(atList.get(0).getCollaborationuser().getId() == user.getId()){ //UserId == UserId => do nothing, because no double assignments
                            at_db.setId(atList.get(0).getId());
                        }else{ //delete old user => add new User

                            //get old user and delete!
                            puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, atList.get(0).getId());
                            hb_session.delete(puff_db);

                            //add new user
                            at_db.setClassId(((ValueSet)obj).getId());
                            at_db.setClassname("ValueSet");
                            at_db.setCollaborationuser(user);
                            hb_session.save(at_db);
                        }
                    }else if(atList.isEmpty()){ //add new User

                        //add new user
                        at_db.setClassId(((ValueSet)obj).getId());
                        at_db.setClassname("ValueSet");
                        at_db.setCollaborationuser(user);
                        hb_session.save(at_db);

                    }else{ //delete all users => add new User 

                        for(AssignedTerm at:atList){
                            puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, at.getId());
                            hb_session.delete(puff_db);
                        }

                        //add new user
                        at_db.setClassId(((ValueSet)obj).getId());
                        at_db.setClassname("ValueSet");
                        at_db.setCollaborationuser(user);
                        hb_session.save(at_db);
                    }
                }
                
                String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";
                
                Query q = hb_session.createQuery(hql);
                q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                List<Proposal> proposalList = q.list();
                Long privId = -1l;
                
                for(Proposal prop:proposalList){  
                    privId = -1l;
                    for(Privilege priv:prop.getPrivileges()){
                        if(priv.getCollaborationuser().getId().equals(user.getId()))
                            privId = priv.getId();
                    }
                    if(privId.equals(-1l)){
                    
                        Privilege priv = new Privilege();
                        priv.setCollaborationuser(new Collaborationuser());
                        priv.getCollaborationuser().setId(user.getId());

                        priv.setSendMail(user.getSendMail());
                        priv.setMayChangeStatus(true);
                        priv.setMayManageObjects(true);
                        
                        priv.setFromDate(new Date());
                        priv.setProposal(new Proposal());
                        priv.getProposal().setId(prop.getId());
                        priv.setDiscussiongroup(null);
                        hb_session.save(priv);
                    }else{
                    
                        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, privId);
                        priv_db.setMayChangeStatus(true);
                        priv_db.setMayManageObjects(true);
                        hb_session.update(priv_db);
                    }     
                }
                
                Mail.sendMailAUT(user, M_AUT.SV_ASSIGNMENT_SUBJECT, M_AUT.getInstance().getSvAssignementText(svAssignmentData.getTermName()));
                
                svAssignmentData.setAssignedTermId(at_db.getId());
                svAssignmentData.setCollaborationuserId(user.getId());
                svAssignmentData.setUsername(user.getUsername());
                svAssignmentData.setFirstName(user.getFirstName());
                svAssignmentData.setName(user.getName());
                svAssignmentData.setOrganisation(user.getOrganisation().getOrganisation());
            }else{
                //Update gleicher oder anderer user...
                AssignedTerm a = (AssignedTerm)hb_session.get(AssignedTerm.class, svAssignmentData.getAssignedTermId());
                Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, dlbu.getChosenDataList().iterator().next().getCollaborationuserId());
                Collaborationuser userOld = (Collaborationuser)hb_session.get(Collaborationuser.class, svAssignmentData.getCollaborationuserId());
                Long oldPrivId = -1l;
                Long privId = -1l;
                
                if(!a.getCollaborationuser().getId().equals(user.getId())){
                
                     String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";

                     Query q = hb_session.createQuery(hql);
                     q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                     q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                     List<Proposal> proposalList = q.list();
                     
                     for(Proposal prop:proposalList){  
                         
                         for(Privilege priv:prop.getPrivileges()){
                             if(priv.getCollaborationuser().getId().equals(user.getId()))
                                 privId = priv.getId();
                             
                             if(priv.getCollaborationuser().getId().equals(userOld.getId()))
                                 oldPrivId = priv.getId();
                         }
                         
                         if(privId.equals(-1l)){

                             Privilege priv = new Privilege();
                             priv.setCollaborationuser(new Collaborationuser());
                             priv.getCollaborationuser().setId(user.getId());

                             priv.setSendMail(user.getSendMail());
                             priv.setMayChangeStatus(true);
                             priv.setMayManageObjects(true);

                             priv.setFromDate(new Date());
                             priv.setProposal(new Proposal());
                             priv.getProposal().setId(prop.getId());
                             priv.setDiscussiongroup(null);
                             hb_session.save(priv);
                         }else{
                         
                             Privilege priv_db = (Privilege) hb_session.get(Privilege.class, privId);
                             priv_db.setMayChangeStatus(true);
                             priv_db.setMayManageObjects(true);
                             hb_session.update(priv_db);
                         }
                         
                         if((!oldPrivId.equals(-1l)) && !prop.getCollaborationuser().getId().equals(userOld.getId())){
                         
                             Privilege priv_db = (Privilege) hb_session.get(Privilege.class, oldPrivId);
                             if(priv_db != null){
                                priv_db.setCollaborationuser(null);
                                priv_db.setDiscussiongroup(null);
                                priv_db.setProposal(null);
                                userOld.getPrivileges().remove(priv_db);
                                prop.getPrivileges().remove(priv_db);
                                hb_session.delete(priv_db);
                             }
                         }
                     }
                }
                
                a.setCollaborationuser(null);
                a.setCollaborationuser(user);                
                hb_session.update(a);
                
                Mail.sendMailAUT(user, M_AUT.SV_ASSIGNMENT_SUBJECT, M_AUT.getInstance().getSvAssignementText(svAssignmentData.getTermName()));
                
                svAssignmentData.setCollaborationuserId(user.getId());
                svAssignmentData.setUsername(user.getUsername());
                svAssignmentData.setFirstName(user.getFirstName());
                svAssignmentData.setName(user.getName());
                svAssignmentData.setOrganisation(user.getOrganisation().getOrganisation());
            }
        }
        //Änderungen des Attributs autoRelease des CodeSystems/ValueSets speichern
        Checkbox checkbox = (Checkbox) getFellow("cAutoRelease");
        svAssignmentData.setAutoRelease(checkbox.isChecked());
        if(svAssignmentData.getClassname().equals("CodeSystem"))
        {
          CodeSystem cs = (CodeSystem)hb_session_term.get(CodeSystem.class, svAssignmentData.getClassId());
          cs.setAutoRelease(checkbox.isChecked());
          hb_session_term.update(cs);
        }
        else if(svAssignmentData.getClassname().equals("ValueSet"))
        {
          ValueSet vs = (ValueSet)hb_session_term.get(ValueSet.class, svAssignmentData.getClassId());
          vs.setAutoRelease(checkbox.isChecked());
          hb_session_term.update(vs);
        }
        
        hb_session_term.getTransaction().commit();
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        hb_session_term.getTransaction().rollback();
        logger.error("Fehler in SvAssignmentDetails.java (onOkClicked()): " + e.getMessage());
      }finally{
        hb_session_term.close();
        hb_session.close();
      }

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(svAssignmentData, !newEntry);
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in SvAssignmentDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
    
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @return the user
   */
  public SvAssignmentData getSvAssignmentData()
  {
    return svAssignmentData;
  }

  /**
   * @param user the user to set
   */
  public void setSvAssignmentData(SvAssignmentData svAssignmentData)
  {
    this.svAssignmentData = svAssignmentData;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }    
}
