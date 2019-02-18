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

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.EscCharCheck;
import de.fhdo.terminologie.helper.ValidityRangeHelper;
import de.fhdo.terminologie.helper.XMLFormatter;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.search.ListValueSetContents;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetails;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Philipp Urbauer
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetSVS
{

    private static Logger logger = Logger4j.getInstance().getLogger();
    ExportValueSetContentRequestType parameter;
    private int countExported = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");

    public ExportValueSetSVS(ExportValueSetContentRequestType _parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ExportSVS gestartet ======");
        }

        parameter = _parameter;
    }

    public String exportSVS(ExportValueSetContentResponseType reponse)
    {
        String s = "";  // Status-Meldung
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean isELGALaborparameter = false;

        ExportType exportType = new ExportType();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc = null;

        try
        {
            builder = builderFactory.newDocumentBuilder();

            doc = builder.newDocument();
            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //StreamResult result = new StreamResult(bos);
            //TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //Transformer transformer = transformerFactory.newTransformer();
            //DOMSource source = new DOMSource(doc);

            if (parameter.getExportInfos().isUpdateCheck())
            {
                //Request-Parameter für ReturnValueSetDetails erstellen
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportSVS] Erstelle Request-Parameter für ReturnValueSetDetails");
                }

                ReturnValueSetDetailsRequestType requestValueSetDetails = new ReturnValueSetDetailsRequestType();
                requestValueSetDetails.setValueSet(parameter.getValueSet());
                requestValueSetDetails.getValueSet().getValueSetVersions().add((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]);
                requestValueSetDetails.setLogin(parameter.getLogin());

                //ValueSetDetails abrufen
                ReturnValueSetDetails rcsd = new ReturnValueSetDetails();
                ReturnValueSetDetailsResponseType responseValueSetDetails = rcsd.ReturnValueSetDetails(requestValueSetDetails);
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportSVS] ReturnValueSetDetails abgerufen");
                }

                if (responseValueSetDetails.getReturnInfos().getStatus() == ReturnType.Status.OK
                        && responseValueSetDetails.getValueSet() != null)
                {
                    if (!responseValueSetDetails.getValueSet().getCurrentVersionId().equals(((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId()))
                    {
                        ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).setVersionId(responseValueSetDetails.getValueSet().getCurrentVersionId());
                    }
                }
            }

            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            try
            {
                String hql = "select distinct vsv from ValueSetVersion vsv join vsv.valueSet vs"
                        + " where "
                        + " vsv.versionId=:versionId";

                org.hibernate.Query q = hb_session.createQuery(hql);
                //Matthias: setReadOnly
                q.setReadOnly(true);
                //q.setLong("id", parameter.getValueSet().getId());
                q.setLong("versionId", parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                List<ValueSetVersion> vsvList = q.list();
                ValueSetVersion vsv = null;
                if (vsvList != null && vsvList.size() == 1)
                {
                    vsv = vsvList.get(0);
                }

                if (vsv != null)
                {
                    //Erstellung Hauptknoten
                    Element valueSet = doc.createElement("valueSet");
                    doc.appendChild(valueSet);

                    Attr name = doc.createAttribute("name");
                    name.setValue(EscCharCheck.check(vsv.getValueSet().getName()));
                    valueSet.setAttributeNode(name);

                    Attr displayName = doc.createAttribute("displayName");
                    displayName.setValue(EscCharCheck.check(vsv.getValueSet().getName()));
                    valueSet.setAttributeNode(displayName);

                    //Matthias: check if value set is "ELGA_Laborparameter"
                    if (displayName.getValue().contains("ELGA_Laborparameter"))
                    {
                        isELGALaborparameter = true;
                    }

                    Attr effectiveDate = doc.createAttribute("effectiveDate");
                    if (vsv.getReleaseDate() != null)
                    {
                        effectiveDate.setValue(sdf.format(vsv.getReleaseDate()));
                        valueSet.setAttributeNode(effectiveDate);
                    }
                    else
                    {
                        effectiveDate.setValue("");
                        valueSet.setAttributeNode(effectiveDate);
                    }

                    Attr id = doc.createAttribute("id");
                    id.setValue(EscCharCheck.check(vsv.getOid()));
                    valueSet.setAttributeNode(id);

                    Attr statusCode = doc.createAttribute("statusCode");
                    if (vsv.getStatus() == 1)
                    {
                        statusCode.setValue("final");
                    }
                    else
                    {
                        statusCode.setValue("not final");
                    }
                    valueSet.setAttributeNode(statusCode);

                    Attr website = doc.createAttribute("website");
                    website.setValue(EscCharCheck.check(vsv.getValueSet().getWebsite()));
                    valueSet.setAttributeNode(website);

                    Attr version = doc.createAttribute("version");
                    version.setValue(EscCharCheck.check(vsv.getName()));
                    valueSet.setAttributeNode(version);

                    Attr beschreibung = doc.createAttribute("beschreibung");
                    beschreibung.setValue(EscCharCheck.check(vsv.getValueSet().getDescription()));
                    valueSet.setAttributeNode(beschreibung);

                    Attr description = doc.createAttribute("description");
                    description.setValue(EscCharCheck.check(vsv.getValueSet().getDescriptionEng()));
                    valueSet.setAttributeNode(description);

                    //Matthias respOrg added
                    Attr responsibleOrganization = doc.createAttribute("verantw_Org");
                    responsibleOrganization.setValue(EscCharCheck.check(vsv.getValueSet().getResponsibleOrganization()));
                    valueSet.setAttributeNode(responsibleOrganization);

                    //Matthias: added status information
                    Attr validityRange = doc.createAttribute("gueltigkeitsbereich");
                    if (vsv.getValidityRange() != null)
                    {
                        validityRange.setValue(ValidityRangeHelper.getValidityRangeNameById(vsv.getValidityRange()));
                    }
                    else
                    {
                        validityRange.setValue("");
                    }
                    valueSet.setAttributeNode(validityRange);

                    Attr versionStatus = doc.createAttribute("statusCode");
                    versionStatus.setValue(EscCharCheck.check(vsv.getStatus() + ""));
                    valueSet.setAttributeNode(versionStatus);

                    Attr statusDate = doc.createAttribute("status_date");
                    if (vsv.getStatusDate() != null)
                    {
                        statusDate.setValue(sdf.format(vsv.getStatusDate()));
                    }
                    else
                    {
                        statusDate.setValue("");
                    }
                    valueSet.setAttributeNode(statusDate);

                    Attr lastChangeDate = doc.createAttribute("last_change_date");
                    if (vsv.getLastChangeDate() != null)
                    {
                        lastChangeDate.setValue(sdf.format(vsv.getLastChangeDate()));
                    }
                    else
                    {
                        lastChangeDate.setValue("");
                    }
                    valueSet.setAttributeNode(lastChangeDate);

                    Element conceptList = doc.createElement("conceptList");
                    valueSet.appendChild(conceptList);

                    ListValueSetContentsRequestType requestListCodeSystemConcepts = new ListValueSetContentsRequestType();
                    requestListCodeSystemConcepts.setValueSet(parameter.getValueSet());
                    requestListCodeSystemConcepts.getValueSet().getValueSetVersions().add((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]);
                    requestListCodeSystemConcepts.setLogin(parameter.getLogin());
                    //requestListCodeSystemConcepts.setLookForward(true);
                    
                    boolean syncEnabled = parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null;

                    if (syncEnabled)
                    {
                        // Datum für Synchronisation hinzufgüen
                        logger.debug("Datum für Synchronisation hinzufügen: " + parameter.getExportParameter().getDateFrom().toString());

                        ValueSetVersion vsvRequest = ((ValueSetVersion) requestListCodeSystemConcepts.getValueSet().getValueSetVersions().toArray()[0]);
                        if (vsvRequest.getConceptValueSetMemberships() == null || vsvRequest.getConceptValueSetMemberships().size() == 0)
                        {
                            ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
                            cvsm.setStatusDate(parameter.getExportParameter().getDateFrom());
                            vsvRequest.getConceptValueSetMemberships().add(cvsm);
                        }
                        else
                        {
                            ((ConceptValueSetMembership) vsvRequest.getConceptValueSetMemberships().toArray()[0]).setStatusDate(parameter.getExportParameter().getDateFrom());
                        }
                    }

                    //ListCodeSystemConcepts abrufen
                    ListValueSetContents lcsc = new ListValueSetContents();
                    ListValueSetContentsResponseType responseListCodeSystemConcepts = lcsc.ListValueSetContents(requestListCodeSystemConcepts, hb_session);
                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] ListValueSetContents abgerufen");
                    }

                    //SVS-Inhalt erstellen
                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] Erstelle SVS-Inhalt mit Konzepten...");
                    }

                    // Flaches Vokabular, einfach alle Einträge exportieren
                    for (CodeSystemEntity cse : responseListCodeSystemConcepts.getCodeSystemEntity())
                    {
                        for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
                        {
                            String awBeschreibung = "";
                            // Nur aktuellste Version exportieren
                            if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
                            {

                                ConceptValueSetMembership cvsm = null;
                                for (ConceptValueSetMembership cvsmL : csev.getConceptValueSetMemberships())
                                {
                                    Long cvsmLVsvId = cvsmL.getId().getValuesetVersionId();
                                    Long paramVsvId = parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId();
                                    Long cvsmCsevId = cvsmL.getId().getCodeSystemEntityVersionId();
                                    Long paramCsevId = csev.getVersionId();

                                    if ((cvsmLVsvId.equals(paramVsvId)) && (cvsmCsevId.equals(paramCsevId)))
                                    {
                                        cvsm = cvsmL;
                                        awBeschreibung = cvsmL.getAwbeschreibung();
                                        break;
                                    }
                                }

                                // 1) nur Status=1
                                // 2) alle Status > 0, wenn Synchronisierungsdatum angegeben ist
                                if (cvsm != null && cvsm.getStatus() != null && (cvsm.getStatus() == 1 || (syncEnabled && cvsm.getStatus() > 0)))
                                {
                                    CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();

                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Schreibe Code: " + csc.getCode());
                                    }

                                    Element concept = doc.createElement("concept");

                                    Attr code = doc.createAttribute("code");
                                    code.setValue(EscCharCheck.check(csc.getCode()));
                                    concept.setAttributeNode(code);

                                    //Matthias: change displayName to Translation in case of ELGA_Laborparameter and add alternative displayName
                                    Attr displayNameC = doc.createAttribute("displayName");
                                    if (isELGALaborparameter)
                                    {

                                        String hqlTranslation = "select distinct csc from CodeSystemConcept csc"
                                                + " where"
                                                + " csc.codeSystemEntityVersionId=:codeSystemEntityVersionId";

                                        org.hibernate.Query qTranslation = hb_session.createQuery(hqlTranslation);
                                        qTranslation.setReadOnly(true);
                                        qTranslation.setLong("codeSystemEntityVersionId", csc.getCodeSystemEntityVersionId());

                                        List<CodeSystemConcept> translationList = qTranslation.list();
                                        if (translationList != null)
                                        {
                                            CodeSystemConcept tempConcept = translationList.get(0);

                                            Attr displayNameAlt = doc.createAttribute("displayNameAlt");

                                            Iterator<CodeSystemConceptTranslation> itTranslations = tempConcept.getCodeSystemConceptTranslations().iterator();
                                            if (itTranslations.hasNext())
                                            {
                                                CodeSystemConceptTranslation translation = itTranslations.next();
                                                if (translation.getLanguageId() == 33l)
                                                {
                                                    displayNameC.setValue(EscCharCheck.check(translation.getTerm()));
                                                    concept.setAttributeNode(displayNameC);

                                                    displayNameAlt.setValue(EscCharCheck.check(translation.getTermAbbrevation()));
                                                    concept.setAttributeNode(displayNameAlt);
                                                }
                                            }
                                            else
                                            {
                                                //in case when no translation is available
                                                displayNameC.setValue(EscCharCheck.check(csc.getTerm()));
                                                concept.setAttributeNode(displayNameC);
                                            }
                                        }

                                    }
                                    else
                                    {
                                        displayNameC.setValue(EscCharCheck.check(csc.getTerm()));
                                        concept.setAttributeNode(displayNameC);
                                    }

                                    //Matthias: ParentCodeSystem
                                    String hqlC = "select distinct csv from CodeSystemVersion csv join csv.codeSystemVersionEntityMemberships csvem join csvem.codeSystemEntity cse join cse.codeSystemEntityVersions csev"
                                            + " where csev.versionId=:versionId";

                                    org.hibernate.Query qC = hb_session.createQuery(hqlC);
                                    qC.setReadOnly(true);
                                    qC.setLong("versionId", csev.getVersionId());

                                    List<CodeSystemVersion> csvlist = qC.list();
                                    String parentCodeSystemName;
                                    if (csvlist.size() == 1)
                                    {
                                        Attr parentCodeSystemC = doc.createAttribute("parentCodeSystemName");
                                        parentCodeSystemName = csvlist.get(0).getName();
                                        parentCodeSystemC.setValue(parentCodeSystemName);
                                        concept.setAttributeNode(parentCodeSystemC);

                                        //Matthias: 20151218 override the displayName in case of ELGA_LaborparameterErgänzung
                                        //translation should not be used
                                        if (parentCodeSystemName.contains("ELGA_LaborparameterErgaenzung"))
                                        {
                                            displayNameC.setValue(csc.getTerm());
                                        }

                                    }
                                    else
                                    {
                                        Attr parentCodeSystemC = doc.createAttribute("parentCodeSystemName");
                                        parentCodeSystemC.setValue("");
                                        concept.setAttributeNode(parentCodeSystemC);
                                    }

                                    //Matthias 20.5.2015 adapted for LOINC fully qualified name
                                    if (isELGALaborparameter)
                                    {
                                        Attr concept_beschreibungC = doc.createAttribute("concept_beschreibung");
                                        //concept_beschreibungC.setValue(EscCharCheck.check(cvsm.getAwbeschreibung()));
                                        String tempString = csc.getDescription();
                                        if(tempString != null)
                                        {
                                            tempString = tempString.replace('|', ':');
                                        }
                                        else
                                        {
                                            tempString = "";
                                        }

                                        concept_beschreibungC.setValue(EscCharCheck.check(tempString));
                                        concept.setAttributeNode(concept_beschreibungC);
                                    }
                                    else
                                    {
                                        Attr concept_beschreibungC = doc.createAttribute("concept_beschreibung");
                                        concept_beschreibungC.setValue(EscCharCheck.check(awBeschreibung));
                                        concept.setAttributeNode(concept_beschreibungC);
                                    }
                                    
                                    //Matthias 20.5.2015 adapted for order number in SVS export
                                    if (cvsm.getOrderNr() != null && cvsm.getOrderNr() != 0)
                                    {
                                        Attr concept_orderNumber = doc.createAttribute("orderNumber");
                                        concept_orderNumber.setValue(cvsm.getOrderNr() + "");
                                        concept.setAttributeNode(concept_orderNumber);
                                    }

                                    Attr deutschC = doc.createAttribute("deutsch");
                                    deutschC.setValue(EscCharCheck.check(cvsm.getBedeutung()));
                                    concept.setAttributeNode(deutschC);

                                    Attr hinweiseC = doc.createAttribute("hinweise");
                                    hinweiseC.setValue(EscCharCheck.check(cvsm.getHinweise()));
                                    concept.setAttributeNode(hinweiseC);

                                    /*String hqlC = "select distinct csv from CodeSystemVersion csv join csv.codeSystemVersionEntityMemberships csvem join csvem.codeSystemEntity cse join cse.codeSystemEntityVersions csev"
                   + " where csev.versionId=:versionId";

                   org.hibernate.Query qC = hb_session.createQuery(hqlC);
                   qC.setLong("versionId", parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                   List<CodeSystemVersion> csvlist = qC.list();
                   if(csvlist.size() == 1){*/
                                    Attr codeSystem = doc.createAttribute("codeSystem");
                                    codeSystem.setValue(EscCharCheck.check(cse.getCodeSystemVersionEntityMemberships().iterator().next().getCodeSystemVersion().getOid()));//csvlist.get(0).getOid());
                                    concept.setAttributeNode(codeSystem);
                                    //}

                                    //Matthias: status information added
                                    Attr conceptStatus = doc.createAttribute("conceptStatus");
                                    conceptStatus.setValue(csev.getStatus() + "");
                                    concept.setAttributeNode(conceptStatus);

                                    Attr conceptStatusDate = doc.createAttribute("conceptStatusDate");
                                    if (csev.getStatusDate() != null)
                                    {
                                        conceptStatusDate.setValue(sdf.format(csev.getStatusDate()));
                                    }
                                    else
                                    {
                                        conceptStatusDate.setValue("");
                                    }

                                    concept.setAttributeNode(conceptStatusDate);

                                    String hqlM = "select distinct vsmv from ValueSetMetadataValue vsmv join vsmv.metadataParameter mp join vsmv.codeSystemEntityVersion csev"
                                            + " where vsmv.valuesetVersionId=:valuesetVersionId AND csev.versionId=:codeSystemEntityVersionId";

                                    org.hibernate.Query qM = hb_session.createQuery(hqlM);
                                    //Matthias: setReadOnly
                                    qM.setReadOnly(true);
                                    qM.setLong("valuesetVersionId", parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                                    qM.setLong("codeSystemEntityVersionId", csev.getVersionId());
                                    List<ValueSetMetadataValue> mlist = qM.list();

                                    Iterator<ValueSetMetadataValue> it = mlist.iterator();
                                    if (!mlist.isEmpty())
                                    {
                                        while (it.hasNext())
                                        {

                                            ValueSetMetadataValue vsmv = (ValueSetMetadataValue) it.next();
                                            if (vsmv.getMetadataParameter().getParamName().equals("Level"))
                                            {
                                                Attr level = doc.createAttribute("level");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    level.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    level.setValue("");
                                                }
                                                concept.setAttributeNode(level);
                                            }
                                            if (vsmv.getMetadataParameter().getParamName().equals("Type"))
                                            {
                                                Attr type = doc.createAttribute("type");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    type.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    type.setValue("");
                                                }
                                                concept.setAttributeNode(type);
                                            }
                                            if (vsmv.getMetadataParameter().getParamName().equals("Relationships"))
                                            {
                                                Attr relationships = doc.createAttribute("relationships");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    relationships.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    relationships.setValue("");
                                                }
                                                concept.setAttributeNode(relationships);
                                            }
                                            if (vsmv.getMetadataParameter().getParamName().equals("Einheit print"))
                                            {
                                                Attr einheit_print = doc.createAttribute("einheit_print");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    einheit_print.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    einheit_print.setValue("");
                                                }
                                                concept.setAttributeNode(einheit_print);
                                            }
                                            if (vsmv.getMetadataParameter().getParamName().equals("Einheit codiert"))
                                            {
                                                Attr einheit_codiert = doc.createAttribute("einheit_codiert");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    einheit_codiert.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    einheit_codiert.setValue("");
                                                }
                                                concept.setAttributeNode(einheit_codiert);
                                            }
                                            //Matthias 20.5.2015 export displayNameShort added
                                            if (vsmv.getMetadataParameter().getParamName().equals("Anwendungsbeschreibung"))
                                            {
                                                Attr displayNameShort = doc.createAttribute("displayNameShort");
                                                if (vsmv.getParameterValue() != null)
                                                {
                                                    displayNameShort.setValue(EscCharCheck.check(vsmv.getParameterValue()));
                                                }
                                                else
                                                {
                                                    displayNameShort.setValue("");
                                                }
                                                concept.setAttributeNode(displayNameShort);
                                            }
                                        }
                                    }
                                    else
                                    {

                                        Attr level = doc.createAttribute("level");
                                        level.setValue("");
                                        concept.setAttributeNode(level);

                                        Attr type = doc.createAttribute("type");
                                        type.setValue("");
                                        concept.setAttributeNode(type);

                                        Attr relationships = doc.createAttribute("relationships");
                                        relationships.setValue("");
                                        concept.setAttributeNode(relationships);

                                        Attr einheit_print = doc.createAttribute("einheit_print");
                                        einheit_print.setValue("");
                                        concept.setAttributeNode(einheit_print);

                                        Attr einheit_codiert = doc.createAttribute("einheit_codiert");
                                        einheit_codiert.setValue("");
                                        concept.setAttributeNode(einheit_codiert);
                                    }
                                    conceptList.appendChild(concept);
                                    countExported++;
                                }
                            }
                        }
                    }

                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] SVS-Inhalt erstellt");
                    }
                }
                else
                {
                    //hb_session.close();
                    //throw new Exception("ExportSVS: Keine Ergebnisse oder ValueSetVersion existiert nicht");
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error(ex.getMessage());
                s = "Fehler (hibernate): " + ex.getLocalizedMessage();
            }
            finally
            {
                if(hb_session.isOpen())
                    hb_session.close();
            }

            if (countExported > 0)
            {
                StringWriter writer = new StringWriter();
                try
                {
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer trans = tf.newTransformer();

                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                    XMLFormatter formatter = new XMLFormatter();
                    String formattedXml = formatter.format(output);
                    //formattedXml = formattedXml.replace("\"", "'");
                    formattedXml = formattedXml.replace("&quot;", "\'");
                    //formattedXml = formattedXml.replace("&amp;", "&");
                    //formattedXml = formattedXml.replace("&amp;lt;", "<");
                    //formattedXml = formattedXml.replace("&amp;gt;", ">");
                    //formattedXml = formattedXml.replace("&apos;", "'");
                    formattedXml = formattedXml.replace("\">\"", "\"&gt;\"");
                    

                    exportType.setFilecontent(formattedXml.getBytes("UTF-8"));
                }
                catch (Exception e)
                {
                }
                finally
                {
                    writer.close();
                }

                exportType.setUrl("");
                reponse.getReturnInfos().setMessage("Export abgeschlossen. " + countExported + " Konzepte exportiert.");
            }
            else
            {
                reponse.getReturnInfos().setMessage("Keine Konzepte exportiert.");
                reponse.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP409);
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportSVS] Kein Export erstellt...");
                }
            }

            //Format-ID (SVS) setzen
            exportType.setFormatId(ExportValueSetContentRequestType.EXPIRT_SVS_ID);

            //ExportInfos in Response schreiben
            reponse.setExportInfos(exportType);
            //hb_session.getTransaction().commit();

        }
        catch (Exception ex)
        {
            if (parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId() == null)
            {
                s = "Fehler: VersionId muss angegeben werden!";
            }
            else
            {
                s = "Fehler: " + ex.getLocalizedMessage();
            }
            ex.printStackTrace();
        }

        return s;
    }

    private String formatOutput(Object o)
    {
        if (o == null)
        {
            return "";
        }

        if (o instanceof String)
        {
            return o.toString();
        }
        else if (o instanceof Date)
        {
            return sdf.format(o);
        }
        else if (o instanceof Integer)
        {
            return ((Integer) o).toString();
        }
        else if (o instanceof Boolean)
        {
            if (((Boolean) o).booleanValue())
            {
                return "1";
            }
            else
            {
                return "0";
            }
        }
        else
        {
            return o.toString();
        }
    }

    /**
     * @return the countExported
     */
    public int getCountExported()
    {
        return countExported;
    }
}
