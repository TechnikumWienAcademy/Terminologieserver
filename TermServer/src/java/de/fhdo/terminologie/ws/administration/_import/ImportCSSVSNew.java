/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

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
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.logger;
import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.VersioningType;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author puraner
 */
public class ImportCSSVSNew extends CodeSystemImport implements ICodeSystemImport
{

    private boolean onlyCSV = false;
    private CodeSystem cs;
    private Long csId = 0L;
    private Long csvId = 0L;
    private CodeSystemVersion csVersion;
    private int lowestLevel = 1;
    private int countFehler;
    private String sortMessage = "";

    public ImportCSSVSNew()
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

        // Hibernate-Block, Session öffnen
        hb_session = HibernateUtil.getSessionFactory().openSession();

        String s = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        countFehler = 0;

        try
        {
            logger.debug("Wandle zu InputStream um...");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document svsDoc = builder.parse(new ByteArrayInputStream(this._fileContent));
            HashMap<String, String> codeListInfoMap = new HashMap<String, String>();
            ArrayList<HashMap<String, String>> conceptsList = new ArrayList<HashMap<String, String>>();

            Node root = svsDoc.getDocumentElement();
            if (root.getNodeName().equals("valueSet"))
            {

                NamedNodeMap valueSetAttributes = root.getAttributes();
                if (valueSetAttributes.getNamedItem("name") != null)
                {
                    codeListInfoMap.put("name", valueSetAttributes.getNamedItem("name").getTextContent());                    //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("displayName") != null)
                {
                    codeListInfoMap.put("displayName", valueSetAttributes.getNamedItem("displayName").getTextContent());      //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("effectiveDate") != null)
                {
                    codeListInfoMap.put("effectiveDate", valueSetAttributes.getNamedItem("effectiveDate").getTextContent());  //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("id") != null)
                {
                    codeListInfoMap.put("id", valueSetAttributes.getNamedItem("id").getTextContent());                        //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("statusCode") != null)
                {
                    codeListInfoMap.put("statusCode", valueSetAttributes.getNamedItem("statusCode").getTextContent());        //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("website") != null)
                {
                    codeListInfoMap.put("website", valueSetAttributes.getNamedItem("website").getTextContent());        //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("version") != null)
                {
                    codeListInfoMap.put("version", valueSetAttributes.getNamedItem("version").getTextContent());        //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("beschreibung") != null)
                {
                    codeListInfoMap.put("beschreibung", valueSetAttributes.getNamedItem("beschreibung").getTextContent());        //ZUWEISUNG
                }
                if (valueSetAttributes.getNamedItem("description") != null)
                {
                    codeListInfoMap.put("description", valueSetAttributes.getNamedItem("description").getTextContent());        //ZUWEISUNG
                }
                //Matthias import VersionsBeschreibung
                if (valueSetAttributes.getNamedItem("version-beschreibung") != null)
                {
                    codeListInfoMap.put("version-beschreibung", valueSetAttributes.getNamedItem("version-beschreibung").getTextContent());
                }

                //Matthias:add additional information
                if (valueSetAttributes.getNamedItem("unvollstaendig") != null)
                {
                    codeListInfoMap.put("unvollstaendig", valueSetAttributes.getNamedItem("unvollstaendig").getTextContent());
                }
                else
                {
                    codeListInfoMap.put("unvollstaendig", "");
                }

                if (valueSetAttributes.getNamedItem("verantw_Org") != null)
                {
                    codeListInfoMap.put("verantw_Org", valueSetAttributes.getNamedItem("verantw_Org").getTextContent());
                }
                else
                {
                    codeListInfoMap.put("verantw_Org", "");
                }

                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++)
                {

                    Node conceptList = children.item(i);
                    if (!(conceptList.getNodeType() == Node.TEXT_NODE))
                    {

                        if (conceptList.getNodeName().equals("conceptList"))
                        {

                            NodeList concepts = conceptList.getChildNodes();
                            for (int j = 0; j < concepts.getLength(); j++)
                            {

                                Node concept = concepts.item(j);
                                if (!(concept.getNodeType() == Node.TEXT_NODE))
                                {

                                    if (concept.getNodeName().equals("concept"))
                                    {
                                        HashMap<String, String> conceptInfo = new HashMap<String, String>();

                                        NamedNodeMap conceptAttributes = concept.getAttributes();
                                        if (conceptAttributes.getNamedItem("code") != null)
                                        {
                                            conceptInfo.put("code", conceptAttributes.getNamedItem("code").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("codeSystem") != null)
                                        {
                                            conceptInfo.put("codeSystem", conceptAttributes.getNamedItem("codeSystem").getTextContent());    //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("displayName") != null)
                                        {
                                            conceptInfo.put("displayName", conceptAttributes.getNamedItem("displayName").getTextContent());  //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("level") != null)
                                        {
                                            conceptInfo.put("level", conceptAttributes.getNamedItem("level").getTextContent());              //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("type") != null)
                                        {
                                            conceptInfo.put("type", conceptAttributes.getNamedItem("type").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("concept_beschreibung") != null)
                                        {
                                            conceptInfo.put("concept_beschreibung", conceptAttributes.getNamedItem("concept_beschreibung").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("deutsch") != null)
                                        {
                                            conceptInfo.put("deutsch", conceptAttributes.getNamedItem("deutsch").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("hinweise") != null)
                                        {
                                            conceptInfo.put("hinweise", conceptAttributes.getNamedItem("hinweise").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("relationships") != null)
                                        {
                                            conceptInfo.put("relationships", conceptAttributes.getNamedItem("relationships").getTextContent());                //ZUWEISUNG
                                        }

                                        conceptsList.add(conceptInfo);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {

                throw new ImportException("SVS-Datei: Kein XML-Root-Node gefunden, bitte die zu importierende Datei prüfen...");
            }
            
            this.setTotalCountInStatusList(conceptsList.size(), this.getImportId());

            // Hibernate-Block, Session öffnen
//      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try // try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                if (createCodeSystem(codeListInfoMap) == false)
                {
                    hb_session.getTransaction().rollback();
                    hb_session.close();
                    throw new ImportException("Code System konnte nicht erstellt werden!");
                }

                // MetadatenParameter speichern => ELGA Specific Level/Type 
                Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
                for (int i = 0; i < 3; i++)
                {

                    String mdText = "";
                    MetadataParameter mp = null;
                    if (i == 0)
                    {
                        mdText = "Level";
                    }
                    if (i == 1)
                    {
                        mdText = "Type";
                    }
                    if (i == 2)
                    {
                        mdText = "Relationships";
                    }

                    //Check if parameter already set in case of new Version!
                    String hql = "select distinct mp from MetadataParameter mp";
                    hql += " join fetch mp.codeSystem cs";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("mp.", "paramName", mdText);

                    // Parameter hinzufügen (immer mit AND verbunden)
                    hql += parameterHelper.getWhere("");
                    logger.debug("HQL: " + hql);

                    // Query erstellen
                    org.hibernate.Query q = hb_session.createQuery(hql);
                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                    parameterHelper.applyParameter(q);

                    List<MetadataParameter> mpList = q.list();
                    for (MetadataParameter mParameter : mpList)
                    {

                        if (mParameter.getCodeSystem().getId().equals(this.getCodeSystem().getId()))
                        {
                            mp = mParameter;
                        }
                    }

                    if (mp == null)
                    {

                        mp = new MetadataParameter();
                        mp.setParamName(mdText);
                        mp.setCodeSystem(this.getCodeSystem());
                        hb_session.save(mp);
                    }

                    headerMetadataIDs.put(mdText, mp.getId());

                    logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit Codesystem-ID: " + mp.getCodeSystem().getId() + ", MD-ID: " + mp.getId());
                }

                //Adding Version Information 
                cs = (CodeSystem) this.getCodeSystem();

                for (CodeSystemVersion csv : this.getCodeSystem().getCodeSystemVersions())
                {

                    if (csv.getVersionId().equals(cs.getCurrentVersionId()))
                    {

                        cs.getCodeSystemVersions().clear();
                        cs.getCodeSystemVersions().add(csv);
                        break;
                    }
                }
                this.setCodeSystem(cs);

                csId = cs.getId();
                csvId = cs.getCodeSystemVersions().iterator().next().getVersionId();

                csVersion = (CodeSystemVersion) this.getCodeSystem().getCodeSystemVersions().iterator().next();

                Date date = new Date();
                csVersion.setInsertTimestamp(date);

                if (csVersion.getName() == null || csVersion.getName().equals(""))
                {
                    csVersion.setName(codeListInfoMap.get("version"));
                }
                csVersion.setOid(codeListInfoMap.get("id"));
                csVersion.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));
                csVersion.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                csVersion.setStatusDate(date);
                csVersion.setValidityRange(236l); //Default Value for empfohlen
                //Matthias: changed from cs.setDescription to csVersion.setDescription
                csVersion.setDescription(codeListInfoMap.get("version-beschreibung"));
                cs.setDescription(codeListInfoMap.get("beschreibung"));
                cs.setDescriptionEng(codeListInfoMap.get("description"));
                cs.setWebsite(codeListInfoMap.get("website"));
                if (codeListInfoMap.get("unvollstaendig").equals("true"))
                {
                    cs.setIncompleteCS(true);
                }
                else
                {
                    cs.setIncompleteCS(false);
                }
                cs.setResponsibleOrganization(codeListInfoMap.get("verantw_Org"));
                cs.setIncompleteCS(onlyCSV);

                //konzept integration here   
                Iterator<HashMap<String, String>> iterator = conceptsList.iterator();
                while (iterator.hasNext())
                {
                    if (StaticStatusList.getStatus(this.getImportId()).cancel)
                    {
                        break;
                    }
                    HashMap<String, String> conceptDetails = (HashMap<String, String>) iterator.next();

                    CreateConceptRequestType request = new CreateConceptRequestType();

                    request.setLogin(this.getLoginType());
                    request.setCodeSystem(this.getCodeSystem());
                    request.setCodeSystemEntity(new CodeSystemEntity());
                    request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

                    CodeSystemConcept csc = new CodeSystemConcept();

                    csc.setCode(conceptDetails.get("code"));
                    csc.setIsPreferred(true);
                    csc.setTerm(conceptDetails.get("displayName"));
                    csc.setTermAbbrevation("");
                    csc.setDescription(conceptDetails.get("concept_beschreibung"));
                    csc.setMeaning(conceptDetails.get("deutsch"));
                    csc.setHints(conceptDetails.get("hinweise"));

                    logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

                    CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
                    membership.setIsMainClass(Boolean.TRUE);
                    membership.setIsAxis(Boolean.FALSE);

                    request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                    request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

                    if (csc.getCode().length() == 0)
                    {
                        if (csc.getTerm().length() > 0)
                        {
                            if (csc.getTerm().length() > 98)
                            {
                                csc.setCode(csc.getTerm().substring(0, 98));
                            }
                            else
                            {
                                csc.setCode(csc.getTerm());
                            }
                        }
                    }
                    else if (csc.getCode().length() > 98)
                    {
                        csc.setCode(csc.getCode().substring(0, 98));
                    }

                    if (csc.getCode().length() > 0)
                    {
                        // Entity-Version erstellen
                        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                        csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                        csev.getCodeSystemConcepts().add(csc);
                        csev.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                        csev.setIsLeaf(true);
                        csev.setEffectiveDate(sdf.parse(codeListInfoMap.get("effectiveDate")));

                        // Entity-Version dem Request hinzufügen
                        request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

                        // Dienst aufrufen (Konzept einfügen)
                        CreateConcept cc = new CreateConcept();
                        CreateConceptResponseType response = cc.CreateConcept(request, hb_session);

                        if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
                        {

                            _aktCount++;
                            this.setCurrentCountInStatusList(_aktCount, this.getImportId());
                            // Metadaten einfügen
                            String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase

                            if (mdLevelValue != null && mdLevelValue.length() > 0)
                            {
                                //Check if parameter already set in case of new Version!
                                String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                                hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
                                parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

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
                                    valueList.get(0).setParameterValue(mdLevelValue);
                                }

                                if (Integer.valueOf(mdLevelValue) < lowestLevel)
                                {
                                    lowestLevel = Integer.valueOf(mdLevelValue);
                                }

                                logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                                hb_session.update(valueList.get(0));
                            }
                            String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
                            if (mdTypeValue != null && mdTypeValue.length() > 0)
                            {
                                //Check if parameter already set in case of new Version!
                                String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                                hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
                                parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

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
                                    valueList.get(0).setParameterValue(mdTypeValue);
                                }

                                logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                                hb_session.update(valueList.get(0));
                            }

                            String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
                            if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
                            {
                                //Check if parameter already set in case of new Version!
                                String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                                hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
                                parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

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
                                    valueList.get(0).setParameterValue(mdRelationshipsValue);
                                }

                                logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                                hb_session.update(valueList.get(0));
                            }
                        }
                        else
                        {
                            countFehler++;
                        }

                    }
                    else
                    {
                        countFehler++;
                        logger.debug("Term ist nicht gegeben");
                    }

                }
                if (_aktCount == 0)
                {

                    String resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
                    hb_session.getTransaction().rollback();
                    throw new ImportException("Keine Konzepte importiert. " + resultStr);
                }
                else
                {
                    if (StaticStatusList.getStatus(this.getImportId()).cancel)
                    {
                        hb_session.getTransaction().rollback();
                    }
                    sortMessage = "";
                    if (sort(csVersion.getVersionId()))
                    {
                        sortMessage = "Sortierung erfolgreich!";
                    }
                    else
                    {
                        sortMessage = "Sortierung fehlgeschlagen!";
                    }

                    CodeSystem cs_ret = new CodeSystem();
                    cs_ret.setId(this.getCodeSystem().getId());
                    cs_ret.setCurrentVersionId(this.getCodeSystem().getCurrentVersionId());
                    cs_ret.setName(this.getCodeSystem().getName());
                    cs_ret.setAutoRelease(this.getCodeSystem().getAutoRelease());

                    CodeSystemVersion csv_ret = new CodeSystemVersion();
                    csv_ret.setVersionId(this.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
                    csv_ret.setName(this.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                    cs_ret.getCodeSystemVersions().clear();
                    cs_ret.getCodeSystemVersions().add(csv_ret);

                    this.setCodeSystem(cs_ret);
                    
                    hb_session.getTransaction().commit();
                }
            }
            catch (Exception ex)
            {
                //ex.printStackTrace();
                logger.error(ex.getMessage());

                try
                {
                    if(!hb_session.getTransaction().wasRolledBack()){
                        hb_session.getTransaction().rollback();
                        logger.info("[ImportSVS.java] Rollback durchgeführt!");
                    }
                        
                    String resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
                    throw new ImportException("Fehler beim Import einer SVS-Datei: " + ex.getLocalizedMessage() + "\n"+resultStr);
                }
                catch (Exception exRollback)
                {
                    logger.info(exRollback.getMessage());
                    logger.info("[ImportSVS.java] Rollback fehlgeschlagen!");
                }
            }
            finally
            {
                // Session schließen
                hb_session.close();
                StaticStatusList.getStatus(this.getImportId()).importRunning = false;
                logger.info("Import CS SVS fertig");
            }
        }
        catch (Exception ex)
        {
            logger.error(ex);
            
            throw new ImportException("Fehler beim Import: " + ex.getLocalizedMessage());
        }

        try
        {
            updateCodeSystemVersion();
        }
        catch (Exception ex)
        {
            s = "Fehler beim Import: " + ex.getLocalizedMessage();
            logger.error(s);
        }

    }

    private boolean createCodeSystem(HashMap<String, String> codeListInfoMap) throws ImportException
    {

        logger.debug("createCodeSystem...");
        /*org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();*/

        // vorhandenes CodeSystem nutzen oder neues anlegen?
        if (this.getCodeSystem().getId() != null && this.getCodeSystem().getId() > 0)
        {
            onlyCSV = true;
            try
            {
                logger.debug("ID ist angegeben");
                Date date = new java.util.Date();

                CodeSystem cs_db = (CodeSystem) hb_session.get(CodeSystem.class, this.getCodeSystem().getId());

                //New Version
                CodeSystemVersion csvNew = new CodeSystemVersion();

                csvNew.setName(this.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                csvNew.setInsertTimestamp(date);
                csvNew.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());

                csvNew.setStatusDate(date);
                csvNew.setCodeSystem(new CodeSystem());
                csvNew.getCodeSystem().setId(cs_db.getId());
                csvNew.setCodeSystemVersionEntityMemberships(null);
                csvNew.setLicencedUsers(null);
                csvNew.setUnderLicence(false);

                Set<LicenceType> licenceTypes = csvNew.getLicenceTypes();
                csvNew.setLicenceTypes(null);
                csvNew.setPreviousVersionId(cs_db.getCurrentVersionId());
                // In DB speichern damit vsvNew eine ID bekommt
                hb_session.save(csvNew);

                cs_db.setCurrentVersionId(csvNew.getVersionId());

                cs_db.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
                cs_db.getCodeSystemVersions().add(csvNew);
                hb_session.update(cs_db);

                // LicenceTypes speichern
                if (licenceTypes != null && csvNew.getVersionId() > 0)
                {
                    Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
                    while (itLicenceType.hasNext())
                    {
                        LicenceType lt = itLicenceType.next();
                        lt.setCodeSystemVersion(new CodeSystemVersion());
                        lt.getCodeSystemVersion().setVersionId(csvNew.getVersionId());
                        lt.setLicencedUsers(null);
                        hb_session.save(lt);
                    }
                }

                //Reload
                cs_db = (CodeSystem) hb_session.get(CodeSystem.class, cs_db.getId());
                this.setCodeSystem(cs_db);

            }
            catch (Exception ex)
            {
                logger.error(ex);
                throw new ImportException("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");

            }
            finally
            {
                Long csvId = ((CodeSystemVersion) this.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId();
                if (this.getCodeSystem().getId() > 0 && csvId > 0)
                {
                    if (StaticStatusList.getStatus(this.getImportId()).cancel)
                    {
                        hb_session.getTransaction().rollback();
                    }
                }
                else
                {
                    // Ã„nderungen nicht erfolgreich
                    logger.warn("CSV konnte nicht gespeichert werden");
                    hb_session.getTransaction().rollback();
                }
            }
        }
        else
        {
            onlyCSV = false;
            try
            {
                logger.debug("ID ist angegeben");
                Date date = new java.util.Date();

                //New CodeSystem
                CodeSystem cs_db = new CodeSystem();
                cs_db.setName(this.getCodeSystem().getName());
                cs_db.setInsertTimestamp(date);
                cs_db.setCodeSystemVersions(null);

                //Matthias incompleteCS and responsible Organization added 
                cs_db.setIncompleteCS(this.getCodeSystem().getIncompleteCS());
                cs_db.setResponsibleOrganization(this.getCodeSystem().getResponsibleOrganization());

                // In DB speichern
                hb_session.save(cs_db);

                //New Version
                CodeSystemVersion csvNew = new CodeSystemVersion();

                csvNew.setName(this.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
                csvNew.setInsertTimestamp(date);

                if (this.getImportType().getRole().equals(CODES.ROLE_ADMIN))
                {
                    csvNew.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                }

                if (this.getImportType().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
                {
                    csvNew.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                }

                csvNew.setStatusDate(date);
                csvNew.setCodeSystem(new CodeSystem());
                csvNew.getCodeSystem().setId(cs_db.getId());
                csvNew.setCodeSystemVersionEntityMemberships(null);
                csvNew.setLicencedUsers(null);
                csvNew.setUnderLicence(false);

                Set<LicenceType> licenceTypes = csvNew.getLicenceTypes();
                csvNew.setLicenceTypes(null);

                // In DB speichern damit vsvNew eine ID bekommt
                hb_session.save(csvNew);

                cs_db.setCurrentVersionId(csvNew.getVersionId());
                cs_db.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
                cs_db.getCodeSystemVersions().add(csvNew);
                hb_session.update(cs_db);

                // LicenceTypes speichern
                if (licenceTypes != null && csvNew.getVersionId() > 0)
                {
                    Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
                    while (itLicenceType.hasNext())
                    {
                        LicenceType lt = itLicenceType.next();
                        lt.setCodeSystemVersion(new CodeSystemVersion());
                        lt.getCodeSystemVersion().setVersionId(csvNew.getVersionId());
                        lt.setLicencedUsers(null);
                        hb_session.save(lt);
                    }
                }

                //Reload
                cs_db = (CodeSystem) hb_session.get(CodeSystem.class, cs_db.getId());
                this.setCodeSystem(cs_db);

            }
            catch (Exception ex)
            {
                logger.error(ex);
                throw new ImportException("CSV konnte nicht gespeichert werden");
            }
            finally
            {
                Long csvId = ((CodeSystemVersion) this.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId();
                if (this.getCodeSystem().getId() > 0 && csvId > 0)
                {
                    if (StaticStatusList.getStatus(this.getImportId()).cancel)
                    {
                        hb_session.getTransaction().rollback();
                    }
                }
                else
                {
                    // Ã„nderungen nicht erfolgreich
                    logger.warn("CSV konnte nicht gespeichert werden");
                    hb_session.getTransaction().rollback();
                }
            }
        }

        return true;
    }

    private boolean sort(long codeSystemVersionId) throws ImportException
    {

        //get List of concepts or whatever
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
        boolean s = false;
        try
        {
            String hql = "select distinct csev from CodeSystemEntityVersion csev";
            hql += " join fetch csev.codeSystemMetadataValues csmv";
            hql += " join fetch csmv.metadataParameter mp";
            hql += " join fetch csev.codeSystemEntity cse";
            hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
            hql += " join fetch csvem.codeSystemVersion csv";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("csv.", "versionId", codeSystemVersionId);

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(q);

            List<CodeSystemEntityVersion> csevList = q.list();

            HashMap<Integer, CodeSystemEntityVersion> workList = new HashMap<Integer, CodeSystemEntityVersion>();
            ArrayList<Integer> sortList = new ArrayList<Integer>();

            Iterator<CodeSystemEntityVersion> iter = csevList.iterator();
            while (iter.hasNext())
            {
                boolean found = false;
                CodeSystemEntityVersion csev = (CodeSystemEntityVersion) iter.next();

                //Get the Level
                int level = 0;
                Iterator<CodeSystemMetadataValue> iter1 = csev.getCodeSystemMetadataValues().iterator();
                while (iter1.hasNext())
                {
                    CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) iter1.next();
                    if (csmv.getMetadataParameter().getParamName().equals("Level"))
                    {

                        String cleaned = csmv.getParameterValue();

                        if (cleaned.contains(" "))
                        {
                            cleaned = cleaned.replace(" ", "");
                        }

                        level = Integer.valueOf(cleaned);
                    }
                }

                if (level == lowestLevel)
                {

                    CodeSystemVersionEntityMembershipId csvemId = null;
                    Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                    Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                    while (it.hasNext())
                    {

                        CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership) it.next();
                        if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                                && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId))
                        {

                            csvemId = member.getId();
                        }
                    }

                    CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership) hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
                    c.setIsMainClass(Boolean.TRUE);
                    hb_session.update(c);

                    workList.put(level, csev);
                    sortList.add(level);
                }
                else
                {

                    int size = sortList.size();
                    int count = 0;
                    while (!found)
                    {

                        if ((sortList.get(size - (1 + count)) - level) == -1)
                        {

                            //Setting MemberShip isMainClass false
                            CodeSystemVersionEntityMembershipId csvemId = null;
                            Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                            Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                            while (it.hasNext())
                            {

                                CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership) it.next();
                                if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                                        && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId))
                                {

                                    csvemId = member.getId();
                                }
                            }

                            CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership) hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
                            c.setIsMainClass(Boolean.FALSE);
                            hb_session.update(c);

                            found = true;
                            sortList.add(level);
                            workList.put(level, csev);
                            CodeSystemEntityVersion csevPrev = workList.get(sortList.get(size - (1 + count)));

                            CodeSystemEntityVersionAssociation association = new CodeSystemEntityVersionAssociation();
                            association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                            association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csevPrev.getVersionId());
                            association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                            association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csev.getVersionId());
                            association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                            association.setLeftId(csevPrev.getVersionId()); // immer linkes Element also csev1
                            association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                            association.getAssociationType().setCodeSystemEntityVersionId(4L);
                            // Weitere Attribute setzen
                            association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                            association.setStatusDate(new Date());
                            association.setInsertTimestamp(new Date());
                            // Beziehung abspeichern
                            hb_session.save(association);

                            CodeSystemEntityVersion csevLoc = (CodeSystemEntityVersion) hb_session.get(CodeSystemEntityVersion.class, csevPrev.getVersionId());
                            if (csevLoc.getIsLeaf() != Boolean.FALSE)
                            {
                                csevLoc.setIsLeaf(Boolean.FALSE);
                                hb_session.update(csevLoc);
                            }

                        }
                        else
                        {
                            count++;
                            found = false;
                        }
                    }
                }
            }

            if (StaticStatusList.getStatus(this.getImportId()).cancel)
            {
                hb_session.getTransaction().rollback();
            }
            else
            {
                hb_session.getTransaction().commit();
            }
            s = true;
        }
        catch (Exception ex)
        {
            hb_session.getTransaction().rollback();
            logger.error(ex);
            throw new ImportException("Fehler bei initList(): " + ex.getMessage());
        }
        finally
        {
            hb_session.close();
        }
        return s;
    }

    private void updateCodeSystemVersion() throws Exception
    {
        //CodeSystemVersion Update
        MaintainCodeSystemVersionRequestType updateParam = new MaintainCodeSystemVersionRequestType();
        VersioningType versioningType = new VersioningType();
        versioningType.setCreateNewVersion(Boolean.FALSE);

        updateParam.setLogin(this.getLoginType());
        updateParam.setCodeSystem(this.getCodeSystem());
        updateParam.setVersioning(versioningType);

        MaintainCodeSystemVersion mcv = new MaintainCodeSystemVersion();
        MaintainCodeSystemVersionResponseType updateResponse = mcv.MaintainCodeSystemVersion(updateParam);

        if (updateResponse.getReturnInfos().getStatus() != ReturnType.Status.OK)
        {
            throw new ImportException("ImportCSSVS: CodeSystemVersion Update konnte nicht durchgeführt werden: " + updateResponse.getReturnInfos().getMessage());
        }
    }

    public int getCountFehler()
    {
        return countFehler;
    }

    public String getSortMessage()
    {
        return sortMessage;
    }

}
