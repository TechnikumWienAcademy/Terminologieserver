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
public class PrivilegeComparatorUserInfo  implements Comparator{
 
    public static final int USER_INFO_FIRST_NAME = 0;
    public static final int USER_INFO_NAME = 1;
    public static final int USER_INFO_ORGANISTION = 2;
    
    private boolean asc = false;
    private int mode = -1;
    
    public PrivilegeComparatorUserInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        PrivilegeUserInfo dcui1 = (PrivilegeUserInfo)o1,
                                dcui2 = (PrivilegeUserInfo)o2;
        
        int v = -1;
        if(mode == USER_INFO_FIRST_NAME){
            v = dcui1.getFirstName().compareTo(dcui2.getFirstName());
        }
        if(mode == USER_INFO_NAME){
            v = dcui1.getName().compareTo(dcui2.getName());
        }
        if(mode == USER_INFO_ORGANISTION){
            v = dcui1.getOrganisation().compareTo(dcui2.getOrganisation());
        }
      
        return asc ? v: -v;
    } 
}
