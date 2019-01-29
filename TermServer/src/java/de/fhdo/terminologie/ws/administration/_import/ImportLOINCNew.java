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
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.LOGGER;
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
import org.hibernate.HibernateException;
import org.hibernate.Query;

/**
 *
 * @author Stefan Puraner
 */
public class ImportLOINCNew extends CodeSystemImport implements ICodeSystemImport{
    
    private Map<String, Long> codesMap;
    private Map<String, MetadataParameter> metadataParameterMap;
    private final Integer LOINC_NUM = 0;
    private int count = 0, countFehler = 0, newCount = 0;
    
    //TODO Move to configuration
    private String[] metadataFields = {
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

    public ImportLOINCNew(){
        super();
    }

    @Override
    public void setImportData(ImportCodeSystemRequestType request){
        this.setImportId(request.getImportId());
        this.setLoginType(request.getLogin());
        this.setImportType(request.getImportInfos());

        this.codesystem = request.getCodeSystem();
        this.fileContent = request.getImportInfos().getFilecontent();
    }

    @Override
    public void startImport() throws ImportException, ImportParameterValidationException{
        LOGGER.info("+++++ startImport started +++++");
        
        try{
            this.validateParameters();
        }
        catch (ImportParameterValidationException ex){
            LOGGER.error("Error [0020]: " + ex.getLocalizedMessage());
            throw ex;
        }

        this.status.setImportRunning(true);
        StaticStatusList.addStatus(this.getImportId(), this.status);

        String LOINCpath = SysParameter.instance().getStringValue("LoincCsvPath", null, null);
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        byte[] bytes = this.getImportType().getFilecontent();
        
        if (!this.getImportType().getOrder()){
            ArrayList<String> errorList = new ArrayList<String>();
            codesMap = new HashMap<String, Long>();
            CsvReader CSV;
            
            try{
                LOGGER.debug("Creating inputstream");
                InputStream inputStream = new ByteArrayInputStream(bytes);
                CSV = new CsvReader(inputStream, Charset.forName("UTF-8"));

                int numberOfLines = -1;

                while (CSV.readRecord())
                    numberOfLines++;
                
                this.setTotalCountInStatusList(numberOfLines, this.getImportId());
                inputStream.close();
                CSV.close();

                inputStream = new ByteArrayInputStream(bytes);
                CSV = new CsvReader(inputStream, Charset.forName("UTF-8"));

                CSV.setDelimiter(',');
                CSV.setTextQualifier('"');
                CSV.setUseTextQualifier(true);

                CSV.readHeaders();
                LOGGER.debug("Number of headers: " + CSV.getHeaderCount());

                hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try{
                    CodeSystem CS_DB = null;
                    
                    //Check if CS exists, if yes => new version
                    if (super.getCodeSystem().getId() != null){
                        CS_DB = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());
                    }

                    CodeSystemVersion newCS_Version = new CodeSystemVersion();
                    Date d = new Date();
                    newCS_Version.setCodeSystem(CS_DB);
                    newCS_Version.setInsertTimestamp(d);
                    newCS_Version.setName(super.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                    newCS_Version.setPreviousVersionId(CS_DB.getCurrentVersionId());
                    newCS_Version.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                    newCS_Version.setStatusDate(d);
                    newCS_Version.setPreferredLanguageId(33l);
                    newCS_Version.setReleaseDate(d);
                    newCS_Version.setUnderLicence(false);
                    newCS_Version.setValidityRange(236l);
                    newCS_Version.setOid("2.16.840.1.113883.6.1");
                    hb_session.save(newCS_Version);
                    
                    CS_DB.setCurrentVersionId(newCS_Version.getVersionId());
                    CS_DB.getCodeSystemVersions().add(newCS_Version);
                    hb_session.update(CS_DB);
                    
                    CodeSystem newCS = new CodeSystem();
                    newCS.setId(CS_DB.getId());
                    newCS.setName(CS_DB.getName());
                    CS_DB.getCodeSystemVersions().clear();
                    CS_DB.getCodeSystemVersions().add(newCS_Version);
                    newCS.setCodeSystemVersions(CS_DB.getCodeSystemVersions());
                    newCS.setCurrentVersionId(CS_DB.getCurrentVersionId());
                    
                    this.setCodeSystem(newCS);

                    //Reading metadata-parameters
                    metadataParameterMap = new HashMap<String, MetadataParameter>();
                    List<MetadataParameter> metadataParameterList = hb_session.createQuery("from MetadataParameter").list();
                    for(MetadataParameter metadataParameter : metadataParameterList) 
                        metadataParameterMap.put(metadataParameter.getParamName(), metadataParameter);
                    
                    LOGGER.debug("Starting import");
                    count = 0;
                    while (CSV.readRecord()){
                        count++;
                        this.setCurrentCountInStatusList(count, this.getImportId());

                        CreateConceptRequestType request = new CreateConceptRequestType();
                        request.setLogin(this.getLoginType());
                        request.setCodeSystem(super.getCodeSystem());
                        request.setCodeSystemEntity(new CodeSystemEntity());
                        request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

                        CodeSystemConcept CSconcept = new CodeSystemConcept();
                        CSconcept.setIsPreferred(true);

                        CodeSystemEntityVersion CSentityVersion = new CodeSystemEntityVersion();
                        CSentityVersion.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        CSentityVersion.setIsLeaf(true);

                        CodeSystemVersionEntityMembership CSVmembership = new CodeSystemVersionEntityMembership();
                        CSVmembership.setIsMainClass(Boolean.TRUE);
                        CSVmembership.setIsAxis(Boolean.FALSE);

                        request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                        request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(CSVmembership);

                        CreateConcept createConcept = new CreateConcept();

                        if (count % 100 == 0)
                            LOGGER.debug("Lines read: " + count);
                        
                        //Setting CSconcept data
                        CSconcept.setCode(CSV.get(LOINC_NUM));
                        CSconcept.setTerm(CSV.get("LONG_COMMON_NAME"));
                        CSconcept.setTermAbbrevation(CSV.get("SHORTNAME"));
                        CSconcept.setDescription(CSV.get("COMPONENT") + " : " + CSV.get("PROPERTY") + " : "
                                + CSV.get("TIME_ASPCT") + " : " + CSV.get("SYSTEM") + " : "
                                + CSV.get("SCALE_TYP") + " : " + CSV.get("METHOD_TYP") + " : ");
                        
                        if (CSentityVersion.getCodeSystemConcepts() == null)
                            CSentityVersion.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        else
                            CSentityVersion.getCodeSystemConcepts().clear();
                        
                        //Adding CSconcept to CSentityVersion
                        CSentityVersion.getCodeSystemConcepts().add(CSconcept);
                        CSentityVersion.setEffectiveDate(parseDate(CSV.get("DATE_LAST_CHANGED")));
                        CSentityVersion.setStatus(1);

                        //Save concept
                        if(CSconcept.getCode().length() > 0){
                            
                            request.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
                            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(CSentityVersion);

                            CreateConceptResponseType responseCC = createConcept.CreateConcept(request, hb_session);

                            if (responseCC.getReturnInfos().getStatus() == ReturnType.Status.OK){
                                if (responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                                    codesMap.put(CSconcept.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());

                                //Saving concept's metadata
                                CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseCC.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                                addMetadataToConcept(CSV, csev_result.getVersionId(), hb_session, super.getCodeSystem().getId());
                            }
                            else{
                                countFehler++;
                                errorList.add(String.valueOf(count));
                                LOGGER.info("Failed to create concept [0030]: " + responseCC.getReturnInfos().getMessage());
                                LOGGER.info("Failed code [0030]: " + CSconcept.getCode());
                            }
                        }
                        else{
                            countFehler++;
                            errorList.add(String.valueOf(count));
                            LOGGER.info("Term is missing [0031]: " + CSconcept.getTerm());
                        }

                        //Freeing memory
                        Runtime runtime = Runtime.getRuntime();
                        if (count % 200 == 0){
                            LOGGER.debug("FreeMemory: " + runtime.freeMemory());

                            if (count % 1000 == 0){
                                hb_session.flush();
                                hb_session.clear();
                            }
                            if (count % 10000 == 0)
                                System.gc();
                        }
                    }

                    for (String errorEntry : errorList)
                        LOGGER.info("Failed importing line: " + errorEntry);

                    if (count == 0){
                        if(!hb_session.getTransaction().wasRolledBack())
                            hb_session.getTransaction().rollback();
                        throw new ImportException("Keine Konzepte importiert.");
                    }
                    else{
                        LOGGER.debug("Import finished, number of read concepts: " + count);
                        hb_session.getTransaction().commit();
                    }
                }
                catch (IOException ex){
                    LOGGER.error("An error occured while importing the LOINC [0032]: " + ex.getLocalizedMessage());
                    try{
                        if(!hb_session.getTransaction().wasRolledBack()){
                            hb_session.getTransaction().rollback();
                            LOGGER.info("Rollback executed");
                        }
                    }
                    catch (Exception exRollback){
                        if(!hb_session.getTransaction().wasRolledBack())
                            LOGGER.info("Rollback failed [0033]: " + exRollback.getLocalizedMessage());
                    }
                } 
                catch (HibernateException ex){
                    LOGGER.error("An error occured while importing the LOINC [0034]: " + ex.getLocalizedMessage());
                    try{
                        if(!hb_session.getTransaction().wasRolledBack()){
                            hb_session.getTransaction().rollback();
                            LOGGER.info("Rollback executed");
                        }
                    }
                    catch (Exception exRollback){
                        if(!hb_session.getTransaction().wasRolledBack())
                            LOGGER.info("Rollback failed [0035]: " + exRollback.getLocalizedMessage());
                    }
                } 
                catch (ImportException ex){
                    LOGGER.error("An error occured while importing the LOINC [0036]: " + ex.getLocalizedMessage());
                    try{
                        if(!hb_session.getTransaction().wasRolledBack()){
                            hb_session.getTransaction().rollback();
                            LOGGER.info("Rollback executed");
                        }
                    }
                    catch (Exception exRollback){
                        if(!hb_session.getTransaction().wasRolledBack())
                            LOGGER.info("Rollback failed [0037]: " + exRollback.getLocalizedMessage());
                    }
                }
                finally{
                    inputStream.close();
                    if(hb_session.isOpen())
                        hb_session.close();
                }
            }
            catch (IOException ex){
                LOGGER.error("An error occured while importing the LOINC [0038]: " + ex.getLocalizedMessage());
                throw new ImportException(ex.getLocalizedMessage());
            } catch (HibernateException ex) {
                LOGGER.error("An error occured while importing the LOINC [0039]: " + ex.getLocalizedMessage());
                throw new ImportException(ex.getLocalizedMessage());
            }

            //Storing LOINC file
            FileOutputStream outputStream;
            try{
                outputStream = new FileOutputStream(LOINCpath);
                Writer out = new OutputStreamWriter(outputStream, "UTF8");
                out.write(new String(bytes, "UTF-8"));
                out.close();
            }
            catch (FileNotFoundException ex){
                LOGGER.error("Error [0040]: " + ex.getLocalizedMessage());
            }
            catch (IOException ex){
                LOGGER.error("Error [0041]: " + ex.getLocalizedMessage());
            }
        }
        else{ 
            //ANCHOR
            //Abgleich LOINC für Tab-Separated LOINC File!
            System.out.println("LOINC Import-Update gestartet: " + dateformat.format(new Date()));
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

                File file = new File(LOINCpath);
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
                                LOGGER.info("Code mehrfach gefunden");

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
                                    LOGGER.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + count);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    LOGGER.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    LOGGER.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                LOGGER.debug("Code nicht gefunden!");
                                LOGGER.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
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
                                    LOGGER.info("LOINC Konzept(" + actKey + ") update durchgefuehrt:  " + count);
                                }
                                catch (Exception e)
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    LOGGER.debug("Fehler im Update-Import Loinc: Vergleich zweier Einträge fehlerhaft!");
                                    LOGGER.info("LOINC Konzept(" + actKey + ") update durchgefuehrt FEHLER: " + count);
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
                                    LOGGER.info("LOINC Konzept(" + actKey + ") neu erstellt: " + count);
                                }
                                else
                                {
                                    countFehler++;
                                    errList.add(String.valueOf(newCount));
                                    LOGGER.debug("Konzept konnte nicht erstellt werden: " + responseCC.getReturnInfos().getMessage());
                                    LOGGER.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + count);
                                }
                            }
                            else
                            {
                                countFehler++;
                                errList.add(String.valueOf(newCount));
                                LOGGER.debug("Term ist nicht angegeben");
                                LOGGER.info("LOINC Konzept(" + actKey + ") neu erstellt FEHLER: " + count);
                            }
                        }
                    }

                    //Mimimum acceptable free memory you think your app needs 
                    //long minRunningMemory = (1024 * 1024);
                    Runtime runtime = Runtime.getRuntime();
                    if (count% 100 == 0)
                    {
                        LOGGER.debug("FreeMemory: " + runtime.freeMemory());

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
                LOGGER.debug("Update-Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
                LOGGER.info("countFehler: " + countFehler);
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
                    LOGGER.info("countFehler: " + countFehler);
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
                        LOGGER.info("[ImportLOINC.java] Rollback durchgefuehrt!");
                    }
                }
                catch (Exception exRollback)
                {
                    if(!hb_session.getTransaction().wasRolledBack()){
                        LOGGER.info(exRollback.getMessage());
                        LOGGER.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
                    }
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

            LOGGER.debug("ImportLOINC - Update - fertig");

            LOGGER.info("ImportLOINC - Update - fertig: " + dateformat.format(new Date()));
            //Store actual "Version" only if no error occured!
            if (countFehler == 0 && !err)
            {
                FileOutputStream fos;
                try
                {
                    fos = new FileOutputStream(LOINCpath);
                    Writer out = new OutputStreamWriter(fos, "UTF8");
                    out.write(new String(bytes, "UTF-8"));
                    out.close();
                }
                catch (FileNotFoundException ex)
                {
                    LOGGER.error(ex);
                }
                catch (IOException ex)
                {
                    LOGGER.error(ex);
                }
            }
        }
    }
    
    /**
     * TODO
     * @param tobeParsed
     * @return 
     */
    private java.util.Date parseDate(String tobeParsed){
        if (tobeParsed == null || tobeParsed.length() == 0){
            LOGGER.debug("No date given to parse");
            return new java.util.Date();
        }

        try{
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            return (Date) formatter.parse(tobeParsed);
        }
        catch (Exception e){
            LOGGER.warn("Error parsing " + tobeParsed + " " + e.getLocalizedMessage());
            return new java.util.Date();
        }
    }
    
    /**
     * TODO
     * @param csv
     * @param csevId
     * @param hb_session
     * @param csId
     * @return 
     */
    private int addMetadataToConcept(CsvReader csv, long csevId, org.hibernate.Session hb_session, Long csId){
        int metadataCount = 0;
        try{
            for (int i = 0; i < metadataFields.length; ++i){
                String content = csv.get(metadataFields[i]);

                if (content != null && content.length() > 0){
                    MetadataParameter metadataParameter = getMetadataParameter(metadataFields[i], hb_session, csId);

                    if (metadataParameter != null && metadataParameter.getId() > 0){
                        CodeSystemMetadataValue metadataValue = new CodeSystemMetadataValue();
                        metadataValue.setParameterValue(content);

                        metadataValue.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                        metadataValue.getCodeSystemEntityVersion().setVersionId((Long) csevId);

                        metadataValue.setMetadataParameter(new MetadataParameter());
                        metadataValue.getMetadataParameter().setId(metadataParameter.getId());

                        hb_session.save(metadataValue);

                        metadataCount++;
                    }
                }
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0040]: " + e.getLocalizedMessage());
        }
        return metadataCount;
    }
    
    /**
     * Gets the metadataparameter if it exists and creates it otherwise
     * @param name the parameters name
     * @param hb_session the hibernate session to be used
     * @param csId the code-system's ID
     * @return the metadataparameter
     */
    private MetadataParameter getMetadataParameter(String name, org.hibernate.Session hb_session, Long csId){
        if (metadataParameterMap.containsKey(name))
            return metadataParameterMap.get(name);
        else{
            //Creating metadata parameter
            MetadataParameter metadataParameter = new MetadataParameter();
            metadataParameter.setParamName(name);
            metadataParameter.setCodeSystem(new CodeSystem());
            metadataParameter.getCodeSystem().setId(csId);
            hb_session.save(metadataParameter);
            metadataParameterMap.put(name, metadataParameter);

            LOGGER.debug("Added metadataparameter to the database: " + name);
            return metadataParameter;
        }
    }
    
    /**
     * Returns the countFehler
     * @return the countFehler
     */
    public int getCountFehler(){
        return countFehler;
    }
    
    /**
     * Returns the newCount
     * @return the newCount
     */
    public int getNewCount(){
        return newCount;
    }
    
    /**
     * Fetches the code system of the class.
     * @return the code system
     */
    @Override
    public CodeSystem getCodeSystem(){
        CodeSystem CS = super.getCodeSystem();
        
        for(CodeSystemVersion CSversion : CS.getCodeSystemVersions())
            CSversion.setCodeSystem(new CodeSystem());
        
        return CS;
    }
}