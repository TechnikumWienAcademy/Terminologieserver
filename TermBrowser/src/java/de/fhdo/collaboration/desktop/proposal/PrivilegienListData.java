/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.desktop.proposal;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Privilege;

/**
 *
 * @author PU
 */
public class PrivilegienListData {
    
    private Collaborationuser user = null;
    private Boolean isCreator = false;
    private Boolean isAdmin = false;
    private Discussiongroup discussiongroup = null;
    private Privilege privilege;

    public Collaborationuser getUser() {
        return user;
    }

    public void setUser(Collaborationuser user) {
        this.user = user;
    }

    public Discussiongroup getDiscussiongroup() {
        return discussiongroup;
    }

    public void setDiscussiongroup(Discussiongroup discussiongroup) {
        this.discussiongroup = discussiongroup;
    }

    public Boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(Boolean isCreator) {
        this.isCreator = isCreator;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }
}
