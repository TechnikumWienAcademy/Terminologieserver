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
package de.fhdo.models.itemrenderer;

import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.TreeModelCS;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Becker
 */
public class ListitemRendererCrossmapping implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private long versionId;
    
    public ListitemRendererCrossmapping(long id){
        logger.debug("ListitemRendererCrossmapping: csevId = " + id );
        versionId = id;
        
    }
    
    @Override
    public void render(Listitem lstm, Object o, int index) throws Exception {
        CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation)o;                        
        
        Listcell cellEntity          = new Listcell();
        Listcell cellCodeSystem      = new Listcell();
        Listcell cellCode            = new Listcell();
        
        CodeSystemEntityVersion csev1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1(),
                                csev2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2(),
                                csev  = null;
        
        if(csev1 != null && csev1.getVersionId().equals(versionId) == false)
            csev = csev1;             
        else if(csev2 != null && csev2.getVersionId().equals(versionId) == false )
            csev = csev2;       
        
        if(csev != null){
            // Term von csev1/2
            cellEntity.setLabel(csev.getCodeSystemConcepts().get(0).getTerm());
            
            // Code von csev1/2
            cellCode.setLabel(csev.getCodeSystemConcepts().get(0).getCode());
            
            // CodeSystem von csev1/2 herausfinden            
            cellCodeSystem.setLabel(getCSNameByCSEV(csev));                         
        }
        lstm.appendChild(cellEntity);
        lstm.appendChild(cellCode);
        lstm.appendChild(cellCodeSystem);        
    }    
    
    private String getCSNameByCSEV(CodeSystemEntityVersion csev){
        String s = "";
        try{
            s = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0).getCodeSystemVersion().getCodeSystem().getName();
        } catch(Exception e){            
            ReturnConceptDetailsRequestType parameter   = new ReturnConceptDetailsRequestType();        
            //Search                          port_Search = new Search_Service().getSearchPort();
            
            // Load Details
            
            // CSE(V)
            CodeSystemEntity cseTemp = new CodeSystemEntity();
            CodeSystemEntityVersion csevTemp = new CodeSystemEntityVersion();
            cseTemp.getCodeSystemEntityVersions().add(csevTemp);
            csevTemp.setVersionId(csev.getVersionId());            
            cseTemp.setId(csev.getCodeSystemEntity().getId());
            parameter.setCodeSystemEntity(cseTemp);

            ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(parameter);

            try{              
                // CS(V) finden
                if(response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().isEmpty() == false){
                    Long csvId = response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0).getId().getCodeSystemVersionId();                                
                    for(CodeSystemVersion csv : TreeModelCS.getCsvList()){                        
                        if(csv.getVersionId().compareTo(csvId) == 0){
                            return csv.getCodeSystem().getName();
                        }
                    }                
                }
            }
            catch(Exception e2){
                
            }
        
            return "N/A";
        }
        
        return s;
    }

  
}
