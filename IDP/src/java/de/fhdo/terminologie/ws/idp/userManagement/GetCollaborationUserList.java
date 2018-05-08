/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.HibernateUtil;
import de.fhdo.exceptions.WebServiceException;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType.Status;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author puraner
 */
public class GetCollaborationUserList
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public GetCollaborationUserResponseType getUserList(GetCollaborationUserRequestType parameter) throws WebServiceException
    {
        logger.info("Get CollaborationUser List gestartet.");
        try
        {
            this.validateParameter(parameter);
        }
        catch (WebServiceException e)
        {
            throw e;
        }

        LoginResponseType loginResponse = LoginInfoHelper.getInstance().getLoginInfos(parameter.getLoginType());
        GetCollaborationUserResponseType response = new GetCollaborationUserResponseType();
        response.setReturnInfos(new ReturnType());

        if (loginResponse.getReturnInfos().getStatus().equals(Status.OK))
        {
            // Daten laden
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.COLLAB_USER).openSession();
            try
            {
                String hql = "from Collaborationuser WHERE deleted=0 order by name";
                logger.debug("hql: " + hql);

                List<Collaborationuser> personList = hb_session.createQuery(hql).list();
                
                for(Collaborationuser user : personList)
                {
                    user.getOrganisation().getCollaborationusers().clear();
                }
                
                response.setUserList(new ArrayList<>(personList));
                response.getReturnInfos().setStatus(Status.OK);
                response.getReturnInfos().setMessage("CollaborationUser erfolgreich gelesen.");
                response.getReturnInfos().setCount(personList.size());
            }
            catch (Exception e)
            {
                logger.error(e);
                hb_session.close();
            }
            finally
            {
                //hb_session.close();
            }
        }
        else
        {
            throw new WebServiceException("User ist nicht eingeloggt.");
        }
        
        return response;
    }

    private void validateParameter(GetCollaborationUserRequestType parameter) throws WebServiceException
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
