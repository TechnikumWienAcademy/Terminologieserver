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

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;

/**
 *
 * @author Becker
 */
public class ListitemRendererLinkedConcepts implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private long versionId;
    
    public ListitemRendererLinkedConcepts(long id){
        versionId = id;
    }
    
    public void render(Listitem lstm, Object o, int index) throws Exception {  
        CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation)o;                        
        
        Listcell cellAssociationType = new Listcell();
        Listcell cellEntity          = new Listcell(); 
        
        CodeSystemEntityVersion csev1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1(),
                                csev2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2(),
                                csev  = null;
        
        if(csev1 != null && csev1.getVersionId().equals(versionId) == false)
            csev = csev1;                      
        else if(csev2 != null && csev2.getVersionId().equals(versionId) == false)
            csev = csev2;                           
                
        if(cseva.getLeftId().equals(versionId))
            cellAssociationType.setLabel(cseva.getAssociationType().getReverseName());        
        else
            cellAssociationType.setLabel(cseva.getAssociationType().getForwardName());                
        
        if(csev != null)
            cellEntity.setLabel(csev.getCodeSystemConcepts().get(0).getTerm());        
        
        lstm.appendChild(cellAssociationType);
        lstm.appendChild(cellEntity);        
    }        
}