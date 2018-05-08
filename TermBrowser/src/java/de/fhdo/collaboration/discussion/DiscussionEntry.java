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
package de.fhdo.collaboration.discussion;

import de.fhdo.collaboration.db.classes.Discussion;
import de.fhdo.collaboration.db.classes.Quote;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert M�tzner
 */
public class DiscussionEntry extends Window implements IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean inDiscussion;
  private Discussion discussion;
  private IUpdateModal updateInterface;

  public DiscussionEntry()
  {
  }

  /**
   * @return the discussion
   */
  public Discussion getDiscussion()
  {
    return discussion;
  }

  /**
   * @param discussion the discussion to set
   */
  public void setDiscussion(Discussion discussion)
  {
    logger.debug("setDiscussion() mit Disc-ID: " + discussion.getId() + ", user-ID: " + discussion.getCollaborationuser().getId());
    this.discussion = discussion;

    showData();

    final DiscussionEntry refThis = this;

    ((Button) getFellow("buttonEdit")).addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        try
        {
          logger.debug("onEditClicked()");

          Map map = new HashMap();
          map.put("discussion_id", refThis.discussion.getId());
          map.put("winHeight", "300px");

          logger.debug("erstelle Fenster...");

          Window win = (Window) Executions.createComponents(
                  "/collaboration/discussion/discussionEntryDetails.zul", null, map);

          ((DiscussionEntryDetails) win).setUpdateInterface(refThis);

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

    ((Button) getFellow("buttonZitieren")).addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        try
        {
          Map map = new HashMap();
          map.put("quoted_text", null);  // TODO nur markierten Text �bergeben
          map.put("quoted_discussion_id", refThis.discussion.getId());
          map.put("proposal_id", refThis.discussion.getProposal().getId());
          map.put("winHeight", "500px");

          logger.debug("erstelle Fenster...");

          Window win = (Window) Executions.createComponents(
                  "/collaboration/discussion/discussionEntryDetails.zul", null, map);

          ((DiscussionEntryDetails) win).setUpdateInterface(refThis);

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
  }

  private void showData()
  {
    logger.debug("showData()");
    int height = 220;

    // Benutzerdaten
    ((Label) getFellow("labelUsername")).setValue(discussion.getCollaborationuser().getName());
    ((Label) getFellow("labelName")).setValue(ProposalHelper.getName(discussion.getCollaborationuser()));
    ((Label) getFellow("labelOrg")).setValue(ProposalHelper.getOrganisation(discussion.getCollaborationuser()));

    // Eintrag
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
    ((Label) getFellow("labelDatum")).setValue(sdf.format(discussion.getDate()));

    if (discussion.getChanged() != null)
    {
      ((Label) getFellow("labelAenderung")).setValue(" , zuletzt ge�ndert am " + sdf.format(discussion.getChanged()));
    }
    else
      ((Label) getFellow("labelAenderung")).setValue("");

    ((Label) getFellow("labelPostNumber")).setValue("#" + discussion.getPostNumber());


    Html html = (Html) getFellow("content");
    html.setContent(discussion.getLongDescription());
    //logger.debug(;html.getHeight()

    // Zitat
    Set<Quote> quotes = discussion.getQuotesForDiscussionId();
    boolean isQuoted = quotes != null && quotes.size() > 0;

    ((North) getFellow("blZitat")).setVisible(isQuoted);

    if (isQuoted)
    {
      Quote quote = quotes.iterator().next();
      logger.debug("Quote: " + quote.getId());
      Discussion dQuoted = quote.getDiscussionByDiscussionIdQuoted();
      logger.debug("dQuoted: " + dQuoted.getId());
      String header = "Zitat: #" + dQuoted.getPostNumber() + " '" + ProposalHelper.getName(dQuoted.getCollaborationuser()) + "' vom " + sdf.format(dQuoted.getDate());
      ((Caption) getFellow("zitatCaption")).setLabel(header);

      if (quote.getText() == null || quote.getText().length() == 0)
        ((Html) getFellow("contentQuote")).setContent(dQuoted.getLongDescription());
      else
        ((Html) getFellow("contentQuote")).setContent(quote.getText());

      height += 120;
    }



    // Buttons
    ((Button) getFellow("buttonEdit")).setVisible(inDiscussion && discussion.getCollaborationuser().getId().longValue() == SessionHelper.getCollaborationUserID());
    ((Button) getFellow("buttonZitieren")).setVisible(inDiscussion);

    //logger.debug("H�he html: " + html.getHeight());
    this.setHeight(height + "px");
  }

  public void onNew()
  {
    Messagebox.show("Test");
  }

  /**
   * @return the inDiscussion
   */
  public boolean isInDiscussion()
  {
    return inDiscussion;
  }

  /**
   * @param inDiscussion the inDiscussion to set
   */
  public void setInDiscussion(boolean inDiscussion)
  {
    this.inDiscussion = inDiscussion;
  }

  public void update(Object o, boolean edited)
  {
    if (o != null && o instanceof Discussion)
    {
      if (edited)
      {
        logger.debug("Aktualisiere Daten...");
        discussion = (Discussion) o;
        showData();
      }
      else
      {
        // Liste aktualisieren
        if(updateInterface != null)
          updateInterface.update(o, edited);
      }
      
      
      
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
