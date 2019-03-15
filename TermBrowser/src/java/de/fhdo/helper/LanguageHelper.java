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

import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.models.comparators.ComparatorStrings;
import de.fhdo.terminologie.ws.search.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.ListDomainValuesResponse;
import de.fhdo.terminologie.ws.search.OverallErrorCategory;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.Domain;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author Becker
 */
public class LanguageHelper {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> languages;
    
    public static HashMap<String, String> getLanguageTable() {
        if (languages == null)
            createLanguageTable();
        
        return languages;
    }
    
    public static Long getLanguageIdByName(String language){
        if(language == null || language.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : LanguageHelper.getLanguageTable().keySet()){
            if(LanguageHelper.getLanguageTable().get(key).compareToIgnoreCase(language) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static ListModelList getListModelList(){
        List listLanguage = new ArrayList<String>();
        for(String language : LanguageHelper.getLanguageTable().values()){
            listLanguage.add(language);
        }
        ListModelList lm2 = new ListModelList(listLanguage);
        Comparator comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    private static void createLanguageTable(){       
        languages = new HashMap<String, String>();

        ListDomainValuesRequestType parameter   = new ListDomainValuesRequestType();

        parameter.setDomain(new Domain());
        parameter.getDomain().setDomainId((long)1); // 1 = Sprachen

        ListDomainValuesResponse.Return response = WebServiceHelper.listDomainValues(parameter);

        if(response != null && response.getReturnInfos().getStatus() == Status.OK){
            if(response.getReturnInfos().getOverallErrorCategory() != OverallErrorCategory.INFO){
                try {
                    Messagebox.show(Labels.getLabel("languageHelper.loadLanguageFailed") + "\n\n" + response.getReturnInfos().getMessage());
                } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}
            }
            else{
                Iterator<DomainValue> it = response.getDomainValues().iterator();
                while(it.hasNext()){
                    DomainValue dv = it.next();
                    languages.put(String.valueOf(dv.getDomainValueId()), dv.getDomainDisplay());
                }
            }
        }                            
    }    
}
