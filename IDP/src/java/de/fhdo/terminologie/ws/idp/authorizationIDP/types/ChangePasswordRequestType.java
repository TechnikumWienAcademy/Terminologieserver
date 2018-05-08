/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

/**
 *
 * @author puraner
 */
public class ChangePasswordRequestType
{

    private LoginType login;
    private String newPassword;
    private String oldPassword;

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public ChangePasswordRequestType()
    {
        
    }

    public LoginType getLogin()
    {
        return login;
    }

    public void setLogin(LoginType login)
    {
        this.login = login;
    }
}
