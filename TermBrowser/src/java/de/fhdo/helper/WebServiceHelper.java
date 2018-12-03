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
package de.fhdo.helper;

import de.fhdo.terminologie.ws.administration.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.Administration;
import de.fhdo.terminologie.ws.administration.Administration_Service;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetResponse;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.Authorization_Service;
import de.fhdo.terminologie.ws.authorization.LoginRequestType;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations_Service;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.ListDomainValuesResponse;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsResponse;
import de.fhdo.terminologie.ws.search.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.ListMetadataParameterResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsByTermOrCodeResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnVsConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnVsConceptMetadataResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class works like an interface between the webservices and the rest of the application.
 * You can call webservices with three parameter sets.
 * 1.: Just with the request parameter.
 * 2.: With the request parameter and the host URL.
 * 3.: With the request parameter, the host URL and the service URL.
 * 
 * All of the methods of this class work analogously.
 * At first they check and transform the URL, so that it conforms to webservice format.
 * Then they call the webservice and return the value which was returned by the webservice.
 * @author Becker
 */
public class WebServiceHelper {
    final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter){
        return exportCodeSystemContent(parameter, SessionHelper.getHostUrl());
    }    
    public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter, String urlHost){
        return exportCodeSystemContent(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter, String urlHost, String urlService) {                
        Administration_Service  service;
        Administration          port;
        try {
            //transform URLs            
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";       
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.exportCodeSystemContent(parameter);
    }
    
    public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter){
        return actualProceedings(parameter, SessionHelper.getHostUrl());
    }    
    public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter, String urlHost){
        return actualProceedings(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter, String urlHost, String urlService) {                
        Administration_Service  service;
        Administration          port;
        try { 
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);         
        }        
        port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.actualProceedings(parameter);
    }
    
    public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter){
        return exportValueSetContent(parameter, SessionHelper.getHostUrl());
    }    
    public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter, String urlHost){
        return exportValueSetContent(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter, String urlHost, String urlService) {                
        Administration_Service  service;
        Administration          port;
        try {      
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getAdministrationServicePort();
        return port.exportValueSetContent(parameter);
    }
        
    // Authoring ///////////////////////////////////////////////////////////////
    public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter){
        return createConcept(parameter, SessionHelper.getHostUrl());
    }    
    public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter, String urlHost){
        return createConcept(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);           
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.createConcept(parameter);
    }
    
    public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter){
        return removeValueSetContent(parameter, SessionHelper.getHostUrl());
    }    
    public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter, String urlHost){
        return removeValueSetContent(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {        
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";         
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.removeValueSetContent(parameter);
    }    
    
    public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter){
        return createValueSetContent(parameter, SessionHelper.getHostUrl());
    }    
    public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter, String urlHost){
        return createValueSetContent(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {     
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";    
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.createValueSetContent(parameter);
    }
    
    public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter){
        return createCodeSystem(parameter, SessionHelper.getHostUrl());
    } 
    public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter, String urlHost){
        return createCodeSystem(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {      
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";    
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.createCodeSystem(parameter);
    }
    
    public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter){
        return createValueSet(parameter, SessionHelper.getHostUrl());
    }    
    public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter, String urlHost){
        return createValueSet(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";       
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.createValueSet(parameter);
    }
    
    public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter){
        return maintainCodeSystemVersion(parameter, SessionHelper.getHostUrl());
    }    
    public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter, String urlHost){
        return maintainCodeSystemVersion(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";         
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.maintainCodeSystemVersion(parameter);
    }
    
    public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter){
        return maintainValueSet(parameter, SessionHelper.getHostUrl());
    }    
    public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter, String urlHost){
        return maintainValueSet(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";      
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);           
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.maintainValueSet(parameter);
    }
    
    public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter){
        return maintainConcept(parameter, SessionHelper.getHostUrl());
    }    
    public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter, String urlHost){
        return maintainConcept(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {        
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";    
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.maintainConcept(parameter);
    }
    
    public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter){
        return maintainConceptValueSetMembership(parameter, SessionHelper.getHostUrl());
    }    
    public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter, String urlHost){
        return maintainConceptValueSetMembership(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {    
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";      
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.maintainConceptValueSetMembership(parameter);
    }
    
    public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter){
        return updateCodeSystemVersionStatus(parameter, SessionHelper.getHostUrl());
    }    
    public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter, String urlHost){
        return updateCodeSystemVersionStatus(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";          
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateCodeSystemVersionStatus(parameter);
    }   

    public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter){
        return updateValueSetStatus(parameter, SessionHelper.getHostUrl());
    }    
    public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter, String urlHost){
        return updateValueSetStatus(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {          
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";           
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);           
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateValueSetStatus(parameter);
    }
    
    public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter){
        return updateConceptStatus(parameter, SessionHelper.getHostUrl());
    }    
    public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter, String urlHost){
        return updateConceptStatus(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {         
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateConceptStatus(parameter);
    }
    
    public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter){
        return updateConceptValueSetMembershipStatus(parameter, SessionHelper.getHostUrl());
    }    
    public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter, String urlHost){
        return updateConceptValueSetMembershipStatus(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter, String urlHost, String urlService) {                
        Authoring_Service  service;
        Authoring          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getAuthoringServicePort();
        return port.updateConceptValueSetMembershipStatus(parameter);
    }
    
    // Authorization ///////////////////////////////////////////////////////////       
    public static LoginResponse.Return login(LoginRequestType parameter){
        return login(parameter, SessionHelper.getHostUrl());
    }  
    public static LoginResponse.Return login(LoginRequestType parameter, String urlHost){
        return login(parameter, urlHost, SessionHelper.getServiceName());
    }  
    public static LoginResponse.Return login(LoginRequestType parameter, String urlHost, String urlService ){    
    Authorization_Service  service;
    Authorization          port;
        try {          
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";       
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getAuthorizationServicePort();
        return port.login(parameter);
    }    
    
    // ConceptAssociations  ////////////////////////////////////////////////////  
    public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter){
        return listConceptAssociations(parameter, SessionHelper.getHostUrl());
    }    
    public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter, String urlHost){
        return listConceptAssociations(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter, String urlHost, String urlService) {                
        ConceptAssociations_Service  service;
        ConceptAssociations          port;
        try {    
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);     
        }        
        port = WebServiceUrlHelper.getInstance().getConceptAssociationServicePort();
        return port.listConceptAssociations(parameter);
    }
    
    public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter){
        return createConceptAssociation(parameter, SessionHelper.getHostUrl());
    }    
    public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter, String urlHost){
        return createConceptAssociation(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter, String urlHost, String urlService) {                
        ConceptAssociations_Service  service;
        ConceptAssociations          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";      
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getConceptAssociationServicePort();
        return port.createConceptAssociation(parameter);
    }      
    
    // Search //////////////////////////////////////////////////////////////////    
    public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter){
        return listMetadataParameter(parameter, SessionHelper.getHostUrl());
    }    
    public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter, String urlHost){        
        return listMetadataParameter(parameter, urlHost, SessionHelper.getServiceName());        
    }    
    public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {      
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";          
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listMetadataParameter(parameter);
    }    
    
    public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter){
        return listDomainValues(parameter, SessionHelper.getHostUrl());
    }    
    public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter, String urlHost){        
        return listDomainValues(parameter, urlHost, SessionHelper.getServiceName());        
    }    
    public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {      
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";     
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);         
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listDomainValues(parameter);
    }
        
    public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystems(ListCodeSystemsInTaxonomyRequestType parameter){
        return listCodeSystems(parameter, SessionHelper.getHostUrl());
    }    
    public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystems(ListCodeSystemsInTaxonomyRequestType parameter, String urlHost){        
        return listCodeSystems(parameter, urlHost, SessionHelper.getServiceName());        
    }    
    public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystems(ListCodeSystemsInTaxonomyRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);         
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listCodeSystemsInTaxonomy(parameter);
    }    
    
    public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter){
        return listValueSets(parameter, SessionHelper.getHostUrl());
    }    
    public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter, String urlHost){
        return listValueSets(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {           
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";         
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listValueSets(parameter);
    }
        
    public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter){
        return listCodeSystemConcepts(parameter, SessionHelper.getHostUrl());
    }    
    public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, String urlHost){
        return listCodeSystemConcepts(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {        
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";          
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);         
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listCodeSystemConcepts(parameter);
    }
    
    public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter){
        return listGloballySearchedConcepts(parameter, SessionHelper.getHostUrl());
    }    
    public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, String urlHost){
        return listGloballySearchedConcepts(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {      
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";     
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);           
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listGloballySearchedConcepts(parameter);
    }
    
    public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter){
        return listValueSetContents(parameter, SessionHelper.getHostUrl());
    }    
    public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter, String urlHost){
        return listValueSetContents(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {          
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listValueSetContents(parameter);
    }
    
    public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter){
        return listValueSetContentsByTermOrCode(parameter, SessionHelper.getHostUrl());
    }    
    public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter, String urlHost){
        return listValueSetContentsByTermOrCode(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {           
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);         
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.listValueSetContentsByTermOrCode(parameter);
    }
            
    public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter){
        return returnCodeSystemDetails(parameter, SessionHelper.getHostUrl());
    }    
    public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter, String urlHost){
        return returnCodeSystemDetails(parameter, urlHost, SessionHelper.getServiceName());
    }
    public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {       
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";        
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);            
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.returnCodeSystemDetails(parameter);
    }                
    
    public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter){
        return returnValueSetDetails(parameter, SessionHelper.getHostUrl());
    }    
    public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter, String urlHost){
        return returnValueSetDetails(parameter, urlHost, SessionHelper.getServiceName());
    }
    public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {         
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";          
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);          
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.returnValueSetDetails(parameter);
    }    
    
    public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter){
        return returnConceptDetails(parameter, SessionHelper.getHostUrl());
    }    
    public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter, String urlHost){
        return returnConceptDetails(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {     
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";      
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);        
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.returnConceptDetails(parameter);
    }
    
    public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter){
        return returnConceptValueSetMembership(parameter, SessionHelper.getHostUrl());
    }    
    public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter, String urlHost){
        return returnConceptValueSetMembership(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {         
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";         
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);           
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.returnConceptValueSetMembership(parameter);
    } 
    
    public static ReturnVsConceptMetadataResponse.Return returnVsConceptMetadata(ReturnVsConceptMetadataRequestType parameter){
        return returnVsConceptMetadata(parameter, SessionHelper.getHostUrl());
    }    
    public static ReturnVsConceptMetadataResponse.Return returnVsConceptMetadata(ReturnVsConceptMetadataRequestType parameter, String urlHost){
        return returnVsConceptMetadata(parameter, urlHost, SessionHelper.getServiceName());
    }    
    public static ReturnVsConceptMetadataResponse.Return returnVsConceptMetadata(ReturnVsConceptMetadataRequestType parameter, String urlHost, String urlService) {                
        Search_Service  service;
        Search          port;
        try {           
            if(urlHost.startsWith("http://") == false)  urlHost = "http://" + urlHost;
            if(urlHost.endsWith("/") == false)          urlHost += "/";
            if(urlService.startsWith("/"))              urlService = urlService.substring(1);
            if(urlService.endsWith("/") == false)       urlService += "/";           
        } catch (Exception ex) {
            Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);       
        }        
        port = WebServiceUrlHelper.getInstance().getSearchServicePort();
        return port.returnVsConceptMetadata(parameter);
    }
}