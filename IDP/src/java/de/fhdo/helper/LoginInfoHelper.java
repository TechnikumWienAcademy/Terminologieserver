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
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginType;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

/**
 * V 3.3 RDY
 * @author Stefan Puraner
 */
public class LoginInfoHelper{
    private static LoginInfoHelper instance;
    private static final Logger LOGGER = Logger4j.getInstance().getLogger();
    
    /**
     * Instantiates the LoginInfoHelper if it is null, otherwise returns the current instance.
     * @return the instance of the LoginInfoHelper.
     */
    public static LoginInfoHelper getInstance(){
        if (instance == null)
            instance = new LoginInfoHelper();
        return instance;
    }
    
    /**
     * Retrieves the termUser information from the database, using the session-ID.
     * @param loginRequestType the parameter holding the session-ID.
     * @return info whether or not the user is logged in or not.
     */
    public LoginResponseType getLoginInfos(LoginType loginRequestType){
        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
        hb_session.getTransaction().begin();

        LoginResponseType response = new LoginResponseType();
        try{
            Query Q_session_search = hb_session.createQuery("from Session s join fetch s.termUser where sessionId=:sessionId");
            Q_session_search.setParameter("sessionId", loginRequestType.getSessionID());

            List<Session> sessionList = Q_session_search.list();

            if (sessionList.size() == 1){
                response.setReturnInfos(new ReturnType());
                response.getReturnInfos().setMessage("Session gefunden, User eingeloggt.");
                response.getReturnInfos().setStatus(Status.OK);
                response.getReturnInfos().setLastIP(sessionList.get(0).getIpAddress());
                response.getReturnInfos().setTermUser(new de.fhdo.db.hibernate.TermUser());
                response.getReturnInfos().getTermUser().setId(sessionList.get(0).getId());
                response.getReturnInfos().getTermUser().setIsAdmin(sessionList.get(0).getTermUser().isIsAdmin());
            }
            else{
                response.setReturnInfos(new ReturnType());
                response.getReturnInfos().setMessage("Session nicht vorhanden, User nicht eingeloggt.");
                response.getReturnInfos().setStatus(Status.FAILURE);
            }
        }
        catch (HibernateException e){
            LOGGER.error("Error [0063]: " + e.getLocalizedMessage());
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        return response;
    }
}
