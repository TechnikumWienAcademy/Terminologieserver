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
import de.fhdo.collaboration.helper.ProposalStatusChangeHelper;
import de.fhdo.collaboration.helper.ProposalStatusChangeHelperController;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
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
 * reworked july 2018 by dario bachinger
 */
public class ProposalStatusChange extends Window implements AfterCompose, EventListener
{
    private long statusToId;
    private boolean isDiscussion;
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    //TS
    private IUpdateModal updateInterface;
    private Proposal proposal;

    public ProposalStatusChange()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("ProposalStatusChange() - Konstruktor");
            logger.debug("Lade Parameter...");
        }

        proposal = (Proposal) ArgumentHelper.getWindowArgument("proposal");
        statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");
    
        if (logger.isDebugEnabled())
        {
            logger.debug("Status-ID (neu): " + statusToId);
            logger.debug("Vorschlags-ID: " + proposal.getId());
        }
    }

    public void afterCompose()
    {
        isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
        ((Row) getFellow("rowZeitraum")).setVisible(isDiscussion);
        if(!Executions.getCurrent().getDesktop().isServerPushEnabled())
            Executions.getCurrent().getDesktop().enableServerPush(true);
    }
        
    public void onOkClicked()
    {    
        Clients.showBusy(this,"");

        //pscHelper stores parameters which are later needed by the thread
        //which executes the proposal status change
        ProposalStatusChangeHelper pscHelper = new ProposalStatusChangeHelper();
        pscHelper.setDesktop(Executions.getCurrent().getDesktop());
        pscHelper.setEventListener(this);
        pscHelper.setReason(((Textbox)getFellow("tbReason")).getValue());
        pscHelper.setDateFrom(((Datebox)getFellow("dateVon")).getValue());
        pscHelper.setDateTo(((Datebox)getFellow("dateBis")).getValue());
        pscHelper.setConnectedToPub(SessionHelper.getValue("pub_connection").toString().equals("connected"));
        pscHelper.setStatusRel(ProposalStatus.getInstance().getStatusRel(proposal.getStatus(), statusToId));
        pscHelper.setIsUserAllowd(ProposalStatus.getInstance().isUserAllowed(pscHelper.getStatusRel(), SessionHelper.getCollaborationUserID()));
        pscHelper.setCollaborationUserID(SessionHelper.getCollaborationUserID());
        pscHelper.setSessionID(CollaborationSession.getInstance().getSessionID());
        pscHelper.setCollaborationSessionID(CollaborationSession.getInstance().getSessionID());
        ProposalStatusChangeHelperController.getPscHelperController().addPscHelperForSessions(pscHelper);
        
        if(pscHelper.getDateFrom() != null)
            logger.debug("Datum von: " + pscHelper.getDateFrom());
        else 
            logger.debug("Datum von: null");
   
        //Change proposal status during thread execution
        Thread pscThread = new Thread(){
            @Override
            public void run() {
                continueStatusChange(this.getName());
            }
        };
        pscThread.setName(pscHelper.getSessionID());
        pscThread.start();
    }
    
    public void continueStatusChange(String paraSessionID){
        ProposalStatusChangeHelper pscHelper = ProposalStatusChangeHelperController.getPscHelperController().getPscHelperBySessionID(paraSessionID);
        
        pscHelper.setRetVal(ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, pscHelper.getReason(), pscHelper.getDateFrom(), pscHelper.getDateTo(), false, pscHelper.getSessionID()));
        
        long statusFrom = proposal.getStatus();
    
        if ((pscHelper.getRetVal().isSuccess()) && (!pscHelper.getStatusRel().getStatusByStatusIdTo().getIsPublic()))
        {
            ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, pscHelper.getReason());
        }
        if ((pscHelper.getStatusRel().getStatusByStatusIdTo().getIsPublic())
            && (pscHelper.getRetVal().isSuccess())
            && (DBSysParam.instance().getBoolValue("isKollaboration", null, null))
            && pscHelper.isConnectedToPub()
        )
        {
            ReturnType transfer_success = new ReturnType();
            transfer_success.setSuccess(false);
            TerminologyReleaseManager releaseManager = new TerminologyReleaseManager();
            transfer_success = releaseManager.initTransfer(proposal.getProposalobjects(), pscHelper.getStatusRel());
            pscHelper.setTransVal(transfer_success);
                        
            if (transfer_success.isSuccess())
            {
                logger.info(proposal.getVocabularyName()+ ": Freigabe erfolgreich.");
                ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, pscHelper.getReason());
                Executions.schedule(pscHelper.getDesktop(), pscHelper.getEventListener(), new Event("MSG_Success",null,""));
            }
            else if (!transfer_success.isSuccess())
            {
                logger.info(proposal.getVocabularyName()+ ": Freigabe fehlgeschlagen." +  transfer_success.getMessage());
                Executions.schedule(pscHelper.getDesktop(), pscHelper.getEventListener(), new Event("MSG_FailureInfoSPLIT" + pscHelper.getSessionID(),null,""));
                proposal.setStatus((int) statusToId);
                //setting status back because transfer to public was not successful
                ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusFrom, "Freigabe konnte auf Grund eines Fehlers nicht durchgeführt werden. "+transfer_success.getMessage() , pscHelper.getDateFrom(), pscHelper.getDateTo(), false, pscHelper.getSessionID());
                if(retResetStatus.isSuccess())
                {
                    logger.info(proposal.getVocabularyName() + ": Status wurde nicht geändert");
                    Executions.schedule(pscHelper.getDesktop(), pscHelper.getEventListener(), new Event("MSG_StatusUnchanged",null,""));
                    //change ReturnType to prevent Messagebox in ProposalView.upate()
                    pscHelper.setRetVal(new ReturnType());
                    pscHelper.getRetVal().setSuccess(true);
                    pscHelper.getRetVal().setMessage("InlinePropUpdate");
                }
            }
        }
        // Fenster schließen
        Executions.schedule(pscHelper.getDesktop(), pscHelper.getEventListener(), new Event("finishSPLIT" + pscHelper.getSessionID(),null,""));
    
    }
  
    /**
     * @param updateInterface the updateInterface to set
     */
    public void setUpdateInterface(IUpdateModal updateInterface)
    {
      this.updateInterface = updateInterface;
    }

    @Override
    public void onEvent(Event event) throws Exception {    
        
        if(event.getName().contains("finish")){
            this.setVisible(false);
            this.detach();
            
            String[] parts = event.getName().split("SPLIT");
            ProposalStatusChangeHelper pscHelper = ProposalStatusChangeHelperController.getPscHelperController().getPscHelperBySessionID(parts[1]);
            
            //Update proposal window
            if (updateInterface != null)
            {
                if(pscHelper.getStatusRel().getStatusByStatusIdTo().getIsPublic())
                    updateInterface.update(pscHelper.getTransVal(), false);
                else
                    updateInterface.update(pscHelper.getRetVal(), false);
            }
            //Cleanup
            Clients.clearBusy(this);
            if(pscHelper.getDesktop().isServerPushEnabled())
                pscHelper.getDesktop().enableServerPush(false);
            ProposalStatusChangeHelperController.getPscHelperController().removePscHelperForSessions(pscHelper);
            return;
        }
        
        if(event.getName().equals("MSG_Success")){
            Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
            return;
        }
        
        if(event.getName().contains("MSG_FailureInfo")){
            String[] parts = event.getName().split("SPLIT");
            ProposalStatusChangeHelper pscHelper = ProposalStatusChangeHelperController.getPscHelperController().getPscHelperBySessionID(parts[1]);
            
            Messagebox.show(pscHelper.getTransVal().getMessage(), "Freigabe", Messagebox.OK, Messagebox.ERROR);
            return;
        }
        
        if(event.getName().equals("MSG_StatusUnchanged")){
            Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
            return;
        }
    }
}
