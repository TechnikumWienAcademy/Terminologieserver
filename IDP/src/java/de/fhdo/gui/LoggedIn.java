/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui;

import de.fhdo.Definitions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author
 */
public class LoggedIn extends Window {
      

    public boolean isLoggedIn() {
        return Sessions.getCurrent().hasAttribute(Definitions.ADMIN_SESS_ID);
    }
}
