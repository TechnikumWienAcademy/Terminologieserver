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
public class ProposalStatusChange extends Window implements AfterCompose, EventListener
{
    final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private IUpdateModal updateInterface;
    final private Proposal proposal;
    final private long statusToId;

    /**
     * TODO
     */
    public Thread pscThread;
    boolean isDiscussion;
  
  //3.2.17 these fields are needed since the execution of the status change is put into a thread
  private Desktop desk;
  private EventListener listener;
  private ReturnType ret;
  private TerminologyReleaseManager releaseManager;
  //3.2.18
  boolean running;

  public ProposalStatusChange()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("ProposalStatusChange() - Constructor");
      logger.debug("Loading parameters");
    }

    proposal = (Proposal) ArgumentHelper.getWindowArgument("proposal");
    statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");
    
    if (logger.isDebugEnabled())
    {
      logger.debug("status_to_id: " + statusToId);
      logger.debug("proposal-ID: " + proposal.getId());
    }

    running = false;
  }

    public void afterCompose()
    {
        isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
        ((Row) getFellow("rowZeitraum")).setVisible(isDiscussion);
    }
    
  public void onOkClicked()
  {
    //3.2.17 blocks the GUI for the client
    Clients.showBusy(this, "");
    //3.2.17 enables the thread to call the Executions.schedule() method
    this.getDesktop().enableServerPush(true);
    //3.2.17 all these variables are needed later by the thread, which cannot access them if they are not set here
    desk = this.getDesktop();
    listener = this;
    releaseManager = new TerminologyReleaseManager();
    long statusFrom = proposal.getStatus();
    Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);
    final boolean isUserAllowed = ProposalStatus.getInstance().isUserAllowed(rel, SessionHelper.getCollaborationUserID());
    final long collabUserId = SessionHelper.getCollaborationUserID();
    final String collabSessionID = CollaborationSession.getInstance().getSessionID();
    final boolean isPubConnected = (SessionHelper.getValue("pub_connection").toString().equals("connected"));
        
    pscThread = new Thread(){
        @Override
        public void run(){
            // Statusänderung durchführen
            String reason = ((Textbox) getFellow("tbReason")).getValue();
            Date dateFrom = ((Datebox)getFellow("dateVon")).getValue();
            Date dateTo = ((Datebox)getFellow("dateBis")).getValue();
    
            if(dateFrom != null)
                logger.debug("Date from: " + dateFrom);
            else 
                logger.debug("Date from: null");
            
            
            //3.2.17 added collabUserId and collabSessionID parameter
            ret = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, dateFrom, dateTo, false, collabUserId, collabSessionID);
    
            long statusFrom = proposal.getStatus();
            Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);
                
            if ((ret.isSuccess()) && (!rel.getStatusByStatusIdTo().getIsPublic()))
            {
                ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
            }
            if ((rel.getStatusByStatusIdTo().getIsPublic())
                && (ret.isSuccess())
                && (DBSysParam.instance().getBoolValue("isKollaboration", null, null))
                //3.2.17 commented out since this is checked before the thread is started
                    //&& (SessionHelper.getValue("pub_connection").toString().equals("connected"))
                    && isPubConnected)
            {
                ReturnType transfer_success = new ReturnType();
                transfer_success.setSuccess(false);
                //3.2.17 commented out since this is invoked before the thread is started
                //TerminologyReleaseManager releaseManager = new TerminologyReleaseManager();
                transfer_success = releaseManager.initTransfer(proposal.getProposalobjects(), rel);
                
                if (transfer_success.isSuccess())
                {
                    logger.info(proposal.getVocabularyName()+ ": Proposal status change successful");
                    ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
                    //3.2.17 commented out since this is done in the Executions.schedule
                    //Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
                    Executions.schedule(desk, listener, new Event("SUCCESS"));
                }
                else if (!transfer_success.isSuccess())
                {
                    logger.info(proposal.getVocabularyName()+ ": Proposal status change failed " +  transfer_success.getMessage());
                    //3.2.17 commented out since this is done in the Executions.schedule
                    //Messagebox.show(transfer_success.getMessage(), "Freigabe", Messagebox.OK, Messagebox.ERROR);
                    Executions.schedule(desk, listener, new Event("FAILURESPLIT" + transfer_success.getMessage()));
                    proposal.setStatus((int) statusToId);
                    //setting status back because transfer to public was not successful
                    //3.2.17 added collabUserId and collabSessionID parameter
                    ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusFrom, "Freigabe konnte auf Grund eines Fehlers nicht durchgeführt werden. "+transfer_success.getMessage() , dateFrom, dateTo, false, collabUserId, collabSessionID);
                    if(retResetStatus.isSuccess())
                    {
                        logger.info(proposal.getVocabularyName() + ": : Proposal status has not been changed");
                        //3.2.17 commented out since this is done in the Executions.schedule
                        //Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
                        Executions.schedule(desk, listener, new Event("RESET"));
                        
                        //change ReturnType to prevent Messagebox in ProposalView.upate()
                        ret = new ReturnType();
                        ret.setSuccess(true);
                        ret.setMessage("InlinePropUpdate");
                    }
                }
            }

            Executions.schedule(desk, listener, new Event("finish"));
        }
    };
    if(isUserAllowed){
        running = true;
        pscThread.start();
    }
    else
        Executions.schedule(desk, listener, new Event("finish"));
}

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }
  
   public void onEvent(Event event) throws Exception
    {
        if(event.getName().contains("finish")){
            running = false;
            // Fenster schließen
            this.setVisible(false);
            this.detach();

            // Vorschlag-Fenster aktualisieren
            if (updateInterface != null)
            {
               updateInterface.update(ret, false);
            }
            Clients.clearBusy(this);
        }
        else if(event.getName().contains("SUCCESS")){
            running = false;
            Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
        else if(event.getName().contains("FAILURE")){
            running = false;
            String[] message = event.getName().split("SPLIT");
            Messagebox.show(message[message.length-1], "Freigabe", Messagebox.OK, Messagebox.ERROR);
        }
        else if(event.getName().contains("RESET")){
            running = false;
            Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
    }
   
}
