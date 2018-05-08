/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.communication.Mail;
import de.fhdo.db.HibernateUtil;
import de.fhdo.exceptions.WebServiceException;
import de.fhdo.helper.LoginInfoHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.LoginType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveTermUserRequestType;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author puraner
 */
public class SaveCollaborationUser
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public SaveCollaborationUserResponseType saveCollaborationUser(SaveCollaborationUserRequestType parameter) throws WebServiceException
    {
        SaveCollaborationUserResponseType response = new SaveCollaborationUserResponseType();
        response.setReturnInfos(new ReturnType());

        try
        {
            this.validateParameter(parameter);
        }
        catch (WebServiceException e)
        {
            throw e;
        }

        logger.info("Update von CollaborationUser gestartet: " + parameter.getUser().getName());

        LoginResponseType loginResponse = LoginInfoHelper.getInstance().getLoginInfos(parameter.getLoginType());
        if (loginResponse.getReturnInfos().getStatus().equals(ReturnType.Status.OK))
        {

            // Daten laden
            org.hibernate.Session hb_session = HibernateUtil.getSessionFactory(HibernateUtil.COLLAB_USER).openSession();
            hb_session.getTransaction().begin();

            try
            {
                if (parameter.isNewEntry())
                {
                    // prüfen, ob Benutzer bereits existiert
                    String hql = "from Collaborationuser where username=:user";
                    Query q = hb_session.createQuery(hql);
                    q.setParameter("user", parameter.getUser().getUsername());
                    List userList = q.list();
                    if (userList != null && userList.size() > 0)
                    {
                        throw new WebServiceException("Benutzers existiert bereits. Bitte wählen Sie einen anderen Benutzernamen.");
                    }
                    String neuesPW = Password.generateRandomPassword(8);
                    String salt1 = Password.generateRandomSalt();
                    parameter.getTermuser().setPassw(Password.getSaltedPassword(neuesPW, salt1, parameter.getTermuser().getName()));
                    parameter.getTermuser().setSalt(salt1);
                            
                    
                    this.SaveTermUser(parameter);


                    // Passwort und Salt generieren
                    String salt2 = Password.generateRandomSalt();
                    Collaborationuser user = parameter.getUser();
                    user.setPassword(Password.getSaltedPassword(neuesPW, salt2, user.getUsername()));
                    user.setSalt(salt2);
                    user.setActivated(false);
                    user.setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
                    user.setActivationTime(new Date());

                    Role r = (Role) hb_session.get(Role.class, parameter.getRole().getId());
                    user.getRoles().clear();
                    user.getRoles().add(r);
                    // Benutzer speichern
                    hb_session.save(user);
                    user.getOrganisation().getCollaborationusers().clear();
                    user.getOrganisation().getCollaborationusers().add(user);

                    hb_session.save(user.getOrganisation());

                    // Benachrichtigung vorbereiten
                    String benutzerName = parameter.getTermuser().getName();
                    String email = user.getEmail();
                    String activationMD5 = user.getActivationMd5();

                    String mailResponse = Mail.sendMailCollaborationNewUser(benutzerName, neuesPW, email, activationMD5);

                    if (mailResponse.length() == 0)
                    {
                        hb_session.save(user);
                        hb_session.getTransaction().commit();
                        response.getReturnInfos().setStatus(ReturnType.Status.OK);
                        response.getReturnInfos().setMessage("Collaboration User erfolgreich erstellt.");
                    }
                    else
                    {
                        hb_session.getTransaction().rollback();
                        throw new WebServiceException("Fehler beim Anlegen eines Benutzers: " + mailResponse);
                    }        
                }
                else
                {
                    this.SaveTermUser(parameter);
                    
                    Collaborationuser user = (Collaborationuser) hb_session.get(Collaborationuser.class, parameter.getUser().getId());
                    user.setEmail(parameter.getUser().getEmail());
                    user.setActivated(parameter.getUser().getActivated());
                    user.setEnabled(parameter.getUser().getEnabled());
                    user.setFirstName(parameter.getUser().getFirstName());
                    user.setName(parameter.getUser().getName());
                    user.setSendMail(parameter.getUser().getSendMail());
                    hb_session.merge(user);
                    hb_session.getTransaction().commit();
                    response.getReturnInfos().setStatus(ReturnType.Status.OK);
                    response.getReturnInfos().setMessage("Collaboration User erfolgreich aktualisiert.");
                }
            }
            catch (WebServiceException e)
            {
                hb_session.getTransaction().rollback();
                logger.info(e);
                throw (e);
            }
            catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                logger.error(e);
                throw new WebServiceException("Beim Update eines Collaboration Users ist ein Fehler aufgetreten.");
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

    private void validateParameter(SaveCollaborationUserRequestType parameter) throws WebServiceException
    {
        if (parameter.getLoginType() == null)
        {
            throw new WebServiceException("LoginType must not be null.");
        }

        if (parameter.getLoginType().getSessionID() == null)
        {
            throw new WebServiceException("SessionId must not be null.");
        }

        if (parameter.getUser()== null)
        {
            throw new WebServiceException("CollaborationUser must not be null.");
        }

        if (!parameter.isNewEntry() && parameter.getUser().getId() == null)
        {
            throw new WebServiceException("CollaborationUser Id must not be null.");
        }
    }
    
    private void SaveTermUser(SaveCollaborationUserRequestType req) throws WebServiceException
    {
        SaveTermUserRequestType request = new SaveTermUserRequestType();
        request.setLoginType(new LoginType());
        request.getLoginType().setSessionID(req.getLoginType().getSessionID());
        request.setNewEntry(req.isNewEntry());
        request.setTermUser(req.getTermuser());

        SaveTermUser saveTermuser = new SaveTermUser();
        saveTermuser.saveTermUser(request);
    }
}
