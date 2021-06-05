/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.exceptions.WebServiceException;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.DeleteUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.DeleteUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import org.hibernate.Query;

/**
 *
 * @author puraner
 */
public class DeleteUser
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public DeleteUserResponseType deleteUser(DeleteUserRequestType parameter) throws WebServiceException
    {
        DeleteUserResponseType response = new DeleteUserResponseType();
        response.setReturnInfos(new ReturnType());

        try
        {
            this.validateParameter(parameter);
        }
        catch (WebServiceException e)
        {
            throw e;
        }

        logger.info("Delete User gestartet: " + parameter.getTermuser().getUserName());

        LoginResponseType loginResponse = LoginInfoHelper.getInstance().getLoginInfos(parameter.getLoginType());
        if (loginResponse.getReturnInfos().getStatus().equals(ReturnType.Status.OK))
        {
            // Daten laden
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN).openSession();
            hb_session.getTransaction().begin();
            
            org.hibernate.Session hb_session_collab = HibernateUtil.getSessionFactory(HibernateUtil.COLLAB_USER).openSession();
            hb_session_collab.getTransaction().begin();

            try
            {
                Query q = hb_session.createQuery("from TermUser WHERE name= :p_user");
                q.setString("p_user", parameter.getTermuser().getUserName());
                java.util.List<de.fhdo.db.hibernate.TermUser> userList = q.list();
                
                if(userList.size() != 1)
                {
                    throw new WebServiceException("TermUser konnte nicht gefunden werden. Ergebnis: " + userList.size());
                }
                
                Collaborationuser collabUser = (Collaborationuser) hb_session_collab.get(Collaborationuser.class, parameter.getCollaborationUser().getId());
                
                collabUser.setPassword("");
                collabUser.setSalt("");
                collabUser.setSendMail(false);
                collabUser.setActivated(false);
                collabUser.setHidden(false);
                collabUser.setEnabled(false);
                collabUser.setDeleted(true);
                hb_session_collab.update(collabUser);

                hb_session_collab.getTransaction().commit();
                
                TermUser user = userList.get(0);
                hb_session.delete(user);
                hb_session.getTransaction().commit();
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                response.getReturnInfos().setMessage("User erfolgreich gelöscht.");
            }
            catch(WebServiceException e)
            {
                hb_session.getTransaction().rollback();
                hb_session_collab.getTransaction().rollback();
                logger.info(e);
                throw e;
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                hb_session_collab.getTransaction().rollback();
                logger.error(e);
                throw new WebServiceException("Beim Update eines Term Users ist ein Fehler aufgetreten.");
            }
            finally
            {
                hb_session.close();
                hb_session_collab.close();
            }
        }
        else
        {
            throw new WebServiceException("User ist nicht eingeloggt.");
        }
        return response;
    }

    private void validateParameter(DeleteUserRequestType parameter) throws WebServiceException
    {
        if (parameter.getLoginType() == null)
        {
            throw new WebServiceException("LoginType must not be null.");
        }

        if (parameter.getLoginType().getSessionID() == null)
        {
            throw new WebServiceException("SessionId must not be null.");
        }

        if (parameter.getTermuser() == null)
        {
            throw new WebServiceException("TermUser must not be null.");
        }
        
        if(parameter.getCollaborationUser() == null)
        {
            throw new WebServiceException("CollaborationUser must not be null.");
        }
        
        if(parameter.getCollaborationUser().getId() == null)
        {
            throw new WebServiceException("CollaborationUser must not be null.");
        }
    }
}
