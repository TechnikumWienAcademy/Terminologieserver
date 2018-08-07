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

import com.csvreader.CsvReader;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.VersioningType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Philipp Urbauer
 */
public class ImportKAL
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;
  private CodeSystemVersion csVersion;
  
  private boolean onlyCSV = true; //Only CSV for this case
  private Long csId = 0L;
  private Long csvId = 0L;
  private String resultStr = "";

  public ImportKAL(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportKAL gestartet ======");

    parameter = _parameter;
  }

  public String importKAL(ImportCodeSystemResponseType reponse)
  {
    String s = "";
    Date date = new Date();
    int count = 0, countFehler = 0;
    CsvReader csv;
    
    try{
       
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(parameter.getImportInfos().getFilecontent()); 
      csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
      csv.setDelimiter(';');
      csv.setTextQualifier('"');
      csv.setUseTextQualifier(true);

      csv.readHeaders();      
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      //Build CS, CSV, Chapters, Supchapters and linking
      // Hibernate-Block, Session �ffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createCodeSystem(hb_session) == false)
        {
          // Fehlermeldung
          hb_session.getTransaction().commit();
          hb_session.close();
          return "Code System konnte nicht erstellt werden!";
        }
        
        csId = parameter.getCodeSystem().getId();
        csvId = parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId();
        
        // MetadatenParameter speichern => ELGA Specific Level/Type 
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (int i=0;i<5;i++)
        {
            
          String mdText ="";
          MetadataParameter mp = null;
          if(i==0){
            mdText = "Leistungseinheit";
          }
          if(i==1){
            mdText = "Langtext";
          }
          if(i==2){
            mdText = "Nicht Inhalt";
          }
          if(i==3){
            mdText = "Codierhinweis";
          }
          if(i==4){
            mdText = csv.getHeader(csv.getHeaderCount()-1);
          }
          
          //Check if parameter already set in case of new Version!
          String hql = "select distinct mp from MetadataParameter mp";
          hql += " join fetch mp.codeSystem cs";

          HQLParameterHelper parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("mp.", "paramName", mdText);

          // Parameter hinzuf�gen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");
          logger.debug("HQL: " + hql);

          // Query erstellen
          org.hibernate.Query q = hb_session.createQuery(hql);
          // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
          parameterHelper.applyParameter(q);

          List<MetadataParameter> mpList= q.list(); 
          for(MetadataParameter mParameter:mpList){

              if(mParameter.getCodeSystem().getId().equals(parameter.getCodeSystem().getId()))
                  mp = mParameter;
          }
          
          if(mp == null){
            
            mp = new MetadataParameter();
            mp.setParamName(mdText);
            mp.setCodeSystem(parameter.getCodeSystem());
            hb_session.save(mp);
          } 
          
          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit Codesystem-ID: " + mp.getCodeSystem().getId() + ", MD-ID: " + mp.getId());
        }
        
        //Adding Version Information
        Iterator<CodeSystemVersion> iter = parameter.getCodeSystem().getCodeSystemVersions().iterator();
        
        while(iter.hasNext()){
        
            csVersion = (CodeSystemVersion)iter.next();
            csVersion.setInsertTimestamp(date);
            csVersion.setOid("");
            csVersion.setReleaseDate(date);
            csVersion.setStatus(1);
            csVersion.setStatusDate(date);
            csVersion.setValidityRange(236l); //Default Value for empfohlen
        }
        //VersionUpdate mit UpdateCodeSystemVersion am Ende der Methode

        //Add catalog
        while (csv.readRecord())
        {
          
          CreateConceptRequestType request = new CreateConceptRequestType();

          request.setLogin(parameter.getLogin());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(csv.get("Code"));
          csc.setIsPreferred(true);
          csc.setTerm(csv.get("Kurztext"));
          csc.setTermAbbrevation("");
          csc.setDescription(csv.get("Beschreibung"));

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
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
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
            csev.setStatus(1);
            csev.setIsLeaf(true);
            csev.setEffectiveDate(date);

            // Entity-Version dem Request hinzuf�gen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einf�gen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session);

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
                count++;
                
                // MetadatenValues einf�gen
                for(int i=0;i<headerMetadataIDs.size();i++){
                
                    String metadataValue ="";
                    
                    if(i == 0)
                        metadataValue = csv.get("LE");
                    if(i == 1)
                        metadataValue = csv.get("Langtext");
                    if(i == 2)
                        metadataValue = csv.get("Nicht Inhalt");
                    if(i == 3)
                        metadataValue = csv.get("Codierhinweis");
                    if(i == 4)
                        metadataValue = csv.get(csv.getHeader(csv.getHeaderCount()-1));

                    //Check if parameter already set in case of new Version!
                    String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                    hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    
                    if(i == 0)
                        parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Leistungseinheit"));
                    if(i == 1)
                        parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Langtext"));
                    if(i == 2)
                        parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Nicht Inhalt"));
                    if(i == 3)
                        parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Codierhinweis"));
                    if(i == 4)
                        parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get(csv.getHeader(csv.getHeaderCount()-1)));
                   
                    parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

                    // Parameter hinzuf�gen (immer mit AND verbunden)
                    hql += parameterHelper.getWhere("");
                    logger.debug("HQL: " + hql);

                    // Query erstellen
                    org.hibernate.Query q = hb_session.createQuery(hql);
                    // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
                    parameterHelper.applyParameter(q);

                    List<CodeSystemMetadataValue> valueList= q.list(); 

                    if(valueList.size() == 1){
                        valueList.get(0).setParameterValue(metadataValue);
                    }

                    logger.debug("Metadaten einf�gen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                    hb_session.update(valueList.get(0));
                }
            }
            else
              countFehler++;

          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }

        }
        if (count == 0)
        {
          hb_session.getTransaction().rollback();
          
          resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
          
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          hb_session.getTransaction().commit(); 
          countImported = count;
          reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler beim Import einer KAL.CSV-Datei: " + ex.getLocalizedMessage();

        try
        {
            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
          
          resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
          
          logger.info("[ImportKAL.java] Rollback durchgef�hrt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportKAL.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schlie�en
        hb_session.close();
      }
    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim Import: " + ex.getLocalizedMessage();
      logger.error(s);
      //ex.printStackTrace();
    }
    
    try {
        
        if(resultStr.equals(""))
            updateCodeSystemVersion();

    } catch (Exception ex) {
        s = "Fehler beim Import: " + ex.getLocalizedMessage();
        logger.error(s);
    }
    
    return s;
  }
  
  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    // TODO zun�chst pr�fen, ob CodeSystem bereits existiert
    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
    request.setCodeSystem(parameter.getCodeSystem());
    request.setLogin(parameter.getLogin());

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session);

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      return false;
    }
    parameter.setCodeSystem(resp.getCodeSystem());
    
    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return true;
  }
  
  private void updateCodeSystemVersion() throws Exception{
  
      //CodeSystemVersion Update
    MaintainCodeSystemVersionRequestType updateParam = new MaintainCodeSystemVersionRequestType();
    VersioningType versioningType = new VersioningType();
    versioningType.setCreateNewVersion(Boolean.FALSE);

    updateParam.setLogin(parameter.getLogin());
    updateParam.setCodeSystem(parameter.getCodeSystem());
    updateParam.setVersioning(versioningType);

    MaintainCodeSystemVersion mcv = new MaintainCodeSystemVersion();
    MaintainCodeSystemVersionResponseType updateResponse = mcv.MaintainCodeSystemVersion(updateParam);

    if(updateResponse.getReturnInfos().getStatus() != ReturnType.Status.OK){

        throw new Exception("ImportKAL: CodeSystemVersion Update konnte nicht durchgef�hrt werden: " + updateResponse.getReturnInfos().getMessage());
    }
  }
  
  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
}
