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
package de.fhdo.collaboration;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.helper.Password;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.terminologie.ws.idp.authorizationidp.Status;
import de.fhdo.terminologie.ws.idp.authorizationidp.AuthorizationIDP;
import de.fhdo.terminologie.ws.idp.authorizationidp.AuthorizationIDP_Service;
import de.fhdo.terminologie.ws.idp.authorizationidp.ChangePasswordRequestType;
import de.fhdo.terminologie.ws.idp.authorizationidp.ChangePasswordResponse;
import de.fhdo.terminologie.ws.idp.authorizationidp.ChangePasswordResponseType;
import de.fhdo.terminologie.ws.idp.authorizationidp.LoginType;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class PasswordDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private IUpdate updateListInterface;

    public PasswordDetails()
    {
        //Map args;

        try
        {
            logger.debug("OrganisationPersonDetails() - Konstruktor");
            //args = Executions.getCurrent().getArg();

        }
        catch (Exception e)
        {
            logger.error(e.getLocalizedMessage());
        }

        /*try
    {
    patientID = Long.parseLong(args.get("patientID").toString());
    logger.debug("Patient-ID: " + patientID);*/
    }

    /**
     * Im Anmeldefenster wurde "Return" gedrückt
     *
     * @param event
     */
    public void onOkPressed(KeyEvent event)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Enter gedrueckt!");
        }

        Button b = (Button) getFellow("okButton");
        b.setDisabled(true);

        onOkClicked();

        b.setDisabled(false);
    }

    public void onOkClicked()
    {
        Textbox tb = (Textbox) getFellow("pwAlt");
        Textbox tb1 = (Textbox) getFellow("pw1");
        Textbox tb2 = (Textbox) getFellow("pw2");

        if (!tb1.getValue().equals(tb2.getValue()))
        {
            Messagebox.show("Das eingegebene Passwort stimmt nicht überein!", "Fehler", Messagebox.OK, Messagebox.ERROR);
            return;
        }

        try
        {
            /*String url = DBSysParam.instance().getStringValue("idp_url", null, null) + "/IDP/AuthorizationIDP?wsdl";
            URL newEndpoint = new URL(url);
            QName qname = new QName("http://authorizationIDP.idp.ws.terminologie.fhdo.de/", "AuthorizationIDP");*/

            logger.info("Change Password gestartet");

            //AuthorizationIDP_Service service = new AuthorizationIDP_Service(newEndpoint, qname);
            AuthorizationIDP port = WebServiceUrlHelper.getInstance().getAuthorizationIdpServicePort();

            ChangePasswordRequestType request = new ChangePasswordRequestType();
            request.setLogin(new LoginType());
            request.getLogin().setSessionID(SessionHelper.getValue("session_id").toString());
            request.setOldPassword(tb.getValue());
            request.setNewPassword(tb1.getValue());

            ChangePasswordResponse.Return response = port.changePassword(request);

            if (response.getReturnInfos().getStatus().equals(Status.OK))
            {
                Messagebox.show("Passwort erfolgreich geändert.", "Passwort Ändern", Messagebox.OK, Messagebox.INFORMATION);
            }
            else
            {
                Messagebox.show(response.getReturnInfos().getMessage(), "Fehler", Messagebox.OK, Messagebox.ERROR);
            }

            tb1.setValue("");
            tb2.setValue("");
            tb.setValue("");

            this.setVisible(false);

            this.detach();
        }
        catch (Exception e)
        {
            // Fehlermeldung ausgeben
            logger.error("Fehler in onOkClicked(): " + e.getMessage());
        }
    }

    public void onCancelClicked()
    {
        this.setVisible(false);
        this.detach();

        //Executions.getCurrent().setAttribute("contactPerson_controller", null);
    }

    public void afterCompose()
    {
        //throw new UnsupportedOperationException("Not supported yet.");

        //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
    }

    /**
     * @param updateListInterface the updateListInterface to set
     */
    public void setUpdateListInterface(IUpdate updateListInterface)
    {
        this.updateListInterface = updateListInterface;
    }

}
