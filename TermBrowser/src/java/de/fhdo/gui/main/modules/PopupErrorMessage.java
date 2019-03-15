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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public class PopupErrorMessage extends  GenericForwardComposer{     
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//    private CodeSystem          cs = null;
//    private CodeSystemVersion   csv = null;          
    private Window   window;
//    private Content  windowParent;       
    private Label    lErrorMessage;
    
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {        
        super.doAfterCompose(comp); 
        Exception e = (Exception)arg.get("Exception");
        String sMessage = (String)arg.get("ExceptionMessage");
        window       = (Window)comp;
        lErrorMessage.setValue(sMessage);
//        windowParent = (Content)comp.getParent();    
    }  
        
//    public void onClick$bClose(){          
//        window.detach();                                              
//    }
}