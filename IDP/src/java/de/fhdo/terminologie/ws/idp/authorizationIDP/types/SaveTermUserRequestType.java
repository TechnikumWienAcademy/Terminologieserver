/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

import de.fhdo.db.hibernate.TermUser;

/**
 *
 * @author puraner
 */
public class SaveTermUserRequestType
{
    private LoginType loginType;
    private TermUser termUser;
    private boolean newEntry = true;

    public LoginType getLoginType()
    {
        return loginType;
    }

    public void setLoginType(LoginType loginType)
    {
        this.loginType = loginType;
    }

    public TermUser getTermUser()
    {
        return termUser;
    }

    public void setTermUser(TermUser termUser)
    {
        this.termUser = termUser;
    }

    public boolean isNewEntry()
    {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry)
    {
        this.newEntry = newEntry;
    }
}
