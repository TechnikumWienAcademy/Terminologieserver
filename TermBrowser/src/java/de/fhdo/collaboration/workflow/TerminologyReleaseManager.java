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
import de.fhdo.terminologie.ws.authoringPub.Authoring;
import de.fhdo.terminologie.ws.authoringPub.GetCreateCodeSystemPubResponseResponse;
import de.fhdo.terminologie.ws.authoringPub.GetCreateValueSetPubResponseResponse;
import de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authorizationPub.Authorization;
import de.fhdo.terminologie.ws.authorizationPub.CheckLoginResponse;
import de.fhdo.terminologie.ws.authorizationPub.LoginRequestType;
import de.fhdo.terminologie.ws.authorizationPub.LoginType;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.searchPub.GetListGloballySearchedConceptsResponseResponse;
import de.fhdo.terminologie.ws.searchPub.GetListValueSetsPubResponeResponse;
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
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private String sessionId;
    private String pubSessionId;
    private de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS;
    private de.fhdo.terminologie.ws.searchPub.ValueSet targetVS;
    private long importId;

    /**
     * TODO
     */
    public TerminologyReleaseManager(){
        this.sessionId = SessionHelper.getSessionId();
        this.pubSessionId = CollaborationSession.getInstance().getPubSessionID();
        this.targetCS = null;
        this.targetVS = null;
        try{
            importId = SecureRandom.getInstance("SHA1PRNG").nextLong();
        }
        catch (NoSuchAlgorithmException ex){
            LOGGER.error("Error [0054]: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Checks if the publication platform port is not null and the user is logged in.
     * @return true if both requirements are met, otherwise false.
     */
    private boolean isPubPlatformAliveAndUserLoggedIn(){
        LOGGER.info("+++++ isPubPlatformAliveAndUserLoggedIn started +++++");
        
        Authorization port_authorizationPub = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();

        if (port_authorizationPub == null){
            LOGGER.debug("Publication service port is null");
            LOGGER.info("----- isPubPlatformAliveAndUserLoggedIn finished (001) -----");
            return false;
        }

        LoginRequestType request = new LoginRequestType();
        request.setLogin(new LoginType());
        request.getLogin().setSessionID(this.pubSessionId);
        
        try{
            CheckLoginResponse.Return response = port_authorizationPub.checkLogin(request);
            if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK)){
                LOGGER.info("----- isPubPlatformAliveAndUserLoggedIn finished (002) -----");
                return true;
            }
            else{
                LOGGER.debug("User not logged in");
                LOGGER.info("----- isPubPlatformAliveAndUserLoggedIn finished (003) -----");
                return false;
            }
        }
        catch (Exception ex){
            LOGGER.error("[0055]: " + ex.getLocalizedMessage());
            LOGGER.info("----- isPubPlatformAliveAndUserLoggedIn finished (004) -----");
            return false;
        }
    }

    /**
     * Checks whether or not the session-IDs are set.
     * @return true if they are set, otherwise false.
     */
    private boolean areSessionIdsSet()
    {
        LOGGER.info("+++++ areSessionIdsSet started +++++");
        if ((this.sessionId != null) && (this.pubSessionId != null)){
            LOGGER.info("----- areSessionIdsSet finished (001) -----");
            return true;
        }
        LOGGER.info("----- areSessionIdsSet finished (002) -----");
        return false;
    }

    /**
     * TODO
     * @param statusTo
     * @param proposedObject
     * @return 
     */
    private ReturnType transferTerminologyToPublicServer(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject proposedObject){
        LOGGER.info("+++++ transferTerminologyToPublicServer started +++++");
        ReturnType returnInfo = new ReturnType();
        returnInfo.setSuccess(false);

        try{
            if (!this.isPubPlatformAliveAndUserLoggedIn()){
                returnInfo.setSuccess(false);
                returnInfo.setMessage("Verbindung zur Publikationsplattform konnte nicht hergestellt werden.");
                LOGGER.debug("Pub-platform is unreachable or user is not logged in");
                LOGGER.info("----- transferTerminologyToPublicServer finished (001) -----");
                return returnInfo;
            }
            LOGGER.debug("Pub-platform is reachable and user is logged in");
            
            //Check if session IDs are set
            if(!this.areSessionIdsSet()){
                returnInfo.setSuccess(false);
                returnInfo.setMessage("SessionIDs sind nicht gesetzt.");
                LOGGER.info("Collaboration Session, must not be null, is: " + this.sessionId);
                LOGGER.info("Pub Session, must not be null, is: " + this.pubSessionId);
                LOGGER.info("----- transferTerminologyToPublicServer finished (002) -----");
                return returnInfo;
            }
            LOGGER.debug("SessionIDs are set");

            //Starting transfer
            if (proposedObject.getClassname().equals("CodeSystemVersion")){
                CodeSystem CStoExport = this.getCodeSystemToExport(proposedObject.getProposal().getVocabularyIdTwo(), proposedObject.getProposal().getVocabularyId());

                //Check if CS was found on collab plattform
                if ((CStoExport.getId() == null) || (CStoExport.getCodeSystemVersions().isEmpty())){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("CodeSystem für Transfer nicht gefunden. CS ID: "+ proposedObject.getProposal().getVocabularyIdTwo()
                            + ", CSV ID: " + proposedObject.getProposal().getVocabularyId()
                            + "CodeSystem: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (003) -----");
                    return returnInfo;
                }

                List<de.fhdo.terminologie.ws.searchPub.CodeSystem> CSpubList = this.getTargetCodeSystemFromPub(CStoExport.getName());
                boolean CSexistsOnPub = false;
                
                
                if (CSpubList != null && CSpubList.size() == 1){
                    //Check if name of found CS and CS to be exported are identical
                    if(CSpubList.get(0).getName().equals(CStoExport.getName())){
                        CSexistsOnPub = true;
                        this.targetCS = CSpubList.get(0);
                    }
                }
                else if ((CSpubList == null) || (CSpubList.isEmpty())){
                    //No codesystem found on pub
                    CSexistsOnPub = false;
                    this.targetCS = null;
                }
                else if (CSpubList.size() > 1){
                    //Checking if one of the pubCSs fits the exportCS
                    for (de.fhdo.terminologie.ws.searchPub.CodeSystem CS : CSpubList)
                        if (CS.getName().equals(CStoExport.getName())){
                            CSexistsOnPub = true;
                            this.targetCS = CS;
                            break;
                        }

                    if (!CSexistsOnPub){
                        //Multiple code systems found, user has to choose one
                        Map map = new HashMap();
                        map.put("targets", CSpubList);
                        map.put("source", CStoExport.getName());

                        Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                        ((SelectTargetPopup) win).setReleaseManager(this);

                        win.doModal();
                        win.setVisible(false);

                        if (this.targetCS != null)
                            CSexistsOnPub = true;
                    }
                }

                if (!CSexistsOnPub){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("CodeSystem konnte auf der Publikationsplattform nicht gefunden werden. (" + CStoExport.getName() + ")");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (004) -----");
                    return returnInfo;
                }
                else
                    LOGGER.info("CS (" + this.targetCS.getName() + ") found on pub, versions size: " + this.targetCS.getCodeSystemVersions().size());
                    
                ExportType exportedCodeSystem = getExportedCodeSystemFromCollab(CStoExport);

                if (exportedCodeSystem == null){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("CodeSystem konnte nicht exportiert werden. CSID: "
                            + proposedObject.getProposal().getVocabularyIdTwo()
                            + ", CSV ID: "
                            + proposedObject.getProposal().getVocabularyId()
                            + "CodeSystem: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (005) -----");
                    return returnInfo;
                }

                de.fhdo.terminologie.ws.administrationPub.ReturnType importReturn = this.importCodeSystem(CStoExport.getCodeSystemVersions().get(0), exportedCodeSystem);

                if (targetCS.getCodeSystemVersions() != null)
                    removeTempCodeSystemVersion();
                
                if (importReturn.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK)){
                    returnInfo.setSuccess(true);
                    returnInfo.setMessage(importReturn.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (006) -----");
                    return returnInfo;
                }
                else{
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage(importReturn.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (007) -----");
                    return returnInfo;
                }
            }
            else if (proposedObject.getClassname().equals("ValueSetVersion")){
                ValueSet VStoExport = this.getValueSetToExport(proposedObject.getProposal().getVocabularyName(), proposedObject.getProposal().getVocabularyId());

                //Check if VS was found on collab plattform
                if ((VStoExport.getId() == null) || (VStoExport.getValueSetVersions().isEmpty())){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("ValueSet für Transfer nicht gefunden.\nVSID: "
                            + proposedObject.getProposal().getVocabularyIdTwo()
                            + ",\nVSV ID: "
                            + proposedObject.getProposal().getVocabularyId()
                            + "\nValueSet: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (008) -----");
                    return returnInfo;
                }
        
                ArrayList<String> missingEntries = this.areValueSetContentsPresentOnPubPlattform(VStoExport);

                if (!missingEntries.isEmpty()){
                    returnInfo.setSuccess(false);
                    String message = "ValueSet kann nicht freigegeben werden, da benötigte CodeSystem(e) auf der Publikationsplattform fehlen.\n"
                            + "VSID: " + proposedObject.getProposal().getVocabularyIdTwo() + "\n"
                            + "VSV ID: " + proposedObject.getProposal().getVocabularyId() + "\n"
                            + "ValueSet: " + proposedObject.getProposal().getVocabularyName() + "\n"
                            + "Fehlende Codes: " + missingEntries.size() + "\n";
                    
                    int maxMissingShownCounter = 0;
                    for(String entry : missingEntries){
                        message += entry+"\n";
                        maxMissingShownCounter++;
                        if(maxMissingShownCounter == 10)
                            break;
                    }
                    
                    returnInfo.setMessage(message);
                    LOGGER.info("----- transferTerminologyToPublicServer finished (009) -----");
                    return returnInfo;
                }

                List<de.fhdo.terminologie.ws.searchPub.ValueSet> result = getTargetValueSetFromPub(VStoExport.getName());

                if ((result != null) && (result.size() == 1)){
                    //Check if name of found CS and CS to be exported are identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet VS : result)
                        if (VS.getName().equals(VStoExport.getName())){
                            this.targetVS = result.get(0);
                            break;
                        }
                }
                else if (result == null || result.isEmpty()){
                    //No CS found
                    this.targetVS = null;
                }
                else if (result.size() > 1){
                    //checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet VS : result)
                        if (VS.getName().equals(VStoExport.getName())){
                            this.targetVS = VS;
                        }

                    if (this.targetVS == null){
                        //User has to choose one from multiple
                        Map map = new HashMap();
                        map.put("targets", result);
                        map.put("source", VStoExport.getName());

                        Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                        ((SelectTargetPopup) win).setReleaseManager(this);

                        win.doModal();
                        win.setVisible(false);
                    }
                }

                if (this.targetVS == null){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("ValueSet konnte auf der Publikationsplattform nicht gefunden werden. (" + VStoExport.getName() + ")");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (010) -----");
                    return returnInfo;
                }

                ExportType exportedValueSet = this.getExportedValueSet(VStoExport);

                if (exportedValueSet == null){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("ValueSet konnte nicht exportiert werden.\nVSID: "
                        + proposedObject.getProposal().getVocabularyIdTwo()
                        + ",\n VSV ID: "
                        + proposedObject.getProposal().getVocabularyId()
                        + "\nValueSet: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (011) -----");
                    return returnInfo;
                }

                de.fhdo.terminologie.ws.administrationPub.ReturnType ret_import = this.importValueSet(VStoExport.getValueSetVersions().get(0), exportedValueSet);

                if (this.targetVS.getValueSetVersions() != null)
                    this.removeTempValueSetVersion();

                if (ret_import.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK)){
                    returnInfo.setSuccess(true);
                    returnInfo.setMessage(ret_import.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (012) -----");
                    return returnInfo;
                }
                else{
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage(ret_import.getMessage());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (013) -----");
                    return returnInfo;
                }
            }
            else if (proposedObject.getClassname().equals("CodeSystem")){
                CodeSystem csToExport = this.getCodeSystemToExport(proposedObject.getProposal().getVocabularyIdTwo(), null);

                //Check if CS was found on collab plattform
                if ((csToExport.getId() == null) || (csToExport.getCodeSystemVersions().isEmpty())){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("CodeSystem für Transfer nicht gefunden.\nCSID: "
                        + proposedObject.getProposal().getVocabularyIdTwo()
                        + ",\n CSV ID: "
                        + proposedObject.getProposal().getVocabularyId()
                        + "\nCodeSystem: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (014) -----");
                    return returnInfo;
                }

                List<de.fhdo.terminologie.ws.searchPub.CodeSystem> CSlist = this.getTargetCodeSystemFromPub(csToExport.getName());

                
                if (CSlist != null && CSlist.size() == 1) //Check if name of found CS and CS to be exported are identical
                    if (CSlist.get(0).getName().equals(csToExport.getName()))
                        this.targetCS = CSlist.get(0);
                else if (CSlist.isEmpty()) //No CS found
                    this.targetCS = null;
                else if (CSlist.size() > 1) //Checks if the CS name is identical
                    for (de.fhdo.terminologie.ws.searchPub.CodeSystem CS : CSlist)
                        if (CS.getName().equals(csToExport.getName())){
                            this.targetCS = CS;
                            break;
                        }
                
                if (this.targetCS == null){
                    this.createTempCodeSystemVersionOnPub(csToExport);
                    //Target CS has to be set
                    if (this.targetCS != null){
                        returnInfo.setSuccess(true);
                        returnInfo.setMessage("CodeSystem successfully created.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (015) -----");
                        return returnInfo;
                    }
                    else{
                        returnInfo.setSuccess(false);
                        returnInfo.setMessage("CodeSystem konnte auf Publikationsumgebung nicht erstellt werden.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (016) -----");
                        return returnInfo;
                    }
                }
                else{
                    returnInfo.setSuccess(true);
                    returnInfo.setMessage("CodeSystem already exists.");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (017) -----");
                    return returnInfo;
                }
            }
            else if (proposedObject.getClassname().equals("ValueSet")){
                ValueSet vsToExport = this.getValueSetToExport(proposedObject.getProposal().getVocabularyName(), null);

                //Check if VS was found on collab plattform
                if (vsToExport.getId() == null || vsToExport.getValueSetVersions().isEmpty()){
                    returnInfo.setSuccess(false);
                    returnInfo.setMessage("ValueSet für Transfer nicht gefunden. VSID: "
                        + proposedObject.getProposal().getVocabularyIdTwo()
                        + ", VSV ID: "
                        + proposedObject.getProposal().getVocabularyId()
                        + "ValueSet: " + proposedObject.getProposal().getVocabularyName());
                    LOGGER.info("----- transferTerminologyToPublicServer finished (018) -----");
                    return returnInfo;
                }

                List<de.fhdo.terminologie.ws.searchPub.ValueSet> VSlist = this.getTargetValueSetFromPub(vsToExport.getName());

                if (VSlist != null && VSlist.size() == 1){ //Check if name of found VS and VS to be exported are identical
                    if(VSlist.get(0).getName().equals(vsToExport.getName()))
                        this.targetVS = VSlist.get(0);
                }
                else if (VSlist == null || VSlist.isEmpty()) //No VS found
                    this.targetVS = null;
                else if (VSlist.size() > 1) //Multiple VS found
                    for (de.fhdo.terminologie.ws.searchPub.ValueSet VS : VSlist)
                        if (VS.getName().equals(vsToExport.getName())){
                            this.targetVS = VS;
                            break;
                        }

                if (this.targetVS == null){
                    this.createTempValueSetVersionOnPub(vsToExport);
                    //Target VS has to be set                    
                    if (this.targetVS != null){
                        returnInfo.setSuccess(true);
                        returnInfo.setMessage("ValueSet successfully created.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (019) -----");
                        return returnInfo;
                    }
                    else{
                        returnInfo.setSuccess(false);
                        returnInfo.setMessage("ValueSet konnte auf Publikationsumgebung nicht erstellt werden.");
                        LOGGER.info("----- transferTerminologyToPublicServer finished (020) -----");
                        return returnInfo;
                    }
                }
                else{
                    returnInfo.setSuccess(true);
                    returnInfo.setMessage("ValueSet already exists.");
                    LOGGER.info("----- transferTerminologyToPublicServer finished (021) -----");
                    return returnInfo;
                }
            }
        }
        catch (ServerSOAPFaultException ex){
            LOGGER.error("Error [0099]: " + ex.getLocalizedMessage());
            returnInfo.setSuccess(false);
            returnInfo.setMessage("Die Antwort des Servers ist fehlerhaft.");
        }
        catch (Exception ex){
            LOGGER.error("Error [0101]: " + ex.getLocalizedMessage());
            returnInfo.setSuccess(false);
            returnInfo.setMessage("An Error occured.");
        }
        LOGGER.info("----- transferTerminologyToPublicServer finished (022) -----");
        return returnInfo;
    }

    /**
     * searches for the CodeSystem which has to be exportedon the collaboration
     * plattform
     *
     * @param codesystemId
     * @param codesystemVersionId
     * @return
     * @throws ServerSOAPFaultException
     */
    private CodeSystem getCodeSystemToExport(Long codesystemId, Long codesystemVersionId) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getCodeSystemToExport started +++++");
        LOGGER.info("Trying to find code system and version in collab plattform, CS-ID=" + codesystemId + ", CSV-ID=" + codesystemVersionId);

        de.fhdo.terminologie.ws.search.Search searchPort = WebServiceUrlHelper.getInstance().getSearchServicePort();
        de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType searchRequest = new de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType();
        searchRequest.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        searchRequest.getLogin().setSessionID(this.sessionId);
        searchRequest.setCodeSystem(new CodeSystem());
        searchRequest.getCodeSystem().setId(codesystemId);
        
        de.fhdo.terminologie.ws.search.ListCodeSystemsResponse.Return resp = searchPort.listCodeSystems(searchRequest);
        
        if ((resp.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
        && (resp.getCodeSystem() != null)
        && (resp.getCodeSystem().size() == 1)){
            if (codesystemVersionId != null){
                CodeSystemVersion CSVsearch = null;
                for (CodeSystemVersion CSVbuffer : resp.getCodeSystem().get(0).getCodeSystemVersions())
                    if (CSVbuffer.getVersionId().equals(codesystemVersionId)){
                        CSVsearch = CSVbuffer;
                        break;
                    }
                
                if (CSVsearch != null){
                    CodeSystem CS = resp.getCodeSystem().get(0);
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
                return resp.getCodeSystem().get(0);
            }
        }
        else{
            LOGGER.info("----- getCodeSystemToExport finished (004) -----");
            return new CodeSystem();
        }
    }

    /**
     * Exports the requested CS from the collab plattform.
     * If a LOINC-file is requested, the file from the LoincCsvPath in the database will be exported instead
     * since the webservice does not support the LOINF-file-format.
     * @param codesystem the CS to be exported.
     * @return TODO
     * @throws ServerSOAPFaultException 
     */
    private ExportType getExportedCodeSystemFromCollab(CodeSystem codesystem) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getExportedCodeSystemFromCollab started +++++");
        Long codesystemID = codesystem.getId();
        Long codesystemVersionID = codesystem.getCodeSystemVersions().get(0).getVersionId();
        
        LOGGER.info("CS ID: " + codesystemID);
        LOGGER.info("CSV ID: " + codesystemVersionID);

        ExportCodeSystemContentRequestType requestExportCS = new ExportCodeSystemContentRequestType();
        requestExportCS.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        requestExportCS.getLogin().setSessionID(this.sessionId);
        requestExportCS.setCodeSystem(new CodeSystem());
        requestExportCS.getCodeSystem().setId(codesystemID);
        CodeSystemVersion CSV = new CodeSystemVersion();
        CSV.setVersionId(codesystemVersionID);
        requestExportCS.getCodeSystem().getCodeSystemVersions().add(CSV);

        //Export Type TODO 193L durch statischen wert ersetzen
        ExportType exportType = new ExportType();
        exportType.setFormatId(193L);
        exportType.setUpdateCheck(false);
        requestExportCS.setExportInfos(exportType);

        //Optional ExportParameter
        ExportParameterType exportParameterType = new ExportParameterType();
        exportParameterType.setAssociationInfos("");
        exportParameterType.setCodeSystemInfos(true);
        exportParameterType.setTranslations(true);
        requestExportCS.setExportParameter(exportParameterType);

        Return response = new Return();

        
        if (codesystem.getName().contains("LOINC")){
            //LOINC is not exported by the webservice, instead the file from the path is loaded
            String path = DBSysParam.instance().getStringValue("LoincCsvPath", null, null);
            File file = new File(path);
            FileInputStream fileInputStream = null;
            try{
                fileInputStream = new FileInputStream(file);
                byte bytesLOINC[] = new byte[(int) file.length()];
                fileInputStream.read(bytesLOINC);
                response.setExportInfos(new ExportType());
                response.getExportInfos().setFilecontent(bytesLOINC);
                response.getExportInfos().setFormatId(200l);
                response.setReturnInfos(new de.fhdo.terminologie.ws.administration.ReturnType());
                response.getReturnInfos().setStatus(de.fhdo.terminologie.ws.administration.Status.OK);
            }
            catch (FileNotFoundException ex){
                LOGGER.error("Error [0056]: " + ex.getLocalizedMessage());
            }
            catch (IOException ex){
                LOGGER.error("Error [0057]: " + ex.getLocalizedMessage());
            }
            finally{
                if (fileInputStream != null){
                    try{
                        fileInputStream.close();
                    }
                    catch (IOException ex){
                        LOGGER.error("Error [0058]: " + ex.getLocalizedMessage());
                    }
                }
            }
        }
        else
            response = WebServiceHelper.exportCodeSystemContent(requestExportCS);
        
        LOGGER.info("----- getExportedCodeSystem finished (001) -----");
        return response.getExportInfos();
    }

    
    //3.2.21
    //private de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPubThread;
    /**
     * TODO
     * */
    private List<de.fhdo.terminologie.ws.searchPub.CodeSystem> getTargetCodeSystemFromPub(String codesystemName) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getTargetCodeSystemFromPub started +++++");
        final de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        //3.2.21
        //de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
        de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPubThread = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
        request_searchPubThread.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        request_searchPubThread.getLogin().setSessionID(this.pubSessionId);
        request_searchPubThread.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
        request_searchPubThread.getCodeSystem().setName(codesystemName);
        
        //de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub = port_searchPub.listCodeSystems(request_searchPub);
        //de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub;
        de.fhdo.terminologie.ws.searchPub.GetListCodeSystemsPubReturnResponse.Return respSearchPub = null;
        
        //3.2.21 start
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    port_searchPub.listCodeSystemsPub(request_searchPubThread);
                }
            };
            thread.start();
            
            boolean searchRunning = true;
            int counter = 1;
            while(searchRunning){
                Thread.sleep(100);
                searchRunning = port_searchPub.checkListCodeSystemsPubRunning();
                if(counter%50 == 0)
                    LOGGER.info("Pub-listCodeSystems running for " + counter/10 + " seconds");
                counter++;
            }
            respSearchPub = port_searchPub.getListCodeSystemsPubReturn();
        }
        catch(Exception e){ 
            LOGGER.info("Error [0089]: " + e.getLocalizedMessage());
        }
        //3.2.21 end

        if (respSearchPub!=null
        && respSearchPub.getReturnInfos()!=null 
        && respSearchPub.getReturnInfos().getStatus()!=null 
        && respSearchPub.getReturnInfos().getStatus().equals(Status.OK)){
            LOGGER.info("----- getTargetCodeSystemFromPub finished (001) -----");
            return respSearchPub.getCodeSystem();
        }
        else{
            LOGGER.info("----- getTargetCodeSystemFromPub finished (002) -----");
            return new ArrayList<>();
        }
    }

    private de.fhdo.terminologie.ws.administrationPub.ReturnType importCodeSystem(CodeSystemVersion csv, ExportType exportedCs) throws ServerSOAPFaultException{
        LOGGER.info("+++++ importCodeSystem started +++++");
        //de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        
        // Login
        de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);
        
        // Codesystem
        request.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
        request.getCodeSystem().setId(this.targetCS.getId());

        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        if (this.targetCS.getName().contains("LOINC")){
            request.getImportInfos().setOrder(true);
            request.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else
            request.getImportInfos().setRole(CODES.ROLE_TRANSFER);

        de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion CSVpub = new de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion();
        CSVpub.setName(csv.getName());
        request.getCodeSystem().getCodeSystemVersions().add(CSVpub);
        request.getImportInfos().setFormatId(exportedCs.getFormatId());
        request.getCodeSystem().setName(targetCS.getName());
        request.getImportInfos().setFilecontent(exportedCs.getFilecontent());
        request.setImportId(importId);

        //3.2.20 next line
        //de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return ret_import = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return();
        //3.2.20 added try catch and thread
        
        de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType requestThread = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
        requestThread.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        requestThread.getLogin().setSessionID(this.pubSessionId);
        requestThread.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
        requestThread.getCodeSystem().setId(this.targetCS.getId());

        requestThread.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        if (this.targetCS.getName().contains("LOINC")){
            requestThread.getImportInfos().setOrder(true);
            requestThread.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else
            requestThread.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        
        requestThread.getCodeSystem().getCodeSystemVersions().add(CSVpub);
        requestThread.getImportInfos().setFormatId(exportedCs.getFormatId());
        requestThread.getCodeSystem().setName(this.targetCS.getName());
        requestThread.getImportInfos().setFilecontent(exportedCs.getFilecontent());
        requestThread.setImportId(this.importId);
        
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
            int counter = 1;
            while(importRunning){
                Thread.sleep(100);
                importRunning = importPort.checkImportRunning();
                if(counter%50 == 0)
                    LOGGER.info("Pub-importCodeSystem running for " + counter*5 + " seconds");
                counter++;
            }
            ret_importThread = importPort.getPubImportResponse();
        }
        catch(Exception e){
            LOGGER.error("Error [0074]: " + e.getLocalizedMessage());
        }
        if(ret_importThread!=null){
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
        final de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

        de.fhdo.terminologie.ws.authoringPub.CodeSystem CStemp = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
        CStemp.setName(CS.getName());
        CStemp.setDescription(CS.getDescription());
        CStemp.setDescriptionEng(CS.getDescriptionEng());
        CStemp.setIncompleteCS(CS.isIncompleteCS());
        CStemp.setResponsibleOrganization(CS.getResponsibleOrganization());
        CStemp.setWebsite(CS.getWebsite());
        CStemp.setAutoRelease(CS.isAutoRelease());
        CStemp.setCodeSystemType(CS.getCodeSystemType());
        de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion CSVtemp = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
        CSVtemp.setName("Temp_import");
        CStemp.getCodeSystemVersions().add(CSVtemp);
        request.setCodeSystem(CStemp);
        
        //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();

        //de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemResponse.Return ret_pub = port.createCodeSystem(request);
        
        //3.2.21 start
        //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);

        final de.fhdo.terminologie.ws.authoringPub.Authoring authoringPort = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        GetCreateCodeSystemPubResponseResponse.Return ret_pub = null;
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    authoringPort.createCodeSystemPub(request);
                }
            };
            thread.start();

            boolean createRunning = true;
            int counter = 1;
            while(createRunning){
                Thread.sleep(100);
                createRunning = authoringPort.getCreateCodeSystemPubRunning();
                if(counter%50==0)
                    LOGGER.info("Pub-createCodeSystem running for " + counter/10 + " seconds");
                counter++;
            }
            ret_pub = authoringPort.getCreateCodeSystemPubResponse();
        }
        catch(Exception e){
            LOGGER.error("Error [0096]: " + e.getLocalizedMessage());
        }
        //3.2.21 end
        
        if (ret_pub!=null && ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK)){
            targetCS = new de.fhdo.terminologie.ws.searchPub.CodeSystem();
            targetCS.setId(ret_pub.getCodeSystem().getId());
        }
        LOGGER.info("----- createTempCodeSystemVersionOnPub finished (001) -----");
    }

    private void removeTempCodeSystemVersion(){
        LOGGER.info("+++++ removeTempCodeSystemVersion started +++++");
        for(de.fhdo.terminologie.ws.searchPub.CodeSystemVersion CSversion : targetCS.getCodeSystemVersions()){
            
            if (CSversion.getName().contains("Temp_import")){
                final de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType removeRequest = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                removeRequest.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                removeRequest.getLogin().setSessionID(this.pubSessionId);

                removeRequest.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.CodeSystem CSremove = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
                CSremove.setId(this.targetCS.getId());
                de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion CSversionRemove = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
                CSversionRemove.setVersionId(CSversion.getVersionId());
                CSremove.getCodeSystemVersions().add(CSversionRemove);

                removeRequest.getDeleteInfo().setCodeSystem(CSremove);
                removeRequest.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.CODE_SYSTEM_VERSION);

                //3.2.21 start
                //de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                //de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);
                
                final Authoring removeTMPport = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                RemoveTerminologyOrConceptResponseType resp_remove = null;
                try{
                    Thread thread = new Thread(){
                      @Override
                      public void run(){
                          removeTMPport.removeTerminologyOrConcept(removeRequest);
                      }
                    };
                    thread.start();

                    boolean removalRunning = true;
                    int counter = 1;
                    while(removalRunning){
                        Thread.sleep(100);
                        removalRunning = removeTMPport.getRemoveTerminologyOrConceptPubRunning();
                        if(counter%50==0)
                            LOGGER.info("Pub-removeTerminologyOrConcept running for " + counter*5 + " seconds");
                        counter++;
                    }
                    resp_remove = removeTMPport.getRemoveTerminologyOrConceptPubResponse();
                }
                catch(Exception e){
                    LOGGER.error("Error [0075]: " + e.getLocalizedMessage());
                }
                //3.2.21 end
                
                if (resp_remove!=null && resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                    LOGGER.info("TermBrowser: Temp Version erfolgreich entfernt.");
                else
                    LOGGER.error("TermBrowser: Löschen der Version fehlgeschlagen. ");
            }
            else
                LOGGER.info("dabaca csv not removed, not temporary: " + CSversion.getName());
        }
        LOGGER.info("----- removeTempCodeSystemVersion finished (001) -----");
    }

    private void removeTempValueSetVersion()
    {
        LOGGER.info("+++++ removeTempValueSetVersion started +++++");
        for (de.fhdo.terminologie.ws.searchPub.ValueSetVersion VSversion : targetVS.getValueSetVersions()){
            if (VSversion.getName().contains("Temp_import")){
                final de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType removeRequest = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                removeRequest.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                removeRequest.getLogin().setSessionID(this.pubSessionId);

                removeRequest.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.ValueSet VStoRemove = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                VStoRemove.setId(targetVS.getId());
                de.fhdo.terminologie.ws.authoringPub.ValueSetVersion VSVtoRemove = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
                VSVtoRemove.setVersionId(VSversion.getVersionId());
                VStoRemove.getValueSetVersions().add(VSVtoRemove);

                removeRequest.getDeleteInfo().setValueSet(VStoRemove);
                removeRequest.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.VALUE_SET_VERSION);

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
                            removePort.removeTerminologyOrConcept(removeRequest);
                        }
                    };
                    thread.start();

                    boolean removalRunning = true;
                    int counter = 1;
                    while(removalRunning){
                        Thread.sleep(100);
                        removalRunning = removePort.getRemoveTerminologyOrConceptPubRunning();
                        if(counter%50 == 0)
                            LOGGER.info("Pub-removeTerminologyOrConcept running for " + counter/10 + " seconds");
                        counter++;
                    }
                    resp_remove = removePort.getRemoveTerminologyOrConceptPubResponse();
                }
                catch(Exception e){
                    LOGGER.error("Error [0086]: " + e.getLocalizedMessage());
                }
                //3.2.21 end
                
                if (resp_remove!=null && resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                    LOGGER.info("TempVersion removed successfully");
                else
                    LOGGER.error("Error [0102]: TempVersion removal failed");
            }
        }
        LOGGER.info("----- removeTempValueSetVersion finished (001) -----");
    }

    private ValueSet getValueSetToExport(String valuesetName, Long valuesetVersionId) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getValueSetToExport started +++++");
        LOGGER.info("Trying to find ValueSet and Version in collab plattform: VS-name = " + valuesetName + " VSV-ID = " + valuesetVersionId);

        ListValueSetsRequestType requestValueSet = new ListValueSetsRequestType();
        requestValueSet.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        requestValueSet.getLogin().setSessionID(this.sessionId);

        ValueSet searchVS = new ValueSet();
        searchVS.setName(valuesetName);
        requestValueSet.setValueSet(searchVS);

        de.fhdo.terminologie.ws.search.Search searchPort = WebServiceUrlHelper.getInstance().getSearchServicePort();

        ListValueSetsResponse.Return returnValueSet = searchPort.listValueSets(requestValueSet);

        if ((returnValueSet.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
        && (returnValueSet.getValueSet() != null)
        && (returnValueSet.getValueSet().size() == 1)){
            if (valuesetVersionId != null){
                ValueSetVersion VSVsearch = null;
                for (ValueSetVersion VSVbuffer : returnValueSet.getValueSet().get(0).getValueSetVersions())
                    if (VSVbuffer.getVersionId().equals(valuesetVersionId))
                        VSVsearch = VSVbuffer;

                if (VSVsearch != null){
                    ValueSet VS = returnValueSet.getValueSet().get(0);
                    VS.getValueSetVersions().clear();
                    VS.getValueSetVersions().add(VSVsearch);
                    LOGGER.info("----- getValueSetToExport finished (001) -----");
                    return VS;
                }
                else{
                    LOGGER.info("----- getValueSetToExport finished (002) -----");
                    return new ValueSet();
                }
            }
            else{
                LOGGER.info("----- getValueSetToExport finished (003) -----");
                return returnValueSet.getValueSet().get(0);
            }
        }
        else if(returnValueSet.getValueSet().size() > 1){
            for(ValueSet VSbuffer : returnValueSet.getValueSet()){
                if(VSbuffer.getName().equals(valuesetName)){
                    if (valuesetVersionId != null){
                        ValueSetVersion VSVsearch = null;
                        for (ValueSetVersion VSVbuffer : VSbuffer.getValueSetVersions())
                            if (VSVbuffer.getVersionId().equals(valuesetVersionId))
                                VSVsearch = VSVbuffer;

                        if (VSVsearch != null){
                            ValueSet VS = VSbuffer;
                            VS.getValueSetVersions().clear();
                            VS.getValueSetVersions().add(VSVsearch);
                            LOGGER.info("----- getValueSetToExport finished (004) -----");
                            return VS;
                        }
                        else{
                            LOGGER.info("----- getValueSetToExport finished (005) -----");
                            return new ValueSet();
                        }
                    }
                    else{
                        LOGGER.info("----- getValueSetToExport finished (006) -----");
                        return returnValueSet.getValueSet().get(0);
                    }
                }
            }
        }
        else{
            LOGGER.info("----- getValueSetToExport finished (007) -----");
            return new ValueSet();
        }
        
        LOGGER.info("----- getValueSetToExport finished (008) -----");
        return new ValueSet();
    }

    /**
     * 
     * @param valueSet
     * @return
     * @throws ServerSOAPFaultException 
     */
    private ExportType getExportedValueSet(ValueSet valueSet) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getExportedValueSet started +++++");
        
        Long valuesetId = valueSet.getId();
        Long valueSetVersionId = valueSet.getValueSetVersions().get(0).getVersionId();

        LOGGER.info("Trying to export value set and version from collab plattform, VS-ID = " + valuesetId + ", VSV-ID = " + valueSetVersionId);

        ExportValueSetContentRequestType exportVSRequest = new ExportValueSetContentRequestType();

        // Login
        exportVSRequest.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        exportVSRequest.getLogin().setSessionID(this.pubSessionId);
        exportVSRequest.setValueSet(new ValueSet());
        exportVSRequest.getValueSet().setId(valuesetId);

        ValueSetVersion VSV = new ValueSetVersion();
        VSV.setVersionId(valueSetVersionId);
        exportVSRequest.getValueSet().getValueSetVersions().add(VSV);

        // Export Type TODO formatID durch statischen wert ersetzen 
        ExportType eType = new ExportType();
        eType.setFormatId(195L);
        eType.setUpdateCheck(false);
        exportVSRequest.setExportInfos(eType);

        // Optional: export parameter
        ExportParameterType exportParameterType = new ExportParameterType();
        exportParameterType.setAssociationInfos("");
        exportParameterType.setCodeSystemInfos(false);
        exportParameterType.setTranslations(false);
        exportVSRequest.setExportParameter(exportParameterType);
        
        // Webservice call
        ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(exportVSRequest);

        LOGGER.info("----- getExportedValueSet finished (001) -----");
        return response.getExportInfos();
    }

    /**
     * Checks if the ValueSet Codes are already present in code systems on the
     * Pub Plattform
     *
     * @param valueSet
     * @return
     */
    private ArrayList<String> areValueSetContentsPresentOnPubPlattform(ValueSet valueSet){
        LOGGER.info("+++++ areValueSetContentsPresentOnPubPlattform started +++++");
        ArrayList<String> missingEntries = new ArrayList<>();
        
        ListValueSetContentsRequestType requestVScontent = new ListValueSetContentsRequestType();
        requestVScontent.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        requestVScontent.getLogin().setSessionID(this.sessionId);

        requestVScontent.setValueSet(valueSet);
        requestVScontent.setReadMetadataLevel(false);

        Search searchPort = WebServiceUrlHelper.getInstance().getSearchServicePort();
        
        ListValueSetContentsResponse.Return responseVScontent = searchPort.listValueSetContents(requestVScontent);

        if (responseVScontent.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK)){
            Iterator<CodeSystemEntity> VScontentIterator = responseVScontent.getCodeSystemEntity().iterator();
            final de.fhdo.terminologie.ws.searchPub.Search portSearchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            do{
                CodeSystemEntity CSentity = VScontentIterator.next();
                final ListGloballySearchedConceptsRequestType parameter = new ListGloballySearchedConceptsRequestType();

                //CS global search
                parameter.setCodeSystemConceptSearch(true);

                parameter.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                parameter.getLogin().setSessionID(this.pubSessionId);

                parameter.setCode(CSentity.getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCode());
                parameter.setTerm("");
                
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
                    int counter = 1;
                    while(listRunning){
                        Thread.sleep(100);
                        listRunning = portSearchPub.isListGloballySearchedConceptsRunning();
                        if(counter%50 == 0)
                            LOGGER.info("Pub-listGloballySearchedConcepts running for " + counter*5 + " seconds");
                        counter++;
                    }
                    response = portSearchPub.getListGloballySearchedConceptsResponse();
                }
                catch(Exception e){
                    LOGGER.error("Error [0079]: " + e.getLocalizedMessage());
                }
                //3.2.21 end
                                
                if (response!=null && response.getReturnInfos().getStatus().equals(Status.OK))
                    if (response.getGlobalSearchResultEntry().isEmpty()){
                        missingEntries.add(parameter.getCode());
                        LOGGER.info("Code" + parameter.getCode() + " not found.");
                    }
                else
                    missingEntries.add(parameter.getCode());
            }
            while (VScontentIterator.hasNext());
        }
        LOGGER.info("----- areValueSetContentsPresentOnPubPlatform finished (001) -----");
        return missingEntries;
    }

    private List<de.fhdo.terminologie.ws.searchPub.ValueSet> getTargetValueSetFromPub(String valuesetName) throws ServerSOAPFaultException{
        LOGGER.info("+++++ getTargetValueSetFromPub started +++++");
        final de.fhdo.terminologie.ws.searchPub.Search searchPortPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        final de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType searchRequestPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
        
        searchRequestPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        searchRequestPub.getLogin().setSessionID(this.pubSessionId);
        searchRequestPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
        searchRequestPub.getValueSet().setName(valuesetName);
        
        //de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);
        GetListValueSetsPubResponeResponse.Return respSearchPub = null;
        //3.2.21 start
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    searchPortPub.listValueSetsPub(searchRequestPub);
                }
            };
            thread.start();

            boolean listRunning = true;
            int counter = 1;
            while(listRunning){
                Thread.sleep(100);
                listRunning = searchPortPub.isListValueSetsPubRunning();
                if(counter%50 == 0)
                    LOGGER.info("Pub-listValueSets running for " + counter/10 + " seconds");
                counter++;
            }
            respSearchPub = searchPortPub.getListValueSetsPubRespone();
        }
        catch(Exception e){
            LOGGER.error("Error [0080]: " + e.getLocalizedMessage());
        }
        //3.2.21 end
        
        if (respSearchPub!=null && respSearchPub.getReturnInfos().getStatus().equals(Status.OK)){
            LOGGER.info("----- getTargetValueSetFromPub finished (001) -----");
            return respSearchPub.getValueSet();
        }
        else{
            LOGGER.info("----- getTargetValueSetFromPub finished (002) -----");
            return new ArrayList<>();
        }
    }

    /**
     * TODO
     * @param vsv
     * @param exportedVs
     * @return
     * @throws ServerSOAPFaultException 
     */
    private de.fhdo.terminologie.ws.administrationPub.ReturnType importValueSet(ValueSetVersion vsv, ExportType exportedVs) throws ServerSOAPFaultException{
        LOGGER.info("+++++ importValueSet started +++++");
        final de.fhdo.terminologie.ws.administrationPub.Administration administrationPort = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        
        //Login
        final de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

        //Value set
        request.setValueSet(new de.fhdo.terminologie.ws.administrationPub.ValueSet());
        request.getValueSet().setId(this.targetVS.getId());

        //TODO export format durch statischen wert ersetzen
        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        request.getImportInfos().setFormatId(301l); //export formatId != import formatId; 195 != 301
        request.getImportInfos().setFilecontent(exportedVs.getFilecontent());
        request.getImportInfos().setOrder(Boolean.TRUE);
        request.setImportId(this.importId);

        de.fhdo.terminologie.ws.administrationPub.ValueSetVersion VSVpub = new de.fhdo.terminologie.ws.administrationPub.ValueSetVersion();
        VSVpub.setName(vsv.getName());
        request.getValueSet().getValueSetVersions().add(VSVpub);
        
        //de.fhdo.terminologie.ws.administrationPub.ImportValueSetResponse.Return ret_import = port.importValueSet(request);
        //3.2.21 start
        GetImportValueSetPubResponseResponse.Return response = null;
        try{
            Thread thread = new Thread(){
                @Override
                public void run(){
                    administrationPort.importValueSetPub(request);
                }
            };
            thread.start();

            boolean importRunning = true;
            int counter = 1;
            while(importRunning){
                Thread.sleep(100);
                importRunning = administrationPort.isImportValueSetPubRunning();
                if(counter%50 == 0)
                    LOGGER.info("Pub-importValueSet running for " + counter/10 + " seconds");
                counter++;
            }
            response = administrationPort.getImportValueSetPubResponse();
        }
        catch(Exception e){
            LOGGER.error("Error [0083]: " + e.getLocalizedMessage());
        }
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

    private void createTempValueSetVersionOnPub(ValueSet VS){
        LOGGER.info("+++++ createTempValueSetVersionOnPub started +++++");
        
        final de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

        de.fhdo.terminologie.ws.authoringPub.ValueSet VStemp = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
        VStemp.setName(VS.getName());
        VStemp.setDescription(VS.getDescription());
        VStemp.setDescriptionEng(VS.getDescriptionEng());
        VStemp.setResponsibleOrganization(VS.getResponsibleOrganization());
        VStemp.setWebsite(VS.getWebsite());
        VStemp.setAutoRelease(VS.isAutoRelease());

        de.fhdo.terminologie.ws.authoringPub.ValueSetVersion VSVtemp = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
        VSVtemp.setName("Temp_import");
        VStemp.getValueSetVersions().add(VSVtemp);
        
        request.setValueSet(VStemp);

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
            int counter = 1;
            while(importRunning){
                Thread.sleep(100);
                importRunning = port.isCreateValueSetPubRunning();
                if(counter%50 == 0)
                    LOGGER.info("Pub-createValueSet running for " + counter/10 + " seconds");
                counter++;
            }
            ret_pub = port.getCreateValueSetPubResponse();

        }
        catch(Exception e){
            LOGGER.error("Error [0095]: " + e.getLocalizedMessage());
        }
        //3.2.21 end     
        
        if (ret_pub!=null && ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK)){
            this.targetVS = new de.fhdo.terminologie.ws.searchPub.ValueSet();
            this.targetVS.setId(ret_pub.getValueSet().getId());
        }
        LOGGER.info("----- createTempValueSetVersionOnPub finished (001) -----");
    }
    
    public ReturnType initTransfer(Set<Proposalobject> proposalObjects, Statusrel rel)
    {
        LOGGER.info("+++++ initTransfer started +++++");
        ReturnType transfer_success = new ReturnType();
        transfer_success.setSuccess(false);

        try
        {
            if(proposalObjects.isEmpty())
            {
                transfer_success.setMessage("Error: Empty set of Proposalobjects.");
                LOGGER.info("----- initTransfer finished (001) -----");
                return transfer_success;
            }
            
            List<Proposalobject> terminologies = new ArrayList<Proposalobject>();
            List<Proposalobject> codesystems = new ArrayList<Proposalobject>();
            List<Proposalobject> valuesets = new ArrayList<Proposalobject>();
            List<Proposalobject> codesystemVersions = new ArrayList<Proposalobject>();
            List<Proposalobject> valuesetVersions = new ArrayList<Proposalobject>();
            List<Proposalobject> codesystemConcepts = new ArrayList<Proposalobject>();
            List<Proposalobject> valuesetConcepts = new ArrayList<Proposalobject>();
            List<Proposalobject> other = new ArrayList<Proposalobject>();
            for (Proposalobject po : proposalObjects)
            {
                if (po.getClassname().equals("CodeSystem"))
                {
                    codesystems.add(po);
                }
                else if (po.getClassname().equals("ValueSet"))
                {
                    valuesets.add(po);
                }
                else if (po.getClassname().equals("CodeSystemVersion"))
                {
                    codesystemVersions.add(po);
                }
                else if (po.getClassname().equals("ValueSetVersion"))
                {
                    valuesetVersions.add(po);
                }
                else if (po.getClassname().equals("CodeSystemConcept"))
                {
                    codesystemConcepts.add(po);
                }
                else if (po.getClassname().equals("ConceptValueSetMembership"))
                {
                    valuesetConcepts.add(po);
                }
                else
                {
                    other.add(po);
                }
            }

            terminologies.addAll(terminologies.size(), codesystems);
            terminologies.addAll(terminologies.size(), valuesets);
            terminologies.addAll(terminologies.size(), codesystemVersions);
            terminologies.addAll(terminologies.size(), valuesetVersions);
            terminologies.addAll(terminologies.size(), codesystemConcepts);
            terminologies.addAll(terminologies.size(), valuesetConcepts);
            terminologies.addAll(terminologies.size(), other);

            LOGGER.info("DABACA terminologies size: " + terminologies.size());
            for (Proposalobject po : terminologies)
            {
                LOGGER.info("DABACA terminology to be transferred = " + po.getName());
                transfer_success = this.transferTerminologyToPublicServer(rel.getStatusByStatusIdTo(), po);
                if (!transfer_success.isSuccess())
                {
                    //3.2.20 changed from debug to info
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
            LOGGER.info("----- initTransfer finished (003) -----");
            return transfer_success;
        }
    }

    public void setTargetCS(de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS)
    {
        this.targetCS = targetCS;
        if(this.targetCS.getCodeSystemVersions()!=null)
                            LOGGER.info("DABACA: codesystems size" + this.targetCS.getCodeSystemVersions().size());
    }

    public void setTargetVS(de.fhdo.terminologie.ws.searchPub.ValueSet targetVS)
    {
        this.targetVS = targetVS;
    }
}
