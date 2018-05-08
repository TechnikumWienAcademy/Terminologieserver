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
import java.util.Set;
import org.zkoss.zk.ui.Desktop;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author Robert Mützner
 */
public class TreeModelCS {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static Desktop desktop;
//    private static TreeModelCS instance  = null;  
    private static TreeModel   treeModel = null;
    private static ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();

    public static ArrayList<CodeSystemVersion> getCsvList() {
        return csvList;
    }   

    public static void reloadData(Desktop d) {
        createModel(d);        
    }         
    
    private static void createModel(Desktop d) {
        logger.info("--- TreeModelCS, initData -------------------");
                       
        desktop = d;
        try {       
            csvList.clear();
            
            ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();            
            
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
            
            if(parameter.getLogin() != null)
            {
                logger.info("SESSION ID :" + parameter.getLogin().getSessionID());
            }
            Return response = WebServiceHelper.listCodeSystems(parameter);
            
            if (response.getReturnInfos().getStatus() == Status.OK) {
                List list = new LinkedList();

                //provideOrderChoosen by AdminSettings
                Map<Integer,DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();
                
                // Domain Values mit untergeordneten DV,CS und CSVs
								int maxOrderNumber = -1;
                for(DomainValue dv : response.getDomainValue()){
										
                    if(dv.getOrderNo() != null){
                        int orderNumber = dv.getOrderNo();
                        mapDomVal.put(orderNumber, dv);
                        if (orderNumber > maxOrderNumber){
                                maxOrderNumber = orderNumber;
                        }
                    }else{
                        mapDomVal.put(maxOrderNumber+1, dv);
                    }
                }
                
                Collection<DomainValue> domainValues = mapDomVal.values();

                for (DomainValue entrie : domainValues){
                        list.add(createTreeNode(entrie));
                }
						
                TreeNode tn_root = new TreeNode(null, list);
                treeModel = new TreeModel(tn_root);       
            }           

        } catch (Exception e) {
            // Bei Fehler, leere Liste zurück geben
            treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
            logger.error("Fehler in TreeModelCS, initData():" + e.getMessage());
            
            e.printStackTrace();
        }
    }

    private static TreeNode createTreeNode(Object x) {        
        TreeNode tn = new TreeNode(x);

        if(x instanceof CodeSystem)
        {    
            CodeSystem cs = (CodeSystem)x;
            
            logger.debug("createTreeNode: " + cs.getName());
            
            // Kinder (CodeSystemVersions) suchen und dem CodeSystem hinzufügen            
            for(CodeSystemVersion csv : cs.getCodeSystemVersions()){
                //logger.debug("Version: " + csv.getName());
                
                csv.setCodeSystem(cs);
                
                // liste von CSV aufbauen
                csvList.add(csv);

                // CSV in das CS einh?ngen
                tn.add(new TreeNode(csv));
            }                                 
        }
        else if (x instanceof DomainValue)
        {
            DomainValue dv = (DomainValue)x;
            
            // Gibts DomainValues im DV? Wenn ja, einf?gen  
            for(DomainValue dv2 : dv.getDomainValuesForDomainValueId2()){
                tn.add(createTreeNode(dv2));
            }
            
            // Gibts CSs im DV? Wenn ja, einf?gen
            for(CodeSystem cs : dv.getCodeSystems()){
                tn.add(createTreeNode(cs));
            }                             
        }
        else
            return null;    // Kein DV oder CS
        
        return tn;
    }

    public static TreeModel getTreeModel(Desktop d) {        
        if(treeModel == null || desktop == null || desktop != d)
            createModel(d);            
        
				createModel(d);
        return treeModel;
    }   
}