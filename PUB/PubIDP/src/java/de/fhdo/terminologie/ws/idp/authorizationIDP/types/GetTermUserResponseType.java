/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author puraner
 */
@XmlRootElement
@XmlType(name = "", propOrder =
{
    "returnInfos",
    "userList"
})
public class GetTermUserResponseType
{
    
    private ReturnType returnInfos;
    private List<de.fhdo.db.hibernate.TermUser> userList;

    public ReturnType getReturnInfos()
    {
        return returnInfos;
    }

    public void setReturnInfos(ReturnType returnInfos)
    {
        this.returnInfos = returnInfos;
    }

    public List<de.fhdo.db.hibernate.TermUser> getUserList()
    {
        return userList;
    }

    public void setUserList(List<de.fhdo.db.hibernate.TermUser> userList)
    {
        this.userList = userList;
    }
}
