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
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "Authoring")
public class Authoring
{
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.

  @Resource
  private WebServiceContext webServiceContext;
  //3.2.21 start
  @Resource
  private boolean removeTerminologyOrConceptPubRunning;
  @Resource
  private RemoveTerminologyOrConceptResponseType removeTerminologyOrConceptPubResponse;
    
  
    @WebMethod(operationName = "getRemoveTerminologyOrConceptPubRunning")
    public boolean getRemoveTerminologyOrConceptRunnning(){
        return removeTerminologyOrConceptPubRunning;
    }
    
    @WebMethod(operationName = "getRemoveTerminologyOrConceptPubResponse")
    public RemoveTerminologyOrConceptResponseType getRemoveTerminologyOrConceptPubResponse(){
        return removeTerminologyOrConceptPubResponse;
    }
  
    /**
     * Web service operation
     */
    @WebMethod(operationName = "RemoveTerminologyOrConceptPub")
    public void RemoveTerminologyOrConceptPub(@WebParam(name = "parameter") RemoveTerminologyOrConceptRequestType parameter) {
        removeTerminologyOrConceptPubRunning = true;
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        removeTerminologyOrConceptPubResponse = new RemoveTerminologyOrConcept().RemoveTerminologyOrConcept(parameter);
        removeTerminologyOrConceptPubRunning = false;
    }
    
    //3.2.21 end
    //3.2.21 start
     @Resource
    private boolean createCodeSystemPubRunning;
    @Resource
    private CreateCodeSystemResponseType createCodeSystemPubResponse;
    
    @WebMethod(operationName = "getCreateCodeSystemPubRunning")
    public boolean getCreateCodeSystemPubRunning(){
        return createCodeSystemPubRunning;
    }
    
    @WebMethod(operationName = "getCreateCodeSystemPubResponse")
    public CreateCodeSystemResponseType getCreateCodeSystemPubResponse(){
        return createCodeSystemPubResponse;
    }
  
    /**
     * Web service operation
     */
    @WebMethod(operationName = "CreateCodeSystemPub")
    public void CreateCodeSystemPub(@WebParam(name = "parameter") CreateCodeSystemRequestType parameter)
    {
      createCodeSystemPubRunning = true;
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
      CreateCodeSystem ccs = new CreateCodeSystem();
      createCodeSystemPubResponse = ccs.CreateCodeSystem(parameter);
      createCodeSystemPubRunning = false;
    }
    //3.2.21 end
    
  @WebMethod(operationName = "CreateCodeSystem")
  public CreateCodeSystemResponseType CreateCodeSystem(@WebParam(name = "parameter") CreateCodeSystemRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    CreateCodeSystem ccs = new CreateCodeSystem();
    return ccs.CreateCodeSystem(parameter);
  }

  //3.2.21 start
  @Resource
  private boolean createValueSetPubRunning;
  @Resource
  private CreateValueSetResponseType createValueSetPubResponse;
  
  @WebMethod(operationName = "isCreateValueSetPubRunning")
  public boolean isCreateValueSetPubRunning(){
      return createValueSetPubRunning;
  }
  
  @WebMethod(operationName = "getCreateValueSetPubResponse")
  public CreateValueSetResponseType getCreateValueSetPubResponse(){
      return createValueSetPubResponse;
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSetPub")
  public void CreateValueSetPub(@WebParam(name = "parameter") CreateValueSetRequestType parameter)
  {
    createValueSetPubRunning = true;
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    createValueSetPubResponse =  new CreateValueSet().CreateValueSet(parameter);
    createValueSetPubRunning = false;
  }
  //3.2.21 end
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSet")
  public CreateValueSetResponseType CreateValueSet(@WebParam(name = "parameter") CreateValueSetRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new CreateValueSet().CreateValueSet(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainValueSet")
  public MaintainValueSetResponseType MaintainValueSet(@WebParam(name = "parameter") MaintainValueSetRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new MaintainValueSet().MaintainValueSet(parameter);
  }

  @WebMethod(operationName = "CreateConcept")
  public CreateConceptResponseType CreateConcept(@WebParam(name = "parameter") CreateConceptRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new CreateConcept().CreateConcept(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateConceptAssociationType")
  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(@WebParam(name = "parameter") CreateConceptAssociationTypeRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new CreateConceptAssociationType().CreateConceptAssociationType(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSetContent")
  public CreateValueSetContentResponseType CreateValueSetContent(@WebParam(name = "parameter") CreateValueSetContentRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new CreateValueSetContent().CreateValueSetContent(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateCodeSystemVersionStatus")
  public UpdateCodeSystemVersionStatusResponseType operation(@WebParam(name = "parameter") UpdateCodeSystemVersionStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new UpdateCodeSystemVersionStatus().UpdateCodeSystemVersionStatus(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateConceptStatus")
  public UpdateConceptStatusResponseType UpdateConceptStatus(@WebParam(name = "parameter") UpdateConceptStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new UpdateConceptStatus().UpdateConceptStatus(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateValueSetStatus")
  public UpdateValueSetStatusResponseType UpdateValueSetStatus(@WebParam(name = "parameter") UpdateValueSetStatusRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new UpdateValueSetStatus().updateValueSetStatus(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainCodeSystemVersion")
  public MaintainCodeSystemVersionResponseType MaintainCodeSystemVersion(@WebParam(name = "parameter") MaintainCodeSystemVersionRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new MaintainCodeSystemVersion().MaintainCodeSystemVersion(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConceptAssociationType")
  public MaintainConceptAssociationTypeResponseType MaintainConceptAssociationType(@WebParam(name = "parameter") MaintainConceptAssociationTypeRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new MaintainConceptAssociationType().MaintainConceptAssociationType(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConcept")
  public MaintainConceptResponseType MaintainConcept(@WebParam(name = "parameter") MaintainConceptRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new MaintainConcept().MaintainConcept(parameter);
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "RemoveValueSetContent")
  public RemoveValueSetContentResponseType RemoveValueSetContent(@WebParam(name = "parameter") RemoveValueSetContentRequestType parameter)
  {
    SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new RemoveValueSetContent().RemoveValueSetContent(parameter);
  }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "CreateValueSetConceptMetadataValue")
    public CreateValueSetConceptMetadataValueResponseType CreateValueSetConceptMetadataValue(@WebParam(name = "parameter") CreateValueSetConceptMetadataValueRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new CreateValueSetConceptMetadataValue().CreateValueSetConceptMetadataValue(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "DeleteValueSetConceptMetadataValue")
    public DeleteValueSetConceptMetadataValueResponseType DeleteValueSetConceptMetadataValue(@WebParam(name = "parameter") DeleteValueSetConceptMetadataValueRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new DeleteValueSetConceptMetadataValue().DeleteValueSetConceptMetadataValue(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "MaintainValueSetConceptMetadataValue")
    public MaintainValueSetConceptMetadataValueResponseType MaintainValueSetConceptMetadataValue(@WebParam(name = "parameter") MaintainValueSetConceptMetadataValueRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new MaintainValueSetConceptMetadataValue().MaintainValueSetConceptMetadataValue(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "MaintainCodeSystemConceptMetadataValue")
    public MaintainCodeSystemConceptMetadataValueResponseType MaintainCodeSystemConceptMetadataValue(@WebParam(name = "parameter") MaintainCodeSystemConceptMetadataValueRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new MaintainCodeSystemConceptMetadataValue().MaintainCodeSystemConceptMetadataValue(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "UpdateConceptValueSetMembershipStatus")
    public UpdateConceptValueSetMembershipStatusResponseType UpdateConceptValueSetMembershipStatus(@WebParam(name = "parameter") UpdateConceptValueSetMembershipStatusRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new UpdateConceptValueSetMembershipStatus().UpdateConceptValueSetMembershipStatus(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "MaintainConceptValueSetMembership")
    public MaintainConceptValueSetMembershipResponseType MaintainConceptValueSetMembership(@WebParam(name = "parameter") MaintainConceptValueSetMembershipRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new MaintainConceptValueSetMembership().MaintainConceptValueSetMembership(parameter);
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "RemoveTerminologyOrConcept")
    public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(@WebParam(name = "parameter") RemoveTerminologyOrConceptRequestType parameter) {
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new RemoveTerminologyOrConcept().RemoveTerminologyOrConcept(parameter);
    }
}
