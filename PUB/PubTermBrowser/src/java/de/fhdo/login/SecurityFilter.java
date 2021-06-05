/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.login;

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.helper.SAMLHelper;
import de.fhdo.helper.SessionHelper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Priority;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.ConfigurationException;

/**
 * Login Filter. Leitet auf die Loginseite um wenn der Benutzer nicht
 * eingelogged ist.
 *
 * @author mathias aschhoff, Christoph Kösner
 * @see http://forums.sun.com/thread.jspa?threadID=5377392
 * @see Markus Stäuble, Hans Jürgen Schumacher -ZK Developers Guide 2008 Packt
 * Publishing Ltd. S 100ff
 */
public class SecurityFilter implements Filter {

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ex) {
            Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.debug("doFilter()");

        HttpSession session = ((HttpServletRequest)request).getSession();

        /*
         Benutzer eingeloggt: Filter lässt durch
         Benutzer nicht eingeloggt. Filter leitet auf IDP weiter.
         */
				
				
        if (isLoggedIn(request, response)) {
            logger.debug("login OK (doFilter)");
            chain.doFilter(request, response);
        } else {
            logger.debug("login nicht OK (doFilter)");
            String lastreq = ((HttpServletRequest) request).getRequestURI();
            session.setAttribute("lastreq", lastreq);
            /*
             leitet weiter auf IDP
             Sendet SAML AuthnRequest als Parameter
             */
						String idp_url = DBSysParam.instance().getStringValue("idp_url", null, null);
                                                String weblink = DBSysParam.instance().getStringValue("weblink", null, null);
            AuthnRequest ar = AuthnRequestBuilder.buildAuthnRequest(idp_url + "/IDP",
                    weblink + "/assertionConsumer", weblink);
            try {
                SAMLHelper.logSAMLObject(ar);
                String marshalled = SAMLHelper.marshallElement(ar);
                String encoded = SAMLHelper.encode(marshalled);
                ((HttpServletResponse) response).sendRedirect(idp_url + "/IDP/collab_login.zul?sso="+encoded);
            } catch (Exception ex) {
                Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private boolean isLoggedIn(ServletRequest request, ServletResponse response) {
        HttpSession lsession = ((HttpServletRequest) request).getSession();
        return lsession.getAttribute("collaboration_user_id")!=null;
    }

    /*
     *  Werden vorerst nicht benötigt
     */
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

}
