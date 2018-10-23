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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.EscCharCheckQuot;
import de.fhdo.terminologie.helper.ValidityRangeHelper;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
//import org.apache.commons.collections.IteratorUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Philipp Urbauer
 */
public class ExportCodeSystemSVS
{

    private static Logger logger = Logger4j.getInstance().getLogger();
    ExportCodeSystemContentRequestType parameter;
    private int countExported = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");

    public ExportCodeSystemSVS(ExportCodeSystemContentRequestType _parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ExportSVS gestartet ======");
        }

        parameter = _parameter;
    }

    public String exportSVS(ExportCodeSystemContentResponseType reponse)
    {
        String s = "";  // Status-Meldung
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ExportType exportType = new ExportType();
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        try
        {

            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlsw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(baos));

            // Hibernate-Block, Session öffnen
            //hb_session.getTransaction().begin();
            try
            {

                if (parameter.getExportInfos().isUpdateCheck())
                {
                    //Request-Parameter für ReturnValueSetDetails erstellen
                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVs] Erstelle Request-Parameter für ReturnCodeSystemDetails");
                    }

                    ReturnCodeSystemDetailsRequestType requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
                    requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
                    requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
                    requestCodeSystemDetails.setLogin(parameter.getLogin());

                    //ValueSetDetails abrufen
                    ReturnCodeSystemDetails rcsd = new ReturnCodeSystemDetails();
                    
                    ReturnCodeSystemDetailsResponseType responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails);
                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] ReturnCodeSystemDetails abgerufen");
                    }

                    if (responseCodeSystemDetails.getReturnInfos().getStatus() == ReturnType.Status.OK
                            && responseCodeSystemDetails.getCodeSystem() != null)
                    {
                        if (!responseCodeSystemDetails.getCodeSystem().getCurrentVersionId().equals(((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId()))
                        {
                            ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setVersionId(responseCodeSystemDetails.getCodeSystem().getCurrentVersionId());
                        }
                    }
                }

                String hql = "select distinct csv from CodeSystemVersion csv join csv.codeSystem cs"
                        + " where cs.id=:id and"
                        + " csv.versionId=:versionId";

                org.hibernate.Query q = hb_session.createQuery(hql);
                //Matthias: set read only
                q.setReadOnly(true);
                q.setLong("id", parameter.getCodeSystem().getId());
                q.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

                List<CodeSystemVersion> csvList = q.list();
                CodeSystemVersion csv = null;
                if (csvList != null && csvList.size() == 1)
                {
                    csv = csvList.get(0);
                }

                if (csv != null)
                {
                    //Erstellung Hauptknoten

                    xmlsw.writeStartElement("valueSet");
                    if (csv.getCodeSystem().getName() != null)
                    {
                        xmlsw.writeAttribute("name", EscCharCheckQuot.check(csv.getCodeSystem().getName()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("name", "");
                    }
                    if (csv.getCodeSystem().getName() != null)
                    {
                        xmlsw.writeAttribute("displayName", EscCharCheckQuot.check(csv.getCodeSystem().getName()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("displayName", "");
                    }
                    if (csv.getReleaseDate() != null)
                    {
                        xmlsw.writeAttribute("effectiveDate", EscCharCheckQuot.check(sdf.format(csv.getReleaseDate())));
                    }
                    else
                    {
                        xmlsw.writeAttribute("effectiveDate", "");
                    }
                    if (csv.getOid() != null)
                    {
                        xmlsw.writeAttribute("id", EscCharCheckQuot.check(csv.getOid()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("id", "");
                    }
                    if (csv.getStatus() != null)
                    {
                        xmlsw.writeAttribute("statusCode", csv.getStatus() + "");
                    }
                    else
                    {
                        xmlsw.writeAttribute("statusCode", "");
                    }
                    if (csv.getCodeSystem().getWebsite() != null)
                    {
                        xmlsw.writeAttribute("website", EscCharCheckQuot.check(csv.getCodeSystem().getWebsite()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("website", "");
                    }
                    if (csv.getName() != null)
                    {
                        xmlsw.writeAttribute("version", EscCharCheckQuot.check(csv.getName()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("version", "");
                    }

                    if (csv.getCodeSystem().getDescription() != null)
                    {
                        xmlsw.writeAttribute("beschreibung", EscCharCheckQuot.check(csv.getCodeSystem().getDescription()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("beschreibung", "");
                    }

                    //Matthias: adding version description
                    if (csv.getDescription() != null)
                    {
                        xmlsw.writeAttribute("version-beschreibung", EscCharCheckQuot.check(csv.getDescription()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("version-beschreibung", "");
                    }

                    if (csv.getCodeSystem().getDescriptionEng() != null)
                    {
                        xmlsw.writeAttribute("description", EscCharCheckQuot.check(csv.getCodeSystem().getDescriptionEng()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("description", "");
                    }

                    //Matthias: added incompleteCS and responsible Organization
                    if (csv.getCodeSystem().getIncompleteCS() != null)
                    {
                        if (csv.getCodeSystem().getIncompleteCS())
                        {
                            xmlsw.writeAttribute("unvollstaendig", "true");
                        }
                        else
                        {
                            xmlsw.writeAttribute("unvollstaendig", "false");
                        }
                    }

                    if (csv.getCodeSystem().getResponsibleOrganization() != null)
                    {
                        xmlsw.writeAttribute("verantw_Org", EscCharCheckQuot.check(csv.getCodeSystem().getResponsibleOrganization()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("verantw_Org", "");
                    }

                    //Matthias: validityRange added
                    if (csv.getValidityRange() != null)
                    {
                        xmlsw.writeAttribute("gueltigkeitsbereich", ValidityRangeHelper.getValidityRangeNameById(csv.getValidityRange()));
                    }
                    else
                    {
                        xmlsw.writeAttribute("gueltigkeitsbereich", "");
                    }

                    //Matthias: added additional dates
                    if (csv.getStatusDate() != null)
                    {
                        xmlsw.writeAttribute("status_date", EscCharCheckQuot.check(sdf.format(csv.getStatusDate())));
                    }
                    else
                    {
                        xmlsw.writeAttribute("status_date", "");
                    }

                    if (csv.getLastChangeDate() != null)
                    {
                        xmlsw.writeAttribute("last_change_date", EscCharCheckQuot.check(sdf.format(csv.getLastChangeDate())));
                    }
                    else
                    {
                        xmlsw.writeAttribute("last_change_date", "");
                    }

                    String hqlC = "select distinct cse from CodeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join cse.codeSystemEntityVersions csev join csev.codeSystemConcepts csc"
                            + " where csv.versionId=:versionId ORDER BY csc.code";

                    org.hibernate.Query qC = hb_session.createQuery(hqlC);
                    //Matthias: set readOnly
                    qC.setReadOnly(true);
                    qC.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

                    List<CodeSystemEntity> cselist = qC.list();

                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] ListValueSetContents abgerufen");
                    }

                    //SVS-Inhalt erstellen
                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] Erstelle SVS-Inhalt mit Konzepten...");
                    }

                    xmlsw.writeStartElement("conceptList");

                    // Flaches Vokabular, einfach alle Einträge exportieren
                    for (CodeSystemEntity cse : cselist)
                    {
                        for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
                        {
                            // Nur aktuellste Version exportieren
                            if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
                            {

                                CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();
                                if (csev.getStatus() == 1)
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Schreibe Code: " + csc.getCode());
                                    }

                                    xmlsw.writeStartElement("concept");
                                    if (csc.getCode() != null)
                                    {
                                        //code to prevent characters to be escaped
//                                        xmlsw.flush();
//                                        String code = " code='"+EscCharCheckQuot.check(csc.getCode())+"'";
//                                        baos.write(code.getBytes());
//                                        baos.flush();
                                        if(csc.getCode().equals(">"))
                                        {
                                            xmlsw.flush();
                                            String code = " code=\"&gt;\"";
                                            baos.write(code.getBytes());
                                            baos.flush();
                                            //xmlsw.writeAttribute("code", code);
                                        }
                                        else
                                        {
                                            xmlsw.writeAttribute("code", EscCharCheckQuot.check(csc.getCode()));
                                        }
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("code", "");
                                    }
                                    if (csc.getTerm() != null)
                                    {
                                        xmlsw.writeAttribute("displayName", EscCharCheckQuot.check(csc.getTerm()));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("displayName", "");
                                    }
                                    if (csc.getDescription() != null)
                                    {
                                        xmlsw.writeAttribute("concept_beschreibung", EscCharCheckQuot.check(csc.getDescription()));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("concept_beschreibung", "");
                                    }
                                    if (csc.getMeaning() != null)
                                    {
                                        xmlsw.writeAttribute("deutsch", EscCharCheckQuot.check(csc.getMeaning()));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("deutsch", "");
                                    }
                                    if (csc.getHints() != null)
                                    {
                                        xmlsw.writeAttribute("hinweise", EscCharCheckQuot.check(csc.getHints()));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("hinweise", "");
                                    }
                                    if (csv.getOid() != null)
                                    {
                                        xmlsw.writeAttribute("codeSystem", EscCharCheckQuot.check(csv.getOid()));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("codeSystem", "");
                                    }
                                    //Matthias: added concept status and concept status changed
                                    if (csc.getCodeSystemEntityVersion().getStatus() != null)
                                    {
                                        xmlsw.writeAttribute("conceptStatus", csc.getCodeSystemEntityVersion().getStatus() + "");
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("conceptStatus", "");
                                    }

                                    if (csc.getCodeSystemEntityVersion().getStatusDate() != null)
                                    {
                                        xmlsw.writeAttribute("conceptStatusDate", EscCharCheckQuot.check(sdf.format(csc.getCodeSystemEntityVersion().getStatusDate())));
                                    }
                                    else
                                    {
                                        xmlsw.writeAttribute("conceptStatusDate", "");
                                    }

                                    Iterator<CodeSystemMetadataValue> it = csev.getCodeSystemMetadataValues().iterator();
                                    /*List<CodeSystemMetadataValue> myList = IteratorUtils.toList(it);
                        boolean output = true;
                        
                        for(CodeSystemMetadataValue v:myList){
                        
                            for(CodeSystemMetadataValue vInt:myList){
                                
                                if(!v.equals(vInt) && (v.getMetadataParameter().getParamName().equals(vInt.getMetadataParameter().getParamName()))
                                   && (v.getParameterValue().equals("") && !vInt.getParameterValue().equals(""))){
                                    output = false;
                                }
                            }
                            
                            if(output)
                                xmlsw.writeAttribute(EscCharCheckQuot.checkAttribute(v.getMetadataParameter().getParamName().toLowerCase()), v.getParameterValue());
                        }*/

                                    while (it.hasNext())
                                    {

                                        CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) it.next();
                                        if (csmv.getParameterValue() != null)
                                        {
                                            xmlsw.writeAttribute(EscCharCheckQuot.checkAttribute(csmv.getMetadataParameter().getParamName().toLowerCase()), EscCharCheckQuot.check(csmv.getParameterValue()));
                                        }
                                        else
                                        {
                                            xmlsw.writeAttribute(EscCharCheckQuot.checkAttribute(csmv.getMetadataParameter().getParamName().toLowerCase()), EscCharCheckQuot.check(csmv.getParameterValue()));
                                        }
                                    }

                                    xmlsw.writeEndElement();
                                    countExported++;
                                    //System.out.println("\n" + countExported);
                                }
                            }
                        }
                    }
                    xmlsw.writeEndElement();
                    xmlsw.writeEndElement();
                    xmlsw.flush();
                    //xmlsw.close();

                    if (logger.isInfoEnabled())
                    {
                        logger.info("[ExportSVS] SVS-Inhalt erstellt");
                    }
                }
                else
                {
                    throw new Exception("ExportSVS: CSV is NULL");
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error(ex.getMessage());
                s = "Fehler: " + ex.getLocalizedMessage();
            }
            finally
            {
                xmlsw.close();

            }

            if (countExported > 0)
            {

                exportType.setFilecontent(baos.toByteArray());
                baos.close();

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
            exportType.setFormatId(ExportCodeSystemContentRequestType.EXPORT_SVS_ID);

            //ExportInfos in Response schreiben
            reponse.setExportInfos(exportType);
            //hb_session.getTransaction().commit();

        }
        catch (Exception ex)
        {
            s = "Fehler: " + ex.getLocalizedMessage();
            ex.printStackTrace();
        }
        finally
        {
            hb_session.close();
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
