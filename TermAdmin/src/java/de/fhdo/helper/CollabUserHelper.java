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
package de.fhdo.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.hibernate.TermUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zul.ListModelList;

/**
 *
 * @author Philipp Urbauer
 */
public class CollabUserHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> collabUsernames = null;
    
    public static Long getCollabUsernameIdByName(String collabUsername){        
        checkForNull();
        
        if(collabUsername == null || collabUsername.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : CollabUserHelper.getCollabUsernameTable().keySet()){
            if(CollabUserHelper.getCollabUsernameTable().get(key).compareToIgnoreCase(collabUsername) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getCollabUsernameTable() {        
        checkForNull();
        
        return collabUsernames;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List<String> listCollabUsername = new ArrayList<String>();
        for(String collabUsername : CollabUserHelper.getCollabUsernameTable().values()){
            listCollabUsername.add(collabUsername);
        }
        ListModelList lm2 = new ListModelList(listCollabUsername);
        ComparatorStrings comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getCollabUsernameNameById(Long domainValueId){    
        checkForNull();
        
        String res = collabUsernames.get(String.valueOf(domainValueId));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){
        //the following check has been removed in order to query the DB in any case
        //if(collabUsernames == null)
            createCollabUsernameTables();
    }
    
    public static void reloadModel(){
    
        collabUsernames = null;
    }
    
    private static void createCollabUsernameTables(){               
        collabUsernames = new HashMap<String, String>();

        
        //Nur die Benutzer hohlen welche noch nicht zur Termserver DB "zugeordnet" sind!!!
        Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();
        Session hb_session_term = de.fhdo.db.HibernateUtil.getSessionFactory().openSession();
        //hb_session_term.getTransaction().begin();
        
        String hqlC = "select distinct cu from Collaborationuser cu where cu.hidden=false AND deleted=0";
        String hqlT = "select distinct tu from TermUser tu";
        
        try{
        
            List<Collaborationuser> userListC = hb_session_kollab.createQuery(hqlC).list();
            List<TermUser> userListT = hb_session_term.createQuery(hqlT).list();
            
            for(Collaborationuser cu:userListC){
                
                if(cu.getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN) ||
                   cu.getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER)){
                   
                        boolean found = false;
                        for(TermUser tu:userListT){

                            if((cu.getUsername() + "_tadm").equals(tu.getName())){
                                found = true;
                                break;
                            }
                        }

                        if(!found)
                            collabUsernames.put(String.valueOf(cu.getId()), cu.getUsername() + "_tadm");
                }
            }
            
        }catch(Exception e){
          logger.error("[Fehler bei CollabUserHelper.java createCollabUserTable(): " + e.getMessage());
        }
        finally
        {
          hb_session_kollab.close();
          hb_session_term.close();
        }                            
    }
}
