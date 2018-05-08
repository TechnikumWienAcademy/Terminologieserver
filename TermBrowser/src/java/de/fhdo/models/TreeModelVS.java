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
package de.fhdo.models;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse.Return;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.Desktop;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Sven Becker
 */
public class TreeModelVS {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//    private static TreeModelVS instance;    
    private static TreeModel treeModel;
    private static Desktop desktop;
    private static ArrayList<ValueSetVersion> vsvList = new ArrayList<ValueSetVersion>();

    public static ArrayList<ValueSetVersion> getVsvList() {
        return vsvList;
    }   

    public static void reloadData(Desktop d) {
        createModel(d);
    }
    
    private static void createModel(Desktop d) {
        desktop = d;
        try {
            vsvList.clear();
            logger.debug("ValueSetTreeModel - initData()");

            ListValueSetsRequestType parameter = new ListValueSetsRequestType();

            // login
            if (SessionHelper.isCollaborationActive())
            {
              // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
              parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
              parameter.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
            }
            else if (SessionHelper.isUserLoggedIn())
            {
              parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
              parameter.getLogin().setSessionID(SessionHelper.getSessionId());
            }
            
            Return response = WebServiceHelper.listValueSets(parameter);

            logger.debug("Response-Msg: " + response.getReturnInfos().getMessage());

            if (response.getReturnInfos().getStatus() == Status.OK) {
                List<TreeNode> list = new LinkedList<TreeNode>();

                for(int i = 0; i < response.getValueSet().size(); ++i) {
                    list.add(createTreeNode(response.getValueSet().get(i)));
                }

                TreeNode tn_root = new TreeNode(null, list);
                treeModel = new TreeModel(tn_root);
            }

            logger.debug("ValueSetTreeModel - initData(): fertig");
        } catch (Exception e) {
            logger.error("Fehler in ValueSetTreeModel, initData():" + e.getMessage());
        }
    }

    private static TreeNode createTreeNode(ValueSet vs) {
        logger.debug("createTreeNode: " + vs.getName());

        TreeNode tn = new TreeNode(vs);

        // Kinder suchen
        for(ValueSetVersion vsv : vs.getValueSetVersions()){                        
            //logger.debug("Version: " + vsv.getVersionId());
            
            vsv.setValueSet(vs);
            
            // liste von CSV aufbauen
            vsvList.add(vsv);
            
            // Nur die aktuellste version anzeigen  // TODO: Als Eigenschaft in Accountdetails regeln ob alle versionen anzeigen oder nicht
						//Matthias 27.05.2015 JIRA 201 - showing previous VS version even for public users
						TreeNode childTN = new TreeNode(vsv);
						tn.getChildren().add(childTN);
//            if(SessionHelper.isUserLoggedIn()){
//                TreeNode childTN = new TreeNode(vsv);            
//                tn.getChildren().add(childTN);
//            }
//            else{
//                if(vs.getCurrentVersionId().equals(vsv.getVersionId())){                
//                    TreeNode childTN = new TreeNode(vsv);            
//                    tn.getChildren().add(childTN);
//                    break;
//                }
//            }
        }

        return tn;
    }

   public static TreeModel getTreeModel(Desktop d) {        
        if(treeModel == null || desktop == null || desktop != d)
            createModel(d);            
        
        return treeModel;
    }        
}
