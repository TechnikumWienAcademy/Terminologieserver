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
import de.fhdo.terminologie.ws.authorization.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.LoginType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;

/**
 *
 * @author Christoph
 */
public class LoginHelperCollab extends LoginHelperTest {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    private static LoginHelperTest instance = null;

    public static LoginHelperTest getInstance() {
        return instance == null ? instance = new LoginHelperCollab() : instance;
    }

    private LoginHelperCollab() {
        super(HibernateUtil.getSessionFactory(HibernateUtil.COLLAB_USER), Definitions.COLLAB_USER_SESS, Definitions.COLLAB_SESS_ID);
    }

    public LoginHelperCollab(SessionFactory sf, String userSessAttr, String sessIDAttr) {
        super(sf, userSessAttr, sessIDAttr);
    }

    @Override
    public UserWrapper getUser(String name, Session hb_session) {
        Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user AND enabled=1");
//        Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user AND enabled=1 AND activated=1");
        q.setString("p_user", name);
        java.util.List<Collaborationuser> userList = q.list();
        if (userList.isEmpty()) {
            return null;
        }
        return new UserWrapper(userList.get(0));
    }

    @Override
    public LoginRequestType generateLoginRequest(UserWrapper uw) {
        LoginRequestType loginRequest = new LoginRequestType();
        loginRequest.setLogin(new LoginType());
        loginRequest.getLogin().setUsername("collaboration_software:" + uw.getName());
        loginRequest.getLogin().setPassword("760e065ea510f539b7bf0c6af1478add");
        return loginRequest;
    }

    @Override
    public void additionalFeatures(UserWrapper uw) {
        //Does nothing here! Only in LoginHelperAdmin
    }

    @Override
    public Assertion buildAssertionFromSessionVars(org.zkoss.zk.ui.Session session) {
        UserWrapper collabUser = (UserWrapper) session.getAttribute(Definitions.COLLAB_USER_SESS);
        //If the user logs in, his information should be stored in session variables.
        //This shouldn't happen
        if (collabUser == null) {
            return null;
        }
        String weblink = DBSysParamCollab.instance().getStringValue("weblink", null, null);
        Assertion as = AssertionHelper.buildAssertion(null, collabUser.getName(), weblink);
        String sessID = (String) session.getAttribute(Definitions.COLLAB_SESS_ID);
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabSessIDAttribute(sessID));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.isAdminAttribute(collabUser.isAdmin()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabId(collabUser.getID().intValue()));
        AssertionHelper.addAttributeToAssertion(as, AssertionHelper.getCollabNameAttribute(collabUser.getName()));
				

        if (collabUser.getRoles().iterator().hasNext()) {
            Role r = collabUser.getRoles().iterator().next();
            AssertionHelper.addAttributeToAssertion(as, AssertionHelper.roleAttribute(r.getName()));
        }
        try {
            logger.info(SAMLHelper.marshallElement(as));
        } catch (Exception ex) {
            logger.error(ex);
        }
        return as;
    }

    @Override
    public void reset() {
    }
}
