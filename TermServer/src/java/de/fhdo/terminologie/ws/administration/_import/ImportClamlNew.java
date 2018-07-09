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
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

/**
 *
 * @author puraner
 */
public class ImportClamlNew extends CodeSystemImport implements ICodeSystemImport
{

    //Properties
    private HashMap _referenceMap;
    private HashMap _metaDataMap;
    private HashMap<Long, MetadataParameter> _metadataParameterMap;
    private HashMap<String, String> _clamlMetaData;
    private ConcurrentHashMap<String, clamlBindingXSD.Class> _clamlClassMap;
    private AssociationType _assoctypeTaxonomy;
    private HashMap _ccatresptHashmap;
    private CreateConceptAssociationTypeResponseType _ccatrespt;
    private CreateConceptAssociationTypeResponseType _ccatresptTaxonomy;
    private HashMap _assoctypeHashmap;
    private CreateConceptResponseType _ccsResponse;
    private int _metadataCounter = 0;

    public ImportClamlNew()
    {
        super();
        this._referenceMap = new HashMap();
        this._metaDataMap = new HashMap();
        this._metadataParameterMap = new HashMap<Long, MetadataParameter>();
        this._clamlMetaData = new HashMap<String, String>();
        this._clamlClassMap = new ConcurrentHashMap<String, clamlBindingXSD.Class>();
        this._ccatresptHashmap = new HashMap();
        this._assoctypeHashmap  = new HashMap();
        this._aktCount = 0;
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
            this.hb_session = HibernateUtil.getSessionFactory().openSession();
            this.hb_session.getTransaction().begin();
            this.hb_session.setFlushMode(FlushMode.COMMIT);
            this.validateParameters();
        }
        catch (HibernateException ex)
        {
            logger.error(ex);
            this.rollbackHibernateTransaction();
            this.closeHibernateSession();
            throw new ImportException(ex.getLocalizedMessage());
            
        }
        catch (ImportParameterValidationException ex)
        {
            logger.error(ex);
            this.rollbackHibernateTransaction();
            this.closeHibernateSession();
            throw ex;
        }
        
        //adding status of import to statuslist
        this._status.setImportRunning(true);
        StaticStatusList.addStatus(this.getImportId(), this._status);
               
        try
        {
            logger.debug("Oeffne Datei...");

            logger.debug("wandle zu InputStream um...");
            InputStream is = new ByteArrayInputStream(this._fileContent);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            NodeList list = doc.getElementsByTagName("Class");

            logger.info("Total of " + list.getLength() + " will be imported.");
            this.setTotalCountInStatusList(list.getLength(), this.getImportId());

            logger.info("loadClamlXML()");
            is = new ByteArrayInputStream(this._fileContent);
            this.loadClamlXML(is);

            this._status = StaticStatusList.getStatus(this.getImportId());

            if (this._status != null && this._status.isCancel())
            {
                hb_session.getTransaction().rollback();
            }
            else
            {
                hb_session.flush();
                hb_session.getTransaction().commit();
            }

        }
        catch (HibernateException ex)
        {
            logger.error("ImportClaml error: " + ex.getLocalizedMessage());
            logger.error(ex);

            try
            {
                //DABACA
                if(!hb_session.getTransaction().wasRolledBack())
                    hb_session.getTransaction().rollback();
                logger.info("[ImportClaml.java] Rollback durchgeführt!");
            }
            catch (Exception exRollback)
            {
                logger.info(exRollback.getMessage());
                logger.info("[ImportClaml.java] Rollback fehlgeschlagen!");
            }
            
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (SAXException ex)
        {
            logger.error(ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (IOException ex)
        {
            logger.error(ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (ParserConfigurationException ex)
        {
            logger.error(ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        catch (Exception ex)
        {
            logger.error(ex);
            throw new ImportException(ex.getLocalizedMessage());
        }
        finally
        {
            try
            {
                //DABACA
                if(!hb_session.getTransaction().wasRolledBack())
                    hb_session.getTransaction().rollback();
                logger.info("[ImportClaml.java] Rollback durchgeführt!");
            }
            catch (Exception exRollback)
            {
                logger.info(exRollback.getMessage());
                logger.info("[ImportClaml.java] Rollback fehlgeschlagen!");
            }
            //currentTask = "";
            //percentageComplete = 0.0;

            // Session schließen
            if(hb_session != null && hb_session.isConnected())
            {
                hb_session.close();
            }
            

            //isRunning = false;
            logger.info("ImportClaml fertig");
        }
        
        //TODO create proposals here
    }

    /**
     * Importiert die ClaML-XML Datei
     */
    private void loadClamlXML(InputStream is) throws Exception
    {
        logger.debug("Create JAXBContext");

        clamlBindingXSD.Class clazz = null;
        clamlBindingXSD.Rubric rubi = null;
        clamlBindingXSD.RubricKinds rks = new clamlBindingXSD.RubricKinds();

        // First create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        // Setup a new eventReader
        XMLEventReader eventReader = inputFactory.createXMLEventReader(is);

        // Read the XML document
        logger.debug("Analyze data...");

        //Attribute für CreateCodeSystem
        String authority = "";
        String uid = "";

        int countEvery = 0;

        while (eventReader.hasNext())
        {
            this._status = StaticStatusList.getStatus(this.getImportId());
            if (this._status != null && this._status.isCancel())
            {
                break;
            }

            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement())
            {
                StartElement startElement = event.asStartElement();
                String startElementName = startElement.getName().toString();

                if (startElementName.equals("Title"))
                {
                    Date datum = new Date();
                    String title = "";
                    String versionName = "";

                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext())
                    {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("name"))
                        {
                            title = attribute.getValue();
                        }
                        if (attribute.getName().toString().equals("version"))
                        {
                            versionName = attribute.getValue();                            
                        }
                        if (attribute.getName().toString().equals("date"))
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            if (!attribute.getValue().equals(""))
                            {
                                datum = sdf.parse(attribute.getValue());
                            }
                        }
                    }

                    event = eventReader.nextEvent();

                    //Matthias 23.04.2015, 
                    //if <Title>-Element does not have content --> description will be set to ""
                    if (event.isEndElement())
                    {
                        this.createCodeSystem(title, uid, versionName, datum, authority, "");
                        continue;
                    }

                    String description = event.asCharacters().getData();
                    this.createCodeSystem(title, uid, versionName, datum, authority, description);
                }
                else if (startElementName.equals("Identifier"))
                {
                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext())
                    {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("authority"))
                        {
                            authority = attribute.getValue();
                        }
                        if (attribute.getName().toString().equals("uid"))
                        {
                            uid = attribute.getValue();
                        }
                    }

                }
                else if (startElementName.equals("RubricKind"))
                {
                    clamlBindingXSD.RubricKind rk = new clamlBindingXSD.RubricKind();

                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext())
                    {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("name"))
                        {
                            rk.setName(attribute.getValue());
                            rks.getRubricKind().add(rk);
                        }
                    }
                }
                //  if (startElement.getName().toString().equals("Class") || startElement.getName().toString().equals("Modifier") ||startElement.getName().toString().equals("Class") || startElement.getName().toString().equals("ModifierClass") ) {
                else if (startElementName.equals("Class"))
                {
                    clazz = new clamlBindingXSD.Class();

                    // We read the attributes from this tag and add the date attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext())
                    {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals("code"))
                        {
                            clazz.setCode(attribute.getValue());                    
                        }
                        if (attribute.getName().toString().equals("kind"))
                        {
                            clazz.setKind(attribute.getValue());                                                        
                        }
                    }
                }
                else if (startElementName.equals("Rubric"))
                {
                    if (clazz != null)
                    {
                        rubi = new clamlBindingXSD.Rubric();
                        // We read the attributes from this tag and add the date attribute to our object
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext())
                        {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("kind"))
                            {
                                rubi.setKind(attribute.getValue());
                            }
                        }
                        clazz.getRubric().add(rubi);
                    }

                }
                else if (startElementName.equals("Label"))
                {
                    if (rubi != null)
                    {
                        event = eventReader.nextEvent();
                        //if(event.isCharacters()){
                        clamlBindingXSD.Label l = new clamlBindingXSD.Label();
                        l.getContent().add(event.asCharacters().getData());
                        
                        rubi.getLabel().add(l);
                        continue;
                        //}
                    }

                }
                else if (startElementName.equals("Fragment"))
                {
                    if (rubi != null)
                    {
                        if (event.isEndElement() == false)
                        {
                            event = eventReader.nextEvent();

                            if (event.isEndElement() == false)
                            {
                                clamlBindingXSD.Label l = new clamlBindingXSD.Label();
                                l.getContent().add(event.asCharacters().getData());
                                rubi.getLabel().add(l);
                            }
                            else
                            {
                                logger.debug("kein Text, da End-Element");
                            }
                        }
                        continue;
                    }

                }
                else if (startElementName.equals("SuperClass"))
                {
                    if (clazz != null)
                    {
                        clamlBindingXSD.SuperClass sc = new clamlBindingXSD.SuperClass();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext())
                        {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("code"))
                            {
                                sc.setCode(attribute.getValue());
                            }
                        }

                        clazz.getSuperClass().add(sc);
                    }

                }
                else if (startElementName.equals("SubClass"))
                {
                    if (clazz != null)
                    {
                        clamlBindingXSD.SubClass sc = new clamlBindingXSD.SubClass();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext())
                        {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("code"))
                            {
                                sc.setCode(attribute.getValue());
                            }
                        }

                        clazz.getSubClass().add(sc);
                    }

                }
                else if (startElementName.equals("Meta"))
                {
                    if (clazz != null)
                    {
                        Meta meta = new Meta();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext())
                        {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name"))
                            {
                                meta.setName(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals("value"))
                            {
                                meta.setValue(attribute.getValue());
                            }
                        }
                        clazz.getMeta().add(meta);
                    }
                    else
                    {
                        //Claml/Metadata

                        Iterator<Attribute> attributes = startElement.getAttributes();
                        String name = "";
                        String value = "";
                        while (attributes.hasNext())
                        {
                            Attribute attribute = attributes.next();

                            if (attribute.getName().toString().equals("name"))
                            {
                                name = attribute.getValue();
                            }
                            if (attribute.getName().toString().equals("value"))
                            {
                                value = attribute.getValue();
                            }
                        }

                        this._clamlMetaData.put(name, value);
                    }
                }
            } // End start element
            if (event.isEndElement())
            {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().toString().equals("Class"))
                {
                    // TODO rm
                    //if (clazz.getKind() == null || paramter.getClasskinds().indexOf(clazz.getKind().toString()) >= 0)

                    //Matthias: write clazz to map to be processed later
                    this._clamlClassMap.put(clazz.getCode(), clazz);
                    logger.info("Concept reading: " + clazz.getCode() + "(" + this._clamlClassMap.size() + ")");
                    
                    //DABACA
                     //This try-catch block is needed to keep the ZK-framework alive while big code-systems
                    //are transfered to the publication-platform.
                    try{
                        Window testWin = (Window)Executions.createComponents("", null, null);
                    }
                    catch(Exception e){
                    }
                    finally{}
                    
                    /*
                    // Jetzt Konzept erstellen
                    this.CreateSingleConcept(clazz);
                    if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
                    {
                        this.createMetaData(clazz);
                    }
                    */
                }
            }
            if (event.isEndElement())
            {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().toString().equals("RubricKinds"))
                {
                    //CreateAssociationType (Unterklasse,Oberklasse)
                    this._assoctypeTaxonomy = this.CreateAssociationType("ist Oberklasse von", "ist Unterklasse von");

                    logger.debug(this._ccatrespt.getReturnInfos().getMessage());
                    if (this._ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
                    {
                        // System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
                        this._ccatresptTaxonomy = this._ccatrespt;

                        //AssociationTypes für alle RubricKinds erstellen
                        Iterator itRubricKinds = rks.getRubricKind().iterator();
                        //System.out.println("anzrk:" + rks.getRubricKind().size());
                        while (itRubricKinds.hasNext())
                        {
                            RubricKind rk = (RubricKind) itRubricKinds.next();

                            AssociationType assoctypeTemp = this.CreateAssociationType(rk.getName(), rk.getName());
                            this._assoctypeHashmap.put(rk.getName(), assoctypeTemp);
                            // System.out.println("vorher");

                            logger.debug(this._ccatrespt.getReturnInfos().getMessage());
                            if (this._ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
                            {
                                //  System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
                                this._ccatresptHashmap.put(rk.getName(), this._ccatrespt);
                            }
                            //  System.out.println("nacher");
                        }
                    }
                }
            }

            if (countEvery % 100000 == 0)
            {

                // sicherheitshalber aufrufen
                System.gc();

            }
            countEvery++;

        }

        Collection<clamlBindingXSD.Class> clazzCollection = this._clamlClassMap.values();
        int counter = 0;
        for (clamlBindingXSD.Class claz : clazzCollection)
        {
            counter++;
            importClazz(claz);

            if (counter % 500 == 0)
            {
                //Wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
                logger.warn("Flushed: " + counter);
                hb_session.flush();
                //hb_session.clear();

                if (countEvery % 5000 == 0)
                {
                    //Sicherheitshalber aufrufen, versucht den garbage collector laufen zu lassen
                    System.gc();
                }
            }
        }

        boolean stop = true;

    }

    private void createCodeSystem(String title, String uid, String versionName, Date date, String authority, String description) throws Exception
    {
        // Codesystem suchen, erst anlegen, wenn nicht vorhanden
        CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();

        if (this._codesystem.getId() > 0)
        {
            request.setCodeSystem(this._codesystem);
        }
        else
        {
            request.setCodeSystem(new CodeSystem());
        }
        request.getCodeSystem().setName(title);

        CodeSystemVersion codeSystemVersion = new CodeSystemVersion();
        codeSystemVersion.setName(title + " " + versionName);
        //codeSystemVersion.setDescription(description);
        codeSystemVersion.setSource(authority);
        codeSystemVersion.setReleaseDate(date);
        codeSystemVersion.setOid(uid);

        if (this.getImportType().getRole() == null || !this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
        {
            codeSystemVersion.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
        }
        else
        {
            codeSystemVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        if (this._clamlMetaData.get("description") != null)
        {
            codeSystemVersion.setDescription(this._clamlMetaData.get("description"));
        }

        if (this._clamlMetaData.get("unvollstaendig") != null)
        {
            if (this._clamlMetaData.get("unvollstaendig").equals("true"))
            {
                request.getCodeSystem().setIncompleteCS(true);
            }
            else
            {
                request.getCodeSystem().setIncompleteCS(false);
            }
        }

        if (this._clamlMetaData.get("description_eng") != null)
        {
            request.getCodeSystem().setDescriptionEng(this._clamlMetaData.get("description_eng"));
        }

        if (this._clamlMetaData.get("gueltigkeitsbereich") != null)
        {
            codeSystemVersion.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(this._clamlMetaData.get("gueltigkeitsbereich")));
        }

        if (this._clamlMetaData.get("verantw_Org") != null)
        {
            request.getCodeSystem().setResponsibleOrganization(this._clamlMetaData.get("verantw_Org"));
        }

        if (this._clamlMetaData.get("version_description") != null)
        {
            codeSystemVersion.setDescription(this._clamlMetaData.get("version_description"));
        }

        request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
        request.getCodeSystem().getCodeSystemVersions().add(codeSystemVersion);

        request.setLogin(this.getLoginType());

        //Code System erstellen
        CreateCodeSystem ccs = new CreateCodeSystem();
        CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session);

        logger.debug(resp.getReturnInfos().getMessage());

        if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
        {
            throw new Exception(resp.getReturnInfos().getMessage());
        }
        this._codesystem = resp.getCodeSystem();

        logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
        logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());

        // Read existing metadata and add to map to avoid double entries
        String hql = "select distinct mp from MetadataParameter mp "
                + " where codeSystemId=" + resp.getCodeSystem().getId();
        List<MetadataParameter> md_list = hb_session.createQuery(hql).list();

        for (MetadataParameter mp : md_list)
        {
            this._metaDataMap.put(mp.getParamName(), mp.getId());
            this._metadataParameterMap.put(mp.getId(), mp);
            logger.debug("found metadata: " + mp.getParamName() + " with id: " + mp.getId());
        }
    }
    
    private AssociationType CreateAssociationType(String forwardName, String reverseName)
    {
        //Associationtype erstellen
        //EntityType erstellen
        CodeSystemEntity etAssoc = new CodeSystemEntity();
        //TODO IsAxis ist nicht in CodeSystemEntity sondern in CodeSystemVersionEntityMembership
        //etAssoc.setIsAxis(false);
        //TODO Ob das wohl so richtig ist.
        Set<CodeSystemVersionEntityMembership> memlist = etAssoc.getCodeSystemVersionEntityMemberships();
        Iterator memIter = memlist.iterator();
        while (memIter.hasNext())
        {
            CodeSystemVersionEntityMembership csvem = (CodeSystemVersionEntityMembership) memIter.next();
            //if(csvem.) wenn es die korrekte Verbindung der beiden Tabellen ist, dann nur setzen
            csvem.setIsAxis(Boolean.FALSE);
        }

        Set<AssociationType> assoList = new HashSet<AssociationType>();
        AssociationType assoctype = new AssociationType();
        assoctype.setForwardName(forwardName);
        assoctype.setReverseName(reverseName);
        assoList.add(assoctype);

        Set<CodeSystemEntityVersion> evlistAssoc = new HashSet<CodeSystemEntityVersion>();
        //EntityVersionType erstellen
        CodeSystemEntityVersion evtAssoc = new CodeSystemEntityVersion();
        evtAssoc.setMajorRevision(0);
        evtAssoc.setMinorRevision(0);
        evtAssoc.setAssociationTypes(assoList);
        evtAssoc.setIsLeaf(true);

        evlistAssoc.add(evtAssoc);

        etAssoc.setCodeSystemEntityVersions(evlistAssoc);

        CreateConceptAssociationTypeRequestType ccatrt = new CreateConceptAssociationTypeRequestType();
        ccatrt.setCodeSystemEntity(etAssoc);

        ccatrt.setLogin(this.getLoginType());

        CreateConceptAssociationType ccat = new CreateConceptAssociationType();
        this._ccatrespt = ccat.CreateConceptAssociationType(ccatrt, hb_session);

        return assoctype;

    }
    
    private void importClazz(clamlBindingXSD.Class clazz) throws ImportException
    {

        if (clazz.getSuperClass() != null && clazz.getSuperClass().iterator().hasNext())
        {
            String code = clazz.getSuperClass().iterator().next().getCode();
            if (this._clamlClassMap.get(code) != null)
            {
                importClazz(this._clamlClassMap.get(code));
            }
        }

        try
        {

            if (this._clamlClassMap.get(clazz.getCode()) != null)
            {
                this.CreateSingleConcept(clazz);
                logger.debug("Concept writing: " + clazz.getCode() + "(" + this._clamlClassMap.size() + ")");
                if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
                {
                    this.createMetaData(clazz);
                }

                this._clamlClassMap.remove(clazz.getCode());
            }

        }
        catch (Exception ex)
        {
            logger.error(ex);
            throw new ImportException(ex.getLocalizedMessage());
        }

    }
    
    private void CreateSingleConcept(clamlBindingXSD.Class cl) throws Exception
    {
        //Konzepte erstellen
        String code = "";
        String rubKind = "";
        String labelString = "";

        clamlBindingXSD.Class clazz = cl;
        code = clazz.getCode();
        //clazz.getKind()  // TODO Kind abspeichern für Export

        //Status aktualisieren
        //this.setCurrentCountInStatusList(++this._aktCount, this.getImportId());;
        this.setCurrentTaskInStatusList(code, this.getImportId());

        Iterator it2 = clazz.getRubric().iterator();
        Iterator it3 = clazz.getRubric().iterator();

        boolean found = false;
        // Alle Rubrics Durchlaufen
        // Das erste mal um preferred zu suchen und anzulegen, dann das zweite mal um alle anderen anzulegen
        Rubric rubi = null;
        while (it2.hasNext())
        {
            rubi = (Rubric) it2.next();
            rubKind = (String) rubi.getKind();

            if (rubKind.equals(RubricKinds.RUBRICKINDS.preferred.getCode()))
            {
                found = true;
                labelString = getAllRubricStrings(rubi);
                this.createPrefferedTerm(labelString, code, clazz);
            }
        }
        if (found == false && rubi != null)
        {
            rubi.setKind(RubricKinds.RUBRICKINDS.preferred.getCode());
            labelString = getAllRubricStrings(rubi);
            this.createPrefferedTerm(labelString, code, clazz);
        }

        while (it3.hasNext() && found == true)
        {
            rubi = (Rubric) it3.next();

            rubKind = (String) rubi.getKind();

            if (!(rubKind.equals(RubricKinds.RUBRICKINDS.preferred.getCode())
                    || rubKind.equals(RubricKinds.RUBRICKINDS.note.getCode())))
            {
                labelString = getAllRubricStrings(rubi);
                this.createNotPrefferdTerm(labelString, code, clazz, rubKind);
            }
        }
    }
    
    private String getAllRubricStrings(Rubric rubric)
    {
        String returnString = "";
        Iterator itRubric = rubric.getLabel().iterator();
        while (itRubric.hasNext())
        {
            Label label = (Label) itRubric.next();
            List contentList = label.getContent();
            Iterator itContent = contentList.iterator();
            while (itContent.hasNext())
            {
                Object o = itContent.next();

                if (o instanceof String)
                {
                    returnString = returnString + o.toString();
                }
                else
                {
                    if (o instanceof Fragment)
                    {
                        Fragment fragment = (Fragment) o;
                        List fragmentListe = fragment.getContent();
                        Iterator itFragmentContent = fragmentListe.iterator();
                        while (itFragmentContent.hasNext())
                        {
                            Object o2 = itFragmentContent.next();
                            if (o2 instanceof String)
                            {
                                returnString = returnString + o2.toString();

                            }
                        }
                    }
                    else
                    {
                        if (o instanceof Para)
                        {
                            Para para = (Para) o;
                            List paraListe = para.getContent();
                            Iterator itParaContent = paraListe.iterator();
                            while (itParaContent.hasNext())
                            {
                                Object o2 = itParaContent.next();
                                if (o2 instanceof String)
                                {
                                    returnString = returnString + o2.toString();

                                }
                            }
                        }
                    }
                }
            }
        }
        return returnString;
    }
    
    private void createPrefferedTerm(String labelString, String code, clamlBindingXSD.Class clazz) throws Exception
    {
        logger.debug("createPrefferedTerm mit Code: " + code + ", Text: " + labelString);

        CreateConceptRequestType request = new CreateConceptRequestType();

        // EntityType erstellen
        CodeSystemEntity cse = new CodeSystemEntity();
        CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
        csvem.setIsAxis(false);
        if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
        {
            csvem.setIsAxis(false);
            csvem.setIsMainClass(false);
        }
        else
        {
            csvem.setIsMainClass(true);
        }

        if (clazz.getKind() != null && clazz.getKind().equals("chapter"))
        {
            csvem.setIsMainClass(true);
        }

        cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
        cse.getCodeSystemVersionEntityMemberships().add(csvem);

        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
        csev.setMajorRevision(1);
        csev.setMinorRevision(0);
        csev.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

        //Matthias: testing
        clamlBindingXSD.Class claz = this._clamlClassMap.get(code);
        if (claz.getSubClass() != null && claz.getSubClass().iterator().hasNext())
        {
            csev.setIsLeaf(false);
        }
        else
        {
            csev.setIsLeaf(true);  // erstmal true, wird per Trigger auf false gesetzt, wenn eine Beziehung eingefügt wird
        }

        CodeSystemConcept csc = new CodeSystemConcept();
        csc.setCode(code);
        csc.setTerm(labelString);
        csc.setTermAbbrevation("");
        csc.setIsPreferred(true);

        for (Rubric r : clazz.getRubric())
        {
            if (r.getKind().equals(RubricKinds.RUBRICKINDS.note.getCode()))
            {
                csc.setDescription(r.getLabel().get(0).getContent().get(0).toString());
            }
        }

        csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
        csev.getCodeSystemConcepts().add(csc);

        cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        cse.getCodeSystemEntityVersions().add(csev);

        addAttributeMetadata(clazz, csev, csc);

        logger.debug("isMainClass: " + csvem.getIsMainClass());

        request.setCodeSystem(this._codesystem);
        request.setCodeSystemEntity(cse);
        request.setLogin(this.getLoginType());

        //Konzept erstellen
        CreateConcept cc = new CreateConcept();
        this._ccsResponse = cc.CreateConcept(request, hb_session);

        logger.debug("[ImportClaml.java]" + this._ccsResponse.getReturnInfos().getMessage());
        if (this._ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
            if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
            {
                this.createSuperclassAssociation(code, clazz);
            }

            //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
            long aktEntityVersionID = 0;

            if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
            {
                aktEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
                this._referenceMap.put(code, aktEntityVersionID);
            }

            this._aktCount++;
            this.setCurrentCountInStatusList(this._aktCount, this.getImportId());
            if(this.getImportType().getRole() != null && this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
            {
                //Status bei Freigabe in log file ausgeben
                logger.info("Import progress: " + this._aktCount + "/" + this.getTotalCountInStatusList(this.getImportId()));
            }
        }
        else
        {
            throw new Exception();
        }
    }

    private void createNotPrefferdTerm(String labelString, String code, clamlBindingXSD.Class clazz, String rubKind) throws Exception
    {
        logger.debug("createNotPrefferdTerm mit Code: " + code + ", Text: " + labelString);
        //System.out.println("test4");
        CreateConceptRequestType request = new CreateConceptRequestType();

        //EntityType erstellen
        CodeSystemEntity cse = new CodeSystemEntity();
        CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
        csvem.setIsAxis(false);
        cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
        cse.getCodeSystemVersionEntityMemberships().add(csvem);

        //EntityVersionType erstellen
        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
        csev.setMajorRevision(1);
        csev.setMinorRevision(0);
        csev.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        csev.setIsLeaf(true);

        //TermType erstellen
        CodeSystemConcept csc = new CodeSystemConcept();
        csc.setCode(code);
        csc.setTerm(labelString);
        csc.setTermAbbrevation("");
        csc.setIsPreferred(false);

        csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
        csev.getCodeSystemConcepts().add(csc);

        cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        cse.getCodeSystemEntityVersions().add(csev);

        addAttributeMetadata(clazz, csev, csc);

        request.setCodeSystem(this._codesystem);
        request.setCodeSystemEntity(cse);
        request.setLogin(this.getLoginType());

        //Konzept erstellen
        CreateConcept cc = new CreateConcept();
        this._ccsResponse = cc.CreateConcept(request, hb_session);

        logger.debug("[ImportClaml.java]" + this._ccsResponse.getReturnInfos().getMessage());
        if (this._ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
            this.createTerm2TermAssociation(code, clazz, rubKind);
            
            this._aktCount++;
            this.setCurrentCountInStatusList(this._aktCount, this.getImportId());

        }
        else
        {
            //throw new Exception();
        }
    }
    
    private void addAttributeMetadata(clamlBindingXSD.Class clazz, CodeSystemEntityVersion csev, CodeSystemConcept csc) throws ImportException
    {
        if (clazz != null && csev != null && csc != null)
        {

            for (Meta meta : clazz.getMeta())
            {
                try
                {
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.hints.getCode()))
                    {
                        csc.setHints(meta.getValue());
                    }
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.meaning.getCode()))
                    {
                        csc.setMeaning(meta.getValue());
                    }
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.termAbbrevation.getCode()))
                    {
                        csc.setTermAbbrevation(meta.getValue());
                    }

                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.majorRevision.getCode()))
                    {
                        csev.setMajorRevision(Integer.parseInt(meta.getValue()));
                    }
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.minorRevision.getCode()))
                    {
                        csev.setMinorRevision(Integer.parseInt(meta.getValue()));
                    }
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.status.getCode()))
                    {
                        csev.setStatus(Integer.parseInt(meta.getValue()));
                    }
                    if (meta.getName().equals(MetadataDefinition.METADATA_ATTRIBUTES.statusDate.getCode()))
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        csev.setStatusDate(sdf.parse(meta.getValue()));
                    }
                }
                catch (Exception ex)
                {
                    logger.error(ex);
                    throw new ImportException(ex.getLocalizedMessage());
                }
            }
        }
    }
    
    private void createTerm2TermAssociation(String code, clamlBindingXSD.Class clazz, String rubkind) throws Exception
    {
        //aktuelle entityVersionID aus der Response merken:
        long aktEntityVersionID = 0;
        if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
        {
            aktEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
        }

        //den eigenen Code in der HashMap suchen (ist der Code des prefferdTerms)
        long prefferedTermEntityVersionID = (Long) this._referenceMap.get(code);

        CodeSystemEntityVersionAssociation evat = new CodeSystemEntityVersionAssociation();
        //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden

        evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(prefferedTermEntityVersionID);

        evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(aktEntityVersionID);

        evat.setAssociationKind(Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode());
        evat.setLeftId(prefferedTermEntityVersionID);

        evat.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        evat.setStatusDate(new Date());

        //AssociationType und Response aus der jeweiligen Hasmap holen
        CreateConceptAssociationTypeResponseType resp = (CreateConceptAssociationTypeResponseType) this._ccatresptHashmap.get(rubkind);
        AssociationType atype = (AssociationType) this._assoctypeHashmap.get(rubkind);
        atype.setCodeSystemEntityVersionId(resp.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());

        evat.setAssociationType(atype);

        CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
        //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
        //es besitzt jedoch eine andere Struktur

        ccar.setCodeSystemEntityVersionAssociation(evat);
        ccar.setLogin(this.getLoginType());

        CreateConceptAssociation cca = new CreateConceptAssociation();
        CreateConceptAssociationResponseType ccaresp = cca.CreateConceptAssociation(ccar, hb_session);
        logger.debug("[ImportClaml.java]" + ccaresp.getReturnInfos().getMessage());
        if (ccaresp.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
            logger.debug("[ImportClaml.java] Create Association Erfolgreich");

        }
        else
        {
            // throw new Exception();
        }
    }
    
    private void createSuperclassAssociation(String code, clamlBindingXSD.Class clazz) throws Exception
    {
        //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
        long aktEntityVersionID = 0;
        if (this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
        {
            aktEntityVersionID = this._ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
            this._referenceMap.put(code, aktEntityVersionID);
        }

        //Die erste SuperClass holen
        String superclazzCode = "";
        long superclazzEntityVersionID = 0;
        if (clazz.getSuperClass().iterator().hasNext())
        {
            superclazzCode = clazz.getSuperClass().iterator().next().getCode();
            //Superclass id in der Hashmap suchen
            superclazzEntityVersionID = (Long) this._referenceMap.get(superclazzCode);
            //System.out.println("superclassCode: " + superclazzCode + " superclassID:" + superclazzEntityVersionID + "aktCode" + code + " aktID:" + aktEntityVersionID);
        }

        CodeSystemEntityVersionAssociation evat = new CodeSystemEntityVersionAssociation();
        //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden
        evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(superclazzEntityVersionID);

        evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(aktEntityVersionID);

        evat.setAssociationKind(Definitions.ASSOCIATION_KIND.TAXONOMY.getCode());
        evat.setLeftId(superclazzEntityVersionID);
        evat.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        evat.setStatusDate(new Date());

        this._assoctypeTaxonomy.setCodeSystemEntityVersionId(this._ccatresptTaxonomy.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());
        evat.setAssociationType(this._assoctypeTaxonomy);

        CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
        //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
        //es besitzt jedoch eine andere Struktur

        ccar.setCodeSystemEntityVersionAssociation(evat);
        ccar.setLogin(this.getLoginType());
        CreateConceptAssociation cca = new CreateConceptAssociation();
        //TODO cca.CreateConceptAssociation( ist noch nicht implementiert

        CreateConceptAssociationResponseType ccaresp = cca.CreateConceptAssociation(ccar, hb_session);

        logger.debug("[ImportClaml.java]" + ccaresp.getReturnInfos().getMessage());
        if (ccaresp.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
            logger.debug("[ImportClaml.java] Create Association Erfolgreich");

        }
        else
        {
            logger.error("Fehler");
        }

    }
    
    private void createMetaData(clamlBindingXSD.Class clazz)
    {
        if (logger.isInfoEnabled())
        {
            logger.debug("createMetaData gestartet");
        }

        for (Meta meta : clazz.getMeta())
        {
            if (meta.getName() != null && meta.getName().length() > 0)
            {
                // Prüfen, ob es ein Metadatenattribut ist
                if (MetadataDefinition.METADATA_ATTRIBUTES.isCodeValid(meta.getName()) == false)
                {
                    long metaDataID = insertMetaData(meta.getName(), meta.getValue(), clazz.getCode());
                    if (metaDataID > 0)
                    {
                        logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
                    }
                }
            }
        }

        // ClassKind in Metadaten abspeichern, damit dieser wieder exportiert werden kann
        if (clazz.getKind() != null)
        {
            String classKind = clazz.getKind().toString();
            if (classKind.length() > 0)
            {
                long metaDataID = insertMetaData("ClaML_ClassKind", classKind, clazz.getCode());
                if (metaDataID > 0)
                {
                    logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
                }
            }
        }
    }
    
    private long insertMetaData(String name, String value, String code)
    {
        this._metadataCounter++;
        if (this._metadataCounter % 500 == 0)
        {
            logger.warn("Session flushed");

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
            mp.setCodeSystem(this._codesystem);

            hb_session.save(mp);

            metaDataID = mp.getId();
            logger.debug("[ImportClaml.java] Neues metadata_parameter mit ID: " + metaDataID + " und name: " + name);

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
            logger.warn("metaDataID ist 0 für: " + name);
        }

        String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
        hql += " join csmv.metadataParameter mp join csmv.codeSystemEntityVersion csev";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("mp.", "id", metaDataID);
        parameterHelper.addParameter("csev.", "versionId", this._referenceMap.get(code));

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        logger.debug("HQL: " + hql);

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
