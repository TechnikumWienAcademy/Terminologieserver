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
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.ValueSet;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalStatus;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
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
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.ImportValueSetResponse;
import de.fhdo.terminologie.ws.administration.ImportValueSetResponse.Return;
import de.fhdo.terminologie.ws.administration.ImportValueSetStatusRequestType;
import de.fhdo.terminologie.ws.administration.ImportValueSetStatusResponse;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class ImportVS_CSV_Async extends Window implements AfterCompose, IGenericListActions, AsyncHandler<ImportValueSetResponse>, EventListener
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    byte[] bytes;
    GenericList genericListVocs;
    ValueSet selectedValueSet;
    Checkbox cbOrder;
    String session_id = "";
    org.zkoss.zk.ui.Session session;
    Response<ImportValueSetResponse> importWS;
    Timer timer;
    boolean importRunning = false;
    Window newTabWindow;
    boolean vsvOnly = false;
    String vsVersion = "";
    boolean overrideAutoRelease = false;
    private long importId;

    public ImportVS_CSV_Async()
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

            if (media != null)
            {
                if (media.getContentType().equals("text/xml") || media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv")
                        || media.getContentType().equals("application/csv")
                        //Matthias media type added
                        || media.getContentType().equals("application/vnd.ms-excel"))
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
                logger.debug("byte-length: " + bytes.length);
                logger.debug("bytes: " + bytes);

                Textbox tb = (Textbox) getFellow("textboxDateiname");
                tb.setValue(media.getName());

            }
        }
        catch (Exception ex)
        {
            logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
        }

        showStatus();

    }

    private void fillValueSetList()
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

                    ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();

                    for (AssignedTerm at : myTerms)
                    {

                        if (at.getClassname().equals("ValueSet"))
                        {

                            ValueSet cs = (ValueSet) hb_session.get(ValueSet.class, at.getClassId());
                            GenericListRowType row = createRowFromCodesystem(cs);
                            dataList.add(row);
                        }
                    }
                    Collections.sort(dataList, new ComparatorRowTypeName(true));
                }
                else
                {

                    String hql = "from ValueSet order by name";
                    List<ValueSet> csList = hb_session.createQuery(hql).list();

                    for (int i = 0; i < csList.size(); ++i)
                    {
                        ValueSet cs = csList.get(i);
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

    private GenericListRowType createRowFromCodesystem(ValueSet cs)
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
            if (data instanceof ValueSet)
            {
                //de.fhdo.list.GenericListRowType row = (de.fhdo.list.GenericListRowType) data;
                selectedValueSet = (ValueSet) data;
                logger.debug("Selected Valueset: " + selectedValueSet.getName());

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
        // TODO Neues Vokabular anlegen
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

        String vsVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        if (bytes == null)
        {
            s += "Bitte wählen Sie eine Datei aus.";
        }

        if (selectedValueSet == null && (vsVersion == null || vsVersion.length() == 0))
        {
            s += "\nBitte wählen Sie ein Value Set aus oder geben Sie einen Namen für ein neues ein.";
        }

        ((Label) getFellow("labelImportStatus")).setValue(s);

        ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
    }

    public void startImport()
    {
        ((Label) getFellow("labelImportStatus")).setValue("");
        vsVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
        if ((vsVersion != null) && (vsVersion.length() > 0))
        {
            vsvOnly = false;
        }
        else
        {
            vsvOnly = true;
        }

        if ((selectedValueSet != null) && (selectedValueSet.getAutoRelease()) && vsvOnly)
        {
            //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
            de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
            request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
            request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
            request_searchPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
            request_searchPub.getValueSet().setName(selectedValueSet.getName());
            de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);

            if (respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
            {
                /*if ((respSearchPub.getValueSet() != null) && (respSearchPub.getValueSet().size() >= 1))
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
        ((Button) getFellow("buttonImport")).setDisabled(true);
//    ((Button) getFellow("buttonCancel")).setVisible(true);

        //String session_id = SessionHelper.getValue("session_id").toString();

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

        String msg = "";
        final Progressmeter progress = (Progressmeter) getFellow("progress");

        //{
        progress.setVisible(true);

        //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort(new MTOMFeature(true));

        // Login
        ImportValueSetRequestType request = new ImportValueSetRequestType();
        request.setLogin(new LoginType());
        request.getLogin().setSessionID("" + session_id);
        request.setImportId(importId);

        // Codesystem
        request.setValueSet(new types.termserver.fhdo.de.ValueSet());
        if (!vsvOnly)
        {
            request.getValueSet().setName(vsVersion); //neues VS
        }
        else
        {
            request.getValueSet().setId(selectedValueSet.getId()); //Version only
            request.getValueSet().setAutoRelease(selectedValueSet.getAutoRelease());
            request.getValueSet().setName(selectedValueSet.getName());
        }

        //ValueSetVersion csv = new ValueSetVersion();
        //csv.setName(vsVersion);
        //request.getValueSet().getValueSetVersions().add(csv);
        // Claml-Datei
        request.setImportInfos(new ImportType());
        request.getImportInfos().setFilecontent(bytes);
        request.getImportInfos().setFormatId(300l);
        request.getImportInfos().setOrder(cbOrder.isChecked());

        if (vsvOnly)
        {
            request.getImportInfos().setRole(CODES.ROLE_ADMIN + ":true"); //Zwischenlösung...
        }
        else
        {
            request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole() + ":false"); //Zwischenlösung...
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
                        ImportValueSetStatusRequestType request = new ImportValueSetStatusRequestType();
                        request.setLogin(new LoginType());
                        request.getLogin().setSessionID(session_id);
                        request.setImportId(importId);

                        ImportValueSetStatusResponse.Return response = importValueSetStatus(request);

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
                                msg = index + "/unbekannt";
                            }

                            Executions.schedule(desktop, el, new Event("updateStatus", null, msg));
                        }
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
        importWS = (Response<ImportValueSetResponse>) port.importValueSetAsync(request, this);
    }

    public void afterCompose()
    {
        cbOrder = (Checkbox) getFellow("cbOrder");
        fillValueSetList();
        showStatus();
        session_id = SessionHelper.getValue("session_id").toString();
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

    /*private static CreateValueSetResponse.Return createValueSet(de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createValueSet(parameter);
  }*/
    public void handleResponse(Response<ImportValueSetResponse> res)
    {
        importRunning = false;
        String msg = "";
        try
        {
            Return response = res.get().getReturn();
            msg = response.getReturnInfos().getMessage();
            logger.debug("Return: " + msg);

            if (response.getValueSet() != null)
            {
                //Proposal mit Proposalobjects anlegen
                Proposal proposal = new Proposal();
                proposal.setVocabularyId(response.getValueSet().getId());
                proposal.setVocabularyName(response.getValueSet().getName());
                proposal.setContentType("valueset");
                proposal.setVocabularyNameTwo("ValueSet");

                types.termserver.fhdo.de.ValueSet vs_prop = new types.termserver.fhdo.de.ValueSet();
                vs_prop.setId(response.getValueSet().getId());
                vs_prop.setCurrentVersionId(response.getValueSet().getCurrentVersionId());
                vs_prop.setName(response.getValueSet().getName());
                vs_prop.getValueSetVersions().add(new ValueSetVersion());

                for (ValueSetVersion vsv1 : response.getValueSet().getValueSetVersions())
                {
                    if (vsv1.getVersionId().equals(vs_prop.getCurrentVersionId()))
                    {
                        vs_prop.getValueSetVersions().get(0).setName(vsv1.getName());
                    }
                }

                ReturnType ret_prop = ProposalWorkflow.getInstance().addProposal(proposal, vs_prop, false, session);

                if (ret_prop.isSuccess())
                {
                    if (response.getValueSet().isAutoRelease() && !overrideAutoRelease)
                    {
                        ReturnType ret_status = ProposalWorkflow.getInstance().changeProposalStatus(proposal, 3L, "", null, null, session, response.getValueSet().isAutoRelease());
                        if (ret_status.isSuccess())
                        {
                            proposal.setStatus(3);
                            ret_status = ProposalWorkflow.getInstance().changeProposalStatus(proposal, 5L, "", null, null, session, response.getValueSet().isAutoRelease());
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

    private void closeWindow(Window win)
    {
        if (win != null)
        {
            win.setVisible(false);
            win.detach();
        }
    }
    
    private static ImportValueSetStatusResponse.Return importValueSetStatus(de.fhdo.terminologie.ws.administration.ImportValueSetStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.importValueSetStatus(parameter);
    }

}
