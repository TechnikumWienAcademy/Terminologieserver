/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP;

import de.fhdo.db.HibernateUtil;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType.Status;
import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.hibernate.Query;
import de.fhdo.db.hibernate.Session;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.helper.Password;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ChangePasswordRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ChangePasswordResponseType;

@WebService(serviceName = "AuthorizationIDP")
public class AuthorizationIDP
{
    /**
     * Web service operation
     */
    @WebMethod(operationName = "getLoginInfos")
    public LoginResponseType getLoginInfos(@WebParam(name = "parameter") LoginRequestType loginRequestType)
    {
        return LoginInfoHelper.getInstance().getLoginInfos(loginRequestType.getLogin());
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "changePassword")
    public ChangePasswordResponseType changePassword(@WebParam(name = "parameter") ChangePasswordRequestType parameter)
    {
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
        hb_session.getTransaction().begin();

        ChangePasswordResponseType response = new ChangePasswordResponseType();

        try
        {
            String hql = "from Session s join fetch s.termUser where sessionId=:sessionId";

            Query query = hb_session.createQuery(hql);
            query.setParameter("sessionId", parameter.getLogin().getSessionID());

            List<Session> result = query.list();

            if (result.size() == 1)
            {
                Session session = result.get(0);
                de.fhdo.db.hibernate.TermUser user_init = session.getTermUser();

                String password = Password.getSaltedPassword(parameter.getOldPassword(), user_init.getSalt(), user_init.getName());

                org.hibernate.Query q = hb_session.createQuery("from TermUser where id=" + session.getTermUser().getId() + " AND passw='" + password + "'");

                List<de.fhdo.db.hibernate.TermUser> list = (List<de.fhdo.db.hibernate.TermUser>) q.list();

                if (list.size() == 1)
                {
                    de.fhdo.db.hibernate.TermUser user = list.get(0);

                    // Passwort aktualisieren
                    String salt = Password.generateRandomSalt();
                    String passwordNeuSalted = Password.getSaltedPassword(parameter.getNewPassword(), salt, user_init.getName());
                    user.setSalt(salt);
                    user.setPassw(passwordNeuSalted);

                    hb_session.merge(user);
                }
                else
                {
                    hb_session.getTransaction().rollback();
                    response.setReturnInfos(new ReturnType());
                    response.getReturnInfos().setMessage("User nicht gefunden.");
                    response.getReturnInfos().setStatus(Status.FAILURE);
                    return response;
                }
            }
            else
            {
                hb_session.getTransaction().rollback();
                response.setReturnInfos(new ReturnType());
                response.getReturnInfos().setMessage("SessionId nicht vorhanden. User nicht eingeloggt");
                response.getReturnInfos().setStatus(Status.FAILURE);
                return response;
            }
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            response.setReturnInfos(new ReturnType());
            response.getReturnInfos().setMessage("Fehler beim Ändern des Passworts.");
            response.getReturnInfos().setStatus(Status.FAILURE);
            return response;
        }
        finally
        {
            if(hb_session.getTransaction().isActive())
            {
                hb_session.getTransaction().commit();
            }
            hb_session.close();
        }
        
        
        response.setReturnInfos(new ReturnType());
        response.getReturnInfos().setMessage("Passwort erfolgreich geändert.");
        response.getReturnInfos().setStatus(Status.OK);
        return response;
    }
}
