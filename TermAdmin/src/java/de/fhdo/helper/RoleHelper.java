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
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zul.ListModelList;

/**
 *
 * @author PU
 */
public class RoleHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> roles = null;
    
    public static Long getRoleIdByName(String role){        
        checkForNull();
        
        if(role == null || role.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : RoleHelper.getRoleTable().keySet()){
            if(RoleHelper.getRoleTable().get(key).compareToIgnoreCase(role) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getRoleTable() {        
        checkForNull();
        
        return roles;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List listRole = new ArrayList<String>();
        for(String role : RoleHelper.getRoleTable().values()){
            listRole.add(role);
        }
        ListModelList lm2 = new ListModelList(listRole);
        Comparator comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getRoleNameById(Long id){    
        checkForNull();
        
        String res = roles.get(String.valueOf(id));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){
        
        if(roles == null)
            createRoleTables();
    }
    
    private static void createRoleTables(){               
        roles = new HashMap<String, String>();

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
              
        // prüfen, ob Benutzer bereits existiert
        String hql = "from Role";
        Query q = hb_session.createQuery(hql);
        List dGroupList = q.list();
        hb_session.close();
        
        Iterator it = dGroupList.iterator();
        while(it.hasNext()){
            Role r = (Role)it.next();
            roles.put(String.valueOf(r.getId()), r.getName());
        }                        
    }
}
