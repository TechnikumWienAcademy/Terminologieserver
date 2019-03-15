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

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.DomainHelper;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.helper.CollaborationuserHelper;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.East;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class Desktop extends Window implements IGenericListActions, AfterCompose, IUpdateModal
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private Proposal selectedProposal;
    GenericList genericList;
    GenericList genList;
    West westId;
    East eastId;

    public Desktop()
    {
    }

    public void afterCompose()
    {
        initList();

        westId = (West) getFellow("westId");
        eastId = (East) getFellow("eastId");

        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))
        {
            initMyTermList();
            westId.setSize("80%");
            eastId.setSize("20%");
            eastId.setVisible(true);
        }
        else
        {
            westId.setSize("100%");
            eastId.setSize("0%");
            eastId.setVisible(false);
        }
    }

    private void initList()
    {
        logger.debug("Desktop(): initList()");

        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Terminologie", 225, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Vorschlag", 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Typ", 140, "", true, DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_PROPOSAL_TYPES), true, true, false, false));
        header.add(new GenericListHeaderType("Status", 120, "", true, ProposalStatus.getInstance().getHeaderFilter(), true, true, false, false));
        header.add(new GenericListHeaderType("Datum", 110, "", true, "DateTime", true, true, false, false));
        header.add(new GenericListHeaderType("Disk.Ende", 100, "", true, "Date", true, true, false, false));
        header.add(new GenericListHeaderType("Rest", 80, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Autor", 140, "", true, "String", true, true, false, false));

        // Daten laden
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        try
        {
            List<Long> discussionGroups = CollaborationuserHelper.GetDiscussionGroupIDsForCurrentUser(hb_session);

            Long statusIdAusgeblendet = ProposalStatus.getInstance().getStatusIDFromString("ausgeblendet");

            // PRIVILEGIEN
            if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
            {
                String hql;
                hql = "select distinct p from Proposal p";

                hql += " where p.status != " + statusIdAusgeblendet;

                // Sortierung
                hql += " order by p.created desc";

                if (logger.isDebugEnabled())
                {
                    logger.debug("HQL: " + hql);
                }

                List<Proposal> proposalList = hb_session.createQuery(hql).list();

                for (int i = 0; i < proposalList.size(); ++i)
                {
                    Proposal proposal = proposalList.get(i);
                    GenericListRowType row = createRow(proposal);
                    dataList.add(row);
                }

            }
            else
            {        //Jedesmal wenn jemand einen Vorschlag macht wird geprüft ob der Vorschlagende der TermVerwalter ist => wenn nicht werden
                //dem Inhaltsverwalter automatisch Privilegien zugesprochen => Auch bei der Zuweisung von Terminologien zu Inhaltsverwaltern im
                //Admin wird das nachträglich gecheckt!
                String hql;
                hql = "select distinct p from Proposal p join fetch p.collaborationuser u "
                        + " left join p.privileges priv"
                        // - alle Vorschläge anzeigen, die Privilegien mit User-ID haben
                        + " where (priv.collaborationuser.id=" + SessionHelper.getCollaborationUserID();

                // - alle Vorschläge anzeigen, die Privilegien mit Discussion-ID haben
                if (discussionGroups != null && discussionGroups.size() > 0)
                {
                    hql += " or priv.discussiongroup.id in (" + CollaborationuserHelper.ConvertDiscussionGroupListToCommaString(discussionGroups) + ")";
                }

                // - alle eigenen Vorschläge dürfen in Liste angezeigt werden (aber nicht Detail-Ansicht)
                hql += " or p.collaborationuser.id=" + SessionHelper.getCollaborationUserID();

                hql += ")";

                // Sortierung
                hql += " order by p.created desc";

                if (logger.isDebugEnabled())
                {
                    logger.debug("HQL: " + hql);
                }

                List<Proposal> proposalList = hb_session.createQuery(hql).list();

                for (int i = 0; i < proposalList.size(); ++i)
                {
                    Proposal proposal = proposalList.get(i);
                    GenericListRowType row = createRow(proposal);
                    dataList.add(row);
                }
            }
            //hb_session.getTransaction().commit();
        }
        catch (Exception e)
        {
            //hb_session.getTransaction().rollback();
            LoggingOutput.outputException(e, this);
            //logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
        }
        finally
        {
            hb_session.close();
        }

        // Liste initialisieren
        Include inc = (Include) getFellow("incList");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;

        genericList.setListActions(this);
        //genericList.setButton_new(true);
        genericList.setButton_edit(true);
        //genericList.setButton_delete(true);
        genericList.setListHeader(header);
        genericList.setDataList(dataList);

        //Matthias: contextmenu adapted
        Menupopup contextMenu = new Menupopup();
        genericList.setContext(contextMenu);
        contextMenu.setParent(this);
        //TODO: use Labels instead of plain text
        Menuitem miOpenTerminology = new Menuitem("Terminologie öffnen");

        miOpenTerminology.addEventListener(Events.ON_CLICK, new EventListener()
        {
            public void onEvent(Event event) throws Exception
            {

                openTerminolgie();
            }
        });

        miOpenTerminology.setParent(contextMenu);

    }

    private Object queryForTerminology(long vocabularyId)
    {

        boolean runS = true;

        String contentType = selectedProposal.getContentType();

        if (contentType.equals("conceptVs") || contentType.equals("valueset"))
        {
            ListValueSetsRequestType para = new ListValueSetsRequestType();
            // login
            if (SessionHelper.isCollaborationActive())
            {
                // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
                para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
                para.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
            }
            else if (SessionHelper.isUserLoggedIn())
            {
                para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
                para.getLogin().setSessionID(SessionHelper.getSessionId());
            }
            ListValueSetsResponse.Return resp = WebServiceHelper.listValueSets(para);
            if (resp.getReturnInfos().getStatus() == Status.OK)
            {
                for (ValueSet vsL : resp.getValueSet())
                {
                    if (vocabularyId == vsL.getCurrentVersionId())
                    {
                        runS = false;
                        for (ValueSetVersion vsvL : vsL.getValueSetVersions())
                        {
                            if (vsvL.getVersionId() == vocabularyId)
                            {
                                vsvL.setValueSet(vsL);
                                return vsvL;
                            }
                        }
                    }
                }
            }

        }

        if (contentType.equals("vocabulary") || contentType.equals("concept"))
        {
            ListCodeSystemsInTaxonomyRequestType para = new ListCodeSystemsInTaxonomyRequestType();
            // login
            if (SessionHelper.isCollaborationActive())
            {
                // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
                para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
                para.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
            }
            else if (SessionHelper.isUserLoggedIn())
            {
                para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
                para.getLogin().setSessionID(SessionHelper.getSessionId());
            }

            ListCodeSystemsInTaxonomyResponse.Return resp = WebServiceHelper.listCodeSystems(para);

            if (resp.getReturnInfos().getStatus() == Status.OK)
            {
                for (DomainValue dv : resp.getDomainValue())
                {
                    for (CodeSystem cs : dv.getCodeSystems())
                    {
                        for (CodeSystemVersion csv : cs.getCodeSystemVersions())
                        {
                            if (csv.getVersionId() == vocabularyId)
                            {
                                csv.setCodeSystem(cs);
                                return csv;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void openTerminolgie()
    {
        Object source = queryForTerminology(selectedProposal.getVocabularyId());

        long versionId;

        String params = "contentConcepts=";

        // Parameter zum laden des Content angeben
        if (source instanceof CodeSystemVersion)
        {
            CodeSystemVersion selectedCSV = (CodeSystemVersion) source;
            versionId = selectedCSV.getVersionId();
            params += ContentConcepts.CONTENTMODE_CODESYSTEM;

        }
        else if (source instanceof ValueSetVersion)
        {
            ValueSetVersion selectedVSV = (ValueSetVersion) source;
            versionId = selectedVSV.getVersionId();

            params += ContentConcepts.CONTENTMODE_VALUESET;

        }
        else
        {
            return;
        }

        params += "&termId=" + versionId
                + "&hideSelection=true"
                + "&hideMenu=true"
                + "&hideStatusbar=true";

        Executions.getCurrent().sendRedirect("../../gui/main/main.zul?" + params, "New Window");

    }

    private GenericListRowType createRow(Proposal proposal)
    {
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[8];
        cells[0] = new GenericListCellType(proposal.getVocabularyName(), false, "");
        cells[1] = new GenericListCellType(proposal.getDescription(), false, "");
        cells[2] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_PROPOSAL_TYPES, proposal.getContentType()), false, "");
        cells[3] = new GenericListCellType(ProposalStatus.getInstance().getStatusStr(proposal.getStatus()), false, "");
        cells[4] = new GenericListCellType(proposal.getCreated(), false, "");
        cells[5] = new GenericListCellType(proposal.getValidTo(), false, "");
        cells[6] = new GenericListCellType(calculateRest(proposal.getValidTo()), false, "");
        cells[7] = new GenericListCellType(getListName(proposal.getCollaborationuser()), false, "");

        /*for(int i=0;i<pt.getProposalStatusChangeList().size();++i)
     {
     ProposalStatusChangeType psct = pt.getProposalStatusChangeList().get(i);
     if(psct.getProposalStatusTo() == pt.getStatus())
     {
     label.setToolTipText(psct.getReason());
     break;
     }
     }
     label.setText(StatusData.instance().GetStatusFromID(pt.getStatus()));
     werte[COLUMN_STATUS] = label;*/
        row.setData(proposal);
        row.setCells(cells);

        return row;
    }

    private String calculateRest(Date date)
    {
        String s = "";

        if (date != null)
        {
            long diff = DateTimeHelper.GetDateDiffInDays(DateTimeHelper.dateToXMLGregorianCalendar(date));
            if (diff == 0)
            {
                s = "letzter Tag";
            }
            else if (diff == 1)
            {
                s = "1 Tag";
            }
            else if (diff > 1)
            {
                s = diff + " Tage";
            }
            else
            {
                s = "-";
            }
        }

        return s;
    }

    public String getListName(Collaborationuser user)
    {
        String s = "";

        s = user.getFirstName();
        if (s != null && s.length() > 0)
        {
            s += " ";
        }
        s += user.getName();

        if (s == null || s.length() == 0)
        {
            s = user.getUsername();
        }

        // TODO Organisation hinzufügen
        return s;
    }

    public void onNewClicked(String id)
    {
    }

    public void onEditClicked(String id, Object data)
    {
        logger.debug("onEditClicked()");

        if (data != null && data instanceof Proposal)
        {
            openProposal((Proposal) data);

        }

    }

    public void onDeleted(String id, Object data)
    {
    }

    public void onSelected(String id, Object data)
    {
        selectedProposal = (Proposal) data;

    }

    private void openProposal(Proposal proposal)
    {
        try
        {
            logger.debug("Vorschlag öffnen mit ID: " + proposal.getId());

            Map map = new HashMap();
            map.put("proposal_id", proposal.getId());

            Window win = (Window) Executions.createComponents(
                    "/collaboration/desktop/proposalView.zul", null, map);

            ((ProposalView) win).setUpdateInterface(this);

            win.doModal();
        }
        catch (Exception ex)
        {
            logger.debug("Fehler beim Ã–ffnen der UserDetails: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    public void update(Object o, boolean edited)
    {
        if (o != null)
        {
            if (o instanceof Proposal)
            {
                // Zeile ändern oder hinzufügen
                Proposal p = (Proposal) o;
                GenericListRowType row = createRow(p);
                if (edited)
                {
                    genericList.updateEntry(row);
                }
                else
                {
                    genericList.addEntry(row);
                }
            }
        }

        initMyTermList();
    }

    private void initMyTermList()
    {
        logger.debug("Desktop(): initMyTermList()");

        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Bezeichnung", 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Typ", 150, "", true, "String", true, true, false, false));

        ArrayList<AssignedTerm> assignedTermList = AssignTermHelper.getUsersAssignedTerms();

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

        for (AssignedTerm at : assignedTermList)
        {

            if (at.getClassname().equals("CodeSystem"))
            {

                ReturnCodeSystemDetailsRequestType parameter = new ReturnCodeSystemDetailsRequestType();
                // Login
                if (SessionHelper.isUserLoggedIn())
                {
                    de.fhdo.terminologie.ws.search.LoginType loginAuthoring = new de.fhdo.terminologie.ws.search.LoginType();
                    loginAuthoring.setSessionID(SessionHelper.getSessionId());
                    parameter.setLogin(loginAuthoring);
                }
                else if (SessionHelper.isCollaborationActive())
                {

                    de.fhdo.terminologie.ws.search.LoginType loginAuthoring = new de.fhdo.terminologie.ws.search.LoginType();
                    loginAuthoring.setSessionID(CollaborationSession.getInstance().getSessionID());
                    parameter.setLogin(loginAuthoring);
                }

                CodeSystem csTemp = new CodeSystem();
                csTemp.setId(at.getClassId());
                parameter.setCodeSystem(csTemp);

                ReturnCodeSystemDetailsResponse.Return response = WebServiceHelper.returnCodeSystemDetails(parameter);

                if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                {
                    if (response.getCodeSystem() != null)
                    {
                        GenericListRowType row = createMyTermRow(response.getCodeSystem());
                        dataList.add(row);
                    }
                }
            }
            else
            {

                de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType request = new de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType();
                // Login
                if (SessionHelper.isUserLoggedIn())
                {
                    de.fhdo.terminologie.ws.search.LoginType loginAuthoring = new de.fhdo.terminologie.ws.search.LoginType();
                    loginAuthoring.setSessionID(SessionHelper.getSessionId());
                    request.setLogin(loginAuthoring);
                }

                ValueSet vs = new ValueSet();
                vs.setId(at.getClassId());
                request.setValueSet(vs);

                ReturnValueSetDetailsResponse.Return response = WebServiceHelper.returnValueSetDetails(request);

                if (response.getReturnInfos().getStatus() == Status.OK)
                {
                    if (response.getValueSet() != null)
                    {
                        GenericListRowType row = createMyTermRow(response.getValueSet());
                        dataList.add(row);
                    }
                }
            }
        }

        // Liste initialisieren
        Include inc = (Include) getFellow("incListMyTerm");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genList = (GenericList) winGenericList;

        genList.setListActions(this);
        genList.setButton_new(false);
        genList.setButton_edit(false);
        genList.setButton_delete(false);
        genList.setListHeader(header);
        genList.setDataList(dataList);
    }

    private GenericListRowType createMyTermRow(Object obj)
    {

        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[2];

        if (obj instanceof CodeSystem)
        {
            cells[0] = new GenericListCellType(((CodeSystem) obj).getName(), false, "");
            cells[1] = new GenericListCellType("CodeSystem", false, "");
        }

        if (obj instanceof ValueSet)
        {
            cells[0] = new GenericListCellType(((ValueSet) obj).getName(), false, "");
            cells[1] = new GenericListCellType("ValueSet", false, "");
        }

        row.setData(obj);
        row.setCells(cells);

        return row;
    }
}
