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

import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.ws.search.ListDomainValues;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author PU
 */
public class ValidityRangeHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> validityRanges = null;
    
    public static Long getValidityRangeIdByName(String validityRange){        
        checkForNull();
        
        if(validityRange == null || validityRange.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : ValidityRangeHelper.getValidityRangeTable().keySet()){
            if(ValidityRangeHelper.getValidityRangeTable().get(key).compareToIgnoreCase(validityRange) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getValidityRangeTable() {        
        checkForNull();
        
        return validityRanges;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List listValidityRange = new ArrayList<String>();
        for(String validityRange : ValidityRangeHelper.getValidityRangeTable().values()){
            listValidityRange.add(validityRange);
        }
        ListModelList lm2 = new ListModelList(listValidityRange);
        Comparator comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getValidityRangeNameById(Long domainValueId){    
        checkForNull();
        
        String res = validityRanges.get(String.valueOf(domainValueId));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){
        
        if(validityRanges == null)
            createValidityRangeTables();
    }
    
    private static void createValidityRangeTables(){               
        validityRanges = new HashMap<String, String>();

        ListDomainValuesRequestType parameter   = new ListDomainValuesRequestType();

        parameter.setDomain(new Domain());
        parameter.getDomain().setDomainId((long)9); // 9 = Validity Frage: Range Extern konfigurierbar oder hardcoded..
				
				ListDomainValues domainValues = new ListDomainValues();
				ListDomainValuesResponseType response = domainValues.ListDomainValues(parameter);

        if(response != null && response.getReturnInfos().getStatus() == ReturnType.Status.OK){
            if(response.getReturnInfos().getOverallErrorCategory() != ReturnType.OverallErrorCategory.INFO){
                try {
                    Messagebox.show(Labels.getLabel("validityRangeHelper.loadValidityRangeFailed") + "\n\n" + response.getReturnInfos().getMessage());
                } catch (Exception ex) {logger.error("ValidityRangeHelper.java Error loading ValidityRange: " + ex);}
            }
            else{
                Iterator<DomainValue> it = response.getDomainValues().iterator();
                while(it.hasNext()){
                    DomainValue dv = it.next();
                    validityRanges.put(String.valueOf(dv.getDomainValueId()), dv.getDomainDisplay());
                }
            }
        }                            
    }
		
		private static class ComparatorStrings implements Comparator{        
			private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

			public int compare(Object o1, Object o2) {
					String s1 = (String)o1,
								 s2 = (String)o2;

					return s1.compareTo(s2);       
			}  
}
}
