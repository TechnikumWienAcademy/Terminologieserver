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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.DateComparator;
import de.fhdo.terminologie.helper.DeleteTermHelper;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.ImportStatus;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.CreateValueSet;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContent;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 * Reworked Philipp Urbauer
 */
public class ImportVSCSV
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportValueSetRequestType parameter;
  private Boolean orderCVSM = false;
  private Map<String, MetadataParameter> metadataParameterMap;
  private ArrayList<ArrayList<String>> csvList = new ArrayList<ArrayList<String>>();
  
  private boolean onlyVSV = true;
  private Long vsId = 0L;
  private Long vsvId = 0L;
  private String resultStr = "";
  private ImportStatus _status;
  private long id;

  public ImportVSCSV(ImportValueSetRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportVS gestartet ======");

    parameter = _parameter;
    
    _status = new ImportStatus();
    _status.importTotal = 0;
    _status.importCount = 0;
    _status.importRunning = true;
    _status.exportRunning = false;
    _status.cancel = false;
  }

  public void importCSV(ImportValueSetResponseType ws_response)
  {
      if(parameter.getImportId() != null)
        {
            logger.info("ImportID: "+parameter.getImportId());
            StaticStatusList.addStatus(parameter.getImportId(), _status);
        }
        else
        {
            logger.info("ImportID: "+ 0);
            StaticStatusList.addStatus(0L, _status);
        }
      
    String s = "";

    String[] str = parameter.getImportInfos().getRole().split(":");
    if(str[1].equals("true")){
        onlyVSV = true;
        parameter.getImportInfos().setRole(str[0]);
    }else{
        onlyVSV = false;
        parameter.getImportInfos().setRole(str[0]);
    }
    
    CsvReader csv;
    try
    {
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);
      orderCVSM = parameter.getImportInfos().getOrder();
      
      //charset has to be UTF-8 because auf character &permil; -> cannot be encoded in ISO-8859-1
      csv = new CsvReader(is, Charset.forName("UTF-8"));
      
      //-1 because of column headers
        int numberOfLines = -1;
      
        while(csv.readRecord())
        {
            numberOfLines++;
        }
      
        id = 0;
        if(parameter.getImportId() != null)
        {
            id = parameter.getImportId();
        }

        
        StaticStatusList.getStatus(id).importTotal = numberOfLines;
        is.close();
        csv.close();
        
        is = new ByteArrayInputStream(bytes);
        csv = new CsvReader(is, Charset.forName("UTF-8"));
        
        csv.setDelimiter(';');
        csv.setTextQualifier('\'');
        csv.setUseTextQualifier(true);


      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createValueSet(hb_session) == false)
        {
          // Fehlermeldung
          hb_session.getTransaction().rollback();
          hb_session.close();
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setMessage("ValueSet konnte nicht erstellt werden!");
          return ;
        }
        
        // MetadatenParameter speichern => ELGA Specific Level/Type 
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        int startIndex = 7;
        int countMp = csv.getHeaderCount() - startIndex;
        for(int i=0;i<countMp;i++){
  
          String mdText ="";
          MetadataParameter mp = null;
          mdText = firstCharUpperCase(replaceApo(csv.getHeader(startIndex + i)));
          
          //Check if parameter already set in case of new Version!
          String hql = "select distinct mp from MetadataParameter mp";
          hql += " join fetch mp.valueSet vs";

          HQLParameterHelper parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("mp.", "paramName", mdText);

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");
          logger.debug("HQL: " + hql);

          // Query erstellen
          org.hibernate.Query q = hb_session.createQuery(hql);
          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q);

          List<MetadataParameter> mpList= q.list(); 
          for(MetadataParameter mParameter:mpList){

              if(mParameter.getValueSet().getId().equals(parameter.getValueSet().getId()))
                  mp = mParameter;
          }
          
          if(mp == null){
            
            mp = new MetadataParameter();
            mp.setParamName(mdText);
            mp.setValueSet(parameter.getValueSet());
            hb_session.save(mp);
          } 
          
          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit ValueSet-ID: " + mp.getValueSet().getId() + ", MD-ID: " + mp.getId());
        }
        
        
        CreateValueSetContentRequestType request = new CreateValueSetContentRequestType();
        request.setLogin(parameter.getLogin());
        
        //Nur letzte Version
        ValueSet vs = parameter.getValueSet();
        
        for(ValueSetVersion vsv:parameter.getValueSet().getValueSetVersions()){
        
            if(vsv.getVersionId().equals(vs.getCurrentVersionId())){
            
                vs.getValueSetVersions().clear();
                vs.getValueSetVersions().add(vsv);
                break;
            }
        }
        parameter.setValueSet(vs);
        
        vsId = vs.getId();
        vsvId = vs.getValueSetVersions().iterator().next().getVersionId();
        
        request.setValueSet(parameter.getValueSet());
        request.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
        Long orderCounter = 1l;
                
        while (csv.readRecord())
        { 
            id = 0;
            if(parameter.getImportId() != null)
            {
                id = parameter.getImportId();
            }

            if (StaticStatusList.getStatus(id).cancel)
              break;

            
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
            cvsm.setAwbeschreibung(replaceApo(csv.get("concept_beschreibung")));
            cvsm.setBedeutung(replaceApo(csv.get("meaning")));
            cvsm.setHinweise(replaceApo(csv.get("hints")));
            
            if(orderCVSM){
                cvsm.setOrderNr(orderCounter);
                ++orderCounter;
            }else{
                cvsm.setOrderNr(0l);
            }
            
            // Version-ID muss anhand des Codes bestimmt werden
            String code = replaceApo(csv.get(0));
            //Alte Version, falls Fehler auftreten mit csv.get(0)
            //String code = replaceApo(csv.get("code"));
            
            logger.debug("Entity zu Code '" + code + "' wird gesucht...");
            String oid = replaceApo(csv.get("codeSystem"));
            String hqlV = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                    + " where csc.code=:code and"
                    + " csv.oid=:oid";
            
            org.hibernate.Query qV = hb_session.createQuery(hqlV);
            
            
            qV.setString("code", code);
            qV.setString("oid", oid);
            
            List<CodeSystemEntityVersion> csevList = qV.list();
            if (csevList != null && csevList.size() >= 1)
            {
                //Bei CS wo es keine Versionierung gibt e.g. LOINC => da gibt es 
                if(csevList.size() > 1){
                    Collections.sort(csevList,new DateComparator());
                }
                
                // Version-ID gefunden, nun übergeben
                logger.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());
                csev.setVersionId(csevList.get(0).getVersionId());
            }
            else
            {
              logger.debug("Entity zu Code '" + code + "' nicht gefunden!");
              throw new Exception("Entity zu Code '" + code + "' nicht gefunden!");
            }

            csev.getConceptValueSetMemberships().clear();
            csev.getConceptValueSetMemberships().add(cvsm);
            // Konzept hinzufügen
            CodeSystemEntity cse = new CodeSystemEntity();
            cse.getCodeSystemEntityVersions().add(csev);
            request.getCodeSystemEntity().add(cse);
            
            ArrayList<String> list = new ArrayList<String>();
            for(int i=0;i<csv.getHeaderCount();i++){
                
                list.add(replaceApo(csv.get(i)));
            }
            csvList.add(list);
        }

//        hb_session.getTransaction().commit();
        
        CreateValueSetContent createValueSetContent = new CreateValueSetContent();
        CreateValueSetContentResponseType response = createValueSetContent.CreateValueSetContent(request, hb_session);

       //Hier erst daten für ValueSetMetadataValue einfügen!!!
        
        if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
            int counter = 0;
            Iterator<ArrayList<String>> it = csvList.iterator();
            while (it.hasNext())
            {
                id = 0;
                if(parameter.getImportId() != null)
                {
                    id = parameter.getImportId();
                }

                if (StaticStatusList.getStatus(id).cancel)
                  break;

                counter++;
                StaticStatusList.getStatus(id).importCount = counter;
                ArrayList<String> csvEntry = (ArrayList<String>)it.next();
                
                // Version-ID muss anhand des Codes bestimmt werden
                String code = csvEntry.get(0);

                logger.debug("Entity zu Code '" + code + "' wird gesucht...");
                String oid = csvEntry.get(1);

                String hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                        + " where csc.code=:code and"
                        + " csv.oid=:oid";

                org.hibernate.Query q = hb_session.createQuery(hql);
                q.setString("code", code);
                q.setString("oid", oid);

                List<CodeSystemEntityVersion> csevList = q.list();
                if (csevList != null && csevList.size() >= 1)
                {
                    
                    if(csevList.size() > 1){
                        Collections.sort(csevList,new DateComparator());
                    }
                    
                    // Version-ID gefunden, nun übergeben
                    logger.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());

                    for(int i = 0;i < countMp;i++){
                    
                        // Metadaten einfügen
                        String mdValue = csvEntry.get(startIndex + i);//Achtung in Maps lowerCase
                        String mdParam = firstCharUpperCase(replaceApo(csv.getHeader(startIndex + i)));
                        if (mdValue != null && mdValue.length() > 0)
                        {
                            //Check if parameter already set in case of new Version!
                            String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                            hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                            HQLParameterHelper parameterHelper = new HQLParameterHelper();
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get(mdParam));
                            parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                            parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                            // Parameter hinzufügen (immer mit AND verbunden)
                            hql2 += parameterHelper.getWhere("");
                            logger.debug("HQL: " + hql2);

                            // Query erstellen
                            org.hibernate.Query q1 = hb_session.createQuery(hql2);
                            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                            parameterHelper.applyParameter(q1);

                            List<ValueSetMetadataValue> valueList= q1.list(); 

                            if(valueList.size() == 1){
                                valueList.get(0).setParameterValue(mdValue);
                            }

                            //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + 
                            //        ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                            hb_session.update(valueList.get(0));
                        }
                    }
                }
                else
                {
                  logger.debug("Entity zu Code '" + code + "' nicht gefunden!");
                  throw new Exception("Entity zu Code '" + code + "' nicht gefunden!");
                }
            }
          
            hb_session.getTransaction().commit();
            ws_response.getReturnInfos().setCount(response.getReturnInfos().getCount());
            ws_response.getReturnInfos().setMessage("Import abgeschlossen. " + response.getReturnInfos().getCount() + " Konzept(e) dem Value Set hinzugefügt.\n" + 
                  response.getReturnInfos().getMessage());
            ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            ws_response.getReturnInfos().setStatus(ReturnType.Status.OK);
            ws_response.setValueSet(parameter.getValueSet());
        }
        else
        {
            hb_session.getTransaction().rollback();
            
            resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);
            
            ws_response.getReturnInfos().setMessage("Fehler beim Importieren von Value Set-Inhalten: " + response.getReturnInfos().getMessage());
            ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
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
                logger.info("[ImportCSV.java] Rollback durchgeführt!");
            }
          resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);
          
          ws_response.getReturnInfos().setMessage("Fehler beim Import eines Value Sets: " + ex.getLocalizedMessage());
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        catch (Exception exRollback)
        {
            if(!hb_session.getTransaction().wasRolledBack()){
                logger.info(exRollback.getMessage());
                logger.info("[ImportCSV.java] Rollback fehlgeschlagen!");
                ws_response.getReturnInfos().setMessage("Rollback fehlgeschlagen! Fehler beim Import eines Value Sets: " + exRollback.getLocalizedMessage());
                ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
        }
      }
      finally
      {
        // Session schließen
          csv.close();
        hb_session.close();
      }


    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportValueSet.class.getName()).log(Level.SEVERE, null, ex);
      ws_response.getReturnInfos().setMessage("Fehler beim Importieren von Value Set-Inhalten: " + ex.getLocalizedMessage());
      ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      
      logger.error(s);
      //ex.printStackTrace();
    }
  }

  private boolean createValueSet(org.hibernate.Session hb_session)
  {
    long vs_id = 0, vsv_id = 0;
    
    logger.debug("createValueSet...");
    
    // vorhandenes Value Set nutzen oder neues anlegen?
    if (parameter.getValueSet().getId() != null && parameter.getValueSet().getId() > 0)
    {
      logger.debug("ID ist angegeben");
      
      if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() == 1)
      {
        logger.debug("eine Value Set-Version ist angegeben");
        ValueSetVersion vsv = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];
        if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
        {
          return false;
        }
        else
        {
          // vorhandenes VS nutzen
          vs_id = parameter.getValueSet().getId();
          vsv_id = vsv.getVersionId();
        }
      }
      else
      {
        logger.debug("eine neue Value Set-Version wird später erstellt");
      }
    }

    if (vs_id == 0 || vsv_id == 0)
    {
      // Neues VS erstellen
      // TODO zunächst prüfen, ob ValueSet bereits existiert
      
      if(!parameter.getValueSet().getValueSetVersions().isEmpty() && parameter.getValueSet().getValueSetVersions().iterator().next().getName() == null)  
          parameter.getValueSet().getValueSetVersions().iterator().next().setName("");
     
      if(parameter.getValueSet().getValueSetVersions().isEmpty())
          parameter.getValueSet().getValueSetVersions().add(new ValueSetVersion());
      
      CreateValueSetRequestType request = new CreateValueSetRequestType();
      request.setValueSet(parameter.getValueSet());
      request.setLogin(parameter.getLogin());
      
      if(parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
        ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).setStatus(1);
        
      if(parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER) && !onlyVSV){
        ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).setStatus(0);
      }else{
        ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).setStatus(1);
      }   
      
      // Value Set erstellen
      CreateValueSet ccs = new CreateValueSet();
      CreateValueSetResponseType resp = ccs.CreateValueSet(request, hb_session);

      if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
      {
        return false;
      }
      parameter.setValueSet(resp.getValueSet());
      logger.debug("Neue ValueSet-ID: " + resp.getValueSet().getId());
    }

    return true;
  }
  
  private String firstCharUpperCase(String str){
  
      String a = str.substring(0,1);
      String b = str.substring(1);
      a = a.toUpperCase();
      return a + b;
  }
  
  private String replaceApo(String str){
  
    if(str.startsWith("\"") && str.endsWith("\"")){
            
        str = str.replaceFirst("\"", "");
        str = str.substring(0, str.length()-1);
    }
    return str;  
  }
}
