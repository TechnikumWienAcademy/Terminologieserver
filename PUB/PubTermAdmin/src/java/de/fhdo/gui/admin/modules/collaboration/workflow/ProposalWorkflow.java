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
package de.fhdo.gui.admin.modules.collaboration.workflow;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.PO_CHANGE_TYPE;
import de.fhdo.collaboration.db.PO_CLASSNAME;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.db.DBSysParam;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse;
import de.fhdo.terminologie.ws.administration.ExportParameterType;
import de.fhdo.terminologie.ws.administration.ExportType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.LoginType;
import de.fhdo.terminologie.ws.authoring.Status;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class ProposalWorkflow extends Window
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    // Singleton-Muster
    private static ProposalWorkflow instance;

    private static de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS = null;
    private static de.fhdo.terminologie.ws.searchPub.ValueSet targetVS = null;

    private static org.zkoss.zk.ui.Session session = null;
    private static Long collab_user_id;

    public static ProposalWorkflow getInstance()
    {
        ProposalWorkflow.session = null;

        if (instance == null)
        {
            instance = new ProposalWorkflow();
        }

        return instance;
    }

    // Konstruktor
    public ProposalWorkflow()
    {
    }

    /**
     * Fügt einen neuen Vorschlag hinzu und benachrichtigt alle verantwortlichen
     * Personen.
     *
     * @param proposal Vorschlag
     * @param obj Einzufügendes Objekt (z.B. CodeSystemConcept)
     * @return
     */
    public ReturnType addProposal(Proposal proposal, Object obj, Boolean isExisting)
    {
        return addProposal(proposal, obj, null, null, "", isExisting);
    }

    public ReturnType addProposal(Proposal proposal, Object obj, Object obj2, Long csId, Boolean isExisting)
    {
        return addProposal(proposal, obj, obj2, csId, "", isExisting);
    }

    public ReturnType addProposal(Proposal proposal, Object obj, Boolean isExisting, org.zkoss.zk.ui.Session session)
    {
        ProposalWorkflow.session = session;
        return addProposal(proposal, obj, null, null, "", isExisting);
    }

    /**
     * Fügt einen neuen Vorschlag hinzu und benachrichtigt alle verantwortlichen
     * Personen.
     *
     * @param proposal Vorschlag
     * @param obj Einzufügendes Objekt (z.B. CodeSystemConcept)
     * @param obj2 Einzufügendes Objekt 2 (z.B. CodeSystemConcept)
     * @return
     */
    public ReturnType addProposal(Proposal proposal, Object obj, Object obj2, Long csId, String searchCode, Boolean isExisting)
    {

        ReturnType returnInfos = new ReturnType();

        if (ProposalWorkflow.session == null)
        {
            ProposalWorkflow.collab_user_id = SessionHelper.getCollaborationUserID();
        }
        else
        {
            ProposalWorkflow.collab_user_id = Long.parseLong(session.getAttribute("collaboration_user_id").toString());
        }

        // TODO erst prüfen, ob Benutzer exisitert (Collaborationuser)
        // SessionHelper.getCollaborationUserID()
        List<Proposalobject> proposalObjectList = new java.util.LinkedList<Proposalobject>();

        boolean tsDataInserted = false;

        // 1. Objekte in Terminologieserver erstellen
        try
        {

            if (proposal.getContentType().equals("vocabulary"))
            {
                CodeSystem cs = (CodeSystem) obj;

                tsDataInserted = true;   // Erfolg
                if (!AssignTermHelper.assignedTermExists(cs, ProposalWorkflow.collab_user_id))
                {
                    AssignTermHelper.assignTermToUser(cs, ProposalWorkflow.collab_user_id);
                }

                proposal.setVocabularyId(cs.getCurrentVersionId());
                proposal.setVocabularyIdTwo(cs.getId());
                proposal.setDescription("Dieses Code System wurde über den Import eingefügt.");

                // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
                Proposalobject po = new Proposalobject();
                if (cs.getId() != null)
                {
                    po.setClassId(cs.getId());
                    po.setClassname("CodeSystem");
                    po.setName(cs.getName());
                    po.setChangeType(PO_CHANGE_TYPE.NEW.id());
                    proposalObjectList.add(po);
                }

                po = new Proposalobject();
                po.setClassId(cs.getCurrentVersionId());
                po.setClassname("CodeSystemVersion");
                po.setName(cs.getCodeSystemVersions().get(0).getName());
                po.setChangeType(PO_CHANGE_TYPE.NEW.id());
                proposalObjectList.add(po);

            }
            else if (proposal.getContentType().equals("valueset"))
            {

                ValueSet vs = (ValueSet) obj;
                tsDataInserted = true;   // Erfolg
                if (!AssignTermHelper.assignedTermExists(vs))
                {
                    AssignTermHelper.assignTermToUser(vs);
                }

                proposal.setVocabularyId(vs.getCurrentVersionId());
                proposal.setVocabularyIdTwo(vs.getId());
                proposal.setDescription("Dieses ValueSet wurde über den Import eingefügt.");

                // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
                Proposalobject po = new Proposalobject();
                po.setClassId(vs.getId());
                po.setClassname("ValueSet");
                po.setName(vs.getName());
                po.setChangeType(PO_CHANGE_TYPE.NEW.id());
                proposalObjectList.add(po);

                po = new Proposalobject();
                po.setClassId(vs.getCurrentVersionId());
                po.setClassname("ValueSetVersion");
                po.setName(vs.getValueSetVersions().get(0).getName());
                po.setChangeType(PO_CHANGE_TYPE.NEW.id());
                proposalObjectList.add(po);
            }
            else if (proposal.getContentType().equals("concept") || proposal.getContentType().equals("subconcept"))
            {
                CodeSystemConcept csc = (CodeSystemConcept) obj;
                tsDataInserted = true;   // Erfolg

                proposal.setDescription("Dieses Konzept wurde von einem Inhaltsverwalter über den Import eingefügt.");

                Proposalobject po = new Proposalobject();
                po.setClassId(csc.getCodeSystemEntityVersionId());
                po.setClassname("CodeSystemConcept");
                po.setName(csc.getCode() + " (" + csc.getTerm() + ")");
                po.setChangeType(PO_CHANGE_TYPE.NEW.id()); // 1 = hinzugefügt
                proposalObjectList.add(po);

            }
        }
        catch (Exception ex)
        {
            LoggingOutput.outputException(ex, this);
        }

        logger.debug("tsDataInserted: " + tsDataInserted);

        if (tsDataInserted)
        {
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try
            {
                // 2. Vorschlag in DB hinzufügen
                proposal.setStatus(1); // TODO
                proposal.setStatusDate(new Date());
                proposal.setCreated(new Date());
                proposal.setCollaborationuser(new Collaborationuser());
                proposal.getCollaborationuser().setId(collab_user_id);

                hb_session.save(proposal);

                // 3. Objekte in DB hinzufügen
                for (Proposalobject po : proposalObjectList)
                {
                    po.setProposal(proposal);
                    hb_session.save(po);
                }

                //Add creator Default privilege
                Privilege priv = new Privilege();
                priv.setCollaborationuser(new Collaborationuser());
                priv.getCollaborationuser().setId(ProposalWorkflow.collab_user_id);

                Collaborationuser u = (Collaborationuser) hb_session.get(Collaborationuser.class, ProposalWorkflow.collab_user_id);

                priv.setSendMail(u.getSendMail());

                if (u.getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN)
                        || u.getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER))
                {
                    priv.setMayChangeStatus(true);
                    priv.setMayManageObjects(true);
                }
                else
                {
                    priv.setMayChangeStatus(false);
                    priv.setMayManageObjects(false);
                }
                priv.setFromDate(new Date());
                priv.setProposal(new Proposal());
                priv.getProposal().setId(proposal.getId());
                priv.setDiscussiongroup(null);

                hb_session.save(priv);

                returnInfos.setSuccess(true);
                returnInfos.setMessage("Vorschlag erfolgreich eingefügt.");

                if (u.getSendMail())
                {
                    //String[] adr = new String[1];
                    //adr[0] = u.getEmail();
                    Mail.sendMailAUT(u, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalText(
                            proposal.getVocabularyName(),
                            proposal.getContentType(),
                            proposal.getDescription()));
                }
                Long id = 0l;
                String classname = "";
                if (proposal.getContentType().equals("vocabulary"))
                {

                    id = proposal.getVocabularyIdTwo();
                    classname = "CodeSystem";

                }
                else if (proposal.getContentType().equals("valueset"))
                {

                    id = proposal.getVocabularyIdTwo();
                    classname = "ValueSet";

                }
                else if (proposal.getContentType().equals("concept") || proposal.getContentType().equals("subconcept"))
                {

                    id = proposal.getVocabularyIdTwo();
                    classname = "CodeSystem";
                }
                else if (proposal.getContentType().equals("conceptVs"))
                {

                    id = proposal.getVocabularyIdTwo();
                    classname = "ValueSet";
                }

                String termHead = "from Collaborationuser cu join fetch cu.assignedTerms at where at.classId=:classId and at.classname=:classname";
                Query qTermHead = hb_session.createQuery(termHead);
                qTermHead.setParameter("classId", id);
                qTermHead.setParameter("classname", classname);
                List<Collaborationuser> userList = qTermHead.list();

                if (userList.size() == 1)
                {

                    if (userList.get(0).getId().equals(u.getId()))
                    {
                        //SV == Antragsteller => Do nothing
                    }
                    else
                    {

                        //Erstelle privilegien für den SV
                        Privilege privSv = new Privilege();
                        privSv.setCollaborationuser(new Collaborationuser());
                        privSv.getCollaborationuser().setId(userList.get(0).getId());

                        privSv.setSendMail(userList.get(0).getSendMail());

                        privSv.setMayChangeStatus(true);
                        privSv.setMayManageObjects(true);

                        privSv.setFromDate(new Date());
                        privSv.setProposal(new Proposal());
                        privSv.getProposal().setId(proposal.getId());
                        privSv.setDiscussiongroup(null);

                        hb_session.save(privSv);

                        if (userList.get(0).getSendMail())
                        {

                            String[] adr = new String[1];
                            adr[0] = userList.get(0).getEmail();
                            Mail.sendMailAUT_MultiUser(adr, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalSelbstVerwText(
                                    proposal.getVocabularyName(),
                                    proposal.getContentType(),
                                    proposal.getDescription()));
                        }
                    }
                }
                hb_session.getTransaction().commit();
            }
            catch (Exception ex)
            {
                //LoggingOutput.outputException(ex, this);
                returnInfos.setSuccess(false);
                returnInfos.setMessage("Fehler beim Einfügen eines Vorschlags: " + ex.getLocalizedMessage());
                
                hb_session.getTransaction().rollback();
            }
            finally
            {
                hb_session.close();
            }
        }

        return returnInfos;
    }

    public ReturnType changeProposalStatus(Proposal proposal, long statusTo, String reason, Date discDateFrom, Date discDateTo, org.zkoss.zk.ui.Session session, boolean autorelease)
    {
        ProposalWorkflow.session = session;
        return changeProposalStatus(proposal, statusTo, reason, discDateFrom, discDateTo, autorelease);
    }

    public ReturnType changeProposalStatus(Proposal proposal, long statusTo, String reason, Date discDateFrom, Date discDateTo, boolean autorelease)
    {
        ReturnType returnInfos = new ReturnType();

        if (ProposalWorkflow.session == null)
        {
            //ProposalWorkflow.collab_user_id = SessionHelper.getCollaborationUserID();
            session = Sessions.getCurrent();
        }
        else
        {
            ProposalWorkflow.collab_user_id = Long.parseLong(session.getAttribute("collaboration_user_id").toString());
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("changeProposalStatus from " + proposal.getStatus() + " to " + statusTo);
        }

        long statusFrom = proposal.getStatus();

        // 1. prüfen, ob Statusänderung möglich ist
        Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusTo);
        if (rel != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Statusänderung möglich");
            }

            // 2. Rechte prüfen, ob angemeldeter Benutzer die Statusänderung durchführen darf
            if (ProposalStatus.getInstance().isUserAllowed(rel, collab_user_id) || autorelease)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Rechte vorhanden!");
                }

                // 3. Statusänderung durchführen
                Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try
                {
                    // Proposal ändern (Status + StatusDate)
                    logger.debug("Ändere Status vom Vorschlag mit Proposal-ID: " + proposal.getId());
                    Proposal proposal_db = (Proposal) hb_session.get(Proposal.class, proposal.getId());

                    proposal_db.setStatus((int) statusTo);
                    proposal_db.setStatusDate(new Date());

                    proposal_db.setValidFrom(discDateFrom);
                    proposal_db.setValidTo(discDateTo);

                    if (discDateFrom != null)
                    {
                        logger.debug("Datum von: " + discDateFrom);
                    }
                    else
                    {
                        logger.debug("Datum von: null");
                    }

                    hb_session.update(proposal_db);

                    // Statusänderung hinzufügen
                    Proposalstatuschange psc = new Proposalstatuschange();
                    psc.setProposal(proposal_db);
                    //psc.getProposal().setId(proposal.getId());
                    psc.setChangeTimestamp(new Date());
                    psc.setCollaborationuser(new Collaborationuser());
                    psc.getCollaborationuser().setId(collab_user_id);
                    psc.setProposalStatusFrom((int) statusFrom);
                    psc.setProposalStatusTo((int) statusTo);
                    psc.setReason(reason);
                    hb_session.save(psc);

                    hb_session.getTransaction().commit();

                    // 4. Status in Terminologieserver ändern
                    ReturnType transfer_success = new ReturnType();
                    transfer_success.setSuccess(false);
                    //Sortieren der proposalobjects um CS/VS nach vorne zu reihen -> wichtig bei Transfer auf public
                    List<Proposalobject> terminologies = new ArrayList<Proposalobject>();
                    List<Proposalobject> codesystems = new ArrayList<Proposalobject>();
                    List<Proposalobject> valuesets = new ArrayList<Proposalobject>();
                    List<Proposalobject> codesystemVersions = new ArrayList<Proposalobject>();
                    List<Proposalobject> valuesetVersions = new ArrayList<Proposalobject>();
                    List<Proposalobject> codesystemConcepts = new ArrayList<Proposalobject>();
                    List<Proposalobject> valuesetConcepts = new ArrayList<Proposalobject>();
                    List<Proposalobject> other = new ArrayList<Proposalobject>();
                    for (Proposalobject po : proposal_db.getProposalobjects())
                    {
                        if (po.getClassname().equals("CodeSystem"))
                        {
                            codesystems.add(po);
                        }
                        else if (po.getClassname().equals("ValueSet"))
                        {
                            valuesets.add(po);
                        }
                        else if (po.getClassname().equals("CodeSystemVersion"))
                        {
                            codesystemVersions.add(po);
                        }
                        else if (po.getClassname().equals("ValueSetVersion"))
                        {
                            valuesetVersions.add(po);
                        }
                        else if (po.getClassname().equals("CodeSystemConcept"))
                        {
                            codesystemConcepts.add(po);
                        }
                        else if (po.getClassname().equals("ConceptValueSetMembership"))
                        {
                            valuesetConcepts.add(po);
                        }
                        else
                        {
                            other.add(po);
                        }
                    }

                    terminologies.addAll(terminologies.size(), codesystems);
                    terminologies.addAll(terminologies.size(), valuesets);
                    terminologies.addAll(terminologies.size(), codesystemVersions);
                    terminologies.addAll(terminologies.size(), valuesetVersions);
                    terminologies.addAll(terminologies.size(), codesystemConcepts);
                    terminologies.addAll(terminologies.size(), valuesetConcepts);
                    terminologies.addAll(terminologies.size(), other);

                    boolean statusChangeSuccess = false;

                    for (Proposalobject po : terminologies)
                    {
                        statusChangeSuccess = changeTerminologyServerStatus(rel.getStatusByStatusIdTo(), po, returnInfos);
                        if (!statusChangeSuccess)
                        {
                            break;
                        }
                    }
                }
                catch (Exception ex)
                {
                    LoggingOutput.outputException(ex, this);
                    hb_session.getTransaction().rollback();

                    returnInfos.setSuccess(false);
                    returnInfos.setMessage("Fehler beim Ändern des Status: " + ex.getLocalizedMessage());
                    return returnInfos;
                }
                finally
                {
                    // Session schließen
                    hb_session.close();
                }

                returnInfos.setSuccess(true);
                returnInfos.setMessage("Status erfolgreich geändert zu: " + rel.getStatusByStatusIdTo().getStatus() + "\n\nAndere Benutzer wurden über die Statusänderung per Email informiert.");
            }
            else
            {
                // Statusänderung nicht möglich, da keine Rechte
                if (logger.isDebugEnabled())
                {
                    logger.debug("keine Rechte vorhanden!");
                }

                returnInfos.setMessage("Sie besitzen nicht die nötigen Rechte für diese Statusänderung!");
                return returnInfos;
            }

        }
        else
        {
            // Statusänderung nicht möglich, da nicht in DB vorgesehen
            returnInfos.setMessage("Die angegebene Statusänderung ist nicht möglich!");
            return returnInfos;
        }

        return returnInfos;
    }

    /**
     * Ändert den Status des Objektes im Terminologieserver
     *
     * @param statusTo
     * @param obj
     * @param returnInfos
     */
    private static boolean changeTerminologyServerStatus(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject po, ReturnType returnInfos)
    {
        logger.debug("changeTerminologyServerStatus(), classId: " + po.getClassId() + ", classname: " + po.getClassname());
        PO_CLASSNAME classname = PO_CLASSNAME.get(po.getClassname());
        PO_CHANGE_TYPE changeType = PO_CHANGE_TYPE.get(po.getChangeType());

        int newStatus = 0;
        if (statusTo.getIsPublic())
        {
            newStatus = 1; // Schwarz
        }
        else if (statusTo.getIsDeleted())
        {
            newStatus = 2; // Durchgestrichen
        }
        else
        {
            newStatus = 0; // Grau
        }

        logger.debug("Neuer Status: " + newStatus);
        if (changeType != PO_CHANGE_TYPE.CHANGED)
        {
            if (classname == PO_CLASSNAME.CODESYSTEM)
            {
                // nichts, da es keinen Status für CodeSystem gibt (nur Version)
                return true;
            }
            else if (classname == PO_CLASSNAME.CODESYSTEM_VERSION)
            {
                // Status der Codesystem-Version ändern
                UpdateCodeSystemVersionStatusRequestType request = new UpdateCodeSystemVersionStatusRequestType();
                request.setLogin(new LoginType());
                //request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                // Codesystem angeben
                request.setCodeSystem(new CodeSystem());
                CodeSystemVersion csv = new CodeSystemVersion();
                csv.setVersionId(po.getClassId());
                csv.setStatus(newStatus);
                request.getCodeSystem().getCodeSystemVersions().add(csv);

                // Webservice aufrufen
                UpdateCodeSystemVersionStatusResponse.Return ret = updateCodeSystemVersionStatus(request);

                logger.debug("Ergebnis updateCodeSystemVersionStatus: " + ret.getReturnInfos().getMessage());
                if (ret.getReturnInfos().getStatus() == Status.OK)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (classname == PO_CLASSNAME.CODESYSTEM_CONCEPT)
            {
                // Status des Konzepts ändern
                UpdateConceptStatusRequestType request = new UpdateConceptStatusRequestType();
                request.setCodeSystemVersionId(po.getProposal().getVocabularyId());
                request.setLogin(new LoginType());
//          request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                // Codesystem angeben
                request.setCodeSystemEntity(new CodeSystemEntity());
                CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                csev.setVersionId(po.getClassId());
                csev.setStatus(newStatus);
                request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

                // Webservice aufrufen
                UpdateConceptStatusResponse.Return ret = updateConceptStatus(request);

                logger.debug("Ergebnis updateConceptStatus: " + ret.getReturnInfos().getMessage());
                if ((ret.getReturnInfos().getStatus() == Status.OK))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (classname == PO_CLASSNAME.RELATION)
            {
                // TODO
            }
            else if (classname == PO_CLASSNAME.VALUESET)
            {
                //Wäre gut wenn wir das analog zum CS halten und den status hier auch weglassen...

                /*
         // Status des Konzepts ändern
         UpdateValueSetStatusRequestType request = new UpdateValueSetStatusRequestType();

         request.setLogin(new LoginType());
         request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());

         // Codesystem angeben
         ValueSet vs = new ValueSet();
         request.setValueSet(vs);
         vs.setId(po.getClassId());
         vs.setStatus(newStatus);

         // Webservice aufrufen
         UpdateValueSetStatusResponse.Return ret = updateValueSetStatus(request);

         logger.debug("Ergebnis updateValueSetStatus: " + ret.getReturnInfos().getMessage());
         if (ret.getReturnInfos().getStatus() == Status.OK)
         {
         }*/
            }
            else if (classname == PO_CLASSNAME.VALUESET_VERSION)
            {
                // Status der Codesystem-Version ändern
                UpdateValueSetStatusRequestType request = new UpdateValueSetStatusRequestType();
                request.setLogin(new LoginType());
                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                // Codesystem angeben
                request.setValueSet(new ValueSet());
                ValueSetVersion vsv = new ValueSetVersion();
                vsv.setVersionId(po.getClassId());
                vsv.setStatus(newStatus);
                request.getValueSet().getValueSetVersions().add(vsv);

                // Webservice aufrufen
                UpdateValueSetStatusResponse.Return ret = updateValueSetStatus(request);

                logger.debug("Ergebnis updateValueSetVersionStatus: " + ret.getReturnInfos().getMessage());
                if (ret.getReturnInfos().getStatus() == Status.OK)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (classname == PO_CLASSNAME.CONCEPT_VALUESET_MEMBERSHIP)
            {
                // Status der Codesystem-Version ändern
                UpdateConceptValueSetMembershipStatusRequestType request = new UpdateConceptValueSetMembershipStatusRequestType();
                request.setLogin(new LoginType());
//          request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                // Codesystem angeben
                request.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                request.getCodeSystemEntityVersion().getConceptValueSetMemberships().clear();

                ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
                cvsm.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                cvsm.setValueSetVersion(new ValueSetVersion());

                cvsm.getCodeSystemEntityVersion().setVersionId(po.getClassId());
                cvsm.getValueSetVersion().setVersionId(po.getClassId2());
                cvsm.setStatus(newStatus);
                request.getCodeSystemEntityVersion().getConceptValueSetMemberships().add(cvsm);

                // Webservice aufrufen
                UpdateConceptValueSetMembershipStatusResponse.Return ret = updateConceptValueSetMembershipStatus(request);

                logger.debug("Ergebnis updateValueSetVersionStatus: " + ret.getReturnInfos().getMessage());
                if (ret.getReturnInfos().getStatus() == Status.OK)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (classname == PO_CLASSNAME.ASSOCIATION)
            {
                //nothing to do; association needs no status update
                return true;
            }
        }
        return false;
    }

    /**
     * Ändert den Status des Objektes im Terminologieserver
     *
     * @param statusTo
     * @param obj
     * @param returnInfos
     */
    private ReturnType transferTerminologyToPublicServer(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject po, ReturnType returnInfos)
    {
        logger.debug("transferTerminologyToPublicServer(), classId: " + po.getClassId() + ", classname: " + po.getClassname());

        ReturnType ret = new ReturnType();
        ret.setSuccess(false);
        String msg = "";

        //Search_Service service_search = new Search_Service();
        de.fhdo.terminologie.ws.search.Search port_search = WebServiceUrlHelper.getInstance().getSearchServicePort();

        //ID des CodeSystems auf der Public Plattform in welches eine neue Version/Konzept eingefügt werden soll
        targetCS = null;
        targetVS = null;

        if (po.getClassname().equals("CodeSystemVersion"))
        {
            //CodeSystem aus Collab lesen
            ListCodeSystemsRequestType request_search = new ListCodeSystemsRequestType();
            request_search.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
            request_search.getLogin().setSessionID(session.getAttribute("session_id").toString());
            request_search.setCodeSystem(new CodeSystem());
            request_search.getCodeSystem().setId(po.getProposal().getVocabularyIdTwo());
            ListCodeSystemsResponse.Return resp = port_search.listCodeSystems(request_search);

            if ((resp.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                    && (resp.getCodeSystem() != null)
                    && (resp.getCodeSystem().size() == 1))
            {
                CodeSystemVersion csv_search = null;
                for (CodeSystemVersion csv_temp : resp.getCodeSystem().get(0).getCodeSystemVersions())
                {
                    if (csv_temp.getVersionId().equals((po.getProposal().getVocabularyId())))
                    {
                        csv_search = csv_temp;
                    }
                }

                if (csv_search != null)
                {
                    ExportCodeSystemContentRequestType req_export_cs = new ExportCodeSystemContentRequestType();
                    req_export_cs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
                    req_export_cs.getLogin().setSessionID(session.getAttribute("session_id").toString());

                    req_export_cs.setCodeSystem(new CodeSystem());
                    req_export_cs.getCodeSystem().setId(resp.getCodeSystem().get(0).getId());
                    CodeSystemVersion csv = new CodeSystemVersion();
                    csv.setVersionId(csv_search.getVersionId());
                    req_export_cs.getCodeSystem().getCodeSystemVersions().add(csv);

                    long format = 193L;
                    // Export Type
                    ExportType eType = new ExportType();
                    if (resp.getCodeSystem().get(0).getName().contains("LOINC"))
                    {
                        format = 194L; // TODO 193 = ClaML, 194 = CSV, 195 SVS
                    }

                    logger.info(po.getName());
                    logger.info("Format: " + format);
                    eType.setFormatId(format);
                    eType.setUpdateCheck(false);
                    req_export_cs.setExportInfos(eType);

                    // Optional: ExportParameter
                    ExportParameterType eParameterType = new ExportParameterType();
                    eParameterType.setAssociationInfos("");
                    eParameterType.setCodeSystemInfos(true);
                    eParameterType.setTranslations(true);
                    req_export_cs.setExportParameter(eParameterType);

                    logger.debug("Export-Service-Aufruf...");

                    ExportCodeSystemContentResponse.Return response = null;
                    if (resp.getCodeSystem().get(0).getName().contains("LOINC"))
                    {
                        response = new ExportCodeSystemContentResponse.Return();
                        String path = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("LoincCsvPath", null, null);
                        File file = new File(path);
                        FileInputStream fis = null;
                        try
                        {
                            fis = new FileInputStream(file);
                            byte bytesPrev[] = new byte[(int) file.length()];
                            fis.read(bytesPrev);
                            response.setExportInfos(new ExportType());
                            response.getExportInfos().setFilecontent(bytesPrev);
                            response.getExportInfos().setFormatId(200l);
                            response.setReturnInfos(new de.fhdo.terminologie.ws.administration.ReturnType());
                            response.getReturnInfos().setStatus(de.fhdo.terminologie.ws.administration.Status.OK);
                        }
                        catch (FileNotFoundException ex)
                        {
                            Logger.getLogger(ProposalWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        catch (IOException ex)
                        {
                            Logger.getLogger(ProposalWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        finally
                        {
                            if (fis != null)
                            {
                                try
                                {
                                    fis.close();
                                }
                                catch (IOException ex)
                                {
                                    Logger.getLogger(ProposalWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                    else
                    {
                        // WS-Aufruf
                        //Administration_Service service = new Administration_Service();
                        de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
                        response = port.exportCodeSystemContent(req_export_cs);
                    }

                    if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK))
                    {
                        //Nach CodeSystem anhand des Namens in Pub suchen
                        //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
                        de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

                        de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
                        request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                        request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
                        request_searchPub.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
                        request_searchPub.getCodeSystem().setName(resp.getCodeSystem().get(0).getName());
                        de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub = port_searchPub.listCodeSystems(request_searchPub);

                        if (respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
                        {
                            if ((respSearchPub.getCodeSystem() != null) && (respSearchPub.getCodeSystem().size() == 1))
                            {
                                targetCS = respSearchPub.getCodeSystem().get(0);
                            }
                            else if ((respSearchPub.getCodeSystem() == null) || (respSearchPub.getCodeSystem().isEmpty()))
                            {
                                //kein CodeSystem gefunden
                                Messagebox.show("Kein Ziel-Codesystem gefunden. Transfer nicht möglich.", "Status ändern", Messagebox.OK, Messagebox.ERROR);
                            }
                            else if (respSearchPub.getCodeSystem().size() > 1)
                            {
                                //mehrere CodeSysteme gefunden
                                Map map = new HashMap();
                                map.put("targets", respSearchPub.getCodeSystem());
                                map.put("source", resp.getCodeSystem().get(0).getName());

                                Window win = (Window) Executions.createComponents("/gui/admin/modules/publication/selectTargetPopup.zul", null, map);
                                win.doModal();
                            }

                            if (targetCS != null)
                            {
                                //de.fhdo.terminologie.ws.administrationPub.Administration_Service service_pub = new de.fhdo.terminologie.ws.administrationPub.Administration_Service();
                                de.fhdo.terminologie.ws.administrationPub.Administration port_pub = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
                                // Login
                                de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
                                request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
                                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                                // Codesystem
                                request.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
                                request.getCodeSystem().setId(targetCS.getId());

                                request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
                                if (resp.getCodeSystem().get(0).getName().contains("LOINC"))
                                {
                                    request.getImportInfos().setOrder(true);
                                    request.getImportInfos().setRole(CODES.ROLE_ADMIN);
                                }
                                else
                                {
                                    request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
                                }

                                de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion csv_pub = new de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion();
                                csv_pub.setName(csv_search.getName());
                                request.getCodeSystem().getCodeSystemVersions().add(csv_pub);
                                request.getImportInfos().setFormatId(response.getExportInfos().getFormatId());
                                request.getImportInfos().setFilecontent(response.getExportInfos().getFilecontent());
                                request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
                                request.setImportId(targetCS.getId());

                                de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return ret_import = port_pub.importCodeSystem(request);
                                if (ret_import.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK))
                                {
                                    //import erfolgreich --> HilfsVersion für neues CodeSystem wieder löschen  
                                    ret.setSuccess(true);
                                    msg += "Freigabe erfolgreich. ";
                                    if (targetCS.getCodeSystemVersions() != null)
                                    {
                                        for (de.fhdo.terminologie.ws.searchPub.CodeSystemVersion v : targetCS.getCodeSystemVersions())
                                        {
                                            if (v.getName().equals("Temp_import"))
                                            {
                                                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                                                req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                                                req_remove.getLogin().setSessionID(session.getAttribute("session_id").toString());

                                                req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                                                de.fhdo.terminologie.ws.authoringPub.CodeSystem cs_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
                                                cs_remove.setId(targetCS.getId());
                                                de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion csv_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
                                                csv_remove.setVersionId(v.getVersionId());
                                                cs_remove.getCodeSystemVersions().add(csv_remove);

                                                req_remove.getDeleteInfo().setCodeSystem(cs_remove);
                                                req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.CODE_SYSTEM_VERSION);

                                                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = removeTerminologyOrConcept(req_remove);
                                                if (resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                                                {
                                                    ret.setSuccess(true);
                                                }
                                                else
                                                {
                                                    msg += "Löschen der Version fehlgeschlagen. ";
                                                }
                                            }
                                        }
                                    }
                                    ret.setMessage(msg);
                                    return ret;
                                }
                                else
                                {
                                    //Import fehlgeschlagen (z.B.: ein Import läuft bereits)
                                    ret.setSuccess(false);
                                    msg += ret_import.getReturnInfos().getMessage();
                                    ret.setMessage(msg);
                                    return ret;
                                }
                            }
                        }
                        else
                        {
                            msg += "CodeSystem auf Plattform 'Pub' nicht gefunden. ";
                            ret.setMessage(msg);
                            return ret;
                        }
                    }
                    else
                    {
                        msg += "Beim Export ist ein Fehler aufgetreten. ";
                        ret.setMessage(msg);
                        return ret;
                    }
                }
            }
            else
            {
                msg += "CodeSystem konnte nicht gelesen werden. ";
                ret.setMessage(msg);
                return ret;
            }
        }
        else if (po.getClassname().equals("ValueSetVersion"))
        {
            //ValueSet aus Collab lesen
            ListValueSetsRequestType request_search = new ListValueSetsRequestType();
            request_search.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
            request_search.getLogin().setSessionID(session.getAttribute("session_id").toString());
            request_search.setValueSet(new ValueSet());
            for (Proposalobject p : po.getProposal().getProposalobjects())
            {
                if (p.getClassname().equals("ValueSet"))
                {
                    request_search.getValueSet().setName(p.getName());
                }
            }
            ListValueSetsResponse.Return resp = port_search.listValueSets(request_search);

            if ((resp.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                    && (resp.getValueSet() != null)
                    && (resp.getValueSet().size() == 1))
            {
                ValueSetVersion vsv_search = null;
                for (ValueSetVersion vsv_temp : resp.getValueSet().get(0).getValueSetVersions())
                {
                    if (vsv_temp.getVersionId().equals(resp.getValueSet().get(0).getCurrentVersionId()))
                    {
                        vsv_search = vsv_temp;
                    }
                    else
                    {
                        //resp.getValueSet().get(0).getValueSetVersions().remove(vsv_temp);
                    }
                }
                resp.getValueSet().get(0).getValueSetVersions().clear();
                resp.getValueSet().get(0).getValueSetVersions().add(vsv_search);

                if (vsv_search != null)
                {
                    ListValueSetContentsRequestType requestVsContent = new ListValueSetContentsRequestType();
                    requestVsContent.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
                    requestVsContent.getLogin().setSessionID(session.getAttribute("session_id").toString());

                    requestVsContent.setValueSet(resp.getValueSet().get(0));
                    requestVsContent.setReadMetadataLevel(false);

                    //Search_Service service = new Search_Service();
                    de.fhdo.terminologie.ws.search.Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();
                    ListValueSetContentsResponse.Return responseVsContent = port.listValueSetContents(requestVsContent);

                    boolean found = false;
                    if (responseVsContent.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK))
                    {
                        Iterator<CodeSystemEntity> it = responseVsContent.getCodeSystemEntity().iterator();

                        do
                        {
                            CodeSystemEntity cse = it.next();
                            de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType requestCsPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
                            requestCsPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                            requestCsPub.getLogin().setSessionID(session.getAttribute("session_id").toString());

                            requestCsPub.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
                            de.fhdo.terminologie.ws.searchPub.CodeSystemVersion csv = new de.fhdo.terminologie.ws.searchPub.CodeSystemVersion();
                            csv.setOid(cse.getCodeSystemVersionEntityMemberships().get(0).getCodeSystemVersion().getOid());
                            requestCsPub.getCodeSystem().getCodeSystemVersions().add(csv);

                            de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return responseCsPub = listCodeSystemsPub(requestCsPub);

                            if (responseCsPub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.searchPub.Status.OK))
                            {
                                if (responseCsPub.getCodeSystem().size() > 0)
                                {
                                    found = true;
                                }
                            }
                        }
                        while (it.hasNext() && found != false);
                    }

                    ExportValueSetContentRequestType req_export_vs = new ExportValueSetContentRequestType();

                    // Login
                    req_export_vs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
                    req_export_vs.getLogin().setSessionID(session.getAttribute("session_id").toString());

                    req_export_vs.setValueSet(new ValueSet());
                    req_export_vs.getValueSet().setId(resp.getValueSet().get(0).getId());

                    ValueSetVersion vsv = new ValueSetVersion();
                    vsv.setVersionId(vsv_search.getVersionId());
                    req_export_vs.getValueSet().getValueSetVersions().add(vsv);

                    // Export Type
                    ExportType eType = new ExportType();
                    eType.setFormatId(195l);            // TODO 193 = ClaML, 194 = CSV, 195 = SVS
                    eType.setUpdateCheck(false);
                    req_export_vs.setExportInfos(eType);

                    // Optional: ExportParameter
                    ExportParameterType eParameterType = new ExportParameterType();
                    eParameterType.setAssociationInfos("");
                    eParameterType.setCodeSystemInfos(false);
                    eParameterType.setTranslations(false);
                    req_export_vs.setExportParameter(eParameterType);

                    logger.debug("Export-Service-Aufruf...");

                    // WS-Aufruf
                    //Administration_Service service_export = new Administration_Service();
                    de.fhdo.terminologie.ws.administration.Administration port_export = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
                    ExportValueSetContentResponse.Return response = port_export.exportValueSetContent(req_export_vs);
                    if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK))
                    {
                        //Nach ValueSet anhand der OID in Pub suchen
                        //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
                        de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

                        de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
                        request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                        request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
                        request_searchPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
                        request_searchPub.getValueSet().setName(resp.getValueSet().get(0).getName());
                        de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);

                        if (respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
                        {
                            if ((respSearchPub.getValueSet() != null) && (respSearchPub.getValueSet().size() == 1))
                            {
                                targetVS = respSearchPub.getValueSet().get(0);

                                if (!found)
                                {
                                    for (de.fhdo.terminologie.ws.searchPub.ValueSetVersion v : targetVS.getValueSetVersions())
                                    {
                                        if (v.getName().equals("Temp_import"))
                                        {
                                            de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                                            req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                                            req_remove.getLogin().setSessionID(session.getAttribute("session_id").toString());

                                            req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                                            de.fhdo.terminologie.ws.authoringPub.ValueSet vs_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                                            vs_remove.setId(targetVS.getId());

                                            req_remove.getDeleteInfo().setValueSet(vs_remove);
                                            req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.VALUE_SET);

                                            de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = removeTerminologyOrConcept(req_remove);
                                            if (resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                                            {
                                                ret.setSuccess(true);
                                            }
                                            else
                                            {
                                                msg += "Löschen der Version fehlgeschlagen.";
                                            }
                                        }
                                    }
                                    ret.setSuccess(false);
                                    msg += "Quell-CodeSystem ist nicht vorhanden. ";
                                    ret.setMessage(msg);
                                    return ret;
                                }

                            }
                            else if ((respSearchPub.getValueSet() == null) || (respSearchPub.getValueSet().isEmpty()))
                            {
                                //kein ValueSet gefunden
                                Messagebox.show("Kein Ziel-ValueSet gefunden. Transfer nicht möglich.", "Status ändern", Messagebox.OK, Messagebox.ERROR);
                            }
                            else if (respSearchPub.getValueSet().size() > 1)
                            {
                                //mehrere ValueSets gefunden
                                Map map = new HashMap();
                                map.put("targets", respSearchPub.getValueSet());

                                Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                                win.doModal();
                            }

                            if (targetVS != null)
                            {
                                //de.fhdo.terminologie.ws.administrationPub.Administration_Service service_pub = new de.fhdo.terminologie.ws.administrationPub.Administration_Service();
                                de.fhdo.terminologie.ws.administrationPub.Administration port_pub = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));

                                // Login
                                de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType();
                                request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
                                request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                                // ValueSet
                                request.setValueSet(new de.fhdo.terminologie.ws.administrationPub.ValueSet());
                                request.getValueSet().setId(targetVS.getId());

                                de.fhdo.terminologie.ws.administrationPub.ValueSetVersion vsv_pub = new de.fhdo.terminologie.ws.administrationPub.ValueSetVersion();
                                vsv_pub.setName(vsv_search.getName());

                                request.getValueSet().getValueSetVersions().add(vsv_pub);
                                request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
                                request.getImportInfos().setFormatId(301L);
                                request.getImportInfos().setFilecontent(response.getExportInfos().getFilecontent());
                                request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
                                request.getImportInfos().setOrder(Boolean.TRUE);

                                de.fhdo.terminologie.ws.administrationPub.ImportValueSetResponse.Return ret_import = port_pub.importValueSet(request);
                                if (ret_import.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK))
                                {
                                    //import erfolgreich
                                    ret.setSuccess(true);
                                    msg += "Freigabe erfolgreich. ";
                                    for (de.fhdo.terminologie.ws.searchPub.ValueSetVersion v : targetVS.getValueSetVersions())
                                    {
                                        if (v.getName().equals("Temp_import"))
                                        {
                                            de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                                            req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                                            req_remove.getLogin().setSessionID(session.getAttribute("session_id").toString());

                                            req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                                            de.fhdo.terminologie.ws.authoringPub.ValueSet vs_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                                            vs_remove.setId(targetVS.getId());
                                            de.fhdo.terminologie.ws.authoringPub.ValueSetVersion vsv_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
                                            vsv_remove.setVersionId(v.getVersionId());
                                            vs_remove.getValueSetVersions().add(vsv_remove);

                                            req_remove.getDeleteInfo().setValueSet(vs_remove);
                                            req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.VALUE_SET_VERSION);

                                            de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = removeTerminologyOrConcept(req_remove);
                                            if (resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                                            {
                                                ret.setSuccess(true);
                                            }
                                            else
                                            {
                                                msg += "Löschen der Version fehlgeschlagen.";
                                            }
                                        }
                                    }
                                    ret.setMessage(msg);
                                    return ret;
                                }
                            }
                        }
                        {
                            msg += "CodeSystem auf Plattform 'Pub' nicht gefunden. ";
                            ret.setMessage(msg);
                            return ret;
                        }
                    }
                    else
                    {
                        msg += "Beim Export ist ein Fehler aufgetreten. ";
                        ret.setMessage(msg);
                        return ret;
                    }
                }
            }
            else
            {
                msg += "CodeSystem konnte nicht gelesen werden. ";
                ret.setMessage(msg);
                return ret;
            }
        }
        else if (po.getClassname().equals("CodeSystem"))
        {
            //Create new CodeSystem on public platform
            ListCodeSystemsRequestType req_cs = new ListCodeSystemsRequestType();
            req_cs.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
            req_cs.getLogin().setSessionID(session.getAttribute("session_id").toString());

            CodeSystem cs = new CodeSystem();
            cs.setId(po.getClassId());
            req_cs.setCodeSystem(cs);

            ListCodeSystemsResponse.Return ret_cs = listCodeSystems(req_cs);

            if (ret_cs.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK))
            {
                //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
                de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

                de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
                request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
                request_searchPub.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
                request_searchPub.getCodeSystem().setName(ret_cs.getCodeSystem().get(0).getName());
                de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub = port_searchPub.listCodeSystems(request_searchPub);

                if ((respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
                        && (respSearchPub.getCodeSystem().isEmpty()))
                {
                    de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType();
                    request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                    request.getLogin().setSessionID(session.getAttribute("session_id").toString());

                    de.fhdo.terminologie.ws.authoringPub.CodeSystem cs_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
                    cs_pub.setName(ret_cs.getCodeSystem().get(0).getName());
                    cs_pub.setDescription(ret_cs.getCodeSystem().get(0).getDescription());
                    cs_pub.setDescriptionEng(ret_cs.getCodeSystem().get(0).getDescriptionEng());
                    cs_pub.setIncompleteCS(ret_cs.getCodeSystem().get(0).isIncompleteCS());
                    cs_pub.setResponsibleOrganization(ret_cs.getCodeSystem().get(0).getResponsibleOrganization());
                    cs_pub.setWebsite(ret_cs.getCodeSystem().get(0).getWebsite());
                    cs_pub.setAutoRelease(ret_cs.getCodeSystem().get(0).isAutoRelease());
                    cs_pub.setCodeSystemType(ret_cs.getCodeSystem().get(0).getCodeSystemType());
                    de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion csv_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
                    csv_pub.setName("Temp_import");
                    cs_pub.getCodeSystemVersions().add(csv_pub);
                    request.setCodeSystem(cs_pub);

                    de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemResponse.Return ret_pub = createCodeSystemPub(request);
                    if (ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                    {
                        targetCS = new de.fhdo.terminologie.ws.searchPub.CodeSystem();
                        targetCS.setId(ret_pub.getCodeSystem().getId());
                        ret.setSuccess(true);
                        return ret;
                    }
                    else
                    {
                        msg += "CodeSystem konnte nicht erstellt werden. ";
                        ret.setMessage(msg);
                        return ret;
                    }
                }
                else
                {
                    msg += "CodeSystem ist bereits vorhanden. ";
                    ret.setMessage(msg);
                    return ret;
                }
            }
            else
            {
                msg += "CodeSystem konnte nicht gelesen werden. ";
                ret.setMessage(msg);
                return ret;
            }
        }
        else if (po.getClassname().equals("ValueSet"))
        {
            //Create new ValueSet on public platform
            ListValueSetsRequestType req_vs = new ListValueSetsRequestType();
            req_vs.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
            req_vs.getLogin().setSessionID(session.getAttribute("session_id").toString());

            ValueSet vs = new ValueSet();
            //vs.setId(po.getClassId());
            vs.setName(po.getName());
            req_vs.setValueSet(vs);

            ListValueSetsResponse.Return ret_vs = listValueSets(req_vs);

            if (ret_vs.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK))
            {

                //de.fhdo.terminologie.ws.searchPub.Search_Service service_searchPub = new de.fhdo.terminologie.ws.searchPub.Search_Service();
                de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

                de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
                request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                request_searchPub.getLogin().setSessionID(session.getAttribute("session_id").toString());
                request_searchPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
                request_searchPub.getValueSet().setName(ret_vs.getValueSet().get(0).getName());
                de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);

                if ((respSearchPub.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.searchPub.Status.OK)
                        && (respSearchPub.getValueSet().isEmpty()))
                {

                    de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType();
                    request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                    request.getLogin().setSessionID(session.getAttribute("ession_id").toString());

                    de.fhdo.terminologie.ws.authoringPub.ValueSet vs_pub = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                    vs_pub.setName(ret_vs.getValueSet().get(0).getName());
                    vs_pub.setDescription(ret_vs.getValueSet().get(0).getDescription());
                    vs_pub.setDescriptionEng(ret_vs.getValueSet().get(0).getDescriptionEng());
                    vs_pub.setResponsibleOrganization(ret_vs.getValueSet().get(0).getResponsibleOrganization());
                    vs_pub.setWebsite(ret_vs.getValueSet().get(0).getWebsite());
                    vs_pub.setAutoRelease(ret_vs.getValueSet().get(0).isAutoRelease());

                    de.fhdo.terminologie.ws.authoringPub.ValueSetVersion vsv_pub = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
                    vsv_pub.setName("Temp_import");
                    vs_pub.getValueSetVersions().add(vsv_pub);
                    request.setValueSet(vs_pub);

                    de.fhdo.terminologie.ws.authoringPub.CreateValueSetResponse.Return ret_pub = createValueSetPub(request);
                    if (ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                    {
                        targetVS = new de.fhdo.terminologie.ws.searchPub.ValueSet();
                        targetVS.setId(ret_pub.getValueSet().getId());
                        ret.setSuccess(true);
                        return ret;
                    }
                    else
                    {
                        msg += "ValueSet konnte nicht erstellt werden. ";
                        ret.setMessage(msg);
                        return ret;
                    }
                }
            }
            else
            {
                msg += "ValueSet konnte nicht gelesen werden. ";
                ret.setMessage(msg);
                return ret;
            }
        }
        ret.setSuccess(false);
        ret.setMessage("Ein unbekannter Fehler ist aufgetreten.");
        return ret;
    }

    /*
  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter) 
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }
     */
    private static de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemResponse.Return createCodeSystemPub(de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoringPub.Authoring_Service service = new de.fhdo.terminologie.ws.authoringPub.Authoring_Service();
        de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        return port.createCodeSystem(parameter);
    }

    /*
  private static CreateValueSetResponse.Return createValueSet(de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType parameter) {
    //de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = WebServiceHelper.getInstance().getAuthoringServicePort();
    return port.createValueSet(parameter);
  }*/
    private static de.fhdo.terminologie.ws.authoringPub.CreateValueSetResponse.Return createValueSetPub(de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoringPub.Authoring_Service service = new de.fhdo.terminologie.ws.authoringPub.Authoring_Service();
        de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        return port.createValueSet(parameter);
    }

    private static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
        de.fhdo.terminologie.ws.authoring.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateCodeSystemVersionStatus(parameter);
    }

    private static UpdateConceptStatusResponse.Return updateConceptStatus(de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
        de.fhdo.terminologie.ws.authoring.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateConceptStatus(parameter);
    }

    private static UpdateValueSetStatusResponse.Return updateValueSetStatus(de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
        de.fhdo.terminologie.ws.authoring.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateValueSetStatus(parameter);
    }

    private static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
        de.fhdo.terminologie.ws.authoring.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateConceptValueSetMembershipStatus(parameter);
    }

    private static ListCodeSystemsResponse.Return listCodeSystems(de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType parameter)
    {
        //de.fhdo.terminologie.ws.search.Search_Service service = new de.fhdo.terminologie.ws.search.Search_Service();
        de.fhdo.terminologie.ws.search.Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listCodeSystems(parameter);
    }

    private static de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return listCodeSystemsPub(de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType parameter)
    {
        //de.fhdo.terminologie.ws.searchPub.Search_Service service = new de.fhdo.terminologie.ws.searchPub.Search_Service();
        de.fhdo.terminologie.ws.searchPub.Search port = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        return port.listCodeSystems(parameter);
    }

    private static ListValueSetsResponse.Return listValueSets(de.fhdo.terminologie.ws.search.ListValueSetsRequestType parameter)
    {
        //de.fhdo.terminologie.ws.search.Search_Service service = new de.fhdo.terminologie.ws.search.Search_Service();
        de.fhdo.terminologie.ws.search.Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listValueSets(parameter);
    }

    private static de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType removeTerminologyOrConcept(de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authoringPub.Authoring_Service service = new de.fhdo.terminologie.ws.authoringPub.Authoring_Service();
        de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        return port.removeTerminologyOrConcept(parameter);
    }

    public void selectTargets(Object o)
    {
        if (o instanceof de.fhdo.terminologie.ws.searchPub.CodeSystem)
        {
            targetCS = (de.fhdo.terminologie.ws.searchPub.CodeSystem) o;
        }
        else if (o instanceof de.fhdo.terminologie.ws.searchPub.ValueSet)
        {
            targetVS = (de.fhdo.terminologie.ws.searchPub.ValueSet) o;
        }
    }
    
    public void sendEmailNotification(Proposal proposal, long statusFrom, long statusTo, String reason)
    {
        // TODO 5. Benutzer benachrichtigen
        ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();
        
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        //Lade alle Benutzer mit Privilegien auf Proposal
        String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
        Query qPrivilegeUsers = hb_session.createQuery(hqlPrivilegeUsers);
        qPrivilegeUsers.setParameter("id", proposal.getId());
        List<Collaborationuser> privUserList = qPrivilegeUsers.list();

        for (Collaborationuser cu : privUserList)
        {
            completeUserList.add(cu);
        }

        //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
        String hqlPrivilegeGroups = "from Collaborationuser cu join fetch cu.discussiongroups dg join fetch dg.privileges pri join fetch pri.proposal pro where pro.id=:id";
        Query qPrivilegeGroups = hb_session.createQuery(hqlPrivilegeGroups);
        qPrivilegeGroups.setParameter("id", proposal.getId());
        List<Collaborationuser> privGroupList = qPrivilegeGroups.list();

        for (Collaborationuser cu : privGroupList)
        {
            boolean doubleEntry = false;
            for (Collaborationuser cuI : completeUserList)
            {

                if (cu.getId().equals(cuI.getId()))
                {
                    doubleEntry = true;
                }
            }

            if (!doubleEntry)
            {
                completeUserList.add(cu);
            }
        }

        ArrayList<String> mailAdr = new ArrayList<String>();
        for (Collaborationuser u : completeUserList)
        {
            if (u.getSendMail() != null && u.getSendMail())
            {
                mailAdr.add(u.getEmail());
            }
        }
        String[] adr = new String[mailAdr.size()];
        for (int i = 0; i < adr.length; i++)
        {

            adr[i] = mailAdr.get(i);
        }
        Mail.sendMailAUT(adr, M_AUT.PROPOSAL_STATUS_SUBJECT, M_AUT.getInstance().getProposalStatusChangeText(
                proposal.getVocabularyName(),
                proposal.getContentType(),
                proposal.getDescription(),
                ProposalStatus.getInstance().getStatusStr(statusFrom),
                ProposalStatus.getInstance().getStatusStr(statusTo),
                reason));
    }
}
