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

import com.csvreader.CsvWriter;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;

import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.ReturnType.Status;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * 30.04.2013: Export erweitert
 *
 * @author Nico HÃ¤nsch, Robert M�tzner
 */
public class ExportCSV
{

    private static Logger logger = Logger4j.getInstance().getLogger();
    ExportCodeSystemContentRequestType parameter;
    private int countExported = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
    private HashMap<Integer, String> paramList = null;
    private org.hibernate.Session hb_session = null;
    boolean levelExists = false;
    ArrayList<Entry> entryList = new ArrayList<Entry>();

    public ExportCSV(ExportCodeSystemContentRequestType _parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ExportCSV gestartet ======");
        }

        parameter = _parameter;
    }

    public String exportCSV(ExportCodeSystemContentResponseType response)
    {
        String s = "";  // Status-Meldung
        //int count = countExported;

        CsvWriter csv;
        ExportType exportType = new ExportType();
        paramList = new HashMap<Integer, String>();
        hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //add UTF-8 Byte Order Mark (BOM)
        bos.write('\ufeef');
        bos.write('\ufebb');
        bos.write('\ufebf');
        try
        {
            //TODO URL erstellen und setzen (Namenskonvention?) 
            //TODO ggf. auf bereits identischen Export-File pr�fen
            //String csv_output_url = "/var/lib/tomcat6/webapps/csv_test_output.csv";
            //csv = new CsvWriter(new FileWriter(csv_output_url), ';');

            csv = new CsvWriter(bos, ';', Charset.forName("UTF-8")); // TODO Charset pr�fen
            csv.setTextQualifier('\'');
            csv.setForceQualifier(true);

            try
            {
                //Request-Parameter f�r ReturnCodeSystemDetails erstellen
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] Erstelle Request-Parameter f�r ReturnCodeSystemDetails");
                }

                ReturnCodeSystemDetailsRequestType requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
                requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
                if (requestCodeSystemDetails.getCodeSystem() != null && requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions() != null)
                {
                    requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
                }
                requestCodeSystemDetails.setLogin(parameter.getLogin());

                //CodeSystemDetails abrufen
                ReturnCodeSystemDetails rcsd = new ReturnCodeSystemDetails();
                ReturnCodeSystemDetailsResponseType responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails);
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] ReturnCodeSystemDetails abgerufen");
                }

                if (parameter.getExportInfos().isUpdateCheck())
                {
                    if (responseCodeSystemDetails.getReturnInfos().getStatus() == Status.OK
                            && responseCodeSystemDetails.getCodeSystem() != null)
                    {
                        if (!responseCodeSystemDetails.getCodeSystem().getCurrentVersionId().equals(((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId()))
                        {
                            ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setVersionId(responseCodeSystemDetails.getCodeSystem().getCurrentVersionId());

                            requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
                            requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
                            requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
                            requestCodeSystemDetails.setLogin(parameter.getLogin());

                            //CodeSystemDetails abrufen
                            rcsd = new ReturnCodeSystemDetails();
                            responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails);
                            if (logger.isInfoEnabled())
                            {
                                logger.info("[ExportCSV] ReturnCodeSystemDetails abgerufen");
                            }
                        }
                    }
                }

                //Request-Parameter f�r ListCodeSystemConcepts erstellen
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] Erstelle Request-Parameter f�r ListCodeSystemConcepts");
                }

                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] ListCodeSystemConcepts abgerufen");
                }

                String hql = "select distinct csv from CodeSystemVersion csv join csv.codeSystem cs"
                        + " where cs.id=:id and"
                        + " csv.versionId=:versionId";

                org.hibernate.Query q = hb_session.createQuery(hql);
                //Matthias: add query readonly
                q.setReadOnly(true);
                q.setLong("id", parameter.getCodeSystem().getId());
                q.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

                List<CodeSystemVersion> csvList = q.list();
                CodeSystemVersion csversion = null;
                if (csvList != null && csvList.size() == 1)
                {
                    csversion = csvList.get(0);
                }

                String hqlM = "select distinct mp from MetadataParameter mp join mp.codeSystem cs"
                        + " where cs.id=:id";

                org.hibernate.Query qM = hb_session.createQuery(hqlM);
                qM.setLong("id", parameter.getCodeSystem().getId());
                List<MetadataParameter> mlist = qM.list();

                //=================================================
                // CSV-Header erstellen und Dauerattribute auslesen
                //=================================================
                csv.write("code");
                csv.write("codeSystem");
                csv.write("displayName");
                csv.write("parentCode");
                csv.write("concept_Beschreibung");
                csv.write("meaning");
                csv.write("hints");
                int count = 0;
                for (MetadataParameter mp : mlist)
                {
                    String para = mp.getParamName();

                    //Matthias: not all parameters shall start with small letter (LOINC Parameters)
                    //String b = para.substring(0, 1);
                    //b = b.toLowerCase();
                    //para = b + para.substring(1);
                    if (para.equalsIgnoreCase("level"))
                    {
                        levelExists = true;
                        para = "level";
                    }
                    if (para.equalsIgnoreCase("type"))
                    {
                        para = "type";
                    }
                    if (para.equalsIgnoreCase("relationships"))
                    {
                        para = "relationships";
                    }

                    csv.write(para); //lowerCase
                    paramList.put(count, mp.getParamName());//Normal UpperCase
                    count++;

                }

                if (!levelExists)
                {
                    csv.write("level");
                }

                //ENDE Header erstellen
                csv.endRecord();

                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] CSV-Header erstellt.");
                }

                //CSV-Inhalt erstellen
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] Erstelle CSV-Inhalt mit Konzepten...");
                }

                int countRoot = 0;

                String hqlC = "select distinct cse from CodeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join cse.codeSystemEntityVersions csev join csev.codeSystemConcepts csc"
                        + " where csv.versionId=:versionId";

                if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
                {
                    // Datum f�r Synchronisation hinzuf�gen
                    hqlC += " and csev.statusDate>:dateFrom";
                }

                hqlC += " ORDER BY csc.code";

                org.hibernate.Query qC = hb_session.createQuery(hqlC);
                //Matthias: set readOnly
                qC.setReadOnly(true);
                qC.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

                if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
                {
                    // Datum f�r Synchronisation hinzuf�gen
                    qC.setDate("dateFrom", parameter.getExportParameter().getDateFrom());
                    logger.debug("Snych-Zeit: " + parameter.getExportParameter().getDateFrom().toString());
                }
                else
                {
                    logger.debug("keine Snych-Zeit angegeben");
                }

                List<CodeSystemEntity> cselist = qC.list();
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
                                CodeSystemVersionEntityMembership member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
                                writeEntry(csv, 0, csev, member, null, csversion);
                            }
                        }
                    }
                }

                /*
        for (CodeSystemEntity cse : cselist)//responseListCodeSystemConcepts.getCodeSystemEntity())
        {
          CodeSystemVersionEntityMembership member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
          boolean isAxis = false;
          boolean isMainClass = false;
          if (member.getIsAxis() != null)
            isAxis = member.getIsAxis().booleanValue();
          if (member.getIsMainClass() != null)
            isMainClass = member.getIsMainClass().booleanValue();

          if (isAxis || isMainClass)
          {
            countRoot++;

            for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              // Nur aktuellste Version exportieren
              if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
              {
                writeEntry(csv, 0, csev, member, null, csversion);

                // TODO Beziehungen ausgeben
                for (CodeSystemEntityVersionAssociation assChild : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
                {
                  if (assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                  {
                    long childId = assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
                    exportChild(csv, 1, childId, cselist, csev, csversion);
                  }
                }
              }
            }
          }
        }

        if (countRoot == 0)
        {
          // Flaches Vokabular, einfach alle EintrÃ¤ge exportieren
          for (CodeSystemEntity cse : cselist)
          {
            for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              // Nur aktuellste Version exportieren
              if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
              {
                writeEntry(csv, 0, csev, null, null, csversion);
              }
            }
          }
        }
                 */
 /*Iterator<CodeSystemEntity> it_CSE = responseListCodeSystemConcepts.getCodeSystemEntity().iterator();

         while (it_CSE.hasNext())
         {
         Iterator<CodeSystemEntityVersion> it_CSEV = it_CSE.next().getCodeSystemEntityVersions().iterator();

         while (it_CSEV.hasNext())
         {
         Iterator<CodeSystemConcept> it_CSC = it_CSEV.next().getCodeSystemConcepts().iterator();

         while (it_CSC.hasNext())
         {
         temp_CSC = it_CSC.next();

         //CSV-Zeile erstellen (CodeSystemConcept)

         //CodeSystem-Name (immer)                
         csv.write(csvEntryCodeSystem);
         //CodeSystem-Beschreibung (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemDescription);
         }
         //CodeSystem-Version (immer)
         csv.write(csvEntryCodeSystemVersion);
         //CodeSystem-Version Beschreibung (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemVersionDescription);
         }
         //CodeSystem-OID (immer)
         csv.write(csvEntryCodeSystemOid);
         //CodeSystemVersion-Ablaufdatum (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemExpirationDate);
         }


         //Code (immer)
         csv.write(temp_CSC.getCode());
         //Term (immer)
         csv.write(temp_CSC.getTerm());
         //isPreferred (immer)
         if (temp_CSC.getIsPreferred())
         {
         csv.write("Bevorzugt");
         }
         else
         {
         csv.write("Nicht bevorzugt");
         }


         //Übersetzungen (Parameter translations == true)
         if (parameter.getExportParameter().getTranslations())
         {
         //TODO Translations in CSV-Inhalt                 
         }

         //Übersetzungen (Parameter associationInfos == true)
         if (!parameter.getExportParameter().getAssociationInfos().isEmpty())
         {
         //TODO AssociationInfos CSV-Header                                
         }

         count++;
         csv.endRecord();


         } //END CodeSystemConcept
         } //END CodeSystemEntityVersion
         } //END CodeSystemEntity
                 */
                //ENDE CSV-Inhalt erstellen
                //Sort
                //Collections.sort(entryList, new AlphanumComparator());
                for (Entry e : entryList)
                {
                    writeCsvEntry(csv, e.level, e.csev, e.csevParent, e.csv);
                }

                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] CSV-Inhalt erstellt");
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error(ex.getMessage());
                s = "Fehler: " + ex.getLocalizedMessage();
            }

            //CSV-Datei schliessen
            csv.close();
            //if (logger.isInfoEnabled())
            //  logger.info("[ExportCSV] CSV-Datei geschrieben. Dateipfad: " + csv_output_url);

            if (countExported > 0)
            {
                //countExported = count;
                //CSV-Datei in Byte umwandeln

                /*File file = new File(csv_output_url);
         byte[] filecontent = new byte[(int) file.length()];
         FileInputStream fileInputStream = new FileInputStream(file);
         fileInputStream.read(filecontent);
         fileInputStream.close();
         if (logger.isInfoEnabled())
         logger.info("[ExportCSV] CSV-Datei in byte[] umgewandelt. (filecontent) ");*/
                //Filecontent setzen
                exportType.setFilecontent(bos.toByteArray());
                //Export-URL setzen
                //exportType.setUrl(csv_output_url);
                exportType.setUrl("");
                response.getReturnInfos().setMessage("Export abgeschlossen. " + countExported + " Konzepte exportiert.");

            }
            else
            {
                response.getReturnInfos().setMessage("Keine Konzepte exportiert.");
                response.getReturnInfos().setHttpStatus(ReturnType.HttpStatus.HTTP409);
                if (logger.isInfoEnabled())
                {
                    logger.info("[ExportCSV] Kein Export erstellt...");
                }
            }

            //Format-ID (CSV) setzen
            exportType.setFormatId(ExportCodeSystemContentRequestType.EXPORT_CSV_ID);

            //ExportInfos in Response schreiben
            response.setExportInfos(exportType);
        }
        catch (Exception ex)
        {
            s = "Fehler: " + ex.getLocalizedMessage();
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                bos.close();
            }
            catch (IOException ex)
            {
                java.util.logging.Logger.getLogger(ExportCSV.class.getName()).log(Level.SEVERE, null, ex);
            }
            hb_session.close();
        }

        //hb_session.getTransaction().commit();
        return s;
    }

    private void exportChild(CsvWriter csv, int level, long childEntityVersionId,
            List<CodeSystemEntity> entityList, CodeSystemEntityVersion parent, CodeSystemVersion csversion) throws Exception
    {
        for (CodeSystemEntity cse : entityList)
        {
            if (cse.getCurrentVersionId().longValue() == childEntityVersionId)
            {
                for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
                {
                    // Nur aktuellste Version exportieren
                    if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
                    {
                        CodeSystemVersionEntityMembership member = null;
                        if (cse.getCodeSystemVersionEntityMemberships() != null
                                && cse.getCodeSystemVersionEntityMemberships().size() > 0)
                        {
                            member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
                        }

                        writeEntry(csv, level, csev, member, parent, csversion);

                        // Beziehungen darunter ausgeben
                        for (CodeSystemEntityVersionAssociation assChild : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
                        {
                            if (assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                            {
                                long childId = assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
                                exportChild(csv, level + 1, childId, entityList, csev, csversion);
                            }
                        }
                    }
                }

                break;
            }
        }
    }

    private void writeEntry(CsvWriter csv, int level, CodeSystemEntityVersion csev,
            CodeSystemVersionEntityMembership member, CodeSystemEntityVersion csevParent, CodeSystemVersion csversion)
    {
        Entry e = new Entry();
        e.setCsev(csev);
        e.setCsevParent(csevParent);
        e.setLevel(level);
        e.setCsv(csversion);

        entryList.add(e);
    }

    private void writeCsvEntry(CsvWriter csv, int level, CodeSystemEntityVersion csev, CodeSystemEntityVersion csevParent, CodeSystemVersion csversion) throws Exception
    {

        CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();
        if (csev.getStatus() == 1)
        {
            logger.debug("Schreibe Code: " + csc.getCode());

            csv.write(checkFormat(csc.getCode()));
            //Matthias check for null
            if (csversion.getOid() != null)
            {
                csv.write(checkFormat(csversion.getOid()));
            }
            else
            {
                csv.write("");
            }

            csv.write(checkFormat(csc.getTerm()));
            if (csevParent != null && csevParent.getCodeSystemConcepts() != null && !csevParent.getCodeSystemConcepts().isEmpty())
            {
                csv.write(checkFormat(csevParent.getCodeSystemConcepts().iterator().next().getCode()));
            }
            else
            {
                csv.write("");
            }
            //Matthias check for null for the following data
            if (csc.getDescription() != null)
            {
                csv.write(checkFormat(csc.getDescription()));
            }
            else
            {
                csv.write("");
            }
            if (csc.getMeaning() != null)
            {
                csv.write(checkFormat(csc.getMeaning()));
            }
            else
            {
                csv.write("");
            }
            if (csc.getHints() != null)
            {
                csv.write(checkFormat(csc.getHints()));
            }
            else
            {
                csv.write("");
            }

            //Get vsmv for csev/cvsm
            /*String hqlM = "select distinct csmv from CodeSystemMetadataValue csmv join csmv.metadataParameter mp join csmv.codeSystemEntityVersion csev"
       + " where csev.versionId=:versionId";

       org.hibernate.Query qM = hb_session.createQuery(hqlM);
       qM.setLong("versionId", csev.getVersionId());
       List<CodeSystemMetadataValue> csmvList = qM.list();*/
            Set<CodeSystemMetadataValue> csmvList = csev.getCodeSystemMetadataValues();
            for (int i = 0; i < paramList.size(); i++)
            {

                CodeSystemMetadataValue csmv = null;
                for (CodeSystemMetadataValue csmvL : csmvList)
                {

                    if (csmvL.getMetadataParameter().getParamName().equals(paramList.get(i)))
                    {
                        csmv = csmvL;
                        break;
                    }
                }

                if (csmv != null && csmv.getParameterValue() != null)
                {
                    csv.write(checkFormat(csmv.getParameterValue()));
                }
                else
                {
                    csv.write("");
                }
            }

            if (!levelExists)
            {
                csv.write(checkFormat(formatOutput(level)));
            }

            /*
       if (member != null)
       {
       if (member.getIsAxis() != null && member.getIsAxis().booleanValue())
       csv.write("1");
       else
       csv.write("0");

       if (member.getIsMainClass() != null && member.getIsMainClass().booleanValue())
       csv.write("1");
       else
       csv.write("0");
       }
       else
       {
       csv.write("0");
       csv.write("0");
       }

       if (csevParent == null)
       {
       csv.write("");
       }
       else
       {
       // Parent-ID angeben
       csv.write(formatOutput(csevParent.getVersionId()));
       }
             */
            csv.endRecord();
            countExported++;
        }
    }

    private String checkFormat(String input)
    {
        return input.replace(';', ',');
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
