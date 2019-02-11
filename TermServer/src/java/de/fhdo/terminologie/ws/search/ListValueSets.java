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
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * V 3.3 RDY
 * @author Robert Mützner
 * @author Warends
 */
public class ListValueSets{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Retrieves the value sets which fit the parameter from the database and then sorts them before returning them.
     * @param parameter the request parameter with information about which value set should be requested.
     * @return the list of value sets which fit the parameters.
     */
    public ListValueSetsResponseType ListValueSets(ListValueSetsRequestType parameter){
        LOGGER.info("+++++ ListValueSets started +++++");
        
        //Creating return information
        ListValueSetsResponseType response = new ListValueSetsResponseType();
        response.setReturnInfos(new ReturnType());
        
        //Checking parameters
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- ListValueSets finished (001) -----");
            return response; //Faulty parameters
        }

        //Check login (like every webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType ;
        if (parameter != null && parameter.getLogin() != null){
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }

        try{
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            java.util.List<ValueSet> VSlist = null;

            try{
                String HQL_valueSet_search = "select distinct vs from ValueSet vs";
                HQL_valueSet_search += " join fetch vs.valueSetVersions vsv";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getValueSet() != null){
                    parameterHelper.addParameter("vs.", "name", parameter.getValueSet().getName());
                    parameterHelper.addParameter("vs.", "description", parameter.getValueSet().getDescription());

                    if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() > 0){
                        ValueSetVersion VSVfilter = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];

                        parameterHelper.addParameter("vsv.", "releaseDate", VSVfilter.getReleaseDate());
                        parameterHelper.addParameter("vsv.", "statusDate", VSVfilter.getStatusDate());
                        parameterHelper.addParameter("vsv.", "previousVersionId", VSVfilter.getPreviousVersionId());
                        parameterHelper.addParameter("vsv.", "validityRange", VSVfilter.getValidityRange());
                    }
                }

                if (!loggedIn){
                    parameterHelper.addParameter("vs.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                    parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                }

                //Adding parameters, always with AND
                HQL_valueSet_search += parameterHelper.getWhere("");
                LOGGER.debug("HQL: " + HQL_valueSet_search);

                org.hibernate.Query Q_valueSet_search = hb_session.createQuery(HQL_valueSet_search);
                Q_valueSet_search.setReadOnly(true);

                //Now the parameters can be set via the helper
                parameterHelper.applyParameter(Q_valueSet_search);

                VSlist = Q_valueSet_search.list();
            }
            catch (Exception e){ 
                LOGGER.error("Error [0075]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListValueSets', Hibernate: " + e.getLocalizedMessage());
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }

            int VScount;
            if (VSlist != null){
                for (ValueSet VS : VSlist) {
                    if (VS.getValueSetVersions().size() > 1) {
                        //Sorting VS versions by their id
                        ArrayList<ValueSetVersion> VSversionList = new ArrayList(VS.getValueSetVersions());
                        Collections.sort(VSversionList, new Comparator<ValueSetVersion>(){
                            @Override
                            public int compare(ValueSetVersion vsv1, ValueSetVersion vsv2){
                                return (vsv1.getVersionId() < vsv2.getVersionId() ? -1 : (vsv1.getVersionId().equals(vsv2.getVersionId()) ? 0 : 1));
                            }
                        });
                        VS.setValueSetVersions(new LinkedHashSet<ValueSetVersion>(VSversionList));
                    }
                }
                
                VScount = VSlist.size();
                Iterator<ValueSet> VSiterator = VSlist.iterator();

                while (VSiterator.hasNext()){
                    ValueSet VS = VSiterator.next();
                    VS.setMetadataParameters(null);
                    
                    //Valueset versions
                    if (VS.getValueSetVersions() != null){   
                        Iterator<ValueSetVersion> VSViterator = VS.getValueSetVersions().iterator();
                        ValueSetVersion VSV;
                        while (VSViterator.hasNext()){
                            VSV = VSViterator.next();

                            if (!loggedIn && VSV.getStatus() != null && VSV.getStatus() != Definitions.STATUS_CODES.ACTIVE.getCode()){
                                //Not visible, has to be removed from the result
                                VSViterator.remove();
                            }
                            else{
                                //Relationships which should not be shown are set to null
                                VSV.setValueSet(null);
                                VSV.setConceptValueSetMemberships(null);
                                VS.setCurrentVersionId(VSV.getVersionId());
                            }
                        }
                    }
                }

                // Liste der Response beifügen
                response.setValueSet(VSlist);
                response.getReturnInfos().setCount(VSlist.size());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("ValueSets erfolgreich gelesen, Anzahl: " + VScount);
                response.getReturnInfos().setCount(VScount);
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0076]: " + e.getLocalizedMessage());
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListValueSets': " + e.getLocalizedMessage());
        }
        LOGGER.info("----- listValueSets finished (001) -----");
        return response;
    }

    /**
     * Checks the parameters, some are allowed to be set and are required, others must not be set.
     * @param Request the parameters to be checked.
     * @param Response information about the check is stored here.
     * @return true if the parameters passed the test, else false.
     */
    private boolean validateParameter(ListValueSetsRequestType Request, ListValueSetsResponseType Response){
        boolean passed = true;
        if (Request != null){
            if (Request.getLogin() != null){
                if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0){
                    Response.getReturnInfos().setMessage("Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
                    passed = false;
                }
            }

            if (Request.getValueSet() != null && Request.getValueSet().getValueSetVersions() != null){
                if (Request.getValueSet().getValueSetVersions().size() > 1){
                    Response.getReturnInfos().setMessage("Es darf maximal eine ValueSetVersion angegeben sein!");
                    passed = false;
                }
                else{
                    if(!Request.getValueSet().getValueSetVersions().isEmpty()){
                        ValueSetVersion VSversion = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];

                        if (VSversion.getVersionId() != null){
                            Response.getReturnInfos().setMessage("ValueSetVersion VersionId darf nicht angegeben sein!");
                            passed = false;
                        }

                        if (VSversion.getInsertTimestamp() != null){
                            Response.getReturnInfos().setMessage("ValueSetVersion InsertTimestamp darf nicht angegeben sein!");
                            passed = false;
                        }

                        if (VSversion.getPreferredLanguageId() != null){
                            Response.getReturnInfos().setMessage("ValueSetVersion PreferredLanguageId darf nicht angegeben sein!");
                            passed = false;
                        }

                        if (VSversion.getValidityRange() != null){
                            Response.getReturnInfos().setMessage("ValueSetVersion ValidityRange darf nicht angegeben sein!");
                            passed = false;
                        }

                        if (VSversion.getStatus() != null && Request.getLogin() == null){
                            Response.getReturnInfos().setMessage("ValueSetVersion StatusDate darf nicht angegeben sein!");
                            passed = false;
                        }
                    }

                    if (Request.getValueSet().getId() != null){
                        Response.getReturnInfos().setMessage("ValueSet Id darf nicht angegeben sein!");
                        passed = false;
                    }

                    if (Request.getValueSet().getCurrentVersionId() != null){
                        Response.getReturnInfos().setMessage("ValueSet CurrentVersionId darf nicht angegeben sein!");
                        passed = false;
                    }

                    if (Request.getValueSet().getStatus() != null){
                        Response.getReturnInfos().setMessage("ValueSet Status darf nicht angegeben sein!");
                        passed = false;
                    }

                    if (Request.getValueSet().getStatusDate() != null){
                        Response.getReturnInfos().setMessage("ValueSet StatusDate darf nicht angegeben sein!");
                        passed = false;
                    }
                }
            }
        }

        if (!passed){
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        return passed;
    }
}