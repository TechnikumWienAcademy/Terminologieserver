/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.exceptions.WebServiceException;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveTermUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveTermUserResponseType;
import java.util.Date;

/**
 *
 * @author puraner
 */
public class SaveTermUser
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public SaveTermUserResponseType saveTermUser(SaveTermUserRequestType parameter) throws WebServiceException
    {
        SaveTermUserResponseType response = new SaveTermUserResponseType();
        response.setReturnInfos(new ReturnType());

        try
        {
            this.validateParameter(parameter);
        }
        catch (WebServiceException e)
        {
            throw e;
        }

        logger.info("Update von TermUser gestartet: " + parameter.getTermUser().getUserName());

        LoginResponseType loginResponse = LoginInfoHelper.getInstance().getLoginInfos(parameter.getLoginType());
        if (loginResponse.getReturnInfos().getStatus().equals(ReturnType.Status.OK))
        {
            // Daten laden
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
            hb_session.getTransaction().begin();

            try
            {
                if (parameter.isNewEntry())
                {
                    TermUser user = parameter.getTermUser();
                    
                    if(user.getPassw() == null || user.getSalt() == null)
                    {
                        throw new WebServiceException("Password and Salt must not be null");
                    }

                    user.setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
                    user.setActivationTime(new Date());
                    user.setIsAdmin(parameter.getTermUser().isIsAdmin());
                    
                    hb_session.save(user);
                    hb_session.getTransaction().commit();
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Term User erfolgreich erstellt.");
                }
                else
                {
                    TermUser user = (TermUser) hb_session.get(de.fhdo.db.hibernate.TermUser.class, parameter.getTermUser().getId());
                    user.setEmail(parameter.getTermUser().getEmail());
                    user.setUserName(parameter.getTermUser().getUserName());
                    user.setActivated(parameter.getTermUser().getActivated());
                    user.setEnabled(parameter.getTermUser().getEnabled());
                    user.setIsAdmin(parameter.getTermUser().isIsAdmin());
                    user.setName(parameter.getTermUser().getName());
                    hb_session.merge(user);
                    hb_session.getTransaction().commit();
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Term User erfolgreich aktualisiert.");
                }
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                logger.error(e);
                throw new WebServiceException("Beim Update eines Term Users ist ein Fehler aufgetreten.");
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

    private void validateParameter(SaveTermUserRequestType parameter) throws WebServiceException
    {
        if (parameter.getLoginType() == null)
        {
            throw new WebServiceException("LoginType must not be null.");
        }

        if (parameter.getLoginType().getSessionID() == null)
        {
            throw new WebServiceException("SessionId must not be null.");
        }

        if (parameter.getTermUser() == null)
        {
            throw new WebServiceException("TermUser must not be null.");
        }

        if (!parameter.isNewEntry() && parameter.getTermUser().getId() == null)
        {
            throw new WebServiceException("TermUser Id must not be null.");
        }
    }
}
