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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.HibernateException;

/**
 * Class for listing domain-values and creating their relationship-count.
 * These relationship-counts are created and later cleaned up by this class while calling ListDomainValues()
 * @author Robert Mützner, bachinger
 */
public class ListDomainValues
{
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Lists the domains according to the parameter
     * @param parameter the webservice-parameter
     * @return result of the webservice, all found domains which comply to the filter
     */
    public ListDomainValuesResponseType ListDomainValues(ListDomainValuesRequestType parameter)
    {
        LOGGER.info("===== ListDomainValues started =====");

        // Creating return-information
        ListDomainValuesResponseType response = new ListDomainValuesResponseType();
        response.setReturnInfos(new ReturnType());

        // Checking parameters
        if (validateParameter(parameter, response) == false)
        {
            return response; // Parameters are faulty
        }

        java.util.List<DomainValue> list;
        org.hibernate.Session hb_session = null;
        try
        {
            hb_session = HibernateUtil.getSessionFactory().openSession();

            // HQL erstellen
            String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";

            //Adding parameters to the helper
            //Always use the helper or Query.setString() or SQL-Injections are possible
            HQLParameterHelper parameterHelper = new HQLParameterHelper();

            if (parameter != null && parameter.getDomain() != null)
                parameterHelper.addParameter("", "domainId", parameter.getDomain().getDomainId());

            // Adding parameter (always connected with AND)
            String where = parameterHelper.getWhere("");
            hql += where;

            LOGGER.debug("HQL: " + hql);

            // Creating query
            org.hibernate.Query q = hb_session.createQuery(hql);
            q.setReadOnly(true);

            // Now the parameters can be applied via the helper
            parameterHelper.applyParameter(q);

            // Execute db-query
            list = q.list();

            // Evaluating results, class structure will be transformed by Jaxb into XML
            // For that to happen the unused relationships have to be set to null
            int count = 0;
            response.setDomainValues(new LinkedList<DomainValue>());

            if (list != null)
            {
                Iterator<DomainValue> iterator = list.iterator();
                LOGGER.debug("Size: " + list.size());

                while (iterator.hasNext())
                {
                    DomainValue dmv = iterator.next();

                    if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
                        continue;

                    count += applyDomainValue(dmv, response.getDomainValues(), 0);
                }

                cleanUpList(response.getDomainValues());

                response.getReturnInfos().setCount(count);

                // Giving back the response to the caller
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("DomainValues erfolgreich gelesen");
            }
        }
        catch (HibernateException e){
            // Giving back the error-response to the caller
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'DomainValues': " + e.getLocalizedMessage());

            LOGGER.error("Error at 'DomainValues': " + e.getLocalizedMessage());
        }
        finally{
            if(hb_session!=null && hb_session.isOpen())
                hb_session.close();
        }

        return response;
    }

    /**
     * Calls cleanUpEntry() for each domain-value in the list.
     * @param list the list to be cleaned
     */
    private void cleanUpList(List<DomainValue> list)
    {
        if (list == null || list.isEmpty())
            return;

        for (int i = 0; i < list.size(); ++i)
        {
            DomainValue dv = list.get(i);
            cleanUpEntry(dv);
        }
    }

    /**
     * Sets the domain-values for identification to null.
     * @param dv the domain-value which gets its identification set to null
     */
    private void cleanUpEntry(DomainValue dv)
    {
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
     * @return the count of depth ot the relationship
     */
    private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum){
        int count = sum;

        dv.setDomain(null);
        dv.setSysParamsForModifyLevel(null);
        dv.setSysParamsForValidityDomain(null);

        // Affiliated code-systems are returned without versions
        if (dv.getCodeSystems() != null)
        {
            Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

            while (iteratorCS.hasNext())
            {
                CodeSystem cs = iteratorCS.next();
                cs.setCodeSystemVersions(null);
                cs.setDomainValues(null);
                cs.setCodeSystemType(null);
                cs.setDescription(null);
                cs.setCurrentVersionId(null);
                cs.setInsertTimestamp(null);
            }
        }

        // Relationships
        boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().isEmpty());
        if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
        {
            Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

            while (iteratorDV2.hasNext())
            {
                DomainValue dv2 = iteratorDV2.next();
                count = applyDomainValue(dv2, list, sum);
            }
        }
        else
            dv.setDomainValuesForDomainValueId2(null);

        if (root)
        {
            list.add(dv);
        }
        count++;
        return count;
    }

    /**
     * Checks if the parameter for the webservice is correct.
     * @param Request the parameters which is checked
     * @param Response the response if the parameter is faulty
     * @return the boolean-value if the check was successful or not
     */
    private boolean validateParameter(ListDomainValuesRequestType Request, ListDomainValuesResponseType Response){
        boolean success = true;

        if (Request.getDomain() == null)
        {
            Response.getReturnInfos().setMessage("Es muss eine Domain angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDomainId() == null)
        {
            Response.getReturnInfos().setMessage("Es muss eine DomainId angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDomainName() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein DomainName angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDomainOid() != null)
        {
            Response.getReturnInfos().setMessage("Es darf keine DomainOid angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDescription() != null)
        {
            Response.getReturnInfos().setMessage("Es darf keine Description angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDisplayText() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein DisplayText angeben sein!");
            success = false;
        }
        else if (Request.getDomain().getIsOptional() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein IsOptional angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDefaultValue() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein DefaultValue angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDomainType() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein DomainType angegeben sein!");
            success = false;
        }
        else if (Request.getDomain().getDisplayOrder() != null)
        {
            Response.getReturnInfos().setMessage("Es darf kein DisplayOrder angegeben sein!");
            success = false;
        }
        else if ((Request.getDomain().getDomainValues() != null) && (Request.getDomain().getDomainValues().size() > 0))
        {
            Response.getReturnInfos().setMessage("Es dürfen keine Werte zu domainValue angegeben sein!");
            success = false;
        }
        
        if (success == false)
        {
          Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        return success;
    }
}
