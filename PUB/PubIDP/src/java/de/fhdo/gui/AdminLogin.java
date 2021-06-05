/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui;

import de.fhdo.Definitions;
import de.fhdo.helper.LoginHelperAdmin;
import de.fhdo.helper.PropertiesHelperAdmin;

/**
 *
 * @author Christoph
 */
public class AdminLogin extends AbstractLogin{

    public AdminLogin() {
        super(PropertiesHelperAdmin.getInstance(), LoginHelperAdmin.getInstance(), Definitions.ADMIN_SESS_ID, Definitions.ADMIN_SESS_ASSERTION);
        
    }

    
}
