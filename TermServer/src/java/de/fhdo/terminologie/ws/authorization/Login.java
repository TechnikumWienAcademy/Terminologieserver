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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.types.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.types.LoginResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.ReturnType.OverallErrorCategory;
import de.fhdo.terminologie.ws.types.ReturnType.Status;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Login
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public LoginResponseType Login(LoginRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== Login gestartet ======");
        }

        // Return-Informationen anlegen
        LoginResponseType response = new LoginResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter prüfen
        if (validateParameter(parameter, response) == false)
        {
            return response; // Fehler bei den Parametern
        }

        response.setLogin(new LoginType());

        try
        {
            java.util.List<TermUser> list = null;

            // Hibernate-Block, Session öffnen
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {

                Security.checkForDeadSessions(hb_session);

                // HQL erstellen
                String hql = "select u from TermUser u";

                // Parameter dem Helper hinzufügen
                // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
                // sonst sind SQL-Injections möglich
                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getLogin() != null)
                {
                    // Hier alle Parameter aus der Cross-Reference einfügen
                    // addParameter(String Prefix, String DBField, Object Value)
                    if (parameter.getLogin().getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME))
                    {
                        String[] str = parameter.getLogin().getUsername().split(":");
                        parameterHelper.addParameter("u.", "name", str[0]);
                    }
                    else
                    {
                        parameterHelper.addParameter("u.", "name", parameter.getLogin().getUsername());
                    }

                    parameterHelper.addParameter("u.", "passw", parameter.getLogin().getPassword());
                }

                // Parameter hinzufügen (immer mit AND verbunden)
                hql += parameterHelper.getWhere("");

                // Query erstellen
                org.hibernate.Query q = hb_session.createQuery(hql);

                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q);

                // Datenbank-Aufruf durchführen
                list = (java.util.List<TermUser>) q.list();

                if (list != null && list.size() > 0
                        && performLogin(list.get(0), parameter.getLogin(), hb_session, response))
                {
                    // Login erfolgreich
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Login erfolgreich");
                }
                else
                {
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Benutzername oder Passwort ist falsch");
                }

                // Hibernate-Block wird in 'finally' geschlossen, erst danach
                // Auswertung der Daten
                // Achtung: hiernach können keine Tabellen/Daten mehr nachgeladen werden
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'Login', Hibernate: " + e.getLocalizedMessage());

                logger.error("Fehler bei 'Login', Hibernate: " + e.getLocalizedMessage());
            }
            finally
            {
                // Transaktion abschließen
                hb_session.getTransaction().commit();
                hb_session.close();
            }
        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'Login': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'Login': " + e.getLocalizedMessage());
        }

        return response;
    }

    private boolean performLogin(TermUser user_db, LoginType login,
            org.hibernate.Session hb_session, LoginResponseType response)
    {
        boolean erfolg = true;
        if (response.getLogin() == null)
        {
            response.setLogin(new LoginType());
        }

        response.getLogin().setSessionID("");

        // Passwort pruefen
        String pwHash = user_db.getPassw();

        if (!login.getPassword().equalsIgnoreCase(pwHash))
        {
            response.getReturnInfos().setMessage("Falsches Passwort!");
            erfolg = false;
        }
        else
        {
            // Passwort ist richtig

            // nun Hashwert generieren und in Tabelle mit Verbindung der UserID
            // speichern
            String newHash = "";

            UUID uuid = UUID.randomUUID();
            newHash = uuid.toString();
            if (!login.getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME))
            {
                List<Session> sessionSet = Security.checkForExistingSessions(hb_session, login, user_db);

                if (sessionSet != null && !sessionSet.isEmpty())
                {
                    for (Session session : sessionSet)
                    {

                        session.setTermUser(null);
                        hb_session.delete(session);
                    }
                }
            }
            else
            {
                List<Session> sessionSet = Security.checkForExistingKollabSessions(hb_session, login, user_db);

                if (sessionSet != null && !sessionSet.isEmpty())
                {
                    for (Session session : sessionSet)
                    {

                        session.setTermUser(null);
                        hb_session.delete(session);
                    }
                }
            }
            // prüfen, ob bereits eine Session für den User existiert

            // Neue Session hinzufügen
            Session st = new Session();
            st.setSessionId(newHash);
            st.setLastTimestamp(new java.util.Date());
            st.setTermUser(new TermUser());
            st.getTermUser().setId(user_db.getId());
            st.setIpAddress(login.getIp());

            if (login.getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME))
            {
                String[] str = login.getUsername().split(":");
                st.setCollabUsername(str[1]);
            }
            else
            {
                st.setCollabUsername(null);
            }

            logger.debug("IP-Adress (session): " + st.getIpAddress());

            hb_session.save(st);

            response.getLogin().setSessionID(newHash);
            response.getLogin().setUsername(user_db.getName());
        }

        return erfolg;
    }

    private boolean validateParameter(
            LoginRequestType Request,
            LoginResponseType Response)
    {
        boolean erfolg = true;

        LoginType login = Request.getLogin();

        if (login == null)
        {
            Response.getReturnInfos().setMessage("LoginType darf nicht NULL sein!");
            erfolg = false;
        }
        else if (login.getUsername() == null || login.getUsername().length() == 0)
        {
            Response.getReturnInfos().setMessage("Username darf nicht NULL sein!");
            erfolg = false;
        }
        else if (login.getPassword() == null || login.getPassword().length() == 0)
        {
            Response.getReturnInfos().setMessage("Passwort darf nicht NULL sein!");
            erfolg = false;
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(Status.FAILURE);
        }

        return erfolg;
    }

    public LoginResponseType checkLogin(LoginRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== check Login gestartet ======");
        }

        // Return-Informationen anlegen
        LoginResponseType response = new LoginResponseType();
        response.setReturnInfos(new ReturnType());

        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }

        logger.debug("Eingeloggt: " + loggedIn);

        if (loggedIn == false)
        {
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Benutzer ist nicht angemeldet!");
            return response;
        }
        else
        {
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Benutzer ist angemeldet!");
            return response;
        }
    }
}
