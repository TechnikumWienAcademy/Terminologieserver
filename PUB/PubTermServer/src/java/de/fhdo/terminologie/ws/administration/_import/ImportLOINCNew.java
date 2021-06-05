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
            LOGGER.info("LOINC update-import started");
            
            //Get previous version and current version as CSV
            boolean error = false;
            ArrayList<String> errorList = new ArrayList<String>();
            count = 0;
            countFehler = 0;
            newCount = 0;
            codesMap = new HashMap<String, Long>();

            CsvReader currentCSVfile;
            CsvReader previousCSVfile;
            HashMap<String, String> previousLOINC = new HashMap<String, String>();
            
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try{
                InputStream currentInputStream = new ByteArrayInputStream(bytes);
                currentCSVfile = new CsvReader(currentInputStream, Charset.forName("UTF-8"));
                
                int numberOfLines = -1;
                while (currentCSVfile.readRecord())
                    numberOfLines++;

                this.setTotalCountInStatusList(numberOfLines, this.getImportId());
                currentInputStream.close();

                currentInputStream = new ByteArrayInputStream(bytes);
                currentCSVfile = new CsvReader(currentInputStream, Charset.forName("UTF-8"));
                
                currentCSVfile.setDelimiter(',');
                currentCSVfile.setTextQualifier('"');
                currentCSVfile.setUseTextQualifier(true);

                File file = new File(LOINCpath);
                FileInputStream fileInputStream = new FileInputStream(file);
                byte bytesPrev[] = new byte[(int) file.length()];
                fileInputStream.read(bytesPrev);
                InputStream previousInputStream = new ByteArrayInputStream(bytesPrev);
                previousCSVfile = new CsvReader(previousInputStream, Charset.forName("UTF-8"));
                previousCSVfile.setDelimiter(',');
                previousCSVfile.setTextQualifier('"');
                previousCSVfile.setUseTextQualifier(true);

                previousCSVfile.readHeaders();

                //Preparing HashMap to compare
                while (previousCSVfile.readRecord()) 
                    previousLOINC.put(previousCSVfile.get(LOINC_NUM), previousCSVfile.getRawRecord());
               
                previousCSVfile.close();

                currentCSVfile.readHeaders();

                //Reading metadata parameters
                metadataParameterMap = new HashMap<String, MetadataParameter>();
                List<MetadataParameter> metadataParameterList = hb_session.createQuery("from MetadataParameter mp join fetch mp.codeSystem cs where cs.name='LOINC'").list();
                for (MetadataParameter metadataParameter : metadataParameterList) 
                    metadataParameterMap.put(metadataParameter.getParamName(), metadataParameter);

                CodeSystem CS_DB = null;
                //Check if CS exists if yes => new version
                if (super.getCodeSystem().getId() != null)
                    CS_DB = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());

                this.setCodeSystem(CS_DB);

                count = 0;
                while (currentCSVfile.readRecord()){
                    count++;
                    this.setCurrentCountInStatusList(count, this.getImportId());
                    
                    String currentKey = currentCSVfile.get(LOINC_NUM);

                    if (previousLOINC.containsKey(currentKey)){ 
                        //Code exists, checking for updates

                        String previousConcept = previousLOINC.get(currentKey);
                        String currentConcept = currentCSVfile.getRawRecord();

                        if (!previousConcept.equals(currentConcept)){ 
                            //Something has changed

                            Query Q_CSEV_search = hb_session.createQuery("select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemConcepts csc where csc.code=:code");
                            Q_CSEV_search.setParameter("code", currentKey);
                            List<CodeSystemEntityVersion> CSEVList = Q_CSEV_search.list();

                            if (CSEVList != null && CSEVList.size() > 1){
                                LOGGER.info("Code found multiple times");

                                List<CodeSystemEntityVersion> tempList = new ArrayList<CodeSystemEntityVersion>();
                                for (CodeSystemEntityVersion CSEV : CSEVList){
                                    if (!CSEV.getCodeSystemMetadataValues().isEmpty())
                                        if (CSEV.getCodeSystemMetadataValues().iterator().next().getMetadataParameter().getCodeSystem().getId().equals(super.getCodeSystem().getId()))
                                            tempList.add(CSEV);
                                }
                                CSEVList = tempList;
                            }

                            //Get CSEV and all metadata for update
                            if (CSEVList != null && !CSEVList.isEmpty()){
                                CodeSystemEntityVersion CSEV_DB;
                                CodeSystemConcept CSC_DB;
                                try{
                                    //Updating CSEV
                                    CSEV_DB = (CodeSystemEntityVersion) hb_session.load(CodeSystemEntityVersion.class, CSEVList.get(0).getVersionId());
                                    CSEV_DB.setEffectiveDate(parseDate(currentCSVfile.get("DATE_LAST_CHANGED")));
                                    CSEV_DB.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                                    hb_session.update(CSEV_DB);

                                    //Updating CSC
                                    CSC_DB = (CodeSystemConcept) hb_session.load(CodeSystemConcept.class, CSEVList.get(0).getCodeSystemConcepts().iterator().next().getCodeSystemEntityVersionId());
                                    CSC_DB.setCode(currentKey);
                                    CSC_DB.setTerm(currentCSVfile.get("LONG_COMMON_NAME"));
                                    CSC_DB.setTermAbbrevation(currentCSVfile.get("SHORTNAME"));
                                    CSC_DB.setDescription(currentCSVfile.get("COMPONENT") + " : " + currentCSVfile.get("PROPERTY") + " : "
                                            + currentCSVfile.get("TIME_ASPCT") + " : " + currentCSVfile.get("SYSTEM") + " : "
                                            + currentCSVfile.get("SCALE_TYP") + " : " + currentCSVfile.get("METHOD_TYP") + " : ");

                                    hb_session.update(CSC_DB);

                                    //Updating metadata
                                    HashMap<String, CodeSystemMetadataValue> CSMVList = new HashMap<String, CodeSystemMetadataValue>();
                                    for (CodeSystemMetadataValue CSMV : CSEVList.get(0).getCodeSystemMetadataValues())
                                        CSMVList.put(CSMV.getMetadataParameter().getParamName(), CSMV);

                                    for (String metadataField : metadataFields) {
                                        String content = currentCSVfile.get(metadataField);
                                        CodeSystemMetadataValue CSMV = CSMVList.get(metadataField);
                                        if (CSMV != null) {
                                            //Updating
                                            CodeSystemMetadataValue CSMV_DB = (CodeSystemMetadataValue) hb_session.load(CodeSystemMetadataValue.class, CSMV.getId());
                                            CSMV_DB.setParameterValue(content);
                                            hb_session.update(CSMV_DB);
                                        } 
                                        else {
                                            //Not yet created, check if != ""
                                            if (content.length() > 0) {
                                                //New CSMV and link to metadata parameter
                                                CodeSystemMetadataValue metadataValue = new CodeSystemMetadataValue();
                                                metadataValue.setParameterValue(content);
                                                metadataValue.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                                                metadataValue.getCodeSystemEntityVersion().setVersionId(CSEV_DB.getVersionId());
                                                metadataValue.setMetadataParameter(new MetadataParameter());
                                                MetadataParameter metadataParameter;
                                                if (metadataParameterMap.containsKey(metadataField))
                                                    metadataParameter = metadataParameterMap.get(metadataField); 
                                                else {
                                                    //Creating metadata_parameter
                                                    metadataParameter = new MetadataParameter();
                                                    metadataParameter.setParamName(metadataField);
                                                    metadataParameter.setCodeSystem(new CodeSystem());
                                                    metadataParameter.getCodeSystem().setId(super.getCodeSystem().getId());
                                                    
                                                    hb_session.save(metadataParameter);
                                                    metadataParameterMap.put(metadataField, metadataParameter);
                                                }
                                                
                                                metadataValue.getMetadataParameter().setId(metadataParameter.getId());
                                                hb_session.save(metadataValue);
                                            }
                                        }
                                    }
                                    LOGGER.info("LOINC concept (" + currentKey + ") update executed:  " + count);
                                }
                                catch (Exception e){
                                    countFehler++;
                                    errorList.add(String.valueOf(newCount));
                                    LOGGER.error("Error [0043], LOINC concept (" + currentKey + ") update failed comparing two entries (" + count + "): " + e.getLocalizedMessage());
                                }
                            }
                            else{
                                countFehler++;
                                errorList.add(String.valueOf(newCount));
                                LOGGER.info("Error [0044], LOINC concept (" + currentKey + ") update failed, code not found (" + count + ")");
                            }
                        }
                    }
                    else{ 
                        //New entry
                        Query Q_CSconcept_search = hb_session.createQuery("select csc from CodeSystemConcept csc where csc.code=:code");
                        Q_CSconcept_search.setParameter("code", currentKey);
                        List<CodeSystemConcept> CSCList = Q_CSconcept_search.list();

                        if (CSCList != null && CSCList.size() > 0){
                            //Found CSconcept in database
                            String HQL_CSentityVersion_search = "select csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csv.codeSystem cs";
                            HQL_CSentityVersion_search += " where csev.versionId=:id AND cs.id=:csId";
                            Query Q_CSentityVersion_search = hb_session.createQuery(HQL_CSentityVersion_search);
                            Q_CSentityVersion_search.setParameter("id", CSCList.get(0).getCodeSystemEntityVersionId());
                            Q_CSentityVersion_search.setParameter("csId", super.getCodeSystem().getId());
                            List<CodeSystemEntityVersion> CSEVList = Q_CSentityVersion_search.list();

                            if (CSEVList != null && !CSEVList.isEmpty()){
                                //Updating instead
                                CodeSystemEntityVersion CSEV_DB;
                                CodeSystemConcept CSC_DB;

                                try{
                                    //Updating CSEV
                                    CSEV_DB = CSEVList.get(0);
                                    CSEV_DB.setEffectiveDate(parseDate(currentCSVfile.get("DATE_LAST_CHANGED")));
                                    CSEV_DB.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                                    hb_session.update(CSEV_DB);

                                    //Updating CSC
                                    CSC_DB = (CodeSystemConcept) hb_session.load(CodeSystemConcept.class, CSEVList.get(0).getCodeSystemConcepts().iterator().next().getCodeSystemEntityVersionId());
                                    CSC_DB.setCode(currentKey);
                                    CSC_DB.setTerm(currentCSVfile.get("LONG_COMMON_NAME"));
                                    CSC_DB.setTermAbbrevation(currentCSVfile.get("SHORTNAME"));
                                    CSC_DB.setDescription(currentCSVfile.get("COMPONENT") + " : " + currentCSVfile.get("PROPERTY") + " : "
                                            + currentCSVfile.get("TIME_ASPCT") + " : " + currentCSVfile.get("SYSTEM") + " : "
                                            + currentCSVfile.get("SCALE_TYP") + " : " + currentCSVfile.get("METHOD_TYP") + " : ");

                                    hb_session.update(CSC_DB);

                                    //Updating Metadata
                                    HashMap<String, CodeSystemMetadataValue> CSMVList = new HashMap<String, CodeSystemMetadataValue>();
                                    for (CodeSystemMetadataValue CSMV : CSEVList.get(0).getCodeSystemMetadataValues())
                                        CSMVList.put(CSMV.getMetadataParameter().getParamName(), CSMV);

                                    for (String metadataField : metadataFields) {
                                        String content = currentCSVfile.get(metadataField);
                                        CodeSystemMetadataValue CSMV = CSMVList.get(metadataField);
                                        if (CSMV != null) {
                                            //Updating
                                            CodeSystemMetadataValue CSMV_DB = (CodeSystemMetadataValue) hb_session.load(CodeSystemMetadataValue.class, CSMV.getId());
                                            CSMV_DB.setParameterValue(content);
                                            hb_session.update(CSMV_DB);
                                        } 
                                        else {
                                            //Not yet created, check if != ""
                                            if (content.length() > 0) {
                                                //New CSMV and link to metadataparameter
                                                CodeSystemMetadataValue metadataValue = new CodeSystemMetadataValue();
                                                metadataValue.setParameterValue(content);
                                                metadataValue.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                                                metadataValue.getCodeSystemEntityVersion().setVersionId(CSEV_DB.getVersionId());
                                                metadataValue.setMetadataParameter(new MetadataParameter());
                                                MetadataParameter metadataParameter;
                                                if (metadataParameterMap.containsKey(metadataField))
                                                    metadataParameter = metadataParameterMap.get(metadataField);
                                                else {
                                                    //Creating metadata_parameter
                                                    metadataParameter = new MetadataParameter();
                                                    metadataParameter.setParamName(metadataField);
                                                    metadataParameter.setCodeSystem(new CodeSystem());
                                                    metadataParameter.getCodeSystem().setId(super.getCodeSystem().getId());
                                                    
                                                    hb_session.save(metadataParameter);
                                                    metadataParameterMap.put(metadataField, metadataParameter);
                                                }
                                                metadataValue.getMetadataParameter().setId(metadataParameter.getId());
                                                hb_session.save(metadataValue);
                                            }
                                        }
                                    }
                                    LOGGER.info("LOINC concept (" + currentKey + ") update executed:  " + count);
                                }
                                catch (Exception e){
                                    countFehler++;
                                    errorList.add(String.valueOf(newCount));
                                    LOGGER.info("Error [0045], LOINC concept (" + currentKey + ") update failed while comparing two entries (" + count + "): " + e.getLocalizedMessage());
                                }
                            }
                        }
                        else{
                            //New CSC
                            CreateConceptRequestType request = new CreateConceptRequestType();
                            request.setLogin(this.getLoginType());
                            request.setCodeSystem(new CodeSystem());
                            request.getCodeSystem().setId(super.getCodeSystem().getId());
                            request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
                            CodeSystemVersion currentCSV = new CodeSystemVersion();
                            currentCSV.setVersionId(super.getCodeSystem().getCurrentVersionId());
                            request.getCodeSystem().getCodeSystemVersions().add(currentCSV);

                            request.setCodeSystemEntity(new CodeSystemEntity());
                            request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

                            CodeSystemConcept CSC = new CodeSystemConcept();
                            CSC.setIsPreferred(true);

                            CodeSystemEntityVersion CSEV = new CodeSystemEntityVersion();
                            CSEV.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                            CSEV.setIsLeaf(true);

                            CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
                            membership.setIsMainClass(Boolean.TRUE);
                            membership.setIsAxis(Boolean.FALSE);

                            request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

                            CreateConcept createConcept = new CreateConcept();
                            
                            CSC.setCode(currentKey);
                            CSC.setTerm(currentCSVfile.get("LONG_COMMON_NAME"));
                            CSC.setTermAbbrevation(currentCSVfile.get("SHORTNAME"));
                            CSC.setDescription(currentCSVfile.get("COMPONENT") + " : " + currentCSVfile.get("PROPERTY") + " : "
                                    + currentCSVfile.get("TIME_ASPCT") + " : " + currentCSVfile.get("SYSTEM") + " : "
                                    + currentCSVfile.get("SCALE_TYP") + " : " + currentCSVfile.get("METHOD_TYP") + " : ");

                            //Creating entity version
                            if (CSEV.getCodeSystemConcepts() == null)
                                CSEV.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                            else
                                CSEV.getCodeSystemConcepts().clear();
                            
                            CSEV.getCodeSystemConcepts().add(CSC);
                            CSEV.setEffectiveDate(parseDate(currentCSVfile.get("DATE_LAST_CHANGED")));
                            CSEV.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

                            //Saving concept
                            if (CSC.getCode().length() > 0){
                                //Adding entity version to the request
                                request.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
                                request.getCodeSystemEntity().getCodeSystemEntityVersions().add(CSEV);

                                CreateConceptResponseType responseCC = createConcept.CreateConcept(request, hb_session);

                                if (responseCC.getReturnInfos().getStatus() == ReturnType.Status.OK){
                                    newCount++;

                                    if (responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                                        codesMap.put(CSC.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());

                                    LOGGER.info("LOINC concept (" + currentKey + ") created: " + count);
                                }
                                else{
                                    countFehler++;
                                    errorList.add(String.valueOf(newCount));
                                    LOGGER.info("Error [0046], LOINC concept (" + currentKey + ") creation failed: (" + count + "): " + responseCC.getReturnInfos().getMessage());
                                }
                            }
                            else{
                                countFehler++;
                                errorList.add(String.valueOf(newCount));
                                LOGGER.info("Error [0047], LOINC concept (" + currentKey + ") creation failed, term missing: " + count);
                            }
                        }
                    }

                    //Freeing memory
                    Runtime runtime = Runtime.getRuntime();
                    if (count% 100 == 0){
                        runtime.freeMemory();
                        if (count % 1000 == 0){
                            hb_session.flush();
                            hb_session.clear();
                        }
                        if (count % 10000 == 0)
                            System.gc();
                    }
                }
                currentCSVfile.close();
                
                if (countFehler == 0){
                    //Reloading codesystem
                    if (super.getCodeSystem().getId() != null)
                        CS_DB = (CodeSystem) hb_session.get(CodeSystem.class, super.getCodeSystem().getId());

                    this.setCodeSystem(CS_DB);
                    
                    CodeSystem CS_returned = new CodeSystem();
                    CS_returned.setId(super.getCodeSystem().getId());
                    CS_returned.setCurrentVersionId(super.getCodeSystem().getCurrentVersionId());
                    CS_returned.setName(super.getCodeSystem().getName());
                    CS_returned.setAutoRelease(super.getCodeSystem().getAutoRelease());

                    CodeSystemVersion CSV_returned = new CodeSystemVersion();
                    CSV_returned.setVersionId(super.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
                    CSV_returned.setName(super.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                    CS_returned.getCodeSystemVersions().clear();
                    CS_returned.getCodeSystemVersions().add(CSV_returned);

                    this.setCodeSystem(CS_returned);
         
                    LastChangeHelper.updateLastChangeDate(true, super.getCodeSystem().getCurrentVersionId(), null);
                    
                    if(!hb_session.getTransaction().wasCommitted())
                        hb_session.getTransaction().commit();
                }
                else{
                    //TODO error report comes here
                    LOGGER.info("countFehler: " + countFehler);
                }
            }
            catch (IOException ex){
                LOGGER.error("Error [0048]: " + ex.getLocalizedMessage());
                error = true;

                try{
                    if(!hb_session.getTransaction().wasRolledBack()){
                        hb_session.getTransaction().rollback();
                        LOGGER.info("Rollback executed");
                    }
                }
                catch (Exception exRollback){
                    LOGGER.error("Error [0049]: " + exRollback.getLocalizedMessage());
                    if(!hb_session.getTransaction().wasRolledBack())
                        LOGGER.info("Rollback failed");
                }
            } 
            catch (HibernateException ex) {
                LOGGER.error("Error [0050]: " + ex.getLocalizedMessage());
                error = true;
                
                try{
                    if(!hb_session.getTransaction().wasRolledBack()){
                        hb_session.getTransaction().rollback();
                        LOGGER.info("Rollback executed");
                    }
                }
                catch (Exception exRollback){
                    LOGGER.error("Error [0051]: " + exRollback.getLocalizedMessage());
                    if(!hb_session.getTransaction().wasRolledBack())
                        LOGGER.info("Rollback failed");
                }
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }

            //Store current version only if no error occured
            if (countFehler == 0 && !error){
                FileOutputStream fileOutputStream;
                try{
                    fileOutputStream = new FileOutputStream(LOINCpath);
                    Writer out = new OutputStreamWriter(fileOutputStream, "UTF8");
                    out.write(new String(bytes, "UTF-8"));
                    out.close();
                }
                catch (FileNotFoundException ex){
                    LOGGER.error("Error [0052]: " + ex.getLocalizedMessage());
                }
                catch (IOException ex){
                    LOGGER.error("Error [0053]: " + ex.getLocalizedMessage());
                }
            }
        }
        LOGGER.info("----- startImport finished (001) -----");
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
            LOGGER.error("Error [0042]: " + e.getLocalizedMessage());
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