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
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalStatus;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.LoginType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
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
public class ImportCSV_Async extends Window implements AfterCompose, IGenericListActions, AsyncHandler<ImportCodeSystemResponse>, EventListener
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    byte[] bytes;
    GenericList genericListVocs;
    CodeSystem selectedCodeSystem;
    String session_id = "";
    org.zkoss.zk.ui.Session session;
    Response<ImportCodeSystemResponse> importWS;
    Timer timer;
    boolean importRunning = false;
    Window newTabWindow;
    boolean overrideAutoRelease = false;
    boolean csvOnly = true; //Nur Versionsimport möglich!
    String csVersion = "";
    private long importId;

    public ImportCSV_Async()
    {
    }

    public void onDateinameSelect(Event event)
    {
        try
        {
            bytes = null;
            //Media[] media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", 1, 50, true);

            //UploadEvent ue = new UploadEvent(_zclass, this, meds)
            //Media media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", true);
            Media media = ((UploadEvent) event).getMedia();

//text/csv
            if (media != null)
            {

                if (media.getContentType().equals("text/xml") || media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv")
                        || media.getContentType().contains("excel")
                        //Matthias media type added
                        || media.getContentType().equals("application/vnd.ms-excel")
                        || media.getContentType().equals("application/csv"))
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
            {
                logger.debug("Media ist null");
            }
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
            {
                logger.debug("data: " + data.getClass().getCanonicalName());
            }
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
            s += "Bitte wählen Sie eine Datei aus.";
        }

        String vokabular = ((Textbox) getFellow("tbVokabular")).getText();
        if (selectedCodeSystem == null && vokabular.length() == 0)
        {
            s += "\nBitte wählen Sie ein Codesystem aus oder geben Sie ein neuen Namen ein.";
        }

        ((Label) getFellow("labelImportStatus")).setValue(s);

        ((Include) getFellow("incList")).setVisible(vokabular.length() == 0);

        ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
    }

    public void startImport()
    {
        csvOnly = true; //Nur Versionsimport möglich!
        ((Label) getFellow("labelImportStatus")).setValue("");
        
        if ((selectedCodeSystem != null) && (selectedCodeSystem.getAutoRelease()) && csvOnly)
        {
            //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
            de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
            request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
            request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
            request_searchPub.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
            request_searchPub.getCodeSystem().setName(selectedCodeSystem.getName());
            de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub = port_searchPub.listCodeSystems(request_searchPub);

            if (respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
            {
                /*if ((respSearchPub.getCodeSystem() != null) && (respSearchPub.getCodeSystem().size() >= 1))
                {*/
                    Messagebox.show("Achtung diese Terminologie wird nach dem Import automatisch freigegeben. Möchten Sie fortfahren?", "Automatische Freigabe", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                    {
                        public void onEvent(Event evt) throws InterruptedException
                        {
                            if (evt.getName().equals("onYes"))
                            {
                                continueImport();
                            }
                        }
                    });
                /*}
                else
                {
                    overrideAutoRelease = true;
                    Messagebox.show("Terminologie kann nicht automatisch freigeben werden. Es sind mehrere Ziel-Terminologien mit ähnlichem Namen vorhanden", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
                    continueImport();
                }*/
            }
        }
        else
        {
            continueImport();
        }
    }

    public void continueImport()
    {
        csVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        if (csVersion.equals(""))
        {
            Messagebox.show("Bitte geben Sie eine Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }
        else
        {
            ((Button) getFellow("buttonImport")).setDisabled(true);
            ((Button) getFellow("buttonCancel")).setVisible(true);
//    String session_id = SessionHelper.getValue("session_id").toString();

            try
            {
                logger.debug("erstelle Fenster...");

                newTabWindow = (Window) Executions.createComponents(
                        "/gui/admin/modules/addImportTab.zul", null, null);

                newTabWindow.setClosable(false);

                logger.debug("öffne Fenster...");
                newTabWindow.doHighlighted();
            }
            catch (Exception ex)
            {
                if (newTabWindow != null)
                {
                    this.closeWindow(newTabWindow);
                }
                logger.error("Fehler in Klasse '" + this.getClass().getName()
                        + "': " + ex.getMessage());
            }

            String vokabular = ((Textbox) getFellow("tbVokabular")).getText();
            if (vokabular == null)
            {
                vokabular = "";
            }

//      boolean csvOnly = true; //Nur Versionsimport möglich!
            String msg = "";
            final Progressmeter progress = (Progressmeter) getFellow("progress");

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

            //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
            de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort(new MTOMFeature(true));

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
                request.setImportId(this.importId);
            }
            else
            {
                request.getCodeSystem().setId(selectedCodeSystem.getId());
                request.getCodeSystem().setName(selectedCodeSystem.getName());
                request.setImportId(this.importId);
                request.getCodeSystem().setAutoRelease(selectedCodeSystem.getAutoRelease());
            }

            CodeSystemVersion csv = new CodeSystemVersion();
            csv.setName(csVersion);
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

            final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
            if (desktop.isServerPushEnabled() == false)
            {
                desktop.enableServerPush(true);
            }
            final EventListener el = this;

            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (importWS == null || importWS.isCancelled() || importWS.isDone())
                        {
                            //desktop.enableServerPush(false);
                            logger.debug("TIMER FERTIG!");

                            // Import abgeschlossen
                            timer.cancel();
                        }
                        else
                        {
                            logger.debug("ON EVENT!");

                            ImportCodeSystemStatusRequestType request = new ImportCodeSystemStatusRequestType();
                            request.setLogin(new LoginType());
                            request.getLogin().setSessionID(session_id);
                            if (selectedCodeSystem.getId() != null)
                            {
                                request.setImportId(importId);
                            }
                            else
                            {
                                request.setImportId(importId);
                            }
                            ImportCodeSystemStatusResponse.Return response = importCodeSystemStatus(request);

                            logger.debug("Total: " + response.getTotalCount());
                            logger.debug("Current: " + response.getCurrentIndex());
                            int index = response.getCurrentIndex();
                            int total = response.getTotalCount();

                            if (importWS == null || importWS.isCancelled() || importWS.isDone())
                            {
                            }
                            else
                            {
                                String msg = "";

                                if (total > 0)
                                {
                                    int prozent = (index * 100) / total;
                                    msg = prozent+";";
                                    msg += index + "/" + total;

                                }
                                else
                                {
                                    progress.setValue(0);
                                    //((Label) getFellow("labelImportStatus")).setValue(index + "/unbekannt");
                                    msg = index + "/unbekannt";
                                }

                                Executions.schedule(desktop, el, new Event("updateStatus", null, msg));
                            }
                            //Executions.schedule(desktop, eventListener,  new Event("onNewData", null, new ADB_StatusType(msg, "", -1, saisonId, false)));
                        }
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();

                        if (newTabWindow != null)
                        {
                            newTabWindow.setVisible(false);
                        }
                        newTabWindow.detach();

                    }
                    //checkNotification();
                }
            },
                    10000, 10000); // 1. mal nach 1 Minute, dann alle 2 Stunden

            importRunning = true;
            importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);
        }
    }

    public void afterCompose()
    {
        session_id = SessionHelper.getValue("session_id").toString();
        session = Sessions.getCurrent(true);
        fillVocabularyList();
        showStatus();
        try
        {
            importId = SecureRandom.getInstance("SHA1PRNG").nextLong();
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.error(ex);
        }
    }

    /*
  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }
  
  private static ListCodeSystemsResponse.Return listCodeSystems(de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType parameter)
  {
    de.fhdo.terminologie.ws.search.Search_Service service = new Search_Service();
    de.fhdo.terminologie.ws.search.Search port = service.getSearchPort();
    return port.listCodeSystems(parameter);
  }
     */
    private static ImportCodeSystemStatusResponse.Return importCodeSystemStatus(de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.importCodeSystemStatus(parameter);
    }

    private static ImportCodeSystemCancelResponseType importCodeSystemCancel(de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelRequestType parameter)
    {
        //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.importCodeSystemCancel(parameter);
    }

    private void closeWindow(Window win)
    {
        if (win != null)
        {
            win.setVisible(false);
            win.detach();
        }
    }

    public void handleResponse(Response<ImportCodeSystemResponse> res)
    {
        importRunning = false;
        String msg = "";
        try
        {
            Return response = res.get().getReturn();
            msg = response.getReturnInfos().getMessage();
            logger.debug("Return: " + msg);

            if (response.getCodeSystem() != null)
            {
                //Proposal mit Proposalobjects anlegen
                Proposal proposal = new Proposal();
                proposal.setVocabularyId(response.getCodeSystem().getId());
                proposal.setVocabularyName(response.getCodeSystem().getName());
                proposal.setContentType("vocabulary");
                proposal.setVocabularyNameTwo("CodeSystem");

                types.termserver.fhdo.de.CodeSystem cs_prop = new types.termserver.fhdo.de.CodeSystem();
                cs_prop.setId(response.getCodeSystem().getId());
                cs_prop.setCurrentVersionId(response.getCodeSystem().getCurrentVersionId());
                cs_prop.setName(response.getCodeSystem().getName());
                cs_prop.getCodeSystemVersions().add(new CodeSystemVersion());

                for (CodeSystemVersion cs1 : response.getCodeSystem().getCodeSystemVersions())
                {
                    if (cs1.getVersionId().equals(cs_prop.getCurrentVersionId()))
                    {
                        cs_prop.getCodeSystemVersions().get(0).setName(cs1.getName());
                    }
                }

                ReturnType ret_prop = ProposalWorkflow.getInstance().addProposal(proposal, cs_prop, false, session);

                if (ret_prop.isSuccess())
                {
                    if (response.getCodeSystem().isAutoRelease() && !overrideAutoRelease)
                    {
                        ReturnType ret_status = ProposalWorkflow.getInstance().changeProposalStatus(proposal, 3L, "", null, null, session, response.getCodeSystem().isAutoRelease());
                        if (ret_status.isSuccess())
                        {
                            proposal.setStatus(3);
                            ret_status = ProposalWorkflow.getInstance().changeProposalStatus(proposal, 5L, "", null, null, session, response.getCodeSystem().isAutoRelease());
                            if (ret_status.isSuccess())
                            {
                                Set<Proposalobject> proposalobjects = new HashSet<Proposalobject>();

                                Session hb_session = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
                                hb_session.beginTransaction();

                                try
                                {
                                    Proposal proposal_db = (Proposal) hb_session.get(Proposal.class, proposal.getId());
                                    proposalobjects = proposal_db.getProposalobjects();
                                    hb_session.getTransaction().commit();

                                    ReturnType transfer_success = new ReturnType();
                                    transfer_success.setSuccess(false);

                                    long statusFrom = 5L;
                                    long statusToId = 3L;
                                    String reason = "autorelease";

                                    Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusToId);

                                    TerminologyReleaseManager releaseManager = new TerminologyReleaseManager(session.getAttribute("session_id").toString(), session.getAttribute("session_id").toString());
                                    transfer_success = releaseManager.initTransfer(proposalobjects, rel);

                                    if (transfer_success.isSuccess())
                                    {
                                        //logger.info(proposal.getVocabularyName() + ": Freigabe erfolgreich.");
                                        msg += " " + proposal.getVocabularyName() + ": Freigabe erfolgreich.";
                                        ProposalWorkflow.getInstance().sendEmailNotification(proposal, statusFrom, statusToId, reason);
                                    }
                                    else if (!transfer_success.isSuccess())
                                    {
                                        logger.info(proposal.getVocabularyName() + ": Freigabe fehlgeschlagen. " + transfer_success.getMessage());
                                        msg += " " + proposal.getVocabularyName() + ": Freigabe fehlgeschlagen. " + transfer_success.getMessage();

                                        //setting status back because transfer to public was not successful
                                        proposal.setStatus((int) statusFrom);
                                        ReturnType retResetStatus = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, null, null, session, true);
                                        if (retResetStatus.isSuccess())
                                        {
                                            logger.info(proposal.getVocabularyName() + ": Status wurde nicht geändert");
                                            msg += " " + proposal.getVocabularyName() + ": Status wurde nicht geändert.";
                                        }
                                        else
                                        {
                                            logger.info(proposal.getVocabularyName() + ": Status konnte nicht zurückgesetzt werden.");
                                            msg += " " + proposal.getVocabularyName() + ": Status konnte nicht zurückgesetzt werden.";
                                        }
                                    }
                                }
                                catch (Exception ex)
                                {
                                    if(!hb_session.getTransaction().wasRolledBack())
                                        hb_session.getTransaction().rollback();
                                    throw ex;
                                }
                                finally
                                {
                                    if(hb_session.isOpen())
                                        hb_session.close();
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.closeWindow(newTabWindow);
        }
        finally
        {
            overrideAutoRelease = false;
        }

        logger.debug("Import fertig, jetzt Komponenten verstecken");

        try
        {
            Window win = this;
            Executions.activate(win.getDesktop());

            final Progressmeter progress = (Progressmeter) getFellow("progress");
            progress.setVisible(false);
            ((Button) getFellow("buttonImport")).setDisabled(false);
            ((Button) getFellow("buttonCancel")).setVisible(false);
            ((Label) getFellow("labelImportStatus")).setValue(msg);

            this.closeWindow(newTabWindow);

            Executions.deactivate(win.getDesktop());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onEvent(Event event) throws Exception
    {
        if (importRunning == false)
        {
            return;
        }

        // In this part of code the ThreadLocals ARE available
        // Do something with result. You can touch any ZK stuff freely, just like when a normal event is posted.
        try
        {
            logger.debug("Event: " + event.getName());
            String message = event.getData().toString();

            if (event.getName().equals("end"))
            {
                Progressmeter progress = (Progressmeter) getFellow("progress");
                progress.setVisible(false);

                ((Button) getFellow("buttonImport")).setDisabled(false);
                ((Button) getFellow("buttonCancel")).setVisible(false);

                ((Label) getFellow("labelImportStatus")).setValue(message);
            }
            else
            {
                logger.debug("updateStatus: " + message);
                String[] values = message.split(";");
                if(values.length > 1)
                {
                    ((Progressmeter) getFellow("progress")).setValue(Integer.parseInt(values[0]));
                    ((Label) getFellow("labelImportStatus")).setValue(values[1]);
                }
                else
                {
                    ((Label) getFellow("labelImportStatus")).setValue(message);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void cancelImport()
    {
        if (importWS != null)
        {
            try
            {
                ImportCodeSystemCancelRequestType request = new ImportCodeSystemCancelRequestType();
                request.setLogin(new LoginType());

                String session_id = SessionHelper.getValue("session_id").toString();
                request.getLogin().setSessionID(session_id);
                ImportCodeSystemCancelResponseType response = importCodeSystemCancel(request);

                logger.debug("Response: " + response.getReturnInfos().getMessage());

                importRunning = false;
                importWS.cancel(true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
