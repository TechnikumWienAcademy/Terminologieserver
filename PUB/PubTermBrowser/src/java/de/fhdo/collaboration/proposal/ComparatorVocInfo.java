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
package de.fhdo.collaboration.proposal;

import java.util.Comparator;

/**
 *
 * @author Philipp Urbauer
 */
public class ComparatorVocInfo  implements Comparator{
 
    public static final int VOC_INFO_VOCABULARY_NAME = 0;
    public static final int VOC_INFO_VERSION_NAME = 1;
    
    private boolean asc = false;
    private int mode = -1;
    public ComparatorVocInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        VocInfo dgvi1 = (VocInfo)o1,
                                dgvi2 = (VocInfo)o2;
        
        int v = -1;
        if(mode == VOC_INFO_VOCABULARY_NAME){
            v = dgvi1.getVocabularyName().compareTo(dgvi2.getVocabularyName());
        }
        if (mode == VOC_INFO_VERSION_NAME){
            v = dgvi1.getVersionName().compareTo(dgvi2.getVersionName());
        }

        return asc ? v: -v;
    } 
}
