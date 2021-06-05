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
package de.fhdo.gui.admin;

import de.fhdo.db.DBSysParam;
import de.fhdo.helper.SAMLHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.login.RequestBuilder;
import de.fhdo.login.SecurityFilter;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.CheckLoginResponse;
import de.fhdo.terminologie.ws.authorization.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.LoginType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opensaml.saml2.core.AuthnRequest;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Admin extends Window implements AfterCompose
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public Admin()
    {
        LoginRequestType request = new LoginRequestType();
        request.setLogin(new LoginType());
        request.getLogin().setSessionID(SessionHelper.getSessionAttributeByName("session_id").toString());
        Authorization port_authorization = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();

        if (port_authorization != null)
        {
            try
            {
                //TODO endloßschleife
                CheckLoginResponse.Return response = port_authorization.checkLogin(request);

                if (!response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorization.Status.OK))
                {
                    String weblink = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("weblink", null, null);
                    String[] pureLink = weblink.split("/TermBrowser");
                    String idp_url = DBSysParam.instance().getStringValue("idp_url", null, null);
                    AuthnRequest ar = RequestBuilder.buildAuthnRequest(idp_url + "/IDP",
                            pureLink[0] + "/TermAdmin/assertionConsumer", pureLink[0] + "/TermAdmin");
                    
                    try
                    {
                        SAMLHelper.logSAMLObject(ar);
                        String marshalled = SAMLHelper.marshallElement(ar);
                        String encoded = SAMLHelper.encode(marshalled);
                        Executions.getCurrent().sendRedirect(idp_url + "/IDP/?sso=" + encoded);
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            catch (Exception ex)
            {
                logger.error(ex);
            }
        }
    }

    public void onNavigationSelect(SelectEvent event)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("onNavigationSelect()");
        }

        logger.debug("class: " + event.getReference().getClass().getCanonicalName());
        Tab tab = (Tab) event.getReference();
        tabSelected(tab.getId());
    }

    private void tabSelected(String ID)
    {
        if (ID == null || ID.length() == 0)
        {
            return;
        }

        if (ID.equals("tabBenutzer"))
        {
            includePage("incBenutzer", "/gui/admin/modules/user.zul");
        }
        else if (ID.equals("tabDomains"))
        {
            includePage("incDomains", "/gui/admin/modules/domain.zul");
        }
        else if (ID.equals("tabImport"))
        {
            includePage("incImport", "/gui/admin/modules/import.zul");
        }
        else if (ID.equals("tabTerminologie"))
        {
            includePage("incTerminologie", "/gui/admin/modules/terminologie.zul");
        }
        else if (ID.equals("tabCodesysteme"))
        {
            includePage("incCodesysteme", "/gui/admin/modules/codesysteme.zul");
        }
        else if (ID.equals("tabLizenzen"))
        {
            includePage("incLizenzen", "/gui/admin/modules/lizenzen.zul");
        }
        else if (ID.equals("tabDB"))
        {
            includePage("incDB", "/gui/admin/modules/datenbank.zul");
        }
        else if (ID.equals("tabSysParam"))
        {
            includePage("incSysParam", "/gui/admin/modules/sysParam.zul");
        }
        else if (ID.equals("tabKollaboration"))
        {
            includePage("incKollaboration", "/gui/admin/modules/collaboration/kollaboration.zul");
        }
        else if (ID.equals("tabUserManagement"))
        {
            includePage("incUserManagement", "/gui/admin/modules/userManagement.zul");
        }
        else
        {
            logger.debug("ID nicht bekannt: " + ID);
        }

        SessionHelper.setValue("termadmin_tabid", ID);
    }

    private void includePage(String ID, String Page)
    {
        try
        {
            Include inc = (Include) getFellow(ID);
            inc.setSrc(null);

            logger.debug("includePage: " + ID + ", Page: " + Page);
            inc.setSrc(Page);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void afterCompose()
    {   
        String id = "";

        //init WebServices
        WebServiceUrlHelper.getInstance();

        Object o = SessionHelper.getSessionAttributeByName("termadmin_tabid");
        if (o != null)
        {
            id = o.toString();
        }

        if (id != null && id.length() > 0)
        {
            logger.debug("Goto Page: " + id);
            try
            {
                Tabbox tb = (Tabbox) getFellow("tabboxNavigation");
                //Tabpanel panel = (Tabpanel) getFellow("tabboxNavigation");
                Tab tab = (Tab) getFellow(id);
                int index = tab.getIndex();
                logger.debug("Index: " + index);

                tb.setSelectedIndex(index);

                tabSelected(id);
            }
            catch (Exception e)
            {
                tabSelected("tabKollaboration");
                logger.warn(e.getMessage());
            }
        }
        else
        {
            tabSelected("tabKollaboration");
        }
    }
}
