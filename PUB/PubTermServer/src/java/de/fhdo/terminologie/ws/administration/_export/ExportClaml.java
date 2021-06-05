
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
package de.fhdo.terminologie.ws.administration._export;

import clamlBindingXSD.ClaML;
import clamlBindingXSD.Identifier;
import clamlBindingXSD.Label;
import clamlBindingXSD.Meta;
import clamlBindingXSD.Rubric;
import clamlBindingXSD.RubricKind;
import clamlBindingXSD.RubricKinds;
import clamlBindingXSD.SubClass;
import clamlBindingXSD.SuperClass;
import clamlBindingXSD.Title;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.ValidityRangeHelper;
import de.fhdo.terminologie.helper.XMLFormatter;
import de.fhdo.terminologie.ws.administration.claml.MetadataDefinition.METADATA_ATTRIBUTES;
import de.fhdo.terminologie.ws.administration.claml.RubricKinds.RUBRICKINDS;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.ListConceptAssociations;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConcepts;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.ReturnType.Status;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Robert Mützner
 * @author Michael Heller
*/
public class ExportClaml{
    private static final Logger LOGGER = Logger4j.getInstance().getLogger();
    //Variables for import status
    public static double percentageComplete = 0F;
    public static String currentTask = "";
    private int countExported = 0;
    private CodeSystem codeSystem = null;
    private CodeSystemVersion codeSystemVersion = null;
    private ClaML claml = null;
    private ExportCodeSystemContentRequestType request;
    private Map<String, RubricKind> rubricKinds;
    SimpleDateFormat dateFormatter;
    long startingTime;
    private Session hb_session;

    /**
     * Standard constructor with the addition of instantiating the SimpleDateFormat with "yyyy-MM-dd".
     */
    public ExportClaml(){
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        hb_session = HibernateUtil.getSessionFactory().openSession();
    }

    /**
     * TODO
     * @param req
     * @return 
     */
    public ExportCodeSystemContentResponseType exportClaml(ExportCodeSystemContentRequestType req){
        LOGGER.info("+++++ exportClaml started +++++");
        
        countExported = 0;
        request = req;
        claml = new ClaML();
        startingTime = new Date().getTime();
        codeSystem = request.getCodeSystem();
        
        ExportCodeSystemContentResponseType returnInfos = new ExportCodeSystemContentResponseType();
        returnInfos.setReturnInfos(new ReturnType());
        returnInfos.setExportInfos(new ExportType());

        String packagename = clamlBindingXSD.ClaML.class.getPackage().getName();
        try{
            JAXBContext JAXBcontext = JAXBContext.newInstance(packagename);

            //Reading code system details
            ReturnCodeSystemDetailsRequestType RCSDrequest = new ReturnCodeSystemDetailsRequestType();
            RCSDrequest.setCodeSystem(codeSystem);

            //Login
            RCSDrequest.setLogin(req.getLogin());
            ReturnCodeSystemDetailsResponseType RCSDresponse = new ReturnCodeSystemDetails().ReturnCodeSystemDetails(RCSDrequest);

            if (RCSDresponse.getReturnInfos().getStatus() == ReturnType.Status.OK){
                if (request.getExportInfos().isUpdateCheck()){

                    if (!RCSDresponse.getCodeSystem().getCurrentVersionId().equals(codeSystem.getCodeSystemVersions().iterator().next().getVersionId())){
                        request.getCodeSystem().getCodeSystemVersions().iterator().next().setVersionId(RCSDresponse.getCodeSystem().getCurrentVersionId());
                        codeSystem = request.getCodeSystem();
                        
                        //Reading code system details
                        RCSDrequest = new ReturnCodeSystemDetailsRequestType();
                        RCSDrequest.setCodeSystem(codeSystem);
                        RCSDresponse = new ReturnCodeSystemDetails().ReturnCodeSystemDetails(RCSDrequest);
                    }
                }

                if (RCSDresponse.getReturnInfos().getStatus() == ReturnType.Status.OK){
                    //Using codeystem from webservice-call
                    codeSystem = RCSDresponse.getCodeSystem();

                    if (codeSystem != null){
                        LOGGER.debug("Loaded CS: " + codeSystem.getName());
                        LOGGER.debug("CSV-size: " + codeSystem.getCodeSystemVersions().size());
                        //LOGGER.debug("Codesystem-Version geladen: " + codeSystemVersion.getName());

                        codeSystemVersion = codeSystem.getCodeSystemVersions().iterator().next();
                        //Helping variables
                        rubricKinds = new HashMap<String, RubricKind>();
                        // ClaML-Details
                        claml.setVersion("2.0.0");

                        //No direct dependency: vocabularyType from response exists, concepts can be found even if the details fail
                        // Identifier
                        Identifier identifier = new Identifier();
                        identifier.setUid(codeSystemVersion.getOid());
                        identifier.setAuthority(codeSystemVersion.getSource());
                        claml.getIdentifier().add(identifier);

                        Title title = new Title();
                        title.setName(codeSystem.getName());
                        if (codeSystemVersion.getReleaseDate() != null)
                            title.setDate(dateFormatter.format(codeSystemVersion.getReleaseDate()));
                        else
                            title.setDate("");

                        title.setContent(codeSystem.getDescription());
                        title.setVersion(getClamlVersionFromCS(codeSystem));
                        claml.setTitle(title);

                        //CS metadata
                        claml.getMeta().add(createClaMLMetadata("description", codeSystem.getDescription()));
                        claml.getMeta().add(createClaMLMetadata("description_eng", codeSystem.getDescriptionEng()));
                        claml.getMeta().add(createClaMLMetadata("website", codeSystem.getWebsite()));
                        claml.getMeta().add(createClaMLMetadata("version_description", codeSystemVersion.getDescription()));
                        if (codeSystemVersion.getInsertTimestamp() != null)
                            claml.getMeta().add(createClaMLMetadata("insert_ts", codeSystemVersion.getInsertTimestamp().toString()));
                        else
                            claml.getMeta().add(createClaMLMetadata("insert_ts", ""));
                        
                        if (codeSystemVersion.getStatusDate() != null)
                            claml.getMeta().add(createClaMLMetadata("status_date", codeSystemVersion.getStatusDate().toString()));
                        else
                            claml.getMeta().add(createClaMLMetadata("status_date", ""));
                        
                        if (codeSystemVersion.getExpirationDate() != null)
                            claml.getMeta().add(createClaMLMetadata("expiration_date", codeSystemVersion.getExpirationDate().toString()));
                        else
                            claml.getMeta().add(createClaMLMetadata("expiration_date", ""));
                        
                        if (codeSystemVersion.getLastChangeDate() != null)
                            claml.getMeta().add(createClaMLMetadata("last_change_date", codeSystemVersion.getLastChangeDate().toString()));
                        else
                            claml.getMeta().add(createClaMLMetadata("last_change_date", ""));
                        
                        if (codeSystem.getIncompleteCS() != null)
                            if (codeSystem.getIncompleteCS())
                                claml.getMeta().add(createClaMLMetadata("unvollstaendig", "true"));
                            else
                                claml.getMeta().add(createClaMLMetadata("unvollstaendig", "false"));
                        
                        if (codeSystem.getResponsibleOrganization() != null)
                            claml.getMeta().add(createClaMLMetadata("verantw_Org", codeSystem.getResponsibleOrganization()));
                        
                        if (codeSystemVersion.getValidityRange() != null)
                            claml.getMeta().add(createClaMLMetadata("gueltigkeitsbereich", ValidityRangeHelper.getValidityRangeNameById(codeSystemVersion.getValidityRange())));
                        
                        if (codeSystemVersion.getStatus() != null)
                            claml.getMeta().add(createClaMLMetadata("statusCode", codeSystemVersion.getStatus() + ""));

                        createConcepts();

                        //RubricKinds (added during createConcepts in map)
                        claml.setRubricKinds(new RubricKinds());
                        claml.getRubricKinds().getRubricKind().addAll(rubricKinds.values());
                        
                        Marshaller marshaller = JAXBcontext.createMarshaller();

                        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

                        //Creating Claml file from classes in memory
                        marshaller.marshal(this.claml, byteOutputStream);

                        long diff = (new Date().getTime() - startingTime) / 1000;
                        LOGGER.debug("Claml export duration (s): " + diff);

                        StringWriter writer = new StringWriter();
                        try{
                            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder;
                            Document document;

                            builder = builderFactory.newDocumentBuilder();
                            document = builder.parse(new ByteArrayInputStream(byteOutputStream.toByteArray()));
                            TransformerFactory transformerFactory = TransformerFactory.newInstance();
                            Transformer transformer = transformerFactory.newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                            transformer.transform(new DOMSource(document), new StreamResult(writer));
                            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                            XMLFormatter XMLformatter = new XMLFormatter();
                            String formattedXml = XMLformatter.format(output);
                            formattedXml = formattedXml.replace("&quot;", "\"");
                            formattedXml = formattedXml.replace("\">\"", "\"&gt;\"");

                            //Creating return information
                            returnInfos.getExportInfos().setFilecontent(formattedXml.getBytes("UTF-8"));
                        }
                        catch (IOException exi){
                            LOGGER.error("Error [0067]: " + exi.getLocalizedMessage());
                            returnInfos.getExportInfos().setFilecontent(byteOutputStream.toByteArray());
                        } 
                        catch (IllegalArgumentException exi) {
                            LOGGER.error("Error [0068]: " + exi.getLocalizedMessage());
                            returnInfos.getExportInfos().setFilecontent(byteOutputStream.toByteArray());
                        } 
                        catch (ParserConfigurationException exi) {
                            LOGGER.error("Error [0069]: " + exi.getLocalizedMessage());
                            returnInfos.getExportInfos().setFilecontent(byteOutputStream.toByteArray());
                        } 
                        catch (TransformerException exi) {
                            LOGGER.error("Error [0070]: " + exi.getLocalizedMessage());
                            returnInfos.getExportInfos().setFilecontent(byteOutputStream.toByteArray());
                        } 
                        catch (SAXException exi) {
                            LOGGER.error("Error [0071]: " + exi.getLocalizedMessage());
                            returnInfos.getExportInfos().setFilecontent(byteOutputStream.toByteArray());
                        }
                        finally{
                            try{
                                writer.close();
                                byteOutputStream.close();
                            }
                            catch (IOException ex){
                                LOGGER.error("Error [0072]: " + ex.getLocalizedMessage());
                            }
                        }

                        returnInfos.getExportInfos().setFormatId(ExportCodeSystemContentRequestType.EXPORT_CLAML_ID);
                        returnInfos.getReturnInfos().setCount(countExported);
                        returnInfos.getReturnInfos().setMessage(countExported + " Klassen erfolgreich exportiert, Dauer (s): " + diff);
                        returnInfos.getReturnInfos().setStatus(Status.OK);
                        
                        currentTask = "";
                        percentageComplete = 0.0;
                    }
                    else{
                        returnInfos.getReturnInfos().setStatus(Status.FAILURE);
                        returnInfos.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP409);
                        returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                        returnInfos.getReturnInfos().setMessage("Codesystem konnte nicht gefunden werden!");
                    }
                }
                else{
                    LOGGER.error("Error [0073]: Code system not found or loading it failed.");
                    returnInfos.getReturnInfos().setStatus(Status.FAILURE);
                    returnInfos.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP409);
                    returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                    returnInfos.getReturnInfos().setMessage(RCSDresponse.getReturnInfos().getMessage());
                }
            }
            else{
                LOGGER.error("Error [0074]: Code system not found or loading it failed.");
                returnInfos.getReturnInfos().setStatus(Status.FAILURE);
                returnInfos.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP409);
                returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                returnInfos.getReturnInfos().setMessage(RCSDresponse.getReturnInfos().getMessage());
            }
        }
        catch (JAXBException ex){
            LOGGER.error("Error [0075]: " + ex.getLocalizedMessage());
        }

        LOGGER.info("----- exportClaml finished (001) -----");
        return returnInfos;
    }

    /**
     * TODO
     * @param name
     * @param value
     * @return 
     */
    private Meta createClaMLMetadata(String name, String value){
        Meta meta = new Meta();
        meta.setName(name);
        meta.setValue(checkString(value));
        return meta;
    }

    /**
     * Gets the CS version from the CS and returns it as string.
     * @param CS the code system from which to return the claml version.
     * @return the claml version of the CS version as string.
     */
    private String getClamlVersionFromCS(CodeSystem CS){
        String clamlVersion = "";

        try{
            CodeSystemVersion CSversion = CS.getCodeSystemVersions().iterator().next();
            clamlVersion = CSversion.getName().replaceAll(CS.getName(), "").trim();
        }
        catch (Exception e){
            LOGGER.error("Error [0073]: " + e.getLocalizedMessage());
        }

        return clamlVersion;
    }
    
    /**
     * TODO
     */
    public void createConcepts(){
        LOGGER.info("+++++ createConcepts started +++++");
        
        ListCodeSystemConceptsRequestType listCSconceptsRequest = new ListCodeSystemConceptsRequestType();
        listCSconceptsRequest.setCodeSystem(this.codeSystem);
        listCSconceptsRequest.setLogin(this.request.getLogin());

        if (request.getExportParameter() != null && request.getExportParameter().getDateFrom() != null){
            //Adding synchronisation date
            listCSconceptsRequest.setCodeSystemEntity(new CodeSystemEntity());
            listCSconceptsRequest.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
            CodeSystemEntityVersion CSEV = new CodeSystemEntityVersion();
            CSEV.setStatusDate(request.getExportParameter().getDateFrom());
            listCSconceptsRequest.getCodeSystemEntity().getCodeSystemEntityVersions().add(CSEV);

            LOGGER.debug("Synch time: " + request.getExportParameter().getDateFrom().toString());
        }
        else
            LOGGER.debug("No synch time given.");

        //DABACA
        //Session hb_session = HibernateUtil.getSessionFactory().openSession();

        ListCodeSystemConceptsResponseType listCSconceptsResponse = new ListCodeSystemConcepts().ListCodeSystemConcepts(listCSconceptsRequest, true);
        LOGGER.debug("ListCSconceptsResponse: " + listCSconceptsResponse.getReturnInfos().getMessage());

        clamlBindingXSD.Class clazz = null;

        //Going through all concepts
        if (listCSconceptsResponse.getReturnInfos().getStatus() == Status.OK){
            try{
                Iterator CSentityIterator = listCSconceptsResponse.getCodeSystemEntity().iterator();
                double classCount = listCSconceptsResponse.getCodeSystemEntity().size();
                double doneCount = 0;
                int i = 0;
                String oldCode = "";
                while (CSentityIterator.hasNext()){
                    i++;
                    CodeSystemEntity CSentity = (CodeSystemEntity) CSentityIterator.next();

                    for (CodeSystemEntityVersion CSentityVersion : CSentity.getCodeSystemEntityVersions()){
                        if (CSentityVersion.getCodeSystemConcepts() != null && CSentityVersion.getCodeSystemConcepts().size() > 0){
                            CodeSystemConcept CSconcepts = CSentityVersion.getCodeSystemConcepts().iterator().next();
                            if (CSentityVersion.getStatus() == 1){
                                String newCode = CSconcepts.getCode();
                                if (CSconcepts.getCode() == null || CSconcepts.getCode().trim().equals(""))
                                    newCode = i + "";

                                //Updating status
                                doneCount++;
                                percentageComplete = doneCount / classCount * 100.0;
                                currentTask = newCode;

                                LOGGER.info("Code: " + oldCode);

                                //New code (group change)
                                if (!newCode.equals(oldCode)){
                                    oldCode = newCode;
                                    
                                    //Creating new class
                                    clazz = new clamlBindingXSD.Class();
                                    
                                    createMetaData(CSentityVersion, clazz, hb_session);

                                    clazz.setCode(newCode);
                                    
                                    claml.getClazz().add(clazz);
                                    countExported++;
                                }

                                if (CSconcepts.getIsPreferred())
                                    addRubricElement(RUBRICKINDS.preferred, CSconcepts.getTerm(), clazz);
                                else
                                    addRubricElement(null, CSconcepts.getTerm(), clazz);

                                addRubricElement(RUBRICKINDS.note, CSconcepts.getDescription(), clazz);

                                //Storing additional attribues in metadata
                                createMetadata(METADATA_ATTRIBUTES.hints.getCode(), CSconcepts.getHints(), clazz);
                                createMetadata(METADATA_ATTRIBUTES.meaning.getCode(), CSconcepts.getMeaning(), clazz);
                                createMetadata(METADATA_ATTRIBUTES.termAbbrevation.getCode(), CSconcepts.getTermAbbrevation(), clazz);

                                if (CSentityVersion.getIsLeaf() != null)
                                    createMetadata(METADATA_ATTRIBUTES.isLeaf.getCode(), CSentityVersion.getIsLeaf().toString(), clazz);
                                if (CSentityVersion.getMajorRevision() != null)
                                    createMetadata(METADATA_ATTRIBUTES.majorRevision.getCode(), CSentityVersion.getMajorRevision().toString(), clazz);
                                if (CSentityVersion.getMinorRevision() != null)
                                    createMetadata(METADATA_ATTRIBUTES.minorRevision.getCode(), CSentityVersion.getMinorRevision().toString(), clazz);
                                if (CSentityVersion.getStatus() != null)
                                    createMetadata(METADATA_ATTRIBUTES.status.getCode(), CSentityVersion.getStatus().toString(), clazz);
                                if (CSentityVersion.getStatusDate() != null)
                                    createMetadata(METADATA_ATTRIBUTES.statusDate.getCode(), CSentityVersion.getStatusDate().toString(), clazz);

                                createAssociation(CSentity, clazz, null, CSentityVersion, CSconcepts, hb_session);
                            }
                        }
                    }

                    if (i % 500 == 0){
                        LOGGER.info("Session flushed");
                        hb_session.flush();
                        hb_session.clear();

                        if (i % 1000 == 0)
                            Runtime.getRuntime().gc();
                    }
                }
            }
            catch (HibernateException ex){
                LOGGER.error("Error [0066]: " + ex.getLocalizedMessage(), ex);
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
        LOGGER.info("----- createConcepts finished (001) -----");
    }

    /**
     * Creates a set of meta, setting its name and value before adding it to the class.
     * @param name name of the meta.
     * @param value value of the meta.
     * @param clazz the class to which to add the meta.
     */
    private void createMetadata(String name, String value, clamlBindingXSD.Class clazz){
        if (value != null && value.length() > 0 && name != null && name.length() > 0){
            Meta meta = new Meta();
            meta.setName(name);
            meta.setValue(checkString(value));
            clazz.getMeta().add(meta);
        }
    }

    /**
     * TODO
     * @param kind
     * @param value
     * @param clazz 
     */
    private void addRubricElement(RUBRICKINDS kind, String value, clamlBindingXSD.Class clazz){
        if (value != null && value.length() > 0){
            Rubric rubric = new Rubric();
            Label label = new Label();
            label.getContent().add(value);
            rubric.getLabel().add(label);

            if (kind != null){
                RubricKind rubricKind = new RubricKind();
                rubricKind.setName(kind.getCode());
                rubric.setKind(rubricKind);
            }
            
            clazz.getRubric().add(rubric);

            if (kind != null){
                if (rubricKinds.containsKey(kind.getCode()) == false){
                    RubricKind rubricKind = new RubricKind();
                    rubricKind.setInherited("false");
                    rubricKind.setName(checkString(kind.getCode()));
                    rubricKinds.put(kind.getCode(), rubricKind);
                }
            }
        }
    }

    /**
     * TODO
     * @param csev
     * @param clazz
     * @param hb_session 
     */
    public void createMetaData(CodeSystemEntityVersion csev, clamlBindingXSD.Class clazz, Session hb_session)
    {
        LOGGER.info("+++++ createMetaData started +++++");

        String HQL_CSmetadataValue_search = "from CodeSystemMetadataValue md ";
        HQL_CSmetadataValue_search += " join fetch md.metadataParameter mp ";
        HQL_CSmetadataValue_search += " where codeSystemEntityVersionId=" + csev.getVersionId();

        List<CodeSystemMetadataValue> metadataList = hb_session.createQuery(HQL_CSmetadataValue_search).list();

        if (metadataList != null){
            for (CodeSystemMetadataValue metadata : metadataList){
                if (metadata.getMetadataParameter() == null
                || metadata.getMetadataParameter().getParamName() == null
                || metadata.getMetadataParameter().getParamName().length() == 0)
                    continue;

                //Set classKind, saved in metadata
                if (metadata.getMetadataParameter().getParamName().equals("ClaML_ClassKind"))
                    clazz.setKind(metadata.getParameterValue());
                else{
                    if (!metadata.getParameterValue().equals("")){
                        Meta meta = new Meta();
                        meta.setName(metadata.getMetadataParameter().getParamName());
                        meta.setValue(checkString(metadata.getParameterValue()));

                        clazz.getMeta().add(meta);
                    }
                }
            }
        }
        
        LOGGER.info("----- createMetaData finished (001) -----");
    }

    private String checkString(String input)
    {
        String output = "";
        if (input != null)
        {
            output = input.replace('"', '\'');
            output = output.replace("<", "&lt;");
        }

        return output;
    }

    /**
     * TODO
     * @param cse
     * @param clazz
     * @param hashMap
     * @param csev
     * @param csc
     * @param hb_session 
     */
    public void createAssociation(CodeSystemEntity cse, clamlBindingXSD.Class clazz, HashMap hashMap, CodeSystemEntityVersion csev, CodeSystemConcept csc, Session hb_session){
        ListConceptAssociationsRequestType listConceptAssocRequest = new ListConceptAssociationsRequestType();
        listConceptAssocRequest.setCodeSystemEntity(cse);
        listConceptAssocRequest.setLogin(this.request.getLogin());
        listConceptAssocRequest.setDirectionBoth(true); //Both directions are important
        
        ListConceptAssociationsResponseType conceptAssocResp = new ListConceptAssociations().ListConceptAssociations(listConceptAssocRequest, null);
        LOGGER.info("ListConceptAssociationsResponseType: " + conceptAssocResp.getReturnInfos().getMessage());

        if (conceptAssocResp.getReturnInfos().getStatus() == Status.OK){
            for (CodeSystemEntityVersionAssociation CSEVA : conceptAssocResp.getCodeSystemEntityVersionAssociation()){
                if (CSEVA.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode()){
                    //Taxonomic connection
                    long leftId = 0;
                    if (CSEVA.getLeftId() != null)
                        leftId = CSEVA.getLeftId();

                    if (leftId > 0){
                        if (csev.getVersionId() != leftId){
                            if (CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null
                            && CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts() != null
                            && CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().size() > 0){
                                //Adding superclass
                                CodeSystemConcept CSCsuper = CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().iterator().next();
                                if (CSCsuper != null){
                                    SuperClass superClass = new SuperClass();
                                    superClass.setCode(CSCsuper.getCode());
                                    clazz.getSuperClass().add(superClass);
                                }
                            }
                        }
                        else{
                            SubClass subClass = new SubClass();
                            if (CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null
                            && CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts() != null
                            && CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().size() > 0){
                                //Adding subclass
                                CodeSystemConcept CSCSubClass = CSEVA.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().iterator().next();
                                subClass.setCode(CSCSubClass.getCode());
                                clazz.getSubClass().add(subClass);
                            }
                        }
                    }
                }
            }
        }
    }
}