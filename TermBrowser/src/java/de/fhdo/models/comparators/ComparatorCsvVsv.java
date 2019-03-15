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
package de.fhdo.models.comparators;

import de.fhdo.helper.UtilHelper;
import de.fhdo.models.TreeNode;
import org.zkoss.zul.TreeitemComparator;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class ComparatorCsvVsv extends TreeitemComparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean     ascending = false;   
    
    public ComparatorCsvVsv(boolean asc){
        ascending = asc;      
    }
    
    @Override
    public int compare(Object o1, Object o2) {  
        String s1="", s2="";
        o1 = ((TreeNode)o1).getData();
        o2 = ((TreeNode)o2).getData();
        
        if(o1 instanceof CodeSystemVersion)
            s1 = UtilHelper.getDisplayNameLong(o1);
        else if(o1 instanceof ValueSetVersion)
            s1 = UtilHelper.getDisplayNameLong(o1);    
        else if (o1 instanceof ValueSet)
            s1 = UtilHelper.getDisplayNameLong(o1);      
        else if (o1 instanceof CodeSystem)
            s1 = UtilHelper.getDisplayNameLong(o1);       
        
        if(o2 instanceof CodeSystemVersion)
            s2 = UtilHelper.getDisplayNameLong(o2);      
        else if(o2 instanceof ValueSetVersion)
            s2 = UtilHelper.getDisplayNameLong(o2);       
        else if (o2 instanceof ValueSet)
            s2 = UtilHelper.getDisplayNameLong(o2);   
        else if (o2 instanceof CodeSystem)
            s2 = UtilHelper.getDisplayNameLong(o2);  
     
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int v = s1.compareTo(s2);        
        return ascending ? v: -v;            
    }    
}