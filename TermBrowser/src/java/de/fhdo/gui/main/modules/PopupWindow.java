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

import de.fhdo.terminologie.ws.authoring.VersioningType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public abstract class PopupWindow extends GenericForwardComposer{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    public final static int  EDITMODE_DETAILSONLY = 1,
                             EDITMODE_CREATE = 2,
                             EDITMODE_MAINTAIN_VERSION_NEW = 3,
                             EDITMODE_MAINTAIN = 4,
                             EDITMODE_MAINTAIN_VERSION_EDIT = 5,
                             EDITMODE_UPDATESTATUS = 6,
                             EDITMODE_UPDATESTATUS_VERSION = 7,  
                             EDITMODE_COPYVERSION_NEW = 8;
    
    protected AnnotateDataBinder binder;
    protected VersioningType     versioning;;  
    protected int                editMode = EDITMODE_DETAILSONLY;  
    protected Window             window;
    protected Window             windowParent;  
    
    abstract public void doAfterComposeCustom();
    abstract protected void initializeDatabinder();
    abstract protected void loadDatesIntoGUI();
    
    /* Editmodes legen fest, welche GUI-Elemente sichtbar/aktiv sind und erstellen/laden die nötigen Objekte */
    abstract protected void editmodeDetails();
    abstract protected void editmodeCreate();
    abstract protected void editmodeMaintainVersionNew();
    abstract protected void editmodeMaintain();
    abstract protected void editmodeMaintainVersionEdit();
    abstract protected void editmodeUpdateStatus();
    abstract protected void editmodeUpdateStatusVersion();
    abstract protected void editmodeMaintainVersionCopy();
        
    abstract protected void create();
    abstract protected void maintainVersionNew();
    abstract protected void maintain();
    abstract protected void maintainVersionEdit();
    abstract protected void maintainVersionCopy();
    abstract protected void updateStatus();
    abstract protected void updateStatusVersion();            
    
    @Override
    public final void doAfterCompose(Component comp) throws Exception{
        super.doAfterCompose(comp);
        if(arg.get("EditMode") != null)
            editMode = (Integer)arg.get("EditMode");
        window       = (Window)comp;
        windowParent = (Window)comp.getParent();

        doAfterComposeCustom();
        editMode(editMode);
    }       

    protected void buttonAction(){
        switch(editMode){            
            case EDITMODE_CREATE:
                create();
                break;
            case EDITMODE_MAINTAIN_VERSION_NEW:
                maintainVersionNew();
                break;
            case EDITMODE_MAINTAIN:
                maintain();
                break;    
            case EDITMODE_MAINTAIN_VERSION_EDIT:
                maintainVersionEdit();
                break;    
            case EDITMODE_UPDATESTATUS:
                updateStatus();
                break;
            case EDITMODE_UPDATESTATUS_VERSION:
                updateStatusVersion();
                break;
            case EDITMODE_COPYVERSION_NEW:
                maintainVersionCopy();
                break;
        }
    }

    protected void editMode(int eMode){
        editMode = eMode;
        switch(eMode){
            case EDITMODE_DETAILSONLY:
                editmodeDetails();
                break;
            case EDITMODE_CREATE:
                editmodeCreate();
                break;
            case EDITMODE_MAINTAIN_VERSION_NEW:
                editmodeMaintainVersionNew();
                break;
            case EDITMODE_MAINTAIN:
                editmodeMaintain();
                break;    
            case EDITMODE_MAINTAIN_VERSION_EDIT:
                editmodeMaintainVersionEdit();
                break;    
            case EDITMODE_UPDATESTATUS:
                editmodeUpdateStatus();
                break;
            case EDITMODE_UPDATESTATUS_VERSION:
                editmodeUpdateStatusVersion();
                break;
            case EDITMODE_COPYVERSION_NEW:
                editmodeMaintainVersionCopy();
                break;
        }
        initializeData();
    }

    protected void initializeData(){
        loadDatesIntoGUI();
        initializeDatabinder();
    }       
}
