/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.hibernate.TermUser;

/**
 *
 * @author puraner
 */
public class DeleteUserRequestType
{
    private LoginType loginType;
    private TermUser termuser;
    private Collaborationuser collaborationUser;

    public LoginType getLoginType()
    {
        return loginType;
    }

    public void setLoginType(LoginType loginType)
    {
        this.loginType = loginType;
    }

    public TermUser getTermuser()
    {
        return termuser;
    }

    public void setTermuser(TermUser termuser)
    {
        this.termuser = termuser;
    }

    public Collaborationuser getCollaborationUser()
    {
        return collaborationUser;
    }

    public void setCollaborationUser(Collaborationuser collaborationUser)
    {
        this.collaborationUser = collaborationUser;
    }
    
    
}
