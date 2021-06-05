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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsResponseType;
import de.fhdo.terminologie.ws.types.GlobalSearchResultEntry;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListGloballySearchedConcepts
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    //private static String lastHQLCount = "";
    //private static long lastCountResult = 0;

    public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, boolean noLimit)
    {
        return ListGloballySearchedConcepts(parameter, null, noLimit);
    }

    public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ListGloballySearchedConcepts gestartet ======");
        }

        // Login-Informationen auswerten (gilt für jeden Webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        //3.2.17 added second check
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin(), session);
            loggedIn = loginInfoType != null;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Benutzer ist eingeloggt: " + loggedIn);
        }

        return search(parameter, session, noLimit, true);
    }
    
    private ListGloballySearchedConceptsResponseType search(ListGloballySearchedConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit, boolean withValueSetMemberships)
    {
        boolean createHibernateSession = (session == null);

        // Return-Informationen anlegen
        ListGloballySearchedConceptsResponseType response = new ListGloballySearchedConceptsResponseType();
        response.setReturnInfos(new ReturnType());
        List<GlobalSearchResultEntry> gsreList = new ArrayList<GlobalSearchResultEntry>();
        
        // Hibernate-Block, Session öffnen
        //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Session hb_session = null;
        Integer anzahl = 0;
        //org.hibernate.Transaction tx = null;
        if (createHibernateSession)
        {
            hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();
            //tx = hb_session.beginTransaction();
        }
        else
        {
            hb_session = session;
            //hb_session.getTransaction().begin();
        }
        boolean code = false;
        boolean term = false;
        try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
        {
            if (parameter != null)
            {

                if (parameter.getCodeSystemConcepts())
                { // CS

                    String hqlGroupUsers = "select distinct csc from CodeSystemConcept csc join "
                            + "csc.codeSystemEntityVersion csev join "
                            + "csev.codeSystemEntity cse join "
                            + "cse.codeSystemVersionEntityMemberships csvem join "
                            + "csvem.codeSystemVersion csv join "
                            + "csv.codeSystem cs ";
                    
                    if(withValueSetMemberships)
                    {
                        hqlGroupUsers += "join csev.conceptValueSetMemberships cvsm ";
                    }
                    
                    hqlGroupUsers += "where ";

                    if (!parameter.getCode().equals("") && parameter.getTerm().equals(""))
                    { //nur code

                        hqlGroupUsers += "csc.code like :code";
                        code = true;

                    }
                    else if (!parameter.getCode().equals("") && !parameter.getTerm().equals(""))
                    { // beide
                        //Matthias 28.05.2015 Changed to search for the search string in the description as well
                        //hqlGroupUsers += "lower(csc.term) like :term and csc.code like :code";
                        if(withValueSetMemberships)
                        {
                            hqlGroupUsers += "(lower(csc.term) like :term OR lower(csc.description) like :term OR lower(csc.meaning) like :term OR lower(cvsm.bedeutung) like :term) AND csc.code like :code";
                        }
                        else
                        {
                            hqlGroupUsers += "(lower(csc.term) like :term OR lower(csc.description) like :term OR lower(csc.meaning) like :term) AND csc.code like :code";
                        }
                        
                        code = true;
                        term = true;
                    }
                    else if (parameter.getCode().equals("") && !parameter.getTerm().equals(""))
                    { // nur term

                        //Matthias 28.05.2015 Changed to search for the search string in the description as well
                        //hqlGroupUsers += "lower(csc.term) like :term";
                        if(withValueSetMemberships)
                        {
                            hqlGroupUsers += "lower(csc.term) like :term OR lower(csc.description) like :term OR lower(csc.meaning) like :term OR lower(cvsm.bedeutung) like :term";
                        }
                        else
                        {
                            hqlGroupUsers += "lower(csc.term) like :term OR lower(csc.description) like :term OR lower(csc.meaning) like :term";
                        }
                        
                        term = true;
                    }
                    else
                    { // keine suche

                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
                        response.getReturnInfos().setCount(0);
                        return response;
                    }

                    Query qGroupUsers = hb_session.createQuery(hqlGroupUsers);

                    if (term)
                    {
                        qGroupUsers.setParameter("term", "%" + parameter.getTerm() + "%");
                    }
                    if (code)
                    {
                        qGroupUsers.setParameter("code", parameter.getCode());
                    }

                    List<CodeSystemConcept> cscList = qGroupUsers.list();
                    
                    if(cscList.isEmpty() && withValueSetMemberships)
                    {
                        return search(parameter, session, noLimit, false);
                    }

                    for (CodeSystemConcept csc : cscList)
                    {
                        GlobalSearchResultEntry gsre = new GlobalSearchResultEntry();
                        gsre.setCodeSystemEntry(true);
                        gsre.setCode(csc.getCode());
                        gsre.setTerm(csc.getTerm());

                        gsre.setCodeSystemName(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
                                iterator().next().getCodeSystemVersion().getCodeSystem().getName());
                        gsre.setCodeSystemVersionName(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
                                iterator().next().getCodeSystemVersion().getName());

                        gsre.setCsId(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
                                iterator().next().getCodeSystemVersion().getCodeSystem().getId());
                        gsre.setCsvId(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
                                iterator().next().getCodeSystemVersion().getVersionId());
                        gsre.setCsevId(csc.getCodeSystemEntityVersion().getVersionId());

                        gsreList.add(gsre);
                    }

                }
                else
                {  // VS

                    String hqlGroupUsers = "select distinct cvsm from ConceptValueSetMembership cvsm join "
                            + "cvsm.valueSetVersion vsv join "
                            + "vsv.valueSet vs join "
                            + "cvsm.codeSystemEntityVersion csev join "
                            + "csev.codeSystemEntity cse join "
                            + "csev.codeSystemConcepts csc join "
                            + "cse.codeSystemVersionEntityMemberships csvem join "
                            + "csvem.codeSystemVersion csv join "
                            + "csv.codeSystem cs where ";

                    if (!parameter.getCode().equals("") && parameter.getTerm().equals(""))
                    { //nur code

                        hqlGroupUsers += "csc.code like :code";
                        code = true;

                    }
                    else if (!parameter.getCode().equals("") && !parameter.getTerm().equals(""))
                    { // beide
                        //Matthias 02.06.2015 Changed to search for the search string in the description as well
                        //hqlGroupUsers += "lower(csc.term) like :term and csc.code like :code";
                        hqlGroupUsers += "(lower(csc.term) like :term OR lower(csc.description) like :term) AND csc.code like :code";
                        code = true;
                        term = true;
                    }
                    else if (parameter.getCode().equals("") && !parameter.getTerm().equals(""))
                    { // nur term
                        //Matthias 02.06.2015 Changed to search for the search string in the description as well
                        //hqlGroupUsers += "lower(csc.term) like :term";
                        hqlGroupUsers += "lower(csc.term) like :term OR lower(csc.description) like :term";
                        term = true;
                    }
                    else
                    { // keine suche

                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
                        response.getReturnInfos().setCount(0);
                        return response;
                    }

                    Query qGroupUsers = hb_session.createQuery(hqlGroupUsers);

                    if (term)
                    {
                        qGroupUsers.setParameter("term", "%" + parameter.getTerm() + "%");
                    }
                    if (code)
                    {
                        qGroupUsers.setParameter("code", parameter.getCode());
                    }

                    List<ConceptValueSetMembership> cvsmList = qGroupUsers.list();

                    for (ConceptValueSetMembership cvsm : cvsmList)
                    {
                        GlobalSearchResultEntry gsre = new GlobalSearchResultEntry();
                        gsre.setCodeSystemEntry(false);
                        gsre.setCode(cvsm.getCodeSystemEntityVersion().getCodeSystemConcepts().iterator().next().getCode());
                        gsre.setTerm(cvsm.getCodeSystemEntityVersion().getCodeSystemConcepts().iterator().next().getTerm());

                        gsre.setValueSetName(cvsm.getValueSetVersion().getValueSet().getName());
                        gsre.setValueSetVersionName(cvsm.getValueSetVersion().getName());
                        String csName = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
                                getCodeSystemVersion().getCodeSystem().getName();
                        String csvName = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
                                getCodeSystemVersion().getName();
                        String oid = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
                                getCodeSystemVersion().getOid();

                        gsre.setSourceCodeSystemInfo(csName + " (Version: " + csvName + " OID: " + oid + ")");

                        gsre.setVsId(cvsm.getValueSetVersion().getValueSet().getId());
                        gsre.setVsvId(cvsm.getValueSetVersion().getVersionId());
                        gsre.setCvsmId(cvsm.getId());

                        gsreList.add(gsre);
                    }
                }

                response.setGlobalSearchResultEntry(gsreList);

                if (anzahl > 0)
                {

                    // Status an den Aufrufer weitergeben           
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl!");
                    response.getReturnInfos().setCount(anzahl);
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
                    response.getReturnInfos().setCount(0);
                }
            }
            else
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListGloballySearchedConcepts', parameter == null");
            }
        }
        catch (Exception e)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListGloballySearchedConcepts', Hibernate: " + e.getLocalizedMessage());
            logger.error("Fehler bei 'ListGloballySearchedConcepts', Hibernate: " + e.getLocalizedMessage());
        }
        finally
        {
            // Transaktion abschließen
            if (createHibernateSession)
            {
                if (hb_session != null)
                {
                    hb_session.close();
                }
            }
        }

        return response;
    }
}
