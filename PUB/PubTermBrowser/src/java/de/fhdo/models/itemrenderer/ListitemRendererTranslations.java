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

import de.fhdo.helper.LanguageHelper;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Becker
 */
public class ListitemRendererTranslations implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean editableTranslationsList;

    public ListitemRendererTranslations(boolean editableTranslationsList) {
        this.editableTranslationsList = editableTranslationsList;
    }
    
    public void render(Listitem lstm, Object o, int index) throws Exception {
        CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation)o;                        
        
        Listcell cellLanguage    = new Listcell(); 
        Listcell cellTranslation = new Listcell();
        
        
        cellLanguage.setLabel(LanguageHelper.getLanguageTable().get(String.valueOf(csct.getLanguageId())));
        if(editableTranslationsList){
            Textbox textBox = new Textbox();
            textBox.setText(csct.getTerm());
            textBox.setReadonly(true);
            textBox.setHflex("1");
            cellTranslation.appendChild(textBox);
        }else{
            cellTranslation.setLabel(csct.getTerm());
        }
                
        lstm.appendChild(cellLanguage);
        lstm.appendChild(cellTranslation);
    }    
}