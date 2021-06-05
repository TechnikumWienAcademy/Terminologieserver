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

import de.fhdo.models.TreeNode;
import java.util.Comparator;
import types.termserver.fhdo.de.CodeSystemEntityVersion;

/**
 *
 * @author Philipp Urbauer
 */
public class ComparatorOrderNr implements Comparator{
    private boolean asc;
    
    public ComparatorOrderNr(boolean asc){
        this.asc = asc;
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        if(o1 instanceof TreeNode && o2 instanceof TreeNode){
            TreeNode tn1 = (TreeNode)o1;
            TreeNode tn2 = (TreeNode)o2;

            int v = tn1.getCvsm().getOrderNr().compareTo(tn2.getCvsm().getOrderNr());
            return asc ? v: -v;
        }
        else if(o1 instanceof CodeSystemEntityVersion && o1 instanceof CodeSystemEntityVersion){
            CodeSystemEntityVersion csev1 = (CodeSystemEntityVersion)o1;
            CodeSystemEntityVersion csev2 = (CodeSystemEntityVersion)o2;
            if(csev1.getConceptValueSetMemberships().isEmpty() == false && csev2.getConceptValueSetMemberships().isEmpty() == false){
                int v = csev1.getConceptValueSetMemberships().get(0).getOrderNr().compareTo(csev2.getConceptValueSetMemberships().get(0).getOrderNr());
                return asc ? v: -v;
            }
        }
        return -1;
    }  
}
