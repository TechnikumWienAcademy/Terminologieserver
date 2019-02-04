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
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import org.hibernate.HibernateException;

/**
 * V 3.3 RDY
 * @author Robert Mützner
 */
public class ReturnCodeSystemDetails{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Liefert alle Informationen zu einem Vokabular
     *
     * @param parameter CodeSystem(Vokabular), VersionId
     * @return Ergebnis des Webservices, gibt Details des angegebenen CodeSystems
     * und der angegebenen VersionId zurück; wurde keine VersionId angegeben
     * werden Details zu allen Versionen ausgegeben
     * TODO
    */
    public ReturnCodeSystemDetailsResponseType ReturnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter){
        LOGGER.info("+++++ ReturnCodeSystemDetails started +++++");

        //Creating return information
        ReturnCodeSystemDetailsResponseType response = new ReturnCodeSystemDetailsResponseType();
        response.setReturnInfos(new ReturnType());
    
        if (validateParameter(parameter, response) == false){
            LOGGER.info("----- ReturnCodeSystemDetails finished (001) -----");
            return response; //Faulty parameters
        }

        //Checking login (does every webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null){
            //TODO check class
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }
    
        LOGGER.debug("User is logged in? " + loggedIn);

        try{
            java.util.List<CodeSystem> CSlist;
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            
            try{//Try-catch for hibernate errors
                
                String HQL_codeSystem_search = "select distinct cs from CodeSystem cs";
                HQL_codeSystem_search += " join fetch cs.codeSystemVersions csv";
                if (loggedIn)
                    HQL_codeSystem_search += " left outer join csv.licencedUsers lu";

                //Adding parameters to the helper, always use the helper or add manually via Query.setStrin(), otherwise SQL-injections are possible
                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getCodeSystem() != null){
                    //Adding parameters from the cross reference via addParameter(String Prefix, String DBField, Object Value)
                    parameterHelper.addParameter("cs.", "id", parameter.getCodeSystem().getId());

                    if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0){
                        CodeSystemVersion CSversion = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
                        parameterHelper.addParameter("csv.", "versionId", CSversion.getVersionId());
                    }
                }

                if (loggedIn == false){
                    //Without login only active codes and codes without licence will be used
                    parameterHelper.addParameter("csv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                    parameterHelper.addParameter("csv.", "underLicence", 0);
                }

                //Adding parameters (connected through AND)
                HQL_codeSystem_search += parameterHelper.getWhere("");

                if (loggedIn){
                    //Checking for valid licence
                    //Has to be done manually since it is too complex for the helper
                    HQL_codeSystem_search += " AND ";
                    HQL_codeSystem_search += " (csv.underLicence = 0 OR ";
                    HQL_codeSystem_search += " (lu.validFrom < '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
                    HQL_codeSystem_search += " AND lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
                    HQL_codeSystem_search += " AND lu.id.codeSystemVersionId=csv.versionId";
                    HQL_codeSystem_search += " AND lu.id.userId=" + loginInfoType.getTermUser().getId();
                    HQL_codeSystem_search += " ))";
                }
        
                LOGGER.debug("HQL: " + HQL_codeSystem_search);

                //Creating query
                org.hibernate.Query Q_codeSystem_search = hb_session.createQuery(HQL_codeSystem_search);
                Q_codeSystem_search.setReadOnly(true);

                //Parameters can be set now (via helper)
                parameterHelper.applyParameter(Q_codeSystem_search);

                //Executing database query
                CSlist = (java.util.List<CodeSystem>) Q_codeSystem_search.list();

                if (CSlist != null){
                    Iterator<CodeSystem> CSlistIterator = CSlist.iterator();

                    if (CSlistIterator.hasNext()){
                        CodeSystem CS = CSlistIterator.next();

                        if (CS.getCodeSystemVersions() != null){
                            Iterator<CodeSystemVersion> singleCSversionsIterator = CS.getCodeSystemVersions().iterator();

                            while (singleCSversionsIterator.hasNext()){
                                CodeSystemVersion CSV = singleCSversionsIterator.next();

                                //Relationships which should not be shown are set null
                                if (CSV.getLicenceTypes() != null){
                                    Iterator<LicenceType> singleCSVlicenceIterator = CSV.getLicenceTypes().iterator();
                                    while (singleCSVlicenceIterator.hasNext()){
                                        LicenceType licenceType = singleCSVlicenceIterator.next();
                                        licenceType.setCodeSystemVersion(null);
                                        licenceType.setLicencedUsers(null);
                                    }
                                }

                                CSV.setLicencedUsers(null);
                                CSV.setCodeSystemVersionEntityMemberships(null);
                                CSV.setCodeSystem(null);
                            }
                        }

                        for (MetadataParameter metadataParameter : CS.getMetadataParameters()){
                            metadataParameter.setCodeSystemMetadataValues(null);
                            metadataParameter.setCodeSystem(null);
                            metadataParameter.setValueSet(null);
                            metadataParameter.setValueSetMetadataValues(null);
                        }

                        CS.setDomainValues(null);

                        //Adding cleaned list to the response
                        response.setCodeSystem(CS);

                        //Setting response infos
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setMessage("CodeSystemeDetails erfolgreich gelesen");
                        response.getReturnInfos().setCount(1);
                    }
                    else{
                        //Setting response infos
                        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setMessage("Kein CodeSysteme gefunden. Bitte beachten Sie, dass Sie für Codesysteme, welche einen anderen Status als 1 haben, am Terminologieserver angemeldet sein müssen.");
                        response.getReturnInfos().setCount(0);
                    }
                }
            }
            catch (Exception e){
                LOGGER.error("Error [0064]: " + e.getLocalizedMessage());
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ReturnCodeSystemDetails', Hibernate: " + e.getLocalizedMessage());
            }
            finally{
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
        catch (HibernateException e){
            LOGGER.error("Error [0065]: " + e.getLocalizedMessage());
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems': " + e.getLocalizedMessage());
        }
        
        LOGGER.info("----- ReturnCodeSystemDetails finished (002) -----");
        return response;
    }

    /**
     * TODO
     * @param Request
     * @param Response
     * @return 
     */
    private boolean validateParameter(ReturnCodeSystemDetailsRequestType Request, ReturnCodeSystemDetailsResponseType Response){
        boolean passed = true;

        if(Request.getLogin() != null)
            if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0){
                Response.getReturnInfos().setMessage("Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist.");
                passed = false;
            }
        
        if (Request.getCodeSystem() == null){
            Response.getReturnInfos().setMessage("Es muss ein CodeSystem(Vokabular) angegeben sein!");
            passed = false;
        }

        if (Request.getCodeSystem() != null)
            if (Request.getCodeSystem().getId() == null || Request.getCodeSystem().getId() <= 0){
                Response.getReturnInfos().setMessage("Es muss eine ID für das CodeSystem (Vokabular) angegeben sein.");
                passed = false;
            }
        else if (Request.getCodeSystem() != null && Request.getCodeSystem().getCodeSystemVersions() != null){
            if (Request.getCodeSystem().getCodeSystemVersions().size() > 1){
                Response.getReturnInfos().setMessage("Es darf maximal eine CodeSystemVersion angegeben sein!");
                passed = false;
            }
            else{
                if (!Request.getCodeSystem().getCodeSystemVersions().isEmpty()){
                    CodeSystemVersion csv = (CodeSystemVersion) Request.getCodeSystem().getCodeSystemVersions().toArray()[0];
                    if (csv.getVersionId() == null || csv.getVersionId() <= 0){
                        Response.getReturnInfos().setMessage("Es muss eine ID für die CodeSystem-Version angegeben sein!");
                        passed = false;
                    }
                    if (csv.getPreviousVersionId() != null){
                        Response.getReturnInfos().setMessage("PreviousVersionId darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getName() != null){
                        Response.getReturnInfos().setMessage("Name darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getStatus() != null){
                        Response.getReturnInfos().setMessage("Status darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getStatusDate() != null){
                      Response.getReturnInfos().setMessage("StautsDate darf nicht angegeben sein!");
                      passed = false;
                    }
                    if (csv.getReleaseDate() != null){
                        Response.getReturnInfos().setMessage("ReleaseDate darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getExpirationDate() != null){
                        Response.getReturnInfos().setMessage("ExpirationDate darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getSource() != null){
                        Response.getReturnInfos().setMessage("Source darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getDescription() != null){
                        Response.getReturnInfos().setMessage("Description darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getPreferredLanguageId() != null){
                        Response.getReturnInfos().setMessage("PreferredLanguageId darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getValidityRange() != null){
                        Response.getReturnInfos().setMessage("ValidityRange darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getOid() != null){
                        Response.getReturnInfos().setMessage("Oid darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getLicenceHolder() != null){
                        Response.getReturnInfos().setMessage("LicenceHolde darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getUnderLicence() != null){
                        Response.getReturnInfos().setMessage("UnderLicence darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getInsertTimestamp() != null){
                        Response.getReturnInfos().setMessage("InsertTimestamp darf nicht angegeben sein!");
                        passed = false;
                    }
                    if (csv.getLicenceTypes() != null && csv.getLicenceTypes().size() > 0){
                        Response.getReturnInfos().setMessage("Es darf keine Licence angegeben sein!");
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
