/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.login;

import de.fhdo.Definitions;
import de.fhdo.db.DBSysParamCollab;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.terminologie.ws.authorization.LoginType;
import de.fhdo.terminologie.ws.authorization.LogoutRequestType;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import java.util.Set;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 *
 * @author Christoph
 */
public class Logout extends GenericForwardComposer {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public void singlelogout(String logout) {
        Clients.evalJavaScript("singlelogout(\"" + logout + "/logout.zul\");");
        logger.info("Logout: singlelogout(" + logout + "/logout.zul);");
    }

//    @Override
//    public void afterCompose() {
//    }
    public void onClick$btnExec(Event evt) throws InterruptedException {
        Session s = Sessions.getCurrent();
        Set<String> loggedin = (Set<String>) s.getAttribute(Definitions.LOGGED_IN_SITES);
        if (loggedin != null) {
            for (String oneloggedin : loggedin) {
                logger.info("Logged_in_sites: " + oneloggedin);
                //singlelogout(oneloggedin); //Auskommentiert: PopUp nicht nötig
            }
        }
        LogoutRequestType lrt = new LogoutRequestType();
				
        LoginType lt = new LoginType();
        lt.setSessionID((String) s.getAttribute(Definitions.COLLAB_SESS_ID));
        lrt.setLogin(lt);
        LogoutResponseType resp2 = logout(lrt);
				
        lt = new LoginType();
        lt.setSessionID((String) s.getAttribute(Definitions.ADMIN_SESS_ID));
        lrt.setLogin(lt);
        LogoutResponseType resp1 = logout(lrt);
                
        Sessions.getCurrent().invalidate(); //Korrekte Umleitung!!!
        
        String[] pathRes = new String[10];
        pathRes[0] = DBSysParamCollab.instance().getStringValue("redirectStartPage", null, null);
        if((loggedin != null) && (loggedin.iterator().hasNext()))
        {
            String path = loggedin.iterator().next();
            if(path.contains("/TermAdmin")){
                pathRes = path.split("/TermAdmin");
            }else{
                pathRes = path.split("/TermBrowser");
            }
        }
        
        //redirects to TermAdmin, where TermAdmin deletes Session and redirects to TermBrowser
        Executions.sendRedirect(pathRes[0] +"/TermAdmin/logout.zul"); 
    }

    public static LogoutResponseType logout(de.fhdo.terminologie.ws.authorization.LogoutRequestType parameter)
    {
        //de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
        de.fhdo.terminologie.ws.authorization.Authorization port = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();
        return port.logout(parameter);
    }
}
