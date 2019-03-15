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
package de.fhdo.collaboration.publication;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.cfg.Environment;
import org.zkoss.zk.ui.ext.AfterCompose;
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
public class ProposalStatusChangeBatch extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateInterface;
  private List<Proposal> proposals;
  private long statusToId;
  boolean isDiscussion;
  //3.2.17
  TerminologyReleaseManager releaseManager;

  public ProposalStatusChangeBatch()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("ProposalStatusChange() - Konstruktor");
      logger.debug("lade Parameter...");
    }

    proposals = (List<Proposal>) ArgumentHelper.getWindowArgument("proposal");
    statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");

    if (logger.isDebugEnabled())
    {
      logger.debug("status_to_id: " + statusToId);
      //logger.debug("proposal-ID: " + proposal.getId());
    }
  }

  public void afterCompose()
  {
    isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
    ((Row) getFellow("rowZeitraum")).setVisible(isDiscussion);

  }

  public void onOkClicked()
  {
      //3.2.17
      releaseManager = new TerminologyReleaseManager();
      
    // Statusänderung durchführen
    String reason = ((Textbox) getFellow("tbReason")).getValue();
    Date dateFrom = ((Datebox) getFellow("dateVon")).getValue();
    Date dateTo = ((Datebox) getFellow("dateBis")).getValue();

    if (dateFrom != null)
    {
      logger.debug("Datum von: " + dateFrom);
    } else
    {
      logger.debug("Datum von: null");
    }

    //zeurst CS, VS, dann vocabulary
    //TODO in JAVA 7 noch nicht möglich
//    proposals.sort(new Comparator<Proposal>() {
//
//      public int compare(Proposal o1, Proposal o2) {
//
//        return o2.getContentType().compareTo(o1.getContentType());
//      }
//
//    });
    
    List<Proposal> codeSystemProposals = new ArrayList<Proposal>();
    List<Proposal> valueSetProposals = new ArrayList<Proposal>();
    List<Proposal> otherProposals = new ArrayList<Proposal>();
    Session hb_session = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
    hb_session.beginTransaction();
    Set<Proposalobject> proposalobjects = new HashSet<Proposalobject>();
    
    try
    {
    
        for(Proposal proposal: proposals)
        {

            Proposal proposal_db = (Proposal) hb_session.get(Proposal.class, proposal.getId());
            proposalobjects = proposal_db.getProposalobjects();
            proposal.setProposalobjects(proposalobjects);

            if(proposal.getContentType().equals("valueset"))
            {
              valueSetProposals.add(proposal);
            }
            else if(proposal.getContentType().equals("vocabulary"))
            {
              codeSystemProposals.add(proposal);
            }
            else
            {
              otherProposals.add(proposal);
            }
        }

        List<Proposal> sortedProposals = new ArrayList<Proposal>();
        sortedProposals.addAll(codeSystemProposals);
        sortedProposals.addAll(valueSetProposals);
        sortedProposals.addAll(otherProposals);

        ReturnType ret = new ReturnType();
        ret.setSuccess(true);
        ReturnType transfer_success = new ReturnType();
        transfer_success.setSuccess(false);
        boolean success = true;
        String message = "";

        for (Proposal proposal : sortedProposals)
        {
          if(success)
          {
            //3.2.17 added collabuserID to parameter and added line which gets the collabuserID same with collabsession
              long collabUserID = SessionHelper.getCollaborationUserID();
              String collabSessionID = CollaborationSession.getInstance().getSessionID();
            ret = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, dateFrom, dateTo, true, collabUserID, collabSessionID);

            long statusFrom = proposal.getStatus();
            Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);

            if ((rel.getStatusByStatusIdTo().getIsPublic())
                    && (ret.isSuccess())
                    && (DBSysParam.instance().getBoolValue("isKollaboration", null, null))
                    && (SessionHelper.getValue("pub_connection").toString().equals("connected")))
            {
                //3.2.17 is invoked at the start now
                //TerminologyReleaseManager releaseManager = new TerminologyReleaseManager();
                transfer_success = releaseManager.initTransfer(proposal.getProposalobjects(), rel);

                if (transfer_success.isSuccess())
                {
                    ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
                    message += proposal.getVocabularyName() + ": Freigabe erfolgreich\n";
                    logger.info(message);
                }
                else if (!transfer_success.isSuccess())
                {

                    message += proposal.getVocabularyName() + ": Freigabe fehlgeschlagen\n";
                    message += "Fehler: " + transfer_success.getMessage() + "\n";
                    //setting status back because transfer to public was not successful
                    proposal.setStatus((int) statusToId);
                    //3.2.17 added collabuserID parameter and collabsession
                    ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusFrom, reason, dateFrom, dateTo, false, collabUserID, collabSessionID);
                    if(retResetStatus.isSuccess())
                    {
                        message += proposal.getVocabularyName() + ": Status wurde nicht geändert.";
                    }
                    logger.info(message);
                }
            }
          }

          success = (ret.isSuccess() && transfer_success.isSuccess());
          System.out.println(ret.getMessage());
          logger.info("Batch-Freigabe: " + ret.getMessage());
        }
        
        if(success)
        {
          Messagebox.show(message, "Freigabe", Messagebox.OK, Messagebox.INFORMATION);
        }
        else
        {
          Messagebox.show(message, "Freigabe", Messagebox.OK, Messagebox.ERROR);
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
    catch(Exception ex)
    {
        hb_session.getTransaction().rollback();
    }
    finally
    {
        hb_session.getTransaction().commit();
        hb_session.close();
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
