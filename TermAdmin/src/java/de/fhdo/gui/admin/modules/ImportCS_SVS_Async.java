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
import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalStatus;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.gui.admin.modules.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.HQLParameterHelper;
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
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
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
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner
 */
public class ImportCS_SVS_Async extends Window implements AfterCompose, IGenericListActions, AsyncHandler<ImportCodeSystemResponse>, EventListener
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    byte[] bytes;
    GenericList genericListVocs;
    CodeSystem selectedCodeSystem;
    Checkbox cbNewVoc;
    Textbox tbNewVoc;
    Textbox tbNewVocVersion;
    Textbox textboxDateiname;
    private String autoName = "";
    String session_id = "";
    org.zkoss.zk.ui.Session session;
    Response<ImportCodeSystemResponse> importWS;
    Timer timer;
    boolean importRunning = false;
    Window newTabWindow;
    boolean overrideAutoRelease = false;
    boolean csvOnly = false;
    String csVersion = "";
    private long importId;

    public ImportCS_SVS_Async()
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
            logger.info(event.getName());

            Media media = ((UploadEvent) event).getMedia();

            if (media != null)
            {
                if (media.getContentType().equals("text/xml") 
                        || media.getContentType().equals("application/ms-excel") 
                        || media.getContentType().equals("text/csv")
                        //Matthias media type added
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
                            //f.setData(baos.toByteArray());
                            baos.close();
                        }
                    }
                    else
                    {
                        logger.debug("media.isBinary() is false");
                        //UTF-8 produces error in encoding
//            if (media.getStringData().contains("UTF-8")){
//							bytes = media.getStringData().getBytes("UTF-8");
//						} else {
                        bytes = media.getStringData().getBytes("ISO-8859-1");
//						}

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
                String[] auto = (media.getName()).split("\\.");
                autoName = auto[0];
                cbNewVoc.setDisabled(false);
            }
        }
        catch (IOException ex)
        {
            logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
        }
        catch (ComponentNotFoundException ex)
        {
            logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
        }
        catch (WrongValueException ex)
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
            //hb_session.getTransaction().begin();

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
                            if (cs != null)
                            {
                                GenericListRowType row = createRowFromCodesystem(cs);
                                dataList.add(row);
                            }
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
                        logger.info(cs.getName());
                        logger.info(cs.getId());
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

    public void newVocChecked()
    {

        if (cbNewVoc.isChecked() && !textboxDateiname.getText().equals(""))
        {
            selectedCodeSystem = new CodeSystem();
            selectedCodeSystem.setAutoRelease(false);
            tbNewVocVersion.setDisabled(false);
            tbNewVoc.setDisabled(false);
            tbNewVoc.setText("");
            tbNewVoc.setText(autoName);
            tbNewVocVersion.setText("");
            tbNewVocVersion.setText(autoName);
            showStatus();
        }
        else
        {
            tbNewVocVersion.setDisabled(true);
            tbNewVoc.setDisabled(true);
            showStatus();
        }
    }

    public void onSelected(String id, Object data)
    {
        if (data != null && !cbNewVoc.isChecked())
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

    private void showStatus()
    {
        String s = "";

        if (bytes == null)
        {
            s += "Bitte wählen Sie eine Datei aus.";
        }

        if (selectedCodeSystem == null)
        {
            s += "\nBitte wählen Sie ein Codesystem aus.";
        }

        ((Label) getFellow("labelImportStatus")).setValue(s);

        ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
    }

    public void startImport()
    {
        ((Label) getFellow("labelImportStatus")).setValue("");
        csVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
        if ((csVersion != null) && (csVersion.length() > 0))
        {
            csvOnly = true;
        }
        else
        {
            csvOnly = false;
        }

        if ((selectedCodeSystem != null) && (selectedCodeSystem.getAutoRelease()) && csvOnly)
        {
            //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = WebServiceHelper.getInstance().getSearchPubService();
            de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
            request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
            logger.info("CS_SVS_Async session_id" + session_id);
            logger.info("CS_SVS_Async session_id" + session.getAttribute("session_id").toString());
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
        if (cbNewVoc.isChecked() && (tbNewVoc.getText().equals("") || tbNewVocVersion.getText().equals("")))
        {
            Messagebox.show("Bitte geben Sie Code System Bezeichnung und Code System Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }
        else
        {

            String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
            if (!cbNewVoc.isChecked() && vokVersion.equals(""))
            {
                Messagebox.show("Bitte geben Sie eine Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
            }
            else
            {

                ((Button) getFellow("buttonImport")).setDisabled(true);
                ((Button) getFellow("buttonCancel")).setVisible(true);

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

                //String session_id = SessionHelper.getValue("session_id").toString();
                String newVocVersion = ((Textbox) getFellow("tbNewVocVersion")).getText();

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

                if (cbNewVoc.isChecked())
                {
                    request.getCodeSystem().setName(((Textbox) getFellow("tbNewVoc")).getText());
                    request.setImportId(importId);
                    csvOnly = false;
                }
                else
                {
                    request.setImportId(importId);
                    request.getCodeSystem().setId(selectedCodeSystem.getId());
                    request.getCodeSystem().setName(selectedCodeSystem.getName());
                    request.getCodeSystem().setAutoRelease(selectedCodeSystem.getAutoRelease());
                    csvOnly = true;
                }

                CodeSystemVersion csv = new CodeSystemVersion();
                if (cbNewVoc.isChecked())
                {
                    csv.setName(newVocVersion);
                }
                else
                {
                    csv.setName(vokVersion);
                }

                request.getCodeSystem().getCodeSystemVersions().add(csv);

                // Claml-Datei
                request.setImportInfos(new ImportType());
                request.getImportInfos().setFormatId(235l); // SVS_ID
                request.getImportInfos().setFilecontent(bytes);
                if (csvOnly)
                {
                    request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole());
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
    }

    public void afterCompose()
    {
        cbNewVoc = (Checkbox) getFellow("cbNewVoc");
        tbNewVoc = (Textbox) getFellow("tbNewVoc");
        tbNewVocVersion = (Textbox) getFellow("tbNewVocVersion");
        textboxDateiname = (Textbox) getFellow("textboxDateiname");
        session_id = SessionHelper.getValue("session_id").toString();
        logger.info("afterComposeImportCS_SVS_Async session_id" + session_id);
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

    //used for sorting LevelType Lists manually
    public void sort()
    {

        long listId = Long.valueOf(((Textbox) getFellow("tbSortListId")).getText());

        //get List of concepts or whatever
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
        String s = "";
        String session_id = "";
        try
        {
            session_id = SessionHelper.getValue("session_id").toString();
            String hql = "select distinct csev from CodeSystemEntityVersion csev";
            hql += " join fetch csev.codeSystemMetadataValues csmv";
            hql += " join fetch csmv.metadataParameter mp";
            hql += " join fetch csev.codeSystemEntity cse";
            hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
            hql += " join fetch csvem.codeSystemVersion csv";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("csv.", "versionId", listId);

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(q);

            List<CodeSystemEntityVersion> csevList = q.list();

            HashMap<Integer, CodeSystemEntityVersion> workList = new HashMap<Integer, CodeSystemEntityVersion>();
            ArrayList<Integer> sortList = new ArrayList<Integer>();

            Iterator<CodeSystemEntityVersion> iter = csevList.iterator();
            while (iter.hasNext())
            {
                boolean found = false;
                CodeSystemEntityVersion csev = (CodeSystemEntityVersion) iter.next();

                //Get the Level
                int level = 0;
                Iterator<CodeSystemMetadataValue> iter1 = csev.getCodeSystemMetadataValues().iterator();
                while (iter1.hasNext())
                {
                    CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) iter1.next();
                    if (csmv.getMetadataParameter().getParamName().equals("Level"))
                    {

                        String cleaned = csmv.getParameterValue();

                        if (cleaned.contains(" "))
                        {
                            cleaned = cleaned.replace(" ", "");
                        }

                        level = Integer.valueOf(cleaned);
                    }
                }

                if (level == 0)
                {

                    CodeSystemVersionEntityMembershipId csvemId = null;
                    Set<de.fhdo.db.hibernate.CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                    Iterator<de.fhdo.db.hibernate.CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                    while (it.hasNext())
                    {

                        de.fhdo.db.hibernate.CodeSystemVersionEntityMembership member = (de.fhdo.db.hibernate.CodeSystemVersionEntityMembership) it.next();
                        if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                                && member.getCodeSystemVersion().getVersionId().equals(listId))
                        {

                            csvemId = member.getId();
                        }
                    }

                    de.fhdo.db.hibernate.CodeSystemVersionEntityMembership c = (de.fhdo.db.hibernate.CodeSystemVersionEntityMembership) hb_session.get(de.fhdo.db.hibernate.CodeSystemVersionEntityMembership.class, csvemId);
                    c.setIsMainClass(Boolean.TRUE);
                    hb_session.update(c);

                    workList.put(level, csev);
                    sortList.add(level);
                }
                else
                {

                    int size = sortList.size();
                    int count = 0;
                    while (!found)
                    {

                        if ((sortList.get(size - (1 + count)) - level) == -1)
                        {

                            //Setting MemberShip isMainClass false
                            CodeSystemVersionEntityMembershipId csvemId = null;
                            Set<de.fhdo.db.hibernate.CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                            Iterator<de.fhdo.db.hibernate.CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                            while (it.hasNext())
                            {

                                de.fhdo.db.hibernate.CodeSystemVersionEntityMembership member = (de.fhdo.db.hibernate.CodeSystemVersionEntityMembership) it.next();
                                if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                                        && member.getCodeSystemVersion().getVersionId().equals(listId))
                                {

                                    csvemId = member.getId();
                                }
                            }

                            de.fhdo.db.hibernate.CodeSystemVersionEntityMembership c = (de.fhdo.db.hibernate.CodeSystemVersionEntityMembership) hb_session.get(de.fhdo.db.hibernate.CodeSystemVersionEntityMembership.class, csvemId);
                            c.setIsMainClass(Boolean.FALSE);
                            hb_session.update(c);

                            found = true;
                            sortList.add(level);
                            workList.put(level, csev);
                            types.termserver.fhdo.de.CodeSystemEntityVersion csev1 = new types.termserver.fhdo.de.CodeSystemEntityVersion();
                            types.termserver.fhdo.de.CodeSystemEntityVersion csev2 = new types.termserver.fhdo.de.CodeSystemEntityVersion();
                            int assoKind = 2;
                            int assoType = 4;
                            CodeSystemEntityVersion csevPrev = workList.get(sortList.get(size - (1 + count)));

                            csev1.setVersionId(csevPrev.getVersionId());
                            csev2.setVersionId(csev.getVersionId());

                            ConceptAssociations port_accociation = WebServiceUrlHelper.getInstance().getConceptAssociationServicePort();
                            CreateConceptAssociationRequestType parameterAssociation = new CreateConceptAssociationRequestType();
                            CreateConceptAssociationResponse.Return responseAssociation = null;
                            CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

                            if (csev1 != null && csev2 != null)
                            {
                                cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
                                cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
                                cseva.setAssociationKind(assoKind); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                                cseva.setLeftId(csev1.getVersionId()); // immer linkes Element also csev1
                                cseva.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                                cseva.getAssociationType().setCodeSystemEntityVersionId((long) assoType);

                                // Login
                                de.fhdo.terminologie.ws.conceptassociation.LoginType loginConceptassociation = new de.fhdo.terminologie.ws.conceptassociation.LoginType();
                                loginConceptassociation.setSessionID(session_id);
                                parameterAssociation.setLogin(loginConceptassociation);

                                // Association
                                parameterAssociation.setCodeSystemEntityVersionAssociation(cseva);

                                // Call WS and prevent loops in SOAP Message
                                csev1.setCodeSystemEntity(null);
                                csev2.setCodeSystemEntity(null);
                                responseAssociation = port_accociation.createConceptAssociation(parameterAssociation);

                                if (responseAssociation != null && responseAssociation.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.conceptassociation.Status.OK)
                                {
                                    logger.error("ListSort: Error CreateConceptAssociation failure...");
                                }
                            }
                        }
                        else
                        {
                            count++;
                            found = false;
                        }
                    }
                }
            }

            hb_session.getTransaction().commit();
            s = "Success";
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            s = "ERROR";
            logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
        }
        finally
        {
            hb_session.close();
        }
        ((Label) getFellow("labelImportStatus")).setValue(s);
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

            //Proposal mit Proposalobjects anlegen
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

        final Progressmeter progress = (Progressmeter) getFellow("progress");
        progress.setVisible(false);

        ((Button) getFellow("buttonImport")).setDisabled(false);
        ((Button) getFellow("buttonCancel")).setVisible(false);
    }

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
}
