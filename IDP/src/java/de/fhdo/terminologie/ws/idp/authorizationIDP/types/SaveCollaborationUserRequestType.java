/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.db.hibernate.TermUser;

/**
 *
 * @author puraner
 */
public class SaveCollaborationUserRequestType
{
    private LoginType loginType;
    private Collaborationuser user;
    private TermUser termuser;
    private Role role;
    private boolean newEntry = true;

    public LoginType getLoginType()
    {
        return loginType;
    }

    public void setLoginType(LoginType loginType)
    {
        this.loginType = loginType;
    }

    public Collaborationuser getUser()
    {
        return user;
    }

    public void setUser(Collaborationuser user)
    {
        this.user = user;
    }

    public boolean isNewEntry()
    {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry)
    {
        this.newEntry = newEntry;
    }

    public TermUser getTermuser()
    {
        return termuser;
    }

    public void setTermuser(TermUser termuser)
    {
        this.termuser = termuser;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }
    
    
}
