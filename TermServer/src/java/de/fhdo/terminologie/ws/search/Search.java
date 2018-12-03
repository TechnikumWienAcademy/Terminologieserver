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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesResponseType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.search.types.ListDomainsRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainsResponseType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnVsConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnVsConceptMetadataResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 * Search umfasst das Retrieval von Vokabularen, Konzepten sowie ValueSets.
 * 
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 * @version 1.0
 */
@WebService(serviceName = "Search")
public class Search
{
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.
  @Resource
  private WebServiceContext webServiceContext;
  //3.2.21 start
  @Resource
  private boolean listCodeSystemsPubRunning;
  @Resource
  private ListCodeSystemsResponseType listCodeSystemsPubReturn;
  
  @WebMethod(operationName = "checkListCodeSystemsPubRunning")
  public boolean checkListCodeSystemsPubRunning(){
      return listCodeSystemsPubRunning;
  }
  
  @WebMethod(operationName= "getListCodeSystemsPubReturn")
  public ListCodeSystemsResponseType getListCodeSystemsPubReturn(){
      return listCodeSystemsPubReturn;
  }
  
  @WebMethod(operationName = "ListCodeSystemsPub")
  public void ListCodeSystemsPub(@WebParam(name = "parameter") ListCodeSystemsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    ListCodeSystems lcs = new ListCodeSystems();
    listCodeSystemsPubReturn = lcs.ListCodeSystems(parameter);
  }  
  //3.2.21 end
  
  /**
   * <b>Liefert alle verfügbaren Vokabulare aus dem Terminologieserver.</b><br>
   * Vokabulare mit Lizenzen werden nur den angemeldeten 
   * Benutzern angezeigt, welche Zugriff auf die entsprechenden Lizenzen haben.
   * 
   * @param parameter Anfrage-Parameter
   * @return Antwort
   */
  @WebMethod(operationName = "ListCodeSystems")
  public ListCodeSystemsResponseType ListCodeSystems(@WebParam(name = "parameter") ListCodeSystemsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    ListCodeSystems lcs = new ListCodeSystems();
    return lcs.ListCodeSystems(parameter);
  }

  //3.2.21 start
  @Resource
  private boolean listValueSetsPubRunning;
  @Resource
  private ListValueSetsResponseType listValueSetsPubRespone;
  
  @WebMethod(operationName = "isListValueSetsPubRunning")
  public boolean isListValueSetsPubRunning(){
      return listValueSetsPubRunning;
  }
  
  @WebMethod(operationName= "getListValueSetsPubRespone")
  public ListValueSetsResponseType getListValueSetsPubRespone(){
      return listValueSetsPubRespone;
  }
  
  @WebMethod(operationName = "ListValueSetsPub")
  public void ListValueSetsPub(@WebParam(name = "parameter") ListValueSetsRequestType parameter)
  {
    listValueSetsPubRunning = true;
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    listValueSetsPubRespone = new ListValueSets().ListValueSets(parameter);
    listValueSetsPubRunning = false;
  }  
  //3.2.21 end
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListValueSets")
  public ListValueSetsResponseType ListValueSets(@WebParam(name = "parameter") ListValueSetsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListValueSets().ListValueSets(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnValueSetDetails")
  public ReturnValueSetDetailsResponseType ReturnValueSetDetails(@WebParam(name = "parameter") ReturnValueSetDetailsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ReturnValueSetDetails().ReturnValueSetDetails(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnConceptDetails")
  public ReturnConceptDetailsResponseType ReturnConceptDetails(@WebParam(name = "parameter") ReturnConceptDetailsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ReturnConceptDetails().ReturnConceptDetails(parameter);
  }

  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnCodeSystemDetails")
  public ReturnCodeSystemDetailsResponseType ReturnCodeSystemDetails(@WebParam(name = "parameter") ReturnCodeSystemDetailsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ReturnCodeSystemDetails().ReturnCodeSystemDetails(parameter);
  }
  
 

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListValueSetContents")
  public ListValueSetContentsResponseType ListValueSetContents(@WebParam(name = "parameter") ListValueSetContentsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListValueSetContents().ListValueSetContents(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListCodeSystemConcepts")
  public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(@WebParam(name = "parameter") ListCodeSystemConceptsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListCodeSystemConcepts().ListCodeSystemConcepts(parameter, false);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListConceptAssociationTypes")
  public ListConceptAssociationTypesResponseType ListConceptAssociationTypes(@WebParam(name = "parameter") ListConceptAssociationTypesRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListConceptAssociationTypes().ListConceptAssociationTypes(parameter);
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListDomains")
  public ListDomainsResponseType ListDomains(@WebParam(name = "parameter") ListDomainsRequestType parameter)
  {
    return new ListDomains().ListDomains(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnConceptAssociationTypeDetails")
  public ReturnConceptAssociationTypeDetailsResponseType ReturnConceptAssociationTypeDetails(@WebParam(name = "parameter") ReturnConceptAssociationTypeDetailsRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ReturnConceptAssociationTypeDetails().ReturnConceptAssociationTypeDetails(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListDomainValues")
  public ListDomainValuesResponseType ListDomainValues(@WebParam(name = "parameter") ListDomainValuesRequestType parameter)
  {
    return new ListDomainValues().ListDomainValues(parameter);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListCodeSystemsInTaxonomy")
  public ListCodeSystemsInTaxonomyResponseType ListCodeSystemsInTaxonomy(@WebParam(name = "parameter") ListCodeSystemsInTaxonomyRequestType parameter)
  {
    if(parameter != null)
      SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    return new ListCodeSystemsInTaxonomy().ListCodeSystemsInTaxonomy(parameter);
  }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "ReturnVsConceptMetadata")
    public ReturnVsConceptMetadataResponseType ReturnVsConceptMetadata(@WebParam(name = "parameter") ReturnVsConceptMetadataRequestType parameter) {
        if(parameter != null)
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new ReturnVsConceptMetadata().ReturnVsConceptMetadata(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "ListMetadataParameter")
    public ListMetadataParameterResponseType ListMetadataParameter(@WebParam(name = "parameter") ListMetadataParameterRequestType parameter) {
        if(parameter != null)
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new ListMetadataParameter().ListMetadataParameter(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "ListValueSetContentsByTermOrCode")
    public ListValueSetContentsByTermOrCodeResponseType ListValueSetContentsByTermOrCode(@WebParam(name = "parameter") ListValueSetContentsByTermOrCodeRequestType parameter) {
        if(parameter != null)
        SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new ListValueSetContentsByTermOrCode().ListValueSetContentsByTermOrCode(parameter);
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "ReturnConceptValueSetMembership")
    public ReturnConceptValueSetMembershipResponseType ReturnConceptValueSetMembership(@WebParam(name = "parameter") ReturnConceptValueSetMembershipRequestType parameter) {
        if(parameter != null)
            SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new ReturnConceptValueSetMembership().ReturnConceptValueSetMembership(parameter);
    }

    
    //3.2.21 start
    @Resource
    private boolean listGloballySearchedConceptsRunning;
    @Resource
    private ListGloballySearchedConceptsResponseType listGloballySearchedConceptsResponse;
    
    @WebMethod(operationName = "isListGloballySearchedConceptsRunning")
    public boolean isListGloballySearchedConceptsRunning(){
        return listGloballySearchedConceptsRunning;
    }
    
    @WebMethod(operationName = "getListGloballySearchedConceptsResponse")
    public ListGloballySearchedConceptsResponseType getListGloballySearchedConceptsResponse(){
        return listGloballySearchedConceptsResponse;
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "ListGloballySearchedConceptsPub")
    public void ListGloballySearchedConceptsPub(@WebParam(name = "parameter") ListGloballySearchedConceptsRequestType parameter) {
        listGloballySearchedConceptsRunning = true;
        if(parameter != null)
            SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        listGloballySearchedConceptsResponse = new ListGloballySearchedConcepts().ListGloballySearchedConcepts(parameter, false);
        listGloballySearchedConceptsRunning = false;
    }
    //3.2.21 end 
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "ListGloballySearchedConcepts")
    public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(@WebParam(name = "parameter") ListGloballySearchedConceptsRequestType parameter) {
        if(parameter != null)
            SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
        return new ListGloballySearchedConcepts().ListGloballySearchedConcepts(parameter, false);
    }
}

