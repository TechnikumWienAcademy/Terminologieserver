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

import de.fhdo.models.TreeModelCS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Becker
 */
public class RUDIHelper {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    static int  RUDI_ACTION_READ   = 1,
                RUDI_ACTION_UPDATE = 2,
                RUDI_ACTION_DELETE = 3,
                RUDI_ACTION_INSERT = 4;
    // Map nach CSV mit weiterer Map, welcher user (long) welche Aktionen List<int> durchführen darf   
    static protected HashMap<Long, HashMap<Long, ArrayList<Integer>>> userMap = new HashMap<Long, HashMap<Long, ArrayList<Integer>>>();
    
    static public void createRUDIEntry(long userId){
        HashMap<Long, ArrayList<Integer>> csvMap = new HashMap<Long, ArrayList<Integer>>();
        ArrayList<Integer> listRights  = new ArrayList<Integer>();

        // Durchlaufe alle CS,VS,... und schaue nach den Rechten
        Iterator<CodeSystemVersion> itCSV = TreeModelCS.getCsvList().iterator();
        while(itCSV.hasNext()){         
            CodeSystemVersion csv = itCSV.next();
            listRights.clear();

            // Suche nach Rechten
            if(csv.isUnderLicence()){
                // Rechte Laden  WS aufruf
                if(false){
                    // Rechte verteilen
                }
            }
            else{
                listRights.add(RUDI_ACTION_READ);
                listRights.add(RUDI_ACTION_UPDATE);
                listRights.add(RUDI_ACTION_DELETE);
                listRights.add(RUDI_ACTION_INSERT);            
            } 
            
            // Je eine Map<versionId, Rechteliste> in die Map einfügen
            csvMap.put(csv.getPreviousVersionId(), listRights);
        }        

        // CSVMap dem User zuordnen
        userMap.put(userId, csvMap);             
    }
    
    static public boolean actionAllowed(long userId, long versionId, int action){                
        boolean allowed = false;
        
        if(userMap.containsKey(userId) ){
            if(userMap.get(userId).containsKey(versionId)){
                if(userMap.get(userId).get(versionId).contains(action)){
                    allowed = true;    
                }
                else{
                    // Meldung: Aktion nicht in Liste => nicht erlaubt
                    allowed = false;
                }                    
            }
            else{
                try {
                    Messagebox.show(Labels.getLabel("rudiHelper.csvNotInAccessList"));
                    // Meldung: User nicht in Liste
                } catch (Exception ex) {
                    Logger.getLogger(RUDIHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
        }
        else{
            try {
                Messagebox.show(Labels.getLabel("rudiHelper.userNotInAccessList"));
                // Meldung: User nicht in Liste
            } catch (Exception ex) {
                Logger.getLogger(RUDIHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        return allowed;              
    }
    
}
