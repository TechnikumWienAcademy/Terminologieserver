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
package de.fhdo.gui.main.modules;

import de.fhdo.gui.main.ContentCSVSAssociationEditor;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public class AssociationEditor extends Window implements AfterCompose {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    protected ContentCSVSAssociationEditor windowL,
                                           windowR;        
        
    public void afterCompose() {  
//        ((Borderlayout)getRoot()).setTitle(Labels.getLabel("common.editor"));        
        
        Radiogroup rgMode = (Radiogroup)getFellow("rgMode");
        rgMode.addEventListener(Events.ON_CHECK, new EventListener() {
            public void onEvent(Event event) throws Exception {                                
//                if(windowL != null && windowL.getWindowContentConcepts() != null)
//                    windowL.getWindowContentConcepts().setAssociationMode(getAssociationMode());
//                if(windowR != null && windowR.getWindowContentConcepts() != null)
//                    windowR.getWindowContentConcepts().setAssociationMode(getAssociationMode());
            }
        });
        
        Include incL = (Include) getFellow("incInhalteLinks");                                                        
        incL.setDynamicProperty("radioGroupAssociationMode", rgMode);
        incL.setSrc(null);        
        incL.setSrc("./ContentCSVSAssociationEditor.zul");                
        windowL = (ContentCSVSAssociationEditor)incL.getFellow("windowCSVS");
        
        Include incR = (Include) getFellow("incInhalteRechts");                                                                
        incR.setDynamicProperty("radioGroupAssociationMode", rgMode);
        incR.setSrc(null);
        incR.setSrc("./ContentCSVSAssociationEditor.zul");
        windowR = (ContentCSVSAssociationEditor)incR.getFellow("windowCSVS");                        
    }

    public int getAssociationMode() {
        return Integer.valueOf(((Radiogroup)getFellow("rgMode")).getSelectedItem().getValue().toString());  // TODO überprüfen, ob .toString() funktionert
    } 
}