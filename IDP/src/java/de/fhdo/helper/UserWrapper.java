/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.db.hibernate.TermUser;
import java.util.Set;

/**
 *
 * @author Christoph
 */
public class UserWrapper {

    private final Long ID;
    private final String name;
    private final String pass;
    private final String salt;
    private final Set<Role> roles;
    private final boolean admin;

    public UserWrapper(Collaborationuser user) {
        this.pass = user.getPassword();
        this.salt = user.getSalt();
        this.name = user.getUsername();
        this.ID = user.getId();
        roles = user.getRoles();
        this.admin = false;
    }

    public UserWrapper(TermUser user) {
        this.pass = user.getPassw();
        this.salt = user.getSalt();
        this.name = user.getName();
        this.ID = user.getId();
        this.admin = true;
        this.roles = null;
    }
    public Long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
