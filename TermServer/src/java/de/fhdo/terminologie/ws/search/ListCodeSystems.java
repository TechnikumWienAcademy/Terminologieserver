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
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 *
 * @author Robert Mützner
 */
public class ListCodeSystems{

    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ListCodeSystemsResponseType ListCodeSystems(ListCodeSystemsRequestType parameter){
        LOGGER.info("+++++ ListCodeSystems started +++++");
        
        //Creating return informationen
        ListCodeSystemsResponseType response = new ListCodeSystemsResponseType();
        response.setReturnInfos(new ReturnType());

        //Checking login information (does every webservice)
        boolean loggedIn = false;
        boolean isAdmin = false;
        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null){
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            if (loginInfoType != null){
                loggedIn = true;
                isAdmin = loginInfoType.getTermUser().isIsAdmin();
            }
        }
        
        try{
            java.util.List<CodeSystem> CSlist = null;

            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

            try{
                //Creating HQL
                String HQL_CS_select = "select distinct cs from CodeSystem cs";
                HQL_CS_select += " join fetch cs.codeSystemVersions csv";

                if (loginInfoType != null)
                    HQL_CS_select += " left outer join csv.licencedUsers lu";
                
                //Adding parameters via helper or manually (Query.setString()), otherwise SQL-Injections  are possible
                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getCodeSystem() != null){
                    //Adding all parameters from the cross-referense: addParameter(String Prefix, String DBField, Object Value)
                    parameterHelper.addParameter("cs.", "id", parameter.getCodeSystem().getId());
                    parameterHelper.addParameter("cs.", "name", parameter.getCodeSystem().getName());
                    parameterHelper.addParameter("cs.", "description", parameter.getCodeSystem().getDescription());
                    parameterHelper.addParameter("cs.", "insertTimestamp", parameter.getCodeSystem().getInsertTimestamp()); //Like '2011-09-26T15:40:00'

                    if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0){
                        CodeSystemVersion CSversion = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

                        parameterHelper.addParameter("csv.", "description", CSversion.getDescription());
                        parameterHelper.addParameter("csv.", "expiredDate", CSversion.getExpirationDate());
                        parameterHelper.addParameter("csv.", "insertTimestamp", CSversion.getInsertTimestamp());
                        parameterHelper.addParameter("csv.", "description", CSversion.getLicenceHolder());
                        parameterHelper.addParameter("csv.", "name", CSversion.getName());
                        parameterHelper.addParameter("csv.", "oid", CSversion.getOid());
                        parameterHelper.addParameter("csv.", "preferredLanguageId", CSversion.getPreferredLanguageId());
                        parameterHelper.addParameter("csv.", "releaseDate", CSversion.getReleaseDate());
                        parameterHelper.addParameter("csv.", "source", CSversion.getSource());
                        parameterHelper.addParameter("csv.", "underLicence", CSversion.getUnderLicence());
                        parameterHelper.addParameter("csv.", "validityRange", CSversion.getValidityRange());

                        if (loginInfoType != null)
                            parameterHelper.addParameter("csv.", "status", CSversion.getStatus());
                    }
                }

                //Only listing active vocabulary if not admin
                if (!isAdmin)
                    parameterHelper.addParameter("csv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());

                //Listing only unlicenced vocabulary if not logged in
                if (loginInfoType == null)
                    parameterHelper.addParameter("csv.", "underLicence", 0);

                //Adding parameters (connected through AND)
                String where = parameterHelper.getWhere("");
                HQL_CS_select += where;

                if (loginInfoType!=null){
                    //Checking for valid licence, has to be added manually, too complex for helper
                    if (where.length() > 2)
                        HQL_CS_select += " AND ";
                    else
                        HQL_CS_select += " WHERE ";

                    HQL_CS_select += " (csv.underLicence = 0 OR ";
                    HQL_CS_select += " (lu.validFrom < '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
                    HQL_CS_select += " AND lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
                    HQL_CS_select += " AND lu.id.codeSystemVersionId=csv.versionId";
                    HQL_CS_select += " AND lu.id.userId=" + loginInfoType.getTermUser().getId();
                    HQL_CS_select += " ))";
                }
                HQL_CS_select += " ORDER BY cs.name, csv.name";

                //Creating query
                org.hibernate.Query Q_CS_select = hb_session.createQuery(HQL_CS_select);
                Q_CS_select.setReadOnly(true);

                //Parameters can be set now via helper
                parameterHelper.applyParameter(Q_CS_select);

                //Execute query
                CSlist = (java.util.List<CodeSystem>) Q_CS_select.list();
            }
            catch (Exception ex){
                LOGGER.error("Error [0087]", ex);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems', Hibernate: " + ex.getLocalizedMessage());
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }

            //Later the class structure is transformed by Jaxb, so the unused relationships have to be set to null
            if (CSlist != null){
                for (CodeSystem codeSystem : CSlist) {
                    if (codeSystem.getCodeSystemVersions().size() > 1) {
                        //Sorting CS versions by their id
                        ArrayList<CodeSystemVersion> CSversions = new ArrayList(codeSystem.getCodeSystemVersions());
                        Collections.sort(CSversions, new Comparator<CodeSystemVersion>(){
                            @Override
                            public int compare(CodeSystemVersion csv1, CodeSystemVersion csv2){
                                return (csv1.getVersionId() < csv2.getVersionId() ? -1 : (csv1.getVersionId().equals(csv2.getVersionId()) ? 0 : 1));
                            }
                        });
                        codeSystem.setCodeSystemVersions(new LinkedHashSet<CodeSystemVersion>(CSversions));
                    }
                }
                
                Iterator<CodeSystem> CSlistIterator = CSlist.iterator();
                
                while (CSlistIterator.hasNext()){
                    CodeSystem CS = CSlistIterator.next();

                    if (CS.getCodeSystemVersions() != null){
                        Iterator<CodeSystemVersion> CSversionsIterator = CS.getCodeSystemVersions().iterator();

                        while (CSversionsIterator.hasNext()){
                            CodeSystemVersion CSV = CSversionsIterator.next();

                            //Set values to null which should not be shown
                            if (!loggedIn && CSV.getStatus() != null && CSV.getStatus() != Definitions.STATUS_CODES.ACTIVE.getCode())
                                CSversionsIterator.remove();
                            else{
                                CSV.setLicenceTypes(null);
                                CSV.setLicencedUsers(null);
                                CSV.setCodeSystemVersionEntityMemberships(null);
                                CSV.setCodeSystem(null);
                                CS.setCurrentVersionId(CSV.getVersionId());
                            }
                        }
                    }
                    
                    CS.setDomainValues(null);  //Not returning domains
                    CS.setMetadataParameters(null);

                    //Adding cleaned list to response
                    response.setCodeSystem(CSlist);
                    response.getReturnInfos().setCount(CSlist.size());
                }

                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("CodeSysteme erfolgreich gelesen");
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0088]", ex);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems': " + ex.getLocalizedMessage());
        }

        LOGGER.info("----- ListCodeSystems finished (001) -----");
        return response;
    }
}
