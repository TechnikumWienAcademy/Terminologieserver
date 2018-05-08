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
public class CollabPageInitiator extends PageInitiator {

    private LoginHelperTest lh;

    public CollabPageInitiator() {
        super();
        lh = LoginHelperCollab.getInstance();
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
