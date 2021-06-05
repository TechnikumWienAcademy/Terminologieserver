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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import java.util.Comparator;


/**
 *
 * @author Philipp Urbauer
 */
public class DateComparator implements Comparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    //desc date sort
    public int compare(Object o1, Object o2) {
        CodeSystemEntityVersion csev1 = (CodeSystemEntityVersion)o1,
                                csev2 = (CodeSystemEntityVersion)o2;
        
        long t1 = csev1.getInsertTimestamp().getTime();
        long t2 = csev2.getInsertTimestamp().getTime();
        
        if(t2 > t1)
            return 1;
        else if(t1 > t2)
            return -1;
        else
            return 0;
    }  
}
