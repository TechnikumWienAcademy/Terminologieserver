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
package de.fhdo.terminologie.ws.administration._import;

import clamlBindingXSD.Fragment;
import clamlBindingXSD.Label;
import clamlBindingXSD.Meta;
import clamlBindingXSD.Para;
import clamlBindingXSD.Rubric;
import clamlBindingXSD.RubricKind;
import de.fhdo.logging.Logger4j;
//import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.ws.types.LoginType;
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
import de.fhdo.terminologie.ws.administration.ImportStatus;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import de.fhdo.terminologie.ws.administration.claml.MetadataDefinition.METADATA_ATTRIBUTES;
import de.fhdo.terminologie.ws.administration.claml.RubricKinds.RUBRICKINDS;
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
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import org.apache.log4j.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
//import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
import org.hibernate.FlushMode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) Erweiterungen,
 * Anpassung an neue Version
 */
public class ImportClaml
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  //Variablen für ImportStatus
  public static boolean isRunning = false;
  public static double percentageComplete = 0.0;
  public static String currentTask = "";
  private CodeSystem codeSystem;
  //Attribute für CreateAssociationType
  private AssociationType assoctypeTaxonomy;
  private HashMap assoctypeHashmap = new HashMap();
  private HashMap ccatresptHashmap = new HashMap();
  private CreateConceptAssociationTypeResponseType ccatrespt;
  private CreateConceptAssociationTypeResponseType ccatresptTaxonomy;
  //Attribute für CreateAllConcepts
  private CreateConceptResponseType ccsResponse;
  private HashMap referenceMap;
  private HashMap metaDataMap;
  private LoginType login;
  private int aktCount = 0;
  private org.hibernate.Session hb_session;
  private int countImported = 0;
  private ImportType importType;
  private ImportStatus _status;
  private Long _importId = 0L;
  
  private HashMap<Long, MetadataParameter> metadataParameterMap = new HashMap<Long, MetadataParameter>();
	private HashMap<String, String> clamlMetaData = new HashMap<String, String>();
	private ConcurrentHashMap<String, clamlBindingXSD.Class> clamlClassMap = new ConcurrentHashMap<String, clamlBindingXSD.Class>();

  public ImportClaml(ImportCodeSystemRequestType request)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportClaml Constructor ======");
    
    _status = new ImportStatus();
    _status.importTotal = 0;
    _status.importCount = 0;
    _status.importRunning = true;
    _status.exportRunning = false;
    _status.cancel = false;
    
    if(request.getImportId() != null)
    {
        _importId = request.getImportId();
        StaticStatusList.addStatus(request.getImportId(), _status);
    }
    else
    {
        StaticStatusList.addStatus(0L, _status);
    }
    
    isRunning = true;

    this.login = request.getLogin();
    this.codeSystem = request.getCodeSystem();
    this.referenceMap = new HashMap();
    this.metaDataMap = new HashMap();
    this.importType = request.getImportInfos();

    // Hibernate-Block, Session öffnen
    hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
		
		//Matthias: Testing flushmode commit
		//Session is flushed manually anyway after some write cycles
		hb_session.setFlushMode(FlushMode.COMMIT);

    try // try-catch-Block zum Abfangen von Hibernate-Fehlern
    {
      logger.debug("Oeffne Datei...");
      byte[] bytes = request.getImportInfos().getFilecontent();
      //ByteArrayDataSourace bads = new ByteArrayDataSource(request.getImportInfos().getFilecontent(), "text/xml; charset=UTF-8");

      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(is);
        NodeList list = doc.getElementsByTagName("Class");

        logger.info("Total of " + list.getLength() + " will be imported.");
        StaticStatusList.getStatus(_importId).setImportTotal(list.getLength());

        logger.info("loadClamlXML()");
        is = new ByteArrayInputStream(bytes);
      this.loadClamlXML(is);

      _status = StaticStatusList.getStatus(_importId);
      
      if (_status != null && _status.cancel)
      {
        hb_session.getTransaction().rollback();
      }
			else{
				hb_session.flush();
        hb_session.getTransaction().commit();
			}
				
    }
    catch (Exception ex)
    {
      logger.error("ImportClaml error: " + ex.getLocalizedMessage());
      ex.printStackTrace();

      logger.debug(ex.getMessage());
      try
      {
          if(!hb_session.getTransaction().wasRolledBack()){
                hb_session.getTransaction().rollback();
                logger.info("[ImportClaml.java] Rollback durchgeführt!");
          }
      }
      catch (Exception exRollback)
      {
            if(!hb_session.getTransaction().wasRolledBack()){
                logger.info(exRollback.getMessage());
                logger.info("[ImportClaml.java] Rollback fehlgeschlagen!");
            }
      }

      //LoggingOutput.outputException(ex, this);

      //throw ex;
    }
    finally
    {
      currentTask = "";
      percentageComplete = 0.0;

      // Session schließen
      hb_session.close();
			

      isRunning = false;
			logger.info("ImportClaml fertig");
    }
  }

  /**
   * Importiert die ClaML-XML Datei
   */
  private void loadClamlXML(InputStream is) throws Exception
  {

    logger.debug("create JAXBContext");

    clamlBindingXSD.Class clazz = null;
    clamlBindingXSD.Rubric rubi = null;
    clamlBindingXSD.RubricKinds rks = new clamlBindingXSD.RubricKinds();

    /*String packagename = clamlBindingXSD.ClaML.class.getPackage().getName();
     JAXBContext jc = JAXBContext.newInstance(packagename);
     Unmarshaller u = jc.createUnmarshaller();*/
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
        _status = StaticStatusList.getStatus(_importId);
      if (_status != null && _status.cancel)
        break;

      XMLEvent event = eventReader.nextEvent();

      if (event.isStartElement())
      {
        StartElement startElement = event.asStartElement();
        String startElementName = startElement.getName().toString();
        //logger.debug("Start-Element: " + startElementName);
        //logger.debug("Is-End-Element: " + startElement.isEndElement());

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
							if (!attribute.getValue().equals("")){
								datum = sdf.parse(attribute.getValue());
							}
              
            }
          }

          //String description = "";
          event = eventReader.nextEvent();
		  
		  //Matthias 23.04.2015, 
		  //if <Title>-Element does not have content --> description will be set to ""
			
			
		  if (event.isEndElement()){
			  this.createCodeSystem(title, uid, versionName, datum, authority, "");
			  continue;
		  }
		  
          String description = event.asCharacters().getData();
          logger.debug("description: " + description);

          this.createCodeSystem(title, uid, versionName, datum, authority, description);
          /*if (paramter.isCreateNewVocabulary())
           {
           this.createCodeSystem(title, uid, versionName, authority);
           }
           else
           {
           this.maintainVocabularyVersion();
           }*/
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
              // System.out.println("CODE:  " + attribute.getValue().toString());
              clazz.setCode(attribute.getValue());

            }
            if (attribute.getName().toString().equals("kind"))
            {
              clazz.setKind(attribute.getValue());
              // System.out.println(clazz.getKind());
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
              
              if(event.isEndElement() == false)
              {
            clamlBindingXSD.Label l = new clamlBindingXSD.Label();
            l.getContent().add(event.asCharacters().getData());
            rubi.getLabel().add(l);
              }
              else logger.debug("kein Text, da End-Element");
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
          } else {
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
						
						clamlMetaData.put(name, value);
					}
        }

      } // End start element

	  if (event.isEndElement())
      {
        EndElement endElement = event.asEndElement();
        //  if (endElement.getName().toString().equals("Class")||endElement.getName().toString().equals("Modifier")||endElement.getName().toString().equals("ModifierClass")) {
        if (endElement.getName().toString().equals("Class"))
        {
          //System.out.println(clazz.getCode());
          // TODO rm
          //if (clazz.getKind() == null || paramter.getClasskinds().indexOf(clazz.getKind().toString()) >= 0)
          
					//Matthias: write clazz to map to be processed later
					clamlClassMap.put(clazz.getCode(), clazz);
					logger.info("Concept reading: " + clazz.getCode() + "(" + clamlClassMap.size() + ")");
					
					/*{
            // Jetzt Konzept erstellen
            this.CreateSingleConcept(clazz);
            if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
            {
              this.createMetaData(clazz);
            }
          }*/
        }
      } 
      if (event.isEndElement())
      {
        EndElement endElement = event.asEndElement();
        if (endElement.getName().toString().equals("RubricKinds"))
        {
          //CreateAssociationType (Unterklasse,Oberklasse)
          this.assoctypeTaxonomy = this.CreateAssociationType("ist Oberklasse von", "ist Unterklasse von");

          logger.debug(this.ccatrespt.getReturnInfos().getMessage());
          if (this.ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
          {
            // System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
            this.ccatresptTaxonomy = this.ccatrespt;

            //AssociationTypes für alle RubricKinds erstellen
            Iterator itRubricKinds = rks.getRubricKind().iterator();
            //System.out.println("anzrk:" + rks.getRubricKind().size());
            while (itRubricKinds.hasNext())
            {
              RubricKind rk = (RubricKind) itRubricKinds.next();

              AssociationType assoctypeTemp = this.CreateAssociationType(rk.getName(), rk.getName());
              this.assoctypeHashmap.put(rk.getName(), assoctypeTemp);
              // System.out.println("vorher");

              logger.debug(this.ccatrespt.getReturnInfos().getMessage());
              if (this.ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
              {
                //  System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
                this.ccatresptHashmap.put(rk.getName(), this.ccatrespt);
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
		
		Collection<clamlBindingXSD.Class> clazzCollection = clamlClassMap.values();
		int counter = 0;
		for (clamlBindingXSD.Class claz : clazzCollection){
			counter++;	
			importClazz(claz);
			
			if (counter % 500 == 0){
				//logger.debug("FreeMemory: " + runtime.freeMemory());
        // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
				logger.warn("Flushed: " + counter);
        hb_session.flush();
        //hb_session.clear();
				
				

        if (countEvery % 5000 == 0)
        {
          // sicherheitshalber aufrufen
          System.gc();
        }
			}
		}
		
		boolean stop = true;

  }
	
	private void importClazz(clamlBindingXSD.Class clazz){
		
		if (clazz.getSuperClass() != null && clazz.getSuperClass().iterator().hasNext()){
			String code = clazz.getSuperClass().iterator().next().getCode();
			if (clamlClassMap.get(code) != null){
				importClazz(clamlClassMap.get(code));
			}
		}
			
		try {
			
			if (clamlClassMap.get(clazz.getCode()) != null){
				this.CreateSingleConcept(clazz);
				logger.debug("Concept writing: " + clazz.getCode() + "(" + clamlClassMap.size() + ")");
				if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
				{
					this.createMetaData(clazz);
				}

				clamlClassMap.remove(clazz.getCode());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Fehler::" + e);
		}
		
	}

  /*private void maintainVocabularyVersion() throws Exception
   {
   logger.debug("[ImportClaml.java] Maintain CodeSystemVersion");
   //this.auth = new Authoring();
   MaintainCodeSystemVersionRequestType req = new MaintainCodeSystemVersionRequestType();
   VersioningType v = new VersioningType();
   v.setCreateNewVersion(Boolean.TRUE); //setCreateEmptyVerion(true);
  
   req.setCodeSystem(codeSystem);
   req.setLogin(login);  //setLoginType(login);
   req.setVersioning(v); //setVersioningType(v);
  
   //TODO rausgenommen
   MaintainCodeSystemVersionResponseType resp = null;//auth.MaintainCodeSystemVersion(req);
   logger.info("[ImportClaml.java]" + resp.getReturnInfos().getMessage());
   if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
   {
   throw new Exception("[ImportClaml.java] Maintain Vocabulary Version fehlgeschlagen");
   }
   }*/
  public void createCodeSystem(String title, String uid, String versionName, Date date, String authority, String description) throws Exception
  {
    // Codesystem suchen, erst anlegen, wenn nicht vorhanden
    //this.codeSystem // <- Request

    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();

    if (codeSystem.getId() > 0)
    {
      request.setCodeSystem(codeSystem);
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
		
//		if (clamlMetaData.get("statusCode") != null){
//			int temp = Integer.parseInt(clamlMetaData.get("statusCode"));
//			codeSystemVersion.setStatus(Integer.parseInt(clamlMetaData.get("statusCode")));
//		}
		
    if(importType.getRole() == null || !importType.getRole().equals(CODES.ROLE_TRANSFER))
    {
      codeSystemVersion.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
    }
    else
    {
      codeSystemVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
    }
    
		if (clamlMetaData.get("description") != null){
			codeSystemVersion.setDescription(clamlMetaData.get("description"));
		}
		
		if (clamlMetaData.get("unvollstaendig") != null){
			if (clamlMetaData.get("unvollstaendig").equals("true")){
				request.getCodeSystem().setIncompleteCS(true);
			} else {
				request.getCodeSystem().setIncompleteCS(false);
			}
		}
		
		if (clamlMetaData.get("description_eng") != null){
			request.getCodeSystem().setDescriptionEng(clamlMetaData.get("description_eng"));
		}
		
		if (clamlMetaData.get("gueltigkeitsbereich") != null){
			codeSystemVersion.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(clamlMetaData.get("gueltigkeitsbereich")));
		}
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.S");
		
		/*if (clamlMetaData.get("last_change_date") != null){
			codeSystemVersion.setLastChangeDate(sdf.parse(clamlMetaData.get("last_change_date")));
		}*/
		
		if (clamlMetaData.get("verantw_Org") != null){
			request.getCodeSystem().setResponsibleOrganization(clamlMetaData.get("verantw_Org"));
		}
		
		if (clamlMetaData.get("version_description") != null){
			codeSystemVersion.setDescription(clamlMetaData.get("version_description"));
		}
		

    request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
    request.getCodeSystem().getCodeSystemVersions().add(codeSystemVersion);

    request.setLogin(login);

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session);

    logger.debug(resp.getReturnInfos().getMessage());

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      throw new Exception();
    }
    this.codeSystem = resp.getCodeSystem();

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());

    // Read existing metadata and add to map to avoid double entries
    String hql = "select distinct mp from MetadataParameter mp "
            + " where codeSystemId=" + resp.getCodeSystem().getId();
    List<MetadataParameter> md_list = hb_session.createQuery(hql).list();
    
    for(MetadataParameter mp : md_list)
    {
      metaDataMap.put(mp.getParamName(), mp.getId());
	  metadataParameterMap.put(mp.getId(), mp);
      logger.debug("found metadata: " + mp.getParamName() + " with id: " + mp.getId());
     }
    
    
     }

  public AssociationType CreateAssociationType(String forwardName, String reverseName)
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

    ccatrt.setLogin(login);

    CreateConceptAssociationType ccat = new CreateConceptAssociationType();
    this.ccatrespt = ccat.CreateConceptAssociationType(ccatrt, hb_session);

    return assoctype;

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

    //Status aktuallisieren
    aktCount++;
    percentageComplete = aktCount;
    currentTask = code;

    /*if(aktCount % 200 == 0)
     {
     logger.error("AktClass: " + code + " (" + aktCount + ")");
     }*/
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

      if (rubKind.equals(RUBRICKINDS.preferred.getCode()))
      {
        found = true;
        labelString = getAllRubricStrings(rubi);
        this.createPrefferedTerm(labelString, code, clazz);
      }
    }
    if (found == false && rubi != null)
    {
      rubi.setKind(RUBRICKINDS.preferred.getCode());
      labelString = getAllRubricStrings(rubi);
      this.createPrefferedTerm(labelString, code, clazz);
    }

    while (it3.hasNext() && found == true)
    {
      rubi = (Rubric) it3.next();

      rubKind = (String) rubi.getKind();

      if (!(rubKind.equals(RUBRICKINDS.preferred.getCode())
              || rubKind.equals(RUBRICKINDS.note.getCode())))
      {
        labelString = getAllRubricStrings(rubi);
        this.createNotPrefferdTerm(labelString, code, clazz, rubKind);
      }
    }
  }

  //Durchläuft alle Labels/Fragments/Paras und gibt den zusammengestzten String zurück
  //ignoriert bisher Reference
  public String getAllRubricStrings(Rubric rubric)
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

  private void addAttributeMetadata(clamlBindingXSD.Class clazz, CodeSystemEntityVersion csev, CodeSystemConcept csc)
  {
    if (clazz != null && csev != null && csc != null)
    {
      /*for(Rubric rubric : clazz.getRubric())
       {
       String rubKind = (String) rubric.getKind();
        
       if (rubKind.equals(RUBRICKINDS.coding_hint.getCode()))
       {
       csc.
       }
       }*/

      for (Meta meta : clazz.getMeta())
      {
        try
        {
          if (meta.getName().equals(METADATA_ATTRIBUTES.hints.getCode()))
            csc.setHints(meta.getValue());
          if (meta.getName().equals(METADATA_ATTRIBUTES.meaning.getCode()))
            csc.setMeaning(meta.getValue());
          if (meta.getName().equals(METADATA_ATTRIBUTES.termAbbrevation.getCode()))
            csc.setTermAbbrevation(meta.getValue());

          if (meta.getName().equals(METADATA_ATTRIBUTES.majorRevision.getCode()))
            csev.setMajorRevision(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.minorRevision.getCode()))
            csev.setMinorRevision(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.status.getCode()))
              csev.setStatus(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.statusDate.getCode()))
            csev.setStatusDate(new Date(meta.getValue()));
        }
        catch (Exception ex)
        {
        }
      }
    }
  }

  public void createPrefferedTerm(String labelString, String code, clamlBindingXSD.Class clazz) throws Exception
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
		clamlBindingXSD.Class claz = clamlClassMap.get(code);
		if (claz.getSubClass() != null && claz.getSubClass().iterator().hasNext()){
			csev.setIsLeaf(false);
		} else {
			csev.setIsLeaf(true);  // erstmal true, wird per Trigger auf false gesetzt, wenn eine Beziehung eingefügt wird
		}
    

    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(code);
    csc.setTerm(labelString);
    csc.setTermAbbrevation("");
    csc.setIsPreferred(true);
    
    for(Rubric r : clazz.getRubric())
    {
        if(r.getKind().equals(RUBRICKINDS.note.getCode()))
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

    request.setCodeSystem(codeSystem);
    request.setCodeSystemEntity(cse);
    request.setLogin(login);
    
    //Konzept erstellen
    CreateConcept cc = new CreateConcept();
    this.ccsResponse = cc.CreateConcept(request, hb_session);

    logger.debug("[ImportClaml.java]" + ccsResponse.getReturnInfos().getMessage());
    if (ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
      {
        this.createSuperclassAssociation(code, clazz);
      }
      
      //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
      long aktEntityVersionID = 0;

      if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
      {
        aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
        referenceMap.put(code, aktEntityVersionID);
      }
      
      if(StaticStatusList.getStatus(_importId) != null)
      {
        StaticStatusList.getStatus(_importId).importCount++;
      }

      countImported++;
    }
    else
    {
      throw new Exception();
    }
  }

  public void createNotPrefferdTerm(String labelString, String code, clamlBindingXSD.Class clazz, String rubKind) throws Exception
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

    request.setCodeSystem(codeSystem);
    request.setCodeSystemEntity(cse);
    request.setLogin(login);
    
    //Konzept erstellen
    CreateConcept cc = new CreateConcept();
    this.ccsResponse = cc.CreateConcept(request, hb_session);

    logger.debug("[ImportClaml.java]" + ccsResponse.getReturnInfos().getMessage());
    if (ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      this.createTerm2TermAssociation(code, clazz, rubKind);
      countImported++;
      if(StaticStatusList.getStatus(_importId) != null)
      {
        StaticStatusList.getStatus(_importId).importCount++;
      }

    }
    else
    {
      //throw new Exception();
    }
  }

  public void createTerm2TermAssociation(String code, clamlBindingXSD.Class clazz, String rubkind) throws Exception
  {
    //aktuelle entityVersionID aus der Response merken:
    long aktEntityVersionID = 0;
    if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
    {
      aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
    }

    //den eigenen Code in der HashMap suchen (ist der Code des prefferdTerms)
    long prefferedTermEntityVersionID = (Long) referenceMap.get(code);

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
    CreateConceptAssociationTypeResponseType resp = (CreateConceptAssociationTypeResponseType) ccatresptHashmap.get(rubkind);
    AssociationType atype = (AssociationType) assoctypeHashmap.get(rubkind);
    atype.setCodeSystemEntityVersionId(resp.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());

    evat.setAssociationType(atype);

    CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
    //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
    //es besitzt jedoch eine andere Struktur

    ccar.setCodeSystemEntityVersionAssociation(evat);
    ccar.setLogin(login);

    CreateConceptAssociation cca = new CreateConceptAssociation();
    CreateConceptAssociationResponseType ccaresp = cca.CreateConceptAssociation(ccar, hb_session);
    //System.out.println("test11");
    logger.debug("[ImportClaml.java]" + ccaresp.getReturnInfos().getMessage());
    if (ccaresp.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      logger.debug("[ImportClaml.java] Create Association Erfolgreich");

    }
    else
    {
      // throw new Exception();
    }
    //System.out.println("test8");
  }

  public void createSuperclassAssociation(String code, clamlBindingXSD.Class clazz) throws Exception
  {
    //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
    long aktEntityVersionID = 0;
    if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
    {
      aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
      referenceMap.put(code, aktEntityVersionID);
    }

    //Die erste SuperClass holen
    String superclazzCode = "";
    long superclazzEntityVersionID = 0;
    if (clazz.getSuperClass().iterator().hasNext())
    {
      superclazzCode = clazz.getSuperClass().iterator().next().getCode();
      //Superclass id in der Hashmap suchen
      superclazzEntityVersionID = (Long) referenceMap.get(superclazzCode);
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

    assoctypeTaxonomy.setCodeSystemEntityVersionId(ccatresptTaxonomy.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());
    evat.setAssociationType(assoctypeTaxonomy);

    CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
    //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
    //es besitzt jedoch eine andere Struktur

    ccar.setCodeSystemEntityVersionAssociation(evat);
    ccar.setLogin(login);
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
	

	private int counter = 0;
	
  private long insertMetaData(String name, String value, String code)
  {
		counter++;
		if (counter % 500 == 0){
			logger.warn("Session flushed");
			
			hb_session.flush();
			//hb_session.clear();
		}
		
    if (value == null || value.length() == 0)
      return 0;
    
    long metaDataID = 0;
	
    // Der SQLHelper baut die Insert-Anfrage zusammen
    if (metaDataMap.containsKey(name))
    {
      metaDataID = (Long) metaDataMap.get(name);
    }
    else{		
      //Create metadata_parameter
      MetadataParameter mp = new MetadataParameter();
      mp.setParamName(name);
      mp.setCodeSystem(codeSystem);

      hb_session.save(mp);

      metaDataID = mp.getId();
      logger.debug("[ImportClaml.java] Neues metadata_parameter mit ID: " + metaDataID + " und name: " + name);

      this.metaDataMap.put(name, metaDataID);
			metadataParameterMap.put(metaDataID, mp);

			CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
			mv.setParameterValue(value);

			mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
			mv.getCodeSystemEntityVersion().setVersionId((Long) referenceMap.get(code));

			//Matthias
			//Get metaDataparameter
			if (metadataParameterMap.containsKey(metaDataID)){
				MetadataParameter temp = metadataParameterMap.get(metaDataID);
				mv.setMetadataParameter(temp);
			}
			else {
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
	parameterHelper.addParameter("csev.", "versionId", referenceMap.get(code));

	// Parameter hinzufügen (immer mit AND verbunden)
	hql += parameterHelper.getWhere("");
	logger.debug("HQL: " + hql);

	// Query erstellen
	org.hibernate.Query q = hb_session.createQuery(hql);
	// Die Parameter können erst hier gesetzt werden (übernimmt Helper)
	parameterHelper.applyParameter(q);

	List<CodeSystemMetadataValue> valueList= q.list(); 

	if(valueList.size() == 1){
		valueList.get(0).setParameterValue(value);
		hb_session.update(valueList.get(0));
	}
	else if (valueList.size() == 0){
		CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
    mv.setParameterValue(value);

    mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
    mv.getCodeSystemEntityVersion().setVersionId((Long) referenceMap.get(code));

		//Matthias
		//Get metaDataparameter
		if (metadataParameterMap.containsKey(metaDataID)){
			MetadataParameter temp = metadataParameterMap.get(metaDataID);
			mv.setMetadataParameter(temp);
		}
		else {
			//should never reach the else case
			mv.setMetadataParameter(new MetadataParameter());
			mv.getMetadataParameter().setId(metaDataID);
		}
		hb_session.save(mv);
	}

	//logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

	
	
	/*

    // Der SQLHelper baut die Insert-Anfrage zusammen
    CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
    mv.setParameterValue(value);

    mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
    mv.getCodeSystemEntityVersion().setVersionId((Long) referenceMap.get(code));

	//Matthias
	//Get metaDataparameter
	if (metadataParameterMap.containsKey(metaDataID)){
		MetadataParameter temp = metadataParameterMap.get(metaDataID);
		mv.setMetadataParameter(temp);
	}
	else {
		//should never reach the else case
		mv.setMetadataParameter(new MetadataParameter());
		mv.getMetadataParameter().setId(metaDataID);

	}
	
    hb_session.save(mv);

    metaDataID = mv.getId();
	*/
    return metaDataID;
  }

  public void createMetaData(clamlBindingXSD.Class clazz)
  {
    if (logger.isInfoEnabled())
      logger.debug("createMetaData gestartet");

    for (Meta meta : clazz.getMeta())
    {
      if (meta.getName() != null && meta.getName().length() > 0)
      {
        // Prüfen, ob es ein Metadatenattribut ist
        if (METADATA_ATTRIBUTES.isCodeValid(meta.getName()) == false)
        {
          long metaDataID = insertMetaData(meta.getName(), meta.getValue(), clazz.getCode());
          if (metaDataID > 0)
            logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
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
          logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
      }
    }
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
  
  public CodeSystem getCodeSystem()
  {
    return this.codeSystem;
  }
}
