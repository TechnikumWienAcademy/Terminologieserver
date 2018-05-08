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

import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListRowType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import types.termserver.fhdo.de.CodeSystemMetadataValue;

/**
 *
 * @author Becker
 */
public class ComparatorProceedings implements Comparator{
    private boolean asc = false;
    
    public ComparatorProceedings(boolean ascending){
        asc = ascending;
    }
    
    public int compare(Object o1, Object o2) {
        GenericListRowType row1 = (GenericListRowType)o1,
                                row2 = (GenericListRowType)o2;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GenericListCellType[] arr1 = row1.getCells();
        GenericListCellType[] arr2 = row2.getCells();
        Date dat1 = null;
        Date dat2 = null;
        try {
            dat1 = sdf.parse((String)arr1[4].getData());
            dat2 = sdf.parse((String)arr2[4].getData());
        } catch (Exception ex) {
            dat1 = null;
            dat2 = null;
        }
        int v;
        if(dat1 == null || dat2 == null){
            v=0;
        }else{
            v = dat1.compareTo(dat2);
        }
        
        return asc ? v: -v;
    }  
}
