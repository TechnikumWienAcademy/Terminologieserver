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
package de.fhdo.collaboration.db;

import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;

/**
 *
 * @author Robert Mützner
 */
public class CollaborationSession
{
    final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static CollaborationSession instance;
    private String sessionID;
    private String sessionIDPub;
    
    /**
     * Returns the CollaborationSession-instance, if it is null it will be
     * instantiated before being returned.
     * @return the CollaborationSession-instance
     */
    public static CollaborationSession getInstance(){
        if (instance == null)
            instance = new CollaborationSession();

        return instance;
    }

    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    public void setPubSessionID(String pubSessionID)
    {
        this.sessionIDPub = pubSessionID;
    }

    public String getSessionID()
    {

        return getSessionID(SessionHelper.getCollaborationUserName());
    }

    /**
     * Calls getPubSessionID(Session.Helper.getCollaborationUserName()).
     * @return returns the return-value of the subsequent call
     */
    public String getPubSessionID(){
        return getPubSessionID(SessionHelper.getCollaborationUserName());
    }

    public String getSessionID(String username)
    {
        return SessionHelper.getSessionObjectByName("collab_session_id").toString();
    }

    /**
     * If the current sessionIDPub is null or empty, a new one will be created.
     * @param username
     * @return 
     */
    public String getPubSessionID(String username){
        if (sessionIDPub == null || sessionIDPub.length() == 0){
            logger.debug("Creating new session-ID");

            String collabUsername = PropertiesHelper.getInstance().getCollaborationUser();
            String collabPassword = PropertiesHelper.getInstance().getCollaborationPassword();
            if (collabUsername.isEmpty()){
                logger.error("Error [0148]: Collaboration Username is undefined");
                return null;
            }
            if (collabPassword.isEmpty()){
                logger.error("Error [0149]: Collaboration Password is undefined");
                return null;
            }

            de.fhdo.terminologie.ws.authorizationPub.LoginRequestType loginRequest = new de.fhdo.terminologie.ws.authorizationPub.LoginRequestType();
            loginRequest.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
            loginRequest.getLogin().setUsername(collabUsername + ":" + username); //Needed for cleaner Session Management
            loginRequest.getLogin().setPassword(collabPassword);

            de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return loginResponse = login_pub(loginRequest); //ANKER
            if (loginResponse != null && loginResponse.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authorizationPub.Status.OK){
                sessionIDPub = loginResponse.getLogin().getSessionID();
                logger.debug("Session-ID: " + sessionIDPub);
            }
            else
            {
                logger.error("Error [0133]: Unable to read Session-ID");
                if (loginResponse != null)
                    logger.error(loginResponse.getReturnInfos().getMessage());
            }
        }
        logger.debug("Returning Session-ID: " + sessionIDPub);
        return sessionIDPub;
    }

    /**
     * Calls the authorization-login webservice with the parameter.
     * @param parameter the parameter which is passed to the webservice
     * @return the return-value of the webservice call
     */
    private static de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return login_pub(de.fhdo.terminologie.ws.authorizationPub.LoginRequestType parameter){
        de.fhdo.terminologie.ws.authorizationPub.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();
        return port.login(parameter);
    }
}
