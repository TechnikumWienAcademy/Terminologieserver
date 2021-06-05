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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.types.ActualProceeding;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Months;

/**
 *
 * @author Philipp Urbauer
 */
public class ActualProceedings
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    private static final String PROCEEDING_NEW = "NEU";
    private static final String PROCEEDING_CHANGED = "GEÄNDERT";
    private static final String PROCEEDING_OBSOLETE = "OBSOLET";

    /**
     * Erstellt eine neue Domäne mit den angegebenen Parametern
     *
     * @param parameter
     * @return Antwort des Webservices
     */
    public ActualProceedingsResponseType ActualProceedings(ActualProceedingsRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== Actual Proceedings gestartet ======");
        }

        ActualProceedingsResponseType response = new ActualProceedingsResponseType();
        response.setReturnInfos(new ReturnType());
        
        //3.2.20 commented out
        /*boolean loggedIn = false;
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
        }*/
        
        List<ActualProceeding> apList = new ArrayList<ActualProceeding>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
        Integer months = 6;

        try
        {
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();

            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {

                //CodeSystemVersion
                String hqlCsv = "select distinct csv from CodeSystemVersion csv join fetch csv.codeSystem cs where cs.currentVersionId=csv.versionId";
                Query qCsv = hb_session.createQuery(hqlCsv);
                List<CodeSystemVersion> csvList = qCsv.list();

                for (CodeSystemVersion csv : csvList)
                {

                    ActualProceeding ap = new ActualProceeding();

                    ap.setTerminologieName(csv.getCodeSystem().getName());
                    ap.setTerminologieVersionName(csv.getName());
                    ap.setTerminologieType("Code System");

                    Date insertDate = csv.getInsertTimestamp();
                    Date lastChangeDate = csv.getLastChangeDate();
                    Date statusDate = csv.getStatusDate();

                    if (lastChangeDate != null)
                    {  //Ã„nderung
                        if (csv.getStatus() == 2)
                        { // Obsolet
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months)
                            {
                                apList.add(ap);
                            }
                        }
                        else
                        { // Ã„nderung
                            ap.setStatus(PROCEEDING_CHANGED);
                            ap.setLastChangeDate(sdf.format(lastChangeDate));
                            if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(lastChangeDate)).getMonths()) <= months)
                            {
                                apList.add(ap);
                            }
                        }
                    }
                    else if (csv.getStatus() == 2)
                    {
                        ap.setStatus(PROCEEDING_OBSOLETE);
                        ap.setLastChangeDate(sdf.format(statusDate));
                        if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months)
                        {
                            apList.add(ap);
                        }
                    }
                    else
                    {
                        //New
                        ap.setStatus(PROCEEDING_NEW);
                        ap.setLastChangeDate(sdf.format(insertDate));
                        if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(insertDate)).getMonths()) <= months)
                        {
                            apList.add(ap);
                        }
                    }
                }

                //ValueSetVersion
                String hqlVsv = "select distinct vsv from ValueSetVersion vsv join fetch vsv.valueSet vs where vs.currentVersionId=vsv.versionId";
                Query qVsv = hb_session.createQuery(hqlVsv);
                List<ValueSetVersion> vsvList = qVsv.list();

                for (ValueSetVersion vsv : vsvList)
                {

                    ActualProceeding ap = new ActualProceeding();

                    ap.setTerminologieName(vsv.getValueSet().getName());
                    if (vsv.getName() != null)
                    {
                        ap.setTerminologieVersionName(vsv.getName());
                    }
                    else
                    {
                        ap.setTerminologieVersionName("");
                    }
                    ap.setTerminologieType("Value Set");

                    Date insertDate = vsv.getInsertTimestamp();
                    Date lastChangeDate = vsv.getLastChangeDate();
                    Date statusDate = vsv.getStatusDate();

                    if (lastChangeDate != null)
                    {  //Ã„nderung
                        if (vsv.getStatus() == 2)
                        { // Obsolet
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months)
                            {
                                apList.add(ap);
                            }
                        }
                        else
                        { // Ã„nderung
                            ap.setStatus(PROCEEDING_CHANGED);
                            ap.setLastChangeDate(sdf.format(lastChangeDate));
                            if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(lastChangeDate)).getMonths()) <= months)
                            {
                                apList.add(ap);
                            }
                        }
                    }
                    else if (vsv.getStatus() == 2)
                    {
                        ap.setStatus(PROCEEDING_OBSOLETE);
                        ap.setLastChangeDate(sdf.format(statusDate));
                        if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months)
                        {
                            apList.add(ap);
                        }
                    }
                    else
                    {
                        //New
                        ap.setStatus(PROCEEDING_NEW);
                        ap.setLastChangeDate(sdf.format(insertDate));
                        if (Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(insertDate)).getMonths()) <= months)
                        {
                            apList.add(ap);
                        }
                    }
                }

                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                String message = "Abfrage Erfolgreich!";
                response.getReturnInfos().setMessage(message);
                response.setActualProceedings(apList);
                //hb_session.getTransaction().commit();
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                String message = "Fehler bei 'ActualProceedings', Hibernate: " + e.getLocalizedMessage();
                response.getReturnInfos().setMessage(message);
                logger.error(message);
            }
            finally
            {
                hb_session.close();
            }
        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'ActualProceedings': " + e.getLocalizedMessage());
            logger.error("Fehler bei 'ActualProceedings': " + e.getLocalizedMessage());
        }

        return response;
    }
}
