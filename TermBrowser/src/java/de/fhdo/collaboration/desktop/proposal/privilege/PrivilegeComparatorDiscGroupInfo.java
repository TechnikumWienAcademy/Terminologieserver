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
package de.fhdo.collaboration.desktop.proposal.privilege;

import java.util.Comparator;

/**
 *
 * @author Philipp Urbauer
 */
public class PrivilegeComparatorDiscGroupInfo  implements Comparator{
 
    public static final int DG_INFO_GROUP_NAME = 0;
    public static final int DG_INFO_GROUP_HEAD = 1;
    
    private boolean asc = false;
    private int mode = -1;
    public PrivilegeComparatorDiscGroupInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        PrivilegeDiscGroupInfo dgvi1 = (PrivilegeDiscGroupInfo)o1,
                                dgvi2 = (PrivilegeDiscGroupInfo)o2;
        
        int v = -1;
        if(mode == DG_INFO_GROUP_NAME){
            v = dgvi1.getDiscussionGroupName().compareTo(dgvi2.getDiscussionGroupName());
        }
        if (mode == DG_INFO_GROUP_HEAD){
            v = dgvi1.getDiscussionGroupHead().compareTo(dgvi2.getDiscussionGroupHead());
        }

        return asc ? v: -v;
    } 
}
