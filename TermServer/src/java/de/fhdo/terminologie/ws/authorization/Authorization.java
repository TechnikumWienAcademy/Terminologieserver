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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.authorization.types.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.types.LoginResponseType;
import de.fhdo.terminologie.ws.authorization.types.LogoutRequestType;
import de.fhdo.terminologie.ws.authorization.types.LogoutResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "Authorization")
public class Authorization
{
  @Resource
  private WebServiceContext webServiceContext;
  
  @WebMethod(operationName = "Login")
  public LoginResponseType Login(@WebParam(name = "parameter") LoginRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    Login login = new Login();
    return login.Login(parameter);
  }
  
  @WebMethod(operationName = "Logout")
  public LogoutResponseType Logout(@WebParam(name = "parameter") LogoutRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    Logout logout = new Logout();
    return logout.Logout(parameter);
  }
  
  @WebMethod(operationName = "checkLogin")
  public LoginResponseType checkLogin(@WebParam(name = "parameter") LoginRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    Login login = new Login();
    return login.checkLogin(parameter);
  }
}
