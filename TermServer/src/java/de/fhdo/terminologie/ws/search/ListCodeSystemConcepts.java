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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.helper.CodeSystemHelper;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LicenceHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.conceptAssociation.TraverseConceptToRoot;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.PagingResultType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.SortingType;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystemConcepts
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    //private static String lastHQLCount = "";
    //private static long lastCountResult = 0;

    public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, boolean noLimit)
    {
        return ListCodeSystemConcepts(parameter, null, noLimit);
    }

    public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ListCodeSystemConcepts gestartet ======");
        }

        boolean createHibernateSession = (session == null);

        // Return-Informationen anlegen
        ListCodeSystemConceptsResponseType response = new ListCodeSystemConceptsResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter pr�fen
        if (validateParameter(parameter, response) == false)
        {
            logger.debug("Parameter falsch");
            return response; // Fehler bei den Parametern
        }

        // Login-Informationen auswerten (gilt f�r jeden Webservice)
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

        int maxPageSizeUserSpecific = 10;
        if (parameter.getPagingParameter() != null && parameter.getPagingParameter().getUserPaging() != null)
        {
            if (parameter.getPagingParameter().getUserPaging())
            {
                maxPageSizeUserSpecific = Integer.valueOf(parameter.getPagingParameter().getPageSize());
            }
        }
        else
        {
            maxPageSizeUserSpecific = -1;
        }

        // PagingInfo
        int maxPageSize = 100;   // Gibt an, wieviele Treffer maximal zur�ckgegeben werden

        //Warum loggedIn hier? Das ergibt am Termbrowser folgenden Bug: Wenn man eingeloggt ist kann man sich keine HugeFlat Concept Liste mehr ansehen e.g. LOINC! => WrongValueException!
        if (noLimit)// || loggedIn) 
        {
            maxPageSize = -1;
        }
        else
        {
            String maxPageSizeStr = SysParameter.instance().getStringValue("maxPageSize", null, null);
            try
            {
                maxPageSize = Integer.parseInt(maxPageSizeStr);
            }
            catch (Exception e)
            {
                logger.error("Fehler bei SysParameter.instance().getStringValue(\"maxPageSize\", null, null): " + e.getLocalizedMessage());
            }
        }

        boolean traverseConceptsToRoot = false;
        int maxPageSizeSearch = 5;   // Gibt an, wieviele Treffer bei einer Suche maximal zur�ckgegeben werden

        if (parameter != null && parameter.getSearchParameter() != null
                && parameter.getSearchParameter().getTraverseConceptsToRoot() != null && parameter.getSearchParameter().getTraverseConceptsToRoot())
        {
            traverseConceptsToRoot = true;

            String maxPageSizeSearchStr = SysParameter.instance().getStringValue("maxPageSizeSearch", null, null);
            if (parameter != null && parameter.getSearchParameter() != null)
            {
                if (maxPageSizeSearchStr != null && maxPageSizeSearchStr.length() > 0)
                {
                    try
                    {
                        maxPageSizeSearch = Integer.parseInt(maxPageSizeSearchStr);
                    }
                    catch (Exception e)
                    {
                        logger.error("Fehler bei SysParameter.instance().getStringValue(\"maxPageSizeSearch\", null, null): " + e.getLocalizedMessage());
                    }
                }
            }
        }

        //maxPageSizeSearch = 2;
        //maxPageSize = 2;
        logger.debug("maxPageSize: " + maxPageSizeSearch);
        logger.debug("maxPageSizeSearch: " + maxPageSizeSearch);

        try
        {
            //List<CodeSystemConcept> conceptList = null;

            long codeSystemVersionId = 0;
            if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
            {
                CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
                codeSystemVersionId = csv.getVersionId();
            }

            // Lizenzen pr�fen
            boolean validLicence = LicenceHelper.getInstance().userHasLicence(loginInfoType, codeSystemVersionId);

            if (validLicence == false)
            {
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Sie besitzen keine g�ltige Lizenz f�r dieses Vokabular!");
                return response;
            }
            else
            {
                logger.debug("Lizenz f�r Vokabular vorhanden!");
            }

            // Hibernate-Block, Session �ffnen
            //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Session hb_session = null;
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

            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                if (codeSystemVersionId == 0)
                {
                    // Aktuelle Version des Vokabulars ermitteln
                    long codeSystemId = parameter.getCodeSystem().getId();

                    CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);
                    codeSystemVersionId = CodeSystemHelper.getCurrentVersionId(cs);
                }

                // HQL erstellen
                // Besonderheit hier: es d�rfen keine Werte nachgeladen werden
                // Beim Abruf eines ICD w�re dieses sehr inperformant, da er f�r
                // jeden Eintrag sonst nachladen w�rde

                /*
         SELECT * FROM code_system_concept csc
         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
         JOIN code_system_entity cse ON csev.versionId=cse.id
         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId
         WHERE csvem.codeSystemVersionId=10
                 */
                long languageId = 0;

                /*SELECT * FROM
         (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc
         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
         JOIN code_system_entity cse ON csev.versionId=cse.id
         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
         WHERE csvem.codeSystemVersionId=10 LIMIT 2) csc2
         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId*/
                //
                //String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc"
                String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.*, csct.term translation_term, csct.termAbbrevation translation_termAbbrevation, csct.description translation_description, csct.languageId translation_languageId, csct.id translation_id "
                        + " FROM code_system_concept csc"
                        + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
                        + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
                        + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
                        + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId AND languageId=:languageId"
                        + " WHERE_TEIL) csc2"
                        //+ " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId";
                        + " LEFT JOIN code_system_entity_version_association cseva1 ON cseva1.codeSystemEntityVersionId1=csc2.versionId"
                        + " LEFT JOIN code_system_entity_version_association cseva2 ON cseva2.codeSystemEntityVersionId2=csc2.versionId ORDER BY csc2.code";

                String sqlCount = "SELECT COUNT(*) FROM code_system_concept csc"
                        + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
                        + " JOIN code_system_entity cse ON csev.versionId=cse.id"
                        + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
                        + " WHERE_TEIL";

                /*String sql = " FROM code_system_concept csc"
         + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
         + " JOIN code_system_entity cse ON csev.versionId=cse.id"
         + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
         + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId";*/
                //+ " WHERE csvem.codeSystemVersionId=:codeSystemVersionId"
                //+ " GROUP BY csc.code"
                //+ " ORDER BY csc.code";
                // Parameter dem Helper hinzuf�gen
                // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzuf�gen,
                // sonst sind SQL-Injections m�glich
                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("", "csvem.codeSystemVersionId", codeSystemVersionId);

                String searchTerm = "";

                if (parameter != null && parameter.getCodeSystemEntity() != null)
                {
                    if (parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
                            && parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
                    {
                        CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().toArray()[0];
                        parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
                        parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());
                    }

                    if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
                            && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
                    {
                        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                        parameterHelper.addParameter("csev.", "statusDate", csev.getStatusDate());

                        if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
                        {
                            CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
                            parameterHelper.addParameter("csc.", "code", csc.getCode());
                            //Matthias 02.06.2015 search improved for description - Term is added together with description after getWhere() has been executed
                            parameterHelper.addParameter("csc.", "term", csc.getTerm());
                            searchTerm = csc.getTerm();

                            parameterHelper.addParameter("csc.", "termAbbrevation", csc.getTermAbbrevation());
                            parameterHelper.addParameter("csc.", "isPreferred", csc.getIsPreferred());

                            if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
                            {
                                CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];
                                parameterHelper.addParameter("csct.", "term", csct.getTerm());
                                parameterHelper.addParameter("csct.", "termAbbrevation", csct.getTermAbbrevation());
                                if (csct.getLanguageId() > 0)
                                {
                                    languageId = csct.getLanguageId();
                                    //parameterHelper.addParameter("csct.", "languageId", csct.getLanguageId());
                                }
                            }
                        }
                    }
                }

                if (loggedIn == false)
                {
                    parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                }

                // Parameter hinzuf�gen (immer mit AND verbunden)
                // Gesamt-Anzahl lesen
                String where = parameterHelper.getWhere("");

                //sqlCount = "SELECT COUNT(DISTINCT cse.id) FROM " + sqlCount.replaceAll("WHERE_TEIL", where);
                sqlCount = sqlCount.replaceAll("WHERE_TEIL", where);

                //q.addScalar("csc.code", Hibernate.TEXT);  // Index: 0
                logger.debug("SQL-Count: " + sqlCount);
                SQLQuery qCount = hb_session.createSQLQuery(sqlCount);
                parameterHelper.applySQLParameter(qCount);
                BigInteger anzahlGesamt = (BigInteger) qCount.uniqueResult();

                logger.debug("Anzahl Gesamt: " + anzahlGesamt.longValue());

                if (anzahlGesamt.longValue() > 0)
                {
                    // Suche begrenzen
                    int pageSize = -1;
                    int pageIndex = 0;
                    boolean allEntries = false;

                    if (parameter != null && parameter.getPagingParameter() != null)
                    {
                        logger.debug("Search-Parameter angegeben");
                        if (parameter.getPagingParameter().isAllEntries() != null && parameter.getPagingParameter().isAllEntries().booleanValue() == true)
                        {
                            if (loggedIn)
                            {
                                allEntries = true;
                            }
                        }

                        pageSize = Integer.valueOf(parameter.getPagingParameter().getPageSize());
                        pageIndex = parameter.getPagingParameter().getPageIndex();
                    }

                    // MaxResults mit Wert aus SysParam pr�fen
                    if (traverseConceptsToRoot)
                    {
                        if (pageSize < 0 || (maxPageSizeSearch > 0 && pageSize > maxPageSizeSearch))
                        {
                            pageSize = maxPageSizeSearch;
                        }
                    }
                    else
                    {
                        if (pageSize < 0 || (maxPageSize > 0 && pageSize > maxPageSize))
                        {
                            pageSize = maxPageSize;
                        }
                    }
                    if (pageIndex < 0)
                    {
                        pageIndex = 0;
                    }

                    logger.debug("pageIndex: " + pageIndex);
                    logger.debug("pageSize: " + pageSize);

                    String sortStr = " ORDER BY csc.code";

                    if (parameter.getSortingParameter() != null)
                    {
                        if (parameter.getSortingParameter().getSortType() == null
                                || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY)
                        {
                            sortStr = " ORDER BY";

                            if (parameter.getSortingParameter().getSortBy() != null
                                    && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
                            {
                                sortStr += " csc.term";
                            }
                            else
                            {
                                sortStr += " csc.code";
                            }

                            if (parameter.getSortingParameter().getSortDirection() != null
                                    && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
                            {
                                sortStr += " desc";
                            }

                        }
                    }

                    //String where = parameterHelper.getWhere("");
                    String where_all = where + sortStr;

                    if (pageSize > 0 && allEntries == false)
                    {
                        where_all += " LIMIT " + (pageIndex * pageSize) + "," + pageSize;
                    }

                    sql = sql.replaceAll("WHERE_TEIL", where_all);

                    int anzahl = 0;
                    logger.debug("SQL: " + sql);
                    // Query erstellen
                    SQLQuery q = hb_session.createSQLQuery(sql);
                    //Matthias: add readonly for query to improve performance
                    q.setReadOnly(true);

                    q.addScalar("csc2.code", StandardBasicTypes.TEXT);  // Index: 0
                    q.addScalar("csc2.term", StandardBasicTypes.TEXT);
                    q.addScalar("csc2.termAbbrevation", StandardBasicTypes.TEXT);
                    q.addScalar("csc2.description", StandardBasicTypes.TEXT);
                    q.addScalar("csc2.isPreferred", StandardBasicTypes.BOOLEAN);
                    q.addScalar("csc2.codeSystemEntityVersionId", StandardBasicTypes.LONG);

                    q.addScalar("csc2.effectiveDate", StandardBasicTypes.DATE);  // Index: 6
                    q.addScalar("csc2.insertTimestamp", StandardBasicTypes.DATE);
                    q.addScalar("csc2.isLeaf", StandardBasicTypes.BOOLEAN);
                    q.addScalar("csc2.majorRevision", StandardBasicTypes.INTEGER);
                    q.addScalar("csc2.minorRevision", StandardBasicTypes.INTEGER);
                    q.addScalar("csc2.status", StandardBasicTypes.INTEGER);
                    q.addScalar("csc2.statusDate", StandardBasicTypes.DATE);
                    q.addScalar("csc2.versionId", StandardBasicTypes.LONG);
                    q.addScalar("csc2.codeSystemEntityId", StandardBasicTypes.LONG);

                    q.addScalar("csc2.id", StandardBasicTypes.LONG);  // Index: 15
                    q.addScalar("csc2.currentVersionId", StandardBasicTypes.LONG);

                    q.addScalar("csc2.isAxis", StandardBasicTypes.BOOLEAN);  // Index: 17
                    q.addScalar("csc2.isMainClass", StandardBasicTypes.BOOLEAN);

                    q.addScalar("translation_term", StandardBasicTypes.TEXT);  // Index: 19
                    q.addScalar("translation_termAbbrevation", StandardBasicTypes.TEXT);
                    q.addScalar("translation_languageId", StandardBasicTypes.LONG);
                    q.addScalar("translation_description", StandardBasicTypes.TEXT);
                    q.addScalar("translation_id", StandardBasicTypes.LONG);

                    q.addScalar("cseva1.codeSystemEntityVersionId1", StandardBasicTypes.LONG); // Index: 24
                    q.addScalar("cseva1.codeSystemEntityVersionId2", StandardBasicTypes.LONG);
                    q.addScalar("cseva1.leftId", StandardBasicTypes.LONG);
                    q.addScalar("cseva1.associationTypeId", StandardBasicTypes.LONG);
                    q.addScalar("cseva1.associationKind", StandardBasicTypes.INTEGER);
                    q.addScalar("cseva1.status", StandardBasicTypes.INTEGER);
                    q.addScalar("cseva1.statusDate", StandardBasicTypes.DATE);
                    q.addScalar("cseva1.insertTimestamp", StandardBasicTypes.TIMESTAMP);

                    q.addScalar("csc2.meaning", StandardBasicTypes.TEXT); //Index: 32
                    q.addScalar("csc2.hints", StandardBasicTypes.TEXT);

                    /*csct.term translation_term, csct.termAbbrevation translation_termAbbrevation, csct.description translation_description, csct.languageId, csct.id translation_id*/

 /*q.addScalar("csct.term", Hibernate.TEXT);  // Index: 19
           q.addScalar("csct.termAbbrevation", Hibernate.TEXT);
           q.addScalar("csct.languageId", Hibernate.LONG);
           q.addScalar("csct.description", Hibernate.TEXT);
           q.addScalar("csct.id", Hibernate.LONG);*/

 /*q.addScalar("csev.effectiveDate", Hibernate.DATE);  // Index: 6
           q.addScalar("csev.insertTimestamp", Hibernate.DATE);
           q.addScalar("csev.isLeaf", Hibernate.BOOLEAN);
           q.addScalar("csev.majorRevision", Hibernate.INTEGER);
           q.addScalar("csev.minorRevision", Hibernate.INTEGER);
           q.addScalar("csev.status", Hibernate.INTEGER);
           q.addScalar("csev.statusDate", Hibernate.DATE);
           q.addScalar("csev.versionId", Hibernate.LONG);
           q.addScalar("csev.codeSystemEntityId", Hibernate.LONG);

           q.addScalar("cse.id", Hibernate.LONG);  // Index: 15
           q.addScalar("cse.currentVersionId", Hibernate.LONG);

           q.addScalar("csvem.isAxis", Hibernate.BOOLEAN);  // Index: 17
           q.addScalar("csvem.isMainClass", Hibernate.BOOLEAN);

           q.addScalar("csct.term", Hibernate.TEXT);  // Index: 19
           q.addScalar("csct.termAbbrevation", Hibernate.TEXT);
           q.addScalar("csct.languageId", Hibernate.LONG);
           q.addScalar("csct.description", Hibernate.TEXT);
           q.addScalar("csct.id", Hibernate.LONG);*/
                    parameterHelper.applySQLParameter(q);
                    q.setLong("languageId", languageId);

                    //+ " ORDER BY csc.code"
                    //q.setParameter("codeSystemVersionId", codeSystemVersionId);

                    /*List<CodeSystemConcept> conceptList = (List<CodeSystemConcept>) q.list();

           for (CodeSystemConcept csc : conceptList)
           {
           logger.debug(csc.getCode());
           anzahl++;
           }*/
                    response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

                    List conceptList = (List) q.list();

                    Iterator it = conceptList.iterator();

                    long lastCodeSystemEntityVersionId = 0;
                    CodeSystemEntity cse = new CodeSystemEntity();
                    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                    CodeSystemConcept csc = new CodeSystemConcept();
                    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
                    boolean fertig = false;

                    while (it.hasNext())
                    {
                        Object[] item = null;
                        long codeSystemEntityVersionId = 0;
                        do
                        {
                            if (it.hasNext() == false)
                            {
                                fertig = true;
                                break;
                            }

                            item = (Object[]) it.next();

                            // Pr�fen, ob Translation (1:N)
                            codeSystemEntityVersionId = (Long) item[5];
                            if (lastCodeSystemEntityVersionId == codeSystemEntityVersionId)
                            {
                                // Gleiches Konzept, Assoziation hinzuf�gen
                                if (parameter.isLookForward())
                                {
                                    addAssociationToEntityVersion(csev, item);
                                }
                            }
                        }
                        while (lastCodeSystemEntityVersionId == codeSystemEntityVersionId);

                        if (fertig)
                        {
                            break;
                        }

                        // Konzepte zusammenbauen
                        cse = new CodeSystemEntity();
                        csev = new CodeSystemEntityVersion();
                        csc = new CodeSystemConcept();
                        csvem = new CodeSystemVersionEntityMembership();

                        // Konzept
                        if (item[0] != null)
                        {
                            csc.setCode(item[0].toString());
                        }
                        if (item[1] != null)
                        {
                            csc.setTerm(item[1].toString());
                        }
                        if (item[2] != null)
                        //csc.setTermAbbrevation(new String((char[])item[2]));
                        {
                            csc.setTermAbbrevation(item[2].toString());
                        }
                        if (item[3] != null)
                        {
                            csc.setDescription(item[3].toString());
                        }
                        if (item[4] != null)
                        {
                            csc.setIsPreferred((Boolean) item[4]);
                        }
                        if (item[5] != null)
                        {
                            csc.setCodeSystemEntityVersionId((Long) item[5]);
                        }

                        if (item[32] != null)
                        {
                            csc.setMeaning(item[32].toString());
                        }
                        if (item[33] != null)
                        {
                            csc.setHints(item[33].toString());
                        }

                        // Entity Version
                        if (item[6] != null)
                        {
                            csev.setEffectiveDate((Date) item[6]);
                        }
                        if (item[7] != null)
                        {
                            csev.setInsertTimestamp((Date) item[7]);
                        }
                        if (item[8] != null)
                        {
                            csev.setIsLeaf((Boolean) item[8]);
                        }
                        if (item[9] != null)
                        {
                            csev.setMajorRevision((Integer) item[9]);
                        }
                        if (item[10] != null)
                        {
                            csev.setMinorRevision((Integer) item[10]);
                        }
                        if (item[11] != null)
                        {
                            csev.setStatus((Integer) item[11]);
                        }
                        if (item[12] != null)
                        {
                            csev.setStatusDate((Date) item[12]);
                        }
                        if (item[13] != null)
                        {
                            csev.setVersionId((Long) item[13]);
                        }

                        // Code System Entity
                        if (item[15] != null)
                        {
                            cse.setId((Long) item[15]);
                        }
                        if (item[16] != null)
                        {
                            cse.setCurrentVersionId((Long) item[16]);
                        }

                        // Entity Membership
                        if (item[17] != null)
                        {
                            csvem.setIsAxis((Boolean) item[17]);
                        }
                        if (item[18] != null)
                        {
                            csvem.setIsMainClass((Boolean) item[18]);
                        }

                        // Translation
                        addTranslationToConcept(csc, item);

                        // Assoziation
                        if (parameter.isLookForward())
                        {
                            addAssociationToEntityVersion(csev, item);
                        }

                        if (traverseConceptsToRoot)
                        {
                            // Alle Elemente bis zum Root ermitteln (f�r Suche)
                            TraverseConceptToRoot traverse = new TraverseConceptToRoot();
                            TraverseConceptToRootRequestType requestTraverse = new TraverseConceptToRootRequestType();
                            requestTraverse.setLogin(parameter.getLogin());
                            requestTraverse.setCodeSystemEntity(new CodeSystemEntity());
                            CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
                            csevRequest.setVersionId(csev.getVersionId());
                            requestTraverse.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                            requestTraverse.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);

                            requestTraverse.setDirectionToRoot(true);
                            requestTraverse.setReadEntityDetails(false);
                            //TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, hb_session); // die Session �bergeben, damit diese nicht geschlossen wird
                            TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, null);

                            //logger.debug("responseTraverse: " + responseTraverse.getReturnInfos().getMessage());
                            if (responseTraverse.getReturnInfos().getStatus() == ReturnType.Status.OK)
                            {
                                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
                                {
                                    csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(
                                            responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
                                }
                                else
                                {
                                    csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().addAll(
                                            responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
                                }
                            }
                        }

                        //logger.debug(csc.getCode());
                        //logger.debug("Type: " + csc.getClass().getCanonicalName());
                        /*Object[] o = (Object[]) csc;
             for(int i=0;i<o.length;++i)
             {
             //logger.debug(i + ": " + o.toString());
             if(o[i] != null)
             {
             logger.debug(i + ": " + o[i].toString());
             logger.debug(i + ": " + o[i].getClass().getCanonicalName());
             }
             else logger.debug(i + ": null");
              
             //for(int j=0;j<)
             }*/
                        csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        csev.getCodeSystemConcepts().add(csc);
                        cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                        cse.getCodeSystemEntityVersions().add(csev);
                        cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                        cse.getCodeSystemVersionEntityMemberships().add(csvem);
                        response.getCodeSystemEntity().add(cse);

                        lastCodeSystemEntityVersionId = codeSystemEntityVersionId;

                        anzahl++;
                    }

                    // Treffermenge pr�fen            
                    // Paging wird aktiviert
                    if (anzahlGesamt.longValue() > maxPageSize)
                    {
                        response.setPagingInfos(new PagingResultType());
                        response.getPagingInfos().setMaxPageSize(maxPageSize);
                        response.getPagingInfos().setPageIndex(pageIndex);
                        response.getPagingInfos().setPageSize(String.valueOf(pageSize));
                        response.getPagingInfos().setCount(anzahlGesamt.intValue());
                        if (parameter != null && parameter.getPagingParameter() != null)
                        {
                            response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge gr��er ist als die maximale Seitengr��e.");
                        }
                    }
                    else
                    {

                        if ((maxPageSizeUserSpecific != -1) && anzahlGesamt.longValue() > maxPageSizeUserSpecific)
                        {

                            response.setPagingInfos(new PagingResultType());
                            response.getPagingInfos().setMaxPageSize(maxPageSizeUserSpecific);
                            response.getPagingInfos().setPageIndex(pageIndex);
                            response.getPagingInfos().setPageSize(String.valueOf(maxPageSizeUserSpecific));
                            response.getPagingInfos().setCount(anzahlGesamt.intValue());
                            if (parameter != null && parameter.getPagingParameter() != null)
                            {
                                response.getPagingInfos().setMessage("Paging wurde aktiviert, da popUpSearchCS spezifische Seitenanzahl.");
                            }
                        }
                    }

                    // Status an den Aufrufer weitergeben            
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + anzahl);
                    response.getReturnInfos().setCount(anzahl);

                }
                else
                {
                    response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Keine Konzepte f�r die Filterkriterien vorhanden");
                    response.getReturnInfos().setCount(0);
                }
                /*String hql = "select distinct csc from CodeSystemConcept csc";
         hql += " join fetch csc.codeSystemEntityVersion csev";
         hql += " join fetch csev.codeSystemEntity cse";
         hql += " left outer join fetch csc.codeSystemConceptTranslations csct";
         hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
        
         //if (parameter.isLookForward())
         //{
         //  hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 ass1";
         //  hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 ass2";
         //}

         // Parameter dem Helper hinzuf�gen
         // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzuf�gen,
         // sonst sind SQL-Injections m�glich
         HQLParameterHelper parameterHelper = new HQLParameterHelper();
         parameterHelper.addParameter("", "codeSystemVersionId", codeSystemVersionId);

         if (parameter != null && parameter.getCodeSystemEntity() != null)
         {
         if (parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
         && parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
         {
         CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().toArray()[0];
         parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
         parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());
         }

         if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
         && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
         {
         CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
         parameterHelper.addParameter("csev.", "statusDate", csev.getStatusDate());

         if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
         {
         CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
         parameterHelper.addParameter("csc.", "code", csc.getCode());
         parameterHelper.addParameter("csc.", "term", csc.getTerm());
         parameterHelper.addParameter("csc.", "termAbbrevation", csc.getTermAbbrevation());
         parameterHelper.addParameter("csc.", "isPreferred", csc.getIsPreferred());

         if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
         {
         CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];
         parameterHelper.addParameter("csct.", "term", csct.getTerm());
         parameterHelper.addParameter("csct.", "termAbbrevation", csct.getTermAbbrevation());
         if (csct.getLanguageId() > 0)
         parameterHelper.addParameter("csct.", "languageId", csct.getLanguageId());
         }
         }
         }
         }

         if (loggedIn == false)
         {
         parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
         }

         // Parameter hinzuf�gen (immer mit AND verbunden)
         String where = parameterHelper.getWhere("");
         hql += where;

         // immer neueste Version lesen
         hql += " AND csev.id=cse.currentVersionId";

         hql += " ORDER BY csc.code";



         // Suche begrenzen
         int pageSize = -1;
         int pageIndex = 0;
         boolean allEntries = false;

         if (parameter != null && parameter.getPagingParameter() != null)
         {
         // vorher aber noch die Gesamtanzahl berechnen
         //Integer count = (Integer) hb_session.createQuery("select count(*) from ....").uniqueResult();

         if (parameter.getPagingParameter().isAllEntries() != null && parameter.getPagingParameter().isAllEntries().booleanValue() == true)
         {
         if (loggedIn)
         allEntries = true;
         }

         pageSize = parameter.getPagingParameter().getPageSize();
         pageIndex = parameter.getPagingParameter().getPageIndex();
         }

         // MaxResults mit Wert aus SysParam pr�fen
         if (traverseConceptsToRoot)
         {
         if (pageSize < 0 || (maxPageSizeSearch > 0 && pageSize > maxPageSizeSearch))
         pageSize = maxPageSizeSearch;
         }
         else
         {
         if (pageSize < 0 || (maxPageSize > 0 && pageSize > maxPageSize))
         pageSize = maxPageSize;
         }
         if (pageIndex < 0)
         pageIndex = 0;

         // Gesamt-Anzahl lesen
         String hqlCount = "select count(term) from CodeSystemConcept csc";
         hqlCount += " join  csc.codeSystemEntityVersion csev";
         hqlCount += " join  csev.codeSystemEntity cse";
         hqlCount += " join  cse.codeSystemVersionEntityMemberships csvem";
         hqlCount += where;

         //hql = hql.replace("distinct csc", "count(term)");
         logger.debug("HQL-Count: " + hqlCount);
         org.hibernate.Query q = hb_session.createQuery(hqlCount);

         // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
         parameterHelper.applyParameter(q);
         long anzahlGesamt = (Long) q.uniqueResult();

         // Anzahl z�hlen Datenbank-Aufruf durchf�hren
         //int anzahlGesamt = q.list().size();
         //int anzahlGesamt = 100;  // TODO Gesamt-Anzahl herausbekommen
         logger.debug("Anzahl Gesamt: " + anzahlGesamt);


         logger.debug("HQL: " + hql);
         // Query erstellen
         q = hb_session.createQuery(hql);

         // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
         parameterHelper.applyParameter(q);


         //conceptList = (java.util.List<CodeSystemConcept>) q.list();
         if (anzahlGesamt > 0)
         {
         //hb_session.setFlushMode(FlushMode.AUTO);

         ScrollableResults scrollResults = q.scroll();

         int itCount = 0;

         if (scrollResults != null)
         {
         java.util.List<CodeSystemEntity> entityList = new LinkedList<CodeSystemEntity>();

         if (pageIndex > 0 && allEntries == false && anzahlGesamt > 0)
         {
         // Vorspulen
         //if(pageSize * pageIndex < anzahlGesamt)
         //  scrollResults.setRowNumber(pageSize * pageIndex);
         for (int i = 0; i < pageSize * pageIndex && i < anzahlGesamt; ++i)
         {
         if (scrollResults.next() == false)
         break;

         if (i % 50 == 0)
         {
         // wichtig, da Speicher sonst voll l�uft
         hb_session.flush();
         hb_session.clear();
         }
         }
         }

         //Iterator<CodeSystemConcept> iterator = conceptList.iterator();
         //while (iterator.hasNext())

         try
         {
         while (scrollResults.next())
         {
         if (itCount >= pageSize && allEntries == false)
         break;

         if (itCount % 50 == 0)
         {
         // wichtig, da Speicher sonst voll l�uft
         //hb_session.flush();
         hb_session.clear();
         }
         itCount++;

         //CodeSystemConcept csc = iterator.next();
         CodeSystemConcept csc = (CodeSystemConcept) scrollResults.get(0);

         // neues Entity generieren, damit nicht nachgeladen werden muss
         CodeSystemEntity entity = csc.getCodeSystemEntityVersion().getCodeSystemEntity();

         CodeSystemEntityVersion csev = csc.getCodeSystemEntityVersion();

         csev.setCodeSystemEntity(null);

         if (parameter.isLookForward())
         {
         // Verbindungen suchen
         if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null)
         {
         for (CodeSystemEntityVersionAssociation ass : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())
         {
         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
         ass.setAssociationType(null);
         }
         }
         if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null)
         {
         for (CodeSystemEntityVersionAssociation ass : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
         {
         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
         ass.setAssociationType(null);
         }
         }
         }
         else
         {
         if (traverseConceptsToRoot)
         {
         // Alle Elemente bis zum Root ermitteln (f�r Suche)
         TraverseConceptToRoot traverse = new TraverseConceptToRoot();
         TraverseConceptToRootRequestType requestTraverse = new TraverseConceptToRootRequestType();
         requestTraverse.setLogin(parameter.getLogin());
         requestTraverse.setCodeSystemEntity(new CodeSystemEntity());
         CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
         csevRequest.setVersionId(csev.getVersionId());
         requestTraverse.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
         requestTraverse.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);

         requestTraverse.setDirectionToRoot(true);
         requestTraverse.setReadEntityDetails(false);
         TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, hb_session); // die Session �bergeben, damit diese nicht geschlossen wird

         //logger.debug("responseTraverse: " + responseTraverse.getReturnInfos().getMessage());

         if (responseTraverse.getReturnInfos().getStatus() == ReturnType.Status.OK)
         {
         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(
         responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
         }
         else
         {
         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
         }
         }
         else
         {
         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
         }
         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
         }

         csev.setCodeSystemMetadataValues(null);
         csev.setConceptValueSetMemberships(null);
         csev.setPropertyVersions(null);
         csev.setAssociationTypes(null);

         csc.setCodeSystemEntityVersion(null);

         logger.debug("Akt Code: " + csc.getCode() + ", " + csc.getTerm());

         //Translations
         if (csc.getCodeSystemConceptTranslations() != null)
         {
         Iterator<CodeSystemConceptTranslation> itTrans = csc.getCodeSystemConceptTranslations().iterator();

         while (itTrans.hasNext())
         {
         CodeSystemConceptTranslation csct = itTrans.next();
         csct.setCodeSystemConcept(null);
         }
         }



         csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
         csev.getCodeSystemConcepts().add(csc);

         entity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
         entity.getCodeSystemEntityVersions().add(csev);

         // M:N Verbindung zur Vokabular-Version (ohne nachladen)
         CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) entity.getCodeSystemVersionEntityMemberships().toArray()[0];
         ms.setCodeSystemVersion(null);
         ms.setCodeSystemEntity(null);
         ms.setId(null);

         entity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
         entity.getCodeSystemVersionEntityMemberships().add(ms);

         entityList.add(entity);
         }
         }
         catch (org.hibernate.exception.GenericJDBCException ex)
         {
         logger.debug("Keine Eintraege");
         ex.printStackTrace();
         }

         int anzahl = 0;
         if (entityList != null)
         anzahl = entityList.size();
         response.setCodeSystemEntity(entityList);

         // Treffermenge pr�fen
         if (anzahlGesamt > anzahl)
         {
         // Paging wird aktiviert
         response.setPagingInfos(new PagingResultType());
         response.getPagingInfos().setMaxPageSize(maxPageSize);
         response.getPagingInfos().setPageIndex(pageIndex);
         response.getPagingInfos().setPageSize(pageSize);
         response.getPagingInfos().setCount((int) anzahlGesamt);
         if (parameter != null && parameter.getPagingParameter() != null)
         {
         response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge gr��er ist als die maximale Seitengr��e.");
         }
         //response.getPagingInfos().setMessage();
         }
         //response.setPagingInfos(null);

         // Status an den Aufrufer weitergeben
         response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
         response.getReturnInfos().setStatus(ReturnType.Status.OK);
         response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + anzahl);
         response.getReturnInfos().setCount(anzahl);

         }
         }
         else
         {
         response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
         response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
         response.getReturnInfos().setStatus(ReturnType.Status.OK);
         response.getReturnInfos().setMessage("Keine Konzepte f�r die Filterkriterien vorhanden");
         response.getReturnInfos().setCount(0);
         }*/
                // Hibernate-Block wird in 'finally' geschlossen, erst danach
                // Auswertung der Daten
                // Achtung: hiernach k�nnen keine Tabellen/Daten mehr nachgeladen werden
                //if(createHibernateSession)
                //hb_session.getTransaction().commit();
            }
            catch (Exception e)
            {
                //if(createHibernateSession)
                //hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts', Hibernate: " + e.getLocalizedMessage());

                logger.error("Fehler bei 'ListCodeSystemConcepts', Hibernate: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            finally
            {
                // Transaktion abschlie�en
                //Matthias:testing
                if (createHibernateSession)
                {
                    hb_session.close();
                }
            }

        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'ListCodeSystemConcepts': " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return response;
    }

    private void addTranslationToConcept(CodeSystemConcept csc, Object[] item)
    {
        if (item[19] == null)  // Term muss angegeben sein
        {
            return;
        }

        if (csc.getCodeSystemConceptTranslations() == null)
        {
            csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());
        }

        CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
        csct.setTerm(item[19].toString());
        if (item[20] != null)
        {
            csct.setTermAbbrevation(item[20].toString());
        }
        if (item[21] != null)
        {
            csct.setLanguageId((Long) item[21]);
        }
        if (item[22] != null)
        {
            csct.setDescription(item[22].toString());
        }
        if (item[23] != null)
        {
            csct.setId((Long) item[23]);
        }

        /*q.addScalar("csct.term", Hibernate.TEXT);  // Index: 19
     q.addScalar("csct.termAbbrevation", Hibernate.TEXT);
     q.addScalar("csct.languageId", Hibernate.LONG);
     q.addScalar("csct.description", Hibernate.TEXT);*/
        csc.getCodeSystemConceptTranslations().add(csct);
    }

    private void addAssociationToEntityVersion(CodeSystemEntityVersion csev, Object[] item)
    {
        try
        {
            if (item[24] == null)  // Pflichtfeld
            {
                return;
            }

            CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
            cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId((Long) item[24]);
            cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId((Long) item[25]);

            if (item[26] != null)
            {
                cseva.setLeftId((Long) item[26]);
            }
            else
            {
                logger.warn("LeftId ist null: " + csev.getVersionId());
            }

            if (item[27] != null)
            {
                cseva.setAssociationType(new AssociationType());
                cseva.getAssociationType().setCodeSystemEntityVersionId((Long) item[27]);
            }

            if (item[28] != null)
            {
                cseva.setAssociationKind((Integer) item[28]);
            }
            if (item[29] != null)
            {
                cseva.setStatus((Integer) item[29]);
            }
            if (item[30] != null)
            {
                cseva.setStatusDate((Date) item[30]);
            }
            if (item[31] != null)
            {
                cseva.setInsertTimestamp((Date) item[31]);
            }

            if (cseva.getLeftId() == null || cseva.getLeftId().longValue() == csev.getVersionId().longValue())
            {
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
                {
                    csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(new HashSet<CodeSystemEntityVersionAssociation>());
                }

                csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(cseva);
            }
            else
            {
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() == null)
                {
                    csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(new HashSet<CodeSystemEntityVersionAssociation>());
                }

                csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().add(cseva);
            }

        }
        catch (Exception ex)
        {
            logger.error("Fehler in addAssociationToEntityVersion(): " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        /*q.addScalar("cseva1.codeSystemEntityVersionId1", Hibernate.LONG); // Index: 24
     q.addScalar("cseva1.codeSystemEntityVersionId2", Hibernate.LONG);
     q.addScalar("cseva1.leftId", Hibernate.LONG);
     q.addScalar("cseva1.associationTypeId", Hibernate.LONG);
     q.addScalar("cseva1.associationKind", Hibernate.INTEGER);
     q.addScalar("cseva1.status", Hibernate.INTEGER);
     q.addScalar("cseva1.statusDate", Hibernate.DATE);
     q.addScalar("cseva1.insertTimestamp", Hibernate.TIMESTAMP);*/
    }

    /**
     * Pr�ft die Parameter anhand der Cross-Reference
     *
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
     */
    private boolean validateParameter(ListCodeSystemConceptsRequestType Request,
            ListCodeSystemConceptsResponseType Response)
    {
        boolean erfolg = true;

        CodeSystem codeSystem = Request.getCodeSystem();
        if (codeSystem == null)
        {
            Response.getReturnInfos().setMessage("CodeSystem darf nicht leer sein!");
            erfolg = false;
        }
        else
        {
            //boolean csId = false;
            boolean csvId = false;

            //csId = codeSystem.getId() != null && codeSystem.getId() > 0;

            /* if (codeSystem.getId() == null || codeSystem.getId() == 0)
       {
       Response.getReturnInfos().setMessage(
       "Es muss eine ID f�r das CodeSystem angegeben sein!");
       erfolg = false;
       } */
            if (codeSystem.getCodeSystemVersions() != null)
            {
                Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
                if (csvSet != null)
                {
                    if (csvSet.size() > 1)
                    {
                        Response.getReturnInfos().setMessage(
                                "Die CodeSystem-Version-Liste darf maximal einen Eintrag haben!");
                        erfolg = false;
                    }
                    else if (csvSet.size() == 1)
                    {
                        CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

                        if (csv.getVersionId() == null || csv.getVersionId() == 0)
                        {
                            Response.getReturnInfos().setMessage(
                                    "Es muss eine ID f�r die CodeSystem-Version angegeben sein, wenn Sie ein Typ CodeSystemVersion mitgeben! Ansonsten setzen Sie die CodeSystemVersion auf NULL und es wird die aktuellste Version abgerufen.");
                            erfolg = false;
                        }
                        else
                        {
                            csvId = true;
                        }
                    }
                }
            }

            if (csvId == false)
            {
                Response.getReturnInfos().setMessage(
                        "Es muss entweder eine ID f�r das CodeSystem oder die CodeSystem-Version angegeben sein.");
                erfolg = false;
            }
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }
}
