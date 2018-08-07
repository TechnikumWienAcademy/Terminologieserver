/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.workflow.ReturnType;
import java.util.Date;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.EventListener;

/**
 *
 * @author dabac
 * This class is instantiated during the status change of a proposal.
 * It holds parameters for the thread, which executes the status changes but has no
 * access to the ProposalStatusChange object. These parameters are stored in this class because
 * the ProposalStatusChange class would need these as class variables. You can remove this class
 * if you include the parameters in the ProposalStatusChange class.
 */
public class ProposalStatusChangeHelper {
    private String reason;
    private String sessionID;
    private String collaborationSessionID;
    private long collaborationUserID;
    private Date dateFrom;
    private Date dateTo;
    private boolean connectedToPub;
    private boolean isUserAllowd;
    private boolean isUserLoggedIn;
    //TS
    private ReturnType retVal;
    private ReturnType transVal;
    private Statusrel statusRel;
    //ZK
    private Desktop desktop;
    private EventListener eventListener;

    public String getCollaborationSessionID() {
        return collaborationSessionID;
    }

    public void setCollaborationSessionID(String collaborationSessionID) {
        this.collaborationSessionID = collaborationSessionID;
    }
    
    public boolean isIsUserLoggedIn() {
        return isUserLoggedIn;
    }

    public void setIsUserLoggedIn(boolean isUserLoggedIn) {
        this.isUserLoggedIn = isUserLoggedIn;
    }
    
    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    
    public long getCollaborationUserID() {
        return collaborationUserID;
    }

    public void setCollaborationUserID(long collaborationUserID) {
        this.collaborationUserID = collaborationUserID;
    }
    
    public boolean isIsUserAllowd() {
        return isUserAllowd;
    }

    public void setIsUserAllowd(boolean isUserAllowd) {
        this.isUserAllowd = isUserAllowd;
    }
    
    public Statusrel getStatusRel() {
        return statusRel;
    }

    public void setStatusRel(Statusrel statusRel) {
        this.statusRel = statusRel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public boolean isConnectedToPub() {
        return connectedToPub;
    }

    public void setConnectedToPub(boolean connectedToPub) {
        this.connectedToPub = connectedToPub;
    }

    public ReturnType getRetVal() {
        return retVal;
    }

    public void setRetVal(ReturnType retVal) {
        this.retVal = retVal;
    }

    public Desktop getDesktop() {
        return desktop;
    }

    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    public ReturnType getTransVal() {
        return transVal;
    }

    public void setTransVal(ReturnType transVal) {
        this.transVal = transVal;
    }
}
