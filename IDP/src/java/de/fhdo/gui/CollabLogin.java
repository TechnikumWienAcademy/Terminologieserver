/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.gui;

import de.fhdo.Definitions;
import de.fhdo.helper.LoginHelperCollab;
import de.fhdo.helper.PropertiesHelperCollab;

/**
 *
 * @author Christoph
 */
public class CollabLogin extends AbstractLogin {

    public CollabLogin() {
        super(PropertiesHelperCollab.getInstance(), LoginHelperCollab.getInstance(), Definitions.COLLAB_SESS_ID, Definitions.COLLAB_SESS_ASSERTION);
    }
}
