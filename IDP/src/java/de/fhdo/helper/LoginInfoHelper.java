/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.db.HibernateUtil;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType.Status;
import java.util.List;
import org.hibernate.Query;
import de.fhdo.db.hibernate.Session;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginType;
/**
 *
 * @author puraner
 */
public class LoginInfoHelper
{
    private static LoginInfoHelper instance;
    
    public static LoginInfoHelper getInstance()
    {
        if (instance == null)
        {
            instance = new LoginInfoHelper();
        }
        return instance;
    }
    
    public LoginResponseType getLoginInfos(LoginType loginRequestType)
    {
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
        hb_session.getTransaction().begin();

        LoginResponseType response = new LoginResponseType();

        try
        {

            String hql = "from Session s join fetch s.termUser where sessionId=:sessionId";

            Query query = hb_session.createQuery(hql);
            query.setParameter("sessionId", loginRequestType.getSessionID());

            List<Session> result = query.list();

            if (result.size() == 1)
            {
                Session session = result.get(0);
                String ipAddress = session.getIpAddress();
                response.setReturnInfos(new ReturnType());
                response.getReturnInfos().setMessage("Session gefunden. User eingeloggt");
                response.getReturnInfos().setStatus(Status.OK);
                response.getReturnInfos().setLastIP(result.get(0).getIpAddress());
                response.getReturnInfos().setTermUser(new de.fhdo.db.hibernate.TermUser());
                response.getReturnInfos().getTermUser().setId(result.get(0).getId());
                response.getReturnInfos().getTermUser().setIsAdmin(result.get(0).getTermUser().isIsAdmin());

//				response.getReturnInfos().setSession(result.get(0));
            }
            else
            {
                response.setReturnInfos(new ReturnType());
                response.getReturnInfos().setMessage("SessionId nicht vorhanden. User nicht eingeloggt");
                response.getReturnInfos().setStatus(Status.FAILURE);

            }

        }
        catch (Exception e)
        {
            System.err.println(e);//Logger.getMessageLogger(this.getClass(), "Fehler bei Session Check auf IDP", Locale.GERMANY);
        }
        finally
        {
            hb_session.close();
        }
        return response;
    }
}
