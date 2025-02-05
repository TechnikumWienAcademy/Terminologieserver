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
package de.fhdo.gui.admin.modules;

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.ValueSet;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.ImportValueSetResponse;
import de.fhdo.terminologie.ws.administration.LoginType;
import de.fhdo.terminologie.ws.administration.Status;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert M�tzner
 */
public class ImportVS_CSV extends Window implements AfterCompose, IGenericListActions
{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    byte[] bytes;
    GenericList genericListVocs;
    ValueSet selectedValueSet;
    Checkbox cbOrder;

    public ImportVS_CSV(){}

    public void onDateinameSelect(Event event){
        try{
            bytes = null;
            Media media = ((UploadEvent) event).getMedia();

            if (media != null){
                if(media.getContentType().equals("text/xml") 
                    || media.getContentType().equals("application/ms-excel") 
                    || media.getContentType().equals("text/csv")
                    || media.getContentType().equals("application/csv")
                    || media.getContentType().equals("application/vnd.ms-excel")
                    || media.getContentType().equals("application/soap+xml")){
                    
                    if (media.isBinary()){
                        logger.debug("media.isBinary()");

                        if (media.inMemory()){
                            logger.debug("Media in memory -> media.getByteData()");
                            bytes = media.getByteData();
                        }
                        else{
                            logger.debug("Media not in memory -> media.getStreamData()");
                            InputStream input = media.getStreamData();
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                            int bytesRead;
                            byte[] tempBuffer = new byte[8192 * 2];
                            while ((bytesRead = input.read(tempBuffer)) != -1){
                                byteOut.write(tempBuffer, 0, bytesRead);
                            }
                            bytes = byteOut.toByteArray();
                            byteOut.close();
                        }
                    }
                    else{
                        logger.debug("media.isBinary() is false");
                        bytes = media.getStringData().getBytes("ISO-8859-1");
                    }
                }

                logger.debug("ct: " + media.getContentType());
                logger.debug("format: " + media.getFormat());
                logger.debug("byte-length: " + bytes.length);
                logger.debug("bytes: " + bytes);

                Textbox tb = (Textbox) getFellow("textboxDateiname");
                tb.setValue(media.getName());
            }
        }
        catch (Exception ex){
            logger.error("Error [0151]: Fehler beim Laden eines Dokuments: " + ex.getMessage());
        }
    
        showStatus();
    }

    private void fillValueSetList(){
        try{
            // Header
            List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
            header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
            header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

            // Daten laden
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
      
            List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
            try{
                if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
                    ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();
                    for(AssignedTerm at:myTerms){
                        if(at.getClassname().equals("ValueSet")){
                            ValueSet cs = (ValueSet)hb_session.get(ValueSet.class, at.getClassId());
                            GenericListRowType row = createRowFromCodesystem(cs);
                            dataList.add(row);
                        }
                    }
                    Collections.sort(dataList,new ComparatorRowTypeName(true));
                }
                else{
                    String hql = "from ValueSet order by name";
                    List<ValueSet> csList = hb_session.createQuery(hql).list();

                    for (int i = 0; i < csList.size(); ++i){
                        ValueSet cs = csList.get(i);
                        GenericListRowType row = createRowFromCodesystem(cs);
                        dataList.add(row);
                    }
                }
            }
            catch (Exception e){
                logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
            }
            finally{
                hb_session.close();
            }

            // Liste initialisieren
            Include inc = (Include) getFellow("incList");
            Window winGenericList = (Window) inc.getFellow("winGenericList");
            genericListVocs = (GenericList) winGenericList;
            genericListVocs.setListActions(this);
            genericListVocs.setButton_new(false);
            genericListVocs.setButton_edit(false);
            genericListVocs.setButton_delete(false);
            genericListVocs.setListHeader(header);
            genericListVocs.setDataList(dataList);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private GenericListRowType createRowFromCodesystem(ValueSet cs){
        GenericListRowType row = new GenericListRowType();
        GenericListCellType[] cells = new GenericListCellType[2];
        cells[0] = new GenericListCellType(cs.getId(), false, "");
        cells[1] = new GenericListCellType(cs.getName(), false, "");

        row.setData(cs);
        row.setCells(cells);

        return row;
    }

    public void onSelected(String id, Object data){
        if (data != null){
            if (data instanceof ValueSet){
                selectedValueSet = (ValueSet) data;
                logger.debug("Selected Valueset: " + selectedValueSet.getName());
                showStatus();
            }
            else
                logger.debug("data: " + data.getClass().getCanonicalName());
        }
    }

    public void onNewClicked(String id){}

    public void onEditClicked(String id, Object data){}

    public void onDeleted(String id, Object data){}

    public void showStatus(){
        String s = "";
        String vsVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        if (bytes == null){
            s += "Bitte w�hlen Sie eine Datei aus.";
        }

        if (selectedValueSet == null && (vsVersion == null || vsVersion.length() == 0)){
            s += "\nBitte w�hlen Sie ein Value Set aus oder geben Sie einen Namen f�r ein neues ein.";
        }

        ((Label) getFellow("labelImportStatus")).setValue(s);
        ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
    }

    public void startImport(){
        ((Button) getFellow("buttonImport")).setDisabled(true);
        ((Button) getFellow("buttonCancel")).setVisible(true);

        String session_id = SessionHelper.getSessionAttributeByName("session_id").toString();
        String vsVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        String msg = "";
        Progressmeter progress = (Progressmeter) getFellow("progress");
        progress.setVisible(true);

        de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = service.getAdministrationPort(new MTOMFeature(true));

        //Login
        ImportValueSetRequestType request = new ImportValueSetRequestType();
        request.setLogin(new LoginType());
        request.getLogin().setSessionID("" + session_id);

        // Codesystem
        request.setValueSet(new types.termserver.fhdo.de.ValueSet());
        boolean vsvOnly = false;
        if(vsVersion != null && vsVersion.length() > 0){
            request.getValueSet().setName(vsVersion); //neues VS
            vsvOnly = false;
        }
        else{
            request.getValueSet().setId(selectedValueSet.getId()); //Version only
            vsvOnly = true;
        }
      
        ValueSetVersion csv = new ValueSetVersion(); //MRK ADDED
        csv.setName(vsVersion); //MRK ADDED
        request.getValueSet().getValueSetVersions().add(csv); //MRK ADDED

        // Claml-Datei
        request.setImportInfos(new ImportType());
        request.getImportInfos().setFilecontent(bytes);
        request.getImportInfos().setFormatId(300l);
        request.getImportInfos().setOrder(cbOrder.isChecked());
      
        if(vsvOnly){
            request.getImportInfos().setRole(CODES.ROLE_ADMIN + ":true"); //Zwischenl�sung...
        }else{
            request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole() + ":false"); //Zwischenl�sung...
        }

        ImportValueSetResponse.Return response = port.importValueSet(request);
        //importWS = (Response<ImportValueSetResponse>) port.importValueSetAsync(request, this);
        msg = response.getReturnInfos().getMessage();
        logger.debug("Return: " + msg);
        ReturnType ret=null;
        String returnStr="";
      
        //ValueSetVersion
        if(response.getReturnInfos().getStatus().equals(Status.OK)){    
            if((SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)
                || (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)))
                && !vsvOnly){
            
                // Erstellt ein neues ValueSet
                Proposal proposal = new Proposal();
                //proposal.setVocabularyId(0l);  // wird nach Erstellen eingef�gt
                if(!vsvOnly)//MRK DIESER IF BLOCK STATT OBEREM STATEMENT
                    proposal.setVocabularyId(0l);
                else
                    proposal.setVocabularyId(response.getValueSet().getId());
                
                proposal.setVocabularyName(response.getValueSet().getName());
                proposal.setContentType("valueset");
                proposal.setVocabularyNameTwo("ValueSet");

                ret = ProposalWorkflow.getInstance().addProposal(proposal, response.getValueSet(), false);
            
                returnStr = msg + " | " + ret.getMessage();
            }
            else{
                returnStr = msg;
            }
        }
        else{
            returnStr = msg;
        }
        progress.setVisible(false);
        ((Button) getFellow("buttonImport")).setDisabled(false);
        ((Button) getFellow("buttonCancel")).setVisible(false);
        ((Label) getFellow("labelImportStatus")).setValue(returnStr);
    }

    public void afterCompose(){
        cbOrder = (Checkbox)getFellow("cbOrder");
        fillValueSetList();
        showStatus();
    }
}
