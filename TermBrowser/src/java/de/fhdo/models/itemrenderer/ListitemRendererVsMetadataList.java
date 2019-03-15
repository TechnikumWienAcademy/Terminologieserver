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
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.ValueSetMetadataValue;

/**
 *
 * @author Becker
 */
public class ListitemRendererVsMetadataList implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean editableMetadataList;

    public ListitemRendererVsMetadataList(boolean editableMetadataList) {
        this.editableMetadataList = editableMetadataList;
    }
    
    public void render(Listitem lstm, Object o, int index) throws Exception {
        ValueSetMetadataValue vsmv = (ValueSetMetadataValue)o;        
        
        Listcell cellMetadata = new Listcell();
        Listcell cellValue    = new Listcell();
        
        cellMetadata.setLabel(vsmv.getMetadataParameter().getParamName());
        if(editableMetadataList){
            Textbox textBox = new Textbox();
            textBox.setText(vsmv.getParameterValue());
            textBox.setReadonly(true);
            cellValue.appendChild(textBox);
        }else{
            cellValue.setLabel(vsmv.getParameterValue());
        }
        
        lstm.appendChild(cellMetadata);
        lstm.appendChild(cellValue);
    }    
}
