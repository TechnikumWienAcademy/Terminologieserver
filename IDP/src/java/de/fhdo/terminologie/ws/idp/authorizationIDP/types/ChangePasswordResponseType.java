/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

//import de.fhdo.db.hibernate.TermUser;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Mathias
 */
@XmlRootElement
@XmlType(name = "", propOrder =
{
    "returnInfos"
})//, "termUser"})
public class ChangePasswordResponseType
{

    private ReturnType returnInfos;

    public ChangePasswordResponseType()
    {
    }

    /**
     * @return the returnInfos
     */
    public ReturnType getReturnInfos()
    {
        return returnInfos;
    }

    /**
     * @param returnInfos the returnInfos to set
     */
    public void setReturnInfos(ReturnType returnInfos)
    {
        this.returnInfos = returnInfos;
    }
}
