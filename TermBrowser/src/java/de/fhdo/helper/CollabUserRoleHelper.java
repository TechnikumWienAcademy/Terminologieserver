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
import de.fhdo.collaboration.db.classes.Role;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zul.ListModelList;

/**
 *
 * @author Philipp Urbauer
 */
public class CollabUserRoleHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> collabUserRoles = null;
    
    public static Long getCollabUserRoleIdByName(String collabUserRole){        
        checkForNull();
        
        if(collabUserRole == null || collabUserRole.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : CollabUserRoleHelper.getCollabUserRoleTable().keySet()){
            if(CollabUserRoleHelper.getCollabUserRoleTable().get(key).compareToIgnoreCase(collabUserRole) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getCollabUserRoleTable() {        
        checkForNull();
        
        return collabUserRoles;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List<String> listCollabUserRole = new ArrayList<String>();
        for(String collabUserRole : CollabUserRoleHelper.getCollabUserRoleTable().values()){
            listCollabUserRole.add(collabUserRole);
        }
        ListModelList lm2 = new ListModelList(listCollabUserRole);
        ComparatorStrings comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getCollabUserRoleNameById(Long domainValueId){    
        checkForNull();
        
        String res = collabUserRoles.get(String.valueOf(domainValueId));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){
        
        if(collabUserRoles == null)
            createCollabUserRoleTables();
    }
    
    private static void createCollabUserRoleTables(){               
        collabUserRoles = new HashMap<String, String>();

        Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();
        String hql = "select distinct r from Role r";
        
        try{
        
            List<Role> roleList = hb_session_kollab.createQuery(hql).list();
            
            Iterator<Role> it = roleList.iterator();
            while(it.hasNext()){
                    Role r = it.next();
                    if(!r.getName().equals("Rezensent"))
                        collabUserRoles.put(String.valueOf(r.getId()), r.getName());
                }
            
        }catch(Exception e){
          logger.error("[Fehler bei CollabUserRoleHelper.java createCollabUserRoleTable(): " + e.getMessage());
        }
        finally
        {
          hb_session_kollab.close();
          
        }                            
    }
}
