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
package de.fhdo.gui.main.modules;

import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.itemrenderer.ListitemRendererTranslations;
import de.fhdo.models.itemrenderer.ListitemRendererOntologies;
import de.fhdo.models.TreeModel;
import de.fhdo.models.TreeNode;
import de.fhdo.models.comparators.ComparatorCsMetadata;
import de.fhdo.models.comparators.ComparatorTranslations;
import de.fhdo.models.comparators.ComparatorVsMetadata;
import de.fhdo.models.itemrenderer.ListitemRendererCrossmapping;
import de.fhdo.models.itemrenderer.ListitemRendererCsMetadataList;
import de.fhdo.models.itemrenderer.ListitemRendererLinkedConcepts;
import de.fhdo.models.itemrenderer.ListitemRendererVsMetadataList;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse.Return;
import de.fhdo.terminologie.ws.search.ReturnVsConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnVsConceptMetadataResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemMetadataValue;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import org.zkoss.zul.Row;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSetMetadataValue;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupConcept extends PopupWindow{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private CodeSystemEntityVersion csev;
    private CodeSystemConcept       csc;
    private CodeSystemEntity        cse;
    private CodeSystemVersionEntityMembership csvem;
       
    private long        id, versionId;    
    private int         hierarchyMode, contentMode;   
    private List        metadata     = new ArrayList();
    private List        translations = new ArrayList();
    private Listbox     listTranslations, listMetadata, listCrossmappings, listLinkedConcepts, listOntologies; 
    private Button      bCreate, bMetaParaChange, bTranslationNew;
    private Checkbox    cbNewVersion, cbPreferred, cbAxis, cbMainClass, cbStructureEntry, cbIsLeaf;
    private Datebox     dateBoxED, dateBoxID, dateBoxSD;                      
    private Textbox     tbTerm, tbAbbrevation, tbDescription, tbCode, tbStatus, tbNamePL, tbOrderNr, tbBedeutung, tbAwbeschreibung, tbHinweise,tbHints,tbMeaning;
    private Label       lReq,lName,lCode, lPref, lTermAbbr;
    private TreeNode    tnSelected;
    private Grid        grid, gridT;
    private Tab         tabDetails;
    private ConceptValueSetMembership cvsm;
    private Row         rOrderNr,rStructureEntry, rBedeutung, rAwbeschreibung, rHinweise, rHints,rMeaning;
		private boolean isELGALaborparameter = false;
		private String storedTerm = "";
		private String storedTermAbbr = "";
		
		private ListConceptAssociationsResponse.Return returnHanding;
    
    @Override
    public void doAfterComposeCustom() {
        contentMode = (Integer)arg.get("ContentMode");
				//Matthias
				if(arg.get("CSE") != null){
					cse = (CodeSystemEntity) arg.get("CSE");
					csev = cse.getCodeSystemEntityVersions().get(0);
					return;
				}
				
				if(arg.get("Is_ELGA_Laborparameter") != null){
					isELGALaborparameter = (Boolean) arg.get("Is_ELGA_Laborparameter");
				} else {
					isELGALaborparameter = false;
				}
				
        if(arg.get("TreeNode") != null)
            tnSelected = (TreeNode)arg.get("TreeNode"); 
        id          = (Long)arg.get("Id");
        versionId   = (Long)arg.get("VersionId");
    }
    
    private void loadAssociations(){
			//Matthias: if-Statement geändert da es bei der Anzeige von Details sonst so Fehlern kam
			//TODO genauer überprüfen
        if(true){//if(tnSelected == null || tnSelected.getResponseListConceptAssociations() == null){       
            // Parameter erzeugen und im folgenden zusammenbauen
            ListConceptAssociationsRequestType parameter_ListCA = new ListConceptAssociationsRequestType();

            // CSE erstellen und CSEV einsetzen  
            CodeSystemEntity        cseNew  = new CodeSystemEntity();
            CodeSystemEntityVersion csevNew = new CodeSystemEntityVersion();
            cseNew.setId(csev.getCodeSystemEntity().getId());  
            csevNew.setVersionId(csev.getVersionId());  
            cseNew.getCodeSystemEntityVersions().add(csevNew);

            // Zusatzinformationen anfordern um anzuzeigen ob noch Kinder vorhanden sind oder nicht
    //        parameter_ListCA.setLookForward(true);    
            parameter_ListCA.setDirectionBoth(true);
            parameter_ListCA.setCodeSystemEntity(cseNew);

            // Anfrage an WS (ListConceptAssociations) stellen mit parameter_ListCA                       
            csevNew.setCodeSystemEntity(null);       // damit es kein infinity Deep Problem gibt

            // Falls es beim Ausführen des WS zum Fehler kommt
            ListConceptAssociationsResponse.Return response = null;
            try{
                response = WebServiceHelper.listConceptAssociations(parameter_ListCA);   
            } catch (Exception e){e.printStackTrace();}
						
                returnHanding = response;

                //if (tnSelected != null){
                //	tnSelected.setResponseListConceptAssociations(response);
                //}
        }        
        
        if(returnHanding != null)
        {
            loadCrossmappings(); 
            loadLinkedConcepts();  
            loadOntologies();
        }
    }
    
    private void loadCSEVFromArguments(){
			if(csev == null){
				csev = (CodeSystemEntityVersion)arg.get("CSEV");
			}
               
			if(csev != null){
					csc  = csev.getCodeSystemConcepts().get(0);
					if (cse == null){
						cse  = csev.getCodeSystemEntity();  
					}
					         
					loadDetails();   
			}
    }   
    
    private void loadCsMetadata(Return responseDetails, boolean editableMetadataList){
        for(CodeSystemMetadataValue csmdv : responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemMetadataValues()){
            csmdv.setCodeSystemEntityVersion(responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0));
            metadata.add(csmdv); 
        }                
        
        if(metadata.isEmpty()){
            window.getFellow("tabMetadata").setVisible(false);
            return;
        }
        window.getFellow("tabMetadata").setVisible(true);
        
        Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
                   lh2 = new Listheader(Labels.getLabel("common.value"));
        lh1.setSortAscending(new ComparatorCsMetadata(true));
        lh1.setSortDescending(new ComparatorCsMetadata(false));
        listMetadata.getListhead().getChildren().add(lh1);
        listMetadata.getListhead().getChildren().add(lh2);    
        
                       
        listMetadata.setItemRenderer(new ListitemRendererCsMetadataList(editableMetadataList));
        listMetadata.setModel(new SimpleListModel(metadata));
        lh1.sort(true);
    }
    
    private void loadVsMetadata(ReturnVsConceptMetadataResponse.Return responseDetails, boolean editableMetadataList){
        for(ValueSetMetadataValue vsmdv : responseDetails.getValueSetMetadataValue()){
            metadata.add(vsmdv); 
        }                
        
        if(metadata.isEmpty()){
            window.getFellow("tabMetadata").setVisible(false);
            return;
        }
        window.getFellow("tabMetadata").setVisible(true);
        
        Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
                   lh2 = new Listheader(Labels.getLabel("common.value"));
        lh1.setSortAscending(new ComparatorVsMetadata(true));
        lh1.setSortDescending(new ComparatorVsMetadata(false));
        listMetadata.getListhead().getChildren().add(lh1);
        listMetadata.getListhead().getChildren().add(lh2);    
        
                       
        listMetadata.setItemRenderer(new ListitemRendererVsMetadataList(editableMetadataList));
        listMetadata.setModel(new SimpleListModel(metadata)); 
        lh1.sort(true);
    }
    
    private void loadTranslations(Return responseDetails, boolean editableTranslationsList){
             
        for(CodeSystemConceptTranslation csct : responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations()){
            translations.add(csct);
        }        
        
        if(translations.isEmpty()){
            window.getFellow("tabTranslations").setVisible(true);
            return;
        }
        window.getFellow("tabTranslations").setVisible(true);
        
        Listheader lh1 = new Listheader(Labels.getLabel("common.language")),
                   lh2 = new Listheader(Labels.getLabel("common.value"));       
        lh1.setSortAscending(new ComparatorTranslations(true));
        lh1.setSortDescending(new ComparatorTranslations(false));
        listTranslations.getListhead().getChildren().add(lh1);
        listTranslations.getListhead().getChildren().add(lh2);
        
        
        listTranslations.setItemRenderer(new ListitemRendererTranslations(editableTranslationsList));        
        listTranslations.setModel(new SimpleListModel(translations));         
        lh1.sort(true);
    }  
       
    private void loadCrossmappings(){
        ListModelList crossmappings = new ListModelList();                
        
        //for (CodeSystemEntityVersionAssociation cseva : tnSelected.getResponseListConceptAssociations().getCodeSystemEntityVersionAssociation()){
				for (CodeSystemEntityVersionAssociation cseva : returnHanding.getCodeSystemEntityVersionAssociation()){
            if(cseva.getAssociationKind().compareTo(3) == 0 && crossmappings.contains(cseva) == false){ 
                crossmappings.add(cseva);
            }
        }
        
        if(crossmappings.isEmpty()){
            window.getFellow("tabCrossmapping").setVisible(false);
            return;
        }
        window.getFellow("tabCrossmapping").setVisible(true);
        
        Listheader lh1 = new Listheader(Labels.getLabel("common.concept")),
                   lh2 = new Listheader(Labels.getLabel("common.codeSystem"));       
        listCrossmappings.getListhead().getChildren().add(lh1);
        listCrossmappings.getListhead().getChildren().add(lh2);               
        listCrossmappings.setModel(crossmappings);
        
        //renderer
        listCrossmappings.setItemRenderer(new ListitemRendererCrossmapping(csev.getVersionId()));
    }
    
    private void loadLinkedConcepts(){        
        ListModelList linkedConcepts = new ListModelList();
        
        for(CodeSystemEntityVersionAssociation cseva : returnHanding.getCodeSystemEntityVersionAssociation()){
            if(cseva.getAssociationKind().compareTo(4) == 0 && linkedConcepts.contains(cseva) == false){  
                linkedConcepts.add(cseva);                
            }
        }
        
        if(linkedConcepts.isEmpty()){
            window.getFellow("tabLinkedConcepts").setVisible(false);
            return;
        }
        window.getFellow("tabLinkedConcepts").setVisible(true);        
        Listheader lh1 = new Listheader(Labels.getLabel("common.association")),
                   lh2 = new Listheader(Labels.getLabel("common.concept"));
        lh1.setWidth("30%");
        listLinkedConcepts.getListhead().setSizable(true);
        listLinkedConcepts.getListhead().getChildren().add(lh1);
        listLinkedConcepts.getListhead().getChildren().add(lh2);        
        listLinkedConcepts.setModel(linkedConcepts);
        
        // Renderer
        listLinkedConcepts.setItemRenderer(new ListitemRendererLinkedConcepts(csev.getVersionId()));
    }
    
    private void loadOntologies(){
        ListModelList ontologies = new ListModelList();
        
        for(CodeSystemEntityVersionAssociation cseva : returnHanding.getCodeSystemEntityVersionAssociation()){
            if(cseva.getAssociationKind().compareTo(1) == 0){ 
                if(cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null || cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null){
                    if(ontologies.contains(cseva) == false)
                        ontologies.add(cseva);
                }                  
            }
        }
        
        if(ontologies.isEmpty()){
            window.getFellow("tabOntologies").setVisible(false);
            return;
        }
        window.getFellow("tabOntologies").setVisible(true);            
        Listheader lh1 = new Listheader(Labels.getLabel("common.association")),
                   lh2 = new Listheader(Labels.getLabel("common.concept"));   
        lh1.setWidth("100px");
        listOntologies.getListhead().setSizable(true);                
        listOntologies.getListhead().getChildren().add(lh1);
        listOntologies.getListhead().getChildren().add(lh2);        
        listOntologies.setModel(ontologies);        
        
        // Renderer
        listOntologies.setItemRenderer(new ListitemRendererOntologies(csev.getVersionId()));
    }
    
    private void loadDetails(){     
        // Daten einlesen
        loadDatesIntoGUI();
        
        // Metadaten und Uebersetzungen laden
        ReturnConceptDetailsRequestType parameter   = new ReturnConceptDetailsRequestType();        
        parameter.setCodeSystemEntity(cse);
        parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
        parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
        //CSE aus CSEV entfernen, sonst inf,loop
        csev.setCodeSystemEntity(null);
        
        
        if(SessionHelper.isUserLoggedIn()){
            
            de.fhdo.terminologie.ws.search.LoginType loginSearch = new de.fhdo.terminologie.ws.search.LoginType();
            loginSearch.setSessionID(SessionHelper.getSessionId());
            parameter.setLogin(loginSearch);
        }
        
        Return response = WebServiceHelper.returnConceptDetails(parameter);
       
        // keine csev zurueckgekommen (wegen moeglicher Fehler beim WS)
        if(response.getCodeSystemEntity() == null)
            return;
        
        // das Loeschen der cse aus der csev wieder rueckgaengig machen (war nur fuer die Anfrage an WS)
        csev.setCodeSystemEntity(cse);            
        
        // CodeSystemVersionEntityMembership nachladen
        if(response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().isEmpty() == false){
            csvem = response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0);
            cse.getCodeSystemVersionEntityMemberships().clear();
            cse.getCodeSystemVersionEntityMemberships().add(csvem);
        }
        
        if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){                       
            ReturnVsConceptMetadataRequestType para = new ReturnVsConceptMetadataRequestType();        
            para.setCodeSystemEntityVersionId(csev.getVersionId());
            para.setValuesetVersionId(versionId);
            listMetadata.setAttribute("valuesetVersionId", versionId);
            listMetadata.setAttribute("codeSystemEntityVersionId", csev.getVersionId());
            listMetadata.setAttribute("contentMode", contentMode);
            
            listTranslations.setAttribute("cse", cse);
            listTranslations.setAttribute("csev", csev);
            listTranslations.setAttribute("csevm", csvem);
            
            ReturnVsConceptMetadataResponse.Return resp = WebServiceHelper.returnVsConceptMetadata(para);
            loadVsMetadata(resp,false);

        }else{
            listMetadata.setAttribute("cse", cse);
            listMetadata.setAttribute("csev", csev);
            listMetadata.setAttribute("csevm", csvem);
            listMetadata.setAttribute("contentMode", contentMode);
            listMetadata.setAttribute("versionId", versionId);
            
            listTranslations.setAttribute("cse", cse);
            listTranslations.setAttribute("csev", csev);
            listTranslations.setAttribute("csevm", csvem);
            
            loadCsMetadata(response, false);
        }

        loadTranslations(response,false); 
        loadAssociations();
        
        loadTbStatus();
				
				
				
    }     
    
    private void loadTbStatus(){
    
        if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){
            
            for(ConceptValueSetMembership cvsmL:csev.getConceptValueSetMemberships()){
                if(cvsmL.getId().getValuesetVersionId() == versionId)
                    cvsm = cvsmL;
            }
            
            tbStatus.setValue(String.valueOf(cvsm.getStatus()));
            cbStructureEntry.setChecked(cvsm.isIsStructureEntry());
            rStructureEntry.setVisible(true);
            tbOrderNr.setValue(String.valueOf(cvsm.getOrderNr()));
            rOrderNr.setVisible(true);
            
            tbBedeutung.setValue(cvsm.getBedeutung());
            rBedeutung.setVisible(true);
            tbAwbeschreibung.setValue(cvsm.getAwbeschreibung());
            rAwbeschreibung.setVisible(true);
            tbHinweise.setValue(cvsm.getHinweise());
            rHinweise.setVisible(true);
            
            rHints.setVisible(false);
            rMeaning.setVisible(false);
            
        }else{
            tbStatus.setValue(String.valueOf(csev.getStatus()));
        }
    }
    
    private CreateConceptAssociationResponse.Return createAssociationResponse(CodeSystemEntityVersion csev1, CodeSystemEntityVersion csev2, int assoKind, int assoType){           
        CreateConceptAssociationRequestType     parameterAssociation = new CreateConceptAssociationRequestType();        
        CreateConceptAssociationResponse.Return responseAccociation  = null;        
        CodeSystemEntityVersionAssociation      cseva                = new CodeSystemEntityVersionAssociation();         
        
        if(csev1 != null && csev2 != null){
            cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
            cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2); 
            cseva.setAssociationKind(assoKind); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
            cseva.setLeftId(csev1.getVersionId()); // immer linkes Element also csev1
            cseva.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
            cseva.getAssociationType().setCodeSystemEntityVersionId((long)assoType);

            // Login
            de.fhdo.terminologie.ws.conceptassociation.LoginType loginConceptassociation = new de.fhdo.terminologie.ws.conceptassociation.LoginType();
            loginConceptassociation.setSessionID(SessionHelper.getSessionId());
            parameterAssociation.setLogin(loginConceptassociation);

            // Association
            parameterAssociation.setCodeSystemEntityVersionAssociation(cseva);

            // Call WS and prevent loops in SOAP Message        
            long cse1id = csev1.getCodeSystemEntity().getId();
            csev1.setCodeSystemEntity(null);
            csev2.setCodeSystemEntity(null);            
            responseAccociation = WebServiceHelper.createConceptAssociation(parameterAssociation);            
            csev1.setCodeSystemEntity(new CodeSystemEntity());    
            csev1.getCodeSystemEntity().setId(cse1id);
            csev2.setCodeSystemEntity(cse);
        }
                
        return responseAccociation;
    }                             
    
    @Override
    protected void initializeDatabinder() {
        binder = new AnnotateDataBinder(window);
        binder.bindBean("cse",  cse);
        binder.bindBean("csev", csev);
				//Matthias: In case of ELGA_Laborparameter_VS change the displayName to the one of the german translation
				if (isELGALaborparameter){
					CodeSystemConcept tempConcept = new CodeSystemConcept();
					tempConcept.setCode(csc.getCode());
					tempConcept.setMeaning(csc.getMeaning());
					tempConcept.setDescription(csc.getDescription().replace('|', ':'));
					tempConcept.setHints(csc.getHints());
					tempConcept.setIsPreferred(csc.isIsPreferred());
					
							
					for (Object tra : this.translations){
						CodeSystemConceptTranslation translation = (CodeSystemConceptTranslation) tra;
						if (translation.getLanguageId() == 33l){
							tempConcept.setTerm(translation.getTerm());
							tempConcept.setTermAbbrevation(translation.getTermAbbrevation());
						}
					}
					
					if (tempConcept.getTerm() != null){
						binder.bindBean("csc", tempConcept);
					
						lTermAbbr.setValue("Alternativer Anzeigetext");
					} else {
						binder.bindBean("csc",  csc);
					}
				} else {
					binder.bindBean("csc",  csc);
				}
        
        binder.bindBean("csvem",  csvem);
        binder.bindBean("metadata", metadata);
        binder.bindBean("translations", translations);
        binder.bindBean("versioning", versioning);        
        binder.loadAll();
    }

    @Override
    protected void loadDatesIntoGUI() {
        if(csev != null){
            if(csev.getEffectiveDate() != null)
                dateBoxED.setValue(new Date(csev.getEffectiveDate().toGregorianCalendar().getTimeInMillis()));
            if(csev.getInsertTimestamp() != null)
                dateBoxID.setValue(new Date(csev.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
            if(csev.getStatusDate() != null)
                dateBoxSD.setValue(new Date(csev.getStatusDate().toGregorianCalendar().getTimeInMillis()));
        }
        else{
            dateBoxED.setValue(null);
            dateBoxID.setValue(null);
            dateBoxSD.setValue(null);
        }
    }

    @Override
    protected void editmodeDetails() {
        loadCSEVFromArguments();
        window.setTitle(Labels.getLabel("popupConcept.showConcept"));
        cbAxis.setDisabled(true);
        cbMainClass.setDisabled(true);
        cbIsLeaf.setDisabled(true);
        cbNewVersion.setVisible(false);
        cbPreferred.setDisabled(true);
        dateBoxED.setDisabled(true);
        dateBoxID.setDisabled(true);
        dateBoxSD.setDisabled(true);                
        tbTerm.setReadonly(true);
        tbAbbrevation.setReadonly(true);
        tbDescription.setReadonly(true);
        tbCode.setReadonly(true);
        tbStatus.setReadonly(true);
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.term"));
        lCode.setValue(Labels.getLabel("common.code"));
        lPref.setValue(Labels.getLabel("common.isPreferredTerm"));
        bCreate.setVisible(false);
        listMetadata.setDisabled(true);
        listTranslations.setDisabled(true);
        grid.setVisible(true);
        gridT.setVisible(true);
        bMetaParaChange.setDisabled(true);
        bTranslationNew.setDisabled(true);
        tbHints.setReadonly(true);
        tbMeaning.setReadonly(true);
        if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){
            cbStructureEntry.setDisabled(true);
            tbOrderNr.setReadonly(true);
            tbBedeutung.setReadonly(true);
            tbAwbeschreibung.setReadonly(true);
            tbHinweise.setReadonly(true);
        }
				
    }

    @Override
    protected void editmodeCreate() { //Erstellen
        window.setTitle(Labels.getLabel("popupConcept.newConcept"));
        csev        = new CodeSystemEntityVersion();                
        csc         = new CodeSystemConcept();                
        cse         = new CodeSystemEntity();                    
        csvem       = new CodeSystemVersionEntityMembership();
        versioning  = new VersioningType();                
        csc.setIsPreferred(Boolean.TRUE);
        csev.getCodeSystemConcepts().add(csc); 
        csev.setStatus(1); // TODO: 1 durch Konstante ersetzen
        csev.setIsLeaf(Boolean.TRUE);
        csvem.setIsAxis(Boolean.FALSE);       
        versioning.setCreateNewVersion(Boolean.TRUE);

        hierarchyMode = (Integer)arg.get("Association");   
        if(hierarchyMode == 3){
            window.setTitle(Labels.getLabel("popupConcept.createRootConcept"));
            csvem.setIsMainClass(Boolean.TRUE);
        }
        else{
            window.setTitle(Labels.getLabel("popupConcept.createSubConcept"));
            csvem.setIsMainClass(Boolean.FALSE);
        }

        cbAxis.setDisabled(false);
        cbMainClass.setDisabled(true);
        cbIsLeaf.setDisabled(true);
        cbNewVersion.setVisible(true);
        cbNewVersion.setDisabled(true);
        cbPreferred.setDisabled(false);                
        dateBoxED.setReadonly(false);
        dateBoxID.setReadonly(true);
        dateBoxSD.setReadonly(true);
        tbTerm.setReadonly(false);
        tbTerm.setFocus(true);
        tbAbbrevation.setReadonly(false);
        tbDescription.setReadonly(false);
        tbCode.setReadonly(false);
        tbStatus.setReadonly(false);
        lReq.setVisible(true);
        lName.setValue(Labels.getLabel("common.term") + "*");
        lCode.setValue(Labels.getLabel("common.code") + "*");
        lPref.setValue(Labels.getLabel("common.termPreferred") + "*");
        bCreate.setVisible(true);
        bCreate.setLabel(Labels.getLabel("common.create"));
        listMetadata.setDisabled(true);
        listTranslations.setDisabled(true);
        grid.setVisible(true);
        gridT.setVisible(true);
        bMetaParaChange.setDisabled(true);
        bTranslationNew.setDisabled(true);
				
				
    }

    @Override
    protected void editmodeMaintainVersionNew() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void editmodeMaintain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void editmodeMaintainVersionEdit() { 
        loadCSEVFromArguments();
        window.setTitle(Labels.getLabel("popupConcept.editConcept"));
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.FALSE); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar                

        if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){
            
            cbAxis.setDisabled(true);
            cbMainClass.setDisabled(true);
            cbIsLeaf.setDisabled(true);
            cbNewVersion.setVisible(false);                
            cbNewVersion.setDisabled(true); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar
            cbPreferred.setDisabled(true);
            dateBoxED.setReadonly(true);
            dateBoxID.setReadonly(true);
            dateBoxSD.setReadonly(true);
            tbTerm.setReadonly(true);
            tbAbbrevation.setReadonly(true);
            tbDescription.setReadonly(true);
            tbCode.setReadonly(true);
            tbStatus.setReadonly(true);                
            lReq.setVisible(false);
            lName.setValue(Labels.getLabel("common.term"));
            lCode.setValue(Labels.getLabel("common.code"));
            lPref.setValue(Labels.getLabel("common.termPreferred"));
            bCreate.setVisible(true);
            bCreate.setLabel(Labels.getLabel("common.change"));
            listMetadata.setDisabled(false);
            cbStructureEntry.setDisabled(false);
            tbOrderNr.setReadonly(false);
            tbBedeutung.setReadonly(false);
            tbAwbeschreibung.setReadonly(false);
            tbHinweise.setReadonly(false);
            tbHints.setReadonly(true);
            tbMeaning.setReadonly(true);
            
            Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
                   lh2 = new Listheader(Labels.getLabel("common.value"));
            lh1.setSortAscending(new ComparatorVsMetadata(true));
            lh1.setSortDescending(new ComparatorVsMetadata(false));

            listMetadata.setItemRenderer(new ListitemRendererVsMetadataList(true));
            listMetadata.setModel(new SimpleListModel(metadata)); 
            lh1.sort(true);
            
            grid.setVisible(true);
            gridT.setVisible(true);
            bMetaParaChange.setDisabled(false);
            
            listTranslations.setDisabled(false);
            Listheader lh1t = new Listheader(Labels.getLabel("common.language")),
                   lh2t = new Listheader(Labels.getLabel("common.value"));
            lh1t.setSortAscending(new ComparatorTranslations(true));
            lh1t.setSortDescending(new ComparatorTranslations(false));

            listTranslations.setItemRenderer(new ListitemRendererTranslations(true));
            listTranslations.setModel(new SimpleListModel(translations)); 
            lh1t.sort(true);
            
            bTranslationNew.setDisabled(false);
        }else{
        
            cbAxis.setDisabled(false);
            cbMainClass.setDisabled(false);
            cbIsLeaf.setDisabled(false);
            cbNewVersion.setVisible(true);                
            cbNewVersion.setDisabled(true); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar
            cbPreferred.setDisabled(false);
            dateBoxED.setReadonly(false);
            dateBoxID.setReadonly(true);
            dateBoxSD.setReadonly(true);
            tbTerm.setReadonly(false);
            tbAbbrevation.setReadonly(false);
            tbDescription.setReadonly(false);
            tbHints.setReadonly(false);
            tbMeaning.setReadonly(false);
            tbCode.setReadonly(false);
            tbStatus.setReadonly(true);                
            lReq.setVisible(false);
            lName.setValue(Labels.getLabel("common.term"));
            lCode.setValue(Labels.getLabel("common.code"));
            lPref.setValue(Labels.getLabel("common.termPreferred"));
            bCreate.setVisible(true);
            bCreate.setLabel(Labels.getLabel("common.change"));
            listMetadata.setDisabled(false);

            Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
                   lh2 = new Listheader(Labels.getLabel("common.value"));
            lh1.setSortAscending(new ComparatorVsMetadata(true));
            lh1.setSortDescending(new ComparatorVsMetadata(false));

            listMetadata.setItemRenderer(new ListitemRendererCsMetadataList(true));
            listMetadata.setModel(new SimpleListModel(metadata)); 
            lh1.sort(true);
            
            grid.setVisible(true);
            gridT.setVisible(true);
            bMetaParaChange.setDisabled(false);
            
            listTranslations.setDisabled(false);
            Listheader lh1t = new Listheader(Labels.getLabel("common.language")),
                   lh2t = new Listheader(Labels.getLabel("common.value"));
            lh1t.setSortAscending(new ComparatorTranslations(true));
            lh1t.setSortDescending(new ComparatorTranslations(false));

            listTranslations.setItemRenderer(new ListitemRendererTranslations(true));
            listTranslations.setModel(new SimpleListModel(translations)); 
            lh1t.sort(true);
            
            bTranslationNew.setDisabled(false);
        }
    }

    @Override
    protected void editmodeUpdateStatus() {
        loadCSEVFromArguments();
        
        window.setTitle(Labels.getLabel("popupConcept.editConceptStatus"));
        cbAxis.setDisabled(true);
        cbMainClass.setDisabled(true);
        cbIsLeaf.setDisabled(true);
        cbNewVersion.setVisible(false);
        cbPreferred.setDisabled(true);
        dateBoxED.setReadonly(true);
        dateBoxID.setReadonly(true);
        dateBoxSD.setReadonly(true);
        tbTerm.setReadonly(true);
        tbAbbrevation.setReadonly(true);
        tbDescription.setReadonly(true);
        tbHints.setReadonly(true);
        tbMeaning.setReadonly(true);
        tbCode.setReadonly(true);
        tbStatus.setReadonly(false);  
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.term"));
        lCode.setValue(Labels.getLabel("common.code"));
        lPref.setValue(Labels.getLabel("common.termPreferred"));
        bCreate.setVisible(true); 
        bCreate.setLabel(Labels.getLabel("common.changeStatus"));
        grid.setVisible(true);
        gridT.setVisible(true);
        bMetaParaChange.setDisabled(true);
        bTranslationNew.setDisabled(true);
        listMetadata.setDisabled(true);
        listTranslations.setDisabled(true);
        
        if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){
        
            cbStructureEntry.setDisabled(true);
            tbOrderNr.setReadonly(true);
            tbBedeutung.setReadonly(true);
            tbAwbeschreibung.setReadonly(true);
            tbHinweise.setReadonly(true);
        }
    }

    @Override
    protected void editmodeUpdateStatusVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void create() {
        // Create Concept //////////////////////////////////////////////////////        
//        Authoring                   port_authoring    = new Authoring_Service().getAuthoringPort();
        
        // Login
        de.fhdo.terminologie.ws.authoring.LoginType loginAuthoring = new de.fhdo.terminologie.ws.authoring.LoginType();
        loginAuthoring.setSessionID(SessionHelper.getSessionId());
   
        // CodeSystemVersionEntityMembership
        cse.getCodeSystemVersionEntityMemberships().clear();
        cse.getCodeSystemVersionEntityMemberships().add(csvem);
        
        // CodeSystemEntity(Version)
        cse.getCodeSystemEntityVersions().clear();
        cse.getCodeSystemEntityVersions().add(csev);
        
        // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
        try {            
            if(dateBoxED != null && dateBoxED.getValue() != null){
                GregorianCalendar c = new GregorianCalendar();
                c.setTimeInMillis(dateBoxED.getValue().getTime());               
                csev.setEffectiveDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
            }                         
        } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}                  
                
        // CodeSystem + CodeSystemVersion
        if(contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM){        
            CreateConceptRequestType parameterCSC  = new CreateConceptRequestType();
            CreateConceptResponse.Return responseCreateConcept = null;
            
            // Login                         
            parameterCSC.setLogin(loginAuthoring);
                 
            // CodeSystem
            CodeSystem        cs  = new CodeSystem();
            CodeSystemVersion csv = new CodeSystemVersion(); 
            cs.setId(id);   
            csv.setVersionId(versionId);
            cs.getCodeSystemVersions().add(csv);
            parameterCSC.setCodeSystem(cs);

            // CodeSystemEntity + CSEV
            parameterCSC.setCodeSystemEntity(cse);                              
            
            parameterCSC.setCodeSystem(cs);

            // WS anfrage
            csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
            responseCreateConcept = WebServiceHelper.createConcept(parameterCSC);             
            csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS) 

            // Meldung falls CreateConcept fehlgeschlagen
            if(responseCreateConcept.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
                try {            
                    Messagebox.show(Labels.getLabel("common.error")+"\n" +  Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + responseCreateConcept.getReturnInfos().getMessage());
                    return;
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}                                                
                        
            // die neue cse(v) hat noch keine id. Für Assoziationen aber nötig => aus response auslesen
            csev.setVersionId(responseCreateConcept.getCodeSystemEntity().getCurrentVersionId());
            cse.setId(responseCreateConcept.getCodeSystemEntity().getId());
            cse.setCurrentVersionId(csev.getVersionId());
        }
        else if(contentMode == ContentConcepts.CONTENTMODE_VALUESET){ // ValueSets; Es werden nur Verknüpfungen zu CSE(V)s erstellt und keine neuen Konzepte angelegt
           // siehe ganz unten
        }
        
        // TreeNode erstellen und danach update, damit das neue Konzept auch angezeigt wird                
        TreeNode newTreeNode = new TreeNode(csev);
        
        // In Root einhängen
        if(hierarchyMode == 3){ // Root
            Tree t = (Tree)windowParent.getFellow("treeConcepts");  
            ((TreeNode)((TreeModel)t.getModel()).get_root()).getChildren().add(newTreeNode);
        }    
        // Create Association für sub-konzepte und TreeNode einhängen
        else if(hierarchyMode == 2){ // sub-ebene
            // Assoziation erstellen; geht erst nachdem die neue CSE(V) erstell wurde und eine Id bekommen hat
            CodeSystemEntityVersion csevAssociated       = null;
            csevAssociated = (CodeSystemEntityVersion)arg.get("CSEVAssociated"); // für assoziationen   
            CreateConceptAssociationResponse.Return responseAssociation = null;
            responseAssociation = createAssociationResponse(csevAssociated, csev, 2, 4); 
            
            try {
                if(responseAssociation != null && responseAssociation.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.conceptassociation.Status.OK)
                    Messagebox.show(Labels.getLabel("common.error")+"\n" +  Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
                else{   
                    if(responseAssociation.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.conceptassociation.OverallErrorCategory.INFO){                        
                        tnSelected.getChildren().add(newTreeNode);                                                                                                
                    }
                    else{
                        Messagebox.show(Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
                    }
                }
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);} 
        }   
        ((ContentConcepts)windowParent).updateModel(true);
        window.detach();
    }

    @Override
    protected void maintainVersionNew() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void maintain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void maintainVersionEdit() {

        if(contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM){
            MaintainConceptRequestType parameter = new MaintainConceptRequestType();
            parameter.setCodeSystemVersionId(versionId);
            // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
            try {            
                if(dateBoxED != null && dateBoxED.getValue() != null){
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTimeInMillis(dateBoxED.getValue().getTime());               
                    csev.setEffectiveDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
                }                         
            } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}

            // Login
            de.fhdo.terminologie.ws.authoring.LoginType login = new de.fhdo.terminologie.ws.authoring.LoginType();
            login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());             
            parameter.setLogin(login);

            // Versioning 
            VersioningType versioning = new VersioningType();
            versioning.setCreateNewVersion(cbNewVersion.isChecked());
            parameter.setVersioning(versioning);

            // CSE
            parameter.setCodeSystemEntity(cse);

            //CSEV    
            csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
            parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
            parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            MaintainConceptResponseType response = WebServiceHelper.maintainConcept(parameter);

            csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

            // Meldung
            try {
                if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
                    if(parameter.getVersioning().isCreateNewVersion())
                        Messagebox.show(Labels.getLabel("popupConcept.newVersionSuccessfullyCreated"));  
                    else
                        Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
                else
                    Messagebox.show(Labels.getLabel("common.error")+"\n" + Labels.getLabel("popupConcept.conceptNotCreated") +  "\n\n"+ response.getReturnInfos().getMessage());

                window.detach();
                ((ContentConcepts)windowParent).updateModel(true);
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}
        }else{
            MaintainConceptValueSetMembershipRequestType parameter = new MaintainConceptValueSetMembershipRequestType();
            
            // Login
            de.fhdo.terminologie.ws.authoring.LoginType login = new de.fhdo.terminologie.ws.authoring.LoginType();
            login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());             
            parameter.setLogin(login);

            CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
            codeSystemEntityVersion.getConceptValueSetMemberships().clear();
            ConceptValueSetMembership cvsmL = new ConceptValueSetMembership();
            
            cvsmL.setValueSetVersion(new ValueSetVersion());
            cvsmL.getValueSetVersion().setVersionId(cvsm.getId().getValuesetVersionId());
            cvsmL.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
            cvsmL.getCodeSystemEntityVersion().setVersionId(cvsm.getId().getCodeSystemEntityVersionId());
            cvsmL.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
            
            cvsmL.setStatus(Integer.valueOf(tbStatus.getValue()));
            cvsmL.setIsStructureEntry(cbStructureEntry.isChecked());
            cvsmL.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
            cvsmL.setBedeutung(tbBedeutung.getValue());
            cvsmL.setAwbeschreibung(tbAwbeschreibung.getValue());
            cvsmL.setHinweise(tbHinweise.getValue());
            
            codeSystemEntityVersion.getConceptValueSetMemberships().add(cvsmL);
            parameter.setCodeSystemEntityVersion(codeSystemEntityVersion);
            
            // Versioning 
            MaintainConceptValueSetMembershipResponse.Return response = WebServiceHelper.maintainConceptValueSetMembership(parameter);
            
            try {
                if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
                    Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
                    ((ContentConcepts)windowParent).updateModel(true);
                    window.detach();
                    cvsm.setStatus(Integer.valueOf(tbStatus.getValue()));
                    cvsm.setIsStructureEntry(cbStructureEntry.isChecked());
                    cvsm.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
                    cvsm.setBedeutung(tbBedeutung.getValue());
                    cvsm.setAwbeschreibung(tbAwbeschreibung.getValue());
                    cvsm.setHinweise(tbHinweise.getValue());
                    
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}
        } 
    }

    @Override
    protected void updateStatus() {
        
        if(contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM){

            UpdateConceptStatusRequestType  parameter         = new UpdateConceptStatusRequestType();
            parameter.setCodeSystemVersionId(versionId);
            // Login
            de.fhdo.terminologie.ws.authoring.LoginType login = new de.fhdo.terminologie.ws.authoring.LoginType();
            login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());             
            parameter.setLogin(login);

            // CSE
            parameter.setCodeSystemEntity(cse);
            csev.setStatus(Integer.valueOf(tbStatus.getValue()));
            //CSEV    
            csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
            parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
            parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            UpdateConceptStatusResponse.Return response = WebServiceHelper.updateConceptStatus(parameter);

            csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

            // Meldung
            try {
                if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                    Messagebox.show(Labels.getLabel("popupConcept.editStatusSuccessfully"));              
                    ((ContentConcepts)windowParent).updateModel(true);
                    window.detach();
                }
                else
                    Messagebox.show(Labels.getLabel("common.error")+" \n" + Labels.getLabel("popupConcept.editStatusfailed") + "\n\n" + response.getReturnInfos().getMessage());
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}
        }else{
        
            UpdateConceptValueSetMembershipStatusRequestType  parameter         = new UpdateConceptValueSetMembershipStatusRequestType();
            // Login
            de.fhdo.terminologie.ws.authoring.LoginType login = new de.fhdo.terminologie.ws.authoring.LoginType();
            login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());             
            parameter.setLogin(login);

            CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
            codeSystemEntityVersion.getConceptValueSetMemberships().clear();
            ConceptValueSetMembership cvsmL = new ConceptValueSetMembership();
            
            cvsmL.setValueSetVersion(new ValueSetVersion());
            cvsmL.getValueSetVersion().setVersionId(cvsm.getId().getValuesetVersionId());
            cvsmL.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
            cvsmL.getCodeSystemEntityVersion().setVersionId(cvsm.getId().getCodeSystemEntityVersionId());
            cvsmL.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
            cvsmL.setStatus(Integer.valueOf(tbStatus.getValue()));
            codeSystemEntityVersion.getConceptValueSetMemberships().add(cvsmL);
            parameter.setCodeSystemEntityVersion(codeSystemEntityVersion);

            UpdateConceptValueSetMembershipStatusResponse.Return response = WebServiceHelper.updateConceptValueSetMembershipStatus(parameter);

            // Meldung
            try {
                if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                    Messagebox.show(Labels.getLabel("popupConcept.editStatusSuccessfully"));              
                    ((ContentConcepts)windowParent).updateModel(true);
                    window.detach();
                    cvsm.setStatus(Integer.valueOf(tbStatus.getValue()));
                }
                else
                    Messagebox.show(Labels.getLabel("common.error")+" \n" + Labels.getLabel("popupConcept.editStatusfailed") + "\n\n" + response.getReturnInfos().getMessage());
            } catch (Exception ex) {Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);}
        }         
    }

    @Override
    protected void updateStatusVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void onCheck$cbNewVersion(Event event){
        if(cbNewVersion.isChecked())
            bCreate.setLabel(Labels.getLabel("common.create"));    
        else
            bCreate.setLabel(Labels.getLabel("common.change"));
    }
    
    public void onClick$bCreate(Event event){
        buttonAction();                                 
    }    
    
    
    public void onClick$tabDetails(){
    
        bCreate.setDisabled(false);
    }
    public void onClick$tabMetadata(){
    
        bCreate.setDisabled(true);
    }
    public void onClick$tabTranslations(){
    
        bCreate.setDisabled(true);
    }
    public void onClick$tabCrossmapping(){
    
        bCreate.setDisabled(true);
    }
    public void onClick$tabLinkedConcepts(){
    
        bCreate.setDisabled(true);
    }
    public void onClick$tabOntologies(){
    
        bCreate.setDisabled(true);
    }

  @Override
  protected void editmodeMaintainVersionCopy()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected void maintainVersionCopy()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}