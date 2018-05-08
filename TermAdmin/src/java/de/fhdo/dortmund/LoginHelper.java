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
package de.fhdo.dortmund;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.Definitions;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.helper.CODES;
import de.fhdo.helper.CookieHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.SessionHelper;
import de.fhdo.logging.LoggingOutput;
/*import de.fhdo.ws.authorization.LoginRequestType;
import de.fhdo.ws.authorization.LoginResponseType;
import de.fhdo.ws.authorization.LoginType;
import de.fhdo.ws.authorization.LogoutRequestType;
import de.fhdo.ws.authorization.LogoutResponseType;
import de.fhdo.ws.authorization.LogoutType;
import de.fhdo.ws.authorization.RoleType;*/
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

/**
 *
 * @author Robert Mützner
 */
public class LoginHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static de.fhdo.dortmund.LoginHelper instance = null;

  public LoginHelper()
  {
  }

  /*public boolean login(String username, String password)
  {
    logger.debug("FH Dortmund Login Helper - login()");

    LoginRequestType request = new LoginRequestType();
    request.setLogin(new LoginType());
    request.getLogin().setApplicationKey(Definitions.APP_KEY);
    request.getLogin().setUsername(username);
    request.getLogin().setPasswordHash(MD5.getMD5(password));

    LoginResponseType response = login_1(request);

    if (response.getReturnInfos().isSuccess().booleanValue())
    {
      // Webservice-Aufruf i.O., Token prüfen
      if (response.getAccessInfos() != null && response.getAccessInfos().getToken() != null
              && response.getAccessInfos().getToken().length() > 0)
      {
        return handleLoginResponse(response.getAccessInfos().getRoleList(),
                response.getAccessInfos().getToken(), username,
                response.getAccessInfos().getUserId(),
                response.getAccessInfos().getEmail());
      }
    }

    return false;
  }

  public boolean handleLoginResponse(List<RoleType> listRoles, String token,
          String username, long userId, String email)
  {
    boolean success = false;
    // Token erhalten, Login erfolgreich, aber Rollen prüfen

    for (RoleType roleType : listRoles)
    {
      if (roleType.getKey().equals("ADMIN"))
      {
        success = true;
        SessionHelper.setValue("is_admin", true);
      }
    }

    if (success)
    {
      // Zugriff in Ordnung, Ergebnis in Session speichern
      org.zkoss.zk.ui.Session session = Sessions.getCurrent();
      session.setAttribute("token", token);
      session.setAttribute("user_name", username);
      session.setAttribute("user_id", userId);
      session.setAttribute("is_admin", success);
      
      session.setAttribute("session_id", token);
      

      session.setAttribute("collaboration_user_role", CODES.ROLE_ADMIN);
      session.setAttribute("collaboration_user_id", "0");

      logger.debug("user_id: " + session.getAttribute("user_id"));
      logger.debug("is_admin: " + session.getAttribute("is_admin"));
      logger.debug("user_name: " + session.getAttribute("user_name"));
      logger.debug("Speicher Token in Session: " + token);

      // Token auch in Cookies speichern (SSO)
      CookieHelper.setCookie("FHLOGINTOKEN", token);

      // User mit DB abgleichen
      addUserIfNotExists(userId, username, true, email);

      return true;
    }

    return false;
  }*/

  private void addUserIfNotExists(long userId, String username, boolean isAdmin, String email)
  {
    logger.debug("Prüfe Benutzer: " + userId);
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      // Prüfe, ob Benutzer existiert
      TermUser termUser = (TermUser) hb_session.get(TermUser.class, userId);

      if (termUser == null)
      {
        logger.debug("Speicher neuen Benutzer in TermUser: " + username + " mit ID: " + userId);

        // Benutzer anlegen, da er nicht existiert
        org.hibernate.Transaction tx = hb_session.beginTransaction();

        termUser = new TermUser(isAdmin);
        termUser.setEmail(email);
        termUser.setPassw("");
        termUser.setUserName(username);
        termUser.setName(username);
        termUser.setId(userId);
        termUser.setActivated(true);
        termUser.setEnabled(true);

        hb_session.save(termUser);

        tx.commit();

        // Kollaborationsbenutzer ebenfalls speichern
        Session hb_sessionKollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
        try
        {
          logger.debug("Speichere ebenfalls in Kollaborations-DB");
          org.hibernate.Transaction txKollab = hb_sessionKollab.beginTransaction();

          Collaborationuser cUser = new Collaborationuser();
          cUser.setEmail(email);
          cUser.setPassword("");
          cUser.setUsername(username);
          cUser.setId(userId);
          cUser.setActivated(true);
          cUser.setEnabled(true);
          cUser.setSendMail(true);
          cUser.setName(username);

          hb_sessionKollab.save(cUser);

          txKollab.commit();
        }
        catch (Exception ex)
        {
          LoggingOutput.outputException(ex, this);
        }
        finally
        {
          hb_sessionKollab.close();
        }
      }
      else
        logger.debug("Benutzer existiert bereits");
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close();
    }


  }

  public void reset()
  {
    logger.debug("reset()");
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    // Session-Variablen zurücksetzen
    session.setAttribute("user_id", 0);
    session.setAttribute("user_name", "");
    session.setAttribute("is_admin", false);
    session.setAttribute("token", "");

    session.setAttribute("collaboration_user_role", "");
    session.setAttribute("collaboration_user_id", "0");
    //session.invalidate();

    // Token zurücksetzen
    CookieHelper.setCookie("FHLOGINTOKEN", "");

    // RightsHelper.getInstance().clear();
  }

  /*public void logout()
  {
    Clients.showBusy("Abmelden...");

    LogoutRequestType request = new LogoutRequestType();
    request.setLogoutInfos(new LogoutType());
    request.getLogoutInfos().setToken(SessionHelper.getValue("token").toString());

    LogoutResponseType response = logout_1(request);
    //if (response.getReturnInfos().isSuccess().booleanValue())
    {
      reset();
      org.zkoss.zk.ui.Session session = Sessions.getCurrent();
      session.invalidate();

      Executions.sendRedirect("/index.zul");
    }
  }*/

  public static de.fhdo.dortmund.LoginHelper getInstance()
  {
    if (instance == null)
      instance = new de.fhdo.dortmund.LoginHelper();

    return instance;
  }

/*
  // http://stackoverflow.com/questions/13626965/how-to-ignore-pkix-path-building-failed-sun-security-provider-certpath-suncertp
  // KeyStore.Entry newEntry = new KeyStore.TrustedCertificateEntry(someCert);
  // ks.setEntry("someAlias", newEntry, null);`
  static
  {
    // http://stackoverflow.com/questions/6755180/java-ssl-connect-add-server-cert-to-keystore-programatically

    // http://stackoverflow.com/questions/13626965/how-to-ignore-pkix-path-building-failed-sun-security-provider-certpath-suncertp
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier()
    {
      public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession)
      {
        return true;

      }
    });


    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[]
    {
      new X509TrustManager()
      {
        public X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
        {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
        {
        }
      }
    };

// Install the all-trusting trust manager
    try
    {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    catch (Exception e)
    {
    }
  }

  private static LoginResponseType login_1(de.fhdo.ws.authorization.LoginRequestType parameter)
  {
    de.fhdo.ws.authorization.Authorization_Service service = new de.fhdo.ws.authorization.Authorization_Service();
    de.fhdo.ws.authorization.Authorization port = service.getAuthorizationPort();
    return port.login(parameter);
  }

  private static LogoutResponseType logout_1(de.fhdo.ws.authorization.LogoutRequestType parameter)
  {
    de.fhdo.ws.authorization.Authorization_Service service = new de.fhdo.ws.authorization.Authorization_Service();
    de.fhdo.ws.authorization.Authorization port = service.getAuthorizationPort();
    return port.logout(parameter);
  }*/

  
}
