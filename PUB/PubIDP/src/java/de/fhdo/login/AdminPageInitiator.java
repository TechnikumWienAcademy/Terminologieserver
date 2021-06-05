/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.login;

import de.fhdo.Definitions;
import de.fhdo.helper.LoginHelperTest;
import de.fhdo.helper.LoginHelperAdmin;
import de.fhdo.helper.LoginHelperCollab;
import java.util.Map;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Initiator;

/**
 *
 * @author Christoph Kösner
 */
public class AdminPageInitiator extends PageInitiator {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    private LoginHelperTest lh;

    public AdminPageInitiator() {
        super();
        lh = LoginHelperAdmin.getInstance();
    }

    @Override
    public void authReqToSession(AuthnRequest ar) {
        Sessions.getCurrent().setAttribute(Definitions.SESS_REQUEST, ar);
    }

    @Override
    public LoginHelperTest getLoginHelper() {
        return lh;
    }

}
