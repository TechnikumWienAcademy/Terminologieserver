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
 
 * 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.administration._import;

import com.csvreader.CsvReader;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.administration.ImportStatus;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;

/**
 * @since 2012-03-28
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 *
 * LOINC-DB-Version: Database Version 2.38
 */
public class ImportLOINC
{

    private static Map<String, Long> codesMap;
    private Map<String, AssociationType> associationMap;
    private static final Integer LOINC_NUM = 0;
    private Map<String, MetadataParameter> metadataParameterMap;
    private static String[] metadataFields =
    {
        "COMPONENT",
        "PROPERTY",
        "TIME_ASPCT",
        "SYSTEM",
        "SCALE_TYP",
        "METHOD_TYP",
        "CLASS",
        "SOURCE",
        "CHNG_TYPE",
        "COMMENTS",
        "CONSUMER_NAME",
        "MOLAR_MASS",
        "CLASSTYPE",
        "FORMULA",
        "SPECIES",
        "EXMPL_ANSWERS",
        "ACSSYM",
        "BASE_NAME",
        "NAACCR_ID",
        "CODE_TABLE",
        "SURVEY_QUEST_TEXT",
        "SURVEY_QUEST_SRC",
        "UNITSREQUIRED",
        "SUBMITTED_UNITS",
        "RELATEDNAMES2",
        "ORDER_OBS",
        "CDISC_COMMON_TESTS",
        "HL7_FIELD_SUBFIELD_ID",
        "EXTERNAL_COPYRIGHT_NOTICE",
        "EXAMPLE_UNITS",
        "HL7_V2_DATATYPE",
        "HL7_V3_DATATYPE",
        "CURATED_RANGE_AND_UNITS",
        "DOCUMENT_SECTION",
        "EXAMPLE_UCUM_UNITS",
        "EXAMPLE_SI_UCUM_UNITS",
        "STATUS_REASON",
        "STATUS_TEXT",
        "CHANGE_REASON_PUBLIC",
        "COMMON_TEST_RANK",
        "COMMON_ORDER_RANK",
        "COMMON_SI_TEST_RANK",
        "HL7_ATTACHMENT_STRUCTURE",
        "STATUS",
    };
    private static Logger logger = Logger4j.getInstance().getLogger();
    ImportCodeSystemRequestType parameter;
    private int countImported = 0;

    private boolean onlyCSV = true; //Only CSV for this case
    private Long csId = 0L;
    private Long csvId = 0L;
    private String resultStr = "";
    private boolean update = false;
    private ImportStatus _status;
    private Long _importId = 0L;

    public ImportLOINC(ImportCodeSystemRequestType _parameter)
    {
        logger.setLevel(Level.DEBUG);

        if (logger.isInfoEnabled())
        {
            logger.info("====== ImportLOINC gestartet ======");
        }

        parameter = _parameter;
        
        _status = new ImportStatus();
        _status.importTotal = 0;
        _status.importCount = 0;
        _status.importRunning = true;
        _status.exportRunning = false;
        _status.cancel = false;

        if(_parameter.getImportId() != null)
        {
            _importId = _parameter.getImportId();
            StaticStatusList.addStatus(_parameter.getImportId(), _status);
        }
    }

    /**
     * @return the countImported
     */
    public int getCountImported()
    {
        return countImported;
    }

    public String importLOINC_Associations(ImportCodeSystemResponseType response)
    {
        StaticStatus.importCount = 0;
        StaticStatus.importTotal = 0;

        String s = "";
        if (codesMap == null)
        {
            codesMap = new HashMap<String, Long>();
        }

        int count = 0, countFehler = 0;

        CsvReader csv;
        try
        {
            logger.debug("codesMap-Size: " + codesMap.size());

            byte[] bytes = parameter.getImportInfos().getFilecontent();
            logger.debug("wandle zu InputStream um...");
            InputStream is = new ByteArrayInputStream(bytes);
            csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
            csv.setDelimiter('\t');
            csv.setUseTextQualifier(true);
            csv.readHeaders();
            logger.debug("Anzahl Header: " + csv.getHeaderCount());
            for (int i = 0; i < csv.getHeaderCount(); ++i)
            {
                logger.debug("Header: " + csv.getHeader(i));
            }

            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try // try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                if (parameter.getCodeSystem() == null
                        || parameter.getCodeSystem().getId() == 0
                        || parameter.getCodeSystem().getCodeSystemVersions() == null
                        || parameter.getCodeSystem().getCodeSystemVersions().size() == 0)
                {
                    // Fehlermeldung
                    hb_session.getTransaction().rollback();
                    hb_session.close();
                    return "Kein Codesystem angegeben!";
                }

                csId = parameter.getCodeSystem().getId();
                csvId = parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId();

                // Assoziationen lesen
                associationMap = new HashMap<String, AssociationType>();
                List<AssociationType> associationList = hb_session.createQuery("from AssociationType").list();
                for (int i = 0; i < associationList.size(); ++i)
                {
                    associationMap.put(associationList.get(i).getForwardName(), associationList.get(i));
                }

                logger.debug("Starte Import...");

                int countEvery = 0;

                // Daten laden
                while (csv.readRecord())
                {
                    //logger.warn(new Date().getTime() + ", Lese Index " + StaticStatus.importCount);
                    countEvery++;

                    CreateConceptAssociationRequestType request = new CreateConceptAssociationRequestType();
                    request.setLogin(parameter.getLogin());

                    request.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());

                    String code1 = csv.get("LOINC");
                    String code2 = csv.get("MAP_TO");
                    String comment = csv.get("COMMENT");

                    //logger.warn(new Date().getTime() + ", CSV gelesen");
                    //logger.warn("Code1: " + code1 + ", Code2: " + code2 + ", comment: " + comment);
                    // CSV-ID für LOINC-Code lesen
                    long csev_id1 = getCSEV_VersionIdFromCode(code1);
                    long csev_id2 = getCSEV_VersionIdFromCode(code2);

                    //logger.warn(new Date().getTime() + ", Codes gelesen");
                    if (csev_id1 > 0 && csev_id2 > 0)
                    {
                        //logger.warn(new Date().getTime() + ", Füge Assoziation ein...");

                        // IDs setzen
                        request.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                        request.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());

                        request.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csev_id1);
                        request.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csev_id2);

                        request.getCodeSystemEntityVersionAssociation().setAssociationKind(1); // ontologisch
                        request.getCodeSystemEntityVersionAssociation().setLeftId(csev_id1);

                        request.getCodeSystemEntityVersionAssociation().setAssociationType(new AssociationType());

                        // Assoziationstyp erhalten
                        //logger.warn(new Date().getTime() + ", Assoziationstyp erhalten...");
                        if (comment != null && comment.length() > 0 && associationMap.containsKey(comment))
                        {
                            request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(
                                    associationMap.get(comment).getCodeSystemEntityVersionId());
                        }
                        else
                        {
                            if(comment != null && comment.length() > 0)
                            {
                            // Neuen Beziehungstyp einfÃ¼gen

                                CreateConceptAssociationTypeRequestType requestAssociation = new CreateConceptAssociationTypeRequestType();
                                requestAssociation.setLogin(parameter.getLogin());
                                requestAssociation.setCodeSystem(parameter.getCodeSystem());
                                requestAssociation.setCodeSystemEntity(new CodeSystemEntity());
                                requestAssociation.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                                AssociationType at = new AssociationType();
                                if (comment.length() > 48)
                                {
                                    at.setForwardName(comment.substring(0, 48));
                                }
                                else
                                {
                                    at.setForwardName(comment);
                                }
                                at.setReverseName("");
                                CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                                csev.setAssociationTypes(new HashSet<AssociationType>());
                                csev.getAssociationTypes().add(at);
                                requestAssociation.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

                                CreateConceptAssociationType ccat = new CreateConceptAssociationType();
                                CreateConceptAssociationTypeResponseType responseAssociation = ccat.CreateConceptAssociationType(requestAssociation, hb_session);

                                if (responseAssociation.getReturnInfos().getStatus() == ReturnType.Status.OK)
                                {
                                    CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseAssociation.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                                    request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(csev_result.getVersionId());
                                }
                                else 
                                {
                                    request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(5L);
                                }
                                // gehÃ¶rt zu
                
                                //logger.warn(new Date().getTime() + ", Neue Beziehung eingefÃ¼gt");
                            }
                            else
                            {
                                // Standard-Beziehung nehmen
                                request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(5L); // gehÃ¶rt zu
                            }
                        }

                        //logger.warn(new Date().getTime() + ", Assoziationstyp bestimmt");
                        StaticStatus.importCount++;
                        if (StaticStatus.importCount % 100 == 0)
                        {
                            logger.debug("Lese Datensatz " + StaticStatus.importCount + ", count: " + count);
                        }

                        // Beziehung speichern
                        // Dienst aufrufen (Konzept einfügen)
                        //logger.warn(new Date().getTime() + ", Assoziation in DB einfügen...");
                        CreateConceptAssociation cca = new CreateConceptAssociation();
                        CreateConceptAssociationResponseType responseCCA = cca.CreateConceptAssociation(request, hb_session);

                        if (responseCCA.getReturnInfos().getStatus() == ReturnType.Status.OK)
                        {
                            count++;

                            if (StaticStatus.importCount % 100 == 0)
                            {
                                logger.debug("Neue Beziehung hinzugefügt, ID1: " + csev_id1 + ", ID2: " + csev_id2);
                            }
                        }
                        else
                        {
                            countFehler++;
                        }

                        //logger.warn(new Date().getTime() + ", Assoziation in DB eingefügt");
                    }
                    else
                    {
                        countFehler++;
                        logger.debug("Term ist nicht angegeben");
                    }

                    //logger.warn(new Date().getTime() + ", vor Speicherbereinigung");
                    //Mimimum acceptable free memory you think your app needs 
                    //long minRunningMemory = (1024 * 1024);
                    //Runtime runtime = Runtime.getRuntime();
                    if (countEvery % 500 == 0)
                    {
                        //logger.debug("FreeMemory: " + runtime.freeMemory());

                        // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                        hb_session.flush();
                        hb_session.clear();

                        if (countEvery % 1000 == 0)
                        {
                            // sicherheitshalber aufrufen
                            System.gc();
                        }
                    }

                    //logger.warn(new Date().getTime() + ", nach Speicherbereinigung");
                    //logger.warn(new Date().getTime() + ", Lese nächsten Record");
                    csv.skipLine();

                    if (countFehler > 10)
                    {
                        break;
                    }
                }

                if (count == 0 || countFehler > 10)
                {
                    hb_session.getTransaction().rollback();

                    resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);

                    response.getReturnInfos().setMessage("Keine Beziehungen importiert. Möglicherweise ist die Fehleranzahl zu hoch. Anzahl Fehler: " + countFehler);
                }
                else
                {
                    logger.debug("Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                    hb_session.getTransaction().commit();
                    countImported = count;
                    response.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Beziehung(en) importiert, " + countFehler + " Fehler");
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error("Fehler beim Import der LOINC-Association-Datei: " + ex.getMessage());
                s = "Fehler beim Import der LOINC-Association-Datei: " + ex.getLocalizedMessage();

                try
                {
                    hb_session.getTransaction().rollback();

                    resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);

                    logger.info("[ImportLOINC.java] Rollback durchgefuehrt!");
                }
                catch (Exception exRollback)
                {
                    logger.info(exRollback.getMessage());
                    logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
                }
            }
            finally
            {
                // Session schließen
                hb_session.close();
            }

        }
        catch (Exception ex)
        {
            //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
            s = "Fehler beim LOINC-Association-Import: " + ex.getLocalizedMessage();
            ex.printStackTrace();
        }

        associationMap.clear();
        associationMap = null;
        codesMap.clear();
        codesMap = null;

        logger.debug("ImportLOINC-Association - fertig");
        return s;
    }

    private long getCSEV_VersionIdFromCode(String code)
    {

        //List<CodeSystemConcept> list = hb_session.createQuery("from CodeSystemConcept where code=\"" + code + "\"").list();
        /*Query q = hb_session.createQuery("from CodeSystemConcept where code=:s_code");
    q.setString("s_code", code);
    List<CodeSystemConcept> list = q.list();
    
    if(list != null && list.size() > 0)
    {
      CodeSystemConcept csc = list.get(0);
      return csc.getCodeSystemEntityVersionId();
    }*/
        if (codesMap.containsKey(code))
        {
            return codesMap.get(code);
        }
        else
        {
            logger.warn("Konzept mit Code '" + code + "' nicht in Map gefunden!");
        }

        return 0;
    }

    public String importLOINC(ImportCodeSystemResponseType response)
    {

        String path = SysParameter.instance().getStringValue("LoincCsvPath", null, null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        byte[] bytes = parameter.getImportInfos().getFilecontent();
        if (!parameter.getImportInfos().getOrder())
        {

            StaticStatus.importCount = 0;
            StaticStatus.importTotal = 0;
            ArrayList<String> errList = new ArrayList<String>();

            String s = "";

            int count = 0, countFehler = 0;

            codesMap = new HashMap<String, Long>();

            CsvReader csv;
            try
            {

                logger.debug("Wandle zu InputStream um...");
                InputStream is = new ByteArrayInputStream(bytes);
                csv = new CsvReader(is, Charset.forName("UTF-8"));
                
                int numberOfLines = -1;

                while (csv.readRecord())
                {
                    numberOfLines++;
                }

                StaticStatusList.getStatus(parameter.getImportId()).importTotal = numberOfLines;
                is.close();

                is = new ByteArrayInputStream(bytes);
                csv = new CsvReader(is, Charset.forName("UTF-8"));
                
                csv.setDelimiter(',');
                csv.setTextQualifier('"');
                csv.setUseTextQualifier(true);

                csv.readHeaders();
                logger.debug("Anzahl Header: " + csv.getHeaderCount());

                // Hibernate-Block, Session öffnen
                org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try // try-catch-Block zum Abfangen von Hibernate-Fehlern
                {

                    CodeSystem cs_db = null;
                    //check if cs exists if yes => new version if not
                    if (parameter.getCodeSystem().getId() != null)
                    {
                        cs_db = (CodeSystem) hb_session.get(CodeSystem.class, parameter.getCodeSystem().getId());
                    }

                    CodeSystemVersion csv2 = new CodeSystemVersion();
                    csv2.setCodeSystem(cs_db);
                    Date d = new Date();
                    csv2.setInsertTimestamp(d);
                    csv2.setName(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                    csv2.setPreviousVersionId(cs_db.getCurrentVersionId());
                    csv2.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                    csv2.setStatusDate(d);
                    csv2.setPreferredLanguageId(33l);
                    csv2.setReleaseDate(d);
                    csv2.setUnderLicence(false);
                    csv2.setValidityRange(236l);
                    csv2.setOid("2.16.840.1.113883.6.1");
                    hb_session.save(csv2);
                    cs_db.setCurrentVersionId(csv2.getVersionId());
                    cs_db.getCodeSystemVersions().add(csv2);
                    hb_session.update(cs_db);

                    cs_db.getCodeSystemVersions().clear();
                    cs_db.getCodeSystemVersions().add(csv2);

                    parameter.setCodeSystem(cs_db);

                    // Metadaten-Parameter lesen
                    metadataParameterMap = new HashMap<String, MetadataParameter>();
                    List<MetadataParameter> mpList = hb_session.createQuery("from MetadataParameter").list();
                    for (int i = 0; i < mpList.size(); ++i)
                    {
                        metadataParameterMap.put(mpList.get(i).getParamName(), mpList.get(i));
                    }

                    logger.debug("Starte Import...");

                    int counter = 0;
                    while (csv.readRecord())
                    {
                        StaticStatusList.getStatus(parameter.getImportId()).importCount = counter;
                        counter ++;
                        
                        CreateConceptRequestType request = new CreateConceptRequestType();
                        request.setLogin(parameter.getLogin());
                        request.setCodeSystem(parameter.getCodeSystem());
                        request.setCodeSystemEntity(new CodeSystemEntity());
                        request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

                        CodeSystemConcept csc = new CodeSystemConcept();
                        csc.setIsPreferred(true);

                        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                        csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        csev.setIsLeaf(true);

                        CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
                        membership.setIsMainClass(Boolean.TRUE);
                        membership.setIsAxis(Boolean.FALSE);

                        request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                        request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

                        CreateConcept cc = new CreateConcept();

                        StaticStatus.importCount++;
                        if (StaticStatus.importCount % 200 == 0)
                        {
                            logger.debug("Lese Datensatz " + StaticStatus.importCount + ", count: " + count);
                        }

                        //request.setCodeSystemEntity(new CodeSystemEntity());
                        //request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                        csc.setCode(csv.get(LOINC_NUM));
                        csc.setTerm(csv.get("LONG_COMMON_NAME"));
                        csc.setTermAbbrevation(csv.get("SHORTNAME"));

                        //Matthias: change "|" to ":" for fully specified name
                        csc.setDescription(csv.get("COMPONENT") + " : " + csv.get("PROPERTY") + " : "
                                + csv.get("TIME_ASPCT") + " : " + csv.get("SYSTEM") + " : "
                                + csv.get("SCALE_TYP") + " : " + csv.get("METHOD_TYP") + " : ");
                        // Entity-Version erstellen
                        if (csev.getCodeSystemConcepts() == null)
                        {
                            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        }
                        else
                        {
                            csev.getCodeSystemConcepts().clear();
                        }
                        csev.getCodeSystemConcepts().add(csc);
                        csev.setEffectiveDate(parseDate(csv.get("DATE_LAST_CHANGED")));
                        csev.setStatus(1); //Fix laut Mail vom 25.06.2014 13:48

                        // Konzept speichern
                        if (csc.getCode().length() > 0)
                        {
                            // Entity-Version dem Request hinzufügen
                            request.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
                            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

                            // Dienst aufrufen (Konzept einfügen)
                            CreateConceptResponseType responseCC = cc.CreateConcept(request, hb_session);

                            if (responseCC.getReturnInfos().getStatus() == ReturnType.Status.OK)
                            {
                                count++;

                                if (responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                                {
                                    codesMap.put(csc.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());
                                }

                                // Metadaten zu diesem Konzept speichern
                                int mdCount = 0;

                                CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseCC.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                                mdCount = addMetadataToConcept(csv, csev_result.getVersionId(), hb_session, parameter.getCodeSystem().getId());

                                //System.out.println(count);
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(count));
                                logger.info("Fehler bei CreateConcept: "+responseCC.getReturnInfos().getMessage());
                                logger.info("Fehler bei CreateConcept: "+csc.getCode());
                            }
                        }
                        else
                        {
                            countFehler++;
                            errList.add(String.valueOf(count));
                            logger.info("Term ist nicht angegeben");
                            logger.info(csc.getTerm());
                        }

                        //Mimimum acceptable free memory you think your app needs 
                        //long minRunningMemory = (1024 * 1024);
                        Runtime runtime = Runtime.getRuntime();
                        if (StaticStatus.importCount % 200 == 0)
                        {
                            logger.debug("FreeMemory: " + runtime.freeMemory());

                            if (StaticStatus.importCount % 1000 == 0)
                            {
                                // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                                hb_session.flush();
                                hb_session.clear();
                            }
                            if (StaticStatus.importCount % 10000 == 0)
                            {
                                // sicherheitshalber aufrufen
                                System.gc();
                            }
                        }
                    }

                    for (String str : errList)
                    {

                        logger.info("-----Zeile: " + str + "-----\n");
                    }

                    if (count == 0)
                    {
                        hb_session.getTransaction().rollback();
                        response.getReturnInfos().setMessage("Keine Konzepte importiert.");
                    }
                    else
                    {
                        logger.debug("Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                        hb_session.getTransaction().commit();
                        countImported = count;
                        response.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    logger.error("Fehler beim Import der LOINC-Datei: " + ex.getMessage());
                    s = "Fehler beim Import der LOINC-Datei: " + ex.getLocalizedMessage();

                    try
                    {
                        hb_session.getTransaction().rollback();
                        logger.info("[ImportLOINC.java] Rollback durchgefuehrt!");
                    }
                    catch (Exception exRollback)
                    {
                        logger.info(exRollback.getMessage());
                        logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
                    }
                }
                finally
                {
                    // Session schließen
                    hb_session.close();
                }

            }
            catch (Exception ex)
            {
                //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
                s = "Fehler beim LOINC-Import: " + ex.getLocalizedMessage();
                ex.printStackTrace();
            }

            StaticStatus.importCount = 0;
            StaticStatus.importTotal = 0;

            logger.debug("ImportLOINC - fertig");

            //Store actual "Version"
            FileOutputStream fos;
            try
            {

                fos = new FileOutputStream(path);
                Writer out = new OutputStreamWriter(fos, "UTF8");
                out.write(new String(bytes, "UTF-8"));
                out.close();
            }
            catch (FileNotFoundException ex)
            {
                logger.error(ex);
            }
            catch (IOException ex)
            {
                logger.error(ex);
            }

            return s;
        }
        else
        { //Abgleich LOINC für Tab-Separated LOINC File!
            System.out.println("LOINC Import-Update gestartet: " + sdf.format(new Date()));
            //Get previous Version and actual Version as CSV
            String s = "";
            boolean err = false;
            StaticStatus.importCount = 0;
            StaticStatus.importTotal = 0;
            ArrayList<String> errList = new ArrayList<String>();
            int count = 0, countFehler = 0, newCount = 0;

            codesMap = new HashMap<String, Long>();

            CsvReader csvAct;
            CsvReader csvPrev;
            HashMap<String, String> prevLoinc = new HashMap<String, String>();
            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

            try
            {

                InputStream isAct = new ByteArrayInputStream(bytes);
                csvAct = new CsvReader(isAct, Charset.forName("UTF-8"));
                csvAct.setDelimiter(',');
                csvAct.setTextQualifier('"');
                csvAct.setUseTextQualifier(true);

                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                byte bytesPrev[] = new byte[(int) file.length()];
                fis.read(bytesPrev);
                InputStream isPrev = new ByteArrayInputStream(bytesPrev);
                csvPrev = new CsvReader(isPrev, Charset.forName("UTF-8"));
                csvPrev.setDelimiter(',');
                csvPrev.setTextQualifier('"');
                csvPrev.setUseTextQualifier(true);

                csvPrev.readHeaders();

                //Prepare HashMap to compare
                while (csvPrev.readRecord())
                {
                    prevLoinc.put(csvPrev.get(LOINC_NUM), csvPrev.getRawRecord());
                }
                csvPrev.close();

                csvAct.readHeaders();

                hb_session.getTransaction().begin();

                // Metadaten-Parameter lesen
                metadataParameterMap = new HashMap<String, MetadataParameter>();
                List<MetadataParameter> mpList = hb_session.createQuery("from MetadataParameter mp join fetch mp.codeSystem cs where cs.name='LOINC'").list();
                for (int i = 0; i < mpList.size(); ++i)
                {
                    metadataParameterMap.put(mpList.get(i).getParamName(), mpList.get(i));
                }

                CodeSystem cs_db = null;
                //check if cs exists if yes => new version if not
                if (parameter.getCodeSystem().getId() != null)
                {
                    cs_db = (CodeSystem) hb_session.get(CodeSystem.class, parameter.getCodeSystem().getId());
                }
                parameter.setCodeSystem(cs_db);

                while (csvAct.readRecord())
                {
                    String actKey = csvAct.get(LOINC_NUM);
                    StaticStatus.importCount++;

                    if (prevLoinc.containsKey(actKey))
                    { //Vorhanden => Check for update

                        String prevRaw = prevLoinc.get(actKey);
                        String actRaw = csvAct.getRawRecord();

                        if (!prevRaw.equals(actRaw))
                        { //Something has changed

                            String shortHqlString = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemConcepts csc where csc.code=:code";
                            Query shortQ = hb_session.createQuery(shortHqlString);
                            shortQ.setParameter("code", actKey);
                            List<CodeSystemEntityVersion> csevList = shortQ.list();

                            if (csevList != null && csevList.size() > 1)
                            { //more codes found
                                logger.info("Code mehrfach gefunden");

                                List<CodeSystemEntityVersion> tempList = new ArrayList<CodeSystemEntityVersion>();

                                for (CodeSystemEntityVersion csev : csevList)
                                {
                                    if (!csev.getCodeSystemMetadataValues().isEmpty())
                                    {
                                        if (csev.getCodeSystemMetadataValues().iterator().next().getMetadataParameter().getCodeSystem().getId() == parameter.getCodeSystem().getId())
                                        {
                                            tempList.add(csev);
                                        }
                                    }
                                }
                                csevList = tempList;

                                /*System.out.println("Code mehrfach gefunden");
                            String hqlString = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemConcepts csc join fetch csev.codeSystemMetadataValues csmv join fetch csmv.metadataParameter mp join fetch mp.codeSystem cs";
                            hqlString += " where cs.id=:cs_id and csc.code=:code";
                            Query q = hb_session.createQuery(hqlString);
                            q.setParameter("cs_id", parameter.getCodeSystem().getId());
                            q.setParameter("code", actKey);
                            csevList = q.list();*/
                            }

                            //Get CSEV and all Metadata for update
                            /*String hqlString = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemConcepts csc join fetch csev.codeSystemMetadataValues csmv join fetch csmv.metadataParameter mp join fetch mp.codeSystem cs";
                    hqlString += " where cs.id=:cs_id and csc.code=:code";
                    Query q = hb_session.createQuery(hqlString);
                    q.setParameter("cs_id", parameter.getCodeSystem().getId());
                    q.setParameter("code", actKey);
                    List<CodeSystemEntityVersion> csevList = q.list();*/
                            if (csevList != null && !csevList.isEmpty())
                            {
                                CodeSystemEntityVersion csev_db = null;
                                CodeSystemConcept csc_db = null;
                                try
                                {
                                    //Update CSEV
                                    csev_db = (CodeSystemEntityVersion) hb_session.load(CodeSystemEntityVersion.class, csevList.get(0).getVersionId());
                                    csev_db.setEffectiveDate(parseDate(csvAct.get("DATE_LAST_CHANGED")));
                                    csev_db.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                                    hb_session.update(csev_db);

                                    //Update CSC
                                    csc_db = (CodeSystemConcept) hb_session.load(CodeSystemConcept.class, csevList.get(0).getCodeSystemConcepts().iterator().next().getCodeSystemEntityVersionId());
                                    csc_db.setCode(actKey);
                                    csc_db.setTerm(csvAct.get("LONG_COMMON_NAME"));
                                    csc_db.setTermAbbrevation(csvAct.get("SHORTNAME"));

                                    //Matthias: change "|" to ":" for fully specified name
                                    csc_db.setDescription(csvAct.get("COMPONENT") + " : " + csvAct.get("PROPERTY") + " : "
                                            + csvAct.get("TIME_ASPCT") + " : " + csvAct.get("SYSTEM") + " : "
                                            + csvAct.get("SCALE_TYP") + " : " + csvAct.get("METHOD_TYP") + " : ");

                                    hb_session.update(csc_db);

                                    //Update Metadata
                                    HashMap<String, CodeSystemMetadataValue> csmvList = new HashMap<String, CodeSystemMetadataValue>();
                                    for (CodeSystemMetadataValue csmv : csevList.get(0).getCodeSystemMetadataValues())
                                    {
                                        csmvList.put(csmv.getMetadataParameter().getParamName(), csmv);
                                    }

                                    for (int i = 0; i < metadataFields.length; ++i)
                                    {
                                        String content = csvAct.get(metadataFields[i]);
                                        CodeSystemMetadataValue csmv = csmvList.get(metadataFields[i]);
                                        if (csmv != null)
                                        { //Update
                                            CodeSystemMetadataValue csmv_db = (CodeSystemMetadataValue) hb_session.load(CodeSystemMetadataValue.class, csmv.getId());
                                            csmv_db.setParameterValue(content);
                                            hb_session.update(csmv_db);
                                        }
                                        else //Noch nicht angelegt => Check if != ""
                                        {
                                            if (content.length() > 0)
                                            {
                                                //Neues CSMV plus link auf MP
                                                CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
                                                mv.setParameterValue(content);

                                                mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                                                mv.getCodeSystemEntityVersion().setVersionId((Long) csev_db.getVersionId());

                                                mv.setMetadataParameter(new MetadataParameter());
                                                MetadataParameter mp = null;
                                                if (metadataParameterMap.containsKey(metadataFields[i]))
                                                {
                                                    mp = metadataParameterMap.get(metadataFields[i]);
                                                }
                                                else
                                                {
                                                    //Create metadata_parameter
                                                    mp = new MetadataParameter();
                                                    mp.setParamName(metadataFields[i]);
                                                    mp.setCodeSystem(new CodeSystem());
                                                    mp.getCodeSystem().setId(parameter.getCodeSystem().getId());
                                                    hb_session.save(mp);
                                                    metadataParameterMap.put(metadataFields[i], mp);
                                                }
                                                mv.getMetadataParameter().setId(mp.getId());

                                                hb_session.save(mv);
                                            }
                                        }
                                    }
                                    count++;
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + StaticStatus.importCount);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + StaticStatus.importCount);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                logger.debug("Code nicht gefunden!");
                                logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + StaticStatus.importCount);
                            }
                        }
                        else
                        {
                            //System.out.println("LOINC Konzept(" + actKey + ") update nicht noetig:  " + countNr);
                        }
                    }
                    else
                    { //New entry!

                        //Testing
                        String hqlTest = "select csc from CodeSystemConcept csc";// join fetch csc.codeSystemEntityVersion csev";
                        hqlTest += " where csc.code=:code";
                        Query testQ = hb_session.createQuery(hqlTest);
                        testQ.setParameter("code", actKey);
                        //testQ.setReadOnly(true);
                        List<CodeSystemConcept> cscList = testQ.list();

                        if (cscList != null && cscList.size() > 0)
                        {
                            //found csc.code in db
                            String hqlFindCS = "select csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csv.codeSystem cs";
                            hqlFindCS += " where csev.versionId=:id AND cs.id=:csId";
                            Query qFindCS = hb_session.createQuery(hqlFindCS);
                            //qFindCS.setReadOnly(true);
                            qFindCS.setParameter("id", cscList.get(0).getCodeSystemEntityVersionId());
                            qFindCS.setParameter("csId", parameter.getCodeSystem().getId());
                            List<CodeSystemEntityVersion> csevList = qFindCS.list();
                            boolean stop = true;

                            //}
                            //if (true)
                            //  return null;
                            //Check if it is really new or if it is in DB but not in csv!
                            //Matthias add parameters directly to improve speed  
                            //remove commend here	
                            /*String hqlString = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemConcepts csc join fetch csev.codeSystemMetadataValues csmv join fetch csmv.metadataParameter mp join fetch mp.codeSystem cs";
                  //hqlString += " where cs.id=:cs_id and csc.code=:code";
                  hqlString += " where cs.id='" + parameter.getCodeSystem().getId() + "' and csc.code='" + actKey + "'";
									Query q = hb_session.createQuery(hqlString);
									//Matthias: readonly
									q.setReadOnly(true);
                  //q.setParameter("cs_id", parameter.getCodeSystem().getId());
                  //q.setParameter("code", actKey);
									System.out.println("Query with poor performance follows");
                  List<CodeSystemEntityVersion> csevList = q.list();*/
                            if (csevList != null && !csevList.isEmpty())
                            {

                                //Update instead
                                CodeSystemEntityVersion csev_db = null;
                                CodeSystemConcept csc_db = null;

                                try
                                {
                                    //Update CSEV
                                    csev_db = csevList.get(0);
                                    //csev_db = (CodeSystemEntityVersion)hb_session.load(CodeSystemEntityVersion.class, csevList.get(0).getVersionId());
                                    csev_db.setEffectiveDate(parseDate(csvAct.get("DATE_LAST_CHANGED")));
                                    csev_db.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                                    hb_session.update(csev_db);

                                    //Update CSC
                                    csc_db = (CodeSystemConcept) hb_session.load(CodeSystemConcept.class, csevList.get(0).getCodeSystemConcepts().iterator().next().getCodeSystemEntityVersionId());
                                    csc_db.setCode(actKey);
                                    csc_db.setTerm(csvAct.get("LONG_COMMON_NAME"));
                                    csc_db.setTermAbbrevation(csvAct.get("SHORTNAME"));

                                    //Matthias: change "|" to ":" for fully specified name
                                    csc_db.setDescription(csvAct.get("COMPONENT") + " : " + csvAct.get("PROPERTY") + " : "
                                            + csvAct.get("TIME_ASPCT") + " : " + csvAct.get("SYSTEM") + " : "
                                            + csvAct.get("SCALE_TYP") + " : " + csvAct.get("METHOD_TYP") + " : ");

                                    hb_session.update(csc_db);

                                    //!! Matthias: metadata check with poor performance
                                    //this might be a temporal measure
                                    //Update Metadata
                                    HashMap<String, CodeSystemMetadataValue> csmvList = new HashMap<String, CodeSystemMetadataValue>();
                                    for (CodeSystemMetadataValue csmv : csevList.get(0).getCodeSystemMetadataValues())
                                    {
                                        csmvList.put(csmv.getMetadataParameter().getParamName(), csmv);
                                    }

                                    for (int i = 0; i < metadataFields.length; ++i)
                                    {
                                        String content = csvAct.get(metadataFields[i]);
                                        CodeSystemMetadataValue csmv = csmvList.get(metadataFields[i]);
                                        if (csmv != null)
                                        { //Update
                                            CodeSystemMetadataValue csmv_db = (CodeSystemMetadataValue) hb_session.load(CodeSystemMetadataValue.class, csmv.getId());
                                            csmv_db.setParameterValue(content);
                                            hb_session.update(csmv_db);
                                        }
                                        else
                                        {//Noch nicht angelegt => Check if != ""
                                            if (content.length() > 0)
                                            {
                                                //Neues CSMV plus link auf MP
                                                CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
                                                mv.setParameterValue(content);

                                                mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                                                mv.getCodeSystemEntityVersion().setVersionId((Long) csev_db.getVersionId());

                                                mv.setMetadataParameter(new MetadataParameter());
                                                MetadataParameter mp = null;
                                                if (metadataParameterMap.containsKey(metadataFields[i]))
                                                {
                                                    mp = metadataParameterMap.get(metadataFields[i]);
                                                }
                                                else
                                                {
                                                    //Create metadata_parameter
                                                    mp = new MetadataParameter();
                                                    mp.setParamName(metadataFields[i]);
                                                    mp.setCodeSystem(new CodeSystem());
                                                    mp.getCodeSystem().setId(parameter.getCodeSystem().getId());
                                                    hb_session.save(mp);
                                                    metadataParameterMap.put(metadataFields[i], mp);
                                                }
                                                mv.getMetadataParameter().setId(mp.getId());

                                                hb_session.save(mv);
                                            }
                                        }
                                    }
                                    count++;
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + StaticStatus.importCount);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + StaticStatus.importCount);
                                }
                            }
                        }
                        else
                        {
                            //New CSC
                            CreateConceptRequestType request = new CreateConceptRequestType();
                            request.setLogin(parameter.getLogin());
                            //request.setCodeSystem(parameter.getCodeSystem());
                            request.setCodeSystem(new CodeSystem());
                            request.getCodeSystem().setId(parameter.getCodeSystem().getId());
                            request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
                            CodeSystemVersion csv_act = new CodeSystemVersion();
                            csv_act.setVersionId(parameter.getCodeSystem().getCurrentVersionId());
                            request.getCodeSystem().getCodeSystemVersions().add(csv_act);

                            request.setCodeSystemEntity(new CodeSystemEntity());
                            request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

                            CodeSystemConcept csc = new CodeSystemConcept();
                            csc.setIsPreferred(true);

                            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                            csev.setIsLeaf(true);

                            CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
                            membership.setIsMainClass(Boolean.TRUE);
                            membership.setIsAxis(Boolean.FALSE);

                            request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

                            CreateConcept cc = new CreateConcept();

                            //request.setCodeSystemEntity(new CodeSystemEntity());
                            //request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                            csc.setCode(actKey);
                            csc.setTerm(csvAct.get("LONG_COMMON_NAME"));
                            csc.setTermAbbrevation(csvAct.get("SHORTNAME"));

                            //Matthias: change "|" to ":" for fully specified name
                            csc.setDescription(csvAct.get("COMPONENT") + " : " + csvAct.get("PROPERTY") + " : "
                                    + csvAct.get("TIME_ASPCT") + " : " + csvAct.get("SYSTEM") + " : "
                                    + csvAct.get("SCALE_TYP") + " : " + csvAct.get("METHOD_TYP") + " : ");

                            // Entity-Version erstellen
                            if (csev.getCodeSystemConcepts() == null)
                            {
                                csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                            }
                            else
                            {
                                csev.getCodeSystemConcepts().clear();
                            }
                            csev.getCodeSystemConcepts().add(csc);
                            csev.setEffectiveDate(parseDate(csvAct.get("DATE_LAST_CHANGED")));
                            csev.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                            // Konzept speichern
                            if (csc.getCode().length() > 0)
                            {
                                // Entity-Version dem Request hinzufügen
                                request.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
                                request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

                                // Dienst aufrufen (Konzept einfügen)
                                CreateConceptResponseType responseCC = cc.CreateConcept(request, hb_session);

                                if (responseCC.getReturnInfos().getStatus() == ReturnType.Status.OK)
                                {
                                    newCount++;

                                    if (responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                                    {
                                        codesMap.put(csc.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());
                                    }

                                    // Metadaten zu diesem Konzept speichern
                                    int mdCount = 0;

                                    CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseCC.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                                    mdCount = addMetadataToConcept(csvAct, csev_result.getVersionId(), hb_session, parameter.getCodeSystem().getId());

                                    //System.out.println(count);
                                    logger.info("LOINC Konzept(" + actKey + ") neu erstellt: " + StaticStatus.importCount);
                                }
                                else
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Konzept konnte nicht erstellt werden: "+responseCC.getReturnInfos().getMessage());
                                    logger.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + StaticStatus.importCount);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                logger.debug("Term ist nicht angegeben");
                                logger.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + StaticStatus.importCount);
                            }
                        }
                    }

                    //Mimimum acceptable free memory you think your app needs 
                    //long minRunningMemory = (1024 * 1024);
                    Runtime runtime = Runtime.getRuntime();
                    if (StaticStatus.importCount % 100 == 0)
                    {
                        logger.debug("FreeMemory: " + runtime.freeMemory());

                        if (StaticStatus.importCount % 1000 == 0)
                        {
                            // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                            hb_session.flush();
                            hb_session.clear();
                        }
                        if (StaticStatus.importCount % 10000 == 0)
                        {
                            // sicherheitshalber aufrufen
                            System.gc();
                        }
                    }
                }
                csvAct.close();
                logger.debug("Update-Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                logger.info("countFehler: "+countFehler);
                if(countFehler == 0)
                {
            
                    LastChangeHelper.updateLastChangeDate(true, parameter.getCodeSystem().getCurrentVersionId(),hb_session);
                    hb_session.getTransaction().commit();
                    response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + count + " Konzept(en). " + newCount + " Konzepte wurden neu importiert. " + countFehler + " Fehler");
                }
                else
                {
                    logger.info("countFehler: "+countFehler);
                    response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + count + " Konzept(en). " + newCount + " Konzepte wurden neu importiert. " + countFehler + " Fehler;" + "Daten auf Grund der Fehler nicht in der Datenbank gespeichert. ");
                }
            }
                catch (Exception ex)
            {
                s = "Fehler beim LOINC-Import - Update: " + ex.getLocalizedMessage();
                err = true;

                try
                {
                    hb_session.getTransaction().rollback();
                    logger.info("[ImportLOINC.java] Rollback durchgefuehrt!");
                }
                catch (Exception exRollback)
                {
                    logger.info(exRollback.getMessage());
                    logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
                }
            }
            finally
            {
                // Session schließen
                logger.setLevel(Level.INFO);
                hb_session.close();
            }

            StaticStatus.importCount = 0;
            StaticStatus.importTotal = 0;

            logger.debug("ImportLOINC - Update - fertig");

            logger.info("ImportLOINC - Update - fertig: " + sdf.format(new Date()));
            //Store actual "Version" only if no error occured!
            if (countFehler == 0 && !err)
            {
                FileOutputStream fos;
                try
                {
                    fos = new FileOutputStream(path);
                    Writer out = new OutputStreamWriter(fos, "UTF8");
                    out.write(new String(bytes, "UTF-8"));
                    out.close();
                }
                catch (FileNotFoundException ex)
                {
                    logger.error(ex);
                }
                catch (IOException ex)
                {
                    logger.error(ex);
                }
            }
            return s;
        }
    }

    /**
     *
     *
     * @param s Datensatz in der Schreibweise JJJJMMTT
     * @return java.util.Date
     */
    private java.util.Date parseDate(String s)
    {
        if (s == null || s.length() == 0)
        {
            logger.debug("Fehler beim Parsen des Datums: nicht angegeben");
            return new java.util.Date();
        }

        try
        {
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            return (Date) formatter.parse(s);
        }
        catch (Exception e)
        {
            logger.warn("Fehler beim Parsen des Datums: " + s);
            return new java.util.Date();
        }
    }

    /**
     *
     * @param s STATUS-Feldinhalt
     * @return Terminologieserver-Status
     */
    private int getLoincStatus(String s)
    {
        // mögliche LOINC-Status: ACTIVE, DEPRECATED, DISCOURAGED, TRIAL
        if (s == null || s.length() == 0)
        {
            logger.debug("Fehler beim Lesen des Loinc-Status: nicht angegeben");
            return 0;
        }

        if (s.equals("ACTIVE"))
        {
            return 1;
        }
        else if (s.equals("DEPRECATED"))
        {
            return 2;
        }
        else if (s.equals("DISCOURAGED"))
        {
            return 3;
        }
        else if (s.equals("TRIAL"))
        {
            return 4;
        }

        return 0;
    }

    /**
     * Fügt alle oben angegebenen Metadaten zum Konzept hinzu
     *
     * @param csv
     * @param csevId Konzept-ID (Entity-Version-ID)
     * @param hb_session
     */
    private int addMetadataToConcept(CsvReader csv, long csevId, org.hibernate.Session hb_session, Long csId)
    {
        int mdCount = 0;
        try
        {
            for (int i = 0; i < metadataFields.length; ++i)
            {
                String content = csv.get(metadataFields[i]);

                if (content != null && content.length() > 0)
                {
                    MetadataParameter mp = getMetadataParameter(metadataFields[i], hb_session, csId);

                    if (mp != null && mp.getId() > 0)
                    {
                        CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
                        mv.setParameterValue(content);

                        mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                        mv.getCodeSystemEntityVersion().setVersionId((Long) csevId);

                        mv.setMetadataParameter(new MetadataParameter());
                        mv.getMetadataParameter().setId(mp.getId());

                        hb_session.save(mv);

                        mdCount++;
                    }
                }

            }
        }
        catch (Exception e)
        {
        }
        return mdCount;
    }

    private MetadataParameter getMetadataParameter(String name, org.hibernate.Session hb_session, Long csId)
    {
        if (metadataParameterMap.containsKey(name))
        {
            return metadataParameterMap.get(name);
        }
        else
        {
            //Create metadata_parameter
            MetadataParameter mp = new MetadataParameter();
            mp.setParamName(name);
            mp.setCodeSystem(new CodeSystem());
            mp.getCodeSystem().setId(csId);
            hb_session.save(mp);
            metadataParameterMap.put(name, mp);

            logger.debug("MetadataParameter in DB hinzugefuegt: " + name);

            return mp;
        }
    }
}
