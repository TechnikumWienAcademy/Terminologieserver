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

import de.fhdo.helper.ParameterHelper;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Sven Becker
 */
public class ContentVSV extends Window implements AfterCompose {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    int lastSelection = -1;
    long ValueSetVersionId = 0, ValueSetId = 0;    

    public ContentVSV() {
        // Argumente laden
        ValueSetId = ParameterHelper.getLong("ValueSetId");
        ValueSetVersionId = ParameterHelper.getLong("ValueSetVersionId");
    }

    public void afterCompose() {
        onTabSelect();
    }

    public void onTabSelect() {
        Include inc = null;
        String src = "";
        Tabbox tb = (Tabbox) getFellow("tabboxFilter");
        int sel = tb.getSelectedIndex();

        if (sel == lastSelection) {
            return;
        }

        switch (sel) {
            case 0:  // XXX
                inc = (Include) getFellow("incXXX");
                src = "modules/ContentConceptsVSV.zul?ValueSetVersionId=" + ValueSetVersionId + "&ValueSetId=" + ValueSetId;
                break;
        }
        inc.setSrc(null);
        inc.setSrc(src);        
        lastSelection = sel;
    }

    public void onFilterSelect() {
    }
}
