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

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.gui.admin.Login;
import de.fhdo.terminologie.ws.authorization.Status;
import de.fhdo.terminologie.ws.authorization.LoginType;
import de.fhdo.terminologie.ws.authorization.LogoutRequestType;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class LoginHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static LoginHelper instance = null;

  public LoginHelper()
  {
  }

  public void openLoginWindow()
  {
    try
    {
      // Fenster für Eingabedaten erzeugen und aufrufen
      Window win = (Window) Executions.createComponents("/gui/admin/login.zul", null, null);
      win.doModal();
      
      if(((Login)win).isErfolg())
        ViewHelper.gotoSrc(null);

    }
    catch (SuspendNotAllowedException ex)
    {
      Logger.getLogger(LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public boolean login(String username, String password, boolean isHash, String tAdminSessId, String pubSessionId)
  {
    //if(username.equalsIgnoreCase("user") && password.equals("test"))
    //  return true;
    if(!tAdminSessId.equals("bogous")){
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        session.setAttribute("user_name", username);
        session.setAttribute("session_id", tAdminSessId);
        session.setAttribute("pub_session_id", pubSessionId);
        
        if (tAdminSessId.length() > 0)
            return true;
    }else{
        return true;
    }

    /*
    String pwMD5 = "";
    
    if(!isHash){
        pwMD5 = Password.getMD5Password(password);
    }else{
        pwMD5 = password;
    }

    // Login-Webservice aufrufen
    logger.debug("Authorization.login()-Webservice wird aufgerufen");
    de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
    de.fhdo.terminologie.ws.authorization.Authorization port = service.getAuthorizationPort();
    
    LoginRequestType request = new LoginRequestType();
    request.setLogin(new LoginType());
    request.getLogin().setPassword(pwMD5);
    request.getLogin().setUsername(username);

    LoginResponse.Return response = port.login(request);
    logger.debug("Antwort: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      String sessionId = "";

      if (response.getLogin() != null)
      {
        // ZK-Session setzen
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        sessionId = response.getLogin().getSessionID();
        session.setAttribute("user_name", response.getLogin().getUsername());
        session.setAttribute("session_id", response.getLogin().getSessionID());
//        session.setAttribute("session_isAdmin", response.getTermUser().isIsAdmin()); // TermUser ist == null

        logger.debug("Session - user_name: " + session.getAttribute("user_name"));
        logger.debug("Session - session_id: " + session.getAttribute("session_id"));
      }

      if (sessionId.length() > 0)
        return true;
    }
    */
    return false;
    
  }

    public void reset() {
        logger.debug("reset()");
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        session.removeAttribute("user_id");
        session.removeAttribute("user_name");
        session.removeAttribute("session_id");

        // RightsHelper.getInstance().clear();
    }
    
  public void logout()
  {
    Clients.showBusy(Labels.getLabel("loginHelper.loggingOff"));

    // Webservice aufrufen
    logger.debug("Authorization.login()-Webservice wird aufgerufen");
    //de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
    de.fhdo.terminologie.ws.authorization.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();

    LogoutRequestType request = new LogoutRequestType();
    request.setLogin(new LoginType());
    request.getLogin().setSessionID(SessionHelper.getSessionId());

    LogoutResponseType response = port.logout(request);
    logger.debug("Antwort: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      
      //Logout Pub Plattform
			try {
				if(DBSysParam.instance().getBoolValue("isKollaboration", null, null)){
					de.fhdo.terminologie.ws.authorizationPub.Authorization_Service servicePub = new de.fhdo.terminologie.ws.authorizationPub.Authorization_Service();
					de.fhdo.terminologie.ws.authorizationPub.Authorization portPub = servicePub.getAuthorizationPort();

					de.fhdo.terminologie.ws.authorizationPub.LogoutRequestType requestPub = new de.fhdo.terminologie.ws.authorizationPub.LogoutRequestType();
					requestPub.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
					requestPub.getLogin().setSessionID(SessionHelper.getSessionObjectByName("pub_session_id").toString());

					de.fhdo.terminologie.ws.authorizationPub.LogoutResponseType responsePub = portPub.logout(requestPub);

					if(responsePub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK))
					{
						SessionHelper.setValue("pub_session_id", null);
					}
				}
			} catch (Exception e) {
				logger.error("Fehler beim Abmelden am Publikbereich: ", e);
			}
			
			
      
      reset();
      //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
      //session.invalidate();

      Executions.sendRedirect("/gui/main/main.zul");
    }
    else
    {
      try
      {
        Messagebox.show(Labels.getLabel("loginHelper.loggingOffError") + ": " + response.getReturnInfos().getMessage());
      }
      catch (Exception ex)
      {
        Logger.getLogger(LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
      }
    }


  }

  public boolean activate(String hash)
  {
    /*Session hb_session = HibernateUtil.getSessionFactory().openSession();
    org.hibernate.Transaction tx = hb_session.beginTransaction();
    
    org.hibernate.Query q = hb_session.createQuery("from User WHERE activationMD5=:p_hash");
    q.setString("p_hash", hash);
    
    java.util.List<User> userList = (java.util.List<User>) q.list();
    
    if (userList.size() == 1)
    {
    User user = userList.get(0);
    
    user.setEnabled(true);
    user.setActivated(true);
    user.setActivationMd5("");
    
    hb_session.merge(user);
    
    tx.commit();
    HibernateUtil.getSessionFactory().close();
    
    return true;
    }
    
    HibernateUtil.getSessionFactory().close();*/

    return false;
  }

  public static boolean resendPassword(boolean New, String Username)
  {
    /*boolean erfolg = false;
    
    if (logger.isDebugEnabled())
    logger.debug("Neues Passwort fuer Benutzer " + Username);
    
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    org.hibernate.Transaction tx = hb_session.beginTransaction();
    
    List list = hb_session.createQuery("from User where user=:p_user").setString("p_user", Username).list();
    
    if (list.size() > 0)
    {
    User user = (User) list.get(0);
    
    // Neues Passwort generieren
    String neuesPW = Password.generateRandomPassword(8);
    
    // Email-Adresse lesen
    String hql = "select distinct c from Communication c";
    hql += " WHERE refer_classname='" + Definitions.REFER_CLASSNAME_PERSON + "'"
    + " AND refer_id=" + user.getPerson().getPersId();
    
    List<Communication> communicationList = (List<Communication>) hb_session.createQuery(hql).list();
    
    if (communicationList != null)
    {
    // Evtl. mehrere Email-Adressen, also an alle schicken
    for (int i = 0; i < communicationList.size(); ++i)
    {
    // Neues Passwort per Email versenden
    String result = Mail.sendNewPassword(Username, Username, neuesPW, communicationList.get(i).getComContact());
    if (result.length() == 0)
    erfolg = true;
    }
    }
    
    if (erfolg)
    {
    // Neues Passwort in der Datenbank speichern
    String salt = Password.generateRandomSalt();
    user.setPass(Password.getSaltedPassword(neuesPW, salt, Username));
    user.setSalt(salt);
    user.setPasswordMethodCd("md5_salt_1000");
    
    hb_session.merge(user);
    }
    }
    
    tx.commit();
    HibernateUtil.getSessionFactory().close();
    
    return erfolg;*/
    return false;
  }

  public static LoginHelper getInstance()
  {
    if (instance == null)
      instance = new LoginHelper();

    return instance;
  }
  /*
  public ArrayList<String> getPseudonym(String username){
  
        Clients.showBusy(Labels.getLabel("loginHelper.working"));
        
        ArrayList<String> data = new ArrayList<String>();

        // Webservice aufrufen
        logger.debug("Authorization.mPseudonym()-Webservice wird aufgerufen");
  
        MPseudonymRequestType request = new MPseudonymRequestType();
        request.setMPseudonymInputType(new MPseudonymInputType());
        request.getMPseudonymInputType().setType("get");
        request.getMPseudonymInputType().setUsername(username);
        
        MPseudonymResponse.Return response = WebServiceHelper.mPseudonym(request);
        logger.debug("Antwort: " + response.getReturnType().getMessage());
        
        if (response.getReturnType().getStatus() == de.fhdo.terminologie.ws.sso.Status.OK)
        {
            if(response.getMPseudonymOutputType().getStatus().equals("success")){
                data.add(0, response.getMPseudonymOutputType().getPseudonym());
                data.add(1, response.getMPseudonymOutputType().getHash());
            }
        }
        else
        {
          try
          {
            Messagebox.show(Labels.getLabel("loginHelper.mPseudonymError") + ": " + response.getReturnType().getMessage());
          }
          catch (Exception ex)
          {
              Logger.getLogger(LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        Clients.clearBusy();
        return data;
  }
  
  public boolean setPseudonym(String username, String pseudonym){
  
        boolean success = false;
        Clients.showBusy(Labels.getLabel("loginHelper.working"));
        
        ArrayList<String> data = new ArrayList<String>();

        // Webservice aufrufen
        logger.debug("Authorization.mPseudonym()-Webservice wird aufgerufen");
 
        MPseudonymRequestType request = new MPseudonymRequestType();
        request.setMPseudonymInputType(new MPseudonymInputType());
        request.getMPseudonymInputType().setType("set");
        request.getMPseudonymInputType().setUsername(username);
        request.getMPseudonymInputType().setPseudonym(pseudonym);

        MPseudonymResponse.Return response = WebServiceHelper.mPseudonym(request);
        logger.debug("Antwort: " + response.getReturnType().getMessage());

        if (response.getReturnType().getStatus() == de.fhdo.terminologie.ws.sso.Status.OK)
        {
            if(response.getMPseudonymOutputType().getStatus().equals("success")){
                success = true;
            }
        }
        else
        {
          try
          {
            Messagebox.show(Labels.getLabel("loginHelper.mPseudonymError") + ": " + response.getReturnType().getMessage());
          }
          catch (Exception ex)
          {
            Logger.getLogger(LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        Clients.clearBusy();
        return success;
  }
  
  public boolean deletePseudonym(String username){
  
        boolean success = false;
        Clients.showBusy(Labels.getLabel("loginHelper.working"));
        
        ArrayList<String> data = new ArrayList<String>();

        // Webservice aufrufen
        logger.debug("Authorization.mPseudonym()-Webservice wird aufgerufen");

        MPseudonymRequestType request = new MPseudonymRequestType();
        request.setMPseudonymInputType(new MPseudonymInputType());
        request.getMPseudonymInputType().setType("delete");
        request.getMPseudonymInputType().setUsername(username);

        MPseudonymResponse.Return response = WebServiceHelper.mPseudonym(request);
        logger.debug("Antwort: " + response.getReturnType().getMessage());

        if (response.getReturnType().getStatus() == de.fhdo.terminologie.ws.sso.Status.OK)
        {
            if(response.getMPseudonymOutputType().getStatus().equals("success")){
                success = true;
            }
        }
        else
        {
          try
          {
            Messagebox.show(Labels.getLabel("loginHelper.mPseudonymError") + ": " + response.getReturnType().getMessage());
          }
          catch (Exception ex)
          {
            Logger.getLogger(LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        Clients.clearBusy();
        return success;
  }*/
}
