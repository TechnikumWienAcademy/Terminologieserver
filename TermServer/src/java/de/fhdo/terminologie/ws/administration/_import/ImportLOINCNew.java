/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import com.csvreader.CsvReader;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.logger;
import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.hibernate.Query;

/**
 *
 * @author puraner
 */
public class ImportLOINCNew extends CodeSystemImport implements ICodeSystemImport
{
    private Map<String, Long> codesMap;
    private Map<String, MetadataParameter> metadataParameterMap;
    private final Integer LOINC_NUM = 0;
    private int count = 0, countFehler = 0, newCount = 0;
    
    //TODO move to configuration
    private String[] metadataFields =
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

    public ImportLOINCNew()
    {
        super();
    }

    @Override
    public void setImportData(ImportCodeSystemRequestType request)
    {
        logger.info("setImportData started");
        this.setImportId(request.getImportId());
        this.setLoginType(request.getLogin());
        this.setImportType(request.getImportInfos());

        this._codesystem = request.getCodeSystem();
        this._fileContent = request.getImportInfos().getFilecontent();
    }

    @Override
    public void startImport() throws ImportException, ImportParameterValidationException
    {
        logger.info("startImport started");
        //creating Hibernate Session and starting transaction
        try
        {
            this.validateParameters();
        }
        catch (ImportParameterValidationException ex)
        {
            logger.error(ex);
            throw ex;
        }

        this._status.setImportRunning(true);
        StaticStatusList.addStatus(this.getImportId(), this._status);

        String path = SysParameter.instance().getStringValue("LoincCsvPath", null, null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        byte[] bytes = this.getImportType().getFilecontent();
        if (!this.getImportType().getOrder())
        {

            ArrayList<String> errList = new ArrayList<String>();
            String s = "";

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
                
                this.setTotalCountInStatusList(numberOfLines, this.getImportId());
                is.close();
                csv.close();

                is = new ByteArrayInputStream(bytes);
                csv = new CsvReader(is, Charset.forName("UTF-8"));

                csv.setDelimiter(',');
                csv.setTextQualifier('"');
                csv.setUseTextQualifier(true);

                csv.readHeaders();
                logger.debug("Anzahl Header: " + csv.getHeaderCount());

                // Hibernate-Block, Session öffnen
                hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try // try-catch-Block zum Abfangen von Hibernate-Fehlern
                {

                    CodeSystem cs_db = null;
                    //check if cs exists if yes => new version if not
                    if (super.getCodeSystem().getId() != null)
                    {
                        cs_db = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());
                    }

                    CodeSystemVersion csv2 = new CodeSystemVersion();
                    csv2.setCodeSystem(cs_db);
                    Date d = new Date();
                    csv2.setInsertTimestamp(d);
                    csv2.setName(super.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
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
                    
                    CodeSystem cs = new CodeSystem();
                    cs.setId(cs_db.getId());
                    cs.setName(cs_db.getName());
                    cs.setCodeSystemVersions(cs_db.getCodeSystemVersions());
                    cs.setCurrentVersionId(cs_db.getCurrentVersionId());
                    
                    this.setCodeSystem(cs);

                    // Metadaten-Parameter lesen
                    metadataParameterMap = new HashMap<String, MetadataParameter>();
                    List<MetadataParameter> mpList = hb_session.createQuery("from MetadataParameter").list();
                    for (int i = 0; i < mpList.size(); ++i)
                    {
                        metadataParameterMap.put(mpList.get(i).getParamName(), mpList.get(i));
                    }

                    logger.debug("Starte Import...");

                    count = 0;
                    while (csv.readRecord())
                    {
                        count++;
                        this.setCurrentCountInStatusList(count, this.getImportId());

                        CreateConceptRequestType request = new CreateConceptRequestType();
                        request.setLogin(this.getLoginType());
                        request.setCodeSystem(super.getCodeSystem());
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

                        if (count % 200 == 0)
                        {
                            logger.debug("Lese Datensatz " + count + ", count: " + count);
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
                                if (responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                                {
                                    codesMap.put(csc.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());
                                }

                                // Metadaten zu diesem Konzept speichern
                                int mdCount = 0;

                                CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseCC.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                                mdCount = addMetadataToConcept(csv, csev_result.getVersionId(), hb_session, super.getCodeSystem().getId());

                                //System.out.println(count);
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(count));
                                logger.info("Fehler bei CreateConcept: " + responseCC.getReturnInfos().getMessage());
                                logger.info("Fehler bei CreateConcept: " + csc.getCode());
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
                        if (count % 200 == 0)
                        {
                            logger.debug("FreeMemory: " + runtime.freeMemory());

                            if (count % 1000 == 0)
                            {
                                // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                                hb_session.flush();
                                hb_session.clear();
                            }
                            if (count % 10000 == 0)
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
                        throw new ImportException("Keine Konzepte importiert.");
                    }
                    else
                    {
                        logger.debug("Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                        hb_session.getTransaction().commit();
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
                    finally
                    {
                        throw new ImportException(s);
                    }
                }
                finally
                {
                    // Session schließen
                    is.close();
                    hb_session.close();
                }

            }
            catch (Exception ex)
            {
                //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
                s = "Fehler beim LOINC-Import: " + ex.getLocalizedMessage();
                ex.printStackTrace();
                throw new ImportException(s);
            }

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
        }
        else
        { 
            //Abgleich LOINC für Tab-Separated LOINC File!
            System.out.println("LOINC Import-Update gestartet: " + sdf.format(new Date()));
            //Get previous Version and actual Version as CSV
            String s = "";
            boolean err = false;
            ArrayList<String> errList = new ArrayList<String>();
            count = 0;
            countFehler = 0;
            newCount = 0;

            codesMap = new HashMap<String, Long>();

            CsvReader csvAct;
            CsvReader csvPrev;
            HashMap<String, String> prevLoinc = new HashMap<String, String>();
            // Hibernate-Block, Session öffnen
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try
            {

                InputStream isAct = new ByteArrayInputStream(bytes);
                csvAct = new CsvReader(isAct, Charset.forName("UTF-8"));
                
                int numberOfLines = -1;

                while (csvAct.readRecord())
                {
                    numberOfLines++;
                }

                this.setTotalCountInStatusList(numberOfLines, this.getImportId());
                isAct.close();

                isAct = new ByteArrayInputStream(bytes);
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

                // Metadaten-Parameter lesen
                metadataParameterMap = new HashMap<String, MetadataParameter>();
                List<MetadataParameter> mpList = hb_session.createQuery("from MetadataParameter mp join fetch mp.codeSystem cs where cs.name='LOINC'").list();
                for (int i = 0; i < mpList.size(); ++i)
                {
                    metadataParameterMap.put(mpList.get(i).getParamName(), mpList.get(i));
                }

                CodeSystem cs_db = null;
                //check if cs exists if yes => new version if not
                if (super.getCodeSystem().getId() != null)
                {
                    cs_db = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());
                }

                this.setCodeSystem(cs_db);

                count = 0;
                while (csvAct.readRecord())
                {
                    count++;
                    this.setCurrentCountInStatusList(count, this.getImportId());
                    
                    String actKey = csvAct.get(LOINC_NUM);

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
                                        if (csev.getCodeSystemMetadataValues().iterator().next().getMetadataParameter().getCodeSystem().getId() == super.getCodeSystem().getId())
                                        {
                                            tempList.add(csev);
                                        }
                                    }
                                }
                                csevList = tempList;

                            }

                            //Get CSEV and all Metadata for update
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
                                                    mp.getCodeSystem().setId(super.getCodeSystem().getId());
                                                    hb_session.save(mp);
                                                    metadataParameterMap.put(metadataFields[i], mp);
                                                }
                                                mv.getMetadataParameter().setId(mp.getId());

                                                hb_session.save(mv);
                                            }
                                        }
                                    }
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + count);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                logger.debug("Code nicht gefunden!");
                                logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
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
                            qFindCS.setParameter("csId", super.getCodeSystem().getId());
                            List<CodeSystemEntityVersion> csevList = qFindCS.list();
                            boolean stop = true;

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
                                                    mp.getCodeSystem().setId(super.getCodeSystem().getId());
                                                    hb_session.save(mp);
                                                    metadataParameterMap.put(metadataFields[i], mp);
                                                }
                                                mv.getMetadataParameter().setId(mp.getId());

                                                hb_session.save(mv);
                                            }
                                        }
                                    }
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + count);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    logger.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
                                }
                            }
                        }
                        else
                        {
                            //New CSC
                            CreateConceptRequestType request = new CreateConceptRequestType();
                            request.setLogin(this.getLoginType());
                            //request.setCodeSystem(super.getCodeSystem());
                            request.setCodeSystem(new CodeSystem());
                            request.getCodeSystem().setId(super.getCodeSystem().getId());
                            request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
                            CodeSystemVersion csv_act = new CodeSystemVersion();
                            csv_act.setVersionId(super.getCodeSystem().getCurrentVersionId());
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
                                    mdCount = addMetadataToConcept(csvAct, csev_result.getVersionId(), hb_session, super.getCodeSystem().getId());

                                    //System.out.println(count);
                                    logger.info("LOINC Konzept(" + actKey + ") neu erstellt: " + count);
                                }
                                else
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    logger.debug("Konzept konnte nicht erstellt werden: " + responseCC.getReturnInfos().getMessage());
                                    logger.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + count);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                logger.debug("Term ist nicht angegeben");
                                logger.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + count);
                            }
                        }
                    }

                    //Mimimum acceptable free memory you think your app needs 
                    //long minRunningMemory = (1024 * 1024);
                    Runtime runtime = Runtime.getRuntime();
                    if (count% 100 == 0)
                    {
                        logger.debug("FreeMemory: " + runtime.freeMemory());

                        if (count % 1000 == 0)
                        {
                            // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                            hb_session.flush();
                            hb_session.clear();
                        }
                        if (count % 10000 == 0)
                        {
                            // sicherheitshalber aufrufen
                            System.gc();
                        }
                    }
                }
                csvAct.close();
                logger.debug("Update-Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                logger.info("countFehler: " + countFehler);
                if (countFehler == 0)
                {
                    //reload codesystem
                    if (super.getCodeSystem().getId() != null)
                    {
                        cs_db = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());
                    }

                    this.setCodeSystem(cs_db);
                    
                    CodeSystem cs_ret = new CodeSystem();
                    cs_ret.setId(super.getCodeSystem().getId());
                    cs_ret.setCurrentVersionId(super.getCodeSystem().getCurrentVersionId());
                    cs_ret.setName(super.getCodeSystem().getName());
                    cs_ret.setAutoRelease(super.getCodeSystem().getAutoRelease());

                    CodeSystemVersion csv_ret = new CodeSystemVersion();
                    csv_ret.setVersionId(super.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
                    csv_ret.setName(super.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                    cs_ret.getCodeSystemVersions().clear();
                    cs_ret.getCodeSystemVersions().add(csv_ret);

                    this.setCodeSystem(cs_ret);
                    
                    LastChangeHelper.updateLastChangeDate(true, super.getCodeSystem().getCurrentVersionId(), null);
                    
                    hb_session.getTransaction().commit();
                    //response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + count + " Konzept(en). " + newCount + " Konzepte wurden neu importiert. " + countFehler + " Fehler");
                }
                else
                {
                    logger.info("countFehler: " + countFehler);
                    //response.getReturnInfos().setMessage("Update-Import abgeschlossen. Update bei " + count + " Konzept(en). " + newCount + " Konzepte wurden neu importiert. " + countFehler + " Fehler;" + "Daten auf Grund der Fehler nicht in der Datenbank gespeichert. ");
                }
            }
            catch (Exception ex)
            {
                s = "Fehler beim LOINC-Import - Update: " + ex.getLocalizedMessage();
                err = true;

                try
                {
                    if(!hb_session.getTransaction().wasRolledBack()){
                        hb_session.getTransaction().rollback();
                        logger.info("[ImportLOINC.java] Rollback durchgefuehrt!");
                    }
                }
                catch (Exception exRollback)
                {
                    logger.info(exRollback.getMessage());
                    logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
                }
                finally
                {
                    throw new ImportException(s);
                }
                
            }
            finally
            {
                // Session schließen
                hb_session.close();
            }

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
    
    public int getCountFehler()
    {
        return countFehler;
    }
    
    public int getNewCount()
    {
        return newCount;
    }
    
    @Override
    public CodeSystem getCodeSystem()
    {
        //method is needed to get CodeSystem for ImportCodeSystem.ImportCodeSystem
        //because codesystem must not contain codesystemversions with information about codesystems
        //otherwhise a xml circle would be created and an exception thrown
        /*
        hb_session = HibernateUtil.getSessionFactory().openSession();
        
        CodeSystem cs_db = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());
        Hibernate.initialize(cs_db);
        
        for(CodeSystemVersion version : cs_db.getCodeSystemVersions())
        {
            version.setCodeSystem(new CodeSystem());
        }
        
        hb_session.close();
        
        return cs_db;
        */
        
        
        CodeSystem cs = super.getCodeSystem();
        
        for(CodeSystemVersion version : cs.getCodeSystemVersions())
        {
            version.setCodeSystem(new CodeSystem());
        }
        
        return cs;
    }
}
