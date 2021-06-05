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

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.helper.CopyTerminologyVersion;
import de.fhdo.helper.LanguageHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.ValidityRangeHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.LoginType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupValueSet extends PopupWindow implements IUpdateModal{    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private ValueSet          vs = null;
    private ValueSetVersion   vsv = null;        
       
    private Datebox dateBoxID, dateBoxRD, dateBoxSD1, dateBoxSD2, dateBoxLD;
    private Textbox tbVSName, tbVSDescription, tbVSVStatus, tbVSStatus, tbVSVOid, tbVSDescriptionEng, tbWebsite, tbVSVName, tbVSResponsibleOrganization;
    private Checkbox cbNewVersion;
    private Button  bCreate,bOidBeantragen;
    private Label lReq, lName, lStatus;    
    private Combobox cboxPreferredLanguage, cboxCSVValidityRange;
    private ValueSetVersion vsvToBeCopied;
    
    
    private void loadLanguages(){        
        cboxPreferredLanguage.setModel(LanguageHelper.getListModelList());
    }
    
    public void onInitRenderLater$cboxPreferredLanguage(Event e) {
        if(vsv == null || vsv.getPreferredLanguageId() == null)
            return;
        
        Iterator<Comboitem> it = cboxPreferredLanguage.getItems().iterator();
        while(it.hasNext()){
            Comboitem ci = it.next();
            if(vsv.getPreferredLanguageId().compareTo(LanguageHelper.getLanguageIdByName(ci.getLabel())) == 0){
                cboxPreferredLanguage.setSelectedItem(ci);                
            }
        }                 
    }
    
    public void onInitRenderLater$cboxCSVValidityRange(Event e) {
        if(vsv == null || vsv.getValidityRange()== null)
            return;
        
        Iterator<Comboitem> it = cboxCSVValidityRange.getItems().iterator();
        while(it.hasNext()){
            Comboitem ci = it.next();
            if(vsv.getValidityRange().compareTo(ValidityRangeHelper.getValidityRangeIdByName(ci.getLabel())) == 0){
                cboxCSVValidityRange.setSelectedItem(ci);                
            }
        }                 
    }
    
    @Override
    public void doAfterComposeCustom() {
        loadLanguages();
        cboxCSVValidityRange.setModel(ValidityRangeHelper.getListModelList());
    }
    
    public void onClick$bOidBeantragen(){

        try
        {
          logger.debug("Ã–ffnen des OID Antragsformulars");

          Map map = new HashMap();
          map.put("version", vsv);
					map.put("terminology", "valueset");

          Window win = (Window) Executions.createComponents(
                  "/gui/main/modules/oidEnquiry.zul", null, map);
          ((OidEnquiry) win).setUpdateInterface(this);
          
          win.doModal();
        }
        catch (Exception ex)
        {
          logger.debug("Fehler beim Ã–ffnen der UserDetails: " + ex.getLocalizedMessage());
          ex.printStackTrace();
        }
    }
    
    @Override
    protected void loadDatesIntoGUI() {
        if(vs != null){
            if(vs.getStatusDate() != null)
                dateBoxSD1.setValue(new Date(vs.getStatusDate().toGregorianCalendar().getTimeInMillis()));
        }
        
        if(vsv != null){
            if(vsv.getStatusDate() != null)
                dateBoxSD2.setValue(new Date(vsv.getStatusDate().toGregorianCalendar().getTimeInMillis()));
            if(vsv.getInsertTimestamp() != null)
                dateBoxID.setValue(new Date(vsv.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
            if(vsv.getReleaseDate() != null)
                dateBoxRD.setValue(new Date(vsv.getReleaseDate().toGregorianCalendar().getTimeInMillis()));
            if(vsv.getLastChangeDate() != null)
                dateBoxLD.setValue(new Date(vsv.getLastChangeDate().toGregorianCalendar().getTimeInMillis()));
        }
    }    

     @Override
    protected void initializeDatabinder() {
        binder = new AnnotateDataBinder(window);
        binder.bindBean("vs"        , vs);
        binder.bindBean("vsv"       , vsv);
        binder.bindBean("versioning", versioning);
        binder.loadAll();                
    }
     
    private void createNewValueSetVersion(){        
        vsv = new ValueSetVersion();        
        vsv.setStatus(0);
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.TRUE);   // sonst ist das hier null und das kann zu Problemen führen                
        dateBoxSD2.setValue(null);
        dateBoxRD.setValue(null);
				cboxCSVValidityRange.setText("optional");
    } 
    
    public void onClick$bCreate(){
        buttonAction();
    }                 
   
    public void onClick$bClose(){
       window.detach();
   }     

    public void onCheck$cbNewVersion(){
        // Nur bei Version-bearbeiten soll diese Option eine auswirkung haben. Bei neuen VS,
        if(editMode != EDITMODE_MAINTAIN_VERSION_NEW && EDITMODE_MAINTAIN_VERSION_NEW != EDITMODE_MAINTAIN_VERSION_EDIT)
            return;
        if(cbNewVersion.isChecked()){
            editMode = EDITMODE_MAINTAIN_VERSION_NEW;
        }
        else{
            if(arg.get("VSV") == null){
                editMode = EDITMODE_MAINTAIN_VERSION_NEW; 
                cbNewVersion.setChecked(true);
                try {
                    Messagebox.show(Labels.getLabel("popupValueSet.editNotPossibleNoVersionSelected"));
                } catch (Exception ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}                
            }
            else{
                editMode = EDITMODE_MAINTAIN_VERSION_EDIT;                
            }                               
        }
        editMode(editMode);
    }
////// EditModes ///////////////////////////////////////////////////////////////    
    @Override
    protected void editmodeDetails() {
        vs  = (ValueSet)arg.get("VS");  
        vsv = (ValueSetVersion)arg.get("VSV");                

        window.setTitle(Labels.getLabel("popupValueSet.valueSetDetails"));
        dateBoxRD.setDisabled(true);
        dateBoxSD1.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        tbVSName.setDisabled(true);
        tbVSDescription.setDisabled(true);
        tbVSDescriptionEng.setDisabled(true);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(true);
				tbVSResponsibleOrganization.setDisabled(true);
        tbVSVStatus.setDisabled(true);
        bOidBeantragen.setVisible(false);
        bOidBeantragen.setDisabled(true);
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(true);
        tbVSStatus.setDisabled(true);
        cboxPreferredLanguage.setReadonly(true);
        cboxPreferredLanguage.setDisabled(true);
        cboxCSVValidityRange.setReadonly(true);
        cboxCSVValidityRange.setDisabled(true);  
        bCreate.setVisible(false);        
        cbNewVersion.setVisible(false);
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status"));
    }   
    
    @Override
    protected void editmodeCreate() {
        vs = new ValueSet();        
        vs.setStatus(1);  
        createNewValueSetVersion();

        window.setTitle(Labels.getLabel("popupValueSet.createValueSet"));
        dateBoxRD.setDisabled(false);
        dateBoxSD1.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        tbVSName.setDisabled(false);
        tbVSDescription.setDisabled(false);
        tbVSDescriptionEng.setDisabled(false);
        tbWebsite.setDisabled(false);
        tbVSVName.setDisabled(false);
				tbVSResponsibleOrganization.setDisabled(false);
				tbVSResponsibleOrganization.setValue("");
        tbVSVStatus.setDisabled(false);
        //bOidBeantragen.setVisible(true);
        //bOidBeantragen.setDisabled(false);
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(false);
        tbVSStatus.setDisabled(false);
        cboxPreferredLanguage.setReadonly(false);
        cboxPreferredLanguage.setDisabled(false);
        cboxCSVValidityRange.setReadonly(false);
        cboxCSVValidityRange.setDisabled(false);
        bCreate.setVisible(true);
        bCreate.setLabel(Labels.getLabel("common.create"));
        cbNewVersion.setVisible(true);
        cbNewVersion.setDisabled(true);
        lReq.setVisible(true);
        lName.setValue(Labels.getLabel("common.name") + "*");
        lStatus.setValue(Labels.getLabel("common.status") + "*");
    }

    @Override
    protected void editmodeMaintainVersionNew() {
        vs  = (ValueSet)arg.get("VS");  
        //vsvToBeCopied = (ValueSetVersion)arg.get("VSV"); 
        createNewValueSetVersion();

        window.setTitle(Labels.getLabel("popupValueSet.createValueSetVersion"));
        dateBoxRD.setDisabled(false);
        dateBoxSD1.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        tbVSName.setDisabled(true);
        tbVSDescription.setDisabled(true);
        tbVSDescriptionEng.setDisabled(true);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(false);
				tbVSResponsibleOrganization.setDisabled(false);
        tbVSVStatus.setDisabled(false);
        
        tbVSVOid.setTooltiptext("Beantragen einer OID ist nach dem Anlegen möglich! "
                              + "Bitte rufen sie dazu \"Version bearbeiten\" über das Context-Menü auf! "
                              + "Haben sie bereits eine OID können Sie diese hier eintragen.");
        //bOidBeantragen.setVisible(false);
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(false);
        /*
        if(vsv != null){
            if(vsv.getOid() == null || vsv.getOid().length() <= 0){
                bOidBeantragen.setVisible(true);
                bOidBeantragen.setDisabled(false);
                tbVSVOid.setVisible(false);
            }else{
                bOidBeantragen.setVisible(false);
                tbVSVOid.setVisible(true);
                tbVSVOid.setReadonly(false);
            }
        }*/
        tbVSStatus.setDisabled(false);
        cboxPreferredLanguage.setReadonly(false);
        cboxPreferredLanguage.setDisabled(false);
        cboxCSVValidityRange.setReadonly(false);
        cboxCSVValidityRange.setDisabled(false);
        bCreate.setVisible(true);
        bCreate.setLabel(Labels.getLabel("common.create"));
        cbNewVersion.setVisible(true);
        cbNewVersion.setDisabled(true);        
        lReq.setVisible(true);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status") + "*");
    }

    @Override
    protected void editmodeMaintain() {
        vs  = (ValueSet)arg.get("VS");   
        vsv = (ValueSetVersion)arg.get("VSV");                    
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.FALSE);

        window.setTitle(Labels.getLabel("popupValueSet.editValueSetVersion"));
        dateBoxRD.setDisabled(true);
        dateBoxSD1.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        tbVSName.setDisabled(false);
        tbVSDescription.setDisabled(false);
        tbVSDescriptionEng.setDisabled(false);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(true);
				tbVSResponsibleOrganization.setDisabled(false);
        tbVSVStatus.setDisabled(true);
        bOidBeantragen.setVisible(false);
        bOidBeantragen.setDisabled(true);
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(false);
        tbVSStatus.setDisabled(true);
        cboxPreferredLanguage.setReadonly(true);
        cboxPreferredLanguage.setDisabled(true);
        cboxCSVValidityRange.setReadonly(true);
        cboxCSVValidityRange.setDisabled(true);
        bCreate.setVisible(true);
        bCreate.setLabel(Labels.getLabel("common.change"));
        cbNewVersion.setVisible(false);
        cbNewVersion.setDisabled(true);
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status"));
    }

    @Override
    protected void editmodeMaintainVersionEdit() {
        vs  = (ValueSet)arg.get("VS");   
        vsv = (ValueSetVersion)arg.get("VSV");  
        if(vsv == null){
            editMode = EDITMODE_MAINTAIN_VERSION_NEW; 
            editmodeMaintainVersionNew();
        }            
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.FALSE);

        window.setTitle(Labels.getLabel("popupValueSet.editValueSetVersion"));
        dateBoxRD.setDisabled(false);
        dateBoxSD1.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        tbVSName.setDisabled(true);
        tbVSDescription.setDisabled(true);
        tbVSDescriptionEng.setDisabled(true);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(false);
				tbVSResponsibleOrganization.setDisabled(true);
        tbVSVStatus.setDisabled(true);
				if (vsv.getOid() != null && ! vsv.getOid().equals("")){
					bOidBeantragen.setVisible(false);
					bOidBeantragen.setDisabled(true);
				} else {
					bOidBeantragen.setVisible(true);
					bOidBeantragen.setDisabled(false);
				}
        
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(false);
        tbVSStatus.setDisabled(true);
        cboxPreferredLanguage.setReadonly(false);
        cboxPreferredLanguage.setDisabled(false);
        cboxCSVValidityRange.setReadonly(false);
        cboxCSVValidityRange.setDisabled(false);
        bCreate.setVisible(true);
        bCreate.setLabel(Labels.getLabel("common.change"));
        cbNewVersion.setVisible(true);
        cbNewVersion.setDisabled(true);
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status"));
    }

    @Override
    protected void editmodeUpdateStatus() {
        vs  = (ValueSet)arg.get("VS");   
        vsv = (ValueSetVersion)arg.get("VSV");  
                  
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.FALSE);

        window.setTitle(Labels.getLabel("popupValueSet.editValueSetVersion"));
        
        tbVSName.setDisabled(true);
        tbVSDescription.setDisabled(true);
        tbVSDescriptionEng.setDisabled(true);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(true);
				tbVSResponsibleOrganization.setDisabled(true);
        tbVSStatus.setDisabled(false);
        dateBoxSD1.setDisabled(true);
        cboxPreferredLanguage.setReadonly(true);
        cboxPreferredLanguage.setDisabled(true);
        cboxCSVValidityRange.setReadonly(true);
        cboxCSVValidityRange.setDisabled(true);
        
        dateBoxRD.setDisabled(true);
        tbVSVStatus.setDisabled(true);
        bOidBeantragen.setVisible(false);
        bOidBeantragen.setDisabled(true);
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(true);
        tbVSVOid.setDisabled(true);
        dateBoxSD2.setDisabled(true);
        
        cbNewVersion.setVisible(false);        
        cbNewVersion.setDisabled(true);
        cbNewVersion.setChecked(false);
        
        bCreate.setVisible(true);
        bCreate.setDisabled(false);
        bCreate.setLabel(Labels.getLabel("common.changeStatus"));
        
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status"));
    }

    @Override
    protected void editmodeUpdateStatusVersion() {
        vs  = (ValueSet)arg.get("VS");   
        vsv = (ValueSetVersion)arg.get("VSV");  
                  
        versioning = new VersioningType();
        versioning.setCreateNewVersion(Boolean.FALSE);

        window.setTitle(Labels.getLabel("popupValueSet.editValueSetVersion"));
        
        tbVSName.setDisabled(true);
        tbVSDescription.setDisabled(true);
        tbVSDescriptionEng.setDisabled(true);
        tbWebsite.setDisabled(true);
        tbVSVName.setDisabled(true);
				tbVSResponsibleOrganization.setDisabled(true);
        tbVSStatus.setDisabled(true);
        dateBoxSD1.setDisabled(true);
        cboxPreferredLanguage.setReadonly(true);
        cboxPreferredLanguage.setDisabled(true);
        cboxCSVValidityRange.setReadonly(true);
        cboxCSVValidityRange.setDisabled(true);
        
        dateBoxRD.setDisabled(true);
        tbVSVStatus.setDisabled(false);
				if (vsv.getOid() != null && !vsv.getOid().equals("")){
					bOidBeantragen.setVisible(false);
					bOidBeantragen.setDisabled(true);
				} else {
					bOidBeantragen.setVisible(true);
					bOidBeantragen.setDisabled(false);
				}
        
        tbVSVOid.setVisible(true);
        tbVSVOid.setReadonly(true);
        dateBoxSD2.setDisabled(true);
        
        cbNewVersion.setVisible(false);        
        cbNewVersion.setDisabled(true);
        cbNewVersion.setChecked(false);
        
        bCreate.setVisible(true);
        bCreate.setDisabled(false);
        bCreate.setLabel(Labels.getLabel("common.changeStatus"));
        
        lReq.setVisible(false);
        lName.setValue(Labels.getLabel("common.name"));
        lStatus.setValue(Labels.getLabel("common.status"));
    }   

////// Button-Actions //////////////////////////////////////////////////////////    
    
    @Override
    protected void create() {
        
        boolean runS = true;
        ListValueSetsRequestType para = new ListValueSetsRequestType();

        // login
        if (SessionHelper.isCollaborationActive())
        {
          // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
          para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
          para.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());
        }
        else if (SessionHelper.isUserLoggedIn())
        {
          para.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
          para.getLogin().setSessionID(SessionHelper.getSessionId());
        }

        ListValueSetsResponse.Return resp = WebServiceHelper.listValueSets(para);

        for(ValueSet vsL:resp.getValueSet()){
            if(vs.getName().equals(vsL.getName())){
                runS=false;
            }
        }
                
        
        if(runS){
            if(cboxCSVValidityRange.getSelectedItem() != null){

                //Authoring authoring = new Authoring_Service().getAuthoringPort();

                // Login   
                LoginType login = new LoginType();
                login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId()); 
								
								//Matthias adding resposible Organization
								vs.setResponsibleOrganization(tbVSResponsibleOrganization.getValue());

                // preferredLanguage
                if(cboxPreferredLanguage.getSelectedItem() != null)
                    vsv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));

                // Range of Validity
                if(cboxCSVValidityRange.getSelectedItem() != null)
                    vsv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));

                // Datum in VSV speichern
                try {
                    GregorianCalendar c;
                    if(dateBoxRD != null && dateBoxRD.getValue() != null){
                        c = new GregorianCalendar();
                        c.setTimeInMillis(dateBoxRD.getValue().getTime());               
                        vsv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
                    }   
                } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}       

                vs.getValueSetVersions().clear();
                vs.getValueSetVersions().add(vsv);

                // Create new VS
                try{
                    CreateValueSetRequestType parameter = new CreateValueSetRequestType();

                    // Login, VS
                    parameter.setLogin(login);                       
                    parameter.setValueSet(vs); 

                    // WS aufruf
                    vsv.setValueSet(null);                
                    de.fhdo.terminologie.ws.authoring.CreateValueSetResponse.Return response = WebServiceHelper.createValueSet(parameter);
                    vsv.setValueSet(vs);

                    // Meldung über Erfolg/Misserfolg
                    try {
                        if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                            AssignTermHelper.assignTermToUser(response.getValueSet());
                            Proposal proposal = new Proposal();
                            ValueSet vs_prop = new ValueSet();
                            vs_prop.setId(response.getValueSet().getId());
                            vs_prop.setName(response.getValueSet().getName());
                            vs_prop.setCurrentVersionId(response.getValueSet().getCurrentVersionId());

                            ValueSetVersion vsv_prop = new ValueSetVersion();
                            vsv_prop.setName(response.getValueSet().getValueSetVersions().get(0).getName());
                            vsv_prop.setVersionId(response.getValueSet().getValueSetVersions().get(0).getVersionId());

                            vsv_prop.setValueSet(vs_prop);

                            proposal.setVocabularyId(vs_prop.getId());
                            proposal.setVocabularyName(vs_prop.getName());
                            proposal.setContentType("valueset");
                            proposal.setVocabularyNameTwo("ValueSet");

                            ProposalWorkflow.getInstance().addProposal(proposal, vsv_prop, true);
                            Messagebox.show(Labels.getLabel("popupValueSet.createValueSetSuccessfully"));    
                            ((ContentCSVSDefault)windowParent).refresh();    
                            window.detach();
                        }
                        else
                            Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupValueSet.createValueSetFailed"));                
                    } catch (Exception ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}
                } catch(Exception e){}
            }else{

                Messagebox.show(Labels.getLabel("popupValueSet.editValueSetValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
            }
        }else{
            Messagebox.show("Ein ValueSet mit dem gleichen Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    @Override
    protected void maintainVersionNew() {
        
        boolean runS = true;
        for(ValueSetVersion vsvL:vs.getValueSetVersions()){

                if(vsvL.getName().equals(vsv.getName())){

                    runS = false;
                    break;
                }
            }
        
        if(runS){
            maintainVersionEdit();
        }else{
            Messagebox.show("Eine ValueSetVersion mit dem gleichen Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    @Override
    protected void maintain() {
        //Authoring authoring = new Authoring_Service().getAuthoringPort();
       
        // Login   
        LoginType login = new LoginType();
        login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());                 
        
        ValueSetVersion vsvTemp = vs.getValueSetVersions().get(0);
        vs.getValueSetVersions().clear();
        vs.getValueSetVersions().add(vsvTemp);
               
        try {                                            
            MaintainValueSetRequestType parameter = new MaintainValueSetRequestType();

            // Login, ValueSet, Versioning                       
            parameter.setLogin(login);
            parameter.setValueSet(vs); 
            parameter.setVersioning(versioning);           

            // WS aufruf
            vsvTemp.setValueSet(null); // Zirkel entfernen
            MaintainValueSetResponse.Return response = WebServiceHelper.maintainValueSet(parameter);               
            vsvTemp.setValueSet(vs); // VS wieder einfügen (falls das mal später gebracht wird)

            // Medlung über Erfolg/Misserfolg
            if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                if(parameter.getVersioning().isCreateNewVersion())
                    Messagebox.show(Labels.getLabel("popupValueSet.createValueSetVerisonSuccessfully"));                                            
                else
                    Messagebox.show(Labels.getLabel("popupValueSet.editValueSetVerisonSuccessfully"));
                ((ContentCSVSDefault)windowParent).refresh();    
                window.detach();
            }                            
            else
                Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage() + "\n\n" + Labels.getLabel("popupValueSet.createValueSetVerisonFailed"));                
        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    protected void maintainVersionEdit() {                                                            
        //Authoring authoring = new Authoring_Service().getAuthoringPort();
       
        // Login   
        LoginType login = new LoginType();
        login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId()); 
        
        // preferredLanguage
        if(cboxPreferredLanguage.getSelectedItem() != null)
            vsv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));
        
        // Range of Validity
        if(cboxCSVValidityRange.getSelectedItem() != null){
            vsv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));
        
            // Datum in VSV speichern
            try {
                GregorianCalendar c;
                if(dateBoxRD != null && dateBoxRD.getValue() != null){
                    c = new GregorianCalendar();
                    c.setTimeInMillis(dateBoxRD.getValue().getTime());               
                    vsv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
                }   
            } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}       

            vs.getValueSetVersions().clear();
            vs.getValueSetVersions().add(vsv);

            try {    
              MaintainValueSetRequestType parameter = new MaintainValueSetRequestType();

              // Login, ValueSet, Versioning                       
              parameter.setLogin(login);
              parameter.setValueSet(vs); 
              parameter.setVersioning(versioning);

                //Matthias changed (commented out)
                //Dario: changed back, because creating a new Value-Set-Version threw an error since the VersionID was missing.
                 vsv.setVersionId(Long.MAX_VALUE);

              // WS aufruf
              vsv.setValueSet(null); // Zirkel entfernen
              MaintainValueSetResponse.Return response = WebServiceHelper.maintainValueSet(parameter);               
              vsv.setValueSet(vs); // VS wieder einfügen (falls das mal später gebracht wird)

              // Medlung über Erfolg/Misserfolg
              if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                  if(parameter.getVersioning().isCreateNewVersion())
                  {
                    if(response.getValueSet().getValueSetVersions().get(0).getStatus() == 0)
                    {
                      Proposal proposal = new Proposal();
                      ValueSet vs_prop = new ValueSet();
                      vs_prop.setId(response.getValueSet().getId());
                      vs_prop.setName(response.getValueSet().getName());
                      vs_prop.setCurrentVersionId(response.getValueSet().getCurrentVersionId());

                      ValueSetVersion vsv_prop = new ValueSetVersion();
                      vsv_prop.setName(response.getValueSet().getValueSetVersions().get(0).getName());
                      vsv_prop.setVersionId(response.getValueSet().getValueSetVersions().get(0).getVersionId());

                      vsv_prop.setValueSet(vs_prop);

                      proposal.setVocabularyId(vs_prop.getId());
                      proposal.setVocabularyName(vs_prop.getName());
                      proposal.setContentType("valueset");
                      proposal.setVocabularyNameTwo("ValueSet");

                      ProposalWorkflow.getInstance().addProposal(proposal, vsv_prop, true);
                    }
                    Messagebox.show(Labels.getLabel("popupValueSet.createValueSetVerisonSuccessfully")); 
                  }
                  else
                      Messagebox.show(Labels.getLabel("popupValueSet.editValueSetVerisonSuccessfully"));
                  ((ContentCSVSDefault)windowParent).refresh();    
                  window.detach();
              }                            
              else
              {
                  Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage() + "\n\n" + Labels.getLabel("popupValueSet.createValueSetVerisonFailed"));                
              }
            } catch (Exception e) {e.printStackTrace();}
        
        }else{
        
            Messagebox.show(Labels.getLabel("popupValueSet.editValueSetValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }         
    }

    @Override
    protected void updateStatus() {
        updateStatusVersion();
    }

    @Override
    protected void updateStatusVersion() {
        //Authoring authoring = new Authoring_Service().getAuthoringPort();
       
        // Login   
        LoginType login = new LoginType();
        login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId()); 
        
        // Datum in VSV speichern
        try {
            GregorianCalendar c;
            if(dateBoxRD != null && dateBoxRD.getValue() != null){
                c = new GregorianCalendar();
                c.setTimeInMillis(dateBoxRD.getValue().getTime());               
                vsv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
            }   
        } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}       
        
        vs.getValueSetVersions().clear();
        vs.getValueSetVersions().add(vsv);        
        
        UpdateValueSetStatusRequestType parameter = new UpdateValueSetStatusRequestType();

        // Login, ValueSet, Versioning                       
        parameter.setLogin(login);                        
        parameter.setValueSet(vs);  

        // WS aufruf
        vsv.setValueSet(null); // Zirkel entfernen
        UpdateValueSetStatusResponse.Return response = WebServiceHelper.updateValueSetStatus(parameter);               
        vsv.setValueSet(vs); // VS wieder einfügen (falls das mal später gebracht wird)

        // Medlung über Erfolg/Misserfolg
        try{
            if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK){
                Messagebox.show(Labels.getLabel("popupValueSet.editValueSetStatusSuccessfully"));
                ((ContentCSVSDefault)windowParent).refresh();    
                window.detach();
            }                            
            else
                Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage() + "\n\n" + Labels.getLabel("popupValueSet.editValueSetStatusFailed"));                
        } catch (Exception e) {e.printStackTrace();}             
    }   

    public void update(Object o, boolean edited) {
        
    }

  @Override
  protected void editmodeMaintainVersionCopy()
  {
    vs  = (ValueSet)arg.get("VS");  
    vsvToBeCopied = (ValueSetVersion)arg.get("VSV"); 
    createNewValueSetVersion();

    window.setTitle(Labels.getLabel("popupValueSet.createValueSetVersion"));
    dateBoxRD.setDisabled(false);
    dateBoxSD1.setDisabled(true);
    dateBoxSD2.setDisabled(true);
    tbVSName.setDisabled(true);
    tbVSDescription.setDisabled(true);
    tbVSDescriptionEng.setDisabled(true);
    tbWebsite.setDisabled(true);
    tbVSVName.setDisabled(false);
            tbVSResponsibleOrganization.setDisabled(false);
    tbVSVStatus.setDisabled(false);

    tbVSVOid.setTooltiptext("Beantragen einer OID ist nach dem Anlegen möglich! "
                          + "Bitte rufen sie dazu \"Version bearbeiten\" über das Context-Menü auf! "
                          + "Haben sie bereits eine OID können Sie diese hier eintragen.");
    tbVSVOid.setVisible(true);
    tbVSVOid.setReadonly(false);
    tbVSStatus.setDisabled(false);
    cboxPreferredLanguage.setReadonly(false);
    cboxPreferredLanguage.setDisabled(false);
    cboxCSVValidityRange.setReadonly(false);
    cboxCSVValidityRange.setDisabled(false);
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.create"));
    cbNewVersion.setVisible(true);
    cbNewVersion.setDisabled(true);        
    lReq.setVisible(true);
    lName.setValue(Labels.getLabel("common.name"));
    lStatus.setValue(Labels.getLabel("common.status") + "*");
  }

  @Override
  protected void maintainVersionCopy()
  {
     boolean runS = true;
    for(ValueSetVersion vsvL:vs.getValueSetVersions()){

            if(vsvL.getName().equals(vsv.getName())){

                runS = false;
                break;
            }
        }

    if(runS){
        //Authoring authoring = new Authoring_Service().getAuthoringPort();

        // Login   
        LoginType login = new LoginType();
        login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId()); 

        // preferredLanguage
        if(cboxPreferredLanguage.getSelectedItem() != null)
            vsv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));

        // Range of Validity
        if(cboxCSVValidityRange.getSelectedItem() != null){
            vsv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));

            // Datum in VSV speichern
            try {
                GregorianCalendar c;
                if(dateBoxRD != null && dateBoxRD.getValue() != null){
                    c = new GregorianCalendar();
                    c.setTimeInMillis(dateBoxRD.getValue().getTime());               
                    vsv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));                    
                }   
            } catch (DatatypeConfigurationException ex) {Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);}       

            vs.getValueSetVersions().clear();
            vs.getValueSetVersions().add(vsv);

            try {    
              if(vsvToBeCopied != null)
              {
                ReturnType ret = CopyTerminologyVersion.copyValueSetVersion(vs, vsv, vsvToBeCopied);
                if(ret.isSuccess())
                  Messagebox.show(ret.getMessage(),"",Messagebox.OK,Messagebox.INFORMATION);
                else
                  Messagebox.show(ret.getMessage(),"",Messagebox.OK,Messagebox.ERROR);

                ((ContentCSVSDefault)windowParent).refresh();    
                window.detach();
              }
            } catch (Exception e) {e.printStackTrace();}

        }
        else
        {
            Messagebox.show(Labels.getLabel("popupValueSet.editValueSetValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }
    else
    {
        Messagebox.show("Eine ValueSetVersion mit dem gleichen Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }
  }
}