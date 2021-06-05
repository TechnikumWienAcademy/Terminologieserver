/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.workflow;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.proposal.ProposalStatusChange;
import de.fhdo.collaboration.publication.SelectTargetPopup;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.helper.WebServiceUrlHelper;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse.Return;
import de.fhdo.terminologie.ws.administration.ExportParameterType;
import de.fhdo.terminologie.ws.administration.ExportType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.administrationPub.Administration;
import de.fhdo.terminologie.ws.administrationPub.GetImportValueSetPubResponseResponse;
import de.fhdo.terminologie.ws.administrationPub.GetPubImportResponseResponse;
import de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoringPub.Authoring;
import de.fhdo.terminologie.ws.authoringPub.GetCreateCodeSystemPubResponseResponse;
import de.fhdo.terminologie.ws.authoringPub.GetCreateValueSetPubResponseResponse;
import de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authorizationPub.Authorization;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.searchPub.GetListGloballySearchedConceptsResponseResponse;
import de.fhdo.terminologie.ws.searchPub.GetListValueSetsPubResponeResponse;
import de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.searchPub.ListGloballySearchedConceptsRequestType;
import types.termserver.fhdo.de.CodeSystem;
import de.fhdo.terminologie.ws.searchPub.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.ws.soap.MTOMFeature;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Stefan Puraner
 */
public class TerminologyReleaseManager{
    
    static final private org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    final private boolean sessionIDsSet;
    private String sessionID;
    private String pubSessionID;
    private long importID;
    private de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS;
    private de.fhdo.terminologie.ws.searchPub.ValueSet targetVS;
    private Proposalobject po;

    /**
     * 
     */
    public TerminologyReleaseManager(){
        this.sessionID = SessionHelper.getSessionId();
        this.pubSessionID = CollaborationSession.getInstance().getPubSessionID(); //ANKERNEW
        if(!this.sessionID.isEmpty() && !this.pubSessionID.isEmpty())
            sessionIDsSet = true;
        else
            sessionIDsSet = false;
        this.targetCS = null;
        this.targetVS = null;
        try{
            importID = SecureRandom.getInstance("SHA1PRNG").nextLong();
        }
        catch (NoSuchAlgorithmException ex){
            LOGGER.error("Error [0140]", ex);
        }
    }

    private boolean isPubPlattformAliveAndUserLoggedIn(){
        LOGGER.info("+++++ isPubPlattformAliveAndUserLoggedIn started +++++");
        de.fhdo.terminologie.ws.authorizationPub.LoginRequestType loginRequest = new de.fhdo.terminologie.ws.authorizationPub.LoginRequestType();
        loginRequest.setLogin(new de.fhdo.terminologie.ws.authorizationPub.LoginType());
        loginRequest.getLogin().setSessionID(this.pubSessionID);

        Authorization port_authorizationPub = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();

        if (port_authorizationPub == null){
            LOGGER.info("----- isPubPlattformAliveAndUserLoggedIn finished (001) -----");
            return false;
        }
        
        LOGGER.info("----- isPubPlattformAliveAndUserLoggedIn finished (002) -----");
        return true;
        
        /* DABACA VIELLEICHT WIEDER EINBAUEN
        try{
            CheckLoginResponse.Return loginResponse = port_authorizationPub.checkLogin(loginRequest);
            if (loginResponse.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK)){
                LOGGER.info("----- isPubPlattformAliveAndUserLoggedIn finished (002) -----");
                return true;
            }
            else{
                LOGGER.debug("User not logged in");
                LOGGER.info("----- isPubPlattformAliveAndUserLoggedIn finished (003) -----");
                return false;
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0135]", ex);
            LOGGER.info("----- isPubPlattformAliveAndUserLoggedIn finished (004) -----");
            return false;
        }*/
    }

    private ReturnType transferTerminologyToPublicServer(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject proposalObject){
        LOGGER.info("+++++ transferTerminologyToPublicServer started +++++");
        po = proposalObject;
        ReturnType response = new ReturnType();
        response.setSuccess(false);

        try{
            if (!this.isPubPlattformAliveAndUserLoggedIn()){
                response.setSuccess(false);
                response.setMessage("Verbindung zur Publikationsplattform konnte nicht hergestellt werden.");
                LOGGER.info("----- transferTerminologyToPublicServer finished (001) -----");
                return response;
            }
            
            if(!this.sessionIDsSet){
                response.setSuccess(false);
                response.setMessage("SessionIDs sind nicht gesetzt.");
                LOGGER.info("TermBrowser: Collaboration Session: " + this.sessionID);
                LOGGER.info("TermBrowser: Pub Session: " + this.pubSessionID);
                LOGGER.info("----- transferTerminologyToPublicServer finished (002) -----");
                return response;
            }

            //Start transfer
            if(proposalObject.getClassname().equals("CodeSystemVersion")){
                CodeSystem CStoExport = this.getCodeSystemToExport(proposalObject.getProposal().getVocabularyIdTwo(), proposalObject.getProposal().getVocabularyId());

                //Check if CS was found on collab plattform
                if (CStoExport.getId() == null || CStoExport.getCodeSystemVersions().isEmpty()){
                    response.setSuccess(false);
                    response.setMessage("CodeSystem für Transfer nicht gefunden. CSID: "
                        + proposalObject.getProposal().getVocabularyIdTwo()
                        + ", CSV ID: "
                        + proposalObject.getProposal().getVocabularyId()
                        + "CodeSystem: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (003) -----");
                    return response;
                }

                List<de.fhdo.terminologie.ws.searchPub.CodeSystem> result = this.getTargetCodeSystemFromPub(CStoExport.getName());

                boolean exists = false;
                if ((result != null) && (result.size() == 1)){
                    if(result.get(0).getName().equals(CStoExport.getName())){
                        exists = true;
                        this.targetCS = result.get(0);
                    }
                }
                else if ((result == null) || (result.isEmpty())){
                    //Codesystem not found on pub
                    this.targetCS = null;
                    exists = false;
                }
                else if (result.size() > 1){
                    //Checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.CodeSystem CS : result){
                        if (CS.getName().equals(CStoExport.getName())){
                            exists = true;
                            this.targetCS = CS;
                            break;
                        }
                    }

                    if (!exists){
                        
                        //DABACA COMMENTED OUT
                        //Multiple code systems found
                        /*
                        Map map = new HashMap();
                        map.put("targets", result);
                        map.put("source", CStoExport.getName());

                        Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                        ((SelectTargetPopup) win).setReleaseManager(this);

                        win.doModal();
                        win.setVisible(false);
                        */
                        
                        this.targetCS = result.get(0);
                        
                        if (this.targetCS != null){
                            exists = true;
                        }
                    }
                }                
                if(this.targetCS == null && !exists)
                    this.createTempCodeSystemVersionOnPub(CStoExport); //DABACA ADDED 
                
                if ((this.targetCS == null) && (!exists)){
                    //ANKER3
                    
                    response.setSuccess(false);
                    response.setMessage("CodeSystem konnte auf der Publikationsplattform nicht gefunden werden. (" + CStoExport.getName() + ")");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (004) -----");
                    return response;
                }

                ExportType exportedCodeSystem = this.getExportedCodeSystem(CStoExport);

                if (exportedCodeSystem == null)
                {
                    response.setSuccess(false);
                    response.setMessage("CodeSystem konnte nicht exportiert werden. CSID: "
                            + proposalObject.getProposal().getVocabularyIdTwo()
                            + ", CSV ID: "
                            + proposalObject.getProposal().getVocabularyId()
                            + "CodeSystem: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (005) -----");
                    return response;
                }

                de.fhdo.terminologie.ws.administrationPub.ReturnType ret_import = this.importCodeSystem(CStoExport.getCodeSystemVersions().get(0), exportedCodeSystem);

                if (this.targetCS.getCodeSystemVersions() != null)
                {
                    this.removeTempCodeSystemVersion();
                }

                //3.2.39 added not null checks
                if (ret_import!=null && ret_import.getStatus()!=null && ret_import.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK)) //NULL POINTER
                {
                    response.setSuccess(true);
                    response.setMessage(ret_import.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (006) -----");
                    return response;
                }
                else
                {
                    response.setSuccess(false);
                    if(ret_import!=null)//3.2.39
                        response.setMessage(ret_import.getMessage());
                    else//3.2.39
                        response.setMessage("ret_import message null");//3.2.39
                    LOGGER.info("----- transferTerminologyToPublicServer finished (007) -----");
                    return response;
                }
            }
            else if (proposalObject.getClassname().equals("ValueSetVersion"))
            {   
                ValueSet vsToExport = this.getValueSetToExport(proposalObject.getProposal().getVocabularyName(), proposalObject.getProposal().getVocabularyId());
                
                //check if CS was found on Collab plattform
                if ((vsToExport.getId() == null) || (vsToExport.getValueSetVersions().isEmpty()))
                {
                    response.setSuccess(false);
                    response.setMessage("ValueSet für Transfer nicht gefunden.\nVSID: "
                            + proposalObject.getProposal().getVocabularyIdTwo()
                            + ",\nVSV ID: "
                            + proposalObject.getProposal().getVocabularyId()
                            + "\nValueSet: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (008) -----");
                    return response;
                }
                
                ArrayList<String> missingEntries = this.areValueSetContentsPresentOnPubPlattform(vsToExport);

                if (!missingEntries.isEmpty())
                {
                    response.setSuccess(false);
                    String message = "ValueSet kann nicht freigegeben werden, da benötigte CodeSystem(e) auf der Publikationsplattform fehlen.\n"
                            + "VSID: " + proposalObject.getProposal().getVocabularyIdTwo() + "\n"
                            + "VSV ID: " + proposalObject.getProposal().getVocabularyId() + "\n"
                            + "ValueSet: " + proposalObject.getProposal().getVocabularyName() + "\n"
                            + "Fehlende Codes: " + missingEntries.size() + "\n";
                    
                    int i = 0;
                    for(String s : missingEntries)
                    {
                        message += s+"\n";
                        i++;
                        if(i == 10)
                        {
                            break;
                        }
                    }
                    
                    response.setMessage(message);
                    LOGGER.info("----- transferTerminologyToPublicServer finished (009) -----");
                    return response;
                }

                List<de.fhdo.terminologie.ws.searchPub.ValueSet> result = this.getTargetValueSetFromPub(vsToExport.getName());

                boolean exists = false;
                if ((result != null) && (result.size() == 1))
                {
                    //check if name of found CS and CS to be exported are identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet vs : result)
                    {
                        if (vs.getName().equals(vsToExport.getName()))
                        {
                            exists = true;
                        }
                    }

                    if (exists)
                    {
                        this.targetVS = result.get(0);
                        exists = true;
                    }
                }
                else if ((result == null) || (result.isEmpty()))
                {
                    //kein CodeSystem gefunden
                    this.targetVS = null;
                    exists = false;
                }
                else if (result.size() > 1)
                {
                    //checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet vs : result)
                    {
                        if (vs.getName().equals(vsToExport.getName()))
                        {
                            exists = true;
                            this.targetVS = vs;
                        }
                    }

                    if (!exists)
                    {
                        //mehrere CodeSysteme gefunden
                        Map map = new HashMap();
                        map.put("targets", result);
                        map.put("source", vsToExport.getName());

                        Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                        ((SelectTargetPopup) win).setReleaseManager(this);

                        win.doModal();
                        win.setVisible(false);

                        if (this.targetVS != null)
                        {
                            exists = true;
                        }
                    }
                }

                if(this.targetVS == null && !exists)
                    this.createTempValueSetVersionOnPub(vsToExport); //3.2.39
                targetVS.setName(proposalObject.getProposal().getVocabularyName());
                
                if ((this.targetVS == null) && (!exists))
                {
                    response.setSuccess(false);
                    response.setMessage("ValueSet konnte auf der Publikationsplattform nicht gefunden werden. (" + vsToExport.getName() + ")");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (010) -----");
                    return response;
                }

                ExportType exportedValueSet = this.getExportedValueSet(vsToExport);

                if (exportedValueSet == null)
                {
                    response.setSuccess(false);
                    response.setMessage("ValueSet konnte nicht exportiert werden.\nVSID: "
                            + proposalObject.getProposal().getVocabularyIdTwo()
                            + ",\n VSV ID: "
                            + proposalObject.getProposal().getVocabularyId()
                            + "\nValueSet: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (011) -----");
                    return response;
                }
                
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + vsToExport == null);
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + vsToExport.getValueSetVersions() == null);
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + vsToExport.getValueSetVersions().get(0) == null);
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + proposalObject == null);
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + proposalObject.getProposal() == null);
                LOGGER.info("+++++++++++++++++++++++ CHECK NULL: " + proposalObject.getProposal().getVocabularyName() == null);
                vsToExport.getValueSetVersions().get(0).setName(proposalObject.getProposal().getVocabularyName());//3.2.39

                de.fhdo.terminologie.ws.administrationPub.ReturnType ret_import = this.importValueSet(vsToExport.getValueSetVersions().get(0), exportedValueSet);

                if (this.targetVS.getValueSetVersions() != null)
                {
                    this.removeTempValueSetVersion();
                }

                if (ret_import.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK))
                {
                    response.setSuccess(true);
                    response.setMessage(ret_import.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (012) -----");
                    return response;
                }
                else
                {
                    response.setSuccess(false);
                    response.setMessage(ret_import.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (013) -----");
                    return response;
                }

            }
            else if (proposalObject.getClassname().equals("CodeSystem"))
            {
                CodeSystem csToExport = this.getCodeSystemToExport(proposalObject.getProposal().getVocabularyIdTwo(), null);

                //check if CS was found on Collab plattform
                if ((csToExport.getId() == null) || (csToExport.getCodeSystemVersions().isEmpty()))
                {
                    response.setSuccess(false);
                    response.setMessage("CodeSystem für Transfer nicht gefunden.\nCSID: "
                            + proposalObject.getProposal().getVocabularyIdTwo()
                            + ",\n CSV ID: "
                            + proposalObject.getProposal().getVocabularyId()
                            + "\nCodeSystem: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (014) -----");
                    return response;
                }

                List<de.fhdo.terminologie.ws.searchPub.CodeSystem> result = this.getTargetCodeSystemFromPub(csToExport.getName());

                boolean exists = false;
                if ((result != null) && (result.size() == 1))
                {
                    //check if name of found CS and CS to be exported are identical
                    for (de.fhdo.terminologie.ws.searchPub.CodeSystem cs : result)
                    {
                        if (cs.getName().equals(csToExport.getName()))
                        {
                            exists = true;
                        }
                    }

                    if (exists)
                    {
                        this.targetCS = result.get(0);
                        exists = true;
                    }
                }
                else if ((result == null) || (result.isEmpty()))
                {
                    //kein CodeSystem gefunden
                    this.targetCS = null;
                    exists = false;
                    //Messagebox.show("Kein Ziel-Codesystem gefunden. Transfer nicht möglich.", "Status ändern", Messagebox.OK, Messagebox.ERROR);
                }
                else if (result.size() > 1)
                {
                    //checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.CodeSystem cs : result)
                    {
                        if (cs.getName().equals(csToExport.getName()))
                        {
                            exists = true;
                            this.targetCS = cs;
                        }
                    }
                }

                if ((this.targetCS == null) && (!exists))
                {
                    this.createTempCodeSystemVersionOnPub(csToExport);
                    //now target CS has to be set
                    if (this.targetCS != null)
                    {
                        response.setSuccess(true);
                        response.setMessage("CodeSystem successfully created.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (015) -----");
                        return response;
                    }
                    else
                    {
                        response.setSuccess(false);
                        response.setMessage("CodeSystem konnte auf Publikationsumgebung nicht erstellt werden.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (016) -----");
                        return response;
                    }
                }
                else
                {
                    response.setSuccess(true);
                    response.setMessage("CodeSystem already exists.");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (017) -----");
                    return response;
                }
            }
            else if (proposalObject.getClassname().equals("ValueSet"))
            {
                ValueSet vsToExport = this.getValueSetToExport(proposalObject.getProposal().getVocabularyName(), null);

                //check if CS was found on Collab plattform
                if ((vsToExport.getId() == null) || (vsToExport.getValueSetVersions().isEmpty()))
                {
                    response.setSuccess(false);
                    response.setMessage("ValueSet für Transfer nicht gefunden. VSID: "
                            + proposalObject.getProposal().getVocabularyIdTwo()
                            + ", VSV ID: "
                            + proposalObject.getProposal().getVocabularyId()
                            + "ValueSet: " + proposalObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (018) -----");
                    return response;
                }

                List<de.fhdo.terminologie.ws.searchPub.ValueSet> result = this.getTargetValueSetFromPub(vsToExport.getName());

                boolean exists = false;
                if ((result != null) && (result.size() == 1))
                {
                    //check if name of found CS and CS to be exported are identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet vs : result)
                    {
                        if (vs.getName().equals(vsToExport.getName()))
                        {
                            exists = true;
                        }
                    }

                    if (exists)
                    {
                        this.targetVS = result.get(0);
                        exists = true;
                    }
                }
                else if ((result == null) || (result.isEmpty()))
                {
                    //kein CodeSystem gefunden
                    this.targetVS = null;
                    exists = false;
                }
                else if (result.size() > 1)
                {
                    //checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet vs : result)
                    {
                        if (vs.getName().equals(vsToExport.getName()))
                        {
                            exists = true;
                            this.targetVS = vs;
                        }
                    }
                }

                if ((this.targetVS == null) && (!exists))
                {
                    this.createTempValueSetVersionOnPub(vsToExport);
                    //now target CS has to be set                    
                    if (this.targetVS != null)
                    {
                        response.setSuccess(true);
                        response.setMessage("ValueSet successfully created.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (019) -----");
                        return response;
                    }
                    else
                    {
                        response.setSuccess(false);
                        response.setMessage("ValueSet konnte auf Publikationsumgebung nicht erstellt werden.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (020) -----");
                        return response;
                    }
                }
                else
                {
                    response.setSuccess(true);
                    response.setMessage("ValueSet already exists.");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (021) -----");
                    return response;
                }
            }
        }
        catch (ServerSOAPFaultException ex){
            LOGGER.error("Error [0142]", ex);
            response.setSuccess(false);
            response.setMessage("Die Antwort des Servers ist fehlerhaft.");
        }
        catch (Exception ex){
            LOGGER.error("Error [0141]", ex);
            response.setSuccess(false);
            response.setMessage("An Error occured.");
        }
        LOGGER.info("----- transferTerminologyToPublicServer finished (022) -----");
        return response;
    }

    /**
     * searches for the CodeSystem which has to be exported on the collaboration
     * plattform
     *
     * @param codesystemId
     * @param codesystemVersionId
     * @return
     * @throws ServerSOAPFaultException
     */
    private CodeSystem getCodeSystemToExport(Long codesystemId, Long codesystemVersionId) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getCodeSystemToExport started +++++");
        LOGGER.info("TermBrowser: TRANSFER: Trying to find CodeSystem and Version in COLLAB plattform");
        LOGGER.info("TermBrowser: CS ID: " + codesystemId);
        LOGGER.info("TermBrowser: CSV ID: " + codesystemVersionId);

        de.fhdo.terminologie.ws.search.Search portSearch = WebServiceUrlHelper.getInstance().getSearchServicePort();
        de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType requestSearch = new de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType();
        requestSearch.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        requestSearch.getLogin().setSessionID(this.sessionID);
        requestSearch.setCodeSystem(new CodeSystem());
        requestSearch.getCodeSystem().setId(codesystemId);
        
        de.fhdo.terminologie.ws.search.ListCodeSystemsResponse.Return responseSearch = portSearch.listCodeSystems(requestSearch);
        
        if (responseSearch.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK
            && responseSearch.getCodeSystem() != null
            && responseSearch.getCodeSystem().size() == 1){
            
            if (codesystemVersionId != null){
                CodeSystemVersion CSVsearch = null;
                for (CodeSystemVersion CSVtemp : responseSearch.getCodeSystem().get(0).getCodeSystemVersions()){
                    if (CSVtemp.getVersionId().equals(codesystemVersionId)){
                        CSVsearch = CSVtemp;
                        break;
                    }
                }

                if (CSVsearch != null){
                    CodeSystem CS = responseSearch.getCodeSystem().get(0);
                    CS.getCodeSystemVersions().clear();
                    CS.getCodeSystemVersions().add(CSVsearch);
                    LOGGER.info("----- getCodeSystemToExport finished (001) -----");
                    return CS;
                }
                else{
                    LOGGER.info("----- getCodeSystemToExport finished (002) -----");
                    return new CodeSystem();
                }
            }
            else{
                LOGGER.info("----- getCodeSystemToExport finished (003) -----");
                return responseSearch.getCodeSystem().get(0);
            }
        }
        else{
            LOGGER.info("----- getCodeSystemToExport finished (004) -----");
            return new CodeSystem();
        }
    }

    /**
     * Exports the requested CodeSystem from the collab plattform.
     * If a LOINC-file is requested, the file from the LoincCsvPath in the database will be exported instead
     * since the webservice does not support the LOINF-file-format.
     *
     * @param codesystemId The Id of the code system which will be exported
     * @param codesystemVersionId The versionId of the code system which will be exported
     * @return TODO
     */
    private ExportType getExportedCodeSystem(CodeSystem codesystem) throws ServerSOAPFaultException
    {
        LOGGER.info("+++++ getExportedCodeSystem started +++++");
        Long codesystemId = codesystem.getId();
        Long codesystemVersionId = codesystem.getCodeSystemVersions().get(0).getVersionId();

        LOGGER.info("TermBrowser: TRANSFER: Trying to export CodeSystem and Version from collab plattform");
        LOGGER.info("TermBrowser: CS ID: " + codesystemId);
        LOGGER.info("TermBrowser: CSV ID: " + codesystemVersionId);

        ExportCodeSystemContentRequestType req_export_cs = new ExportCodeSystemContentRequestType();
        req_export_cs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        req_export_cs.getLogin().setSessionID(this.sessionID);
        //3.2.17 added
        //req_export_cs.setLoginAlreadyChecked(true);

        req_export_cs.setCodeSystem(new CodeSystem());
        req_export_cs.getCodeSystem().setId(codesystemId);
        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setVersionId(codesystemVersionId);
        req_export_cs.getCodeSystem().getCodeSystemVersions().add(csv);

        // Export Type
        ExportType eType = new ExportType();
        long format = 193L;

        LOGGER.info("TermBrowser: export format: " + format);
        eType.setFormatId(format);
        eType.setUpdateCheck(false);
        req_export_cs.setExportInfos(eType);

        // Optional: ExportParameter
        ExportParameterType eParameterType = new ExportParameterType();
        eParameterType.setAssociationInfos("");
        eParameterType.setCodeSystemInfos(true);
        eParameterType.setTranslations(true);
        req_export_cs.setExportParameter(eParameterType);

        Return response = new Return();

        /**
         * LOINC is not exported by webservice. Instead a file from the
         * filesystem is loaded.
         */
        if (codesystem.getName().contains("LOINC"))
        {
            String path = DBSysParam.instance().getStringValue("LoincCsvPath", null, null);
            File file = new File(path);
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
                byte bytesPrev[] = new byte[(int) file.length()];
                fis.read(bytesPrev);
                response.setExportInfos(new ExportType());
                response.getExportInfos().setFilecontent(bytesPrev);
                response.getExportInfos().setFormatId(200l);
                response.setReturnInfos(new de.fhdo.terminologie.ws.administration.ReturnType());
                response.getReturnInfos().setStatus(de.fhdo.terminologie.ws.administration.Status.OK);
            }
            catch (FileNotFoundException ex)
            {
                LOGGER.error(ex);
            }
            catch (IOException ex)
            {
                LOGGER.error(ex);
            }
            finally
            {
                if (fis != null)
                {
                    try
                    {
                        fis.close();
                    }
                    catch (IOException ex)
                    {
                        LOGGER.error(ex);
                    }
                }
            }
        }
        else
        {
            // WS-Aufruf
            LOGGER.info("TermBrowser: Calling export service");
            response = WebServiceHelper.exportCodeSystemContent(req_export_cs);
        }
        LOGGER.info("----- getExportedCodeSystem finished (001) -----");
        return response.getExportInfos();
    }

    private List<de.fhdo.terminologie.ws.searchPub.CodeSystem> getTargetCodeSystemFromPub(String codesystemName) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getTargetCodeSystemFromPub started +++++");
        
        final de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        
        ListCodeSystemsRequestType request_searchPubThread = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
        request_searchPubThread.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        request_searchPubThread.getLogin().setSessionID(this.pubSessionID);
        request_searchPubThread.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
        request_searchPubThread.getCodeSystem().setName(codesystemName);
        
        de.fhdo.terminologie.ws.searchPub.GetListCodeSystemsPubReturnResponse.Return response_SearchPub = null;
        
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    port_searchPub.listCodeSystemsPub(request_searchPubThread);
                }
            };
            thread.start();
            
            boolean searchRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(searchRunning){
                Thread.sleep(2*1000);
                searchRunning = thread.isAlive();
                //searchRunning = port_searchPub.checkListCodeSystemsPubRunning();
                seconds += 2;
                if(seconds >= 60){
                    minutes++;
                    seconds = 0;
                    if(minutes >= 60){
                        hours++;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-listCodeSystems running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            response_SearchPub = port_searchPub.getListCodeSystemsPubReturn();
        }
        catch(Exception e){ 
            LOGGER.info("Error [0098]", e);
        }

        if (response_SearchPub!=null
            && response_SearchPub.getReturnInfos()!=null 
            && response_SearchPub.getReturnInfos().getStatus()!=null 
            && response_SearchPub.getReturnInfos().getStatus().equals(Status.OK)){
            for(int n=0;n<response_SearchPub.getCodeSystem().size();n++)
                LOGGER.info("TargetCodeSystemFromPub result: " + response_SearchPub.getCodeSystem().get(n).getName());
            
            LOGGER.info("----- getTargetCodeSystemFromPub finished (001) -----");
            return response_SearchPub.getCodeSystem();
        }
        else{
            LOGGER.info("----- getTargetCodeSystemFromPub finished (002) -----");
            return new ArrayList<>();
        }
    }
    
    private de.fhdo.terminologie.ws.administrationPub.ReturnType importCodeSystem(CodeSystemVersion CSversion, ExportType exportedCS) throws ServerSOAPFaultException{
        LOGGER.info("+++++ importCodeSystem started +++++");
        //de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        
        // Login
        de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionID);
        
        // Codesystem
        request.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
        request.getCodeSystem().setId(this.targetCS.getId());

        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        if (this.targetCS!=null && this.targetCS.getName()!=null && this.targetCS.getName().contains("LOINC")){
            request.getImportInfos().setOrder(true);
            request.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else{
            request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        }

        de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion CSversionPub = new de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion();
        CSversionPub.setName(CSversion.getName());
        request.getCodeSystem().getCodeSystemVersions().add(CSversionPub);
        request.getImportInfos().setFormatId(exportedCS.getFormatId());
        request.getCodeSystem().setName(this.targetCS.getName());
        request.getImportInfos().setFilecontent(exportedCS.getFilecontent());
        request.setImportId(this.importID);

        //3.2.20 next line
        //de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return ret_import = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return();
        //3.2.20 added try catch and thread
        ImportCodeSystemRequestType requestThread = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
        requestThread.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        requestThread.getLogin().setSessionID(this.pubSessionID);
        requestThread.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
        requestThread.getCodeSystem().setId(this.targetCS.getId());

        requestThread.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        if (this.targetCS!=null && this.targetCS.getName()!=null && this.targetCS.getName().contains("LOINC"))
        {
            requestThread.getImportInfos().setOrder(true);
            requestThread.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else
        {
            requestThread.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        }
        requestThread.getCodeSystem().getCodeSystemVersions().add(CSversionPub);
        requestThread.getImportInfos().setFormatId(exportedCS.getFormatId());
        requestThread.getCodeSystem().setName(this.targetCS.getName());
        requestThread.getImportInfos().setFilecontent(exportedCS.getFilecontent());
        requestThread.setImportId(this.importID);
        
        final Administration importPort = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        GetPubImportResponseResponse.Return ret_importThread = null;
        try{
            //original line before 3.2.20
            //de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return ret_import2 = port.importCodeSystem(request);
            Thread thread = new Thread(){
              @Override
              public void run(){
                  //de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
                  //port.importCodeSystem(requestThread);
                  importPort.importCodeSystemPub(requestThread);
                  
              }
            };
            thread.start();
            
            boolean importRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(importRunning){
                Thread.sleep(2*1000);
                //importRunning = thread.isAlive();
                //importRunning = importPort.checkImportRunning();
                importRunning = thread.isAlive();
                seconds += 2;
                if(seconds >= 60){
                    minutes++;
                    seconds = 0;
                    if(minutes >= 60){
                        hours++;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-importCodeSystem running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            ret_importThread = importPort.getPubImportResponse();
        }
        catch(Exception e){
            LOGGER.info("Error [0146]", e);
        }
        
        if(ret_importThread != null){
            LOGGER.info("----- importCodeSystem finished (001) -----");
            return ret_importThread.getReturnInfos();
        }
        else{
            LOGGER.info("----- importCodeSystem finished (002) -----");
            return null;
        }
    }

    private void createTempCodeSystemVersionOnPub(CodeSystem CS){
        LOGGER.info("+++++ createTempCodeSystemVersionOnPub started +++++");
        final de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType createRequest = new de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType();
        createRequest.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        createRequest.getLogin().setSessionID(this.pubSessionID);

        de.fhdo.terminologie.ws.authoringPub.CodeSystem CS_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
        CS_pub.setName(CS.getName());
        CS_pub.setDescription(CS.getDescription());
        CS_pub.setDescriptionEng(CS.getDescriptionEng());
        CS_pub.setIncompleteCS(CS.isIncompleteCS());
        CS_pub.setResponsibleOrganization(CS.getResponsibleOrganization());
        CS_pub.setWebsite(CS.getWebsite());
        CS_pub.setAutoRelease(CS.isAutoRelease());
        CS_pub.setCodeSystemType(CS.getCodeSystemType());
        
        de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion CSV_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
        CSV_pub.setName("Temp_import");
        CS_pub.getCodeSystemVersions().add(CSV_pub);
        
        createRequest.setCodeSystem(CS_pub);
        //3.2.17 added
        //request.setLoginAlreadyChecked(true);
        
        //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();

        //de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemResponse.Return ret_pub = port.createCodeSystem(request);
        
        //3.2.21 start
        //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);

        final de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        GetCreateCodeSystemPubResponseResponse.Return ret_pub = null;
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    port.createCodeSystemPub(createRequest);
                }
            };
            thread.start();

            boolean createRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(createRunning){
                Thread.sleep(2*1000);
                createRunning = thread.isAlive();
                seconds += 2;
                if(seconds >= 60){
                    minutes++;
                    seconds = 0;
                    if(minutes >= 60){
                        hours++;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-createCodeSystem running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            ret_pub = port.getCreateCodeSystemPubResponse();
        }
        catch(Exception e){
            LOGGER.error("Error [0150]", e);
        }
        
        //if (ret_pub != null && ret_pub.getReturnInfos() != null && ret_pub.getReturnInfos().getStatus() != null && ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
        //DABACA next line pulled out of following if statement
        targetCS = new de.fhdo.terminologie.ws.searchPub.CodeSystem();
        if(ret_pub != null){
            //targetCS = new de.fhdo.terminologie.ws.searchPub.CodeSystem();
            if(ret_pub.getCodeSystem()!=null)
                targetCS.setId(ret_pub.getCodeSystem().getId());
        }
        LOGGER.info("----- createTempCodeSystemVersionOnPub finished (001) -----");
    }

    private void removeTempCodeSystemVersion()
    {
        LOGGER.info("+++++ removeTempCodeSystemVersion started +++++");
        for (de.fhdo.terminologie.ws.searchPub.CodeSystemVersion v : this.targetCS.getCodeSystemVersions())
        {
            //3.2.26 changed from equals to contains
            if (v.getName().contains("Temp_import"))
            {
                final de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                req_remove.getLogin().setSessionID(this.pubSessionID);
                //3.2.17 added
                //req_remove.setLoginAlreadyChecked(true);

                req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.CodeSystem cs_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
                cs_remove.setId(this.targetCS.getId());
                de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion csv_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
                csv_remove.setVersionId(v.getVersionId());
                cs_remove.getCodeSystemVersions().add(csv_remove);

                req_remove.getDeleteInfo().setCodeSystem(cs_remove);
                req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.CODE_SYSTEM_VERSION);

                //3.2.21 start
                //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);
                
                final Authoring removeTMPport = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                RemoveTerminologyOrConceptResponseType resp_remove = null;
                try{
                    Thread thread = new Thread(){
                      @Override
                      public void run(){
                          removeTMPport.removeTerminologyOrConcept(req_remove);
                      }
                    };
                    thread.start();

                    boolean removalRunning = true;
                    byte seconds = 0;
                    byte minutes = 0;
                    byte hours = 0;
                    while(removalRunning){
                        Thread.sleep(2*1000);
                        removalRunning = thread.isAlive();
                        seconds += 2;
                        if(seconds >= 60){
                            minutes++;
                            seconds = 0;
                            if(minutes >= 60){
                                hours++;
                                minutes = 0;
                            }
                        }
                        LOGGER.info("Pub-removeTerminologyOrConcept running for " + hours + "h " + minutes + "m " + seconds + "s ");
                    }
                    resp_remove = removeTMPport.getRemoveTerminologyOrConceptPubResponse();

                }
                catch(Exception e){
                    LOGGER.info("Exception caught while removing code-system on pub and communicating back to collab");
                }
                //3.2.21 end
                //3.2.21 end
                
                if (resp_remove != null && resp_remove.getReturnInfos() != null && resp_remove.getReturnInfos().getStatus() != null && resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                {
                    LOGGER.info("TermBrowser: Temp Version erfolgreich entfernt.");
                }
                else
                {
                    LOGGER.error("TermBrowser: Löschen der Version fehlgeschlagen. ");
                }
            }
            else
            {
            }
        }
        LOGGER.info("----- removeTempCodeSystemVersion finished (001) -----");
    }

    private void removeTempValueSetVersion()
    {
        LOGGER.info("+++++ removeTempValueSetVersion started +++++");
        for (de.fhdo.terminologie.ws.searchPub.ValueSetVersion v : targetVS.getValueSetVersions())
        {
            if (v.getName().equals("Temp_import"))
            {
                final de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                req_remove.getLogin().setSessionID(this.pubSessionID);

                req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.ValueSet vs_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                vs_remove.setId(targetVS.getId());
                de.fhdo.terminologie.ws.authoringPub.ValueSetVersion vsv_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
                vsv_remove.setVersionId(v.getVersionId());
                vs_remove.getValueSetVersions().add(vsv_remove);

                req_remove.getDeleteInfo().setValueSet(vs_remove);
                req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.VALUE_SET_VERSION);

                final de.fhdo.terminologie.ws.authoringPub.Authoring removePort = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();

                //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);
                //3.2.21 start
                //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);
                RemoveTerminologyOrConceptResponseType resp_remove = null;
                try{
                    Thread thread = new Thread(){
                      @Override
                      public void run(){
                          removePort.removeTerminologyOrConcept(req_remove);
                      }
                    };
                    thread.start();

                    boolean removalRunning = true;
                    byte seconds = 0;
                    byte minutes = 0;
                    byte hours = 0;
                    while(removalRunning){
                        Thread.sleep(2*1000);
                        removalRunning = thread.isAlive();
                        seconds += 2;
                        if(seconds >= 60){
                            minutes++;
                            seconds = 0;
                            if(minutes >= 60){
                                hours++;
                                minutes = 0;
                            }
                        }
                        LOGGER.info("Pub-removeTerminologyOrConcept running for " + hours + "h " + minutes + "m " + seconds + "s");
                    }
                    resp_remove = removePort.getRemoveTerminologyOrConceptPubResponse();

                }
                catch(Exception e){
                    LOGGER.info("Exception caught while removing terminology or concept on pub and communicating back to collab");
                }
                //3.2.21 end
                //3.2.21 end
                
                if (resp_remove != null && resp_remove.getReturnInfos() != null && resp_remove.getReturnInfos().getStatus() != null && resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                {
                    LOGGER.info("TermBrowser: Temp Version erfolgreich entfernt.");
                }
                else
                {
                    LOGGER.error("TermBrowser: Löschen der Version fehlgeschlagen. ");
                }
            }
        }
        LOGGER.info("----- removeTempValueSetVersion finished (001) -----");
    }

    private ValueSet getValueSetToExport(String valuesetName, Long valuesetVersionId) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getValueSetToExport started +++++");
        LOGGER.info("TermBrowser: TRANSFER: Trying to find ValueSet and Version in collab plattform");
        LOGGER.info("TermBrowser: VS name: " + valuesetName);
        LOGGER.info("TermBrowser: VSV ID: " + valuesetVersionId);

        ListValueSetsRequestType req_vs = new ListValueSetsRequestType();
        req_vs.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        req_vs.getLogin().setSessionID(this.sessionID);

        ValueSet vs_search = new ValueSet();
        //vs_search.setId(po.getClassId());
        //vs_search.setId(valuesetVersionId);
        vs_search.setName(valuesetName);
        req_vs.setValueSet(vs_search);
        
        de.fhdo.terminologie.ws.search.Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();

        ListValueSetsResponse.Return ret_vs = port.listValueSets(req_vs);
        
        if ((ret_vs.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                && (ret_vs.getValueSet() != null)
                && (ret_vs.getValueSet().size() == 1)){
            if (valuesetVersionId != null){
                ValueSetVersion vsv_search = null;
                for (ValueSetVersion vsv_temp : ret_vs.getValueSet().get(0).getValueSetVersions()){
                    if (vsv_temp.getVersionId().equals(valuesetVersionId)){
                        vsv_search = vsv_temp;
                    }
                }

                if (vsv_search != null){
                    ValueSet vs = ret_vs.getValueSet().get(0);
                    vs.getValueSetVersions().clear();
                    vs.getValueSetVersions().add(vsv_search);
                    LOGGER.info("----- getValueSetToExport finished (001) -----");
                    return vs;
                }
                else{
                    LOGGER.info("----- getValueSetToExport finished (002) -----");
                    return new ValueSet();
                }
            }
            else{
                LOGGER.info("----- getValueSetToExport finished (003) -----");
                return ret_vs.getValueSet().get(0);
            }
        }
        else if(ret_vs.getValueSet().size() > 1){
            ValueSetVersion vsv_search = null;
            for(ValueSet v : ret_vs.getValueSet()){
                if(v.getName().equals(valuesetName)){
                    if (valuesetVersionId != null){
                        //ValueSetVersion vsv_search = null;
                        for (ValueSetVersion vsv_temp : v.getValueSetVersions())
                        {
                            if (vsv_temp.getVersionId().equals(valuesetVersionId))
                            {
                                vsv_search = vsv_temp;
                            }
                        }

                        if (vsv_search != null)
                        {
                            ValueSet vs = v;
                            vs.getValueSetVersions().clear();
                            vs.getValueSetVersions().add(vsv_search);
                            LOGGER.info("----- getValueSetToExport finished (004) -----");
                            return vs;
                        }
                        else
                        {
                            //3.2.39 commented out
                            //LOGGER.info("----- getValueSetToExport finished (005) -----");
                            //return new ValueSet();
                        }
                    }
                    else
                    {
                        LOGGER.info("----- getValueSetToExport finished (006) -----");
                        return ret_vs.getValueSet().get(0);
                    }
                }
            }
        }
        else
        {
            LOGGER.info("----- getValueSetToExport finished (007) -----");
            return new ValueSet();
        }
        LOGGER.info("----- getValueSetToExport finished (008) -----");
        return new ValueSet();
    }

    private static long FIX_VS_ID;
    
    private ExportType getExportedValueSet(ValueSet valueSet) throws ServerSOAPFaultException
    {
        LOGGER.info("+++++ getExportedValueSet started +++++");
        Long valuesetId = valueSet.getId();
        Long valueSetVersionId = valueSet.getValueSetVersions().get(0).getVersionId();

        LOGGER.info("TermBrowser: TRANSFER: Trying to export ValueSet and Version from collab plattform");
        LOGGER.info("TermBrowser: VS ID: " + valuesetId);
        LOGGER.info("TermBrowser: VSV ID: " + valueSetVersionId);
        FIX_VS_ID = valuesetId; //TRMMRK
        
        ExportValueSetContentRequestType req_export_vs = new ExportValueSetContentRequestType();

        // Login
        req_export_vs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        req_export_vs.getLogin().setSessionID(this.pubSessionID);

        req_export_vs.setValueSet(new ValueSet());
        req_export_vs.getValueSet().setId(valuesetId);

        ValueSetVersion vsv = new ValueSetVersion();
        vsv.setVersionId(valueSetVersionId);
        req_export_vs.getValueSet().getValueSetVersions().add(vsv);

        // Export Type
        ExportType eType = new ExportType();
        eType.setFormatId(195l);            // TODO 193 = ClaML, 194 = CSV, 195 = SVS
        eType.setUpdateCheck(false);
        req_export_vs.setExportInfos(eType);

        // Optional: ExportParameter
        ExportParameterType eParameterType = new ExportParameterType();
        eParameterType.setAssociationInfos("");
        eParameterType.setCodeSystemInfos(false);
        eParameterType.setTranslations(false);
        req_export_vs.setExportParameter(eParameterType);

        LOGGER.debug("TermBrowser - calling export-service");

        //3.2.17
        //req_export_vs.setLoginAlreadyChecked(true);
        
        // WS-Aufruf
        ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(req_export_vs);

        LOGGER.info("----- getExportedValueSet finished (001) -----");
        return response.getExportInfos();
    }

    /**
     * checks if the ValueSet Codes are already present in CodeSystems on the
     * Pub Plattform
     *
     * @param valueSet
     * @return
     */
    private ArrayList<String> areValueSetContentsPresentOnPubPlattform(ValueSet valueSet)
    {
        LOGGER.info("+++++ areValueSetContentsPresentOnPubPlattform started +++++");
        ArrayList<String> missingEntries = new ArrayList<String>();
        
        ListValueSetContentsRequestType requestVsContent = new ListValueSetContentsRequestType();
        requestVsContent.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        requestVsContent.getLogin().setSessionID(this.sessionID);

        requestVsContent.setValueSet(valueSet);
        requestVsContent.setReadMetadataLevel(false);

        Search search_port = WebServiceUrlHelper.getInstance().getSearchServicePort();

        //3.2.17 added
        //requestVsContent.setLoginAlreadyChecked(true);
        
        ListValueSetContentsResponse.Return responseVsContent = search_port.listValueSetContents(requestVsContent);

        if (responseVsContent.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK))
        {
            Iterator<CodeSystemEntity> it = responseVsContent.getCodeSystemEntity().iterator();
            final de.fhdo.terminologie.ws.searchPub.Search portSearchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            do
            {
                CodeSystemEntity cse = it.next();
                final ListGloballySearchedConceptsRequestType parameter = new ListGloballySearchedConceptsRequestType();

                //CS Global Search
                parameter.setCodeSystemConceptSearch(true);

                parameter.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                parameter.getLogin().setSessionID(this.pubSessionID);

                parameter.setCode(cse.getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCode());
                parameter.setTerm("");
                //3.2.17
                //parameter.setLoginAlreadyChecked(true);
                
                //PUB ÄNDERUNGEN
                //ListGloballySearchedConceptsResponse.Return response = portSearchPub.listGloballySearchedConcepts(parameter);
                //3.2.21 start
                GetListGloballySearchedConceptsResponseResponse.Return response = null;
                try{
                    Thread thread = new Thread(){
                      @Override
                      public void run(){
                          portSearchPub.listGloballySearchedConceptsPub(parameter);
                      }
                    };
                    thread.start();

                    boolean listRunning = true;
                    double seconds = 0;
                    byte minutes = 0;
                    byte hours = 0;
                    while(listRunning){
                        Thread.sleep(10);
                        listRunning = thread.isAlive();
                        seconds += 0.01;
                        if(seconds >= 60){
                            minutes++;
                            seconds = 0;
                            if(minutes >= 60){
                                hours++;
                                minutes = 0;
                            }
                        }
                        if(seconds%1==0)
                            LOGGER.info("Pub-listGloballySearchedConcepts running for " + hours + "h " + minutes + "m " + seconds + "s");
                    }
                    response = portSearchPub.getListGloballySearchedConceptsResponse();

                }
                catch(Exception e){
                    LOGGER.info("Exception caught while listing Value-Sets on pub and communicating back to collab");
                }
                //3.2.21 end
                //3.2.21 end
                
                
                if (response != null && response.getReturnInfos() != null && response.getReturnInfos().getStatus() != null && response.getReturnInfos().getStatus().equals(Status.OK))
                {
                    if (response.getGlobalSearchResultEntry().isEmpty())
                    {
                        missingEntries.add(parameter.getCode());
                        LOGGER.info("TermBrowser: TerminologyReleaseManager.areValueSetContentsPresentOnPubPlattform: Code" + parameter.getCode() + " nicht gefunden");
                    }
                }
                else
                {
                    missingEntries.add(parameter.getCode());
                }
            }
            while (it.hasNext());
        }
        LOGGER.info("----- areValueSetContentsPresentOnPubPlatform finished (001) -----");
        return missingEntries;
    }

    private List<de.fhdo.terminologie.ws.searchPub.ValueSet> getTargetValueSetFromPub(String valuesetName) throws ServerSOAPFaultException
    {
        LOGGER.info("+++++ getTargetValueSetFromPub started +++++");
        final de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        final de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
        request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        request_searchPub.getLogin().setSessionID(this.pubSessionID);
        request_searchPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
        request_searchPub.getValueSet().setName(valuesetName);
        //3.2.17 added
        //request_searchPub.setLoginAlreadyChecked(true);
        
        //de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);
        GetListValueSetsPubResponeResponse.Return respSearchPub = null;
        //3.2.21 start
        try{
            Thread thread = new Thread(){
              @Override
              public void run(){
                  port_searchPub.listValueSetsPub(request_searchPub);
              }
            };
            thread.start();

            boolean listRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(listRunning){
                Thread.sleep(2*1000);
                listRunning = thread.isAlive();
                seconds += 2;
                if(seconds >= 60){
                    minutes++;
                    seconds = 0;
                    if(minutes >= 60){
                        hours++;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-listValueSets running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            respSearchPub = port_searchPub.getListValueSetsPubRespone();

        }
        catch(Exception e){
            LOGGER.info("Exception caught while listing Value-Sets on pub and communicating back to collab");
        }
        //3.2.21 end
        //3.2.21 end
        
        if (respSearchPub != null && respSearchPub.getReturnInfos() != null && respSearchPub.getReturnInfos().getStatus() != null && respSearchPub.getReturnInfos().getStatus().equals(Status.OK))
        {
            LOGGER.info("----- getTargetValueSetFromPub finished (001) -----");
            return respSearchPub.getValueSet();
        }
        else
        {
            LOGGER.info("----- getTargetValueSetFromPub finished (002) -----");
            return new ArrayList<de.fhdo.terminologie.ws.searchPub.ValueSet>();
        }
    }

    private de.fhdo.terminologie.ws.administrationPub.ReturnType importValueSet(ValueSetVersion vsv, ExportType exportedVs) throws ServerSOAPFaultException
    {
        LOGGER.info("+++++ importValueSet started +++++");
        final de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        // Login
        final de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionID);
        LOGGER.info("TRMMRK FIX_VS_ID: " + FIX_VS_ID);
        request.getValueSet().setId(FIX_VS_ID); //TRMMRK
        
        // Codesystem
        request.setValueSet(new de.fhdo.terminologie.ws.administrationPub.ValueSet());
        request.getValueSet().setId(this.targetVS.getId());

        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        request.getImportInfos().setFormatId(301l); //export formatId != import formatId; 195 != 301
        request.getImportInfos().setFilecontent(exportedVs.getFilecontent());
        request.getImportInfos().setOrder(Boolean.TRUE);
        request.setImportId(this.importID);

        de.fhdo.terminologie.ws.administrationPub.ValueSetVersion vsv_pub = new de.fhdo.terminologie.ws.administrationPub.ValueSetVersion();
        vsv_pub.setName(vsv.getName());
        request.getValueSet().getValueSetVersions().add(vsv_pub);

        //3.2.17 added
        //request.setLoginAlreadyChecked(true);
        
        //de.fhdo.terminologie.ws.administrationPub.ImportValueSetResponse.Return ret_import = port.importValueSet(request);
        //3.2.21 start
        GetImportValueSetPubResponseResponse.Return response = null;
        try{
            Thread thread = new Thread(){
              @Override
              public void run(){
                  port.importValueSetPub(request);
              }
            };
            thread.start();

            boolean importRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(importRunning){
                Thread.sleep(2*1000);
                importRunning = thread.isAlive();
                seconds += 2;
                if(seconds >= 60){
                    minutes++;
                    seconds = 0;
                    if(minutes >= 60){
                        hours++;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-importValueSet running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            response = port.getImportValueSetPubResponse();

        }
        catch(Exception e){
            LOGGER.info("Exception caught while listing Value-Sets on pub and communicating back to collab");
        }
        //3.2.21 end        
        //3.2.21 end        
        
        if(response != null){
            LOGGER.info("----- importValueSet finished (001) -----");
            return response.getReturnInfos();
        }
        else{
            LOGGER.info("----- importValueSet finished (002) -----");
            return null;
        }
    }

    private void createTempValueSetVersionOnPub(ValueSet vs){
        LOGGER.info("+++++ createTempValueSetVersionOnPub started +++++");
        final de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionID);

        de.fhdo.terminologie.ws.authoringPub.ValueSet vs_pub = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
        vs_pub.setName(vs.getName());
        vs_pub.setDescription(vs.getDescription());
        vs_pub.setDescriptionEng(vs.getDescriptionEng());
        vs_pub.setResponsibleOrganization(vs.getResponsibleOrganization());
        vs_pub.setWebsite(vs.getWebsite());
        vs_pub.setAutoRelease(vs.isAutoRelease());

        de.fhdo.terminologie.ws.authoringPub.ValueSetVersion vsv_pub = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
        vsv_pub.setName("Temp_import");
        vs_pub.getValueSetVersions().add(vsv_pub);
        request.setValueSet(vs_pub);

        //PUBCHANGE
        final de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        //de.fhdo.terminologie.ws.authoringPub.CreateValueSetResponse.Return ret_pub = port.createValueSet(request);

        //3.2.21 start
        GetCreateValueSetPubResponseResponse.Return ret_pub = null;
        try{
            Thread thread = new Thread(){
              @Override
              public void run(){
                  port.createValueSetPub(request);
              }
            };
            thread.start();

            boolean importRunning = true;
            byte seconds = 0;
            byte minutes = 0;
            byte hours = 0;
            while(importRunning){
                Thread.sleep(2*1000);
                importRunning = thread.isAlive();
                seconds += 2;
                if(seconds>=60){
                    minutes += 1;
                    seconds = 0;
                    if(minutes>=60){
                        hours += 1;
                        minutes = 0;
                    }
                }
                LOGGER.info("Pub-createValueSet running for " + hours + "h " + minutes + "m " + seconds + "s");
            }
            ret_pub = port.getCreateValueSetPubResponse();

        }
        catch(Exception e){
            LOGGER.info("Exception caught while listing Value-Sets on pub and communicating back to collab");
        }
        //3.2.21 end  
        
        if (ret_pub != null && ret_pub.getReturnInfos() != null && ret_pub.getReturnInfos().getStatus() != null && ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
        {
            this.targetVS = new de.fhdo.terminologie.ws.searchPub.ValueSet();
            if(ret_pub.getValueSet()!=null)//3.2.39
                this.targetVS.setId(ret_pub.getValueSet().getId());
        }
        LOGGER.info("----- createTempValueSetVersionOnPub finished (001) -----");
    }
    
    public ReturnType initTransfer(Set<Proposalobject> proposalObjects, Statusrel rel){
        LOGGER.info("+++++ initTransfer started +++++");
        ReturnType transfer_success = new ReturnType();
        transfer_success.setSuccess(false);

        try{
            if(proposalObjects.isEmpty()){
                transfer_success.setMessage("Error: Empty set of Proposalobjects.");
                LOGGER.info("----- initTransfer finished (001) -----");
                return transfer_success;
            }
            
            List<Proposalobject> terminologies = new ArrayList<>();
            List<Proposalobject> codesystems = new ArrayList<>();
            List<Proposalobject> valuesets = new ArrayList<>();
            List<Proposalobject> codesystemVersions = new ArrayList<>();
            List<Proposalobject> valuesetVersions = new ArrayList<>();
            List<Proposalobject> codesystemConcepts = new ArrayList<>();
            List<Proposalobject> valuesetConcepts = new ArrayList<>();
            List<Proposalobject> other = new ArrayList<>();
            for (Proposalobject proposalObject : proposalObjects){
                if (proposalObject.getClassname().equals("CodeSystem"))
                    codesystems.add(proposalObject);
                else if (proposalObject.getClassname().equals("ValueSet"))
                    valuesets.add(proposalObject);
                else if (proposalObject.getClassname().equals("CodeSystemVersion"))
                    codesystemVersions.add(proposalObject);
                else if (proposalObject.getClassname().equals("ValueSetVersion"))
                    valuesetVersions.add(proposalObject);
                else if (proposalObject.getClassname().equals("CodeSystemConcept"))
                    codesystemConcepts.add(proposalObject);
                else if (proposalObject.getClassname().equals("ConceptValueSetMembership"))
                    valuesetConcepts.add(proposalObject);
                else
                    other.add(proposalObject);
            }

            terminologies.addAll(codesystems);
            terminologies.addAll(valuesets);
            terminologies.addAll(codesystemVersions);
            terminologies.addAll(valuesetVersions);
            terminologies.addAll(codesystemConcepts);
            terminologies.addAll(valuesetConcepts);
            terminologies.addAll(other);

            for (Proposalobject proposalObject : terminologies){
                transfer_success = this.transferTerminologyToPublicServer(rel.getStatusByStatusIdTo(), proposalObject); //ANKER NULL POINTER
                if (!transfer_success.isSuccess()){
                    LOGGER.info("A ProposalObject could not be transferred to the public server");
                    break;
                }
            }
            LOGGER.info("----- initTransfer finished (002) -----");
            return transfer_success;
        }
        catch (Exception ex)
        {
            transfer_success.setSuccess(false);
            transfer_success.setMessage(ex.getMessage());
            LOGGER.error("Error [0134]", ex);
            LOGGER.info("----- initTransfer finished (003) -----");
            return transfer_success;
        }
    }

    public void setTargetCS(de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS){
        this.targetCS = targetCS;
    }

    public void setTargetVS(de.fhdo.terminologie.ws.searchPub.ValueSet targetVS){
        this.targetVS = targetVS;
    }
}
