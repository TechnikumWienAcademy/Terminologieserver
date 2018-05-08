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

import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsResponseType;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnConceptAssociationTypeDetails
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ReturnConceptAssociationTypeDetailsResponseType ReturnConceptAssociationTypeDetails(ReturnConceptAssociationTypeDetailsRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ReturnConceptAssociationTypeDetails gestartet ======");
        }

        // Return-Informationen anlegen
        ReturnConceptAssociationTypeDetailsResponseType response = new ReturnConceptAssociationTypeDetailsResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter prüfen
        if (validateParameter(parameter, response) == false)
        {
            return response; // Fehler bei den Parametern
        }

        // Login-Informationen auswerten (gilt für jeden Webservice)
        boolean loggedIn = false;

        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }

        if (loggedIn == false)
        {
            // Benutzer muss für diesen Webservice eingeloggt sein
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
            return response;
        }

        try
        {
            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();

            CodeSystemEntity codeSystemEntity = null;

            try
            {
                String hql = "select distinct cse from CodeSystemEntity cse";
                hql += " join fetch cse.codeSystemEntityVersions csev";
                hql += " join fetch csev.associationTypes at";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getCodeSystemEntity() != null)
                {
                    //parameterHelper.addParameter("cse.", "id", parameter.getCodeSystemEntity().getId());

                    if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
                    {
                        CodeSystemEntityVersion vsvFilter = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];

                        parameterHelper.addParameter("csev.", "versionId", vsvFilter.getVersionId());
                    }
                }

                if (!loggedIn)
                {
                    parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                }

                // Parameter hinzufügen (immer mit AND verbunden)
                hql += parameterHelper.getWhere("");

                logger.debug("HQL: " + hql);

                // Query erstellen
                org.hibernate.Query q = hb_session.createQuery(hql);
                //Matthias: set readOnly
                q.setReadOnly(true);

                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q);

                List<CodeSystemEntity> liste = q.list();

                if (liste != null && liste.size() > 0)
                {
                    codeSystemEntity = liste.get(0);
                }

                //hb_session.getTransaction().commit();
            }
            catch (Exception e)
            {
                //hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptAssociationTypeDetails', Hibernate: " + e.getLocalizedMessage());

                logger.error("Fehler bei 'ReturnConceptAssociationTypeDetails', Hibernate: " + e.getLocalizedMessage());
            }
            finally
            {
                hb_session.close();
            }

            if (codeSystemEntity != null)
            {
                // M:N zu Vokabular
                if (codeSystemEntity.getCodeSystemVersionEntityMemberships() != null)
                {
                    Iterator<CodeSystemVersionEntityMembership> itMember = codeSystemEntity.getCodeSystemVersionEntityMemberships().iterator();

                    while (itMember.hasNext())
                    {
                        CodeSystemVersionEntityMembership member = itMember.next();

                        member.setCodeSystemEntity(null);
                        member.setCodeSystemVersion(null);
                    }
                }

                if (codeSystemEntity.getCodeSystemEntityVersions() != null)
                {
                    Iterator<CodeSystemEntityVersion> itVersions = codeSystemEntity.getCodeSystemEntityVersions().iterator();

                    while (itVersions.hasNext())
                    {
                        CodeSystemEntityVersion csev = itVersions.next();

                        csev.setCodeSystemEntity(null);
                        csev.setConceptValueSetMemberships(null);
                        csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                        csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
                        csev.setCodeSystemConcepts(null);
                        csev.setCodeSystemMetadataValues(null); // TODO
                        csev.setValueSetMetadataValues(null);

                        // AssociationTypes
                        if (csev.getAssociationTypes() != null)
                        {
                            Iterator<AssociationType> itAtypes = csev.getAssociationTypes().iterator();

                            while (itAtypes.hasNext())
                            {
                                AssociationType at = itAtypes.next();

                                at.setCodeSystemEntityVersion(null);
                                at.setCodeSystemEntityVersionAssociations(null);
                            }
                        }

                        // Properties
                        if (csev.getPropertyVersions() != null)
                        {
                            Iterator<PropertyVersion> itPropVersions = csev.getPropertyVersions().iterator();

                            while (itPropVersions.hasNext())
                            {
                                PropertyVersion propVersion = itPropVersions.next();

                                propVersion.setCodeSystemEntityVersion(null);

                                if (propVersion.getProperty() != null)
                                {
                                    propVersion.getProperty().setPropertyVersions(null);
                                }
                            }
                        }
                    }
                }

                // Liste der Response beifügen
                response.setCodeSystemEntity(codeSystemEntity);
            }

            if (codeSystemEntity == null)
            {
                response.getReturnInfos().setMessage("Zu den angegebenen IDs wurde kein AssociationType gefunden!");
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            }
            else
            {
                response.getReturnInfos().setMessage("AssociationType-Details erfolgreich gelesen");
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
            }
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);

        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptAssociationTypeDetails': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'ReturnConceptAssociationTypeDetails': " + e.getLocalizedMessage());
        }

        return response;
    }

    private boolean validateParameter(ReturnConceptAssociationTypeDetailsRequestType Request, ReturnConceptAssociationTypeDetailsResponseType Response)
    {
        boolean erfolg = true;

        if (Request.getLogin() != null)
        {
            if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
            {
                Response.getReturnInfos().setMessage(
                        "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
                erfolg = false;
            }
        }

        if (Request.getCodeSystemEntity() == null)
        {
            Response.getReturnInfos().setMessage(
                    "CodeSystemEntity darf nicht null sein!");
            erfolg = false;
        }
        else if (Request.getCodeSystemEntity().getCodeSystemEntityVersions() != null)
        {
            if (Request.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 1)
            {
                Response.getReturnInfos().setMessage(
                        "Es darf maximal eine CodeSystemEntityVersion angegeben sein!");
                erfolg = false;
            }
            else if (Request.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
            {
                CodeSystemEntityVersion vsv = (CodeSystemEntityVersion) Request.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                if (vsv.getVersionId() == null || vsv.getVersionId() <= 0)
                {
                    Response.getReturnInfos().setMessage(
                            "Es muss eine ID für die CodeSystemEntity-Version angegeben sein!");
                    erfolg = false;
                }
            }
        }
        else
        {
            /*if(Request.getCodeSystemEntity().getId() == null || Request.getCodeSystemEntity().getId() == 0)
        {
          Response.getReturnInfos().setMessage(
            "CodeSystemEntityVersion darf nicht null sein, wenn keine Entity-ID angegeben ist!");
          erfolg = false;
        }*/
            Response.getReturnInfos().setMessage(
                    "CodeSystemEntityVersion darf nicht null sein!");
            erfolg = false;
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }
}
