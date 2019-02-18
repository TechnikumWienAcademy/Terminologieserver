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
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Login{

    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public LoginResponseType Login(LoginRequestType parameter){
        LOGGER.info("+++++ Login started +++++");

        //Creating response
        LoginResponseType response = new LoginResponseType();
        response.setReturnInfos(new ReturnType());

        //Checking parameters
        if (validateParameters(parameter, response) == false){
            LOGGER.info("----- Login finished (001) -----");
            return response; //Faulty parameters
        }

        response.setLogin(new LoginType());

        try{
            java.util.List<TermUser> userList;

            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            try{
                Security.checkForDeadSessions(hb_session);

                //Creating HQL
                String HQL_termUser_select = "select u from TermUser u";

                //Adding parameters to the helper, always use the helper or do it manually via Query.setString()
                //Otherwise SQL-injections are possible
                HQLParameterHelper parameterHelper = new HQLParameterHelper();

                if (parameter != null && parameter.getLogin() != null){
                    //Adding parameters from the cross reference via addParameter(String Prefix, String DBField, Object Value)
                    if (parameter.getLogin().getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME)){
                        String[] usernameArray = parameter.getLogin().getUsername().split(":");
                        parameterHelper.addParameter("u.", "name", usernameArray[0]);
                    }
                    else
                        parameterHelper.addParameter("u.", "name", parameter.getLogin().getUsername());

                    parameterHelper.addParameter("u.", "passw", parameter.getLogin().getPassword());
                }

                //Adding parameters (connected with AND)
                HQL_termUser_select += parameterHelper.getWhere("");

                //Creating query
                org.hibernate.Query Q_termUser_select = hb_session.createQuery(HQL_termUser_select);

                //Parameters can be set now via the helper
                parameterHelper.applyParameter(Q_termUser_select);

                //Executing database query
                userList = (java.util.List<TermUser>) Q_termUser_select.list();

                if (userList != null && userList.size() > 0 && parameter != null && performLogin(userList.get(0), parameter.getLogin(), hb_session, response)){
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Login erfolgreich");
                }
                else{
                    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
                    response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                    response.getReturnInfos().setMessage("Benutzername oder Passwort ist falsch");
                }
            }
            catch (Exception ex){
                LOGGER.error("Error [0117]", ex);
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                response.getReturnInfos().setMessage("Fehler bei 'Login', Hibernate: " + ex.getLocalizedMessage());
                
                try{
                    if(!hb_session.getTransaction().wasRolledBack())
                        hb_session.getTransaction().rollback();
                }
                catch(Exception exRollback){
                    LOGGER.error("Error [0116]: Rollback failed", exRollback);
                }
            }
            finally{
                if(!hb_session.getTransaction().wasCommitted())
                    hb_session.getTransaction().commit();
                if(hb_session.isOpen())
                    hb_session.close();
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0118]", ex);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'Login': " + ex.getLocalizedMessage());
        }
        
        LOGGER.info("----- Login finished (002) -----");
        return response;
    }

    private boolean performLogin(TermUser user_db, LoginType login, org.hibernate.Session hb_session, LoginResponseType response){
        boolean success = true;
        if (response.getLogin() == null)    
            response.setLogin(new LoginType());

        response.getLogin().setSessionID("");

        //Check password
        String pwHash = user_db.getPassw();

        if (!login.getPassword().equalsIgnoreCase(pwHash)){//Wrong password
            response.getReturnInfos().setMessage("Falsches Passwort!");
            success = false;
        }
        else{//Correct password
            //Creating hash value and saving in table with userID
            String newHash;

            UUID uuid = UUID.randomUUID();
            newHash = uuid.toString();
            
            //Checking for existing sessions and deleting them
            if (!login.getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME)){
                List<Session> sessionList = Security.checkForExistingSessions(hb_session, login, user_db);

                if (sessionList != null && !sessionList.isEmpty())
                    for (Session session : sessionList){
                        session.setTermUser(null);
                        hb_session.delete(session);
                    }
            }
            else{
                List<Session> sessionList = Security.checkForExistingKollabSessions(hb_session, login, user_db);

                if (sessionList != null && !sessionList.isEmpty())
                    for (Session session : sessionList){
                        session.setTermUser(null);
                        hb_session.delete(session);
                    }
            }

            // Neue Session hinzufügen
            Session newSession = new Session();
            newSession.setSessionId(newHash);
            newSession.setLastTimestamp(new java.util.Date());
            newSession.setTermUser(new TermUser());
            newSession.getTermUser().setId(user_db.getId());
            newSession.setIpAddress(login.getIp());

            if (login.getUsername().startsWith(Security.COLLAB_SOFTWARE_NAME)){
                String[] usernameArray = login.getUsername().split(":");
                newSession.setCollabUsername(usernameArray[1]);
            }
            else
                newSession.setCollabUsername(null);

            LOGGER.debug("IP-adress (session): " + newSession.getIpAddress());

            hb_session.save(newSession);

            response.getLogin().setSessionID(newHash);
            response.getLogin().setUsername(user_db.getName());
        }

        return success;
    }

    private boolean validateParameters(LoginRequestType Request, LoginResponseType Response){
        boolean passed = true;

        LoginType login = Request.getLogin();

        if (login == null){
            Response.getReturnInfos().setMessage("LoginType darf nicht null sein.");
            passed = false;
        }
        else if (login.getUsername() == null || login.getUsername().length() == 0){
            Response.getReturnInfos().setMessage("Username darf nicht oder leer null sein.");
            passed = false;
        }
        else if (login.getPassword() == null || login.getPassword().length() == 0){
            Response.getReturnInfos().setMessage("Passwort darf nicht null oder leer sein.");
            passed = false;
        }

        if (passed == false){
            Response.getReturnInfos().setOverallErrorCategory(OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(Status.FAILURE);
        }

        return passed;
    }

    public LoginResponseType checkLogin(LoginRequestType parameter){
        LOGGER.info("+++++ checkLogin started +++++");

        //Creating response
        LoginResponseType response = new LoginResponseType();
        response.setReturnInfos(new ReturnType());

        boolean loggedIn = false;
        LoginInfoType loginInfoType;
        if (parameter != null && parameter.getLogin() != null){
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
        }

        LOGGER.debug("User logged in? " + loggedIn);

        if (!loggedIn){
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Benutzer ist nicht angemeldet!");
            LOGGER.info("----- checkLogin finished (001) -----");
            return response;
        }
        else{
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Benutzer ist angemeldet!");
            LOGGER.info("----- checkLogin finished (002) -----");
            return response;
        }
    }
}
