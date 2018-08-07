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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class StaticStatusList
{
    private static HashMap<Long,ImportStatus> _statusList = new HashMap<Long, ImportStatus>();
    
    public static void addStatus(Long importId, ImportStatus status)
    {
        //removing finished imports to release memory
        
        Iterator it = _statusList.entrySet().iterator();
        while(it.hasNext()){
            Entry<Long, ImportStatus> item = (Entry<Long, ImportStatus>) it.next();
            it.remove();
        }
        
        for(Entry<Long, ImportStatus> entry : _statusList.entrySet())
        {
            if((!entry.getValue().importRunning) && (entry.getValue().getImportTotal() == entry.getValue().getImportCount()))
            {
                _statusList.remove(entry.getKey());
            }
        }
        
        //adding new Status
        _statusList.put(importId, status);
    }
    
    public static ImportStatus getStatus(Long importId)
    {
        return _statusList.get(importId);
    }
    
}
