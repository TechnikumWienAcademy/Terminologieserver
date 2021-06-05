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
package de.fhdo.helper;

import de.fhdo.Definitions;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.communication.Mail;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.authorization.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.LoginResponse.Return;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

/**
 *
 * @author Robert Mützner
 */
public abstract class LoginHelperTest {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    private final SessionFactory sf;
    private final String sessIDAttr;
    private final String userSessAttr;

    public LoginHelperTest(SessionFactory sf, String userSessAttr, String sessIDAttr) {
        this.sf = sf;
        this.sessIDAttr = sessIDAttr;
        this.userSessAttr = userSessAttr;
    }

    public abstract UserWrapper getUser(String name, Session hb_session);

    public abstract LoginRequestType generateLoginRequest(UserWrapper uw);

    public abstract void additionalFeatures(UserWrapper uw);

    public abstract Assertion buildAssertionFromSessionVars(org.zkoss.zk.ui.Session session);

    public abstract void reset();

    public boolean loginUserPass(String username, String password) {
        logger.info("login try with username: " + username);
        Session hb_session = sf.openSession();
        UserWrapper user = getUser(username, hb_session);

        //user not in table
        if (user == null) {
            logger.info("user not found.");
            hb_session.close();
            return false;
        }

        String passwordSalted = Password.getSaltedPassword(password, user.getSalt(), username);
        //password doesn't match
        if (!passwordSalted.equals(user.getPass())) {
            logger.info("login failed.");
            hb_session.close();
            return false;
        }

        logger.info("login succeeded");
        logger.info("user id:" + user.getID());
        logger.info("username: " + username);
        if(user.getRoles() != null)
        {
            for(Role role : user.getRoles())
            {
                logger.info("user_role: "+ role.getName());
                logger.info("user_role_is_admin: "+ role.getAdminFlag());
            }
        }

        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        LoginRequestType loginRequest = generateLoginRequest(user);
        //Webservice call to TermServer
        Return response = login_1(loginRequest);
        if (response.getReturnInfos().getStatus() != Status.OK) {
            logger.error("Login Status not OK");
            hb_session.close();
            return false;
        }
        String sessID = response.getLogin().getSessionID();
        session.setAttribute(sessIDAttr, sessID);
        logger.info("session id: " + sessID);
        session.setAttribute(userSessAttr, user);
        //this method is used, because the login method of TermAdmin
        //logs also in as collaborationuser. it does nothing, when a 
        //collaboration-only user logs in.
        additionalFeatures(user);
        hb_session.close();
        return true;
    }

    public void sendUserBack() {
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        Assertion a = buildAssertionFromSessionVars(session);
        if (a == null) {
            return;
        }
        Clients.showBusy("Wird eingeloggt");
        AuthnRequest ar = (AuthnRequest) session.getAttribute(Definitions.SESS_REQUEST);
        if (ar != null) {

            Set<String> sites = (Set<String>) session.getAttribute(Definitions.LOGGED_IN_SITES);
            if (sites == null) {
                sites = new HashSet<>();
            }
            sites.add(ar.getIssuer().getValue());
						for (String temp : sites){
							logger.info("Logged_IN_Sites: " + temp);
						}
						
            session.setAttribute(Definitions.LOGGED_IN_SITES, sites);

            String redirectLink = ar.getAssertionConsumerServiceURL();
            redirectLink += "?assert=" + AssertionHelper.encodeAndMarshallAssertion(a);
            Executions.sendRedirect(redirectLink);
        }
    }

    public void logout() {
        Clients.showBusy("Abmelden...");
        reset();
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        session.invalidate();
        Executions.sendRedirect(Definitions.DEFAULT_PROJECT);
    }

    public boolean resendPassword(boolean New, String Username) {
        boolean erfolg = false;

        if (logger.isDebugEnabled()) {
            logger.debug("Neues Passwort fuer Benutzer " + Username);
        }

        Session hb_session = sf.openSession();
        hb_session.getTransaction().begin();

        try {
            List list = hb_session.createQuery("from TermUser where name=:p_user").setString("p_user", Username).list();

            if (list.size() > 0) {
                TermUser user = (TermUser) list.get(0);

                // Neues Passwort generieren
                String neuesPW = Password.generateRandomPassword(8);

                // Email-Adresse lesen
                String mail = user.getEmail();

                // Neues Passwort per Email versenden
                String result = Mail.sendNewPassword(Username, neuesPW, mail);
                if (result.length() == 0) {
                    erfolg = true;
                }

                if (erfolg) {
                    // Neues Passwort in der Datenbank speichern
          /*String salt = Password.generateRandomSalt();
                     user.setPassw(Password.getSaltedPassword(neuesPW, salt, Username));
                     user.setSalt(salt);

                     hb_session.merge(user);*/
                    String salt = "";
                    user.setPassw(Password.getSaltedPassword(neuesPW, salt, Username));
                    user.setSalt(salt);

                    hb_session.merge(user);
                }
            }

            hb_session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Fehler bei resendPassword(): " + e.getLocalizedMessage());
        } finally {
            hb_session.close();
        }

        return erfolg;

    }

    public static Return login_1(de.fhdo.terminologie.ws.authorization.LoginRequestType parameter) 
    {
        //de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
        de.fhdo.terminologie.ws.authorization.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();
        return port.login(parameter);
    }
}
