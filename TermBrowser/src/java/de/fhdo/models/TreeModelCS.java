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
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse.Return;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Desktop;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;

/**
 * This class builds and offers the tree model, which is used to display the sarch results.
 * @author Robert Mützner, bachinger
 */
public class TreeModelCS {
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    final private static ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();
    private static TreeModel   treeModel = null;
    private static Desktop desktop;
    
    /**
     * Gets the CSV-list.
     * TODO: What is the CSV-list used for?
     * @return the CSV-list
     */
    public static ArrayList<CodeSystemVersion> getCsvList() {
        return csvList;
    }   

    /**
     * Calls the createModel() function to rebuild the tree model.
     * @param d the ZK-framework-desktop
     */
    public static void reloadData(Desktop d) {
        createModel(d);        
    }         
    
    /**
     * Creates the tree model, checks if the user is logged in in the beginning to
     * ensure that, depending on the login, all the data or only parts of it are shown.
     * @param d the ZK-framework-desktop
     */
    private static void createModel(Desktop d) {
        LOGGER.info("===== createModel started =====");
                       
        desktop = d;
        try {       
            csvList.clear();
            
            ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();            
            
            //Check login
            if (SessionHelper.isCollaborationActive())
            {
              //Use collabuser so that non-active concepts can be shown
              parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
              parameter.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
            }
            else if (SessionHelper.isUserLoggedIn())
            {
              parameter.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
              parameter.getLogin().setSessionID(SessionHelper.getSessionId());
            }
            
            if(parameter.getLogin() != null)
            {
                LOGGER.info("Session-ID: " + parameter.getLogin().getSessionID());
            }
            Return response = WebServiceHelper.listCodeSystems(parameter);
            
            if (response.getReturnInfos().getStatus() == Status.OK) {
                List list = new LinkedList();

                //provideOrderChoosen by AdminSettings
                Map<Integer,DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();
                
                //Domain values with underlying domain-values, code-systems and code-system-versions
		int maxOrderNumber = -1;
                for(DomainValue dv : response.getDomainValue()){			
                    if(dv.getOrderNo() != null){
                        int orderNumber = dv.getOrderNo();
                        mapDomVal.put(orderNumber, dv);
                        if (orderNumber > maxOrderNumber)
                            maxOrderNumber = orderNumber;
                    }
                    else
                        mapDomVal.put(maxOrderNumber+1, dv);
                }
                
                Collection<DomainValue> domainValues = mapDomVal.values();

                for (DomainValue entry : domainValues){
                        list.add(createTreeNode(entry));
                }			
                TreeNode tn_root = new TreeNode(null, list);
                treeModel = new TreeModel(tn_root);       
            }      
        } catch (Exception e) {
            //Return an empty list if there is an error
            treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
            LOGGER.error("There was an error initializing the tree-data: " + e.getMessage());
        }
    }

    /**
     * Create a single tree node for the model, using domain-values and code-systems.
     * @param x the object for which a node is created
     * @return the created tree node
     */
    private static TreeNode createTreeNode(Object x) {        
        TreeNode tn = new TreeNode(x);

        if(x instanceof CodeSystem)
        {    
            CodeSystem cs = (CodeSystem)x;
            LOGGER.debug("Creating code-system tree node: " + cs.getName());
            
            // Add children (code-system versions)            
            for(CodeSystemVersion csv : cs.getCodeSystemVersions()){
                csv.setCodeSystem(cs);
                LOGGER.debug("Version: " + csv.getName());
                csvList.add(csv);
                tn.add(new TreeNode(csv));
            }
        }
        else if (x instanceof DomainValue)
        {
            DomainValue dv = (DomainValue)x;
            LOGGER.debug("Creating domain-value tree node: " + dv.getAttribut1Classname());
            
            // Add Children (domain-values and code-systems)
            for(DomainValue dv2 : dv.getDomainValuesForDomainValueId2()){
                tn.add(createTreeNode(dv2));
            }
            for(CodeSystem cs : dv.getCodeSystems()){
                tn.add(createTreeNode(cs));
            }                             
        }
        else
            return null;
        
        return tn;
    }

    /**
     * Get the currently active tree model, if one exists. If not create a new one.
     * @param d the ZK-framework-desktop
     * @return the currently active tree model
     */
    public static TreeModel getTreeModel(Desktop d) {     
        if(treeModel == null || desktop == null || desktop != d)
            createModel(d);
        return treeModel;
    }   
}