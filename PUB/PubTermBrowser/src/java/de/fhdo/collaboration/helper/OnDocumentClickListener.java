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
package de.fhdo.collaboration.helper;


import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.File;
import de.fhdo.collaboration.db.classes.Link;
import java.io.IOException;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;
//import de.fhdo.gui.viewer.ImageViewer;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**

 @author Robert Mützner
 */
public class OnDocumentClickListener implements EventListener
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  Object referClass;

  public OnDocumentClickListener(Object ReferClass)
  {
    referClass = ReferClass;
  }

  public void onEvent(Event event) throws Exception
  {
    if (logger.isDebugEnabled())
      logger.debug("onEvent(): ");

    logger.debug("name: " + event.getName());

    if (event.getName().equals("onRightClick"))
    {
      //image.setContext("docContext");
      if (event.getTarget() != null)
      {
        if (event.getTarget() instanceof Image)
        {
          Image image = (Image) event.getTarget();
          //image.setContext("docContext");

          // TODO
          if (referClass == null)
            logger.warn("TODO - Event abarbeiten, Class: " + referClass.getClass().getCanonicalName());
          /* if (referClass != null && referClass instanceof Standards)
           {
           ((Standards) referClass).setClickedDocument(image.getAttribute("document_id").toString());
           } */

          /* if (referClass != null && referClass instanceof Standards)
           {
           ((Standards) referClass).setClickedDocument(image.getAttribute("document_id").toString());
           } */



        }
      }
    }
    else
    {
      if (event.getTarget() != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("Classname: " + event.getTarget().getClass().getName());

        if (event.getTarget() instanceof Image)
        {
          Image image = (Image) event.getTarget();

          String documentID = image.getAttribute("document_id").toString();
          Object textObj = image.getAttribute("text");
          Object linkObj = image.getAttribute("link");

          if (textObj != null)
          {
            openNote(textObj.toString());
          }
          else if (linkObj != null)
          {
            openLink(linkObj.toString());
          }
          else
          {
            if (logger.isDebugEnabled())
              logger.debug("Image identifiziert mit id: " + documentID);

            openDocument(documentID);
          }
        }


      }
    }
  }

  private void openNote(String Text) throws InterruptedException
  {
    //Messagebox.show(Text, "Notiz", Messagebox.OK, Messagebox.INFORMATION);
    Map map = new HashMap();
    map.put("note", Text);
    logger.debug("erstelle Fenster...");
    Window win = (Window) Executions.createComponents(
      "/gui/main/modules/attachmentNote.zul", null, map);

    logger.debug("öffne Fenster...");
    win.doModal();
  }

  private void openLink(String Text) throws InterruptedException
  {
    //Messagebox.show(Text, "Link", Messagebox.OK, Messagebox.INFORMATION);
    String link = Text;

    if (link.startsWith("http://") == false)
      link = "http://" + link;

    Executions.getCurrent().sendRedirect(link, "_blank");
  }

  private void openDocument(String Attachment) throws InterruptedException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("openDocument() mit id: " + Attachment);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      //org.hibernate.Transaction tx = hb_session.beginTransaction();

      File file = (File) hb_session.get(File.class, Long.parseLong(Attachment));

      if (file != null)
      {
        Link att = (Link) hb_session.get(Link.class, Long.parseLong(Attachment));

        Filedownload.save(file.getData(),
          att.getMimeType(),
          att.getContent());
      }
      else
      {
        // Fehlermeldung ausgeben
        Messagebox.show("Anhang konnte nicht in der Datenbank gefunden werden!", "Download", Messagebox.OK, Messagebox.INFORMATION);
      }


      //Collection result = new LinkedHashSet(q.list());

      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
        //hb_session.getTransaction().rollback();
    }finally{
        hb_session.close();
    }

  }
}
