/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.dortmund;

import de.fhdo.db.DBSysParam;
import de.fhdo.db.Definitions;
import de.fhdo.helper.CookieHelper;
import de.fhdo.helper.SessionHelper;
/*import de.fhdo.ws.authorization.AuthenticateRequestType;
import de.fhdo.ws.authorization.AuthenticateResponseType;
import de.fhdo.ws.authorization.AuthenticateType;*/
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Robert Mützner
 */
public class DortmundHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static DortmundHelper instance;
  
  public static DortmundHelper getInstance()
  {
    if(instance == null)
      instance = new DortmundHelper();
              
    return instance;
  }
  
  private boolean fhDortmund;
  
  public DortmundHelper()
  {
    reloadData();
  }
  
  public void reloadData()
  {
    logger.debug("DortmundHelper - reloadData()");
    Boolean fhdo = DBSysParam.instance().getBoolValue("fh_dortmund", null, null);
    if(fhdo != null && fhdo.booleanValue())
      fhDortmund = true;
    else fhDortmund = false;
    
    logger.debug("fhDortmund: " + fhDortmund);
  }

  /**
   * @return the fhDortmund
   */
  public boolean isFhDortmund()
  {
    return fhDortmund;
  }
  
  
  

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

  public static boolean tryLogin()
  {
    return false;
    /*if(DortmundHelper.getInstance().isFhDortmund() == false)
      return false;
    
    if (SessionHelper.getUserID() == 0)
    {
      // Nicht angemeldet
      String token = CookieHelper.getCookie("FHLOGINTOKEN");
      logger.debug("Token: " + token);
      
      if (token != null && token.length() > 0)
      {
        logger.debug("SsoHelper - Versuche anzumelden...");

        // Token vorhanden, versuchen anzumelden
        AuthenticateRequestType request = new AuthenticateRequestType();
        request.setAuthenticateInfos(new AuthenticateType());
        request.getAuthenticateInfos().setApplicationKey(Definitions.APP_KEY);
        request.getAuthenticateInfos().setToken(token);

        AuthenticateResponseType response = authenticate(request);
        logger.debug("WS-Antwort: " + response.getReturnInfos().getMessage());

        if (response.getReturnInfos().isSuccess().booleanValue())
        {
          if (response.getAccessInfos() != null)
          {
            return de.fhdo.dortmund.LoginHelper.getInstance().handleLoginResponse(
                    response.getAccessInfos().getRoleList(), 
                    token, 
                    response.getAccessInfos().getUserName(), 
                    response.getAccessInfos().getUserId(),
                    response.getAccessInfos().getEmail());
          }
        }
        else
        {
          logger.debug("Login nicht erfolgreich, lösche Token!");
          CookieHelper.setCookie("FHLOGINTOKEN", "");
        }

      }
    }

    return false;*/
  }

  /*private static AuthenticateResponseType authenticate(de.fhdo.ws.authorization.AuthenticateRequestType parameter)
  {
    de.fhdo.ws.authorization.Authorization_Service service = new de.fhdo.ws.authorization.Authorization_Service();
    de.fhdo.ws.authorization.Authorization port = service.getAuthorizationPort();
    return port.authenticate(parameter);
  }*/

  

  
  
  
}
