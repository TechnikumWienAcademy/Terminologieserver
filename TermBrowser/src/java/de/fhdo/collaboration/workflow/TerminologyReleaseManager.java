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
import de.fhdo.terminologie.ws.authorizationPub.Authorization;
import de.fhdo.terminologie.ws.authorizationPub.CheckLoginResponse;
import de.fhdo.terminologie.ws.authorizationPub.LoginRequestType;
import de.fhdo.terminologie.ws.authorizationPub.LoginType;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.searchPub.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.searchPub.ListGloballySearchedConceptsResponse;
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
 * @author puraner
 */
public class TerminologyReleaseManager
{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private String sessionId;
    private String pubSessionId;
    private de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS;
    private de.fhdo.terminologie.ws.searchPub.ValueSet targetVS;
    private long importId;
    //3.2.17 added
    private boolean sessionIDsSet;

    public TerminologyReleaseManager()
    {
        this.sessionId = SessionHelper.getSessionId();
        this.pubSessionId = CollaborationSession.getInstance().getPubSessionID();
        this.targetCS = null;
        this.targetVS = null;
        try
        {
            importId = SecureRandom.getInstance("SHA1PRNG").nextLong();
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.error(ex);
        }
        //3.2.17 added
        sessionIDsSet = this.areSessionIdsSet();
    }

    /**
     * checks if publication plattform is alive
     *
     * @return
     */
    private boolean isPubPlattformAliveAndUserLoggedIn()
    {
        logger.info("TermBrowser: TerminologyReleaseManager.isPubPlattformAliveAndUserLoggedIn gestartet");
        //3.2.17 auskommentiert
        /*LoginRequestType request = new LoginRequestType();
        request.setLogin(new LoginType());
        request.getLogin().setSessionID(this.pubSessionId);*/
        Authorization port_authorizationPub = WebServiceUrlHelper.getInstance().getAuthorizationPubServicePort();

        if (port_authorizationPub == null)
        {
            logger.debug("PubServicePort ist null");
            return false;
        }
        //3.2.17
        return true;

        //3.2.17 auskommentiert
        /*
        try
        {
            CheckLoginResponse.Return response = port_authorizationPub.checkLogin(request);
            //ListCodeSystemsResponse.Return response = port_searchPub.listCodeSystems(parameter);
            if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authorizationPub.Status.OK))
            {
                return true;
            }
            else
            {
                logger.debug("User ist nicht eingeloggt");
                return false;
            }
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }*/
    }

    private boolean areSessionIdsSet()
    {
        logger.info("TermBrowser: TerminologyReleaseManager.areSessionIdsSet gestartet");
        if ((this.sessionId != null) && (this.pubSessionId != null))
        {
            return true;
        }

        return false;
    }

    private ReturnType transferTerminologyToPublicServer(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject po)
    {
        logger.info("TermBrowser: TerminologyReleaseManager.transferTerminologyToPublicServer gestartet");
        //initially set return value
        ReturnType ret = new ReturnType();
        ret.setSuccess(false);

        try
        {
            //check if pub is alive
            logger.debug("Überprüfe ob Publikationsplattform erreichbar ist und der Benutzer eingeloggt ist.");
            if (!this.isPubPlattformAliveAndUserLoggedIn())
            {
                logger.debug("Überprüfung fehlgeschlagen, Publikationsplattform nicht erreichbar oder Benutzer nicht eingeloggt.");
                ret.setSuccess(false);
                ret.setMessage("Verbindung zur Publikationsplattform konnte nicht hergestellt werden.");
                return ret;
            }
            logger.debug("Überprüfung erfolgreich.");
            
            //check if session Ids are set
            logger.debug("Überprüfe ob die SessionIDs gesetzt sind.");
            //3.2.17
            //if (!this.areSessionIdsSet())
            if(!this.sessionIDsSet)
            {
                logger.debug("Überprüfung fehlgeschlagen, SessionIDs nicht gesetzt.");
                ret.setSuccess(false);
                ret.setMessage("Session Ids are not set.");
                logger.info("TermBrowser: Collaboration Session: " + this.sessionId);
                logger.info("TermBrowser: Pub Session: " + this.pubSessionId);
                return ret;
            }
            logger.debug("Überprüfung erfolgreich.");

            //start with transfer
            if (po.getClassname().equals("CodeSystemVersion"))
            {
                CodeSystem csToExport = this.getCodeSystemToExport(po.getProposal().getVocabularyIdTwo(), po.getProposal().getVocabularyId());

                //check if CS was found on Collab plattform
                if ((csToExport.getId() == null) || (csToExport.getCodeSystemVersions().isEmpty()))
                {
                    ret.setSuccess(false);
                    ret.setMessage("CodeSystem für Transfer nicht gefunden. CSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ", CSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "CodeSystem: " + po.getProposal().getVocabularyName());
                    return ret;
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

                    if (!exists)
                    {
                        //mehrere CodeSysteme gefunden
                        Map map = new HashMap();
                        map.put("targets", result);
                        map.put("source", csToExport.getName());

                        Window win = (Window) Executions.createComponents("/collaboration/publication/selectTargetPopup.zul", null, map);
                        ((SelectTargetPopup) win).setReleaseManager(this);

                        win.doModal();
                        win.setVisible(false);

                        if (this.targetCS != null)
                        {
                            exists = true;
                        }
                    }
                }

                if ((this.targetCS == null) && (!exists))
                {
                    ret.setSuccess(false);
                    ret.setMessage("CodeSystem konnte auf der Publikationsplattform nicht gefunden werden. (" + csToExport.getName() + ")");
                    return ret;
                }

                ExportType exportedCodeSystem = this.getExportedCodeSystem(csToExport);

                if (exportedCodeSystem == null)
                {
                    ret.setSuccess(false);
                    ret.setMessage("CodeSystem konnte nicht exportiert werden. CSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ", CSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "CodeSystem: " + po.getProposal().getVocabularyName());
                    return ret;
                }

                de.fhdo.terminologie.ws.administrationPub.ReturnType ret_import = this.importCodeSystem(csToExport.getCodeSystemVersions().get(0), exportedCodeSystem);

                if (this.targetCS.getCodeSystemVersions() != null)
                {
                    this.removeTempCodeSystemVersion();
                }

                if (ret_import.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK))
                {
                    ret.setSuccess(true);
                    ret.setMessage(ret_import.getMessage());
                    return ret;
                }
                else
                {
                    ret.setSuccess(false);
                    ret.setMessage(ret_import.getMessage());
                    return ret;
                }
            }
            else if (po.getClassname().equals("ValueSetVersion"))
            {
                ValueSet vsToExport = this.getValueSetToExport(po.getProposal().getVocabularyName(), po.getProposal().getVocabularyId());

                //check if CS was found on Collab plattform
                if ((vsToExport.getId() == null) || (vsToExport.getValueSetVersions().isEmpty()))
                {
                    ret.setSuccess(false);
                    ret.setMessage("ValueSet für Transfer nicht gefunden.\nVSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ",\nVSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "\nValueSet: " + po.getProposal().getVocabularyName());
                    return ret;
                }
                
                ArrayList<String> missingEntries = this.areValueSetContentsPresentOnPubPlattform(vsToExport);

                if (!missingEntries.isEmpty())
                {
                    ret.setSuccess(false);
                    String message = "ValueSet kann nicht freigegeben werden, da benötigte CodeSystem(e) auf der Publikationsplattform fehlen.\n"
                            + "VSID: " + po.getProposal().getVocabularyIdTwo() + "\n"
                            + "VSV ID: " + po.getProposal().getVocabularyId() + "\n"
                            + "ValueSet: " + po.getProposal().getVocabularyName() + "\n"
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
                    
                    ret.setMessage(message);
                    return ret;
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

                if ((this.targetVS == null) && (!exists))
                {
                    ret.setSuccess(false);
                    ret.setMessage("ValueSet konnte auf der Publikationsplattform nicht gefunden werden. (" + vsToExport.getName() + ")");
                    return ret;
                }

                ExportType exportedValueSet = this.getExportedValueSet(vsToExport);

                if (exportedValueSet == null)
                {
                    ret.setSuccess(false);
                    ret.setMessage("ValueSet konnte nicht exportiert werden.\nVSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ",\n VSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "\nValueSet: " + po.getProposal().getVocabularyName());
                    return ret;
                }

                de.fhdo.terminologie.ws.administrationPub.ReturnType ret_import = this.importValueSet(vsToExport.getValueSetVersions().get(0), exportedValueSet);

                if (this.targetVS.getValueSetVersions() != null)
                {
                    this.removeTempValueSetVersion();
                }

                if (ret_import.getStatus().equals(de.fhdo.terminologie.ws.administrationPub.Status.OK))
                {
                    ret.setSuccess(true);
                    ret.setMessage(ret_import.getMessage());
                    return ret;
                }
                else
                {
                    ret.setSuccess(false);
                    ret.setMessage(ret_import.getMessage());
                    return ret;
                }

            }
            else if (po.getClassname().equals("CodeSystem"))
            {
                CodeSystem csToExport = this.getCodeSystemToExport(po.getProposal().getVocabularyIdTwo(), null);

                //check if CS was found on Collab plattform
                if ((csToExport.getId() == null) || (csToExport.getCodeSystemVersions().isEmpty()))
                {
                    ret.setSuccess(false);
                    ret.setMessage("CodeSystem für Transfer nicht gefunden.\nCSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ",\n CSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "\nCodeSystem: " + po.getProposal().getVocabularyName());
                    return ret;
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
                        ret.setSuccess(true);
                        ret.setMessage("CodeSystem successfully created.");
                        return ret;
                    }
                    else
                    {
                        ret.setSuccess(false);
                        ret.setMessage("CodeSystem konnte auf Publikationsumgebung nicht erstellt werden.");
                        return ret;
                    }
                }
                else
                {
                    ret.setSuccess(true);
                    ret.setMessage("CodeSystem already exists.");
                    return ret;
                }
            }
            else if (po.getClassname().equals("ValueSet"))
            {
                ValueSet vsToExport = this.getValueSetToExport(po.getProposal().getVocabularyName(), null);

                //check if CS was found on Collab plattform
                if ((vsToExport.getId() == null) || (vsToExport.getValueSetVersions().isEmpty()))
                {
                    ret.setSuccess(false);
                    ret.setMessage("ValueSet für Transfer nicht gefunden. VSID: "
                            + po.getProposal().getVocabularyIdTwo()
                            + ", VSV ID: "
                            + po.getProposal().getVocabularyId()
                            + "ValueSet: " + po.getProposal().getVocabularyName());
                    return ret;
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
                        ret.setSuccess(true);
                        ret.setMessage("ValueSet successfully created.");
                        return ret;
                    }
                    else
                    {
                        ret.setSuccess(false);
                        ret.setMessage("ValueSet konnte auf Publikationsumgebung nicht erstellt werden.");
                        return ret;
                    }
                }
                else
                {
                    ret.setSuccess(true);
                    ret.setMessage("ValueSet already exists.");
                    return ret;
                }
            }
        }
        catch (ServerSOAPFaultException ex)
        {
            logger.error(ex);
            ret.setSuccess(false);
            ret.setMessage("Die Antwort des Servers ist fehlerhaft.");
        }
        catch (Exception ex)
        {
            //TODO set message
            logger.error(ex);
            ret.setSuccess(false);
            ret.setMessage("An Error occured.");
        }

        return ret;
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
    private CodeSystem getCodeSystemToExport(Long codesystemId, Long codesystemVersionId) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getCodeSystemToExport gestartet");
        logger.info("TermBrowser: TRANSFER: Trying to find CodeSystem and Version in collab plattform");
        logger.info("TermBrowser: CS ID: " + codesystemId);
        logger.info("TermBrowser: CSV ID: " + codesystemVersionId);

        de.fhdo.terminologie.ws.search.Search port_search = WebServiceUrlHelper.getInstance().getSearchServicePort();
        de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType request_search = new de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType();
        request_search.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        request_search.getLogin().setSessionID(this.sessionId);
        request_search.setCodeSystem(new CodeSystem());
        request_search.getCodeSystem().setId(codesystemId);
        //3.2.17
        request_search.setLoginAlreadyChecked(true);
        
        de.fhdo.terminologie.ws.search.ListCodeSystemsResponse.Return resp = port_search.listCodeSystems(request_search);
        
        if ((resp.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                && (resp.getCodeSystem() != null)
                && (resp.getCodeSystem().size() == 1))
        {
            if (codesystemVersionId != null)
            {
                CodeSystemVersion csv_search = null;
                for (CodeSystemVersion csv_temp : resp.getCodeSystem().get(0).getCodeSystemVersions())
                {
                    if (csv_temp.getVersionId().equals(codesystemVersionId))
                    {
                        csv_search = csv_temp;
                    }
                }

                if (csv_search != null)
                {
                    CodeSystem cs = resp.getCodeSystem().get(0);
                    cs.getCodeSystemVersions().clear();
                    cs.getCodeSystemVersions().add(csv_search);
                    return cs;
                }
                else
                {
                    return new CodeSystem();
                }
            }
            else
            {
                return resp.getCodeSystem().get(0);
            }
        }
        else
        {
            return new CodeSystem();
        }
    }

    /**
     * Exports the requestes CodeSystem from the collab plattform
     *
     * @param codesystemId
     * @param codesystemVersionId
     * @return
     */
    private ExportType getExportedCodeSystem(CodeSystem codesystem) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getExportedCodeSystem gestartet");
        Long codesystemId = codesystem.getId();
        Long codesystemVersionId = codesystem.getCodeSystemVersions().get(0).getVersionId();

        logger.info("TermBrowser: TRANSFER: Trying to export CodeSystem and Version from collab plattform");
        logger.info("TermBrowser: CS ID: " + codesystemId);
        logger.info("TermBrowser: CSV ID: " + codesystemVersionId);

        ExportCodeSystemContentRequestType req_export_cs = new ExportCodeSystemContentRequestType();
        req_export_cs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        req_export_cs.getLogin().setSessionID(this.sessionId);
        //3.2.17 added
        req_export_cs.setLoginAlreadyChecked(true);

        req_export_cs.setCodeSystem(new CodeSystem());
        req_export_cs.getCodeSystem().setId(codesystemId);
        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setVersionId(codesystemVersionId);
        req_export_cs.getCodeSystem().getCodeSystemVersions().add(csv);

        // Export Type
        ExportType eType = new ExportType();
        long format = 193L;

        logger.info("TermBrowser: Exportformat: " + format);
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
                logger.error(ex);
            }
            catch (IOException ex)
            {
                logger.error(ex);
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
                        logger.error(ex);
                    }
                }
            }
        }
        else
        {
            // WS-Aufruf
            logger.info("TermBrowser: Export-Service-Aufruf...");
            response = WebServiceHelper.exportCodeSystemContent(req_export_cs);
        }

        return response.getExportInfos();
    }

    private List<de.fhdo.terminologie.ws.searchPub.CodeSystem> getTargetCodeSystemFromPub(String codesystemName) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getTargetCodeSystemFromPub gestartet");
        de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListCodeSystemsRequestType();
        request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        request_searchPub.getLogin().setSessionID(this.pubSessionId);
        request_searchPub.setCodeSystem(new de.fhdo.terminologie.ws.searchPub.CodeSystem());
        request_searchPub.getCodeSystem().setName(codesystemName);
        //3.2.17
        request_searchPub.setLoginAlreadyChecked(true);
        
        de.fhdo.terminologie.ws.searchPub.ListCodeSystemsResponse.Return respSearchPub = port_searchPub.listCodeSystems(request_searchPub);

        if (respSearchPub.getReturnInfos().getStatus().equals(Status.OK))
        {

            return respSearchPub.getCodeSystem();
        }
        else
        {
            return new ArrayList<de.fhdo.terminologie.ws.searchPub.CodeSystem>();
        }
    }

    private de.fhdo.terminologie.ws.administrationPub.ReturnType importCodeSystem(CodeSystemVersion csv, ExportType exportedCs) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.importCodeSystem gestartet");
        de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        // Login
        de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);
        //3.2.17
        request.setLoginAlreadyChecked(true);
        
        // Codesystem
        request.setCodeSystem(new de.fhdo.terminologie.ws.administrationPub.CodeSystem());
        request.getCodeSystem().setId(this.targetCS.getId());

        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        if (this.targetCS.getName().contains("LOINC"))
        {
            request.getImportInfos().setOrder(true);
            request.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else
        {
            request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        }

        de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion csv_pub = new de.fhdo.terminologie.ws.administrationPub.CodeSystemVersion();
        csv_pub.setName(csv.getName());
        request.getCodeSystem().getCodeSystemVersions().add(csv_pub);
        request.getImportInfos().setFormatId(exportedCs.getFormatId());
        request.getCodeSystem().setName(this.targetCS.getName());
        request.getImportInfos().setFilecontent(exportedCs.getFilecontent());
        request.setImportId(this.importId);

        
        
        de.fhdo.terminologie.ws.administrationPub.ImportCodeSystemResponse.Return ret_import = port.importCodeSystem(request);

        return ret_import.getReturnInfos();
    }

    private void createTempCodeSystemVersionOnPub(CodeSystem cs)
    {
        logger.info("TermBrowser: TerminologyReleaseManager.createTempCodeSystemVersionOnPub gestartet");
        de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

        de.fhdo.terminologie.ws.authoringPub.CodeSystem cs_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
        cs_pub.setName(cs.getName());
        cs_pub.setDescription(cs.getDescription());
        cs_pub.setDescriptionEng(cs.getDescriptionEng());
        cs_pub.setIncompleteCS(cs.isIncompleteCS());
        cs_pub.setResponsibleOrganization(cs.getResponsibleOrganization());
        cs_pub.setWebsite(cs.getWebsite());
        cs_pub.setAutoRelease(cs.isAutoRelease());
        cs_pub.setCodeSystemType(cs.getCodeSystemType());
        de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion csv_pub = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
        csv_pub.setName("Temp_import");
        cs_pub.getCodeSystemVersions().add(csv_pub);
        request.setCodeSystem(cs_pub);
        //3.2.17 added
        request.setLoginAlreadyChecked(true);
        
        de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();

        de.fhdo.terminologie.ws.authoringPub.CreateCodeSystemResponse.Return ret_pub = port.createCodeSystem(request);
        if (ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
        {
            targetCS = new de.fhdo.terminologie.ws.searchPub.CodeSystem();
            targetCS.setId(ret_pub.getCodeSystem().getId());
        }
    }

    private void removeTempCodeSystemVersion()
    {
        logger.info("TermBrowser: TerminologyReleaseManager.removeTempCodeSystemVersion gestartet");
        for (de.fhdo.terminologie.ws.searchPub.CodeSystemVersion v : targetCS.getCodeSystemVersions())
        {
            if (v.getName().equals("Temp_import"))
            {
                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                req_remove.getLogin().setSessionID(this.pubSessionId);
                //3.2.17 added
                req_remove.setLoginAlreadyChecked(true);

                req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.CodeSystem cs_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystem();
                cs_remove.setId(this.targetCS.getId());
                de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion csv_remove = new de.fhdo.terminologie.ws.authoringPub.CodeSystemVersion();
                csv_remove.setVersionId(v.getVersionId());
                cs_remove.getCodeSystemVersions().add(csv_remove);

                req_remove.getDeleteInfo().setCodeSystem(cs_remove);
                req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.CODE_SYSTEM_VERSION);

                de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);

                if (resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                {
                    logger.info("TermBrowser: Temp Version erfolgreich entfernt.");
                }
                else
                {
                    logger.error("TermBrowser: Löschen der Version fehlgeschlagen. ");
                }
            }
        }
    }

    private void removeTempValueSetVersion()
    {
        logger.info("TermBrowser: TerminologyReleaseManager.removeTempValueSetVersion gestartet");
        for (de.fhdo.terminologie.ws.searchPub.ValueSetVersion v : targetVS.getValueSetVersions())
        {
            if (v.getName().equals("Temp_import"))
            {
                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType req_remove = new de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptRequestType();
                req_remove.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
                req_remove.getLogin().setSessionID(this.pubSessionId);

                req_remove.setDeleteInfo(new de.fhdo.terminologie.ws.authoringPub.DeleteInfo());
                de.fhdo.terminologie.ws.authoringPub.ValueSet vs_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSet();
                vs_remove.setId(targetVS.getId());
                de.fhdo.terminologie.ws.authoringPub.ValueSetVersion vsv_remove = new de.fhdo.terminologie.ws.authoringPub.ValueSetVersion();
                vsv_remove.setVersionId(v.getVersionId());
                vs_remove.getValueSetVersions().add(vsv_remove);

                req_remove.getDeleteInfo().setValueSet(vs_remove);
                req_remove.getDeleteInfo().setType(de.fhdo.terminologie.ws.authoringPub.Type.VALUE_SET_VERSION);

                de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();

                de.fhdo.terminologie.ws.authoringPub.RemoveTerminologyOrConceptResponseType resp_remove = port.removeTerminologyOrConcept(req_remove);
                if (resp_remove.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
                {
                    logger.info("TermBrowser: Temp Version erfolgreich entfernt.");
                }
                else
                {
                    logger.error("TermBrowser: Löschen der Version fehlgeschlagen. ");
                }
            }
        }
    }

    private ValueSet getValueSetToExport(String valuesetName, Long valuesetVersionId) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getValueSetToExport gestartet");
        logger.info("TermBrowser: TRANSFER: Trying to find ValueSet and Version in collab plattform");
        logger.info("TermBrowser: VS ID: " + valuesetName);
        logger.info("TermBrowser: VSV ID: " + valuesetVersionId);

        ListValueSetsRequestType req_vs = new ListValueSetsRequestType();
        req_vs.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        req_vs.getLogin().setSessionID(this.sessionId);

        ValueSet vs_search = new ValueSet();
        //vs.setId(po.getClassId());
        vs_search.setName(valuesetName);
        req_vs.setValueSet(vs_search);

        de.fhdo.terminologie.ws.search.Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();

        ListValueSetsResponse.Return ret_vs = port.listValueSets(req_vs);

        if ((ret_vs.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
                && (ret_vs.getValueSet() != null)
                && (ret_vs.getValueSet().size() == 1))
        {
            if (valuesetVersionId != null)
            {
                ValueSetVersion vsv_search = null;
                for (ValueSetVersion vsv_temp : ret_vs.getValueSet().get(0).getValueSetVersions())
                {
                    if (vsv_temp.getVersionId().equals(valuesetVersionId))
                    {
                        vsv_search = vsv_temp;
                    }
                }

                if (vsv_search != null)
                {
                    ValueSet vs = ret_vs.getValueSet().get(0);
                    vs.getValueSetVersions().clear();
                    vs.getValueSetVersions().add(vsv_search);
                    return vs;
                }
                else
                {
                    return new ValueSet();
                }
            }
            else
            {
                return ret_vs.getValueSet().get(0);
            }
        }
        else if(ret_vs.getValueSet().size() > 1)
        {
            for(ValueSet v : ret_vs.getValueSet())
            {
                if(v.getName().equals(valuesetName))
                {
                    if (valuesetVersionId != null)
                    {
                        ValueSetVersion vsv_search = null;
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
                            return vs;
                        }
                        else
                        {
                            return new ValueSet();
                        }
                    }
                    else
                    {
                        return ret_vs.getValueSet().get(0);
                    }
                }
            }
        }
        else
        {
            return new ValueSet();
        }
        
        return new ValueSet();
    }

    private ExportType getExportedValueSet(ValueSet valueSet) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getExportedValueSet gestartet");
        Long valuesetId = valueSet.getId();
        Long valueSetVersionId = valueSet.getValueSetVersions().get(0).getVersionId();

        logger.info("TermBrowser: TRANSFER: Trying to export ValueSet and Version from collab plattform");
        logger.info("TermBrowser: VS ID: " + valuesetId);
        logger.info("TermBrowser: VSV ID: " + valueSetVersionId);

        ExportValueSetContentRequestType req_export_vs = new ExportValueSetContentRequestType();

        // Login
        req_export_vs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
        req_export_vs.getLogin().setSessionID(this.pubSessionId);

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

        logger.debug("TermBrowser: Export-Service-Aufruf...");

        // WS-Aufruf
        ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(req_export_vs);

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
        logger.info("TermBrowser: TerminologyReleaseManager.areValueSetContentsPresentOnPubPlattform gestartet");
        ArrayList<String> missingEntries = new ArrayList<String>();
        
        ListValueSetContentsRequestType requestVsContent = new ListValueSetContentsRequestType();
        requestVsContent.setLogin(new de.fhdo.terminologie.ws.search.LoginType());
        requestVsContent.getLogin().setSessionID(this.sessionId);

        requestVsContent.setValueSet(valueSet);
        requestVsContent.setReadMetadataLevel(false);

        Search search_port = WebServiceUrlHelper.getInstance().getSearchServicePort();

        ListValueSetContentsResponse.Return responseVsContent = search_port.listValueSetContents(requestVsContent);

        if (responseVsContent.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.search.Status.OK))
        {
            Iterator<CodeSystemEntity> it = responseVsContent.getCodeSystemEntity().iterator();
            de.fhdo.terminologie.ws.searchPub.Search portSearchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();

            do
            {
                CodeSystemEntity cse = it.next();
                ListGloballySearchedConceptsRequestType parameter = new ListGloballySearchedConceptsRequestType();

                //CS Global Search
                parameter.setCodeSystemConceptSearch(true);

                parameter.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
                parameter.getLogin().setSessionID(this.pubSessionId);

                parameter.setCode(cse.getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCode());
                parameter.setTerm("");
                
                ListGloballySearchedConceptsResponse.Return response = portSearchPub.listGloballySearchedConcepts(parameter);

                if (response.getReturnInfos().getStatus().equals(Status.OK))
                {
                    if (response.getGlobalSearchResultEntry().isEmpty())
                    {
                        missingEntries.add(parameter.getCode());
                        logger.info("TermBrowser: TerminologyReleaseManager.areValueSetContentsPresentOnPubPlattform: Code" + parameter.getCode() + " nicht gefunden");
                    }
                }
                else
                {
                    missingEntries.add(parameter.getCode());
                }
            }
            while (it.hasNext());
        }
        return missingEntries;
    }

    private List<de.fhdo.terminologie.ws.searchPub.ValueSet> getTargetValueSetFromPub(String valuesetName) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.getTargetValueSetFromPub gestartet");
        de.fhdo.terminologie.ws.searchPub.Search port_searchPub = WebServiceUrlHelper.getInstance().getSearchPubServicePort();
        de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType request_searchPub = new de.fhdo.terminologie.ws.searchPub.ListValueSetsRequestType();
        request_searchPub.setLogin(new de.fhdo.terminologie.ws.searchPub.LoginType());
        request_searchPub.getLogin().setSessionID(this.pubSessionId);
        request_searchPub.setValueSet(new de.fhdo.terminologie.ws.searchPub.ValueSet());
        request_searchPub.getValueSet().setName(valuesetName);
        de.fhdo.terminologie.ws.searchPub.ListValueSetsResponse.Return respSearchPub = port_searchPub.listValueSets(request_searchPub);

        if (respSearchPub.getReturnInfos().getStatus().equals(Status.OK))
        {

            return respSearchPub.getValueSet();
        }
        else
        {
            return new ArrayList<de.fhdo.terminologie.ws.searchPub.ValueSet>();
        }
    }

    private de.fhdo.terminologie.ws.administrationPub.ReturnType importValueSet(ValueSetVersion vsv, ExportType exportedVs) throws ServerSOAPFaultException
    {
        logger.info("TermBrowser: TerminologyReleaseManager.importValueSet gestartet");
        de.fhdo.terminologie.ws.administrationPub.Administration port = WebServiceUrlHelper.getInstance().getAdministrationPubServicePort(new MTOMFeature(true));
        // Login
        de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType request = new de.fhdo.terminologie.ws.administrationPub.ImportValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.administrationPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

        // Codesystem
        request.setValueSet(new de.fhdo.terminologie.ws.administrationPub.ValueSet());
        request.getValueSet().setId(this.targetVS.getId());

        request.setImportInfos(new de.fhdo.terminologie.ws.administrationPub.ImportType());
        request.getImportInfos().setRole(CODES.ROLE_TRANSFER);
        request.getImportInfos().setFormatId(301l); //export formatId != import formatId; 195 != 301
        request.getImportInfos().setFilecontent(exportedVs.getFilecontent());
        request.getImportInfos().setOrder(Boolean.TRUE);
        request.setImportId(this.importId);

        de.fhdo.terminologie.ws.administrationPub.ValueSetVersion vsv_pub = new de.fhdo.terminologie.ws.administrationPub.ValueSetVersion();
        vsv_pub.setName(vsv.getName());
        request.getValueSet().getValueSetVersions().add(vsv_pub);

        de.fhdo.terminologie.ws.administrationPub.ImportValueSetResponse.Return ret_import = port.importValueSet(request);

        return ret_import.getReturnInfos();
    }

    private void createTempValueSetVersionOnPub(ValueSet vs)
    {
        logger.info("TermBrowser: TerminologyReleaseManager.createTempValueSetVersionOnPub gestartet");
        de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType request = new de.fhdo.terminologie.ws.authoringPub.CreateValueSetRequestType();
        request.setLogin(new de.fhdo.terminologie.ws.authoringPub.LoginType());
        request.getLogin().setSessionID(this.pubSessionId);

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

        de.fhdo.terminologie.ws.authoringPub.Authoring port = WebServiceUrlHelper.getInstance().getAuthoringPubServicePort();
        de.fhdo.terminologie.ws.authoringPub.CreateValueSetResponse.Return ret_pub = port.createValueSet(request);

        if (ret_pub.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.authoringPub.Status.OK))
        {
            this.targetVS = new de.fhdo.terminologie.ws.searchPub.ValueSet();
            this.targetVS.setId(ret_pub.getValueSet().getId());
        }
    }
    
    public ReturnType initTransfer(Set<Proposalobject> proposalObjects, Statusrel rel)
    {
        logger.info("TermBrowser: TerminologyReleaseManager.initTransfer gestartet");
        ReturnType transfer_success = new ReturnType();
        transfer_success.setSuccess(false);

        try
        {
            if(proposalObjects.isEmpty())
            {
                transfer_success.setMessage("Error: Empty set of Proposalobjects.");
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

            for (Proposalobject po : terminologies)
            {
                transfer_success = this.transferTerminologyToPublicServer(rel.getStatusByStatusIdTo(), po);
                if (!transfer_success.isSuccess())
                {
                    logger.debug("Ein ProposalObject konnte nicht erfolgreich zum PublicServer transferiert werden");
                    break;
                }
            }
            return transfer_success;
        }
        catch (Exception ex)
        {
            transfer_success.setSuccess(false);
            transfer_success.setMessage(ex.getMessage());
            return transfer_success;
        }
    }

    public void setTargetCS(de.fhdo.terminologie.ws.searchPub.CodeSystem targetCS)
    {
        this.targetCS = targetCS;
    }

    public void setTargetVS(de.fhdo.terminologie.ws.searchPub.ValueSet targetVS)
    {
        this.targetVS = targetVS;
    }
}
