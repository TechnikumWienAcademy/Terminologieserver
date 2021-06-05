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

import de.fhdo.models.comparators.ComparatorStrings;
import de.fhdo.terminologie.ws.search.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.ListMetadataParameterResponse;
import de.fhdo.terminologie.ws.search.OverallErrorCategory;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Philipp Urbauer
 */
public class MetadataParameterHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> paramNames = null;
    private static List<MetadataParameter> mpList = null;
    
    public static Long getMetadataParameterIdByName(String paramName){        
        checkForNull();
        
        if(paramName == null || paramName.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : MetadataParameterHelper.getMetadataParameterNameTable().keySet()){
            if(MetadataParameterHelper.getMetadataParameterNameTable().get(key).compareToIgnoreCase(paramName) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getMetadataParameterNameTable() {        
        checkForNull();
        
        return paramNames;
    }
    
    public static List<MetadataParameter> getMetadataParameterList(){        
        checkForNull();
        
        return mpList;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List listMetadataParameter = new ArrayList<String>();
        for(String paramName : MetadataParameterHelper.getMetadataParameterNameTable().values()){
            
            if(!listMetadataParameter.contains(paramName))
                listMetadataParameter.add(paramName);
        }
        ListModelList lm2 = new ListModelList(listMetadataParameter);
        Comparator comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getMetadataParameterNameById(Long paramNameId){    
        checkForNull();
        
        String res = paramNames.get(String.valueOf(paramNameId));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){        
        if(paramNames == null)
            createMetadataParameterTables();
    }
    
    private static void createMetadataParameterTables(){               
        paramNames = new HashMap<String, String>();

        ListMetadataParameterRequestType parameter   = new ListMetadataParameterRequestType();

        de.fhdo.terminologie.ws.search.LoginType login = new de.fhdo.terminologie.ws.search.LoginType();
        login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());             
        parameter.setLogin(login);

        ListMetadataParameterResponse.Return response = WebServiceHelper.listMetadataParameter(parameter);

        if(response != null && response.getReturnInfos().getStatus() == Status.OK){
            if(response.getReturnInfos().getOverallErrorCategory() != OverallErrorCategory.INFO){
                try {
                    Messagebox.show(Labels.getLabel("metadataParameterHelper.loadMetadataParameterFailed") + "\n\n" + response.getReturnInfos().getMessage());
                } catch (Exception ex) {logger.error("metadataParameterHelper.java Error loading MetadataParameter: " + ex);}
            }
            else{
                Iterator<MetadataParameter> it = response.getMetadataParameter().iterator();
                int x = 0;
                while(it.hasNext()){
                    MetadataParameter mp = it.next();
                    paramNames.put(String.valueOf(x++), mp.getParamName());
                }
                mpList = response.getMetadataParameter();
            }
        }                            
    }
}
