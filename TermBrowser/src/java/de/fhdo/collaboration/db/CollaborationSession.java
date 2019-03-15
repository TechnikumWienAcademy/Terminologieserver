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

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static CollaborationSession instance;

    public static CollaborationSession getInstance()
    {
        if (instance == null)
        {
            instance = new CollaborationSession();
        }

        return instance;
    }

    String sessionID;
    String sessionIDPub;

    public CollaborationSession()
    {
        sessionID = "";
        sessionIDPub = "";
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

    public String getPubSessionID()
    {

        return getPubSessionID(SessionHelper.getCollaborationUserName());
    }

    public String getSessionID(String username)
    {
        return SessionHelper.getValue("collab_session_id").toString();
    }

    public String getPubSessionID(String username)
    {
        if (sessionIDPub == null || sessionIDPub.length() == 0)
        {
            logger.debug("Erhalte neue Session-ID");

            String collabUsername = PropertiesHelper.getInstance().getCollaborationUser();
            String collabPassword = PropertiesHelper.getInstance().getCollaborationPassword();
            if (collabUsername.equals(""))
            {
                logger.error("Collaboration Username is undefined");
                return null;
            }
            if (collabPassword.equals(""))
            {
                logger.error("Collaboration Password is undefined");
                return null;
            }

            de.fhdo.terminologie.ws.authorizationPub.LoginRequestType parameter = new de.fhdo.terminologie.ws.authorizationPub.LoginRequestType();
            parameter.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
            parameter.getLogin().setUsername(collabUsername + ":" + username); //Needed for cleaner Session Management
            parameter.getLogin().setPassword(collabPassword);

            de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return ret = login_pub(parameter);
            if (ret != null && ret.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authorizationPub.Status.OK)
            {
                sessionIDPub = ret.getLogin().getSessionID();
                logger.debug("Session-ID: " + sessionIDPub);
            }
            else
            {
                // TODO Fehlermeldung
                logger.warn("Fehler beim Lesen der Session-ID: ");
                if (ret != null)
                {
                    logger.warn("" + ret.getReturnInfos().getMessage());
                }
            }
        }

        logger.debug("Gebe Session-ID pub zurück: " + sessionIDPub);

        return sessionIDPub;
    }

    private static de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return login_pub(de.fhdo.terminologie.ws.authorizationPub.LoginRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authorizationPub.Authorization_Service service = new de.fhdo.terminologie.ws.authorizationPub.Authorization_Service();
        de.fhdo.terminologie.ws.authorizationPub.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();
        return port.login(parameter);
    }
}
