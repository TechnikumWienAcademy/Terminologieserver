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
package de.fhdo.terminologie.ws.sso;

import de.fhdo.terminologie.ws.sso.types.MPseudonymRequestType;
import de.fhdo.terminologie.ws.sso.types.MPseudonymResponseType;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Philipp Urbauer
 */
@WebService(serviceName = "SSo")
public class SSo {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "MPseudonym")
    public MPseudonymResponseType MPseudonym(@WebParam(name = "parameter") MPseudonymRequestType parameter) {
        MPseudonym mPseudonym = new MPseudonym();
        return mPseudonym.manipulate(parameter);
    }
}
