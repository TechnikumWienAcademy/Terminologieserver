/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.Definitions;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.db.DBSysParamCollab;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.authorization.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.LoginType;
import de.fhdo.terminologie.ws.authorization.Status;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Christoph
 */
public class LoginHelperAdmin extends LoginHelperCollab {

    private static LoginHelperTest instance = null;
    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public static LoginHelperTest getInstance() {
        return instance == null ? instance = new LoginHelperAdmin() : instance;
    }

    private LoginHelperAdmin() {
        super(HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN), Definitions.ADMIN_USER_SESS, Definitions.ADMIN_SESS_ID);
    }

    @Override
    public UserWrapper getUser(String name, Session hb_session) {
        Query q = hb_session.createQuery("from TermUser WHERE name=:p_user AND enabled=1 AND activated=1");
        q.setString("p_user", name);
        java.util.List<TermUser> userList = q.list();
        if (userList.size() != 1) {
            return null;
        }
        return new UserWrapper(userList.get(0));
    }

    @Override
    public LoginRequestType generateLoginRequest(UserWrapper uw) {
        LoginRequestType loginRequest = new LoginRequestType();
        loginRequest.setLogin(new LoginType());
        loginRequest.getLogin().setUsername(uw.getName());
        loginRequest.getLogin().setPassword(uw.getPass());
        return loginRequest;
    }

    @Override
    public void additionalFeatures(UserWrapper user) {
        SessionFactory sf = HibernateUtil.getSessionFactory(HibernateUtil.COLLAB_USER);
        Session hb_session = sf.openSession();
        
        String username = user.getName().split("_")[0];
        
        UserWrapper admin_collab = super.getUser(username, hb_session);
        hb_session.close();

        if (admin_collab == null) {
            logger.info("admin_collab was null. abort");
            return;
        }
        
        //Webservice Call for Collab Session
        LoginRequestType loginRequest = super.generateLoginRequest(admin_collab);
        LoginResponse.Return response = LoginHelperTest.login_1(loginRequest);
        if (response.getReturnInfos().getStatus() != Status.OK) {
            logger.info("Login Status not OK. abort");
            return;
        }
        
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        String sessID = response.getLogin().getSessionID();
        session.setAttribute(Definitions.ADMINCOLLAB_USER_SESS, admin_collab);
        session.setAttribute(Definitions.COLLAB_SESS_ID, sessID);
    }

    @Override
    public Assertion buildAssertionFromSessionVars(org.zkoss.zk.ui.Session session) {
        UserWrapper adminuser = (UserWrapper) session.getAttribute(Definitions.ADMIN_USER_SESS);
        //If the user logs in, his information should be stored in session variables.
        //This shouldn't happen
        if (adminuser == null) {
            return null;
        }
        String weblink = DBSysParamCollab.instance().getStringValue("weblink", null, null);
        Assertion as = AssertionHelper.buildAssertion(null, adminuser.getName(), weblink);
        String sessID = (String) session.getAttribute(Definitions.ADMIN_SESS_ID);
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getSessIDAttribute(sessID));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.isAdminAttribute(adminuser.isAdmin()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getIDAttribute(adminuser.getID().intValue()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getNameAttribute(adminuser.getName()));
        
        UserWrapper collabuser = (UserWrapper) session.getAttribute(Definitions.ADMINCOLLAB_USER_SESS);
        String collab_sessID = (String) session.getAttribute(Definitions.COLLAB_SESS_ID);
        //This is the case, if the admin user exists, e.g under the name: "exmpl_tadm",
        //but there is no  user "exmpl" in the collaboration table
        //The assertion then is sent back without addditional collab user info
        if (collabuser == null || collab_sessID == null) {
            return as;
        }
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabId(collabuser.getID().intValue()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabNameAttribute(collabuser.getName()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabSessIDAttribute(collab_sessID));

        if (collabuser.getRoles() != null && !collabuser.getRoles().isEmpty()) {
            Attribute at = null;
            for (Role r : collabuser.getRoles()) {
                AssertionHelper.addAttributeToAssertion(as, AssertionHelper.roleAttribute(r.getName()));
            }
            AssertionHelper.addAttributeToAssertion(as, at);
        }
        
        //Für den Abgleich der Benutzerdaten zwischen Publikation und Kollaboration
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabUserDataToMatch(collabuser.getID()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getAdminUserDataToMatch(adminuser.getID()));
        
        return as;
    }

    @Override
    public void reset() {
    }
    }
