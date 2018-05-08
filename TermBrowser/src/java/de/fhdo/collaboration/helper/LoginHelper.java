/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.communication.Mail;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.helper.Password;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.terminologie.ws.authorization.LoginType;
import de.fhdo.terminologie.ws.authorization.LogoutRequestType;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author Robert Mützner
 */
public class LoginHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static LoginHelper instance = null;
  private static final int activationTimespan = 259200; //72h

  public LoginHelper()
  {
  }

  public boolean login(String username, String password)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    boolean loggedin = false;

    try
    {
      String salt = "";

      // 1. User lesen (Salt)
      Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user AND enabled=1 AND activated=1");
      q.setString("p_user", username);
      java.util.List<Collaborationuser> userList = q.list();

      logger.debug("User-List-length: " + userList.size());

      if (userList.size() == 1)
      {
        try
        {
          salt = userList.get(0).getSalt();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      if (salt == null)
        salt = "";
      
      logger.debug("salt: " +salt);

      // Super, sichere Methode
      String passwordSalted = Password.getSaltedPassword(password, salt, username);
      
      logger.debug("username: " +username);
      logger.debug("password: " +password);
      logger.debug("passwordSalted: " +passwordSalted);

      org.hibernate.Query q2 = hb_session.createQuery("from Collaborationuser WHERE username=:p_user AND password=:p_passwordSalted AND enabled=1 AND activated=1");
      q2.setString("p_user", username);
      q2.setString("p_passwordSalted", passwordSalted);
      userList = (java.util.List<Collaborationuser>) q2.list();
      
      logger.debug("User-List-length 2: " + userList.size());

      if (userList.size() == 1)
      {
        Collaborationuser user = userList.get(0);
        logger.debug("Login mit ID: " + user.getId());

        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        session.setAttribute("collaboration_user_id", user.getId());
        //session.setAttribute("is_admin", (user.getAdmin() == null ? false : user.getAdmin().booleanValue()));
        session.setAttribute("collaboration_user_name", user.getUsername());
        session.setAttribute("collaboration_user_role", user.getRoles().iterator().next().getName());
        
        session.setAttribute("CollaborationActive", true);
        
        /*if (user.getPersons() != null && user.getPersons().size() > 0)
        {
          session.setAttribute("person_id", ((Person) user.getPersons().toArray()[0]).getId());
          session.setAttribute("person_obj", user.getPersons().toArray()[0]);
        }*/
				
        loggedin = true;
        
        String collabUsername = PropertiesHelper.getInstance().getCollaborationUser();
        String collabPassword = PropertiesHelper.getInstance().getCollaborationPassword();
        if(collabUsername.equals(""))
        {
            logger.error("Collaboration Username is undefined");
            return false;
        }
        if(collabPassword.equals(""))
        {
            logger.error("Collaboration Password is undefined");
            return false;
        }
				
        if(DBSysParam.instance().getBoolValue("isKollaboration", null, null))
        {
          de.fhdo.terminologie.ws.authorizationPub.LoginRequestType parameter = new de.fhdo.terminologie.ws.authorizationPub.LoginRequestType();
          parameter.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
          parameter.getLogin().setUsername(collabUsername + ":" + username); //Needed for cleaner Session Management
          parameter.getLogin().setPassword(collabPassword);

          de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return ret = login_pub(parameter);

          if(ret.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK))
          {
            session.setAttribute("pub_collab_session", ret.getLogin().getSessionID());
          }
        }
        logger.debug("collaboration_user_id: " + session.getAttribute("collaboration_user_id"));
        logger.debug("collaboration_user_name: " + session.getAttribute("collaboration_user_name"));
 
      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
        logger.error("Fehler beim Login: " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }

    return loggedin;
  }

    public void reset() {
        logger.debug("reset()");
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        session.removeAttribute("collaboration_user_id");
        session.removeAttribute("collaboration_user_name");
        session.removeAttribute("collaboration_user_role");
        
        CollaborationSession.getInstance().setPubSessionID(null);
    }

  public void logout()
  {
    Clients.showBusy("Abmelden...");
    //collabsoftware muss abgemeldet werden!
    
    logger.debug("Authorization.login()-Webservice wird aufgerufen");
    //de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
    de.fhdo.terminologie.ws.authorization.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();

    LogoutRequestType request = new LogoutRequestType();
    request.setLogin(new LoginType());
    request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());

    LogoutResponseType response = port.logout(request);
    logger.debug("Antwort: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      
      CollaborationSession.getInstance().setSessionID(null);
      //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
      //session.invalidate();
      
      //Logout Pub Plattform
			try {
				if(DBSysParam.instance().getBoolValue("isKollaboration", null, null)){
					//de.fhdo.terminologie.ws.authorizationPub.Authorization_Service servicePub = new de.fhdo.terminologie.ws.authorizationPub.Authorization_Service();
					de.fhdo.terminologie.ws.authorizationPub.Authorization portPub = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();

					de.fhdo.terminologie.ws.authorizationPub.LogoutRequestType requestPub = new de.fhdo.terminologie.ws.authorizationPub.LogoutRequestType();
					requestPub.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
					requestPub.getLogin().setSessionID(CollaborationSession.getInstance().getPubSessionID());

					de.fhdo.terminologie.ws.authorizationPub.LogoutResponseType responsePub = portPub.logout(requestPub);

					if(responsePub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK))
					{
						CollaborationSession.getInstance().setPubSessionID(null);
					}
				}
			} catch (Exception e) {
				logger.error("Fehler beim Abmelden an der Kollaborationsplattform: ", e);
				
			}
			
      
      reset();
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
        Logger.getLogger(de.fhdo.helper.LoginHelper.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public boolean activate(String hash)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    org.hibernate.Query q = hb_session.createQuery("from Collaborationuser WHERE activation_md5=:p_hash");
    q.setString("p_hash", hash);

    java.util.List<Collaborationuser> userList = (java.util.List<Collaborationuser>) q.list();

    if (userList.size() == 1)
    {
      Collaborationuser user = userList.get(0);
      DateTime now = DateTime.now();
      DateTime origin = new DateTime(user.getActivationTime());
      Seconds seconds = Seconds.secondsBetween(origin, now);
      int sec = seconds.getSeconds();
      if(seconds.getSeconds() < activationTimespan){
      
        user.setEnabled(true);
        user.setActivated(true);
        user.setActivationMd5("");
        String termUserName = user.getUsername();
        
        hb_session.merge(user);
        
        String termuserPostfix = "";
        if(!user.getRoles().isEmpty())
        {
            Role r = user.getRoles().iterator().next();
            if(r.getName().equals(CODES.ROLE_ADMIN))
            {
                termuserPostfix = "_tadm";
            }
            else if(r.getName().equals(CODES.ROLE_INHALTSVERWALTER))
            {
                termuserPostfix = "_iv";
            }
            else if(r.getName().equals(CODES.ROLE_BENUTZER))
            {
                termuserPostfix = "_dtn";

            }
            else if(r.getName().equals(CODES.ROLE_REZENSENT))
            {
                termuserPostfix = "_rzt";
            }
            else 
            {
                Messagebox.show("Die angegebene Rolle kann nicht verarbeitet werden.");
                hb_session.getTransaction().rollback();
                hb_session.close();
                return false;
            }
        }

        hb_session.getTransaction().commit();
        hb_session.close();

        //do the same for TermUser
        Session hb_session_term = de.fhdo.db.HibernateUtil.getSessionFactory().openSession();
        hb_session_term.getTransaction().begin();
        
        termUserName += termuserPostfix;
        Query qr = hb_session_term.createQuery("from TermUser WHERE name= :p_user AND enabled=0");
        qr.setString("p_user", termUserName);
        java.util.List<de.fhdo.db.hibernate.TermUser> userListTerm = qr.list();
        
        TermUser userTerm = userListTerm.get(0);
        
        userTerm.setActivated(true);
        userTerm.setEnabled(true);
        userTerm.setActivationMd5("");
        
        hb_session_term.merge(userTerm);
        hb_session_term.getTransaction().commit();
        hb_session_term.close();
        
        return true;
      }else{
        return false;
      }
    }

    hb_session.close();

    return false;
  }

  public static boolean resendPassword(boolean New, String Username)
  {
    boolean erfolg = false;

    if (logger.isDebugEnabled())
      logger.debug("Neues Passwort fuer Benutzer " + Username);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      List list = hb_session.createQuery("from Collaborationuser where username=:p_user").setString("p_user", Username).list();

      if (list.size() > 0)
      {
        Collaborationuser user = (Collaborationuser) list.get(0);

        // Neues Passwort generieren
        String neuesPW = Password.generateRandomPassword(8);

        // Email-Adresse lesen
        String mail = user.getEmail();

        // TODO Neues Passwort per Email versenden
        String[] adr = new String[1];
        adr[0] = mail;
        String result = Mail.sendNewPasswordCollaboration(Username, neuesPW, adr);
        if (result.length() == 0)
          erfolg = true;


        if (erfolg)
        {
          // Neues Passwort in der Datenbank speichern
          String salt = Password.generateRandomSalt();
          user.setPassword(Password.getSaltedPassword(neuesPW, salt, Username));
          user.setSalt(salt);

          hb_session.merge(user);
        }
      }

      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
        logger.error("Fehler bei resendPassword(): " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }

    return erfolg;

  }

    public static LoginHelper getInstance()
    {
        if (instance == null)
        {
            instance = new LoginHelper();
        }

        return instance;
    }

    private static de.fhdo.terminologie.ws.authorizationPub.LoginResponse.Return login_pub(de.fhdo.terminologie.ws.authorizationPub.LoginRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authorizationPub.Authorization_Service service = new de.fhdo.terminologie.ws.authorizationPub.Authorization_Service();
        de.fhdo.terminologie.ws.authorizationPub.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();
        return port.login(parameter);
    }
}
