/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import com.csvreader.CsvWriter;
import de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Frohner
 */
public class FetchCSandVSHelper {
	
	private List<TermInfo> termInfoList = new ArrayList<TermInfo>();
	
	public byte[] getAllCSandVS(){
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			getCS();
			getVS();
			

			
			CsvWriter csv = new CsvWriter(bos, ';', Charset.forName("ISO-8859-1"));
			csv.setTextQualifier('\'');
			csv.setForceQualifier(true);
			
			csv.write("Name");
			csv.write("Typ");
			csv.write("OID");
			csv.write("ParentCodeSystem");
			csv.write("ParentCodeSystemOID");
			csv.endRecord();
			
			for (TermInfo termInfo : termInfoList){
				if (termInfo.getName() != null){
					csv.write(termInfo.getName());
				}
				if (termInfo.getCodeSystemType() != null){
					csv.write(termInfo.getCodeSystemType());
				}
				if (termInfo.getOID() != null){
					csv.write(termInfo.getOID());
				}
				if (termInfo.getParentCodeSystem() != null){
					csv.write(termInfo.getParentCodeSystem());
				}
				if (termInfo.getParentCodeSystemOID() != null){
					csv.write(termInfo.getParentCodeSystemOID());
				}
				
				csv.endRecord();
			}
			
			csv.close();
			
			
			
			
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			sw.toString();
			Messagebox.show(ex.getLocalizedMessage() + "\n" + sw.toString());
			Logger.getLogger(FetchCSandVSHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return bos.toByteArray();
	}
	
	private void getVS(){
		ListValueSetsRequestType parameter = new ListValueSetsRequestType();
		//Search_Service service = new Search_Service();
		Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();
		ListValueSetsResponse.Return resp = port.listValueSets(parameter);
		
		for (ValueSet vs : resp.getValueSet()){
			TermInfo termInfo = new TermInfo();
			
			if (vs.getName().equals("TestValueSet")){
				boolean stop = true;
			}
			
			termInfo.setName(vs.getName());
			for (ValueSetVersion vsv : vs.getValueSetVersions()){
				if (vs.getCurrentVersionId().equals(vsv.getVersionId())){
					System.out.println("Name " + vs.getName());
					termInfo.setCodeSystemType("Valueset");
					termInfo.setOID(vsv.getOid());
					
					// Request 
					ListValueSetContentsRequestType param2 = new ListValueSetContentsRequestType();
					// ValueSet und ValueSetVersion
					ValueSet vs1 = new ValueSet();
					ValueSetVersion vsv1 = new ValueSetVersion();
					vs1.setId(vs.getId());
					vs1.setName(vs.getName());
					vs1.getValueSetVersions().add(vsv1);
					vsv1.setVersionId(vsv.getVersionId());
					param2.setValueSet(vs1);

					// Sortierung
					//parameter.setSortingParameter(createSortingParameter());

					param2.setReadMetadataLevel(false);                

					// Response                                
					ListValueSetContentsResponse.Return response = WebServiceHelper.listValueSetContents(param2);
					
					if (response.getCodeSystemEntity().size() > 0)
					{
						Set<String> nameSet = new LinkedHashSet<String>();
						Set<String> oidSet = new LinkedHashSet<String>();
						
						for (CodeSystemEntity entity : response.getCodeSystemEntity()){
							nameSet.add(entity.getCodeSystemVersionEntityMemberships().get(0).getCodeSystemVersion().getName());
							oidSet.add(entity.getCodeSystemVersionEntityMemberships().get(0).getCodeSystemVersion().getOid());
						}
						
						Iterator<String> itNames = nameSet.iterator();
						Iterator<String> itOids = oidSet.iterator();
						
						String names = "";
						String oids = "";
						while (itNames.hasNext()){
							names += itNames.next() + ", ";
						}
						names = names.substring(0, names.lastIndexOf(","));
						
						while (itOids.hasNext()){
							oids += itOids.next() + ", ";
						}
						oids = oids.substring(0, oids.lastIndexOf(","));
						
						termInfo.setParentCodeSystem(names);
						termInfo.setParentCodeSystemOID(oids);
					}
					
					
					boolean stop = true;
				}
			}
			termInfoList.add(termInfo);
		}
	}
	
	private void getCS(){
		ListCodeSystemsRequestType parameter1 = new ListCodeSystemsRequestType();
		//Search_Service service = new Search_Service();
                Search port = WebServiceUrlHelper.getInstance().getSearchServicePort();
		ListCodeSystemsResponse.Return resp = port.listCodeSystems(parameter1);
			
		for (CodeSystem cs : resp.getCodeSystem()){
			TermInfo termInfo = new TermInfo();
			termInfo.setName(cs.getName());
			for (CodeSystemVersion csv : cs.getCodeSystemVersions()){
				
				//check if current version is not registered in CS, should not be but has been on BRZ Testsystem
				if (cs.getCurrentVersionId() != null){
					if(cs.getCurrentVersionId().equals(csv.getVersionId())){
						termInfo.setCodeSystemType("Codesystem");
						termInfo.setOID(csv.getOid());
						termInfoList.add(termInfo);
					}	
				}
				
			}
			
		}
	}
	
	public class TermInfo {

		private String name;
		private String CodeSystemType;
		private String OID;
		private String parentCodeSystem;
		private String parentCodeSystemOID;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCodeSystemType() {
			return CodeSystemType;
		}

		public void setCodeSystemType(String CodeSystemType) {
			this.CodeSystemType = CodeSystemType;
		}

		public String getOID() {
			return OID;
		}

		public void setOID(String OID) {
			this.OID = OID;
		}

		public String getParentCodeSystem() {
			return parentCodeSystem;
		}

		public void setParentCodeSystem(String parentCodeSystem) {
			this.parentCodeSystem = parentCodeSystem;
		}

		public String getParentCodeSystemOID() {
			return parentCodeSystemOID;
		}

		public void setParentCodeSystemOID(String parentCodeSystemOID) {
			this.parentCodeSystemOID = parentCodeSystemOID;
		}
		
		
		
	}
	
}
