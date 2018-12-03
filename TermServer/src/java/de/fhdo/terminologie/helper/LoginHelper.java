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
 * The LoginHelper is a singleton object.
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginHelper
{

    private static LoginHelper instance;
    final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    final private HashMap<String, LoginInfoType> userMap = new HashMap<String, LoginInfoType>();

    /**
     * Return the singleton instance.
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

    /**
     * Calls doLogin(login, returnType, loginRequired, null)
     * TODO
     * @param login 
     * @param returnType 
     * @param loginRequired 
     * @return return value of doLogin(login, returnType, loginRequired, null)
     */
    public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired)
    {
        return doLogin(login, returnType, loginRequired, null);
    }

    /**
     * TODO
     * @param login
     * @param returnType
     * @param loginRequired
     * @param hb_session
     * @return 
     */
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

        if (loggedIn == false && loginRequired)
        {
            returnType.setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            returnType.setStatus(ReturnType.Status.OK);
            returnType.setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
        }
        return loggedIn;
    }

    /**
     * TODO
     * @param Login
     * @return 
     */
    public LoginInfoType getLoginInfos(LoginType Login)
    {
        return getLoginInfos(Login, null);
    }

    /**
     * Checks if the user is logged in and if the session is still valid, using the session-ID.
     * TODO
     * @param Login LoginType with session-ID
     * @param session 
     * @return LoginInfoType if successfull, else null
     */
    public LoginInfoType getLoginInfos(LoginType Login, org.hibernate.Session session)
    {
        if (Login == null || Login.getSessionID() == null || Login.getSessionID().length() == 0)
        {
            logger.debug("Session-ID missing");
            return null;
        }
        
        LoginRequestType request = new LoginRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.idp.authorizationIDP.LoginType());
        logger.info("Requested session-id: " + Login.getSessionID());
        request.getLogin().setSessionID(Login.getSessionID());
        GetLoginInfosResponse.Return loginInfos = null;
        try
        {
            AuthorizationIDP port = WebServiceUrlHelper.getInstance().getAuthorizationIdpServicePort();
            logger.info("WS endpoint: " + ((BindingProvider) port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
            loginInfos = port.getLoginInfos(request);
        }
        catch (Exception e)
        {
            logger.info(e);
        }

        if (loginInfos.getReturnInfos().getStatus().equals(Status.OK))
        {
            LoginInfoType loginInfoType = new LoginInfoType();
            loginInfoType.setLastIP(loginInfos.getReturnInfos().getLastIP());

            TermUser termUser = new TermUser();
            termUser.setId(loginInfos.getReturnInfos().getTermUser().getId());
            termUser.setIsAdmin(loginInfos.getReturnInfos().getTermUser().isIsAdmin());
            termUser.setName(loginInfos.getReturnInfos().getTermUser().getName());
            loginInfoType.setTermUser(termUser);

            LoginType loginType = new LoginType();
            loginType.setUsername(loginInfos.getReturnInfos().getTermUser().getName());
            loginType.setSessionID(Login.getSessionID());

            loginInfoType.setLogin(loginType);
            return loginInfoType;
        }

        LoginInfoType loginInfoType = null;

        boolean hibernateSessionCreated = (session != null);

        logger.debug("Hibernate session created? " + hibernateSessionCreated);

        if (Login.getSessionID() == null || Login.getSessionID().length() == 0)
        {
            logger.debug("Session-ID missing");
            return null;
        }

        if (userMap.containsKey(Login.getSessionID()))
        {
            loginInfoType = userMap.get(Login.getSessionID());
            
            if (Login.getIp() != null && Login.getIp().length() > 0)
            {
                if (!(loginInfoType.getLastIP() != null && loginInfoType.getLastIP().equals(Login.getIp())))
                {
                    logger.debug("IP does not match (" + Login.getIp() + ")");
                    return null;
                }
            }

            // Check timeout
            long now = new java.util.Date().getTime();
            long timestamp = loginInfoType.getLastTimestamp().getTime();
            //3.2.21 increased session timeout from 30 to 120
            long session_timeout = 120 * 60000; // 120 minutes, TODO read from DB
            
            if (now - session_timeout < timestamp)
            {
                //Refresh timestamp
                loginInfoType.setLastTimestamp(new Date());
                userMap.put(Login.getSessionID(), loginInfoType);
                return loginInfoType;
            }
            else
            {
                logger.debug("Login has timed out");
                return null;
            }
        }
        else
        {
            logger.debug("Session-ID is missing from userMap");
        }

        // Session aus DB lesen und in Map speichern
        org.hibernate.Session hb_session;

        if (hibernateSessionCreated)
        {
            hb_session = session;
        }
        else
        {
            hb_session = HibernateUtil.getSessionFactory().openSession();
        }
        try
        {
            String hql = "select distinct s from Session s";
            hql += " join fetch s.termUser tu";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();

            parameterHelper.addParameter("s.", "sessionId", Login.getSessionID());

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
                logger.debug("Session exists");

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
        catch (Exception e)
        {
            logger.error("Error at 'getLoginInfos', Hibernate: " + e.getLocalizedMessage());
        }
        finally
        {
            if (hibernateSessionCreated)
            {
                logger.debug("Closing hibernate session (LoginHelper.java)");
                if(hb_session!=null)
                    hb_session.close();
                else
                    logger.debug("Hibernate session has already been closed unexpectedly");
            }
        }
        return loginInfoType;
    }

    /**
     * TODO
     * @param Login
     * @return 
     */
    public boolean isUserPermitted(LoginType Login)
    {
        LoginInfoType loginInfo = getLoginInfos(Login);
        return loginInfo != null;
    }
}
