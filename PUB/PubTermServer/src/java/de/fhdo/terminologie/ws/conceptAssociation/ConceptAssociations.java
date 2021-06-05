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
package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "ConceptAssociations")
public class ConceptAssociations
{
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.
  @Resource
  private WebServiceContext webServiceContext;
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateConceptAssociation")
  public CreateConceptAssociationResponseType CreateConceptAssociation(@WebParam(name = "parameter") CreateConceptAssociationRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new CreateConceptAssociation().CreateConceptAssociation(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListConceptAssociations")
  public ListConceptAssociationsResponseType ListConceptAssociations(@WebParam(name = "parameter") ListConceptAssociationsRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListConceptAssociations().ListConceptAssociations(parameter);
  }
  
  /**
  * Web service operation
  */
  @WebMethod(operationName = "ReturnConceptAssociationDetails")
  public ReturnConceptAssociationDetailsResponseType ReturnConceptAssociationDetails(@WebParam(name = "parameter") ReturnConceptAssociationDetailsRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ReturnConceptAssociationDetails().ReturnConceptAssociationDetails(parameter);
  }

 
  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConceptAssociation")
  public MaintainConceptAssociationResponseType MaintainConceptAssociation(@WebParam(name = "parameter") MaintainConceptAssociationRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new MaintainConceptAssociation().MaintainConceptAssociation(parameter);
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "TraverseConceptToRoot")
  public TraverseConceptToRootResponseType TraverseConceptToRoot(@WebParam(name = "parameter") TraverseConceptToRootRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new TraverseConceptToRoot().TraverseConceptToRoot(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateConceptAssociationStatus")
  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(@WebParam(name = "parameter") UpdateConceptAssociationStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new UpdateConceptAssociationStatus().UpdateConceptAssociationStatus(parameter);
  }
}
