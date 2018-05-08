/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.hibernate.TermUser;

/**
 *
 * @author PU
 */
public class UserPackage {
 
    private Collaborationuser user;
    private TermUser userTerm;

    public Collaborationuser getUser() {
        return user;
    }

    public void setUser(Collaborationuser user) {
        this.user = user;
    }

    public TermUser getUserTerm() {
        return userTerm;
    }

    public void setUserTerm(TermUser userTerm) {
        this.userTerm = userTerm;
    }
    
    
}
