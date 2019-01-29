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
import de.fhdo.terminologie.DomainIDs;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystemsInTaxonomy
{

  final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Lists the domains according to the filters, given by the paramter.
   * @param parameter the parameter of the webservice
   * @return the domains which comply to the filters
   */
    public ListCodeSystemsInTaxonomyResponseType ListCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter)
    {
        LOGGER.info("===== ListCodeSystemsInTaxonomy started =====");

        // Listing all code-systems
        List<CodeSystem> allCodesystemList;
        ListCodeSystemsRequestType lcsRequest = new ListCodeSystemsRequestType();
        if (parameter != null)
          lcsRequest.setLogin(parameter.getLogin());
        ListCodeSystems lcs = new ListCodeSystems();
        ListCodeSystemsResponseType lcsResponse = lcs.ListCodeSystems(lcsRequest);
        allCodesystemList = lcsResponse.getCodeSystem();

        // Creating return information
        ListCodeSystemsInTaxonomyResponseType response = new ListCodeSystemsInTaxonomyResponseType();
        response.setReturnInfos(new ReturnType());

        // Checking login, changes whether only active code-systems are displayed or not
        boolean loggedIn = false;
        boolean isAdmin = false;
        LoginInfoType loginInfoType;
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
            if (loggedIn && loginInfoType!=null && loginInfoType.getTermUser()!=null)
                isAdmin = loginInfoType.getTermUser().isAdmin();
        }
        LOGGER.debug("Is User logged in? " + loggedIn);

        org.hibernate.Session hb_session = null;
        try{
            java.util.List<DomainValue> list;
            hb_session = HibernateUtil.getSessionFactory().openSession();
            
            // HQL erstellen DABACA CHANGED
            //String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";
            //hql += " left join fetch cs.codeSystemVersions csv";
            String hql = "select distinct dmv from DomainValue dmv";
            hql += " left join fetch dmv.codeSystems cs";
            hql += " left join fetch cs.codeSystemVersions csv";
            
            // Adding parameters to the helper
            // Add parameters only through the helper or via Query.setString(), otherwise SQL-Injections are possible
            HQLParameterHelper parameterHelper = new HQLParameterHelper();

            parameterHelper.addParameter("", "domainId", DomainIDs.CODESYSTEM_TAXONOMY);
            // Adding paramter, always added with AND
            String where = parameterHelper.getWhere("");

            if (!loggedIn)
            {
              where += " and (csv.status=" + Definitions.STATUS_CODES.ACTIVE.getCode() + " or cs is null)";
            }

            hql += where;
            LOGGER.debug("HQL: " + hql);

            // Creating query
            org.hibernate.Query q = hb_session.createQuery(hql);
            q.setReadOnly(true);

            // Applying parameters (cannot be done sooner)
            parameterHelper.applyParameter(q);

            // Executing db-query
            list = q.list();
            
            // Evaluating result, since the class-structure will be transformed into 
            // a XML-structure by Jaxb the unused relationships have to be set to null
            int count = 0;
            response.setDomainValue(new LinkedList<DomainValue>());

            if (list != null){
                Iterator<DomainValue> iterator = list.iterator();
                LOGGER.debug("DomainValue list size: " + list.size());

                while (iterator.hasNext()){
                    DomainValue dmv = iterator.next();

                    if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
                        continue; // Not a root element
            
                    count += applyDomainValue(dmv, response.getDomainValue(), 0, allCodesystemList);
                }
                
                cleanUpList(response.getDomainValue());

                if (allCodesystemList.size() > 0){
                    // Adding all other code-systems
                    DomainValue otherDV = new DomainValue();
                    otherDV.setDomainCode("other");
                    otherDV.setDomainDisplay("Sonstige");
                    otherDV.setCodeSystems(new HashSet<CodeSystem>());

                    for (int i = 0; i < allCodesystemList.size(); ++i)
                        otherDV.getCodeSystems().add(allCodesystemList.get(i));

                    response.getDomainValue().add(otherDV);
                }

                response.getReturnInfos().setCount(count);

                // Forward status to caller
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("CodeSysteme erfolgreich in Taxonomie gelesen");
            }   
        }
        catch (Exception e)
        {
            // Forward error to caller
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());

            LOGGER.error("Error at 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());
        }
        finally
        {
            if(hb_session!=null && hb_session.isOpen())
                hb_session.close();
        }

        return response;
    }

    /**
     * Cleans up the provided list by calling cleanUpEntry on each entry.
     * @param list the list to be cleaned
     */
    private void cleanUpList(List<DomainValue> list){
        if (list == null || list.isEmpty())
            return;

        for (int i = 0; i < list.size(); ++i){
            DomainValue dv = list.get(i);
            cleanUpEntry(dv);
        }
    }
    
    /**
     * Cleans up a single entry by setting its domain-id to null.
     * If it has affiliated entries, they are cleaned up as well.
     * @param dv the domain-value to be cleaned
     */
    private void cleanUpEntry(DomainValue dv){
        dv.setDomainValuesForDomainValueId1(null);

        if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0){
            Iterator<DomainValue> itDV2 = dv.getDomainValuesForDomainValueId2().iterator();

            while (itDV2.hasNext()){
                DomainValue dv2 = itDV2.next();
                cleanUpEntry(dv2);
            }
        }
    }
    
    /**
     * Applies the domain-value to each code-system and domain-value so that a count can be created.
     * This count depends on the depth of the code-system or domain-value.
     * TODO check this information.
     * @param dv the domain-value which gets the value applied
     * @param list of the secondary domain-value list
     * @param sum count of depth of the relationship
     * @param allCodesystemList list of all code-systems
     * @return the count of depth ot the relationship
     */
    private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum, List<CodeSystem> allCodesystemList){
        int count = sum;

        dv.setDomain(null);
        dv.setSysParamsForModifyLevel(null);
        dv.setSysParamsForValidityDomain(null);

        // Get related code-systems with versions
        if (dv.getCodeSystems() != null){
            Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

            while (iteratorCS.hasNext()){
                CodeSystem cs = iteratorCS.next();
                cs.setDomainValues(null);
                cs.setMetadataParameters(null);

                Iterator<CodeSystemVersion> iteratorCSV = cs.getCodeSystemVersions().iterator();
                while (iteratorCSV.hasNext()){
                    CodeSystemVersion csv = iteratorCSV.next();
                    csv.setCodeSystem(null);
                    csv.setCodeSystemVersionEntityMemberships(null);
                    csv.setLicenceTypes(null);
                    csv.setLicencedUsers(null);
                }

                // Remove from list
                for (int i = 0; i < allCodesystemList.size(); ++i){
                    if (allCodesystemList.get(i).getId() == cs.getId().longValue()){
                        allCodesystemList.remove(i);
                        break;
                    }
                }
            }
        }

        // Relationships
        boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().isEmpty());
        if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0){
            Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

            while (iteratorDV2.hasNext()){
                DomainValue dv2 = iteratorDV2.next();
                count = applyDomainValue(dv2, list, sum, allCodesystemList);
            }
        }
        else
            dv.setDomainValuesForDomainValueId2(null);

        if (root)
            list.add(dv);
    
        count++;
        return count;
    }
}
