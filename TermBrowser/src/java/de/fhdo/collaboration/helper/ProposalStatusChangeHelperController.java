/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author dabac
 */
public class ProposalStatusChangeHelperController {
    static private List<ProposalStatusChangeHelper> pscHelpersForSessions;
    static private ProposalStatusChangeHelperController pscHelperController;
    
    static public ProposalStatusChangeHelperController getPscHelperController(){
        if(pscHelperController==null){
            pscHelperController = new ProposalStatusChangeHelperController();
            pscHelpersForSessions = Collections.synchronizedList(new ArrayList<ProposalStatusChangeHelper>());
        }
        return pscHelperController;
    }
    
    public ProposalStatusChangeHelper getPscHelperBySessionID(String sessionID){
        for(ProposalStatusChangeHelper pscHelper : pscHelpersForSessions){
            if(pscHelper.getSessionID().equals(sessionID))
                return pscHelper;
        }
        return null;
    }
    
    public void addPscHelperForSessions(ProposalStatusChangeHelper helper){
        pscHelpersForSessions.add(helper);
    }
    
    public void removePscHelperForSessions(ProposalStatusChangeHelper helper){
        pscHelpersForSessions.remove(helper);
    }
}
