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
package de.fhdo.collaboration.proposal;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 * edited: bachinger (3.2.17)
 */
public class ProposalStatusChange extends Window implements AfterCompose, EventListener{
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    final private Proposal proposal;
    final private long statusToId;
    private IUpdateModal updateInterface;
    private boolean isDiscussion;
    
    //3.2.17 Thread variables
    private Thread pscThread;
    private Desktop threadDesktop;
    private EventListener threadListener;
    private ReturnType threadReturn;
    private TerminologyReleaseManager threadReleaseManager;
    private boolean threadRunning = false;
    
    /**
     * Fetches the proposal and the status to which it should be changed to
     * from the window.
     */
    public ProposalStatusChange(){
        proposal = (Proposal) ArgumentHelper.getWindowArgument("proposal");
        statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");
    
        LOGGER.debug("status_to_id: " + statusToId);
        LOGGER.debug("proposal-ID: " + proposal.getId());
    }

    /**
     * Sets the isDiscussion attribute on true, if the target status is a
     * discussion. If it is true, the rowZeitraum will be set visible.
     */
    public void afterCompose(){
        isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
        ((Row) getFellow("rowZeitraum")).setVisible(isDiscussion);
    }
    
    /**
     * 
     */
    public void onOkClicked(){
        //Locks the GUI and enables the thread to call Excetions.schedule() method
        Clients.showBusy(this, "");
        this.getDesktop().enableServerPush(true);
        
        threadDesktop = this.getDesktop();
        threadListener = this;
        threadReleaseManager = new TerminologyReleaseManager(); //ANKERNEW
        
        long statusFrom = proposal.getStatus();
        Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);
        final boolean isUserAllowed = ProposalStatus.getInstance().isUserAllowed(rel, SessionHelper.getCollaborationUserID());
        final long collabUserId = SessionHelper.getCollaborationUserID();
        final String collabSessionID = CollaborationSession.getInstance().getSessionID();
        final boolean isPubConnected = (SessionHelper.getSessionObjectByName("pub_connection").toString().equals("connected"));
        
        pscThread = new Thread(){
            @Override
            public void run(){
                // Statusänderung durchführen
                String reason = ((Textbox) getFellow("tbReason")).getValue();
                Date dateFrom = ((Datebox)getFellow("dateVon")).getValue();
                Date dateTo = ((Datebox)getFellow("dateBis")).getValue();
    
                if(dateFrom != null)
                    LOGGER.debug("Date from: " + dateFrom);
                else 
                    LOGGER.debug("Date from: null");
            
            
                //3.2.17 added collabUserId and collabSessionID parameter
                threadReturn = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, dateFrom, dateTo, false, collabUserId, collabSessionID);
    
                long statusFrom = proposal.getStatus();
                Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);
                
                if ((threadReturn.isSuccess()) && (!rel.getStatusByStatusIdTo().getIsPublic()))
                {
                    ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
                }
                if ((rel.getStatusByStatusIdTo().getIsPublic())
                    && (threadReturn.isSuccess())
                    && (DBSysParam.instance().getBoolValue("isKollaboration", null, null))
                    //3.2.17 commented out since this is checked before the thread is started
                        //&& (SessionHelper.getValue("pub_connection").toString().equals("connected"))
                        && isPubConnected)
                {
                    ReturnType transfer_success = new ReturnType();
                    transfer_success.setSuccess(false);
                    //3.2.17 commented out since this is invoked before the thread is started
                    //TerminologyReleaseManager releaseManager = new TerminologyReleaseManager();
                    transfer_success = threadReleaseManager.initTransfer(proposal.getProposalobjects(), rel); //ANKER
                
                if (transfer_success.isSuccess())
                {
                    LOGGER.info(proposal.getVocabularyName()+ ": Proposal status change successful");
                    ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
                    //3.2.17 commented out since this is done in the Executions.schedule
                    //Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
                    Executions.schedule(threadDesktop, threadListener, new Event("SUCCESS"));
                }
                else if (!transfer_success.isSuccess())
                {
                    LOGGER.info(proposal.getVocabularyName()+ ": Proposal status change failed " +  transfer_success.getMessage());
                    //3.2.17 commented out since this is done in the Executions.schedule
                    //Messagebox.show(transfer_success.getMessage(), "Freigabe", Messagebox.OK, Messagebox.ERROR);
                    Executions.schedule(threadDesktop, threadListener, new Event("FAILURESPLIT" + transfer_success.getMessage()));
                    proposal.setStatus((int) statusToId);
                    //setting status back because transfer to public was not successful
                    //3.2.17 added collabUserId and collabSessionID parameter
                    ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusFrom, "Freigabe konnte auf Grund eines Fehlers nicht durchgeführt werden. "+transfer_success.getMessage() , dateFrom, dateTo, false, collabUserId, collabSessionID);
                    if(retResetStatus.isSuccess())
                    {
                        LOGGER.info(proposal.getVocabularyName() + ": : Proposal status has not been changed");
                        //3.2.17 commented out since this is done in the Executions.schedule
                        //Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
                        Executions.schedule(threadDesktop, threadListener, new Event("RESET"));
                        
                        //change ReturnType to prevent Messagebox in ProposalView.upate()
                        threadReturn = new ReturnType();
                        threadReturn.setSuccess(true);
                        threadReturn.setMessage("InlinePropUpdate");
                    }
                }
            }

            Executions.schedule(threadDesktop, threadListener, new Event("finish"));
        }
    };
    if(isUserAllowed){
        threadRunning = true;
        pscThread.start();
    }
    else
        Executions.schedule(threadDesktop, threadListener, new Event("finish"));
}

    /**
     * @param updateInterface the updateInterface to set
     */
    public void setUpdateInterface(IUpdateModal updateInterface){
        this.updateInterface = updateInterface;
    }
  
    public void onEvent(Event event) throws Exception{
        if(event.getName().contains("finish")){
            threadRunning = false;
            // Fenster schließen
            this.setVisible(false);
            this.detach();

            // Vorschlag-Fenster aktualisieren
            if (updateInterface != null){
               updateInterface.update(threadReturn, false);
            }
            Clients.clearBusy(this);
        }
        else if(event.getName().contains("SUCCESS")){
            threadRunning = false;
            Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
        else if(event.getName().contains("FAILURE")){
            threadRunning = false;
            String[] message = event.getName().split("SPLIT");
            Messagebox.show(message[message.length-1], "Freigabe", Messagebox.OK, Messagebox.ERROR);
        }
        else if(event.getName().contains("RESET")){
            threadRunning = false;
            Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
    }
}
