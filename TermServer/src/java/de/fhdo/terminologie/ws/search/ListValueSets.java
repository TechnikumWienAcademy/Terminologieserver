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
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) / warends
 */
public class ListValueSets
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ListValueSetsResponseType ListValueSets(ListValueSetsRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== ListValueSets gestartet ======");
        }

        // Return-Informationen anlegen
        ListValueSetsResponseType response = new ListValueSetsResponseType();
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

        try
        {
            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();

            java.util.List<ValueSet> liste = null;

            try
            {
                String hql = "select distinct vs from ValueSet vs";
                hql += " join fetch vs.valueSetVersions vsv";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getValueSet() != null)
                {
                    parameterHelper.addParameter("vs.", "name", parameter.getValueSet().getName());
                    parameterHelper.addParameter("vs.", "description", parameter.getValueSet().getDescription());

                    if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() > 0)
                    {
                        ValueSetVersion vsvFilter = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];

                        parameterHelper.addParameter("vsv.", "releaseDate", vsvFilter.getReleaseDate());
                        parameterHelper.addParameter("vsv.", "statusDate", vsvFilter.getStatusDate());
                        parameterHelper.addParameter("vsv.", "previousVersionId", vsvFilter.getPreviousVersionId());
                        //parameterHelper.addParameter("vsv.", "status", vsvFilter.getStatus());
                        parameterHelper.addParameter("vsv.", "validityRange", vsvFilter.getValidityRange());
                    }
                }

                if (loggedIn == false)
                {
                    parameterHelper.addParameter("vs.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                    parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
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

                liste = q.list();
                //hb_session.getTransaction().commit();
            }
            catch (Exception e)
            {
                //hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'ListValueSets', Hibernate: " + e.getLocalizedMessage());

                logger.error("Fehler bei 'ListValueSets', Hibernate: " + e.getLocalizedMessage());
            }
            finally
            {
                hb_session.close();
            }

            int anzahl = 0;
            if (liste != null)
            {
                for(int i = 0; i< liste.size(); i++)
                {
                    if(liste.get(i).getValueSetVersions().size() > 1)
                    {
                         //sorting VS Versions by their id
                        ArrayList<ValueSetVersion> list = new ArrayList(liste.get(i).getValueSetVersions());
                        Collections.sort(list, new Comparator<ValueSetVersion>(){
                            public int compare(ValueSetVersion vsv1, ValueSetVersion vsv2)
                            {
                                return (vsv1.getVersionId() < vsv2.getVersionId() ? -1 : (vsv1.getVersionId().equals(vsv2.getVersionId()) ? 0 : 1));
                            }
                        });
                        
                        liste.get(i).setValueSetVersions(new LinkedHashSet<ValueSetVersion>(list));
                    }
                }
                
                anzahl = liste.size();
                Iterator<ValueSet> itVS = liste.iterator();

                while (itVS.hasNext())
                {
                    ValueSet vs = itVS.next();
                    vs.setMetadataParameters(null);
//          vs.setValueSetVersions(null);
//          vs.setDescription(null);
//          vs.setStatus(null);
//          vs.setStatusDate(null);

                    //Matthias: check if current Version is member of codeSystemVersion --> Status ==1
                    boolean validCurrentVersion = false;

                    // ValueSetVersions
                    if (vs.getValueSetVersions() != null)
                    {   
                        Iterator<ValueSetVersion> itVSV = vs.getValueSetVersions().iterator();
                        ValueSetVersion vsv;
                        while (itVSV.hasNext())
                        {
                            vsv = itVSV.next();

                            if (!loggedIn && vsv.getStatus() != null && vsv.getStatus().intValue() != Definitions.STATUS_CODES.ACTIVE.getCode())
                            {
                                // Nicht sichtbar, also von der Ergebnismenge entfernen
                                itVSV.remove();
                            }
                            else
                            {
                                // Nicht anzuzeigende Beziehungen null setzen
                                vsv.setValueSet(null);
                                vsv.setConceptValueSetMemberships(null);
                                vs.setCurrentVersionId(vsv.getVersionId());
                            }
                            
                        }

                        //parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
                    }
                }

                // Liste der Response beifügen
                response.setValueSet(liste);
                response.getReturnInfos().setCount(liste.size());
                
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("ValueSets erfolgreich gelesen, Anzahl: " + anzahl);
                response.getReturnInfos().setCount(anzahl);
            }

        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ListValueSets': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'ListValueSets': " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return response;
    }

    private boolean validateParameter(ListValueSetsRequestType Request, ListValueSetsResponseType Response)
    {
        boolean erfolg = true;
        if (Request != null)
        {
            //DABACA TODO SESSION ID MUSS IN DEN PARAMETER EINGEBAUT WERDEN NACHDEM ER VOM THREAD
            //NICHT MEHR ANGESPROCHEN WERDEN KANN BEI DER FREIGABE
            /*
            if (Request.getLogin() != null)
            {
                if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
                {
                    Response.getReturnInfos().setMessage(
                            "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
                    erfolg = false;
                }
            }*/

            if (Request.getValueSet() != null && Request.getValueSet().getValueSetVersions() != null)
            {
                if (Request.getValueSet().getValueSetVersions().size() > 1)
                {
                    Response.getReturnInfos().setMessage(
                            "Es darf maximal eine ValueSetVersion angegeben sein!");
                    erfolg = false;

                }
                else
                {
                    if (Request.getValueSet().getValueSetVersions().size() != 0)
                    {
                        ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];

                        // folgende Parameter dürfen nicht angegeben sein:
                        if (vsv.getVersionId() != null)
                        {
                            Response.getReturnInfos().setMessage(
                                    "ValueSetVersion VersionId darf nicht angegeben sein!");
                            erfolg = false;
                        }

                        if (vsv.getInsertTimestamp() != null)
                        {
                            Response.getReturnInfos().setMessage(
                                    "ValueSetVersion InsertTimestamp darf nicht angegeben sein!");
                            erfolg = false;
                        }

                        if (vsv.getPreferredLanguageId() != null)
                        {
                            Response.getReturnInfos().setMessage(
                                    "ValueSetVersion PreferredLanguageId darf nicht angegeben sein!");
                            erfolg = false;
                        }

                        if (vsv.getValidityRange() != null)
                        {
                            Response.getReturnInfos().setMessage(
                                    "ValueSetVersion ValidityRange darf nicht angegeben sein!");
                            erfolg = false;
                        }

                        if ((vsv.getStatus() != null) && (Request.getLogin() == null))
                        {
                            Response.getReturnInfos().setMessage(
                                    "ValueSetVersion StatusDate darf nicht angegeben sein!");
                            erfolg = false;
                        }

                    }

                    if (Request.getValueSet().getId() != null)
                    {
                        Response.getReturnInfos().setMessage(
                                "ValueSet Id darf nicht angegeben sein!");
                        erfolg = false;
                    }

                    if (Request.getValueSet().getCurrentVersionId() != null)
                    {
                        Response.getReturnInfos().setMessage(
                                "ValueSet CurrentVersionId darf nicht angegeben sein!");
                        erfolg = false;
                    }

                    if (Request.getValueSet().getStatus() != null)
                    {
                        Response.getReturnInfos().setMessage(
                                "ValueSet Status darf nicht angegeben sein!");
                        erfolg = false;
                    }

                    if (Request.getValueSet().getStatusDate() != null)
                    {
                        Response.getReturnInfos().setMessage(
                                "ValueSet StatusDate darf nicht angegeben sein!");
                        erfolg = false;
                    }

                }
            }
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
    }
}
