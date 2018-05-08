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

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.io.IOException;
import java.util.Date;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ProposalStatusChange extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateInterface;
  private Proposal proposal;
  private long statusToId;
  boolean isDiscussion;

  public ProposalStatusChange()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("ProposalStatusChange() - Konstruktor");
      logger.debug("lade Parameter...");
    }

    proposal = (Proposal) ArgumentHelper.getWindowArgument("proposal");
    statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");
    
    if (logger.isDebugEnabled())
    {
      logger.debug("status_to_id: " + statusToId);
      logger.debug("proposal-ID: " + proposal.getId());
    }


  }

    public void afterCompose()
    {
        isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
        ((Row) getFellow("rowZeitraum")).setVisible(isDiscussion);
    }
    
  public void onOkClicked()
  {
    // Statusänderung durchführen
    String reason = ((Textbox) getFellow("tbReason")).getValue();
    Date dateFrom = ((Datebox)getFellow("dateVon")).getValue();
    Date dateTo = ((Datebox)getFellow("dateBis")).getValue();
    
    if(dateFrom != null)
      logger.debug("Datum von: " + dateFrom);
    else logger.debug("Datum von: null");
    
    ReturnType ret = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, dateFrom, dateTo, false);
    
    long statusFrom = proposal.getStatus();
    Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);
    
    if ((ret.isSuccess()) && (!rel.getStatusByStatusIdTo().getIsPublic()))
    {
        ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
    }
    
    if ((rel.getStatusByStatusIdTo().getIsPublic())
            && (ret.isSuccess())
            && (DBSysParam.instance().getBoolValue("isKollaboration", null, null))
            && (SessionHelper.getValue("pub_connection").toString().equals("connected")))
    {
        ReturnType transfer_success = new ReturnType();
        transfer_success.setSuccess(false);
        TerminologyReleaseManager releaseManager = new TerminologyReleaseManager();
        transfer_success = releaseManager.initTransfer(proposal.getProposalobjects(), rel);
        
        if (transfer_success.isSuccess())
        {
            logger.info(proposal.getVocabularyName()+ ": Freigabe erfolgreich.");
            ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
            Messagebox.show("Freigabe erfolgreich", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
        else if (!transfer_success.isSuccess())
        {
            logger.info(proposal.getVocabularyName()+ ": Freigabe fehlgeschlagen." +  transfer_success.getMessage());
            Messagebox.show(transfer_success.getMessage(), "Freigabe", Messagebox.OK, Messagebox.ERROR);
            proposal.setStatus((int) statusToId);
            //setting status back because transfer to public was not successful
            ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusFrom, "Freigabe konnte auf Grund eines Fehlers nicht durchgeführt werden. "+transfer_success.getMessage() , dateFrom, dateTo, false);
            if(retResetStatus.isSuccess())
            {
                logger.info(proposal.getVocabularyName() + ": Status wurde nicht geändert");
                Messagebox.show("Status wurde nicht geändert", "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
                
                //change ReturnType to prevent Messagebox in ProposalView.upate()
                ret = new ReturnType();
                ret.setSuccess(true);
                ret.setMessage("InlinePropUpdate");
            }
        }
    }

    // Fenster schließen
    this.setVisible(false);
    this.detach();

    // Vorschlag-Fenster aktualisieren
    if (updateInterface != null)
    {
        updateInterface.update(ret, false);
    }

  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }
}
