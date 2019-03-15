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
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Becker
 */
public class ListitemRendererMetadataParameter implements ListitemRenderer{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public void render(Listitem lstm, Object o, int index) throws Exception {
        MetadataParameter mdp = (MetadataParameter)o;        
        
        Listcell cellName           = new Listcell(mdp.getParamName());
        Listcell cellDatatype       = new Listcell(mdp.getParamDatatype()); 
        Listcell cellParameterType  = new Listcell(mdp.getMetadataParameterType());   
        
        lstm.appendChild(cellName);
        lstm.appendChild(cellDatatype);
        lstm.appendChild(cellParameterType);
    }
}
