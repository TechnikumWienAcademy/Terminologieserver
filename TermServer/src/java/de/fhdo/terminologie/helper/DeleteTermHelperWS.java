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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * 3.2.26 complete check
 * This class deletes terminologies from the database.
 * @author Philipp Urbauer, Dario Bachinger
 */
public class DeleteTermHelperWS {
    
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    /**
     * Deletes a value-set-version and all belonging data.
     * Calls itself recursively.
     * @param onlyVSV true to delete only the value-set-version
     * @param valueSetId the ID of the value-set from which to delete
     * @param valueSetVersionId the ID of the value-set-version from which to delete
     * @return info about the execution of the function
     */
    public static String deleteVS_VSV(Boolean onlyVSV, Long valueSetId, Long valueSetVersionId){
        LOGGER.info("+++++ deleteVS_VSV started +++++");
        
        String result="\n";
        int rowCountVSMV = 0;
        int rowCountCVSM = 0;
        int rowCountVSV = 0;
        int rowCountMP = 0;
        int rowCountVS = 0;
        
        List<ConceptValueSetMembership> listCVSM;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try{
            ValueSet VS = (ValueSet)hb_session.get(ValueSet.class,valueSetId);
            
            if(valueSetVersionId == null){
                for(ValueSetVersion VSV : VS.getValueSetVersions()){
                    result += "Version: " + VSV.getName();
                    result += deleteVS_VSV(false, valueSetId, VSV.getVersionId());
                }

                hb_session.getTransaction().begin();

                //Check for value-set-metadata-value
                Query Q_metadataParameter_Search = hb_session.createQuery("from MetadataParameter where valueSetId=:vsId");
                Q_metadataParameter_Search.setParameter("vsId", valueSetId);
                List<MetadataParameter> listMP = Q_metadataParameter_Search.list();

                //Delete value-set-netadata-value
                for(MetadataParameter MP : listMP){
                    Query Q_VSmetadataValue_delete = hb_session.createQuery("delete from ValueSetMetadataValue where metadataParameterId=:mpId");
                    Q_VSmetadataValue_delete.setParameter("mpId", MP.getId());
                    rowCountVSMV += Q_VSmetadataValue_delete.executeUpdate();
                }

                //Delete metadata-parameter
                Query Q_metadataParameter_delete = hb_session.createQuery("delete from MetadataParameter where valueSetId=:vsId");
                Q_metadataParameter_delete.setParameter("vsId", valueSetId);
                rowCountMP += Q_metadataParameter_delete.executeUpdate();

                //Delete value-set
                Query Q_valueSet_delete = hb_session.createQuery("delete from ValueSet where id=:vsId");
                Q_valueSet_delete.setParameter("vsId", valueSetId);
                rowCountVS += Q_valueSet_delete.executeUpdate();

                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
                result += "Metadata-parameter: " + rowCountMP + "\n";
                result += "Value-set: " + rowCountVS + "\n\n";
            }
            else{
                if(VS.getValueSetVersions().size() == 1 && onlyVSV){
                    LOGGER.info("----- deleteVS_VSV finished (001) -----");
                    return "Ein Value Set MUSS zumindest eine Version haben! \nBitte legen Sie eine neue an bevor Sie diese entfernen. \nAlternativ kann das ganze Value Set entfernt werden.";
                }
                
                //Searching for concept-value-set-memberships
                Query Q_conceptValueSetMembership_search = hb_session.createQuery("select distinct cvsm from ConceptValueSetMembership cvsm join cvsm.valueSetVersion vsv where vsv.versionId=:valueSetVersionId");
                Q_conceptValueSetMembership_search.setParameter("valueSetVersionId", valueSetVersionId);
                listCVSM = Q_conceptValueSetMembership_search.list();
                
                hb_session.getTransaction().begin();

                for(ConceptValueSetMembership CVSM : listCVSM){
                    //Searching for value-set-metadata-value
                    Query Q_valueSetMetadataValue_search = hb_session.createQuery("select distinct vsmv from ValueSetMetadataValue vsmv join fetch vsmv.codeSystemEntityVersion csev where vsmv.valuesetVersionId=:vsvId and csev.versionId=:csevId");
                    Q_valueSetMetadataValue_search.setParameter("vsvId", CVSM.getValueSetVersion().getVersionId());
                    Q_valueSetMetadataValue_search.setParameter("csevId", CVSM.getCodeSystemEntityVersion().getVersionId());
                    List<ValueSetMetadataValue> listVSMV = Q_valueSetMetadataValue_search.list();
                    
                    //Deleting value-set-metadata-value
                    for(ValueSetMetadataValue VSMV : listVSMV){
                        hb_session.delete(VSMV);
                        rowCountVSMV++;
                    }
                    
                    //Deleting concept-value-set-memberships
                    hb_session.delete(CVSM);
                    rowCountCVSM++;
                }
                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
                hb_session.getTransaction().begin();

                //Deleting value-set-version
                Query Q_valueSetVersion_delete = hb_session.createQuery("delete from ValueSetVersion where versionId=:vsvId");
                Q_valueSetVersion_delete.setParameter("vsvId", valueSetVersionId);
                rowCountVSV += Q_valueSetVersion_delete.executeUpdate();

                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();

                result += "Value-set: " + VS.getName() + "\n";
                result += "Value-set-metadata-value: " + rowCountVSMV + "\n";
                result += "Concept-value-set-membership: " + rowCountCVSM + "\n";
                result += "Value-set-version: " + rowCountVSV + "\n\n";
            }
        }
        catch (Exception e){
            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
            result += "Ein Fehler ist aufgetreten [0000]: " + e.getMessage();
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        LOGGER.info("----- deleteVS_VSV finished (002) -----");
        return result;
    }
    
    /**
     * Deletes a code-system-version and all belonging data.
     * Calls itself recursively.
     * @param onlyCSV if true, only the code-system-version will be deleted
     * @param codeSystemId the ID of the code-system to be deleted
     * @param codeSystemVersionId the ID of the code-system-version to be deleted
     * @return info about the execution of the function
     */
    public static String deleteCS_CSV(Boolean onlyCSV,Long codeSystemId, Long codeSystemVersionId){
        LOGGER.info("+++++ deleteCS_CSV started +++++");
        
        String result="\n";
        int rowCountCSCT = 0;
        int rowCountCSC = 0;
        int rowCountCVSM = 0;
        int rowCountCSMV = 0;
        int rowCountCSEVA = 0;
        int rowCountAT = 0;
        int rowCountCSEV = 0;
        int rowCountCSVEM = 0;
        int rowCountCSE = 0;
        int rowCountCSV = 0;
        int rowCountLU = 0;
        int rowCountLT = 0;
        int rowCountMP = 0;
        int rowCountDVHCS = 0;
        int rowCountCS = 0;
        
        List listCSEV_IDs;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try{
            CodeSystem CS = (CodeSystem)hb_session.get(CodeSystem.class,codeSystemId);
            
            if(codeSystemVersionId == null){
                for(CodeSystemVersion CSV : CS.getCodeSystemVersions()){
                    result += "Version: " + CSV.getName();
                    result += deleteCS_CSV(false, codeSystemId, CSV.getVersionId());
                }
                
                hb_session.getTransaction().begin();
                
                //Search for metadata-parameters
                Query Q_metadataParameter_search = hb_session.createQuery("from MetadataParameter where codeSystemId=:csId");
                Q_metadataParameter_search.setParameter("csId", codeSystemId);
                List<MetadataParameter> listMP = Q_metadataParameter_search.list();

                //Delete code-system-metadata-values
                for(MetadataParameter MP : listMP){
                    Query Q_codeSystemMetadataValue_delete = hb_session.createQuery("delete from CodeSystemMetadataValue where metadataParameterId=:mpId");
                    Q_codeSystemMetadataValue_delete.setParameter("mpId", MP.getId());
                    rowCountCSMV += Q_codeSystemMetadataValue_delete.executeUpdate();
                }

                //Delete metadata-parameters
                Query Q_metadataParameter_delete = hb_session.createQuery("delete from MetadataParameter where codeSystemId=:csId");
                Q_metadataParameter_delete.setParameter("csId", codeSystemId);
                rowCountMP += Q_metadataParameter_delete.executeUpdate();

                //Delete domain-value-has-code-system
                Query Q_domainValueHasCodeSystem_delete = hb_session.createSQLQuery("delete from domain_value_has_code_system where code_system_id = ?");
                Q_domainValueHasCodeSystem_delete.setLong(0, codeSystemId);
                rowCountDVHCS += Q_domainValueHasCodeSystem_delete.executeUpdate();

                //Delete code-system
                Query Q_codeSystem_delete = hb_session.createQuery("delete from CodeSystem where id=:id");
                Q_codeSystem_delete.setParameter("id", codeSystemId);
                rowCountCS += Q_codeSystem_delete.executeUpdate();

                result += "Code-system: " + CS.getName() + "\n";
                result += "Metadata-parameter: " + rowCountMP + "\n";
                result += "Domain-value-has-code-system: " + rowCountDVHCS + "\n";
                result += "Code-system: " + rowCountCS + "\n\n";  
                
                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
            }
            else{
                if(CS.getCodeSystemVersions().size() == 1 && onlyCSV){
                    LOGGER.info("----- deleteCS_CSV finished (001) -----");
                    return "Ein Code System MUSS zumindest eine Version haben! \nBitte legen Sie eine neue an bevor Sie diese entfernen. \nAlternativ kann das gesamte Code-System entfernt werden.";
                }
                
                //Searching for code-system-entity-versions
                String HQL_buffer = "select csev.versionId,csev.codeSystemEntity.id from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem";
                      HQL_buffer += " join csvem.codeSystemVersion csv where csv.versionId=:versionId";
                Query Q_codeSystemEntityVersion_search = hb_session.createQuery(HQL_buffer);
                Q_codeSystemEntityVersion_search.setParameter("versionId", codeSystemVersionId);
                listCSEV_IDs = Q_codeSystemEntityVersion_search.list();
                
                //Start deleting
                hb_session.getTransaction().begin();

                int HBflushCounter = 0;
                for(Object preCastCS_IDs : listCSEV_IDs){
                    HBflushCounter++;
										
                    Object[] cast_CS_IDs = (Object[])preCastCS_IDs;
                    Long CSEV_ID = (Long)cast_CS_IDs[0];
                    Long CSE_ID = (Long)cast_CS_IDs[1];

                    //Deleting code-system-concept-translation
                    Query Q_codeSystemConceptTranslation_delete = hb_session.createQuery("delete from CodeSystemConceptTranslation where codeSystemConcept.codeSystemEntityVersionId=:csevId");
                    Q_codeSystemConceptTranslation_delete.setParameter("csevId", CSEV_ID);
                    rowCountCSCT += Q_codeSystemConceptTranslation_delete.executeUpdate();

                    //Deleting code-system-concept
                    Query Q_codeSystemConcept_delete = hb_session.createQuery("delete from CodeSystemConcept where codeSystemEntityVersionId=:csevId");
                    Q_codeSystemConcept_delete.setParameter("csevId", CSEV_ID);
                    rowCountCSC += Q_codeSystemConcept_delete.executeUpdate();

                    //Deleting concept-value-set-membership
                    Query Q_conceptValueSetMembership_delete = hb_session.createQuery("delete from ConceptValueSetMembership where codeSystemEntityVersionId=:csevId");
                    Q_conceptValueSetMembership_delete.setParameter("csevId", CSEV_ID);
                    rowCountCVSM += Q_conceptValueSetMembership_delete.executeUpdate();

                    //Deleting code-system-metadata-value
                    Query Q_codeSystemMetadataValue_delete = hb_session.createQuery("delete from CodeSystemMetadataValue where codeSystemEntityVersionId=:csevId");
                    Q_codeSystemMetadataValue_delete.setParameter("csevId", CSEV_ID);
                    rowCountCSMV += Q_codeSystemMetadataValue_delete.executeUpdate();

                    //Deleting code-system-entity-version-association1
                    Query Q_codeSystemEntityVersionAssociation_delete1 = hb_session.createQuery("delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId1=:csevId");
                    Q_codeSystemEntityVersionAssociation_delete1.setParameter("csevId", CSEV_ID);
                    rowCountCSEVA += Q_codeSystemEntityVersionAssociation_delete1.executeUpdate();

                    //Deleting code-system-entity-version-association2
                    Query Q_codeSystemEntityVersionAssociation_delete2 = hb_session.createQuery("delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId2=:csevId");
                    Q_codeSystemEntityVersionAssociation_delete2.setParameter("csevId", CSEV_ID);
                    rowCountCSEVA += Q_codeSystemEntityVersionAssociation_delete2.executeUpdate();

                    //Deleting association-type
                    Query Q_associationType_delete = hb_session.createQuery("delete from AssociationType where codeSystemEntityVersionId=:csevId");
                    Q_associationType_delete.setParameter("csevId", CSEV_ID);
                    rowCountAT += Q_associationType_delete.executeUpdate();

                    //Deleting code-system-entity-version
                    //TODO Error for csevId 657084 according to Matthias
                    Query Q_codeSystemEntityVersion_delete = hb_session.createQuery("delete from CodeSystemEntityVersion where versionId=:csevId");
                    Q_codeSystemEntityVersion_delete.setParameter("csevId", CSEV_ID);
                    rowCountCSEV += Q_codeSystemEntityVersion_delete.executeUpdate();

                    //Deleting code-system-version-entity-membership
                    Query Q_codeSystemVersionEntityMembership_delete = hb_session.createQuery("delete from CodeSystemVersionEntityMembership where codeSystemVersionId=:csvId");
                    Q_codeSystemVersionEntityMembership_delete.setParameter("csvId", codeSystemVersionId);
                    rowCountCSVEM += Q_codeSystemVersionEntityMembership_delete.executeUpdate();

                    //Deleting code-system-entity
                    Query Q_codeSystemEntity_delete = hb_session.createQuery("delete from CodeSystemEntity where id=:cseId");
                    Q_codeSystemEntity_delete.setParameter("cseId", CSE_ID);
                    rowCountCSE += Q_codeSystemEntity_delete.executeUpdate();
										
                    if (HBflushCounter%500 == 0){
                        hb_session.flush();
                        hb_session.clear();
                        HBflushCounter = 0;
                    }
                }

                //Deleting licenced-user
                Query Q_licencedUser_delete = hb_session.createQuery("delete from LicencedUser where codeSystemVersionId=:csvId");
                Q_licencedUser_delete.setParameter("csvId", codeSystemVersionId);
                rowCountLU += Q_licencedUser_delete.executeUpdate();

                //Deleting licence-type
                Query Q_licenceType_delete = hb_session.createQuery("delete from LicenceType where codeSystemVersionId=:csvId");
                Q_licenceType_delete.setParameter("csvId", codeSystemVersionId);
                rowCountLT += Q_licenceType_delete.executeUpdate();

                //Deleting code-system-version
                Query Q_codeSystemVersion_delete = hb_session.createQuery("delete from CodeSystemVersion where versionId=:versionId");
                Q_codeSystemVersion_delete.setParameter("versionId", codeSystemVersionId);
                rowCountCSV += Q_codeSystemVersion_delete.executeUpdate();

                result += "Code-system-concept-translation: " + rowCountCSCT + "\n";
                result += "Code-system-concept: " + rowCountCSC + "\n";
                result += "Concept-value-set-membership: " + rowCountCVSM + "\n";
                result += "Code-system-metadata-value: " + rowCountCSMV + "\n";
                result += "Code-system-entity-version-association: " + rowCountCSEVA + "\n";
                result += "Association-type: " + rowCountAT + "\n";
                result += "Code-system-entity-version: " + rowCountCSEV + "\n";
                result += "Code-system-version-entity-membership: " + rowCountCSVEM + "\n";
                result += "Code-system-entity: " + rowCountCSE + "\n";
                result += "Licenced-user: " + rowCountLU + "\n";
                result += "Licence-type: " + rowCountLT + "\n";
                result += "Code-system-version: " + rowCountCSV + "\n\n";

                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
            }
        }
        catch (Exception e){
            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
            result += "Ein Fehler ist aufgetreten [0001]: " + e.getMessage();
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- deleteCS_CSV finished (002) -----");
        return result;
    }
    
    /**
     * Deletes a code-system-entity-version and all belonging data.
     * @param csevId the ID of the CSEV to be deleted
     * @param csvId the ID of the CSV to be deleted
     * @return info about the execution of the function
     */
    public static String deleteCSEV(Long csevId, Long csvId){
        LOGGER.info("+++++ deleteCSEV started +++++");
        
        String result="\n";
        int rowCountCSCT = 0;
        int rowCountCSC = 0;
        int rowCountCVSM = 0;
        int rowCountCSMV = 0;
        int rowCountCSEVA = 0;
        int rowCountAT = 0;
        int rowCountCSEV = 0;
        int rowCountCSVEM = 0;
        int rowCountCSE = 0;
        
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try{
            hb_session.getTransaction().begin();
            
            //Get missing IDs for all code-system-entity-versions
            CodeSystemEntityVersion CSEV = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, csevId);
            CodeSystemEntity CSE = CSEV.getCodeSystemEntity();
            
            boolean deleteCSE = false;
            if(CSE.getCodeSystemEntityVersions().size() == 1)
                deleteCSE = true;
            
            //Deleting code-system-concept-translation
            Query Q_codeSystemConceptTranslation_delete = hb_session.createQuery("delete from CodeSystemConceptTranslation where codeSystemConcept.codeSystemEntityVersionId=:csevId");
            Q_codeSystemConceptTranslation_delete.setParameter("csevId", csevId);
            rowCountCSCT += Q_codeSystemConceptTranslation_delete.executeUpdate();

            //Deleting code-system-concept
            Query Q_codeSystemConcept_delete = hb_session.createQuery("delete from CodeSystemConcept where codeSystemEntityVersionId=:csevId");
            Q_codeSystemConcept_delete.setParameter("csevId", csevId);
            rowCountCSC += Q_codeSystemConcept_delete.executeUpdate();

            //Deleting concept-value-set-membership
            Query Q_conceptValueSetMembership_delete = hb_session.createQuery("delete from ConceptValueSetMembership where codeSystemEntityVersionId=:csevId");
            Q_conceptValueSetMembership_delete.setParameter("csevId", csevId);
            rowCountCVSM += Q_conceptValueSetMembership_delete.executeUpdate();

            //Deleting code-system-metadata-value
            Query Q_codeSystemMetadataValue_delete = hb_session.createQuery("delete from CodeSystemMetadataValue where codeSystemEntityVersionId=:csevId");
            Q_codeSystemMetadataValue_delete.setParameter("csevId", csevId);
            rowCountCSMV += Q_codeSystemMetadataValue_delete.executeUpdate();

            //Deleting code-system-entity-version-association1
            Query Q_codeSystemEntityVersionAssociation_delete1 = hb_session.createQuery("delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId1=:csevId");
            Q_codeSystemEntityVersionAssociation_delete1.setParameter("csevId", csevId);
            rowCountCSEVA += Q_codeSystemEntityVersionAssociation_delete1.executeUpdate();

            //Deleting code-system-entity-version-association2
            Query Q_codeSystemEntityVersionAssociation_delete2 = hb_session.createQuery("delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId2=:csevId");
            Q_codeSystemEntityVersionAssociation_delete2.setParameter("csevId", csevId);
            rowCountCSEVA += Q_codeSystemEntityVersionAssociation_delete2.executeUpdate();

            //Deleting association-type
            Query q_at = hb_session.createQuery("delete from AssociationType where codeSystemEntityVersionId=:csevId");
            q_at.setParameter("csevId", csevId);
            rowCountAT += q_at.executeUpdate();

            //Deleting code-system-entity-version
            Query Q_codeSystemEntityVersion_delete = hb_session.createQuery("delete from CodeSystemEntityVersion where versionId=:csevId");
            Q_codeSystemEntityVersion_delete.setParameter("csevId", csevId);
            rowCountCSEV += Q_codeSystemEntityVersion_delete.executeUpdate();

            if(deleteCSE){
                //Deleting code-system-version-entity-membership 
                Query Q_codeSystemVersionEntityMembership_delete = hb_session.createQuery("delete from CodeSystemVersionEntityMembership where codeSystemVersionId=:csvId and codeSystemEntityId=:cseId");
                Q_codeSystemVersionEntityMembership_delete.setParameter("csvId", csvId);
                Q_codeSystemVersionEntityMembership_delete.setParameter("cseId", CSE.getId());
                rowCountCSVEM += Q_codeSystemVersionEntityMembership_delete.executeUpdate();
                
                //Deleting code-system-entity
                Query q_cse = hb_session.createQuery("delete from CodeSystemEntity where id=:cseId");
                q_cse.setParameter("cseId", CSE.getId());
                rowCountCSE += q_cse.executeUpdate();
            }
            
            result += "Code-system-concept-translation: " + rowCountCSCT + "\n";
            result += "Code-system-concept: " + rowCountCSC + "\n";
            result += "Concept-value-set-membership: " + rowCountCVSM + "\n";
            result += "Code-system-metadata-value: " + rowCountCSMV + "\n";
            result += "Code-system-entity-version-association: " + rowCountCSEVA + "\n";
            result += "Association-type: " + rowCountAT + "\n";
            result += "Code-system-entity-version: " + rowCountCSEV + "\n";
            
            if(deleteCSE){
                result += "Code-system-version-entity-membership: " + rowCountCSVEM + "\n";
                result += "Code-system-entity: " + rowCountCSE + "\n";
            }
            
            if(!hb_session.getTransaction().wasCommitted())
                hb_session.getTransaction().commit();
        }
        catch (Exception e){
            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();
            result += "Ein Fehler ist aufgetreten [0002]: " + e.getMessage();
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- deleteCSEV finished (001) ------");
        return result;
    }
}
