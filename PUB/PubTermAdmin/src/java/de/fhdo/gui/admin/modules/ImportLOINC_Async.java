/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui.admin.modules;

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalStatus;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.gui.admin.modules.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author puraner
 */
public class ImportLOINC_Async extends Window implements AfterCompose, IGenericListActions, AsyncHandler<ImportCodeSystemResponse>, EventListener
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    byte[] bytes;
    GenericList genericListVocs;
    CodeSystem selectedCodeSystem;
    Checkbox cbUpdate;
    private Timer timer;
    Response<ImportCodeSystemResponse> importWS;
    boolean importRunning = false;
    String session_id;
    private long importId;
    org.zkoss.zk.ui.Session session;
    boolean overrideAutoRelease = false;
    Window newTabWindow;

    public ImportLOINC_Async()
    {
        timer = new Timer();
    }

    public void onDateinameSelect(Event event)
    {
        try
        {
            bytes = null;
            //Media[] media = Fileupload.get("Bitte w�hlen Sie ein Datei aus.", "Datei w�hlen", 1, 50, true);

            //UploadEvent ue = new UploadEvent(_zclass, this, meds)
            //Media media = Fileupload.get("Bitte w�hlen Sie ein Datei aus.", "Datei w�hlen", true);
            Media media = ((UploadEvent) event).getMedia();

            if (media != null)
            {

                if (media.getContentType().equals("text/xml")
                        || media.getContentType().equals("application/ms-excel")
                        || media.getContentType().equals("application/csv")
                        || media.getContentType().equals("text/csv")
                        || media.getContentType().equals("application/vnd.ms-excel")
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
                            baos.close();
                        }
                    }
                    else
                    {
                        logger.debug("media.isBinary() is false");
                        bytes = media.getStringData().getBytes("UTF-8");

                    }
                }

                logger.info("LOINC ct: " + media.getContentType());
                logger.info("LOINC format: " + media.getFormat());
                logger.info("LOINC byte-length: " + bytes.length);
                System.out.println("LOINC ct: " + media.getContentType());
                System.out.println("LOINC format: " + media.getFormat());
                System.out.println("LOINC byte-length: " + bytes.length);
                //logger.info("LOINC bytes: " + bytes);

                Textbox tb = (Textbox) getFellow("textboxDateiname");
                tb.setValue(media.getName());

            }
        }
        catch (Exception ex)
        {
            logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
        }

        showStatus();
        logger.info("LOINC File uploaded");
        System.out.println("LOINC File uploaded");
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
            //hb_session.getTransaction().begin();

            List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
            try
            {
                if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))
                {
                    //Get LOINC if it is assigned
                    ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();

                    for (AssignedTerm at : myTerms)
                    {

                        String hql = "from CodeSystem WHERE name='LOINC'";
                        List<CodeSystem> csList = hb_session.createQuery(hql).list();
                        if (!csList.isEmpty())
                        {
                            if (at.getClassId().equals(csList.get(0).getId()) && at.getClassname().equals("CodeSystem"))
                            {
                                GenericListRowType row = createRowFromCodesystem(csList.get(0));
                                dataList.add(row);
                            }
                        }
                    }

                }
                else
                {
                    //Get LOINC (Admin has rights)
                    String hql = "from CodeSystem WHERE name='LOINC'";
                    List<CodeSystem> csList = hb_session.createQuery(hql).list();

                    for (int i = 0; i < csList.size(); ++i)
                    {
                        CodeSystem cs = csList.get(i);
                        GenericListRowType row = createRowFromCodesystem(cs);

                        dataList.add(row);
                    }
                }

                //tx.commit();
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

    @Override
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

    @Override
    public void onNewClicked(String id)
    {
        ((Label) getFellow("labelImportStatus")).setValue("Bitte legen sie ein neues CodeSystem �ber den Terminologie-Browser an!");
    }

    @Override
    public void onEditClicked(String id, Object data)
    {
    }

    @Override
    public void onDeleted(String id, Object data)
    {
    }

    private void showStatus()
    {
        String s = "";

        if (bytes == null)
        {
            s += "Bitte w�hlen Sie eine Datei aus.";
        }

        if (selectedCodeSystem == null)
        {
            s += "\nBitte w�hlen Sie ein Codesystem aus.";
        }

        ((Label) getFellow("labelImportStatus")).setValue(s);

        ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
    }

    public void startImport()
    {
        ((Label) getFellow("labelImportStatus")).setValue("");
        
        String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
        
        if((!cbUpdate.isChecked()) && (vokVersion.equals("")))
        {
            String value = ((Label) getFellow("labelImportStatus")).getValue();
            ((Label) getFellow("labelImportStatus")).setValue(value + " Bitte geben Sie einen Versionsnamen an oder w�hlen Sie die Updatefunktion.");
            return;
        }
        
        if ((selectedCodeSystem != null) && (selectedCodeSystem.getAutoRelease()))
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
                Messagebox.show("Achtung diese Terminologie wird nach dem Import automatisch freigegeben. M�chten Sie fortfahren?", "Automatische Freigabe", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                {
                    public void onEvent(Event evt) throws InterruptedException
                    {
                        if (evt.getName().equals("onYes"))
                        {
                            continueImport();
                        }
                    }
                });
            }
        }
        else
        {
            continueImport();
        }
    }

    public void continueImport()
    {
        ((Button) getFellow("buttonImport")).setDisabled(true);
        ((Button) getFellow("buttonCancel")).setVisible(true);

        session_id = SessionHelper.getSessionAttributeByName("session_id").toString();

        String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        try
        {
            logger.debug("erstelle Fenster...");

            newTabWindow = (Window) Executions.createComponents(
                    "/gui/admin/modules/addImportTab.zul", null, null);

            newTabWindow.setClosable(false);

            logger.debug("�ffne Fenster...");
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

        String msg = "";
        final Progressmeter progress = (Progressmeter) getFellow("progress");

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
        request.getCodeSystem().setId(selectedCodeSystem.getId());
        request.getCodeSystem().setName(selectedCodeSystem.getName());

        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setName(vokVersion);
        request.getCodeSystem().getCodeSystemVersions().add(csv);

        // Claml-Datei
        request.setImportInfos(new ImportType());
        request.getImportInfos().setFormatId(200l); // LOINC_ID
        request.getImportInfos().setFilecontent(bytes);
        request.getImportInfos().setOrder(cbUpdate.isChecked()); // In the context of LOINC => Update existing one, no new Version!
        request.setImportId(importId);

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
                catch (WebServiceException wsEx)
                {
                    Throwable t = wsEx.getCause();
                    if (t instanceof NullPointerException)
                    {
                        importRunning = false;
                        importWS.cancel(true);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                //checkNotification();
            }
        },
                10000, 10000); // 1. mal nach 1 Minute, dann alle 2 Stunden

        importRunning = true;
        importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);
    }

    @Override
    public void afterCompose()
    {
        cbUpdate = (Checkbox) getFellow("cbUpdate");
        fillVocabularyList();
        showStatus();
        session_id = SessionHelper.getSessionAttributeByName("session_id").toString();
        session = Sessions.getCurrent(true);
        try
        {
            importId = SecureRandom.getInstance("SHA1PRNG").nextLong();
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.error(ex);
        }
    }

    public void cbUpdateChecked()
    {
        Textbox tb = (Textbox) getFellow("tbVokabularVersion");
        if (cbUpdate.isChecked())
        {

            tb.setDisabled(true);
        }
        else
        {
            tb.setDisabled(false);
        }
    }

    @Override
    public void handleResponse(Response<ImportCodeSystemResponse> res)
    {
        // Antwort des Import-Webservices
        importRunning = false;
        String msg = "";
        try
        {
            Return response = res.get().getReturn();
            msg = response.getReturnInfos().getMessage();
            logger.debug("Return: " + msg);

            if (response.getCodeSystem() != null)
            {
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
                                        logger.info(proposal.getVocabularyName() + ": Freigabe erfolgreich.");
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
                                            logger.info(proposal.getVocabularyName() + ": Status wurde nicht ge�ndert");
                                            msg += " " + proposal.getVocabularyName() + ": Status wurde nicht ge�ndert.";
                                        }
                                        else
                                        {
                                            logger.info(proposal.getVocabularyName() + ": Status konnte nicht zur�ckgesetzt werden.");
                                            msg += " " + proposal.getVocabularyName() + ": Status konnte nicht zur�ckgesetzt werden.";
                                        }
                                    }
                                }
                                catch (Exception ex)
                                {
                                    hb_session.getTransaction().rollback();
                                    throw ex;
                                }
                                finally
                                {
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
            newTabWindow.setVisible(false);
            newTabWindow.detach();
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

            Progressmeter progress = (Progressmeter) getFellow("progress");
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

    @Override
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

    private static ImportCodeSystemStatusResponse.Return importCodeSystemStatus(de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusRequestType parameter)
    {
        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.importCodeSystemStatus(parameter);
    }

    private void closeWindow(Window win)
    {
        if (win != null)
        {
            win.setVisible(false);
            win.detach();
        }
    }
}