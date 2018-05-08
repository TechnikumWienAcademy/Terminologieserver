/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.login;

import de.fhdo.helper.LoginHelperTest;
import de.fhdo.helper.SAMLHelper;
import java.util.Map;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.ConfigurationException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;

/**
 *
 * @author Christoph Kösner
 */
public abstract class PageInitiator implements Initiator {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public PageInitiator() {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ex) {
            logger.error(ex);
        }
    }

    public abstract LoginHelperTest getLoginHelper();

    public abstract void authReqToSession(AuthnRequest ar);

    @Override
    public void doInit(Page page, Map<String, Object> args) throws Exception {
        logger.info("Page init started");

        String encodedMarshalledReq = Executions.getCurrent().getParameter("sso");

        if (encodedMarshalledReq != null) {
            logger.info("AuthnRequest got");
            String decoded = SAMLHelper.decode(encodedMarshalledReq);
            AuthnRequest ar
                    = (AuthnRequest) SAMLHelper.unmarshall(decoded.getBytes());
            authReqToSession(ar);
        }
        getLoginHelper().sendUserBack();
    }

}
