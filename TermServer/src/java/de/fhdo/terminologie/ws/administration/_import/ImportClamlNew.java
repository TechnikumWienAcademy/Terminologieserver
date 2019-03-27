/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import clamlBindingXSD.Fragment;
import clamlBindingXSD.Label;
import clamlBindingXSD.Meta;
import clamlBindingXSD.Para;
import clamlBindingXSD.Rubric;
import clamlBindingXSD.RubricKind;
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
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.ValidityRangeHelper;
import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import de.fhdo.terminologie.ws.administration.claml.MetadataDefinition;
import de.fhdo.terminologie.ws.administration.claml.RubricKinds;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Stefan Puraner
 */
public class ImportClamlNew extends CodeSystemImport implements ICodeSystemImport{

    //Properties
    private final HashMap _referenceMap;
    private final HashMap _metaDataMap;
    private final HashMap<Long, MetadataParameter> _metadataParameterMap;
    private final HashMap<String, String> _clamlMetaData;
    private final ConcurrentHashMap<String, clamlBindingXSD.Class> _clamlClassMap;
    private AssociationType _assoctypeTaxonomy;
    private final HashMap _ccatresptHashmap;
    private CreateConceptAssociationTypeResponseType _ccatrespt;
    private CreateConceptAssociationTypeResponseType _ccatresptTaxonomy;
    private final HashMap _assoctypeHashmap;
    private CreateConceptResponseType _ccsResponse;
    private int _metadataCounter = 0;

    public ImportClamlNew(){
        super();
        this._referenceMap = new HashMap();
        this._metaDataMap = new HashMap();
        this._metadataParameterMap = new HashMap<Long, MetadataParameter>();
        this._clamlMetaData = new HashMap<String, String>();
        this._clamlClassMap = new ConcurrentHashMap<String, clamlBindingXSD.Class>();
        this._ccatresptHashmap = new HashMap();
        this._assoctypeHashmap  = new HashMap();
        this.aktCount = 0;
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
        
        //Creating Hibernate Session and starting transaction
        try{
            this.hb_session = HibernateUtil.getSessionFactory().openSession();
            this.hb_session.getTransaction().begin();
            this.hb_session.setFlushMode(FlushMode.COMMIT);
            this.validateParameters();
        }
        catch (HibernateException ex){
            LOGGER.error("Error [0103]", ex);
            this.rollbackHibernateTransaction();
            this.closeHibernateSession();
            throw ex;
        }
        catch (ImportParameterValidationException ex){
            LOGGER.error("Error [0104]", ex);
            this.rollbackHibernateTransaction();
            this.closeHibernateSession();
            throw ex;
        }
        
        //Adding status of import to statuslist
        this.status.setImportRunning(true);
        StaticStatusList.addStatus(this.getImportId(), this.status);
               
        try{
            LOGGER.debug("Opening file and creating ByteArrayInputStream"); //anker2
            InputStream inputStream = new ByteArrayInputStream(this.fileContent);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            NodeList list = doc.getElementsByTagName("Class");

            LOGGER.info("Total of " + list.getLength() + " will be imported.");
            this.setTotalCountInStatusList(list.getLength(), this.getImportId());

            inputStream = new ByteArrayInputStream(this.fileContent);
            this.loadClamlXML(inputStream); //ANCHOR

            this.status = StaticStatusList.getStatus(this.getImportId());

            if (this.status != null && this.status.isCancel() && !hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
            else{
                if(hb_session.getTransaction().isActive() && !hb_session.getTransaction().wasCommitted()){
                    hb_session.flush();
                    hb_session.getTransaction().commit();
                }
            }
        }
        catch (HibernateException ex){
            LOGGER.error("Error [0110]", ex);

            try{
                if(!hb_session.getTransaction().wasRolledBack()){
                    hb_session.getTransaction().rollback();
                    LOGGER.info("Rollback executed");
                }
            }
            catch (Exception exRollback){
                LOGGER.error("Error [0111]", exRollback);
                if(!hb_session.getTransaction().wasRolledBack())
                    LOGGER.info("Rollback failed");
            }
            
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (SAXException ex){
            LOGGER.error("Error [0112]", ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (IOException ex){
            LOGGER.error("Error [0113]", ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (ParserConfigurationException ex){
            LOGGER.error("Error [0114]", ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (Exception ex){
            LOGGER.error("Error [0115]", ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        finally{
            try{
                if(hb_session.isOpen() && hb_session.getTransaction().isActive() && !hb_session.getTransaction().wasRolledBack()){
                    hb_session.getTransaction().rollback();
                    LOGGER.info("Rollback executed");
                }
            }
            catch (Exception exRollback){
                LOGGER.error("Error [0116]: " + exRollback.getLocalizedMessage(), exRollback);
                if(!hb_session.getTransaction().wasRolledBack())
                    LOGGER.info("Rollback failed");
            }

            if(hb_session != null && hb_session.isOpen())
                hb_session.close();  

            //TODO check this
            //isRunning = false;
        }
        //TODO create proposals here
        LOGGER.info("----- startImport finished (001) -----");
    }

    /**
     * Importiert die ClaML-XML Datei
     */
    private void loadClamlXML(InputStream inputStream) throws Exception{
        LOGGER.info("+++++ loadClamlXML started +++++");
        
        LOGGER.debug("Create JAXBContext");
        clamlBindingXSD.Class clazz = null;
        clamlBindingXSD.Rubric rubric = null;
        clamlBindingXSD.RubricKinds rubricKinds = new clamlBindingXSD.RubricKinds();

        // First create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        // Setup a new eventReader
        XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);

        // Read the XML document
        LOGGER.debug("Analyze data");

        //Attributes for CreateCodeSystem
        String authority = "";
        String uid = "";

        int countEvery = 0;
        
        while (eventReader.hasNext()){
            
            this.status = StaticStatusList.getStatus(this.getImportId());
            if (this.status != null && this.status.isCancel())
                break;
            
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()){
                StartElement startElement = event.asStartElement();
                String startElementName = startElement.getName().toString();

                if (startElementName.equals("Title")){
                    Date date = new Date();
                    String title = "";
                    String versionName = "";

                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()){
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("name"))
                            title = attribute.getValue();
                        
                        if (attribute.getName().toString().equals("version"))
                            versionName = attribute.getValue();                            
                        
                        if (attribute.getName().toString().equals("date")){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            if (!attribute.getValue().equals(""))
                                date = dateFormat.parse(attribute.getValue());
                        }
                    }

                    event = eventReader.nextEvent();

                    //If <Title>-element does not have content --> description will be set to ""
                    if (event.isEndElement()){
                        this.createCodeSystem(title, uid, versionName, date, authority, "");
                        continue;
                    }

                    String description = event.asCharacters().getData();
                    this.createCodeSystem(title, uid, versionName, date, authority, description);
                }
                else if (startElementName.equals("Identifier")){
                    //We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()){
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("authority"))
                            authority = attribute.getValue();
                        
                        if (attribute.getName().toString().equals("uid"))
                            uid = attribute.getValue();
                    }
                }
                else if (startElementName.equals("RubricKind")){
                    //We read the attributes from this tag and add the date attribute to our object
                    clamlBindingXSD.RubricKind rubricKind = new clamlBindingXSD.RubricKind();
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()){
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("name")){
                            rubricKind.setName(attribute.getValue());
                            rubricKinds.getRubricKind().add(rubricKind);
                        }
                    }
                }
                else if (startElementName.equals("Class")){
                    clazz = new clamlBindingXSD.Class();

                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()){
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("code"))
                            clazz.setCode(attribute.getValue());                    
                        if (attribute.getName().toString().equals("kind"))
                            clazz.setKind(attribute.getValue());                                                        
                    }
                }
                else if (startElementName.equals("Rubric")){
                    if (clazz != null){
                        rubric = new clamlBindingXSD.Rubric();
                        // We read the attributes from this tag and add the date attribute to our object
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()){
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("kind"))
                                rubric.setKind(attribute.getValue());
                        }
                        clazz.getRubric().add(rubric);
                    }
                }
                else if (startElementName.equals("Label")){
                    if (rubric != null){
                        event = eventReader.nextEvent();
                        clamlBindingXSD.Label label = new clamlBindingXSD.Label();
                        label.getContent().add(event.asCharacters().getData());
                        
                        rubric.getLabel().add(label);
                        continue;
                    }
                }
                else if (startElementName.equals("Fragment")){
                    if (rubric != null){
                        if (event.isEndElement() == false){
                            event = eventReader.nextEvent();

                            if (event.isEndElement() == false){
                                clamlBindingXSD.Label label = new clamlBindingXSD.Label();
                                label.getContent().add(event.asCharacters().getData());
                                rubric.getLabel().add(label);
                            }
                            else
                                LOGGER.debug("No text, end element reached");
                        }
                        continue;
                    }
                }
                else if (startElementName.equals("SuperClass")){
                    if (clazz != null){
                        clamlBindingXSD.SuperClass superClass = new clamlBindingXSD.SuperClass();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()){
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("code"))
                                superClass.setCode(attribute.getValue());
                        }

                        clazz.getSuperClass().add(superClass);
                    }
                }
                else if (startElementName.equals("SubClass")){
                    if (clazz != null){
                        clamlBindingXSD.SubClass subClass = new clamlBindingXSD.SubClass();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()){
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("code"))
                                subClass.setCode(attribute.getValue());
                        }

                        clazz.getSubClass().add(subClass);
                    }
                }
                else if (startElementName.equals("Meta")){
                    if (clazz != null){
                        Meta meta = new Meta();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()){
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name"))
                                meta.setName(attribute.getValue());
                            if (attribute.getName().toString().equals("value"))
                                meta.setValue(attribute.getValue());
                        }
                        clazz.getMeta().add(meta);
                    }
                    else{
                        //Claml/Metadata
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        String name = "";
                        String value = "";
                        while (attributes.hasNext()){
                            Attribute attribute = attributes.next();

                            if (attribute.getName().toString().equals("name"))
                                name = attribute.getValue();
                            if (attribute.getName().toString().equals("value"))
                                value = attribute.getValue();
                        }
                        this._clamlMetaData.put(name, value);
                    }
                }
            } // End start element
            if (event.isEndElement()){
                EndElement endElement = event.asEndElement();
                if (endElement.getName().toString().equals("Class")){
                    //Write clazz to map to be processed later
                    if(clazz!=null){
                        this._clamlClassMap.put(clazz.getCode(), clazz);
                        LOGGER.info("Concept reading: " + clazz.getCode() + "(" + this._clamlClassMap.size() + ")");
                    }
                }
                else if (endElement.getName().toString().equals("RubricKinds")){
                    //CreateAssociationType (Unterklasse,Oberklasse)
                    this._assoctypeTaxonomy = this.CreateAssociationType("ist Oberklasse von", "ist Unterklasse von");
                    LOGGER.debug(this._ccatrespt.getReturnInfos().getMessage());
                    
                    if (this._ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK){
                        this._ccatresptTaxonomy = this._ccatrespt;
                        
                        //Creating associationTypes for all rubricKinds
                        for (RubricKind rubricKind : rubricKinds.getRubricKind()) {
                            AssociationType assocTypeTemp = this.CreateAssociationType(rubricKind.getName(), rubricKind.getName());
                            this._assoctypeHashmap.put(rubricKind.getName(), assocTypeTemp);
                            LOGGER.debug(this._ccatrespt.getReturnInfos().getMessage());
                            if (this._ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
                                this._ccatresptHashmap.put(rubricKind.getName(), this._ccatrespt);
                        }
                    }
                }
            }

            if (countEvery % 100000 == 0)
                System.gc();
            countEvery++;
        }

        Collection<clamlBindingXSD.Class> clazzCollection = this._clamlClassMap.values();
        int counter = 0;
        for (clamlBindingXSD.Class bufferClazz : clazzCollection){
            counter++;
            importClazz(bufferClazz);

            if (counter % 500 == 0){
                hb_session.flush();
                hb_session.clear();

                if (countEvery % 5000 == 0)
                    System.gc();
            }
        }
        
        LOGGER.info("----- LoadClamlXML finished (001) -----");
    }

    private void createCodeSystem(String title, String uid, String versionName, Date date, String authority, String description) throws Exception{
        LOGGER.info("+++++ createCodeSystem started +++++");
        
        //Search for CS first, then create it, if it does not exist
        CreateCodeSystemRequestType createCSrequest = new CreateCodeSystemRequestType();

        if (this.codesystem.getId() > 0)
            createCSrequest.setCodeSystem(this.codesystem);
        else
            createCSrequest.setCodeSystem(new CodeSystem());
        
        createCSrequest.getCodeSystem().setName(title);

        CodeSystemVersion CSversion = new CodeSystemVersion();
        CSversion.setName(title + " " + versionName);
        CSversion.setDescription(description);
        CSversion.setSource(authority);
        CSversion.setReleaseDate(date);
        CSversion.setOid(uid);

        if (this.getImportType().getRole() == null || !this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
            CSversion.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
        else
            CSversion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

        if (this._clamlMetaData.get("description") != null)
            CSversion.setDescription(this._clamlMetaData.get("description"));

        if (this._clamlMetaData.get("unvollstaendig") != null)
            if (this._clamlMetaData.get("unvollstaendig").equals("true"))
                createCSrequest.getCodeSystem().setIncompleteCS(true);
            else
                createCSrequest.getCodeSystem().setIncompleteCS(false);

        if (this._clamlMetaData.get("description_eng") != null)
            createCSrequest.getCodeSystem().setDescriptionEng(this._clamlMetaData.get("description_eng"));

        if (this._clamlMetaData.get("gueltigkeitsbereich") != null)
            CSversion.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(this._clamlMetaData.get("gueltigkeitsbereich")));

        if (this._clamlMetaData.get("verantw_Org") != null)
            createCSrequest.getCodeSystem().setResponsibleOrganization(this._clamlMetaData.get("verantw_Org"));

        if (this._clamlMetaData.get("version_description") != null)
            CSversion.setDescription(this._clamlMetaData.get("version_description"));

        createCSrequest.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
        createCSrequest.getCodeSystem().getCodeSystemVersions().add(CSversion);

        createCSrequest.setLogin(this.getLoginType());
        
        //Creating code system
        CreateCodeSystem createCodeSystem = new CreateCodeSystem();
        CreateCodeSystemResponseType createCSresponse = createCodeSystem.CreateCodeSystem(createCSrequest, hb_session); //NULL ANKER
        
        LOGGER.debug(createCSresponse.getReturnInfos().getMessage());

        if (createCSresponse.getReturnInfos().getStatus() != ReturnType.Status.OK)
            throw new Exception(createCSresponse.getReturnInfos().getMessage());
        
        this.codesystem = createCSresponse.getCodeSystem();

        //LOGGER.debug("New CS-ID: " + createCSresponse.getCodeSystem().getId());
        //LOGGER.debug("New CSV-ID: " + ((CodeSystemVersion) createCSresponse.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());

        //Read existing metadata and add to map to avoid double entries
        String HQL_metadataParameter_search = "select distinct mp from MetadataParameter mp "
            + " where codeSystemId=" + createCSresponse.getCodeSystem().getId();
        
        /*
        //DABACA
        if(!hb_session.isOpen()){
            this.hb_session = HibernateUtil.getSessionFactory().openSession();
            this.hb_session.getTransaction().begin();
        }*/
            
        List<MetadataParameter> metadataParameterList = hb_session.createQuery(HQL_metadataParameter_search).list();

        for (MetadataParameter metadataParameter : metadataParameterList){
            this._metaDataMap.put(metadataParameter.getParamName(), metadataParameter.getId());
            this._metadataParameterMap.put(metadataParameter.getId(), metadataParameter);
            LOGGER.debug("Found metadata: " + metadataParameter.getParamName() + ", with id: " + metadataParameter.getId());
        }
        LOGGER.info("----- createCodeSystem finished (001) -----");
    }
    
    private AssociationType CreateAssociationType(String forwardName, String reverseName){
        LOGGER.info("+++++ CreateAssociationType started +++++");
        
        //Creating entityType
        CodeSystemEntity CSentityAssoc = new CodeSystemEntity();
        //TODO IsAxis ist nicht in CodeSystemEntity sondern in CodeSystemVersionEntityMembership
        //CSentityAssoc.setIsAxis(false);
        Set<CodeSystemVersionEntityMembership> membershipList = CSentityAssoc.getCodeSystemVersionEntityMemberships();
        for (CodeSystemVersionEntityMembership CSVentityMembership : membershipList) {
            //TODO if(csvem.) wenn es die korrekte Verbindung der beiden Tabellen ist, dann nur setzen
            CSVentityMembership.setIsAxis(Boolean.FALSE);
        }

        Set<AssociationType> associationList = new HashSet<AssociationType>();
        AssociationType associationType = new AssociationType();
        associationType.setForwardName(forwardName);
        associationType.setReverseName(reverseName);
        associationList.add(associationType);

        Set<CodeSystemEntityVersion> CSEVlistAssociations = new HashSet<CodeSystemEntityVersion>();
        
        //Creating entityVersionType
        CodeSystemEntityVersion EVTassociation = new CodeSystemEntityVersion();
        EVTassociation.setMajorRevision(0);
        EVTassociation.setMinorRevision(0);
        EVTassociation.setAssociationTypes(associationList);
        EVTassociation.setIsLeaf(true);

        CSEVlistAssociations.add(EVTassociation);
        
        CSentityAssoc.setCodeSystemEntityVersions(CSEVlistAssociations);

        CreateConceptAssociationTypeRequestType createConceptAssocRequest = new CreateConceptAssociationTypeRequestType();
        createConceptAssocRequest.setCodeSystemEntity(CSentityAssoc);
        createConceptAssocRequest.setLogin(this.getLoginType());
        
        CreateConceptAssociationType createConceptAssoc = new CreateConceptAssociationType();
        this._ccatrespt = createConceptAssoc.CreateConceptAssociationType(createConceptAssocRequest, hb_session);

        LOGGER.info("----- CreateAssociationType finished (001) -----");
        return associationType;
    }
    
    private void importClazz(clamlBindingXSD.Class clazz) throws ImportException{
        LOGGER.info("+++++ importClazz started +++++");
        
        if (clazz.getSuperClass() != null && clazz.getSuperClass().iterator().hasNext()){
            String code = clazz.getSuperClass().iterator().next().getCode();
            if (this._clamlClassMap.get(code) != null)
                importClazz(this._clamlClassMap.get(code));
        }

        try{
            if (this._clamlClassMap.get(clazz.getCode()) != null){
                this.CreateSingleConcept(clazz);
                LOGGER.debug("Concept writing: " + clazz.getCode() + "(" + this._clamlClassMap.size() + ")");
                if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
                    this.createMetaData(clazz);
                this._clamlClassMap.remove(clazz.getCode());
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0109]", ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        
        LOGGER.info("----- importClazz finished (001) -----");
    }
    
    private void CreateSingleConcept(clamlBindingXSD.Class cl) throws Exception{
        LOGGER.info("+++++ CreateSingleConcept started +++++");
        
        //Creating concept
        String code;
        String rubKind;
        String labelString;

        clamlBindingXSD.Class clazz = cl;
        code = clazz.getCode();
        //clazz.getKind()  // TODO Kind abspeichern für Export

        //Updating import status
        this.setCurrentTaskInStatusList(code, this.getImportId());

        Iterator iteratorPreferred = clazz.getRubric().iterator();
        Iterator iteratorRest = clazz.getRubric().iterator();

        boolean rubricFound = false;
        //Searching all rubric, first time to find and create preferred, second time to create all others
        Rubric rubric = null;
        while (iteratorPreferred.hasNext()){
            rubric = (Rubric) iteratorPreferred.next();
            rubKind = (String) rubric.getKind();

            if (rubKind.equals(RubricKinds.RUBRICKINDS.preferred.getCode())){
                rubricFound = true;
                labelString = getAllRubricStrings(rubric);
                this.createPreferredTerm(labelString, code, clazz); //ANKER
            }
        }
        if (rubricFound == false && rubric != null){
            rubric.setKind(RubricKinds.RUBRICKINDS.preferred.getCode());
            labelString = this.getAllRubricStrings(rubric);
            this.createPreferredTerm(labelString, code, clazz);
        }

        while (iteratorRest.hasNext() && rubricFound == true){
            rubric = (Rubric) iteratorRest.next();
            rubKind = (String) rubric.getKind();
            if (!(rubKind.equals(RubricKinds.RUBRICKINDS.preferred.getCode())
            || rubKind.equals(RubricKinds.RUBRICKINDS.note.getCode()))){
                labelString = getAllRubricStrings(rubric);
                this.createNotPrefferdTerm(labelString, code, clazz, rubKind);
            }
        }
    }
    
    private String getAllRubricStrings(Rubric rubric){
        String returnString = "";
        
        for (Label label : rubric.getLabel()) {
            List contentList = label.getContent();
            Iterator contentIterator = contentList.iterator();
            while (contentIterator.hasNext()){
                Object object = contentIterator.next();

                if (object instanceof String)
                    returnString = returnString + object.toString();
                else{
                    if (object instanceof Fragment){
                        Fragment fragment = (Fragment) object;
                        List fragmentContentList = fragment.getContent();
                        Iterator fragmentIterator = fragmentContentList.iterator();
                        while (fragmentIterator.hasNext()){
                            Object fragmentObject = fragmentIterator.next();
                            if (fragmentObject instanceof String)
                                returnString = returnString + fragmentObject.toString();
                        }
                    }
                    else{
                        if (object instanceof Para){
                            Para para = (Para) object;
                            List paraList = para.getContent();
                            Iterator paraIterator = paraList.iterator();
                            while (paraIterator.hasNext()){
                                Object paraObject = paraIterator.next();
                                if (paraObject instanceof String)
                                    returnString = returnString + paraObject.toString();
                            }
                        }
                    }
                }
            }
        }
        return returnString;
    }
    
    private void createPreferredTerm(String labelString, String code, clamlBindingXSD.Class clazz) throws Exception{
        LOGGER.info("+++++ createPreferredTerm started +++++");
        LOGGER.debug("createPrefferedTerm mit Code: " + code + ", Text: " + labelString);

        CreateConceptRequestType createConceptRequest = new CreateConceptRequestType();

        //Creating entityType
        CodeSystemEntity CSentity = new CodeSystemEntity();
        CodeSystemVersionEntityMembership CSVentityMembership = new CodeSystemVersionEntityMembership();
        CSVentityMembership.setIsAxis(false);
        if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0){
            CSVentityMembership.setIsAxis(false);
            CSVentityMembership.setIsMainClass(false);
        }
        else
            CSVentityMembership.setIsMainClass(true);

        if (clazz.getKind() != null && clazz.getKind().equals("chapter"))
            CSVentityMembership.setIsMainClass(true);

        CSentity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
        CSentity.getCodeSystemVersionEntityMemberships().add(CSVentityMembership);

        CodeSystemEntityVersion CSentityVersion = new CodeSystemEntityVersion();
        CSentityVersion.setMajorRevision(1);
        CSentityVersion.setMinorRevision(0);
        CSentityVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

        clamlBindingXSD.Class claz = this._clamlClassMap.get(code);
        if (claz.getSubClass() != null && claz.getSubClass().iterator().hasNext())
            CSentityVersion.setIsLeaf(false);
        else
            CSentityVersion.setIsLeaf(true);  //True, will be trigger-set to false if a relationship is added

        CodeSystemConcept CSconcept = new CodeSystemConcept();
        CSconcept.setCode(code);
        CSconcept.setTerm(labelString);
        CSconcept.setTermAbbrevation("");
        CSconcept.setIsPreferred(true);

        for (Rubric rubric : clazz.getRubric())
            if (rubric.getKind().equals(RubricKinds.RUBRICKINDS.note.getCode()))    
                CSconcept.setDescription(rubric.getLabel().get(0).getContent().get(0).toString());

        CSentityVersion.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
        CSentityVersion.getCodeSystemConcepts().add(CSconcept);

        CSentity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        CSentity.getCodeSystemEntityVersions().add(CSentityVersion);

        this.addAttributeMetadata(clazz, CSentityVersion, CSconcept);
        LOGGER.debug("isMainClass: " + CSVentityMembership.getIsMainClass());

        createConceptRequest.setCodeSystem(this.codesystem);
        createConceptRequest.setCodeSystemEntity(CSentity);
        createConceptRequest.setLogin(this.getLoginType());
        
        //Creating concept
        CreateConcept createConcept = new CreateConcept();
        this._ccsResponse = createConcept.CreateConcept(createConceptRequest, hb_session);
        LOGGER.debug(this._ccsResponse.getReturnInfos().getMessage());
        
        if (this._ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK){
            if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
                this.createSuperclassAssociation(code, clazz);

            //Writing current entityVersionID from response into hashmap
            long currentEntityVersionID;

            if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext()){
                currentEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
                this._referenceMap.put(code, currentEntityVersionID);
            }

            this.aktCount++;
            this.setCurrentCountInStatusList(this.aktCount, this.getImportId());
            if(this.getImportType().getRole() != null && this.getImportType().getRole().equals(CODES.ROLE_TRANSFER)){
                LOGGER.info("Import progress: " + this.aktCount + "/" + this.getTotalCountInStatusList(this.getImportId()));
            }
        }
        else
            throw new Exception();
    }

    private void createNotPrefferdTerm(String labelString, String code, clamlBindingXSD.Class clazz, String rubKind) throws Exception{
        LOGGER.debug("createNotPrefferdTerm mit Code: " + code + ", Text: " + labelString);
        
        CreateConceptRequestType createConceptRequest = new CreateConceptRequestType();

        //Creating entityType
        CodeSystemEntity CSentity = new CodeSystemEntity();
        CodeSystemVersionEntityMembership CSVentityMembership = new CodeSystemVersionEntityMembership();
        CSVentityMembership.setIsAxis(false);
        CSentity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
        CSentity.getCodeSystemVersionEntityMemberships().add(CSVentityMembership);

        //Creating entityVersionType
        CodeSystemEntityVersion CSEV = new CodeSystemEntityVersion();
        CSEV.setMajorRevision(1);
        CSEV.setMinorRevision(0);
        CSEV.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        CSEV.setIsLeaf(true);

        //Creating termType
        CodeSystemConcept CSconcept = new CodeSystemConcept();
        CSconcept.setCode(code);
        CSconcept.setTerm(labelString);
        CSconcept.setTermAbbrevation("");
        CSconcept.setIsPreferred(false);

        CSEV.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
        CSEV.getCodeSystemConcepts().add(CSconcept);

        CSentity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        CSentity.getCodeSystemEntityVersions().add(CSEV);

        this.addAttributeMetadata(clazz, CSEV, CSconcept);

        createConceptRequest.setCodeSystem(this.codesystem);
        createConceptRequest.setCodeSystemEntity(CSentity);
        createConceptRequest.setLogin(this.getLoginType());
        
        //Creating concept
        CreateConcept createConcept = new CreateConcept();
        this._ccsResponse = createConcept.CreateConcept(createConceptRequest, hb_session);

        LOGGER.debug(this._ccsResponse.getReturnInfos().getMessage());
        if (this._ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK){
            this.createTerm2TermAssociation(code, clazz, rubKind);
            this.aktCount++;
            this.setCurrentCountInStatusList(this.aktCount, this.getImportId());
        }
        else
            throw new Exception();
    }
    
    private void addAttributeMetadata(clamlBindingXSD.Class clazz, CodeSystemEntityVersion csev, CodeSystemConcept csc) throws ImportException{
        if (clazz != null && csev != null && csc != null){
            for (Meta meta : clazz.getMeta()){
                try{
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.hints.getCode()))
                        csc.setHints(meta.getValue());
                    
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.meaning.getCode()))
                        csc.setMeaning(meta.getValue());
                    
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.termAbbrevation.getCode()))
                        csc.setTermAbbrevation(meta.getValue());

                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.majorRevision.getCode()))
                        csev.setMajorRevision(Integer.parseInt(meta.getValue()));
                    
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.minorRevision.getCode()))
                        csev.setMinorRevision(Integer.parseInt(meta.getValue()));
                    
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.status.getCode()))
                        csev.setStatus(Integer.parseInt(meta.getValue()));

                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.statusDate.getCode())){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        csev.setStatusDate(dateFormat.parse(meta.getValue()));
                    }
                }
                catch (NumberFormatException ex){
                    LOGGER.error("Error [0105]: " + ex.getLocalizedMessage(), ex);
                    throw new ImportException(ex.getLocalizedMessage());
                } catch (ParseException ex) {
                    LOGGER.error("Error [0106]: " + ex.getLocalizedMessage(), ex);
                    throw new ImportException(ex.getLocalizedMessage());
                }
            }
        }
    }
    
    private void createTerm2TermAssociation(String code, clamlBindingXSD.Class clazz, String rubkind) throws Exception{
        //Storing curent entityVersionID from response
        long currentEntityVersionID = 0;
        if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
            currentEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();

        //Searching own code of prefferedTerms in Hashmap
        long prefferedTermEntityVersionID = (Long) this._referenceMap.get(code);

        CodeSystemEntityVersionAssociation CSEVassoc = new CodeSystemEntityVersionAssociation();
        
        //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden
        CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(prefferedTermEntityVersionID);

        CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(currentEntityVersionID);

        CSEVassoc.setAssociationKind(Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode());
        CSEVassoc.setLeftId(prefferedTermEntityVersionID);

        CSEVassoc.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        CSEVassoc.setStatusDate(new Date());

        //Getting associationType and response from the corresponding hashMaps
        CreateConceptAssociationTypeResponseType CCassocResponse = (CreateConceptAssociationTypeResponseType) this._ccatresptHashmap.get(rubkind);
        AssociationType assocType = (AssociationType) this._assoctypeHashmap.get(rubkind);
        assocType.setCodeSystemEntityVersionId(CCassocResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());

        CSEVassoc.setAssociationType(assocType);

        CreateConceptAssociationRequestType CCassocRequest = new CreateConceptAssociationRequestType();
        
        //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
        //es besitzt jedoch eine andere Struktur
        CCassocRequest.setCodeSystemEntityVersionAssociation(CSEVassoc);
        CCassocRequest.setLogin(this.getLoginType());

        CreateConceptAssociation createConceptAssoc = new CreateConceptAssociation();
        CreateConceptAssociationResponseType CCAresponse = createConceptAssoc.CreateConceptAssociation(CCassocRequest, hb_session);
        
        LOGGER.debug(CCAresponse.getReturnInfos().getMessage());
        if (CCAresponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
            LOGGER.debug("Create Association finished successfully");
        else
            throw new Exception();
    }
    
    private void createSuperclassAssociation(String code, clamlBindingXSD.Class clazz) throws Exception{
        //Writing current entityVersionID from response into the hashmap
        long currentEntityVersionID = 0;
        if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext()){
            currentEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
            this._referenceMap.put(code, currentEntityVersionID);
        }

        //Get first superClass
        String superclazzCode;
        long superclazzEntityVersionID = 0;
        if (clazz.getSuperClass().iterator().hasNext()){
            superclazzCode = clazz.getSuperClass().iterator().next().getCode();
            superclazzEntityVersionID = (Long) this._referenceMap.get(superclazzCode);
        }

        CodeSystemEntityVersionAssociation CSEVassoc = new CodeSystemEntityVersionAssociation();
        
        //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden
        CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(superclazzEntityVersionID);

        CSEVassoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        CSEVassoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(currentEntityVersionID);

        CSEVassoc.setAssociationKind(Definitions.ASSOCIATION_KIND.TAXONOMY.getCode());
        CSEVassoc.setLeftId(superclazzEntityVersionID);
        CSEVassoc.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        CSEVassoc.setStatusDate(new Date());

        this._assoctypeTaxonomy.setCodeSystemEntityVersionId(this._ccatresptTaxonomy.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());
        CSEVassoc.setAssociationType(this._assoctypeTaxonomy);

        CreateConceptAssociationRequestType createConceptAssocRequest = new CreateConceptAssociationRequestType();
        //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
        //es besitzt jedoch eine andere Struktur

        createConceptAssocRequest.setCodeSystemEntityVersionAssociation(CSEVassoc);
        createConceptAssocRequest.setLogin(this.getLoginType());
        CreateConceptAssociation createConceptAssoc = new CreateConceptAssociation();
        //TODO cca.CreateConceptAssociation( ist noch nicht implementiert

        CreateConceptAssociationResponseType CCAresponse = createConceptAssoc.CreateConceptAssociation(createConceptAssocRequest, hb_session);
        LOGGER.debug(CCAresponse.getReturnInfos().getMessage());
    }
    
    private void createMetaData(clamlBindingXSD.Class clazz){
        LOGGER.info("+++++ createMetaData started +++++");
        
        for (Meta meta : clazz.getMeta())
            if (meta.getName() != null && meta.getName().length() > 0){
                //Checking if it is a metadata attribute
                if (MetadataDefinition.METADATA_ATTRIBUTES.isCodeValid(meta.getName()) == false){
                    long metaDataID = insertMetaData(meta.getName(), meta.getValue(), clazz.getCode());
                    if (metaDataID > 0)
                        LOGGER.debug("New entity_version_parameter_value with ID: " + metaDataID);
                }
            }

        //Storing classKind in metadata, so that it can be exported
        if (clazz.getKind() != null){
            String classKind = clazz.getKind().toString();
            if (classKind.length() > 0){
                long metaDataID = insertMetaData("ClaML_ClassKind", classKind, clazz.getCode());
                if (metaDataID > 0)
                    LOGGER.debug("New entity_version_parameter_value with ID: " + metaDataID);
            }
        }
        
        LOGGER.info("----- createMetaData finished (001) -----");
    }
    
    private long insertMetaData(String name, String value, String code){
        this._metadataCounter++;
        if (this._metadataCounter % 500 == 0){
            LOGGER.warn("Session flushed");

            hb_session.flush();
            //hb_session.clear();
        }

        if (value == null || value.length() == 0)
        {
            return 0;
        }

        long metaDataID = 0;

        // Der SQLHelper baut die Insert-Anfrage zusammen
        if (this._metaDataMap.containsKey(name))
        {
            metaDataID = (Long) this._metaDataMap.get(name);
        }
        else
        {
            //Create metadata_parameter
            MetadataParameter mp = new MetadataParameter();
            mp.setParamName(name);
            mp.setCodeSystem(this.codesystem);

            hb_session.save(mp);

            metaDataID = mp.getId();
            LOGGER.debug("[ImportClaml.java] Neues metadata_parameter mit ID: " + metaDataID + " und name: " + name);

            this._metaDataMap.put(name, metaDataID);
            this._metadataParameterMap.put(metaDataID, mp);

            CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
            mv.setParameterValue(value);

            mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
            mv.getCodeSystemEntityVersion().setVersionId((Long) this._referenceMap.get(code));

            //Matthias
            //Get metaDataparameter
            if (this._metadataParameterMap.containsKey(metaDataID))
            {
                MetadataParameter temp = this._metadataParameterMap.get(metaDataID);
                mv.setMetadataParameter(temp);
            }
            else
            {
                //should never reach the else case
                mv.setMetadataParameter(new MetadataParameter());
                mv.getMetadataParameter().setId(metaDataID);
            }

            hb_session.save(mv);

            metaDataID = mv.getId();
            return metaDataID;
        }

        //Create parameter_value
        if (metaDataID == 0)
        {
            LOGGER.warn("metaDataID ist 0 für: " + name);
        }

        String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
        hql += " join csmv.metadataParameter mp join csmv.codeSystemEntityVersion csev";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("mp.", "id", metaDataID);
        parameterHelper.addParameter("csev.", "versionId", this._referenceMap.get(code));

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        LOGGER.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        List<CodeSystemMetadataValue> valueList = q.list();

        if (valueList.size() == 1)
        {
            valueList.get(0).setParameterValue(value);
            hb_session.update(valueList.get(0));
        }
        else if (valueList.isEmpty())
        {
            CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
            mv.setParameterValue(value);

            mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
            mv.getCodeSystemEntityVersion().setVersionId((Long) this._referenceMap.get(code));

            //Matthias
            //Get metaDataparameter
            if (this._metadataParameterMap.containsKey(metaDataID))
            {
                MetadataParameter temp = this._metadataParameterMap.get(metaDataID);
                mv.setMetadataParameter(temp);
            }
            else
            {
                //should never reach the else case
                mv.setMetadataParameter(new MetadataParameter());
                mv.getMetadataParameter().setId(metaDataID);
            }
            hb_session.save(mv);
        }
        
        return metaDataID;
    }
}
