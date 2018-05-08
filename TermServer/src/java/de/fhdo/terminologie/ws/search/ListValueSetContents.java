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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.SortingType;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListValueSetContents
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListValueSetContentsResponseType ListValueSetContents(ListValueSetContentsRequestType parameter)
  {
    return ListValueSetContents(parameter, null);
  }
  
  public ListValueSetContentsResponseType ListValueSetContents(ListValueSetContentsRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListValueSetContents gestartet ======");

    // Return-Informationen anlegen
    ListValueSetContentsResponseType response = new ListValueSetContentsResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    
    boolean createHibernateSession = (session == null);


    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;

    if (parameter != null && parameter.getLogin() != null)
    {
      LoginInfoType loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }


    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
      }
      else
      {
        hb_session = session;
      }
      
      //hb_session.getTransaction().begin();

      //List<CodeSystemEntity> entityList = null;//new LinkedList<CodeSystemEntity>();

      try
      {
        ValueSetVersion vsv = parameter.getValueSet().getValueSetVersions().iterator().next();
        long valueSetVersionId = vsv.getVersionId();
				
				//Matthias: check for ELGA_Laborparameter VS
				boolean isELGALaborparamter = false;
				
				if (parameter.getValueSet().getName() != null 
						&&parameter.getValueSet().getName().contains("ELGA_Laborparameter")){
					isELGALaborparamter = true;
				}
        
        logger.debug("valueSetVersionId: " + valueSetVersionId);

        // Zuerst passenden Level-Metadataparameter lesen
        long metadataParameter_Level_Id = 0;  // 355

        if (parameter.getReadMetadataLevel() != null && parameter.getReadMetadataLevel().booleanValue())
        {
          logger.debug("Finde Level...");
          String hql = "select distinct mp from MetadataParameter mp join mp.valueSet vs join vs.valueSetVersions vsv where vsv.versionId=" + valueSetVersionId
                  + " and mp.paramName='Level'";
          List list = hb_session.createQuery(hql).list();
          
          if(list != null && list.size() > 0)
          {
            MetadataParameter mp = (MetadataParameter) list.get(0);
            metadataParameter_Level_Id = mp.getId();
          }
        }
        
        logger.debug("metadataParameter_Level_Id: " + metadataParameter_Level_Id);

        String sql = "select * from code_system_entity_version csev"
                + " JOIN concept_value_set_membership cvsm ON csev.versionId=cvsm.codeSystemEntityVersionId"
                + " JOIN code_system_concept csc ON csev.versionId=csc.codeSystemEntityVersionId"
                + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
                + " JOIN code_system_version_entity_membership csvem ON csvem.codeSystemEntityId=cse.id"
                + " JOIN code_system_version csv ON csv.versionId=csvem.codeSystemVersionId";
					
				if (isELGALaborparamter){
					sql += " LEFT JOIN code_system_concept_translation csct ON cvsm.codeSystemEntityVersionId=csct.codeSystemEntityVersionId";
				}
				
        
        if(metadataParameter_Level_Id > 0)
          sql += " LEFT JOIN value_set_metadata_value vsmv ON csev.versionId=vsmv.codeSystemEntityVersionId";

				
        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "cvsm.valuesetVersionId", valueSetVersionId);
        
        if(metadataParameter_Level_Id > 0)
          parameterHelper.addParameter("", "vsmv.metadataParameterId", metadataParameter_Level_Id);
        
        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        }
        
        if(vsv.getConceptValueSetMemberships() != null && vsv.getConceptValueSetMemberships().size() > 0)
        {
          ConceptValueSetMembership cvsm = vsv.getConceptValueSetMemberships().iterator().next();
          if(cvsm.getStatusDate() != null)
            parameterHelper.addParameter("cvsm.", "statusDate", cvsm.getStatusDate());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        // Gesamt-Anzahl lesen
        String where = parameterHelper.getWhere("");

        String sortStr = " ORDER BY cvsm.orderNr,csc.code";

        if (parameter.getSortingParameter() != null)
        {
          if (parameter.getSortingParameter().getSortType() == null
                  || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY)
          {
            sortStr = " ORDER BY";

            if (parameter.getSortingParameter().getSortBy() != null
                    && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
            {
              sortStr += " csc.term";
            }
            else
            {
              sortStr += " csc.code";
            }

            if (parameter.getSortingParameter().getSortDirection() != null
                    && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
            {
              sortStr += " desc";
            }
          }
        }

        String where_all = where + sortStr;
        sql += " " + where_all;

        logger.debug("SQL: " + sql);
        
        // Query erstellen
        SQLQuery q = hb_session.createSQLQuery(sql);
				q.setReadOnly(true);
				
        q.addScalar("csc.code", StandardBasicTypes.TEXT);  // Index: 0
        q.addScalar("csc.term", StandardBasicTypes.TEXT);
        q.addScalar("csc.termAbbrevation", StandardBasicTypes.TEXT);
        q.addScalar("csc.description", StandardBasicTypes.TEXT);
        q.addScalar("csc.isPreferred", StandardBasicTypes.BOOLEAN);
        q.addScalar("csc.codeSystemEntityVersionId", StandardBasicTypes.LONG);

        q.addScalar("csev.effectiveDate", StandardBasicTypes.DATE);  // Index: 6
        q.addScalar("csev.insertTimestamp", StandardBasicTypes.DATE);
        q.addScalar("csev.isLeaf", StandardBasicTypes.BOOLEAN);
        q.addScalar("csev.majorRevision", StandardBasicTypes.INTEGER);
        q.addScalar("csev.minorRevision", StandardBasicTypes.INTEGER);
        q.addScalar("csev.status", StandardBasicTypes.INTEGER);
        q.addScalar("csev.statusDate", StandardBasicTypes.DATE);
        q.addScalar("csev.versionId", StandardBasicTypes.LONG);
        q.addScalar("csev.codeSystemEntityId", StandardBasicTypes.LONG);

        q.addScalar("cse.id", StandardBasicTypes.LONG);  // Index: 15
        q.addScalar("cse.currentVersionId", StandardBasicTypes.LONG);

        q.addScalar("csc.meaning", StandardBasicTypes.TEXT); //Index: 17
        q.addScalar("csc.hints", StandardBasicTypes.TEXT);
        
        q.addScalar("cvsm.valueOverride", StandardBasicTypes.TEXT); //Index: 19
        q.addScalar("cvsm.status", StandardBasicTypes.INTEGER);
        q.addScalar("cvsm.statusDate", StandardBasicTypes.DATE);
        q.addScalar("cvsm.isStructureEntry", StandardBasicTypes.BOOLEAN);
        q.addScalar("cvsm.orderNr", StandardBasicTypes.LONG);
        q.addScalar("cvsm.awbeschreibung", StandardBasicTypes.TEXT);
        q.addScalar("cvsm.bedeutung", StandardBasicTypes.TEXT);
        q.addScalar("cvsm.hinweise", StandardBasicTypes.TEXT);
        
        
        q.addScalar("csvem.isAxis", StandardBasicTypes.BOOLEAN); // Index: 27
        q.addScalar("csvem.isMainClass", StandardBasicTypes.BOOLEAN);
        
        q.addScalar("csv.previousVersionID", StandardBasicTypes.LONG); // Index: 29
        q.addScalar("csv.name", StandardBasicTypes.TEXT);
        q.addScalar("csv.status", StandardBasicTypes.INTEGER);
        q.addScalar("csv.statusDate", StandardBasicTypes.DATE);
        q.addScalar("csv.releaseDate", StandardBasicTypes.DATE);
        q.addScalar("csv.expirationDate", StandardBasicTypes.DATE);
        q.addScalar("csv.source", StandardBasicTypes.TEXT);
        q.addScalar("csv.preferredLanguageId", StandardBasicTypes.LONG);
        q.addScalar("csv.oid", StandardBasicTypes.TEXT);
        q.addScalar("csv.licenceHolder", StandardBasicTypes.TEXT);
        q.addScalar("csv.underLicence", StandardBasicTypes.BOOLEAN);
        q.addScalar("csv.insertTimestamp", StandardBasicTypes.DATE);
        q.addScalar("csv.validityRange", StandardBasicTypes.LONG);  // Index: 41
        
        q.addScalar("csv.versionId", StandardBasicTypes.LONG);
				
			
				
				if (isELGALaborparamter){
					q.addScalar("csct.term", StandardBasicTypes.TEXT); // Index 43
					q.addScalar("csct.termAbbrevation", StandardBasicTypes.TEXT);
					q.addScalar("csct.languageId", StandardBasicTypes.LONG);
				}
				
        
        if(metadataParameter_Level_Id > 0)
        {
          q.addScalar("vsmv.parameterValue", StandardBasicTypes.TEXT); // Index: 46 or 43
        }
				
				
        
        // TODO Übersetzungen


        /*q.addScalar("translation_term", Hibernate.TEXT);  // Index: 17
         q.addScalar("translation_termAbbrevation", Hibernate.TEXT);
         q.addScalar("translation_languageId", Hibernate.LONG);
         q.addScalar("translation_description", Hibernate.TEXT);
         q.addScalar("translation_id", Hibernate.LONG);

         */

        parameterHelper.applySQLParameter(q);
        //q.setLong("languageId", languageId);

        //+ " ORDER BY csc.code"
        //q.setParameter("codeSystemVersionId", codeSystemVersionId);

        /*List<CodeSystemConcept> conceptList = (List<CodeSystemConcept>) q.list();

         for (CodeSystemConcept csc : conceptList)
         {
         logger.debug(csc.getCode());
         anzahl++;
         }*/

        response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

        List conceptList = (List) q.list();
        
        logger.debug("Anzahl: " + conceptList.size());

        long lastCodeSystemEntityVersionId = 0;
        CodeSystemEntity cse;
        CodeSystemEntityVersion csev;
        CodeSystemConcept csc;
        CodeSystemVersionEntityMembership csvem;
        CodeSystemVersion csv;
        ConceptValueSetMembership cvsm;
				CodeSystemConceptTranslation csct;
        boolean fertig = false;
        int anzahl = 0;

        Iterator it = conceptList.iterator();

        while (it.hasNext())
        {
          Object[] item = null;
          long codeSystemEntityVersionId = 0;
          do
          {
            if (it.hasNext() == false)
            {
              fertig = true;
              break;
            }

            item = (Object[]) it.next();

            // Prüfen, ob Translation (1:N)
            codeSystemEntityVersionId = (Long) item[5];
            /*if (lastCodeSystemEntityVersionId == codeSystemEntityVersionId)
            {
              // Gleiches Konzept, Assoziation hinzufügen
              // TODO Sprachen hinzufügen ?
              //if (parameter.isLookForward())
              //  addAssociationToEntityVersion(csev, item);
            }*/
          }
          while (lastCodeSystemEntityVersionId == codeSystemEntityVersionId);

          if (fertig)
            break;

          // Konzepte zusammenbauen
          cse = new CodeSystemEntity();
          csev = new CodeSystemEntityVersion();
          csc = new CodeSystemConcept();
          csvem = new CodeSystemVersionEntityMembership();
          csvem.setCodeSystemVersion(new CodeSystemVersion());
          cvsm = new ConceptValueSetMembership();
          csv = new CodeSystemVersion();
					csct = new CodeSystemConceptTranslation();

          // Konzept
          if (item[0] != null)
            csc.setCode(item[0].toString());
          if (item[1] != null)
            csc.setTerm(item[1].toString());
          if (item[2] != null)
            csc.setTermAbbrevation(item[2].toString());
          if (item[3] != null)
            csc.setDescription(item[3].toString());
          if (item[4] != null)
            csc.setIsPreferred((Boolean) item[4]);
          if (item[5] != null)
            csc.setCodeSystemEntityVersionId((Long) item[5]);
          if (item[17] != null)
            csc.setMeaning(item[17].toString());
          if (item[18] != null)
            csc.setHints(item[18].toString());

          // Entity Version
          if (item[6] != null)
            csev.setEffectiveDate((Date) item[6]);
          if (item[7] != null)
            csev.setInsertTimestamp((Date) item[7]);
          if (item[8] != null)
            csev.setIsLeaf((Boolean) item[8]);
          if (item[9] != null)
            csev.setMajorRevision((Integer) item[9]);
          if (item[10] != null)
            csev.setMinorRevision((Integer) item[10]);
          if (item[11] != null)
            csev.setStatus((Integer) item[11]);
          if (item[12] != null)
            csev.setStatusDate((Date) item[12]);
          if (item[13] != null){
            csev.setVersionId((Long) item[13]);
            cvsm.setId(new ConceptValueSetMembershipId(csev.getVersionId(), valueSetVersionId));
          }
          // Code System Entity
          if (item[15] != null)
            cse.setId((Long) item[15]);
          if (item[16] != null)
            cse.setCurrentVersionId((Long) item[16]);
          
          if(item[19] != null)
            cvsm.setValueOverride(item[19].toString());
          if (item[20] != null)
            cvsm.setStatus((Integer) item[20]);
          if (item[21] != null)
            cvsm.setStatusDate((Date) item[21]);
          if (item[22] != null)
            cvsm.setIsStructureEntry((Boolean) item[22]);
          if (item[23] != null)
            cvsm.setOrderNr((Long) item[23]);
          if(item[24] != null)
            cvsm.setAwbeschreibung(item[24].toString());
          if(item[25] != null)
            cvsm.setBedeutung(item[25].toString());
          if(item[26] != null)
            cvsm.setHinweise(item[26].toString());
          
          
          if (item[27] != null)
            csvem.setIsAxis((Boolean) item[27]);
          if (item[28] != null)
            csvem.setIsMainClass((Boolean) item[28]);
          
          if(item[29] != null)
            csv.setPreviousVersionId((Long)item[29]);
          if(item[30] != null)
            csv.setName(item[30].toString());
          if(item[31] != null)
            csv.setStatus((Integer)item[31]);
          if(item[32] != null)
            csv.setStatusDate((Date) item[32]);
          if(item[33] != null)
            csv.setReleaseDate((Date) item[33]);
          if(item[34] != null)
            csv.setExpirationDate((Date) item[34]);
          if(item[35] != null)
            csv.setSource(item[35].toString());
          if(item[36] != null)
            csv.setPreferredLanguageId((Long)item[36]);
          if(item[37] != null)
            csv.setOid(item[37].toString());          
          if(item[38] != null)
            csv.setLicenceHolder(item[38].toString());
          if(item[39] != null)
            csv.setUnderLicence((Boolean) item[39]);
          if(item[40] != null)
            csv.setInsertTimestamp((Date)item[40]);
          if(item[41] != null)
            csv.setValidityRange((Long)item[41]);
          
          
          // Metadaten hinzufügen
          if(item[42] != null)
            csv.setVersionId((Long)item[42]);
					
					int position = 43;
					//Matthias: Translation added
					if (isELGALaborparamter){
						if(item[43] != null)
							csct.setTerm(item[43].toString());
						if(item[44] != null)
							csct.setTermAbbrevation(item[44].toString());
						if(item[45] != null)
							csct.setLanguageId((Long) item[45]);
						
						position = 46;
					}
					
          
          // Metadaten hinzufügen
					//46
          if(item != null && item.length > position && item[position] != null)
          {
            ValueSetMetadataValue mv = new ValueSetMetadataValue();
            mv.setParameterValue(item[position].toString());
            mv.setCodeSystemEntityVersion(null);
            mv.setMetadataParameter(null);
            csev.setValueSetMetadataValues(new HashSet<ValueSetMetadataValue>());
            csev.getValueSetMetadataValues().add(mv);
          }
          
          // TODO Sprachen hinzufügen
					
					csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());
					csc.getCodeSystemConceptTranslations().add(csct);
          
          csvem.setCodeSystemVersion(csv);
          csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
          csev.getCodeSystemConcepts().add(csc);
          csev.setConceptValueSetMemberships(new HashSet<ConceptValueSetMembership>());
          csev.getConceptValueSetMemberships().add(cvsm);
          cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
          cse.getCodeSystemEntityVersions().add(csev);
          cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
          cse.getCodeSystemVersionEntityMemberships().add(csvem);
          response.getCodeSystemEntity().add(cse);

          lastCodeSystemEntityVersionId = codeSystemEntityVersionId;

          anzahl++;
        }

        response.getReturnInfos().setCount(anzahl);

        if (response.getCodeSystemEntity() == null || anzahl == 0)
        {
          response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet wurden keine Konzepte gefunden!");
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        else
        {
          response.getReturnInfos().setMessage("Konzepte zu einem ValueSet erfolgreich gelesen");
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
        }
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        if(createHibernateSession)
          hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListValueSetContents': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ListValueSetContentsRequestType Request, ListValueSetContentsResponseType Response)
  {
    boolean erfolg = true;

    if (Request != null)
    {

      if (Request.getLogin() != null)
      {
        if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
        {
          Response.getReturnInfos().setMessage(
                  "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
          erfolg = false;
        }
      }

      if (Request.getValueSet() == null)
      {
        Response.getReturnInfos().setMessage(
                "ValueSet darf nicht NULL sein!");
        erfolg = false;
      }
      /*else if (Request.getValueSet().getId() == null || Request.getValueSet().getId() == 0)
       {
       Response.getReturnInfos().setMessage(
       "Die ID im ValueSet darf nicht NULL oder 0 sein!");
       erfolg = false;
       }*/
      else if (Request.getValueSet().getValueSetVersions() != null)
      {
        if (Request.getValueSet().getValueSetVersions().size() != 1)
        {
          Response.getReturnInfos().setMessage(
                  "Die ValueSetVersion-Liste muss genau einen Eintrag haben oder die Liste ist NULL!");
          erfolg = false;
        }
        else
        {
          ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];
          if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Die ValueSetVersion muss eine ID größer als 0 beinhalten!");
            erfolg = false;
          }
        }
      }
    }
    else
    {
      Response.getReturnInfos().setMessage(
              "Request == NULL");
      erfolg = false;
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }


    return true;
  }
}
