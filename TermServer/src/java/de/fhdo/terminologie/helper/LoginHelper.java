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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.ws.BindingProvider;

/**
 * The LoginHelper is a singleton object, which calls the login operations.
 * @author Robert Mützner
 */
public class LoginHelper{

    private static LoginHelper instance;
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    final private ConcurrentHashMap<String, LoginInfoType> userMap = new ConcurrentHashMap<String, LoginInfoType>();
    
    /**
     * Return the singleton instance or instantiate a new instance and return that.
     * @return the instance
     */
    public static LoginHelper getInstance(){
        if (instance == null)
            instance = new LoginHelper();
        return instance;
    }

    /**
     * Calls doLogin(login, returnType, loginRequired, null).
     * @param login the parameter for the subsequent call.
     * @param returnType parameter for the subsequent call.
     * @param loginRequired parameter for the subsequent call.
     * @return return response of the subsequent call.
     */
    public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired){
        return doLogin(login, returnType, loginRequired, null);
    }

    /**
     * Calls getLoginInfos() to check whether or not the user is logged in or not.
     * @param login the login parameter, containing information about the user.
     * @param returnType contains the return information about the login.
     * @param loginRequired wheter or not a login is required.
     * @param hb_session the hibernate session from which to retrieve the login infos.
     * @return true if the user was successfully logged in.
     */
    public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired, org.hibernate.Session hb_session){   
        LOGGER.info("+++++ doLogin started +++++");
        boolean loggedIn = false;

        if (login != null && returnType != null){
            LoginInfoType loginInfoType;
            loginInfoType = LoginHelper.getInstance().getLoginInfos(login, hb_session);
            loggedIn = loginInfoType != null;

            LOGGER.debug("User is logged in? " + loggedIn);
        }

        if (loggedIn == false && loginRequired && returnType != null){
            returnType.setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            returnType.setStatus(ReturnType.Status.OK);
            returnType.setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
        }
        
        LOGGER.info("----- doLogin finished (001) -----");
        return loggedIn;
    }

    /**
     * Calls getLoginInfos(Login, null).
     * @param Login the parameter for the subsequent call.
     * @return the return of the subsequent call.
     */
    public LoginInfoType getLoginInfos(LoginType Login){
        return getLoginInfos(Login, null);
    }

    /**
     * Checks if the user is logged in and if the session is still valid, using the session-ID.
     * @param Login LoginType with session-ID.
     * @param session the user's session.
     * @return LoginInfoType if successfull, else null.
     */
    public LoginInfoType getLoginInfos(LoginType Login, org.hibernate.Session session){
        LOGGER.info("+++++ getLoginInfos started +++++");
        
        if (Login == null || Login.getSessionID() == null || Login.getSessionID().length() == 0){
            LOGGER.debug("Session-ID missing");
            LOGGER.info("----- getLoginInfos finished (001) -----");
            return null;
        }
        
        LoginRequestType requestLogin = new LoginRequestType();
        requestLogin.setLogin(new de.fhdo.terminologie.ws.idp.authorizationIDP.LoginType());
        requestLogin.getLogin().setSessionID(Login.getSessionID());
        LOGGER.debug("Requested session-id: " + Login.getSessionID());
        
        GetLoginInfosResponse.Return loginInfos = null;
        try{
            AuthorizationIDP portAuthorizationIDP = WebServiceUrlHelper.getInstance().getAuthorizationIdpServicePort();
            LOGGER.info("WS endpoint: " + ((BindingProvider) portAuthorizationIDP).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
            loginInfos = portAuthorizationIDP.getLoginInfos(requestLogin);
        }
        catch (Exception e){
            LOGGER.error("Error [0062]", e);
        }

        //User is logged in
        if (loginInfos !=null && loginInfos.getReturnInfos().getStatus().equals(Status.OK)){
            LoginInfoType loginReturn = new LoginInfoType();
            loginReturn.setLastIP(loginInfos.getReturnInfos().getLastIP());

            TermUser termUser = new TermUser();
            termUser.setId(loginInfos.getReturnInfos().getTermUser().getId());
            termUser.setIsAdmin(loginInfos.getReturnInfos().getTermUser().isIsAdmin());
            termUser.setName(loginInfos.getReturnInfos().getTermUser().getName());
            loginReturn.setTermUser(termUser);

            LoginType loginType = new LoginType();
            loginType.setUsername(loginInfos.getReturnInfos().getTermUser().getName());
            loginType.setSessionID(Login.getSessionID());
            loginReturn.setLogin(loginType);
            
            LOGGER.info("----- getLoginInfos finished (002) -----");
            return loginReturn;
        }

        //User is not logged in
        LoginInfoType loginReturn = null;

        if (userMap.containsKey(Login.getSessionID())){
            loginReturn = userMap.get(Login.getSessionID());
            
            if (Login.getIp() != null && Login.getIp().length() > 0){
                if (!(loginReturn.getLastIP() != null && loginReturn.getLastIP().equals(Login.getIp()))){
                    LOGGER.debug("User-IP does not match (" + Login.getIp() + ")");
                    LOGGER.info("----- getLoginInfos finished (004) -----");
                    return null;
                }
            }

            // Checking timeout
            long now = new java.util.Date().getTime();
            long timestamp = loginReturn.getLastTimestamp().getTime();
            long session_timeout = 120 * 60000; // 120 minutes, TODO read from DB
            
            if (now - session_timeout < timestamp){
                //Refresh timestamp
                loginReturn.setLastTimestamp(new Date());
                userMap.put(Login.getSessionID(), loginReturn);
                LOGGER.info("----- getLoginInfos finished (005) -----");
                return loginReturn;
            }
            else{
                LOGGER.debug("Login has timed out");
                LOGGER.info("----- getLoginInfos finished (006) -----");
                return null;
            }
        }
        else
            LOGGER.debug("Session-ID is missing from userMap");

        org.hibernate.Session hb_session;
        boolean hibernateOpened = false;
        if (session != null)
            hb_session = session;
        else{
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hibernateOpened = true;
        }
        
        try{
            String HQL_session_select = "select distinct s from Session s";
            HQL_session_select += " join fetch s.termUser tu";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("s.", "sessionId", Login.getSessionID());

            //Adding parameters (always connected with AND)
            HQL_session_select += parameterHelper.getWhere("");

            LOGGER.debug("HQL: " + HQL_session_select);

            //Creating query
            org.hibernate.Query Q_session_select = hb_session.createQuery(HQL_session_select);

            // Parameters can be set now via the helper
            parameterHelper.applyParameter(Q_session_select);

            List<Session> sessionList = Q_session_select.list();
            
            if (sessionList != null && sessionList.size() > 0){
                LOGGER.debug("Session exists");

                Session responseSession = sessionList.get(0);

                // Creating response
                loginReturn = new LoginInfoType();
                loginReturn.setLastTimestamp(responseSession.getLastTimestamp());
                loginReturn.setLastIP(responseSession.getIpAddress());
                loginReturn.setTermUser(responseSession.getTermUser());
                loginReturn.getTermUser().setIsAdmin(responseSession.getTermUser().isIsAdmin());
                loginReturn.setLogin(new LoginType());
                loginReturn.getLogin().setUsername(responseSession.getTermUser().getName());
                loginReturn.getLogin().setSessionID(responseSession.getSessionId());

                userMap.put(Login.getSessionID(), loginReturn);
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0118]", ex);
        }
        finally{
            try{
                if(hibernateOpened && hb_session.isOpen())
                    hb_session.close();
            }
            catch(Exception ex){
                LOGGER.error("Error [0119]", ex);
            }
        }
        LOGGER.info("----- getLoginInfos finished (007) -----");
        return loginReturn;
    }

    /**
     * Calls getLoginInfos(Login).
     * @param Login the parameter for the subsequent call.
     * @return the return value of the subsequent call.
     */
    public boolean isUserPermitted(LoginType Login){
        LoginInfoType loginInfo = getLoginInfos(Login);
        return loginInfo != null;
    }
}
