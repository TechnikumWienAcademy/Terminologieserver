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
 * V 3.3 RDY
 * @author Robert Mützner
 */
public class ListValueSetContents{
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Calls ListValueSetContents(parameter, null).
     * @param parameter the parameter for the subsequent call.
     * @return the return information of the subsequent call.
     */
    public ListValueSetContentsResponseType ListValueSetContents(ListValueSetContentsRequestType parameter){
        return ListValueSetContents(parameter, null);
    }
  
    /**
     * TODO
     * @param parameter
     * @param session
     * @return 
     */
    public ListValueSetContentsResponseType ListValueSetContents(ListValueSetContentsRequestType parameter, org.hibernate.Session session){
        LOGGER.info("+++++ ListValueSetContents started +++++");

        //Creating return information
        ListValueSetContentsResponseType response = new ListValueSetContentsResponseType();
        response.setReturnInfos(new ReturnType());

        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- ListValueSetContents finished (001) -----");
            return response; //Faulty parameters
        }

        //Checking login (must be done by every webservice)
        boolean loggedIn = false;

        if (parameter != null && parameter.getLogin() != null){
            LoginInfoType loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }


        try{
            org.hibernate.Session hb_session;

            if (session == null)
                hb_session = HibernateUtil.getSessionFactory().openSession();
            else
                hb_session = session;
            
            try{
                ValueSetVersion VSV = parameter.getValueSet().getValueSetVersions().iterator().next();
                long VSversionID = VSV.getVersionId();
		LOGGER.debug("Value set version ID: " + VSversionID);
                
		boolean isELGALaborparamter = false;		
                if (parameter.getValueSet().getName() != null && parameter.getValueSet().getName().contains("ELGA_Laborparameter"))
                    isELGALaborparamter = true;
                
                //Reading fitting level metadataparameter
                long metadataParameter_Level_Id = 0;
                
                if (parameter.getReadMetadataLevel() != null && parameter.getReadMetadataLevel()){
                    String HQL_metadataParameter_search = "select distinct mp from MetadataParameter mp join mp.valueSet vs join vs.valueSetVersions vsv where vsv.versionId=" + VSversionID
                        + " and mp.paramName='Level'";
                    List metadataList = hb_session.createQuery(HQL_metadataParameter_search).list();

                    if(metadataList != null && metadataList.size() > 0){
                        MetadataParameter metadataParameter = (MetadataParameter) metadataList.get(0);
                        metadataParameter_Level_Id = metadataParameter.getId();
                    }
                }
        
                LOGGER.debug("metadataParameter_Level_Id: " + metadataParameter_Level_Id);

                String HQL = "select * from code_system_entity_version csev"
                    + " JOIN concept_value_set_membership cvsm ON csev.versionId=cvsm.codeSystemEntityVersionId"
                    + " JOIN code_system_concept csc ON csev.versionId=csc.codeSystemEntityVersionId"
                    + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
                    + " JOIN code_system_version_entity_membership csvem ON csvem.codeSystemEntityId=cse.id"
                    + " JOIN code_system_version csv ON csv.versionId=csvem.codeSystemVersionId";
					
                if (isELGALaborparamter)
                    HQL += " LEFT JOIN code_system_concept_translation csct ON cvsm.codeSystemEntityVersionId=csct.codeSystemEntityVersionId";
				
                if(metadataParameter_Level_Id > 0)
                    HQL += " LEFT JOIN value_set_metadata_value vsmv ON csev.versionId=vsmv.codeSystemEntityVersionId";

                //Adding parameters to the helper, always use it or do it manually with Query.setString() otherwise SQL-injections are possible
                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("", "cvsm.valuesetVersionId", VSversionID);
        
                if(metadataParameter_Level_Id > 0)
                    parameterHelper.addParameter("", "vsmv.metadataParameterId", metadataParameter_Level_Id);
        
                if (loggedIn == false)
                    parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        
                if(VSV.getConceptValueSetMemberships() != null && VSV.getConceptValueSetMemberships().size() > 0){
                    ConceptValueSetMembership CVSM = VSV.getConceptValueSetMemberships().iterator().next();
                    if(CVSM.getStatusDate() != null)
                        parameterHelper.addParameter("cvsm.", "statusDate", CVSM.getStatusDate());
                }

                //Adding parameters (connected through AND)
                String where = parameterHelper.getWhere("");
                String sortStr = " ORDER BY cvsm.orderNr,csc.code";

                if (parameter.getSortingParameter() != null){
                    if (parameter.getSortingParameter().getSortType() == null
                    || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY){
                        sortStr = " ORDER BY";

                        if (parameter.getSortingParameter().getSortBy() != null
                        && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
                            sortStr += " csc.term";
                        else
                            sortStr += " csc.code";

                        if (parameter.getSortingParameter().getSortDirection() != null
                        && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
                            sortStr += " desc";
                    }
                }

                String where_all = where + sortStr;
                HQL += " " + where_all;

                LOGGER.debug("HQL: " + HQL);
        
                //Creating query
                SQLQuery Q_CSEversion_search = hb_session.createSQLQuery(HQL);
                Q_CSEversion_search.setReadOnly(true);
				
                Q_CSEversion_search.addScalar("csc.code", StandardBasicTypes.TEXT);  // Index: 0
                Q_CSEversion_search.addScalar("csc.term", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csc.termAbbrevation", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csc.description", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csc.isPreferred", StandardBasicTypes.BOOLEAN);
                Q_CSEversion_search.addScalar("csc.codeSystemEntityVersionId", StandardBasicTypes.LONG);

                Q_CSEversion_search.addScalar("csev.effectiveDate", StandardBasicTypes.DATE);  // Index: 6
                Q_CSEversion_search.addScalar("csev.insertTimestamp", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csev.isLeaf", StandardBasicTypes.BOOLEAN);
                Q_CSEversion_search.addScalar("csev.majorRevision", StandardBasicTypes.INTEGER);
                Q_CSEversion_search.addScalar("csev.minorRevision", StandardBasicTypes.INTEGER);
                Q_CSEversion_search.addScalar("csev.status", StandardBasicTypes.INTEGER);
                Q_CSEversion_search.addScalar("csev.statusDate", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csev.versionId", StandardBasicTypes.LONG);
                Q_CSEversion_search.addScalar("csev.codeSystemEntityId", StandardBasicTypes.LONG);

                Q_CSEversion_search.addScalar("cse.id", StandardBasicTypes.LONG);  // Index: 15
                Q_CSEversion_search.addScalar("cse.currentVersionId", StandardBasicTypes.LONG);

                Q_CSEversion_search.addScalar("csc.meaning", StandardBasicTypes.TEXT); //Index: 17
                Q_CSEversion_search.addScalar("csc.hints", StandardBasicTypes.TEXT);
        
                Q_CSEversion_search.addScalar("cvsm.valueOverride", StandardBasicTypes.TEXT); //Index: 19
                Q_CSEversion_search.addScalar("cvsm.status", StandardBasicTypes.INTEGER);
                Q_CSEversion_search.addScalar("cvsm.statusDate", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("cvsm.isStructureEntry", StandardBasicTypes.BOOLEAN);
                Q_CSEversion_search.addScalar("cvsm.orderNr", StandardBasicTypes.LONG);
                Q_CSEversion_search.addScalar("cvsm.awbeschreibung", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("cvsm.bedeutung", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("cvsm.hinweise", StandardBasicTypes.TEXT);


                Q_CSEversion_search.addScalar("csvem.isAxis", StandardBasicTypes.BOOLEAN); // Index: 27
                Q_CSEversion_search.addScalar("csvem.isMainClass", StandardBasicTypes.BOOLEAN);

                Q_CSEversion_search.addScalar("csv.previousVersionID", StandardBasicTypes.LONG); // Index: 29
                Q_CSEversion_search.addScalar("csv.name", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csv.status", StandardBasicTypes.INTEGER);
                Q_CSEversion_search.addScalar("csv.statusDate", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csv.releaseDate", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csv.expirationDate", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csv.source", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csv.preferredLanguageId", StandardBasicTypes.LONG);
                Q_CSEversion_search.addScalar("csv.oid", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csv.licenceHolder", StandardBasicTypes.TEXT);
                Q_CSEversion_search.addScalar("csv.underLicence", StandardBasicTypes.BOOLEAN);
                Q_CSEversion_search.addScalar("csv.insertTimestamp", StandardBasicTypes.DATE);
                Q_CSEversion_search.addScalar("csv.validityRange", StandardBasicTypes.LONG);  // Index: 41

                Q_CSEversion_search.addScalar("csv.versionId", StandardBasicTypes.LONG);
                
                if (isELGALaborparamter){
                    Q_CSEversion_search.addScalar("csct.term", StandardBasicTypes.TEXT); // Index 43
                    Q_CSEversion_search.addScalar("csct.termAbbrevation", StandardBasicTypes.TEXT);
                    Q_CSEversion_search.addScalar("csct.languageId", StandardBasicTypes.LONG);
                }
				
                if(metadataParameter_Level_Id > 0)
                    Q_CSEversion_search.addScalar("vsmv.parameterValue", StandardBasicTypes.TEXT); // Index: 46 or 43 

                parameterHelper.applySQLParameter(Q_CSEversion_search);
        
                response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

                List conceptList = (List) Q_CSEversion_search.list();
        
                LOGGER.debug("Concept count: " + conceptList.size());

                boolean finished = false;
                int count = 0;
                long lastCodeSystemEntityVersionId = 0;
                CodeSystemEntity CSentity;
                CodeSystemEntityVersion CSentityVersion;
                CodeSystemConcept CSconcept;
                CodeSystemVersionEntityMembership CSVentityMembership;
                CodeSystemVersion CSversion;
                ConceptValueSetMembership conceptValueSetMembership;
                CodeSystemConceptTranslation CSconceptTranslation;

                Iterator conceptIterator = conceptList.iterator();

                while (conceptIterator.hasNext()){
                    Object[] item = null;
                    long codeSystemEntityVersionId = 0;
                    do{
                        if (conceptIterator.hasNext() == false){
                            finished = true;
                            break;
                        }

                        item = (Object[]) conceptIterator.next();
                        codeSystemEntityVersionId = (Long) item[5];
                    }
                    while (lastCodeSystemEntityVersionId == codeSystemEntityVersionId);

                    if (finished)
                        break;

                    //Building concepts
                    CSentity = new CodeSystemEntity();
                    CSentityVersion = new CodeSystemEntityVersion();
                    CSconcept = new CodeSystemConcept();
                    CSVentityMembership = new CodeSystemVersionEntityMembership();
                    CSVentityMembership.setCodeSystemVersion(new CodeSystemVersion());
                    conceptValueSetMembership = new ConceptValueSetMembership();
                    CSversion = new CodeSystemVersion();
                    CSconceptTranslation = new CodeSystemConceptTranslation();

                    if (item[0] != null)
                        CSconcept.setCode(item[0].toString());
                    if (item[1] != null)
                        CSconcept.setTerm(item[1].toString());
                    if (item[2] != null)
                        CSconcept.setTermAbbrevation(item[2].toString());
                    if (item[3] != null)
                        CSconcept.setDescription(item[3].toString());
                    if (item[4] != null)
                        CSconcept.setIsPreferred((Boolean) item[4]);
                    if (item[5] != null)
                        CSconcept.setCodeSystemEntityVersionId((Long) item[5]);
                    if (item[17] != null)
                        CSconcept.setMeaning(item[17].toString());
                    if (item[18] != null)
                        CSconcept.setHints(item[18].toString());

                    if (item[6] != null)
                        CSentityVersion.setEffectiveDate((Date) item[6]);
                    if (item[7] != null)
                        CSentityVersion.setInsertTimestamp((Date) item[7]);
                    if (item[8] != null)
                        CSentityVersion.setIsLeaf((Boolean) item[8]);
                    if (item[9] != null)
                        CSentityVersion.setMajorRevision((Integer) item[9]);
                    if (item[10] != null)
                        CSentityVersion.setMinorRevision((Integer) item[10]);
                    if (item[11] != null)
                        CSentityVersion.setStatus((Integer) item[11]);
                    if (item[12] != null)
                        CSentityVersion.setStatusDate((Date) item[12]);
                    if (item[13] != null){
                        CSentityVersion.setVersionId((Long) item[13]);
                        conceptValueSetMembership.setId(new ConceptValueSetMembershipId(CSentityVersion.getVersionId(), VSversionID));
                    }

                    if (item[15] != null)
                        CSentity.setId((Long) item[15]);
                    if (item[16] != null)
                        CSentity.setCurrentVersionId((Long) item[16]);
                    
                    if(item[19] != null)
                        conceptValueSetMembership.setValueOverride(item[19].toString());
                    if (item[20] != null)
                        conceptValueSetMembership.setStatus((Integer) item[20]);
                    if (item[21] != null)
                        conceptValueSetMembership.setStatusDate((Date) item[21]);
                    if (item[22] != null)
                        conceptValueSetMembership.setIsStructureEntry((Boolean) item[22]);
                    if (item[23] != null)
                        conceptValueSetMembership.setOrderNr((Long) item[23]);
                    if(item[24] != null)
                        conceptValueSetMembership.setAwbeschreibung(item[24].toString());
                    if(item[25] != null)
                        conceptValueSetMembership.setBedeutung(item[25].toString());
                    if(item[26] != null)
                        conceptValueSetMembership.setHinweise(item[26].toString());


                    if (item[27] != null)
                        CSVentityMembership.setIsAxis((Boolean) item[27]);
                    if (item[28] != null)
                        CSVentityMembership.setIsMainClass((Boolean) item[28]);

                    if(item[29] != null)
                        CSversion.setPreviousVersionId((Long)item[29]);
                    if(item[30] != null)
                        CSversion.setName(item[30].toString());
                    if(item[31] != null)
                        CSversion.setStatus((Integer)item[31]);
                    if(item[32] != null)
                        CSversion.setStatusDate((Date) item[32]);
                    if(item[33] != null)
                        CSversion.setReleaseDate((Date) item[33]);
                    if(item[34] != null)
                        CSversion.setExpirationDate((Date) item[34]);
                    if(item[35] != null)
                        CSversion.setSource(item[35].toString());
                    if(item[36] != null)
                        CSversion.setPreferredLanguageId((Long)item[36]);
                    if(item[37] != null)
                        CSversion.setOid(item[37].toString());          
                    if(item[38] != null)
                        CSversion.setLicenceHolder(item[38].toString());
                    if(item[39] != null)
                        CSversion.setUnderLicence((Boolean) item[39]);
                    if(item[40] != null)
                        CSversion.setInsertTimestamp((Date)item[40]);
                    if(item[41] != null)
                        CSversion.setValidityRange((Long)item[41]);
          
                    //Adding metadata
                    if(item[42] != null)
                        CSversion.setVersionId((Long)item[42]);
					
                    int position = 43;
                    if (isELGALaborparamter){
                        if(item[43] != null)
                            CSconceptTranslation.setTerm(item[43].toString());
                        if(item[44] != null)
                            CSconceptTranslation.setTermAbbrevation(item[44].toString());
                        if(item[45] != null)
                            CSconceptTranslation.setLanguageId((Long) item[45]);
		    			
                        position = 46;
                    }
            
                    if(item != null && item.length > position && item[position] != null){
                        ValueSetMetadataValue VSmetadataValue = new ValueSetMetadataValue();
                        VSmetadataValue.setParameterValue(item[position].toString());
                        VSmetadataValue.setCodeSystemEntityVersion(null);
                        VSmetadataValue.setMetadataParameter(null);
                        CSentityVersion.setValueSetMetadataValues(new HashSet<ValueSetMetadataValue>());
                        CSentityVersion.getValueSetMetadataValues().add(VSmetadataValue);
                    }
					
                    CSconcept.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());
                    CSconcept.getCodeSystemConceptTranslations().add(CSconceptTranslation);
          
                    CSVentityMembership.setCodeSystemVersion(CSversion);
                    CSentityVersion.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
                    CSentityVersion.getCodeSystemConcepts().add(CSconcept);
                    CSentityVersion.setConceptValueSetMemberships(new HashSet<ConceptValueSetMembership>());
                    CSentityVersion.getConceptValueSetMemberships().add(conceptValueSetMembership);
                    CSentity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                    CSentity.getCodeSystemEntityVersions().add(CSentityVersion);
                    CSentity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
                    CSentity.getCodeSystemVersionEntityMemberships().add(CSVentityMembership);
                    response.getCodeSystemEntity().add(CSentity);

                    lastCodeSystemEntityVersionId = codeSystemEntityVersionId;

                    count++;
                }

                response.getReturnInfos().setCount(count);

                if (response.getCodeSystemEntity() == null || count == 0){
                    response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet wurden keine Konzepte gefunden!");
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                }
                else{
                    response.getReturnInfos().setMessage("Konzepte zu einem ValueSet erfolgreich gelesen");
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                }
                
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            }
            catch (Exception e){
                LOGGER.error("Error [0077]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0078]: " + e.getLocalizedMessage());
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- ListValueSetContents finished (002) -----");
        return response;
    }

    /**
     * Checks the paramters for validity.
     * @param Request the parameters to be checked.
     * @param Response information about the check.
     * @return true if the check passed, else false.
     */
    private boolean validateParameter(ListValueSetContentsRequestType Request, ListValueSetContentsResponseType Response){
        boolean passed = true;

        if (Request != null){
            if (Request.getLogin() != null){
                if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0){
                    Response.getReturnInfos().setMessage("Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
                    passed = false;
                }
            }

            if (Request.getValueSet() == null){
                Response.getReturnInfos().setMessage("ValueSet darf nicht NULL sein!");
                passed = false;
            }
            else if (Request.getValueSet().getValueSetVersions() != null){
                if (Request.getValueSet().getValueSetVersions().size() != 1){
                    Response.getReturnInfos().setMessage("Die ValueSetVersion-Liste muss genau einen Eintrag haben oder die Liste ist NULL!");
                    passed = false;
                }
                else{
                    ValueSetVersion VSV = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];
                    if (VSV.getVersionId() == null || VSV.getVersionId() == 0){
                        Response.getReturnInfos().setMessage("Die ValueSetVersion muss eine ID größer als 0 beinhalten!");
                        passed = false;
                    }
                }
            }
        }
        else{
            Response.getReturnInfos().setMessage("Request == NULL");
            passed = false;
        }

        if (!passed){
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        return passed;
    }
}
