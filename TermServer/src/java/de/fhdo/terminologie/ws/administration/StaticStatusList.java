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
package de.fhdo.terminologie.ws.administration;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.2.26 checked
 * This class holds the current status of all imports using a concurrent hashmap.
 * @author Robert Mützner, Dario Bachinger
 */
public class StaticStatusList{
    
    private static final ConcurrentHashMap<Long,ImportStatus> statusList = new ConcurrentHashMap<Long, ImportStatus>();
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    /**
     * Deletes old status before returning the requested status.
     * @param importId the ID of the status to be added
     * @param status the status to be added
     */
    public static void addStatus(Long importId, ImportStatus status){
        //Removing old finished imports
        for(Entry<Long, ImportStatus> entry : statusList.entrySet()){
            if((!entry.getValue().importRunning) && (entry.getValue().getImportTotal() == entry.getValue().getImportCount())){
                try{
                    statusList.remove(entry.getKey());
                }
                catch(Exception e){
                    LOGGER.info(e.getLocalizedMessage());
                }
            }
        }
        
        //Adding new status
        statusList.put(importId, status);
    }
    
    /**
     * Returns the status which is linked to the given ID.
     * @param importId the ID of the status
     * @return the status of the ID
     */
    public static ImportStatus getStatus(Long importId){
        return statusList.get(importId);
    }
}