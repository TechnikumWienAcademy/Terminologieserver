/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.db.HibernateUtil;
import de.fhdo.exceptions.WebServiceException;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetTermUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetTermUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType.Status;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author puraner
 */
public class GetTermUserList
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public GetTermUserResponseType getUserList(GetTermUserRequestType parameter) throws WebServiceException
    {
        logger.info("Get TermUser List gestartet.");
        try
        {
            this.validateParameter(parameter);
        }
        catch (WebServiceException e)
        {
            throw e;
        }

        LoginResponseType loginResponse = LoginInfoHelper.getInstance().getLoginInfos(parameter.getLoginType());
        GetTermUserResponseType response = new GetTermUserResponseType();
        response.setReturnInfos(new ReturnType());

        if (loginResponse.getReturnInfos().getStatus().equals(Status.OK))
        {
            // Daten laden
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
            try
            {
                String hql = "from TermUser order by name";
                List<de.fhdo.db.hibernate.TermUser> personList = hb_session.createQuery(hql).list();
                response.setUserList(new ArrayList<>(personList));
                response.getReturnInfos().setStatus(Status.OK);
                response.getReturnInfos().setMessage("TermUser erfolgreich gelesen.");
                response.getReturnInfos().setCount(personList.size());
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            finally
            {
                hb_session.close();
            }
        }
        else
        {
            throw new WebServiceException("User ist nicht eingeloggt.");
        }
        
        return response;
    }

    private void validateParameter(GetTermUserRequestType parameter) throws WebServiceException
    {
        if (parameter.getLoginType() == null)
        {
            throw new WebServiceException("LoginType must not be null.");
        }

        if (parameter.getLoginType().getSessionID() == null)
        {
            throw new WebServiceException("SessionId must not be null.");
        }
    }
}
