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
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.helper.SessionHelper;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert M�tzner
 */
public class AssignTermHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private static ArrayList<AssignedTerm> assignedTerms = null;
  private static ArrayList<AssignedTerm> allAssignedTerms = null;
  private static Long userId = 0l;
  
  public AssignTermHelper()
  {
    
  }

  public static boolean isUserAllowed(Object o){
      
      if(userId.equals(0l) || !userId.equals(SessionHelper.getCollaborationUserID())){
          createAssignedTermsList();
          userId = SessionHelper.getCollaborationUserID();
      }    
      if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
        if(o instanceof CodeSystem){

          for(AssignedTerm at:assignedTerms){
            if(at.getClassId().equals(((CodeSystem)o).getId()) && at.getClassname().equals("CodeSystem"))
                    return true;
          }
        } else if(o instanceof ValueSet){
          for(AssignedTerm at:assignedTerms){
              if(at.getClassId().equals(((ValueSet)o).getId()) && at.getClassname().equals("ValueSet"))
                      return true;
          }
        }
      }else{
          return true;
      }
      return false;
  }
  
  public static boolean isUserAllowed(Long id, String type){
      
      
      
      if(userId.equals(0l) || !userId.equals(SessionHelper.getCollaborationUserID())){
          createAssignedTermsList();
          userId = SessionHelper.getCollaborationUserID();
      }    
      if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
        if(type.equals("CodeSystem")){

          for(AssignedTerm at:assignedTerms){
            if(at.getClassId().equals(id) && at.getClassname().equals("CodeSystem"))
                    return true;
          }
        } else if(type.equals("ValueSet")){
          for(AssignedTerm at:assignedTerms){
              if(at.getClassId().equals(id) && at.getClassname().equals("ValueSet"))
                      return true;
          }
        }
      }else{
          return true;
      }
      return false;
  }
  
  public static void assignTermToUser(Object obj)
  {
    
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    
    try
    {
      if(SessionHelper.isCollaborationActive()){
          
          AssignedTerm at_db = new AssignedTerm();
          AssignedTerm puff_db;
          Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());
          
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
      }else{
      
          Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user");
          q.setString("p_user", SessionHelper.getUserName());
          java.util.List<Collaborationuser> userList = q.list();
          
          if(userList.size() == 1){
            AssignedTerm at_db = new AssignedTerm();
            AssignedTerm puff_db;
            if(obj instanceof CodeSystem){

                //First Check if already exists or other user is connected!hb_sessionhb_session
            String hqlTermAss = "select distinct at from AssignedTerm at join fetch at.collaborationuser cu where at.classId=:classId and at.classname=:classname";
            Query qTermAss = hb_session.createQuery(hqlTermAss);
            qTermAss.setParameter("classname", "CodeSystem");
            qTermAss.setParameter("classId", ((CodeSystem)obj).getId());
            
            List<AssignedTerm> atList = qTermAss.list();
            
            if(atList.size() == 1){ 
            
                if(atList.get(0).getCollaborationuser().getId() == userList.get(0).getId()){ //UserId == UserId => do nothing, because no double assignments
                   
                }else{ //delete old user => add new User
                    
                    //get old user and delete!
                    puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, atList.get(0).getId());
                    hb_session.delete(puff_db);
                    
                    //add new user
                    at_db.setClassId(((CodeSystem)obj).getId());
                    at_db.setClassname("CodeSystem");
                    at_db.setCollaborationuser(userList.get(0));
                    hb_session.save(at_db);
                }
            }else if(atList.isEmpty()){ //add new User
                
                //add new user
                at_db.setClassId(((CodeSystem)obj).getId());
                at_db.setClassname("CodeSystem");
                at_db.setCollaborationuser(userList.get(0));
                hb_session.save(at_db);
                
            }else{ //delete all users => add new User 
            
                for(AssignedTerm at:atList){
                    puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, at.getId());
                    hb_session.delete(puff_db);
                }
                
                //add new user
                at_db.setClassId(((CodeSystem)obj).getId());
                at_db.setClassname("CodeSystem");
                at_db.setCollaborationuser(userList.get(0));
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

                    if(atList.get(0).getCollaborationuser().getId() == userList.get(0).getId()){ //UserId == UserId => do nothing, because no double assignments

                    }else{ //delete old user => add new User

                        //get old user and delete!
                        puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, atList.get(0).getId());
                        hb_session.delete(puff_db);

                        //add new user
                        at_db.setClassId(((ValueSet)obj).getId());
                        at_db.setClassname("ValueSet");
                        at_db.setCollaborationuser(userList.get(0));
                        hb_session.save(at_db);
                    }
                }else if(atList.isEmpty()){ //add new User

                    //add new user
                    at_db.setClassId(((ValueSet)obj).getId());
                    at_db.setClassname("ValueSet");
                    at_db.setCollaborationuser(userList.get(0));
                    hb_session.save(at_db);

                }else{ //delete all users => add new User 

                    for(AssignedTerm at:atList){
                        puff_db = (AssignedTerm)hb_session.get(AssignedTerm.class, at.getId());
                        hb_session.delete(puff_db);
                    }

                    //add new user
                    at_db.setClassId(((ValueSet)obj).getId());
                    at_db.setClassname("ValueSet");
                    at_db.setCollaborationuser(userList.get(0));
                    hb_session.save(at_db);
                }
            }
          }else{
              //Keine Zuweisung
          }
      }
      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      logger.error("Fehler in AssignTermHelper assignTermToUser.java: " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }
  
  public static ArrayList<AssignedTerm> getUsersAssignedTerms(){        
        createAssignedTermsList();
        return assignedTerms;
  }

  public static void createAssignedTermsList()
  {
    assignedTerms = new ArrayList<AssignedTerm>();
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      
          Query q = hb_session.createQuery("from Collaborationuser WHERE id=:p_id");
          q.setParameter("p_id", SessionHelper.getCollaborationUserID());
          java.util.List<Collaborationuser> userList = q.list();
          
          if(userList.size() == 1){
              for(AssignedTerm at:userList.get(0).getAssignedTerms()){
                  assignedTerms.add(at);
              }
          }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
      logger.error("Fehler in AssignTermHelper getUserAssignedTerms(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }
  
  public static boolean isAnyUserAssigned(Long id,String type){        
        createAllAssignedTermsList();
        boolean result = false;
        
        for(AssignedTerm at:allAssignedTerms){
            
            if(at.getClassname().equals(type) && at.getClassId().equals(id)){
                result = true;
                break;
            }
        }
        
        return result;
  }
  
  public static void createAllAssignedTermsList()
  {
    allAssignedTerms = new ArrayList<AssignedTerm>();
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
        Query q = hb_session.createQuery("from AssignedTerm");
        java.util.List<AssignedTerm> atList = q.list();

        for(AssignedTerm at:atList){
            allAssignedTerms.add(at);
        }

        //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
      logger.error("Fehler in AssignTermHelper getAllAssignedTerms(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }
}