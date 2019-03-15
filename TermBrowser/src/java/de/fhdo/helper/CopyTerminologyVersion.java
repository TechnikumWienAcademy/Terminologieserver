/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse;
import de.fhdo.terminologie.ws.administration.ExportParameterType;
import de.fhdo.terminologie.ws.administration.ExportType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.LoginType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse;
import de.fhdo.terminologie.ws.authoring.Status;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import javax.xml.ws.soap.MTOMFeature;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Frohner
 */
public class CopyTerminologyVersion {
		
	private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
		
	public static ReturnType copyCodeSystemVersion(CodeSystem csToBeCopied, CodeSystemVersion csvNew, CodeSystemVersion csvToBeCopied){
		
		CodeSystem targetCS = csToBeCopied;
		ReturnType ret = new ReturnType();
		String msg = "";
		
		ExportCodeSystemContentRequestType req_export_cs = new ExportCodeSystemContentRequestType();
		req_export_cs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
		req_export_cs.getLogin().setSessionID(SessionHelper.getSessionId());

		req_export_cs.setCodeSystem(new CodeSystem());
		req_export_cs.getCodeSystem().setId(csToBeCopied.getId());
		CodeSystemVersion csv = new CodeSystemVersion();
		csv.setVersionId(csvToBeCopied.getVersionId());
		req_export_cs.getCodeSystem().getCodeSystemVersions().add(csv);

		// Export Type
		ExportType eType = new ExportType();
		eType.setFormatId(193l);            // TODO 193 = ClaML, 194 = CSV, 195 SVS
		eType.setUpdateCheck(false);
		req_export_cs.setExportInfos(eType);

		// Optional: ExportParameter
		ExportParameterType eParameterType = new ExportParameterType();
		eParameterType.setAssociationInfos("");
		eParameterType.setCodeSystemInfos(true);
		eParameterType.setTranslations(true);
		req_export_cs.setExportParameter(eParameterType);

		logger.debug("Export-Service-Aufruf...");

		// WS-Aufruf
		ExportCodeSystemContentResponse.Return response = WebServiceHelper.exportCodeSystemContent(req_export_cs);
		if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK)) {
          if(targetCS != null)
          {
              //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
              de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort(new MTOMFeature(true));
              // Login
              de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType request = new de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType();
              request.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
              request.getLogin().setSessionID(SessionHelper.getSessionId());

              // Codesystem
              request.setCodeSystem(new CodeSystem());
              request.getCodeSystem().setId(targetCS.getId());

              CodeSystemVersion csv1 = new CodeSystemVersion();
              csv1.setName(csvNew.getName());
              csv1.setDescription(csvNew.getDescription());
              csv1.setExpirationDate(csvNew.getExpirationDate());
              csv1.setLicenceHolder(csvNew.getLicenceHolder());
              csv1.setPreferredLanguageId(csvNew.getPreferredLanguageId());
              csv1.setSource(csvNew.getSource());
              csv1.setValidityRange(csvNew.getValidityRange());

              csvNew.setCodeSystem(null);

              request.getCodeSystem().getCodeSystemVersions().add(csv1);
              request.setImportInfos(new de.fhdo.terminologie.ws.administration.ImportType());
              request.getImportInfos().setFormatId(193L);
              request.getImportInfos().setFilecontent(response.getExportInfos().getFilecontent());
              request.getImportInfos().setRole(CODES.ROLE_ADMIN);

              de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return ret_import = port.importCodeSystem(request);
              if (ret_import.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK)) 
              {
                MaintainCodeSystemVersionRequestType request_maintain = new MaintainCodeSystemVersionRequestType();
                request_maintain.setLogin(new LoginType());
                request_maintain.getLogin().setSessionID(SessionHelper.getSessionId());
                
                //Add data of new Version
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setName(csvNew.getName());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setDescription(csvNew.getDescription());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setReleaseDate(csvNew.getReleaseDate());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setExpirationDate(csvNew.getExpirationDate());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setLicenceHolder(csvNew.getLicenceHolder());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setPreferredLanguageId(csvNew.getPreferredLanguageId());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setSource(csvNew.getSource());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setValidityRange(csvNew.getValidityRange());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setOid(csvNew.getOid());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setInsertTimestamp(csvNew.getInsertTimestamp());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setLastChangeDate(csvNew.getLastChangeDate());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setLicenceHolder(csvNew.getLicenceHolder());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setStatus(csvNew.getStatus());
                ret_import.getCodeSystem().getCodeSystemVersions().get(0).setStatusDate(csvNew.getStatusDate());
                        
                request_maintain.setCodeSystem(ret_import.getCodeSystem());
                VersioningType vType = new VersioningType();
                vType.setCreateNewVersion(false);
                request_maintain.setVersioning(vType);

                //Authoring_Service service_maintain = new Authoring_Service();
                Authoring port_maintain = WebServiceUrlHelper.getInstance().getAuthoringServicePort();

                MaintainCodeSystemVersionResponse.Return ret_maintain = port_maintain.maintainCodeSystemVersion(request_maintain);
                if(ret_maintain.getReturnInfos().getStatus().equals(Status.OK))
                {
                  Proposal proposal = new Proposal();
                  
                  CodeSystem cs_prop = new CodeSystem();
                  cs_prop.setId(ret_import.getCodeSystem().getId());
                  cs_prop.setName(ret_import.getCodeSystem().getName());
                  cs_prop.setCurrentVersionId(ret_import.getCodeSystem().getCurrentVersionId());

                  CodeSystemVersion csv_prop = new CodeSystemVersion();
                  csv_prop.setName(csvNew.getName());
                  csv_prop.setVersionId(ret_import.getCodeSystem().getCodeSystemVersions().get(0).getVersionId());

                  csv_prop.setCodeSystem(cs_prop);
                  
                  proposal.setVocabularyId(ret_import.getCodeSystem().getId());

                  proposal.setVocabularyName(ret_import.getCodeSystem().getName());
                  proposal.setContentType("vocabulary");
                  proposal.setVocabularyNameTwo("CodeSystem");

                  ProposalWorkflow.getInstance().addProposal(proposal, csv_prop, true);
                  msg += "CodeSystemVersion erfolgreich angelegt. ";
                  ret.setSuccess(true);
                  ret.setMessage(msg);
                  return ret;
                }
                else
                {
                  msg += "Beim Kopieren der Version ist ein Fehler aufgetreten. ";
                  ret.setSuccess(false);
                  ret.setMessage(msg);
                  return ret;
                }
              }
              else
              {
                msg += "Beim Kopieren der Version ist ein Fehler aufgetreten. ";
                ret.setSuccess(false);
                ret.setMessage(msg);
                return ret;
              }
          }
		}
		else
		{
			msg += "Beim Export ist ein Fehler aufgetreten. ";
			ret.setMessage(msg);
			return ret;
		}
		return null;
	}
	
    
    	public static ReturnType copyValueSetVersion(ValueSet vsToBeCopied, ValueSetVersion vsvNew, ValueSetVersion vsvToBeCopied)
        {
          ReturnType ret = new ReturnType();
          String msg = "";
          ValueSet targetVS = vsToBeCopied;
          
          ExportValueSetContentRequestType req_export_vs = new ExportValueSetContentRequestType();

          // Login
          req_export_vs.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
          req_export_vs.getLogin().setSessionID(SessionHelper.getSessionId());

          req_export_vs.setValueSet(new ValueSet());
          req_export_vs.getValueSet().setId(vsToBeCopied.getId());

          ValueSetVersion vsv = new ValueSetVersion();
          vsv.setVersionId(vsvToBeCopied.getVersionId());
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

          logger.debug("Export-Service-Aufruf...");

          // WS-Aufruf
          ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(req_export_vs);
          if (response.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK)) 
          {
            if(targetVS != null)
            {
              //de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
              de.fhdo.terminologie.ws.administration.Administration port = WebServiceUrlHelper.getInstance().getAdministrationServicePort(new MTOMFeature(true));

              // Login
              de.fhdo.terminologie.ws.administration.ImportValueSetRequestType request = new de.fhdo.terminologie.ws.administration.ImportValueSetRequestType();
              request.setLogin(new de.fhdo.terminologie.ws.administration.LoginType());
              request.getLogin().setSessionID(SessionHelper.getSessionId());

              // ValueSet
              request.setValueSet(new ValueSet());
              request.getValueSet().setId(targetVS.getId());

              ValueSetVersion vsvImport = new ValueSetVersion();
              vsvImport.setName(vsvNew.getName());
              vsvImport.setValidityRange(vsvNew.getValidityRange());

              request.getValueSet().getValueSetVersions().add(vsvImport);
              request.setImportInfos(new ImportType());
              request.getImportInfos().setFormatId(301L);
              request.getImportInfos().setFilecontent(response.getExportInfos().getFilecontent());
              request.getImportInfos().setRole(CODES.ROLE_ADMIN);
              request.getImportInfos().setOrder(Boolean.TRUE);

              de.fhdo.terminologie.ws.administration.ImportValueSetResponse.Return ret_import = port.importValueSet(request);
              if (ret_import.getReturnInfos().getStatus().equals(de.fhdo.terminologie.ws.administration.Status.OK)) 
              {
                MaintainValueSetRequestType request_maintain = new MaintainValueSetRequestType();
                
                request_maintain.setLogin(new LoginType());
                request_maintain.getLogin().setSessionID(SessionHelper.getSessionId());

                ret_import.getValueSet().getValueSetVersions().get(0).setName(vsvNew.getName());
                ret_import.getValueSet().getValueSetVersions().get(0).setOid(vsvNew.getOid());
                ret_import.getValueSet().getValueSetVersions().get(0).setReleaseDate(vsvNew.getReleaseDate());
                ret_import.getValueSet().getValueSetVersions().get(0).setStatus(vsvNew.getStatus());
                ret_import.getValueSet().getValueSetVersions().get(0).setStatusDate(vsvNew.getStatusDate());
                ret_import.getValueSet().getValueSetVersions().get(0).setValidityRange(vsvNew.getValidityRange());
                ret_import.getValueSet().getValueSetVersions().get(0).setInsertTimestamp(vsvNew.getInsertTimestamp());
                ret_import.getValueSet().getValueSetVersions().get(0).setLastChangeDate(vsvNew.getLastChangeDate());
                ret_import.getValueSet().getValueSetVersions().get(0).setPreferredLanguageId(vsvNew.getPreferredLanguageId());
                
                request_maintain.setValueSet(ret_import.getValueSet());
                
                VersioningType vType = new VersioningType();
                vType.setCreateNewVersion(false);
                request_maintain.setVersioning(vType);

                //Authoring_Service service_maintain = new Authoring_Service();
                Authoring port_maintain = WebServiceUrlHelper.getInstance().getAuthoringServicePort();

                MaintainValueSetResponse.Return ret_maintain = port_maintain.maintainValueSet(request_maintain);
                if(ret_maintain.getReturnInfos().getStatus().equals(Status.OK))
                {
                  Proposal proposal = new Proposal();
                
                  ValueSet vs_prop = new ValueSet();
                  vs_prop.setId(ret_import.getValueSet().getId());
                  vs_prop.setName(ret_import.getValueSet().getName());
                  vs_prop.setCurrentVersionId(ret_import.getValueSet().getCurrentVersionId());

                  ValueSetVersion vsv_prop = new ValueSetVersion();
                  vsv_prop.setName(vsvNew.getName());
                  vsv_prop.setVersionId(ret_import.getValueSet().getValueSetVersions().get(0).getVersionId());

                  vsv_prop.setValueSet(vs_prop);

                  proposal.setVocabularyId(vs_prop.getId());
                  proposal.setVocabularyName(vs_prop.getName());
                  proposal.setContentType("valueset");
                  proposal.setVocabularyNameTwo("ValueSet");

                  ProposalWorkflow.getInstance().addProposal(proposal, vsv_prop, true);
                  msg += "ValueSetVersion erfolgreich angelegt. ";
                  ret.setSuccess(true);
                  ret.setMessage(msg);
                  return ret;
                }
                else
                {
                  msg += "Beim Kopieren der Version ist ein Fehler aufgetreten. ";
                  ret.setSuccess(false);
                  ret.setMessage(msg);
                  return ret;
                }
              }
              else
              {
                msg += "Beim Kopieren der Version ist ein Fehler aufgetreten. ";
                ret.setSuccess(false);
                ret.setMessage(msg);
                return ret;
              }
            }
          }
          else
          {
              msg += "Beim Export ist ein Fehler aufgetreten. ";
              ret.setMessage(msg);
              return ret;
          }
          return null;
        }
}
