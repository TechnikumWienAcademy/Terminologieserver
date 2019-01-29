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
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration.types.CreateDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Session;

/**
 *
 * @author Bernhard Rimatzki
 */
public class CreateDomain
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Erstellt eine neue Domäne mit den angegebenen Parametern
     * 
     * @param parameter
     * @return Antwort des Webservices
     */
    public CreateDomainResponseType CreateDomain(CreateDomainRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== CreateDomain gestartet ======");
        }

        // Return-Informationen anlegen
        CreateDomainResponseType response = new CreateDomainResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter prüfen
        if (!validateParameter(parameter, response))
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
            
            if(loggedIn){
            
                if(loginInfoType.getTermUser().isAdmin()){
                    loggedIn = true;
                }else{
                    loggedIn = false;
                }
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Benutzer ist eingeloggt: " + loggedIn);

        //Todo loggedIn = true rausnehmen, nachdem der Service ausgiebig getestet ist.
        loggedIn = true;
        if (loggedIn == false)
        {
            // Benutzer muss für diesen Webservice eingeloggt sein
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
            return response;
        }
        
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try
        {
            Domain d_return = new Domain();

            // Hibernate-Block, Session öffnen
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            // Domain und DomainValue zum Speichern vorbereiten
            Domain d = parameter.getDomain();
            Set<DomainValue> dv = parameter.getDomain().getDomainValues();

            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                // Domain in der Datenbank speichern
                d.setDomainValues(null);

                hb_session.save(d);

                Iterator<DomainValue> idv = dv.iterator();
                while (idv.hasNext())
                {
                    DomainValue dvItem = idv.next();

                    dvItem.setDomain(new Domain());
                    dvItem.getDomain().setDomainId(d.getDomainId());

                    hb_session.save(dvItem);
                }

                // Antwort setzen (neue ID)
                d_return.setDomainId(d.getDomainId());
                response.setDomain(d_return);

            } catch (Exception e)
            {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                String message = "Fehler bei 'CreateDomain', Hibernate: " + e.getLocalizedMessage();
                response.getReturnInfos().setMessage(message);

                logger.error(message);
            } finally
            {
                // Transaktion abschließen
                if (d_return.getDomainId() > 0)
                {
                    hb_session.getTransaction().commit();

                } else
                {
                    // Ã„nderungen nicht erfolgreich
                    logger.warn("[CreateDomain.java] Ã„nderungen nicht erfolgreich, d_return.id: "
                            + d_return.getDomainId());

                    hb_session.getTransaction().rollback();
                }
                hb_session.close();
            }

        } catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());
        }
				finally {
					hb_session.close();
				}

        return response;
    }

    /**
     * Prüft die Parameter anhand der Cross-Reference
     * 
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
     */
    private boolean validateParameter(CreateDomainRequestType Request,
            CreateDomainResponseType Response)
    {
        boolean erfolg = true;

        Domain domain = Request.getDomain();
        if (domain == null)
        {
            Response.getReturnInfos().setMessage("Domain darf nicht NULL sein!");
            erfolg = false;
        } else
        {
            if (domain.getDomainName() == null || domain.getDomainName().length() == 0)
            {
                Response.getReturnInfos().setMessage(
                        "Es muss ein Name für die Domain angegeben sein!");
                erfolg = false;
            }

            Set<DomainValue> dvSet = domain.getDomainValues();
            if (dvSet != null)
            {
                Iterator idv = dvSet.iterator();
                while (idv.hasNext())
                {
                    DomainValue dv = (DomainValue) idv.next();
                    if (dv.getDomainCode() == null || dv.getDomainCode().length() == 0)
                    {
                        Response.getReturnInfos().setMessage(
                                "Es muss ein Code für den Domain-Value angegeben sein!");
                        erfolg = false;
                    }
                    if (dv.getDomainDisplay() != null && dv.getDomainDisplay().length() == 0)
                    {
                        Response.getReturnInfos().setMessage(
                                "Es muss ein Displayname für den Domain-Value angegeben sein!");
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
