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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.idp.authorizationIDP.AuthorizationIDP;
import de.fhdo.terminologie.ws.idp.authorizationIDP.AuthorizationIDP_Service;
import de.fhdo.terminologie.ws.idp.authorizationIDP.GetLoginInfosResponse;
import de.fhdo.terminologie.ws.idp.authorizationIDP.LoginRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.Status;

import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginHelper
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    // Singleton-Muster
    private static LoginHelper instance;

    /**
     * @return the instance
     */
    public static LoginHelper getInstance()
    {
        if (instance == null)
        {
            instance = new LoginHelper();
        }
        return instance;
    }
    private HashMap<String, LoginInfoType> userMap;

    public LoginHelper()
    {
        userMap = new HashMap<String, LoginInfoType>();
    }

    public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired)
    {
        return doLogin(login, returnType, loginRequired, null);
    }

    public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired, org.hibernate.Session hb_session)
    {
        boolean loggedIn = false;

        if (login != null && returnType != null)
        {
            LoginInfoType loginInfoType = null;
            loginInfoType = LoginHelper.getInstance().getLoginInfos(login, hb_session);
            loggedIn = loginInfoType != null;

            if (logger.isDebugEnabled())
            {
                logger.debug("Benutzer ist eingeloggt: " + loggedIn);
            }
        }

        // Statusmeldung
        if (loggedIn == false && loginRequired)
        {
            returnType.setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            returnType.setStatus(ReturnType.Status.OK);
            returnType.setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
        }
        return loggedIn;
    }

    public LoginInfoType getLoginInfos(LoginType Login)
    {
        return getLoginInfos(Login, null);
    }

    /**
     * Überprüft anhand der Session-ID, ob der Benutzer angemeldet ist und ob
     * die Session noch gültig ist-
     *
     * @param Login LoginType mit der Session-ID
     * @return LoginInfoType bei Erfolg, sonst null
     */
    public LoginInfoType getLoginInfos(LoginType Login, org.hibernate.Session session)
    {
        if (Login == null || Login.getSessionID() == null || Login.getSessionID().length() == 0)
        {
            logger.debug("Keine Session-ID angegeben!");
            return null;
        }
        
        LoginRequestType request = new LoginRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.idp.authorizationIDP.LoginType());
        logger.info("requested session id: " + Login.getSessionID());
        request.getLogin().setSessionID(Login.getSessionID());
        GetLoginInfosResponse.Return loginInfos = null;
        try
        {
            //AuthorizationIDP_Service idpService = new AuthorizationIDP_Service();
            AuthorizationIDP port = WebServiceUrlHelper.getInstance().getAuthorizationIdpServicePort();
            logger.info("WS Endpoint: " + ((BindingProvider) port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
            loginInfos = port.getLoginInfos(request);

        }
        catch (Exception e)
        {
            System.err.println(e);
        }

        if (loginInfos.getReturnInfos().getStatus().equals(Status.OK))
        {
            LoginInfoType loginInfoType = new LoginInfoType();
            //loginInfoType.setLastTimestamp(loginInfos.getReturnInfos().getLastTimeStamp());
            //loginInfoType.setLastTimestamp(s_session.getLastTimestamp());
            loginInfoType.setLastIP(loginInfos.getReturnInfos().getLastIP());
            //loginInfoType.setLastIP(s_session.getIpAddress());

            TermUser termUser = new TermUser();
            //termUser.setActivationTime(loginInfos.getReturnInfos().getTermUser().getActivationTime());
            termUser.setId(loginInfos.getReturnInfos().getTermUser().getId());
            termUser.setIsAdmin(loginInfos.getReturnInfos().getTermUser().isIsAdmin());
            termUser.setName(loginInfos.getReturnInfos().getTermUser().getName());
            loginInfoType.setTermUser(termUser);

            LoginType loginType = new LoginType();
            loginType.setUsername(loginInfos.getReturnInfos().getTermUser().getName());
            loginType.setSessionID(Login.getSessionID());

            loginInfoType.setLogin(loginType);
            //TODO map relevant info
//			loginInfoType.getTermUser().setName();
//			loginInfoType.setTermUser(s_session.getTermUser());
//			loginInfoType.getTermUser().setIsAdmin(s_session.getTermUser().isIsAdmin());
//			loginInfoType.setLogin(new LoginType());
//			loginInfoType.getLogin().setUsername(s_session.getTermUser().getName());
//			loginInfoType.getLogin().setSessionID(s_session.getSessionId());
            return loginInfoType;
        }

        LoginInfoType loginInfoType = null;

        boolean createHibernateSession = (session == null);

        if (logger.isDebugEnabled())
        {
            logger.debug("Überprüfe Session...");
            logger.debug("createHibernateSession: " + createHibernateSession);
        }

        if (Login == null || Login.getSessionID() == null || Login.getSessionID().length() == 0)
        {
            logger.debug("Keine Session-ID angegeben!");

            return null;
        }

        long session_timeout = 30 * 60000; // 30 Minuten, TODO aus DB lesen

        // Map vewenden (damit nicht für jede Aktion ein Datenbankaufruf stattfindet)
        if (userMap.containsKey(Login.getSessionID()))
        {
            //LoginInfoType sessionInfo = userMap.get(Login.getSessionID());
            loginInfoType = userMap.get(Login.getSessionID());

            // IP überprüfen (nur, falls angegeben)
            //logger.debug("List-IP: " + loginInfoType.getLastIP());
            if (Login.getIp() != null && Login.getIp().length() > 0)
            {
                if (!(loginInfoType.getLastIP() != null
                        && loginInfoType.getLastIP().equals(Login.getIp())))
                {
                    // IP stimmt nicht überein
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("IP stimmt nicht überein (" + Login.getIp() + ")!");
                    }

                    return null;
                }
            }

            // Timeout überprüfen
            long now = new java.util.Date().getTime();
            long timestamp = loginInfoType.getLastTimestamp().getTime();

            if (now - session_timeout < timestamp)
            {
                // OK
                // Zeitstempel aktualisieren
                loginInfoType.setLastTimestamp(new Date());
                userMap.put(Login.getSessionID(), loginInfoType);

                // Logintype zurückgeben (alles in Ordnung)
                return loginInfoType;
            }
            else // Timestamp abgelaufen
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Zeitstempel abgelaufen! (wird im Debug-Modus ignoriert)");

                    //TODO: Das hier kann später weider raus; dient nur dazu, dass im DebugModus der Zeitstempel nicht ablaufen kann
                    // Logintype zurückgeben (Nur der Zeitstempel ist abgelaufen, das ist aber egal im DebugModus)        
                    // Zeitstempel aktualisieren
                    loginInfoType.setLastTimestamp(new Date());
                    userMap.put(Login.getSessionID(), loginInfoType);

                    // Logintype zurückgeben (alles in Ordnung)
                    return loginInfoType;
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            logger.debug("Session-ID nicht in userMap vorhanden");
        }

        // Session aus DB lesen und in Map speichern
        // Hibernate-Block, Session öffnen
        //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        //org.hibernate.Transaction tx = hb_session.beginTransaction();
        org.hibernate.Session hb_session = null;
        //org.hibernate.Transaction tx = null;

        if (createHibernateSession)
        {
            hb_session = HibernateUtil.getSessionFactory().openSession();
            //tx = hb_session.beginTransaction();
        }
        else
        {
            hb_session = session;
        }
        //hb_session.getTransaction().begin();
        try
        {
            String hql = "select distinct s from Session s";
            hql += " join fetch s.termUser tu";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();

            parameterHelper.addParameter("s.", "sessionId", Login.getSessionID());
            // TODO IP-Adresse überprüfen
            /*if (!(loginInfoType.getLastIP() != null
       && loginInfoType.getLastIP().equals(Login.getIp())))
       {
       // IP stimmt nicht überein
       if (logger.isDebugEnabled())
       logger.debug("IP stimmt nicht überein!");

       //return null;
       }
       else*/
            {
                // TODO Session-Timeout überprüfen

                // Parameter hinzufügen (immer mit AND verbunden)
                hql += parameterHelper.getWhere("");

                logger.debug("HQL: " + hql);

                // Query erstellen
                org.hibernate.Query q = hb_session.createQuery(hql);

                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q);

                List<Session> liste = q.list();

                if (liste != null && liste.size() > 0)
                {
                    logger.debug("Session existiert!");

                    Session s_session = liste.get(0);

                    // Antwort erstellen
                    loginInfoType = new LoginInfoType();
                    loginInfoType.setLastTimestamp(s_session.getLastTimestamp());
                    loginInfoType.setLastIP(s_session.getIpAddress());
                    loginInfoType.setTermUser(s_session.getTermUser());
                    loginInfoType.getTermUser().setIsAdmin(s_session.getTermUser().isIsAdmin());
                    loginInfoType.setLogin(new LoginType());
                    loginInfoType.getLogin().setUsername(s_session.getTermUser().getName());
                    loginInfoType.getLogin().setSessionID(s_session.getSessionId());

                    // in Map speichern
                    userMap.put(Login.getSessionID(), loginInfoType);
                }
            }

            //if (createHibernateSession)
            //  tx.commit();
        }
        catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            logger.error("Fehler bei 'getLoginInfos', Hibernate: " + e.getLocalizedMessage());
        }
        finally
        {
            if (createHibernateSession)
            {
                logger.debug("Schließe Hibernate-Session (LoginHelper.java)");
                hb_session.close();
            }
        }


        /*// Benutzer simulieren
     loginInfoType = new LoginInfoType();
     loginInfoType.setTermUser(new TermUser());
     loginInfoType.getTermUser().setId(1l);
     loginInfoType.getTermUser().setName("muetzner");
     loginInfoType.setLogin(new LoginType());
     loginInfoType.getLogin().setUsername("muetzner");*/
        return loginInfoType;
    }

    public boolean isUserPermitted(LoginType Login)
    {
        LoginInfoType loginInfo = getLoginInfos(Login);

        return loginInfo != null;
    }
}
