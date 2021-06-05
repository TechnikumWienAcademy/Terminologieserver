/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.DateComparator;
import de.fhdo.terminologie.helper.DeleteTermHelper;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.LOGGER;
import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContent;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class ImportVSSVSNew extends ValuesetImport implements IValuesetImport
{
    private Boolean orderCVSM = false;
    private boolean onlyVSV = true; //Only CSV for this case
    private Long vsId = 0L;
    private Long vsvId = 0L;
    boolean freigabeFixed = false; //TRMMRK

    @Override
    public void setImportData(ImportValueSetRequestType request)
    {
        LOGGER.info("setImportData started");
        this.setImportId(request.getImportId());
        this.setLoginType(request.getLogin());
        this.setImportType(request.getImportInfos());
        freigabeFixed = request.freigabeFixed();

        this._valueset = request.getValueSet();
        this.fileContent = request.getImportInfos().getFilecontent();
    }

    @Override
    public void startImport() throws ImportException, ImportParameterValidationException
    {
        LOGGER.info("startImport started");
        //creating Hibernate Session and starting transaction
        try
        {
            this.validateParameters();
        }
        catch (ImportParameterValidationException ex)
        {
            LOGGER.error(ex);
            throw ex;
        }

        this.status.setImportRunning(true);
        StaticStatusList.addStatus(this.getImportId(), this.status);
        
        LOGGER.info("HB Session 1.1");
        hb_session = HibernateUtil.getSessionFactory().openSession();
        LOGGER.info("HB Session 1.2");
        boolean isElgaLaborparamter = false;

        try
        {
            LOGGER.debug("Wandle zu InputStream um...");

            orderCVSM = this.getImportType().getOrder();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document svsDoc = builder.parse(new ByteArrayInputStream(this.fileContent));
            HashMap<String, String> codeListInfoMap = new HashMap<String, String>();
            ArrayList<HashMap<String, String>> conceptsList = new ArrayList<HashMap<String, String>>();

            Node root = svsDoc.getDocumentElement();
            if (root.getNodeName().equals("valueSet"))
            {

                NamedNodeMap valueSetAttributes = root.getAttributes();
                if (valueSetAttributes.getNamedItem("name") != null)
                {
                    codeListInfoMap.put("name", valueSetAttributes.getNamedItem("name").getTextContent());                    //ZUWEISUNG					
                    //Matthias 18.5.2015
                    //new mapping for VS Laborparameter
                    if (valueSetAttributes.getNamedItem("name").getTextContent().equalsIgnoreCase("ELGA_Laborparameter"))
                    {
                        isElgaLaborparamter = true;
                    }
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
                                        if (conceptAttributes.getNamedItem("einheit_print") != null)
                                        {
                                            conceptInfo.put("einheit_print", conceptAttributes.getNamedItem("einheit_print").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("einheit_codiert") != null)
                                        {
                                            conceptInfo.put("einheit_codiert", conceptAttributes.getNamedItem("einheit_codiert").getTextContent());                //ZUWEISUNG
                                        }
                                        if (conceptAttributes.getNamedItem("displayNameShort") != null)
                                        {
                                            conceptInfo.put("displayNameShort", conceptAttributes.getNamedItem("displayNameShort").getTextContent());
                                        }

                                        //Matthias: Add displayNameAlternativ for ELGA Laborparameter
                                        if (conceptAttributes.getNamedItem("displayNameAlt") != null)
                                        {
                                            conceptInfo.put("displayNameAlt", conceptAttributes.getNamedItem("displayNameAlt").getTextContent());
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
            LOGGER.info("HB Session 2.1");
            hb_session.getTransaction().begin();
            LOGGER.info("HB Session 2.2");
            
            try // try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                if (createValueSet(codeListInfoMap) == false)
                {
                    // Fehlermeldung
                    throw new ImportException("ValueSet konnte nicht erstellt werden!");
                }

                // MetadatenParameter speichern => ELGA Specific Level/Type 
                Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
                for (int i = 0; i < 5; i++)
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
                    if (i == 3)
                    {
                        mdText = "Einheit print";
                    }
                    if (i == 4)
                    {
                        mdText = "Einheit codiert";
                    }

                    //Check if parameter already set in case of new Version!
                    String hql = "select distinct mp from MetadataParameter mp";
                    hql += " join fetch mp.valueSet vs";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("mp.", "paramName", mdText);

                    // Parameter hinzufügen (immer mit AND verbunden)
                    hql += parameterHelper.getWhere("");
                    LOGGER.debug("HQL: " + hql);

                    // Query erstellen
                    LOGGER.info("HB Session 3.1");
                    org.hibernate.Query q = hb_session.createQuery(hql);
                    LOGGER.info("HB Session 3.3");
                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                    parameterHelper.applyParameter(q);

                    List<MetadataParameter> mpList = q.list();
                    for (MetadataParameter mParameter : mpList)
                    {
                        if (mParameter.getValueSet().getId().equals(this.getValueset().getId()))
                        {
                            mp = mParameter;
                            break;
                        }
                    }

                    if (mp == null)
                    {

                        mp = new MetadataParameter();
                        mp.setParamName(mdText);
                        mp.setValueSet(this.getValueset());
                        LOGGER.info("HB Session 4.1");
                        hb_session.save(mp);
                        LOGGER.info("HB Session 4.2");
                    }

                    headerMetadataIDs.put(mdText, mp.getId());

                    LOGGER.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit ValueSet-ID: " + mp.getValueSet().getId() + ", MD-ID: " + mp.getId());
                }

                //ConceptLinking
                CreateValueSetContentRequestType request = new CreateValueSetContentRequestType();
                request.setLogin(this.getLoginType());

                //Nur letzte Version
                ValueSet vs = this.getValueset();

                for (ValueSetVersion vsv : this.getValueset().getValueSetVersions())
                {

                    if (vsv.getVersionId().equals(vs.getCurrentVersionId()))
                    {

                        vs.getValueSetVersions().clear();
                        vs.getValueSetVersions().add(vsv);
                        break;
                    }
                }
                this.setValueset(vs);

                vsId = vs.getId();
                vsvId = vs.getValueSetVersions().iterator().next().getVersionId();

                request.setValueSet(this.getValueset());
                request.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

                Long orderCounter = 1l;

                Iterator<HashMap<String, String>> iterator = conceptsList.iterator();

                //Matthias: counter added to flush session
                int counter = 0;

                while (iterator.hasNext())
                {

                    if (StaticStatusList.getStatus(this.getImportId()).cancel)
                      break;
                    
                    counter++;

                    HashMap<String, String> conceptDetails = (HashMap<String, String>) iterator.next();
                    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                    ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
                    //Matthias: changed awBeschreibung
                    if (conceptDetails.get("displayNameShort") != null && !conceptDetails.get("displayNameShort").equals(""))
                    {
                        cvsm.setAwbeschreibung(conceptDetails.get("displayNameShort"));
                    }
                    else
                    {
                        cvsm.setAwbeschreibung(conceptDetails.get("concept_beschreibung"));
                    }
                    //
                    cvsm.setBedeutung(conceptDetails.get("deutsch"));
                    cvsm.setHinweise(conceptDetails.get("hinweise"));

                    if (orderCVSM)
                    {
                        cvsm.setOrderNr(orderCounter);
                        ++orderCounter;
                    }
                    else
                    {
                        cvsm.setOrderNr(0l);
                    }

                    // Version-ID muss anhand des Codes bestimmt werden
                    String code = conceptDetails.get("code");

                    LOGGER.debug("Entity zu Code '" + code + "' wird gesucht...");
                    String oid = conceptDetails.get("codeSystem");

                    //Search for code in codesystemconcept and when found search for 
                    //codesystemconcept with correct oid
                    //-----------------
                    LOGGER.info("Query Code: " + code);

                    //Matthias: try to improve performance
                    String hql = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemEntity cse join fetch cse.codeSystemVersionEntityMemberships csvem join fetch csvem.codeSystemVersion csv join fetch csev.codeSystemConcepts csc"
                            + " where csc.code=:code and"
                            + " csv.oid=:oid";

                    LOGGER.info("HB Session 5.1");
                    org.hibernate.Query q = hb_session.createQuery(hql);
                    LOGGER.info("HB Session 5.2");
                    q.setString("code", code);
                    q.setString("oid", oid);
                    //q.setReadOnly(true);

                    List<CodeSystemEntityVersion> csevList = q.list();

                    if (csevList != null && csevList.size() >= 1)
                    {
                        //Im falle einer Lebenden Liste soll der aktuelle Code genommen werden
                        if (csevList.size() > 1)
                        {
                            Collections.sort(csevList, new DateComparator());
                        }

                        // Version-ID gefunden, nun übergeben
                        LOGGER.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());
                        csev.setVersionId(csevList.get(0).getVersionId());

                        //Matthias, Translation for Laborparameter added
                        if (isElgaLaborparamter)
                        {
                            //check if concept has allready a german translation
                            LOGGER.info("HB Session 6.1");
                            CodeSystemConcept csc = (CodeSystemConcept) hb_session.get(CodeSystemConcept.class, csevList.get(0).getVersionId());
                            LOGGER.info("HB Session 6.2");
                            boolean translationAvailable = false;

                            if (csc.getCodeSystemConceptTranslations() != null)
                            {
                                Iterator<CodeSystemConceptTranslation> itTranslations = csc.getCodeSystemConceptTranslations().iterator();
                                if (itTranslations.hasNext())
                                {
                                    CodeSystemConceptTranslation translation = itTranslations.next();

                                    if (translation.getLanguageId() == 33l)
                                    {
                                        if (translation.getTerm().equals(conceptDetails.get("displayName"))
                                                && translation.getTermAbbrevation().equals(conceptDetails.get("displayNameAlt")))
                                        {
                                            translationAvailable = true;
                                        }
                                        else
                                        {
                                            //german translation available but new terms are available
                                            translation.setTerm(conceptDetails.get("displayName"));
                                            translation.setTermAbbrevation(conceptDetails.get("displayNameAlt"));
                                            LOGGER.info("HB Session 7.1");
                                            hb_session.update(translation);
                                            LOGGER.info("HB Session 7.2");
                                            translationAvailable = true;
                                        }
                                    }
                                }
                            }

                            if (!translationAvailable && conceptDetails.get("displayName") != null && !conceptDetails.get("displayName").equals(""))
                            {
                                //translation anlegen
                                CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
                                csct.setLanguageId(33l);
                                csct.setTerm(conceptDetails.get("displayName"));

                                if (conceptDetails.get("displayNameAlt") != null)
                                {
                                    csct.setTermAbbrevation(conceptDetails.get("displayNameAlt"));
                                }

                                csct.setCodeSystemConcept(csc);
                                LOGGER.info("HB Session 8.1");
                                hb_session.save(csct);
                                LOGGER.info("HB Session 8.2");
                            }
                        }

                    }
                    else
                    {

                        String hql2 = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                                + " where csc.code=:code and"
                                + " csv.oid=:oid";
                        String adaptedCode = "_" + code;
                        LOGGER.info("HB Session 9.1");
                        org.hibernate.Query q2 = hb_session.createQuery(hql2);
                        LOGGER.info("HB Session 9.2");
                        q2.setString("code", adaptedCode);
                        q2.setString("oid", oid);

                        List<CodeSystemEntityVersion> csevList2 = q2.list();
                        if (csevList2 != null && csevList2.size() >= 1)
                        {
                            //Im falle einer Lebenden Liste soll der aktuelle Code genommen werden
                            if (csevList2.size() > 1)
                            {
                                Collections.sort(csevList2, new DateComparator());
                            }

                            // Version-ID gefunden, nun übergeben
                            LOGGER.debug("Version-ID anhand des Codes bestimmt: " + csevList2.get(0).getVersionId());
                            csev.setVersionId(csevList2.get(0).getVersionId());

                        }
                        else
                        {
                            LOGGER.debug("Entity zu Code '" + code + "/" + adaptedCode + "' nicht gefunden!");
                            throw new Exception("Entity zu Code '" + code + "/" + adaptedCode + "' nicht gefunden!");
                        }
                    }
                    csev.getConceptValueSetMemberships().clear();
                    csev.getConceptValueSetMemberships().add(cvsm);
                    // Konzept hinzufügen
                    CodeSystemEntity cse = new CodeSystemEntity();
                    cse.getCodeSystemEntityVersions().add(csev);
                    request.getCodeSystemEntity().add(cse);

                    if (counter % 500 == 0)
                    {
                        LOGGER.info("HB Session 10.1");
                        hb_session.flush();
                        LOGGER.info("HB Session 10.2");
                        LOGGER.info("HB Session 11.1");
                        hb_session.clear();
                        LOGGER.info("HB Session 11.2");
                        LOGGER.info("Session flushed");
                    }
                }

                LOGGER.info("HB Session 12.1");
                if(!hb_session.getTransaction().wasCommitted())
                {
                    LOGGER.info("HB Session 12.2");
                    LOGGER.info("HB Session 13.1");
                    hb_session.getTransaction().commit(); //TRMMRK ENTKOMMENTIERT
                    LOGGER.info("HB Session 13.2");
                }
                else
                    LOGGER.info("HB Session 12.2");
                
                CreateValueSetContent createValueSetContent = new CreateValueSetContent();
                LOGGER.info("HB Session 14.1");
                CreateValueSetContentResponseType response = createValueSetContent.CreateValueSetContent(request, hb_session);
                LOGGER.info("HB Session 14.2");
                
                
                //NUR BEI FREIGABE EXEKUTIEREN TRMMRK
                if(this.getImportType().getRole() != null && this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
                    hb_session.getTransaction().begin(); //ADDED
                
                //Hier erst daten für ValueSetMetadataValue einfügen!!!
                if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
                {
                    Iterator<HashMap<String, String>> it = conceptsList.iterator();

                    //Matthias flush cycle added
                    counter = 0;
                    while (it.hasNext())
                    {

                        if (StaticStatusList.getStatus(this.getImportId()).cancel)
                          break;
                        counter++;

                        this.setCurrentCountInStatusList(counter, this.getImportId());
                        if(this.getImportType().getRole() != null && this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
                        {
                            //Status bei Freigabe in log file ausgeben
                            LOGGER.info("Import progress: " + counter + "/" + this.getTotalCountInStatusList(this.getImportId()));
                        }
                        
                        HashMap<String, String> conceptDetails = (HashMap<String, String>) it.next();
                        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();

                        // Version-ID muss anhand des Codes bestimmt werden
                        String code = conceptDetails.get("code");

                        LOGGER.debug("Entity zu Code '" + code + "' wird gesucht...");
                        String oid = conceptDetails.get("codeSystem");

                        //Matthias: try to improve poor performance of the following query
                        /*String hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                        + " where csc.code="+code+" and"
                        + " csv.oid="+oid+"";
			org.hibernate.Query q = hb_session.createQuery(hql);*/
                        String hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                                + " where csc.code=:code and"
                                + " csv.oid=:oid";

                        LOGGER.info("HB Session 15.1");
                        org.hibernate.Query q = hb_session.createQuery(hql);
                        LOGGER.info("HB Session 15.2");
                        //q.setReadOnly(true);
                        q.setParameter("code", code);
                        q.setParameter("oid", oid);

                        List<CodeSystemEntityVersion> csevList = q.list();
                        if (csevList != null && csevList.size() >= 1)
                        {
                            if (csevList.size() > 1)
                            {
                                Collections.sort(csevList, new DateComparator());
                            }
                            // Version-ID gefunden, nun übergeben
                            LOGGER.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());

                            // Metadaten einfügen
                            String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase
                            if (mdLevelValue != null && mdLevelValue.length() > 0)
                            {
                                LOGGER.info("TRMMRK 1 mdLevelValue");
                                //Check if parameter already set in case of new Version!
                                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                LOGGER.info("MP.id=" + headerMetadataIDs.get("Level") + "\tCSEV.versiondId=" + csevList.get(0).getVersionId() + "\tVSMV.valuesetVersiondId=" + request.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
                                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                // Parameter hinzufügen (immer mit AND verbunden)
                                hql2 += parameterHelper.getWhere("");
                                LOGGER.debug("HQL: " + hql2);

                                // Query erstellen
                                LOGGER.info("HB Session 16.1");
                                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                LOGGER.info("HB Session 16.2");
                                LOGGER.info("TRMMRK 1.1 mdLevelValue");
                                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                parameterHelper.applyParameter(q1);

                                LOGGER.info("TRMMRK 1.2 mdLevelValue");
                                List<ValueSetMetadataValue> valueList = q1.list();

                                if (valueList.size() == 1)
                                {
                                    LOGGER.info("TRMMRK 1.3 mdLevelValue");
                                    valueList.get(0).setParameterValue(mdLevelValue);
                                }

                                if(valueList.size() > 0)
                                {
                                    LOGGER.info("TRMMRK 1.4 mdLevelValue");
                                    //TRMMRK 1.1 added if
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    LOGGER.info("HB Session 17.1");
                                    hb_session.update(valueList.get(0));
                                    LOGGER.info("HB Session 17.2");
                                }
                            }

                            String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
                            if (mdTypeValue != null && mdTypeValue.length() > 0)
                            {
                                LOGGER.info("TRMMRK 2 mdTypeValue");
                                //Check if parameter already set in case of new Version!
                                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
                                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                LOGGER.info("MRK parameters added");
                                
                                if (mdTypeValue.equals("A"))
                                {
                                    LOGGER.info("MRK equals A");
                                    ConceptValueSetMembership cvsm_db = null;
                                    ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                                            csevList.get(0).getVersionId(), request.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                                    LOGGER.info("HB Session 18.1");
                                    cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsmId);
                                    LOGGER.info("HB Session 18.2");
                                    
                                    cvsm_db.setIsStructureEntry(true);

                                    LOGGER.info("MRK before update");
                                    
                                    LOGGER.info("HB Session 19.1");
                                    if(!hb_session.getTransaction().wasCommitted()) //MRK ADDED
                                    {
                                        LOGGER.info("HB Session 19.2");
                                        
                                        LOGGER.info("HB Session 20.1");
                                        LOGGER.info("MRK updating");
                                        hb_session.update(cvsm_db);
                                        LOGGER.info("HB Session 20.2");
                                        LOGGER.info("MRK updated");
                                    }
                                    else
                                        LOGGER.info("HB Session 19.2");
                                    
                                    LOGGER.info("MRK after update");
                                }

                                // Parameter hinzufügen (immer mit AND verbunden)
                                hql2 += parameterHelper.getWhere("");
                                LOGGER.debug("HQL: " + hql2);

                                // Query erstellen
                                LOGGER.info("HB Session 21.1");
                                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                LOGGER.info("HB Session 21.2");
                                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                parameterHelper.applyParameter(q1);

                                List<ValueSetMetadataValue> valueList = q1.list();

                                if (valueList.size() == 1)
                                {
                                    valueList.get(0).setParameterValue(mdTypeValue);
                                }

                                LOGGER.info("MRK BEFORE UPDATE");
                                
                                LOGGER.info("HB Session 22.1");
                                if(valueList.size() > 0 && !hb_session.getTransaction().wasCommitted()) //TRMRMK ADDED
                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                {
                                    LOGGER.info("HB Session 22.2");
                                    LOGGER.info("HB Session 23.1");
                                    LOGGER.info("MRK BEFORE LAST UPDATE");
                                    hb_session.update(valueList.get(0));
                                    LOGGER.info("HB Session 23.2");
                                    LOGGER.info("MRK AFTER LAST UPDATE");
                                }
                                else
                                    LOGGER.info("HB Session 22.2");
                            }

                            String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
                            if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
                            {
                                LOGGER.info("TRMMRK 3 mdRelationshipsValue");
                                //Check if parameter already set in case of new Version!
                                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
                                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                // Parameter hinzufügen (immer mit AND verbunden)
                                hql2 += parameterHelper.getWhere("");
                                LOGGER.debug("HQL: " + hql2);

                                // Query erstellen
                                LOGGER.info("HB Session 24.1");
                                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                LOGGER.info("HB Session 24.2");
                                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                parameterHelper.applyParameter(q1);

                                List<ValueSetMetadataValue> valueList = q1.list();

                                if (valueList.size() == 1)
                                {
                                    valueList.get(0).setParameterValue(mdRelationshipsValue);
                                }

                                
                                if(valueList.size() > 0) //TRMRMK ADDED
                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                {
                                    LOGGER.info("HB Session 25.1");
                                    hb_session.update(valueList.get(0));
                                    LOGGER.info("HB Session 25.2");
                                }
                                    
                                    
                            }
                            String mdEinheit_printValue = conceptDetails.get("einheit_print");////Achtung in Maps lowerCase
                            if (mdEinheit_printValue != null && mdEinheit_printValue.length() > 0)
                            {
                                LOGGER.info("TRMMRK 4 mdEinheit_printValue");
                                //Check if parameter already set in case of new Version!
                                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit print"));
                                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                // Parameter hinzufügen (immer mit AND verbunden)
                                hql2 += parameterHelper.getWhere("");
                                LOGGER.debug("HQL: " + hql2);

                                // Query erstellen
                                LOGGER.info("HB Session 26.1");
                                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                LOGGER.info("HB Session 26.2");
                                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                parameterHelper.applyParameter(q1);

                                List<ValueSetMetadataValue> valueList = q1.list();

                                if (valueList.size() == 1)
                                {
                                    valueList.get(0).setParameterValue(mdEinheit_printValue);
                                }

                                if(valueList.size() > 0) //TRMRMK ADDED
                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                {
                                    LOGGER.info("HB Session 27.1");
                                    hb_session.update(valueList.get(0));
                                    LOGGER.info("HB Session 27.2");
                                }
                                    
                            }

                            String mdEinheit_codiertValue = conceptDetails.get("einheit_codiert");////Achtung in Maps lowerCase
                            if (mdEinheit_codiertValue != null && mdEinheit_codiertValue.length() > 0)
                            {
                                LOGGER.info("TRMMRK 5 mdEinheit_codiertValue");
                                //Check if parameter already set in case of new Version!
                                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit codiert"));
                                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                // Parameter hinzufügen (immer mit AND verbunden)
                                hql2 += parameterHelper.getWhere("");
                                LOGGER.debug("HQL: " + hql2);

                                // Query erstellen
                                LOGGER.info("HB Session 28.1");
                                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                LOGGER.info("HB Session 28.2");
                                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                parameterHelper.applyParameter(q1);

                                List<ValueSetMetadataValue> valueList = q1.list();

                                if (valueList.size() == 1)
                                {
                                    valueList.get(0).setParameterValue(mdEinheit_codiertValue);
                                }

                                if(valueList.size() > 0) //TRMRMK ADDED
                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                {
                                    LOGGER.info("HB Session 29.1");
                                    hb_session.update(valueList.get(0));
                                    LOGGER.info("HB Session 29.2");
                                }
                            }
                        }
                        else
                        {

                            csev = new CodeSystemEntityVersion();

                            // Version-ID muss anhand des Codes bestimmt werden
                            code = "_" + code;

                            LOGGER.debug("Entity zu Code '" + code + "' wird gesucht...");

                            hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                                    + " where csc.code=:code and"
                                    + " csv.oid=:oid";

                            LOGGER.info("HB Session 30.1");
                            q = hb_session.createQuery(hql);
                            LOGGER.info("HB Session 30.2");
                            q.setString("code", code);
                            q.setString("oid", oid);

                            if (code.equals("1709716"))
                            {
                                boolean stop = true;
                            }

                            csevList = q.list();
                            if (csevList != null && csevList.size() >= 1)
                            {
                                if (csevList.size() > 1)
                                {
                                    Collections.sort(csevList, new DateComparator());
                                }
                                // Version-ID gefunden, nun übergeben
                                LOGGER.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());

                                // Metadaten einfügen
                                String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase
                                if (mdLevelValue != null && mdLevelValue.length() > 0)
                                {
                                    LOGGER.info("TRMMRK 2.1 mdLevelValue");
                                    //Check if parameter already set in case of new Version!
                                    String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                    hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
                                    parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                    parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                    // Parameter hinzufügen (immer mit AND verbunden)
                                    hql2 += parameterHelper.getWhere("");
                                    LOGGER.debug("HQL: " + hql2);

                                    // Query erstellen
                                    LOGGER.info("HB Session 31.1");
                                    org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                    LOGGER.info("HB Session 31.2");
                                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                    parameterHelper.applyParameter(q1);

                                    List<ValueSetMetadataValue> valueList = q1.list();

                                    if (valueList.size() == 1)
                                    {
                                        valueList.get(0).setParameterValue(mdLevelValue);
                                    }

                                    if(valueList.size() > 0) //TRMRMK ADDED
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    {
                                        LOGGER.info("HB Session 32.1");
                                        hb_session.update(valueList.get(0));
                                        LOGGER.info("HB Session 32.2");
                                    }
                                }

                                String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
                                if (mdTypeValue != null && mdTypeValue.length() > 0)
                                {
                                    LOGGER.info("TRMMRK 2.2 mdTypeValue");
                                    //Check if parameter already set in case of new Version!
                                    String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                    hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
                                    parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                    parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                    if (mdTypeValue.equals("A"))
                                    {

                                        ConceptValueSetMembership cvsm_db = null;
                                        ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                                                csevList.get(0).getVersionId(), request.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                                        LOGGER.info("HB Session 33.1");
                                        cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsmId);
                                        LOGGER.info("HB Session 33.2");
                                        
                                        cvsm_db.setIsStructureEntry(true);

                                        LOGGER.info("HB Session 34.1");
                                        hb_session.update(cvsm_db);
                                        LOGGER.info("HB Session 34.2");
                                    }

                                    // Parameter hinzufügen (immer mit AND verbunden)
                                    hql2 += parameterHelper.getWhere("");
                                    LOGGER.debug("HQL: " + hql2);

                                    // Query erstellen
                                    LOGGER.info("HB Session 35.1");
                                    org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                    LOGGER.info("HB Session 35.2");
                                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                    parameterHelper.applyParameter(q1);

                                    List<ValueSetMetadataValue> valueList = q1.list();

                                    if (valueList.size() == 1)
                                    {
                                        valueList.get(0).setParameterValue(mdTypeValue);
                                    }

                                    if(valueList.size() > 0) //TRMRMK ADDED
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    {
                                        LOGGER.info("HB Session 36.1");
                                        hb_session.update(valueList.get(0));
                                        LOGGER.info("HB Session 36.2");
                                    }
                                }

                                String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
                                if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
                                {
                                    LOGGER.info("TRMMRK 2.3 mdRelationshipsValue");
                                    //Check if parameter already set in case of new Version!
                                    String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                    hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
                                    parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                    parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                    // Parameter hinzufügen (immer mit AND verbunden)
                                    hql2 += parameterHelper.getWhere("");
                                    LOGGER.debug("HQL: " + hql2);

                                    // Query erstellen
                                    LOGGER.info("HB Session 37.1");
                                    org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                    LOGGER.info("HB Session 37.2");
                                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                    parameterHelper.applyParameter(q1);

                                    List<ValueSetMetadataValue> valueList = q1.list();

                                    if (valueList.size() == 1)
                                    {
                                        valueList.get(0).setParameterValue(mdRelationshipsValue);
                                    }

                                    if(valueList.size() > 0) //TRMRMK ADDED
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    {
                                        LOGGER.info("HB Session 38.1");
                                        hb_session.update(valueList.get(0));
                                        LOGGER.info("HB Session 38.2");
                                    }
                                }
                                String mdEinheit_printValue = conceptDetails.get("einheit_print");////Achtung in Maps lowerCase
                                if (mdEinheit_printValue != null && mdEinheit_printValue.length() > 0)
                                {
                                    LOGGER.info("TRMMRK 2.4 mdEinheit_printValue");
                                    //Check if parameter already set in case of new Version!
                                    String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                    hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit print"));
                                    parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                    parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                    // Parameter hinzufügen (immer mit AND verbunden)
                                    hql2 += parameterHelper.getWhere("");
                                    LOGGER.debug("HQL: " + hql2);

                                    // Query erstellen
                                    LOGGER.info("HB Session 39.1");
                                    org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                    LOGGER.info("HB Session 39.2");
                                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                    parameterHelper.applyParameter(q1);

                                    List<ValueSetMetadataValue> valueList = q1.list();

                                    if (valueList.size() == 1)
                                    {
                                        valueList.get(0).setParameterValue(mdEinheit_printValue);
                                    }

                                    if(valueList.size() > 0) //TRMRMK ADDED
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    {
                                        LOGGER.info("HB Session 40.1");
                                        hb_session.update(valueList.get(0));
                                        LOGGER.info("HB Session 40.2");
                                    }
                                }
                                String mdEinheit_codiertValue = conceptDetails.get("einheit_codiert");////Achtung in Maps lowerCase
                                if (mdEinheit_codiertValue != null && mdEinheit_codiertValue.length() > 0)
                                {
                                    LOGGER.info("TRMMRK 2.5 mdEinheit_codiertValue");
                                    //Check if parameter already set in case of new Version!
                                    String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                                    hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit codiert"));
                                    parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                                    parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                                    // Parameter hinzufügen (immer mit AND verbunden)
                                    hql2 += parameterHelper.getWhere("");
                                    LOGGER.debug("HQL: " + hql2);

                                    // Query erstellen
                                    LOGGER.info("HB Session 41.1");
                                    org.hibernate.Query q1 = hb_session.createQuery(hql2);
                                    LOGGER.info("HB Session 41.2");
                                    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                                    parameterHelper.applyParameter(q1);

                                    List<ValueSetMetadataValue> valueList = q1.list();

                                    if (valueList.size() == 1)
                                    {
                                        valueList.get(0).setParameterValue(mdEinheit_codiertValue);
                                    }

                                    if(valueList.size() > 0) //TRMRMK ADDED
                                    //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                                    {
                                        LOGGER.info("HB Session 42.1");
                                        hb_session.update(valueList.get(0));
                                        LOGGER.info("HB Session 42.2");
                                    }
                                }
                            }
                            else
                            {

                                LOGGER.debug("Entity zu Code '" + code + "' nicht gefunden!");
                                throw new Exception("Entity zu Code '" + code + "' nicht gefunden!");
                            }
                        }

                        if (counter % 500 == 0)
                        {
                            LOGGER.info("HB Session 43.1");
                            hb_session.flush();
                            LOGGER.info("HB Session 43.2");
                            LOGGER.info("HB Session 44.1");
                            hb_session.clear();
                            LOGGER.info("HB Session 44.2");
                            LOGGER.info("Session flushed");
                        }
                    }

                    LOGGER.info("HB Session 45.1");
                    if(!hb_session.getTransaction().wasCommitted()) //TRMMRK
                    {
                        LOGGER.info("HB Session 45.2");
                        LOGGER.info("HB Session 46.1");
                        hb_session.getTransaction().commit();
                        LOGGER.info("HB Session 46.2");
                    }
                    else
                        LOGGER.info("HB Session 45.2");
                        

                    ValueSet vs_ret = new ValueSet();
                    vs_ret.setId(this.getValueset().getId());
                    vs_ret.setCurrentVersionId(this.getValueset().getCurrentVersionId());
                    vs_ret.setName(this.getValueset().getName());
                    vs_ret.setAutoRelease(this.getValueset().getAutoRelease());

                    ValueSetVersion vsv_ret = new ValueSetVersion();
                    vsv_ret.setVersionId(this.getValueset().getValueSetVersions().iterator().next().getVersionId());
                    vsv_ret.setName(this.getValueset().getValueSetVersions().iterator().next().getName());
                    vs_ret.getValueSetVersions().clear();
                    vs_ret.getValueSetVersions().add(vsv_ret);

                    this.setValueset(vs_ret);
                }
                else
                {
                    LOGGER.info("HB Session 47.1");
                    if(!hb_session.getTransaction().wasRolledBack())
                    {
                        LOGGER.info("HB Session 47.2");
                        LOGGER.info("HB Session 48.1");
                        hb_session.getTransaction().rollback();
                        LOGGER.info("HB Session 48.2");
                    }
                    else
                        LOGGER.info("HB Session 47.2");

                    String resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);
                    
                    throw new ImportException("Fehler beim Importieren von Value Set-Inhalten: " + response.getReturnInfos().getMessage());
                }
            }
            catch (Exception ex)
            {
                //ex.printStackTrace();
                LOGGER.error(ex.getMessage());

                try
                {
                    LOGGER.info("HB Session 49.1");
                    if(!hb_session.getTransaction().wasRolledBack()){
                        LOGGER.info("HB Session 49.2");
                        LOGGER.info("HB Session 50.1");
                        hb_session.getTransaction().rollback();
                        LOGGER.info("HB Session 50.2");
                        LOGGER.info("[ImportCSV.java] Rollback versucht!");
                    }
                    else
                        LOGGER.info("HB Session 49.2");
                    
                    String resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);
                    throw new ImportException("Fehler beim Import eines Value Sets: " + ex.getLocalizedMessage()+ "\n"+resultStr);
                }
                catch (Exception exRollback)
                {
                    LOGGER.info("HB Session 51.1");
                    if(!hb_session.getTransaction().wasRolledBack()){
                        LOGGER.info("HB Session 51.2");
                        LOGGER.info("[ImportCSV.java] Rollback fehlgeschlagen: " + exRollback.getMessage());
                        throw new ImportException("Rollback fehlgeschlagen! Fehler beim Import eines Value Sets: " + exRollback.getLocalizedMessage());
                    }
                    else
                        LOGGER.info("HB Session 51.2");
                }
            }
            finally
            {
                // Session schlieÃƒÂŸen
                StaticStatusList.getStatus(this.getImportId()).importRunning = false;
                LOGGER.info("HB Session 52.1");
                hb_session.close();
                LOGGER.info("HB Session 52.2");
            }
        }
        catch (Exception ex)
        {
            LOGGER.error(ex);
            throw new ImportException("Fehler beim Importieren von Value Set-Inhalten: " + ex.getLocalizedMessage());
        }
    }
    
    private boolean createValueSet(HashMap<String, String> codeListInfoMap)
    {

        LOGGER.debug("createValueSet...");
        LOGGER.info("HB Session 53.1");
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        LOGGER.info("HB Session 53.2");
        LOGGER.info("HB Session 54.1");
        hb_session.getTransaction().begin();
        LOGGER.info("HB Session 54.2");
            
        // vorhandenes Value Set nutzen oder neues anlegen?
        if (this.getValueset().getId() != null && this.getValueset().getId() > 0 && false)//false for quick fix trmmrk
        {

            onlyVSV = true;
            try
            {
                LOGGER.debug("ID ist angegeben");
                Date date = new java.util.Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                LOGGER.info("HB Session 55.1: ID = " + getValueset().getId());
                ValueSet vs_db = (ValueSet) hb_session.get(ValueSet.class, this.getValueset().getId()); //TRMMRK AUSKOMMENTIERT FUER UNTEREN BLOCK NEU
                LOGGER.info("VS_DB NULL? " + (vs_db == null));
                LOGGER.info("HB Session 55.2");
                
                ValueSetVersion vsvNew = new ValueSetVersion();
                vsvNew.setValidityRange(238l); // optional
                LOGGER.info("HB Session 55.2.1");
                vsvNew.setName(codeListInfoMap.get("version"));
                vsvNew.setInsertTimestamp(date);
                LOGGER.info("HB Session 55.2.2");
                vsvNew.setOid(codeListInfoMap.get("id"));
                LOGGER.info("HB Session 55.2.3");
                if (codeListInfoMap.get("effectiveDate") != null && !codeListInfoMap.get("effectiveDate").equals(""))
                {
                    LOGGER.info("HB Session 55.2.4");
                    vsvNew.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));
                }

                LOGGER.info("HB Session 55.2.5");
                if (!this.getImportType().getRole().equals(CODES.ROLE_TRANSFER))
                {
                    LOGGER.info("HB Session 55.2.6");
                    vsvNew.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                }
                else
                {
                    LOGGER.info("HB Session 55.2.7");
                    vsvNew.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                }

LOGGER.info("HB Session 55.2.8");
                vsvNew.setStatusDate(date);
                vsvNew.setValidityRange(236l);
                vsvNew.setConceptValueSetMemberships(null);
                LOGGER.info("HB Session 55.2.9, vs_db currentVersionID = " + vs_db.getCurrentVersionId());
                
                vsvNew.setPreviousVersionId(vs_db.getCurrentVersionId());
                //vsvNew.setPreviousVersionId(this.getValueset().getCurrentVersionId());//TRMMRK
                LOGGER.info("HB Session 55.2.9.1");
                vsvNew.setValueSet(new ValueSet());
                LOGGER.info("HB Session 55.2.9.2");
                vsvNew.getValueSet().setId(vs_db.getId());
                LOGGER.info("HB Session 55.2.10");
                //vsvNew.getValueSet().setId(this.getValueset().getId()); //TRMMRK
                
                
                LOGGER.info("HB Session 55.2.9");
                
                LOGGER.info("VS DB NULL?: " + vs_db == null);
                
                vsvNew.setPreviousVersionId(vs_db.getCurrentVersionId());
                //vsvNew.setPreviousVersionId(this.getValueset().getCurrentVersionId());//TRMMRK
                LOGGER.info("HB Session 55.2.9.1");
                vsvNew.setValueSet(new ValueSet());
                LOGGER.info("HB Session 55.2.9.2");
                vsvNew.getValueSet().setId(vs_db.getId());
                LOGGER.info("HB Session 55.2.10");
                //vsvNew.getValueSet().setId(this.getValueset().getId()); //TRMMRK
                
                /*
                // In DB speichern damit vsvNew eine ID bekommt NEU NEU NEU
                LOGGER.info("HB Session 56.1");
                hb_session.save(vsvNew);
                LOGGER.info("HB Session 56.2");
                //vs_db.setCurrentVersionId(vsvNew.getVersionId());
                LOGGER.info("HB Session 57.1");
                //hb_session.update(vs_db);
                LOGGER.info("HB Session 57.2");
                //Reload
                LOGGER.info("HB Session 58.1");
                //vs_db = (ValueSet) hb_session.get(ValueSet.class, this.getValueset().getId());
                LOGGER.info("HB Session 58.2");
                //this.setValueset(vs_db);*/
                
                //NEU ENDE
                
                /*
                // In DB speichern damit vsvNew eine ID bekommt
                LOGGER.info("HB Session 56.1");
                hb_session.save(vsvNew);
                LOGGER.info("HB Session 56.2");
                vs_db.setCurrentVersionId(vsvNew.getVersionId());
                LOGGER.info("HB Session 57.1");
                hb_session.update(vs_db);
                LOGGER.info("HB Session 57.2");
                //Reload
                LOGGER.info("HB Session 58.1");
                vs_db = (ValueSet) hb_session.get(ValueSet.class, this.getValueset().getId());
                LOGGER.info("HB Session 58.2");
                this.setValueset(vs_db);*/
            }
            catch (Exception ex)
            {

                LOGGER.error("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
                return false;

            }
            finally
            {
                Long vsvId = ((ValueSetVersion) this.getValueset().getValueSetVersions().toArray()[0]).getVersionId();
                if (this.getValueset().getId() > 0 && vsvId > 0)
                {
                    LOGGER.info("HB Session 59.1");
                    hb_session.getTransaction().commit();
                    LOGGER.info("HB Session 59.2");
                }
                else
                {
                    // ÃƒÂ„nderungen nicht erfolgreich
                    LOGGER.warn("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
                    LOGGER.info("HB Session 60.1");
                    hb_session.getTransaction().rollback();
                    LOGGER.info("HB Session 60.2");
                }
                LOGGER.info("HB Session 61.1");
                hb_session.close();
                LOGGER.info("HB Session 61.2");
            }
        }
        else
        {

            onlyVSV = false;
            try
            {
                LOGGER.debug("ID ist angegeben");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new java.util.Date();

                //New ValueSet
                ValueSet vs = new ValueSet();
                vs.setName(this.getValueset().getName());
                vs.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
                vs.setStatusDate(date);
                vs.setValueSetVersions(null);
                vs.setWebsite(codeListInfoMap.get("website"));
                vs.setDescription(codeListInfoMap.get("beschreibung"));
                vs.setDescriptionEng(codeListInfoMap.get("description"));

                // In DB speichern
                LOGGER.info("HB Session 62.1");
                hb_session.save(vs);
                LOGGER.info("HB Session 62.2");

                //New Version
                ValueSetVersion vsvNew = new ValueSetVersion();
                vsvNew.setValidityRange(238l); // empfohlen
                vsvNew.setName(codeListInfoMap.get("version"));
                vsvNew.setInsertTimestamp(date);
                vsvNew.setOid(codeListInfoMap.get("id"));
                if (codeListInfoMap.get("effectiveDate") != null && !codeListInfoMap.get("effectiveDate").equals(""))
                {
                    vsvNew.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));
                }

                vsvNew.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());

                vsvNew.setStatusDate(date);
                vsvNew.setConceptValueSetMemberships(null);
                vsvNew.setValueSet(new ValueSet());
                vsvNew.getValueSet().setId(vs.getId());
                vsvNew.setPreferredLanguageId(33l); //German

                // In DB speichern damit vsvNew eine ID bekommt
                LOGGER.info("HB Session 63.1");
                hb_session.save(vsvNew);
                LOGGER.info("HB Session 63.2");

                vs.setCurrentVersionId(vsvNew.getVersionId());
                vs.setValueSetVersions(new HashSet<ValueSetVersion>());
                vs.getValueSetVersions().add(vsvNew);
                LOGGER.info("HB Session 64.1");
                hb_session.update(vs);
                LOGGER.info("HB Session 64.2");
                //Reload
                LOGGER.info("HB Session 65.1");
                vs = (ValueSet) hb_session.get(ValueSet.class, vs.getId());
                LOGGER.info("HB Session 65.2");
                this.setValueset(vs);

            }
            catch (Exception ex)
            {

                LOGGER.error("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
                return false;

            }
            finally
            {
                Long vsvId = ((ValueSetVersion) this.getValueset().getValueSetVersions().toArray()[0]).getVersionId();
                if (this.getValueset().getId() > 0 && vsvId > 0)
                {
                    LOGGER.info("HB Session 66.1");
                    hb_session.getTransaction().commit();
                    LOGGER.info("HB Session 66.2");
                }
                else
                {
                    // Änderungen nicht erfolgreich
                    LOGGER.warn("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
                    LOGGER.info("HB Session 67.1");
                    hb_session.getTransaction().rollback();
                    LOGGER.info("HB Session 67.2");
                }
                LOGGER.info("HB Session 68.1");
                hb_session.close();
                LOGGER.info("HB Session 68.2");
            }
        }

        return true;
    }
    
}
