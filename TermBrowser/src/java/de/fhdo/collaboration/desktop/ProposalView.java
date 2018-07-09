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
package de.fhdo.collaboration.desktop;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.desktop.proposal.ProposalViewDetails;
import de.fhdo.collaboration.desktop.proposal.ProposalViewDiscussion;
import de.fhdo.collaboration.desktop.proposal.ProposalViewLinks;
import de.fhdo.collaboration.desktop.proposal.ProposalViewVote;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.collaboration.proposal.ProposalStatusChange;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert M�tzner
 */
public class ProposalView extends Window implements AfterCompose, IUpdateModal
{
private int test = 0;
    
public int testTimer(boolean increment){
    if(increment)
        test++;
    return test;
}
    
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateInterface;
  private Proposal proposal;
  private int letzterStatusButtons = -1;
  //private ProposalHelper proposalHelper;
  private ProposalViewDetails proposalViewDetails;
  private ProposalViewLinks proposalViewLinks;
  private ProposalViewVote proposalViewVote;
  private ProposalViewDiscussion proposalViewDiscussion;

  public ProposalView()
  {
    long proposalId = ArgumentHelper.getWindowArgumentLong("proposal_id");

    if (logger.isDebugEnabled())
      logger.debug("ProposalView(), proposalId: " + proposalId);

    loadProposal(proposalId);

    if (proposal == null)
    {
      Messagebox.show("Vorschlag konnte nicht gelesen werden!");
      // TODO Fehlermeldung ausgeben und zur�ck gehen
      this.setVisible(false);
      this.detach();
    }

  }

  private void loadProposal(long proposalId)
  {
    if (proposalId > 0)
    {
      //proposalHelper = new ProposalHelper(proposalId);
      //proposal = proposalHelper.getProposal();

      //if(proposal != null && proposal.getDescription() != null && proposal.getDescription().length() > 0)
      //  this.setTitle(this.getTitle() + ": " + proposal.getDescription());

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      
      try
      {
        String hql = "from Proposal p "
                + " join fetch p.collaborationuser cu"
                + " left join fetch cu.organisation o"
                + " left join fetch p.proposalobjects po"
                + " where p.id=" + proposalId;

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        org.hibernate.Query q = hb_session.createQuery(hql);
        List liste = q.list();

        if (liste.size() > 0)
        {
          proposal = (Proposal) liste.get(0);

          if (logger.isDebugEnabled())
            logger.debug("Vorschlag gelesen: " + proposal.getDescription());

          //this.setTitle(this.getTitle() + ": " + proposal.getDescription());
        }

      }
      catch (Exception e)
      {
            //hb_session.getTransaction().rollback();
            LoggingOutput.outputException(e, this);
      }
      finally
      {
        // Session schlie�en
        hb_session.close();
      }
    }
  }

  public void afterCompose()
  {
    initData();
  }

  private void initData()
  {
    logger.debug("[ProposalView.java] initData() mit Proposal: " + proposal.getId());

    // Vorschlag hinzuf�gen (1. Tab), andere Tabs dynamisch hinzuf�gen
    Include incVorschlag = (Include) getFellow("incVorschlag");
    incVorschlag.setSrc("proposal/proposalViewDetails.zul");
    proposalViewDetails = (ProposalViewDetails) incVorschlag.getFellow("winProposalViewDetails");
    proposalViewDetails.setProposalView(this);



    showCurrentStatus();
  }

  public void onTabChanged()
  {
    Tabbox tabbox = (Tabbox) getFellow("tb");
    if (tabbox.getSelectedIndex() == 1)
    {
      // Links
      Include inc = (Include) getFellow("incLinks");
      //if (inc.getSrc() == null || inc.getSrc().length() == 0)
      {
        // Tab erstellen
        inc.setSrc(null);
        inc.setSrc("proposal/proposalViewLinks.zul");
        proposalViewLinks = (ProposalViewLinks) inc.getFellow("winProposalViewLinks");
        proposalViewLinks.setProposalView(this);
      }
    }
    else if (tabbox.getSelectedIndex() == 2)
    {
      // Diskussion
      Include inc = (Include) getFellow("incDiskussion");
      //if (inc.getSrc() == null || inc.getSrc().length() == 0)
      {
        // Tab erstellen
        inc.setSrc(null);
        inc.setSrc("proposal/proposalViewDiscussion.zul");
        proposalViewDiscussion = (ProposalViewDiscussion) inc.getFellow("winProposalViewDiscussion");
        proposalViewDiscussion.setProposalView(this);
      }

    }
    else if (tabbox.getSelectedIndex() == 3)
    {
      // Abstimmung
      Include inc = (Include) getFellow("incAbstimmung");
      //if (inc.getSrc() == null || inc.getSrc().length() == 0)
      {
        // Tab erstellen
        inc.setSrc(null);
        inc.setSrc("proposal/proposalViewVote.zul");
        proposalViewVote = (ProposalViewVote) inc.getFellow("winProposalViewVote");
        proposalViewVote.setProposalView(this);
      }
    }
  }

  private void showCurrentStatus()
  {
    // Status anzeigen (String)
    String sStatus = "-";
    if (proposal != null)
    {
      sStatus = ProposalStatus.getInstance().getStatusStr(proposal.getStatus());
    }

    ((Label) getFellow("labelStatus")).setValue(sStatus);

    // m�gliche Buttons zur Status�nderung anzeigen
    if (proposal != null)
    {
      if (proposal.getStatus() != null && proposal.getStatus().intValue() != letzterStatusButtons)
      {
        if (logger.isDebugEnabled())
          logger.debug("F�ge Status-Buttons hinzu...");

        Hbox box = (Hbox) getFellow("boxStatusButtons");
        box.getChildren().clear();

        Set<Statusrel> statusChilds = ProposalStatus.getInstance().getStatusChilds(proposal.getStatus());

        if (logger.isDebugEnabled())
          logger.debug("Anzahl childs: " + statusChilds.size());

        for (final Statusrel child : statusChilds)
        {
          // Buttons nicht anzeigen oder deaktivieren bei fehlenden Rechten
          boolean allowed = ProposalStatus.getInstance().isUserAllowed(child, SessionHelper.getCollaborationUserID());

          Button button = new Button(child.getAction().getAction());
          button.setAutodisable("true");
          if (allowed &&
              AssignTermHelper.isUserAllowed(proposal.getVocabularyIdTwo(),proposal.getVocabularyNameTwo()))
          {
              
              //Matthias 16.4.2015
              //Tooltipp adaption f�r L�schen
              if (child.getAction().getAction().equals("l�schen")){
                  button.setTooltiptext("L�schen des Eintrags");
              } 
              else {
                  button.setTooltiptext("�ndert den Status zu: " + child.getStatusByStatusIdTo().getStatus());
              }
            
          }
          else
          {
            button.setDisabled(true);
            button.setTooltiptext("Sie besitzen nicht die n�tigen Rechte, um diesen Status zu �ndern");
          }

          if (child.getStatusByStatusIdTo().getIsPublic())
            button.setStyle("font-weight: bold;");

          final ProposalView refThis = this;

          button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
          {
            public void onEvent(Event event) throws Exception
            {
              // Status des Objektes �ndern
              try
              {
                // Reason (Grund) in modalem Fenster abfragen und hier �bergeben
                Map map = new HashMap();
                map.put("proposal", proposal);
                map.put("status_to_id", child.getStatusByStatusIdTo().getId());

                logger.debug("status_to_id: " + child.getStatusByStatusIdTo().getId());
                logger.debug("erstelle Fenster...");

                Window win = (Window) Executions.createComponents(
                        "/collaboration/proposal/proposalChangeStatus.zul", null, map);

                ((ProposalStatusChange) win).setUpdateInterface(refThis);

                logger.debug("�ffne Fenster...");
                win.doModal();
              }
              catch (Exception ex)
              {
                logger.error("Fehler in Klasse '" + this.getClass().getName()
                        + "': " + ex.getMessage());
              }
            }
          });

          box.getChildren().add(button);
        }

        letzterStatusButtons = proposal.getStatus().intValue();
      }
    }

    boolean inDiscussion = ProposalHelper.isProposalInDiscussion(proposal);

    // Zeitraum der Diskussion anzeigen
    String diskLabel = "Diskussion";
    String abstimmungLabel = "Abstimmung";
    
    if (inDiscussion)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
      if (proposal.getValidFrom() != null && proposal.getValidTo() != null)
      {
        diskLabel += " (" + sdf.format(proposal.getValidFrom()) + "-" + sdf.format(proposal.getValidTo()) + ")";
      }
      else if (proposal.getValidFrom() != null)
      {
        diskLabel += " (ab " + sdf.format(proposal.getValidFrom()) + ")";
      }
      else if (proposal.getValidTo() != null)
      {
        diskLabel += " (bis " + sdf.format(proposal.getValidTo()) + ")";
      }
    }
    else
    {
      diskLabel += " (inaktiv)";
      abstimmungLabel += " (inaktiv)";
    }

    ((Tab) getFellow("tabDiscussion")).setLabel(diskLabel);
    ((Tab) getFellow("tabAbstimmung")).setLabel(abstimmungLabel);
    
    
  }

  /**
   * Schlie�t das Fenster
   */
  public void onOkClicked()
  {
    this.setVisible(false);
    this.detach();
  }
  
  public void onShowWorkflow()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
              "/collaboration/workflow.zul",
              null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }

  /**
   * @return the proposal
   */
  public Proposal getProposal()
  {
    return proposal;
  }

  /**
   * @param proposal the proposal to set
   */
  public void setProposal(Proposal proposal)
  {
    this.proposal = proposal;
  }

  /**
   * @return the erstellerFull
   */
  public String getErstellerFull()
  {
    if (proposal != null)
    {
      return ProposalHelper.getNameFull(proposal.getCollaborationuser());
    }
    return "";
  }

  public void update(Object o, boolean edited)
  {

    if (o != null && o instanceof ReturnType)
    {
      // Grund in modalem Fenster angegeben.
      // �nderungen auf dem Bildschirm nun anzeigen
      ReturnType ret = (ReturnType) o;
      if (ret.isSuccess())
      {
        if(!(ret.getMessage().equals("InlinePropUpdate") || ret.getMessage().contains("gel�scht")))
            Messagebox.show("Status erfolgreich ge�ndert.", "Status �ndern", Messagebox.OK, Messagebox.INFORMATION);
				
        if(ret.getMessage().contains("gel�scht")){
            this.setVisible(false);
            this.detach();
            updateInterface.update(proposal, true);
            return;
        }
        
        loadProposal(proposal.getId());

        // GUI aktualisieren
        showCurrentStatus();
        proposalViewDetails.initListVerlauf();
        proposalViewDetails.initListObjects();

        // Liste auf Desktop aktualisieren
        if (updateInterface != null)
          updateInterface.update(proposal, true);
      }
      else
      {
        Messagebox.show(ret.getMessage(), "Status �ndern", Messagebox.OK, Messagebox.ERROR);
      }
    }
  }
}
