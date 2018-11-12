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
package de.fhdo.gui.admin.modules;

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.LoginType;
import de.fhdo.terminologie.ws.administration.Status;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner
 */
public class ImportCSV extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  GenericList genericListVocs;
  CodeSystem selectedCodeSystem;

  public ImportCSV()
  {
  }

  public void onDateinameSelect(Event event)
  {
    try
    {
      bytes = null;
      //Media[] media = Fileupload.get("Bitte wÃ¤hlen Sie ein Datei aus.", "Datei wÃ¤hlen", 1, 50, true);

      //UploadEvent ue = new UploadEvent(_zclass, this, meds)
      //Media media = Fileupload.get("Bitte wÃ¤hlen Sie ein Datei aus.", "Datei wÃ¤hlen", true);
      Media media = ((UploadEvent) event).getMedia();

//text/csv
      if (media != null)
      {

        if (media.getContentType().equals("text/xml") 
                || media.getContentType().equals("application/ms-excel") 
                || media.getContentType().equals("text/csv")
                || media.getContentType().contains("excel")
		//Matthias media type added
                || media.getContentType().equals("application/vnd.ms-excel")
		|| media.getContentType().equals("application/csv")
                //3.2.20
                || media.getContentType().equals("application/soap+xml"))
            
        {
          if (media.isBinary())
          {
            logger.debug("media.isBinary()");

            if (media.inMemory())
            {
              logger.debug("media.getByteData()");

              bytes = media.getByteData();
              //f.setData(media.getByteData());
            }
            else
            {
              logger.debug("media.getStreamData()");
							
							InputStream input = media.getStreamData();
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              int bytesRead;
              byte[] tempBuffer = new byte[8192 * 2];
              while ((bytesRead = input.read(tempBuffer)) != -1)
              {
                baos.write(tempBuffer, 0, bytesRead);
              }

              bytes = baos.toByteArray();
              //f.setData(baos.toByteArray());
              baos.close();
            }
          }
          else
          {
            logger.debug("media.isBinary() is false");
						//matthias: changed to ISO-8859-1
            bytes = media.getStringData().getBytes("ISO-8859-1");
            //Reader reader = FileCopy.asReader(media);
            //bytes = reader.toString().getBytes();

          }
        }
        //getAttachment().setFile(f);
        //getAttachment().getAttachment().setFilename(media.getName());
        //getAttachment().getAttachment().setMimeTypeCd(media.getContentType());

        logger.debug("ct: " + media.getContentType());
        logger.debug("format: " + media.getFormat());

        if (bytes != null)
        {
          logger.debug("byte-length: " + bytes.length);
          logger.debug("bytes: " + bytes);

          Textbox tb = (Textbox) getFellow("textboxDateiname");
          tb.setValue(media.getName());
        }
      }
      else
        logger.debug("Media ist null");
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
    }

    showStatus();

  }

  private void fillVocabularyList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))
        {

          ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();

          for (AssignedTerm at : myTerms)
          {

            if (at.getClassname().equals("CodeSystem"))
            {

              CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, at.getClassId());
              GenericListRowType row = createRowFromCodesystem(cs);
              dataList.add(row);
            }
          }
          Collections.sort(dataList, new ComparatorRowTypeName(true));
        }
        else
        {

          String hql = "from CodeSystem order by name";
          List<CodeSystem> csList = hb_session.createQuery(hql).list();

          for (int i = 0; i < csList.size(); ++i)
          {
            CodeSystem cs = csList.get(i);
            GenericListRowType row = createRowFromCodesystem(cs);

            dataList.add(row);
          }
        }
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      // Liste initialisieren
      Include inc = (Include) getFellow("incList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListVocs = (GenericList) winGenericList;
      //genericListVocs.setId("0");

      genericListVocs.setListActions(this);
      genericListVocs.setButton_new(false);
      genericListVocs.setButton_edit(false);
      genericListVocs.setButton_delete(false);
      genericListVocs.setListHeader(header);
      genericListVocs.setDataList(dataList);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
    {
      if (data instanceof de.fhdo.list.GenericListRowType)
      {
        de.fhdo.list.GenericListRowType row = (de.fhdo.list.GenericListRowType) data;
        selectedCodeSystem = (CodeSystem) row.getData();
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());

        showStatus();
      }
      else if (data instanceof CodeSystem)
      {
        selectedCodeSystem = (CodeSystem) data;
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());

        showStatus();
      }
      else
        logger.debug("data: " + data.getClass().getCanonicalName());
    }

  }

  public void onNewClicked(String id)
  {
    ((Label) getFellow("labelImportStatus")).setValue("Bitte legen sie ein neues CodeSystem über den Terminologie-Browser an!");
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
  }

  public void showStatus()
  {
    String s = "";

    if (bytes == null)
    {
      s += "Bitte wÃ¤hlen Sie eine Datei aus.";
    }

    String vokabular = ((Textbox) getFellow("tbVokabular")).getText();
    if (selectedCodeSystem == null && vokabular.length() == 0)
    {
      s += "\nBitte wÃ¤hlen Sie ein Codesystem aus oder geben Sie ein neuen Namen ein.";
    }

    ((Label) getFellow("labelImportStatus")).setValue(s);
    
    ((Include)getFellow("incList")).setVisible(vokabular.length() == 0);

    ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
  }

  public void startImport()
  {
    ((Button) getFellow("buttonImport")).setDisabled(true);
    ((Button) getFellow("buttonCancel")).setVisible(true);

    String session_id = SessionHelper.getValue("session_id").toString();

    String vokabular = ((Textbox) getFellow("tbVokabular")).getText();
    if(vokabular == null)
      vokabular = "";
    
    String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
    
    if(vokVersion.equals(""))
    {
      Messagebox.show("Bitte geben Sie eine Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }
    else
    {
    
      boolean csvOnly = true; //Nur Versionsimport mÃ¶glich!
      String msg = "";
      Progressmeter progress = (Progressmeter) getFellow("progress");

      // Vokabular anlegen
      /*CreateCodeSystemRequestType ccsRequest = new CreateCodeSystemRequestType();
       ccsRequest.setLogin(new de.fhdo.terminologie.ws.authoring.LoginType());
       ccsRequest.getLogin().setSessionID("" + session_id);

       ccsRequest.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
       //ccsRequest.setCodeSystem(selectedCodeSystem);
       ccsRequest.getCodeSystem().setId(selectedCodeSystem.getId());
       ccsRequest.getCodeSystem().setName(selectedCodeSystem.getName());
       CodeSystemVersion csv = new CodeSystemVersion();
       csv.setName(vokVersion);
       ccsRequest.getCodeSystem().getCodeSystemVersions().add(csv);

       CreateCodeSystemResponse.Return ccsResponse = createCodeSystem(ccsRequest);

       if (ccsResponse.getReturnInfos().getStatus() == Status.OK)*/
      //{
      progress.setVisible(true);

      de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
      de.fhdo.terminologie.ws.administration.Administration port = service.getAdministrationPort(new MTOMFeature(true));

      // Login
      ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
      request.setLogin(new LoginType());
      request.getLogin().setSessionID("" + session_id);
        //logger.debug("Session-ID: ");

      // Codesystem
      request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
      if (vokabular.length() > 0)
      {
        request.getCodeSystem().setId(0l);
        request.getCodeSystem().setName(vokabular);
      }
      else
      {
        request.getCodeSystem().setId(selectedCodeSystem.getId());
        request.getCodeSystem().setName(selectedCodeSystem.getName());
      }

      CodeSystemVersion csv = new CodeSystemVersion();
      csv.setName(vokVersion);
      request.getCodeSystem().getCodeSystemVersions().add(csv);

      // Claml-Datei
      request.setImportInfos(new ImportType());
      request.getImportInfos().setFormatId(194l); // CSV_ID
      request.getImportInfos().setFilecontent(bytes);
      request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole());
      if (csvOnly)
      {
        request.getImportInfos().setRole(CODES.ROLE_ADMIN);
      }
      else
      {
        request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole());
      }

      Return response = port.importCodeSystem(request);
      //importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);

      msg = response.getReturnInfos().getMessage();
      logger.debug("Return: " + msg);
      ReturnType ret = null;
      String returnStr = "";

      //CodeSystemVersion
      if (response.getReturnInfos().getStatus().equals(Status.OK))
      {

        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER) && !csvOnly)
        {

          //Proposal Im Falle von CSV gibt es nur neue Versionen keine MÃ¶glichkeit eines neuen CS
          returnStr = msg + " | " + ret.getMessage();
        }
        else
        {
          returnStr = msg;
        }
      }
      else
      {
        returnStr = msg;
      }

      //progress = (Progressmeter) getFellow("progress");
      //}
      /*else
       {
       logger.error(ccsResponse.getReturnInfos().getStatus());
       logger.error(ccsResponse.getReturnInfos().getMessage());
       msg = ccsResponse.getReturnInfos().getMessage();
       }*/
      progress.setVisible(false);
      ((Button) getFellow("buttonImport")).setDisabled(false);
      ((Button) getFellow("buttonCancel")).setVisible(false);
      ((Label) getFellow("labelImportStatus")).setValue(returnStr);

      //Return response = port.importCodeSystem(request);
      //logger.debug(response.getReturnInfos().getMessage());
      //if(response.getReturnInfos().set)
      //return port.importCodeSystem(parameter);
      //progress.setVisible(false);      
    }
  }

  public void afterCompose()
  {
    fillVocabularyList();
    showStatus();
  }

  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }
}
