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

import java.text.SimpleDateFormat;
import java.util.Date;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class UtilHelper {
  
    public static String getDisplayNameLong(Object o){
        String s = "x";
        
        if(o instanceof CodeSystem)
            s = ((CodeSystem)o).getName();
        else if (o instanceof CodeSystemVersion){
            CodeSystemVersion csv = (CodeSystemVersion)o;
            
            if(csv.getName().contains(csv.getCodeSystem().getName()) == false)                             
                s = csv.getCodeSystem().getName() + ": "+csv.getName();                                                                         
            else
                s = csv.getName();
        }
        else if(o instanceof ValueSet){
            s = ((ValueSet)o).getName();
        }
        else if (o instanceof ValueSetVersion){
            ValueSetVersion vsv = (ValueSetVersion)o;
            Date d = vsv.getInsertTimestamp().toGregorianCalendar().getTime();  
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");        
            
						//Matthias 28.05.2015 changed in order show the version number in global search 
						//s = vsv.getValueSet().getName() + ": " +  sdf.format(d);
						s = vsv.getValueSet().getName() + ": " + vsv.getName();
        }
        else if (o instanceof DomainValue){
            s = ((DomainValue)o).getDomainDisplay();
        }     
        else if(o instanceof CodeSystemEntityVersion){
            throw new UnsupportedOperationException("displayName of CSEV not implemented yet!");
        }
        else if(o instanceof CodeSystemEntityVersionAssociation){
            throw new UnsupportedOperationException("displayName of CSEVA not implemented yet!");
        }
        else if(o instanceof CodeSystemConcept){
            throw new UnsupportedOperationException("displayName of CSC not implemented yet!");
        }
        else if(o instanceof CodeSystemConceptTranslation){
            throw new UnsupportedOperationException("displayName of CSCT not implemented yet!");
        }
        
        return s;
    }
}
