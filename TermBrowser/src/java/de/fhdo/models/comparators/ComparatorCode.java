/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.models.comparators;

import de.fhdo.helper.RomanToDecimal;
import java.util.Comparator;
import types.termserver.fhdo.de.CodeSystemEntityVersion;

/**
 *
 * @author puraner
 */
public class ComparatorCode implements Comparator<Object>
{
    
    @Override
    public int compare(Object o1, Object o2)
    {
        if(o1 instanceof CodeSystemEntityVersion)
        {
            CodeSystemEntityVersion csev1 = (CodeSystemEntityVersion) o1;
            CodeSystemEntityVersion csev2 = (CodeSystemEntityVersion) o2;
            
            String s1 = RomanToDecimal.romanToDecimal(csev1.getCodeSystemConcepts().get(0).getCode());
            String s2 = RomanToDecimal.romanToDecimal(csev2.getCodeSystemConcepts().get(0).getCode());
            
            try
            {
                Integer int1 = Integer.parseInt(s1);
                Integer int2 = Integer.parseInt(s2);
                return int1.compareTo(int2);
            }
            catch (NumberFormatException ex)
            {
                return s1.compareTo(s2);
            }
        }
        
        return new ComparatorStrings().compare(o1, o2);
    }
    
}
