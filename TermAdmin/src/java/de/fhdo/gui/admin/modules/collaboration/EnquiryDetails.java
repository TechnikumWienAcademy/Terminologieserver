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
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Organisation;
import de.fhdo.communication.Mail;
import de.fhdo.helper.CODES;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.interfaces.IUpdateModal;
import java.util.Date;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class EnquiryDetails extends Window implements AfterCompose{
    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
    public static final String ENQUIRY_TYPE_General_Enquiry = "Allgemeine Anfrage";
    public static final String ENQUIRY_TYPE_Discussion_Participant = "Anfrage zur Registrierung als Diskussionsteilnehmer";
    public static final String ENQUIRY_TYPE_Content_Admin = "Anfrage zur Registrierung als Inhaltsverwalter";
  
    private de.fhdo.collaboration.db.classes.Enquiry enquiry;
    private IUpdateModal updateListInterface;
    private Collaborationuser userExt;

    private Boolean activateUser = false;
    private Boolean activateUserEx = false;
    private String newPW = "";
  
    private Checkbox userTransfer;
    private Checkbox userExTransfer;
    private Checkbox closedIssue;

    public EnquiryDetails(){
        Map enquiryArguments = Executions.getCurrent().getArg();
        long enquiryID = 0;
        
        try{
            enquiryID = Long.parseLong(enquiryArguments.get("enquiry_id").toString());
        }
        catch (Exception ex){
            LOGGER.error("Error [0099]", ex);
        }

        if (enquiryID > 0){
            //Loading domains
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            enquiry = (de.fhdo.collaboration.db.classes.Enquiry) hb_session.get(de.fhdo.collaboration.db.classes.Enquiry.class, enquiryID);
      
            if(enquiry.getCollaborationuserExtPerson() == null){
                userExt = new Collaborationuser();
                userExt.setOrganisation(new Organisation());
            }
            else
                userExt = enquiry.getCollaborationuserExtPerson();  
            
            if(hb_session.isOpen())
                hb_session.close();
        }
    }

    public void selectedUserTransfer(int ID){
        if(ID == 1){
            if(userTransfer.isChecked()){
                userExTransfer.setChecked(false);
                userExTransfer.setDisabled(true);   
            }
            else{
                userExTransfer.setChecked(false);
                userExTransfer.setDisabled(false);
            }
        }
        else if(ID == 2){
            if(userExTransfer.isChecked()){  
                userTransfer.setChecked(false);
                userTransfer.setDisabled(true);
            }
            else{
                userTransfer.setChecked(false);
                userTransfer.setDisabled(false);
            }
        }
    }
  
    @Override
    public void afterCompose(){
        userTransfer = ((Checkbox)getFellow("cb_UserTransfer"));
        userExTransfer = ((Checkbox)getFellow("cb_UserExTransfer"));
        closedIssue = ((Checkbox)getFellow("cb_CloseIssue"));
      
        closedIssue.setChecked(enquiry.getClosedFlag());
        userTransfer.setChecked(!enquiry.getCollaborationuser().getHidden());
      
        if(enquiry.getCollaborationuserExtPerson() != null)
            userExTransfer.setChecked(!enquiry.getCollaborationuserExtPerson().getHidden());
        else
            userExTransfer.setChecked(false);
        
        if(enquiry.getRequestType().equals(EnquiryDetails.ENQUIRY_TYPE_General_Enquiry)){  
            ((Row)getFellow("row_Username")).setVisible(false);
            ((Row)getFellow("row_ExtendedInfo")).setVisible(false);
            ((Row)getFellow("row_TermName")).setVisible(false);
            ((Row)getFellow("row_TermDescription")).setVisible(false);
            ((Row)getFellow("row_IntendedValidityRange")).setVisible(false);
            ((Row)getFellow("row_MoreExtendedInfo")).setVisible(false);
            ((Row)getFellow("row_UsernameExt")).setVisible(false);
            ((Row)getFellow("row_VornameExt")).setVisible(false);
            ((Row)getFellow("row_NachnameExt")).setVisible(false);
            ((Row)getFellow("row_OrganisationExt")).setVisible(false);
            ((Row)getFellow("row_EmailExt")).setVisible(false);
            ((Row)getFellow("row_PhoneExt")).setVisible(false);
        }
      
        if(enquiry.getRequestType().equals(EnquiryDetails.ENQUIRY_TYPE_Discussion_Participant)){
            ((Checkbox)getFellow("cb_UserTransfer")).setVisible(true);
            ((Checkbox)getFellow("cb_UserExTransfer")).setVisible(false);
            ((Row)getFellow("row_Username")).setVisible(true);
            ((Row)getFellow("row_ExtendedInfo")).setVisible(true);
            ((Row)getFellow("row_TermName")).setVisible(true);
            ((Row)getFellow("row_TermDescription")).setVisible(false);
            ((Row)getFellow("row_IntendedValidityRange")).setVisible(false);
            ((Row)getFellow("row_MoreExtendedInfo")).setVisible(false);
            ((Row)getFellow("row_UsernameExt")).setVisible(false);
            ((Row)getFellow("row_VornameExt")).setVisible(false);
            ((Row)getFellow("row_NachnameExt")).setVisible(false);
            ((Row)getFellow("row_OrganisationExt")).setVisible(false);
            ((Row)getFellow("row_EmailExt")).setVisible(false);
            ((Row)getFellow("row_PhoneExt")).setVisible(false);
        }
      
        if(enquiry.getRequestType().equals(EnquiryDetails.ENQUIRY_TYPE_Content_Admin)){
            ((Checkbox)getFellow("cb_UserTransfer")).setVisible(true);
            if(enquiry.getCollaborationuserExtPerson() != null){ 
                ((Checkbox)getFellow("cb_UserTransfer")).setVisible(false);
                ((Checkbox)getFellow("cb_UserExTransfer")).setVisible(true);
                ((Row)getFellow("row_Username")).setVisible(false);
                ((Row)getFellow("row_ExtendedInfo")).setVisible(true);
                ((Row)getFellow("row_TermName")).setVisible(true);
                ((Row)getFellow("row_TermDescription")).setVisible(true);
                ((Row)getFellow("row_IntendedValidityRange")).setVisible(true);
                ((Row)getFellow("row_MoreExtendedInfo")).setVisible(true);       
                ((Row)getFellow("row_UsernameExt")).setVisible(true);            
                ((Row)getFellow("row_VornameExt")).setVisible(true);             
                ((Row)getFellow("row_NachnameExt")).setVisible(true);            
                ((Row)getFellow("row_OrganisationExt")).setVisible(true);        
                ((Row)getFellow("row_EmailExt")).setVisible(true);               
                ((Row)getFellow("row_PhoneExt")).setVisible(true);    
            }
            else{
                ((Checkbox)getFellow("cb_UserTransfer")).setVisible(true);
                ((Checkbox)getFellow("cb_UserExTransfer")).setVisible(false);
                ((Row)getFellow("row_Username")).setVisible(true);
                ((Row)getFellow("row_ExtendedInfo")).setVisible(true);
                ((Row)getFellow("row_TermName")).setVisible(true);
                ((Row)getFellow("row_TermDescription")).setVisible(true);
                ((Row)getFellow("row_IntendedValidityRange")).setVisible(true);
                ((Row)getFellow("row_MoreExtendedInfo")).setVisible(false);       
                ((Row)getFellow("row_UsernameExt")).setVisible(false);            
                ((Row)getFellow("row_VornameExt")).setVisible(false);             
                ((Row)getFellow("row_NachnameExt")).setVisible(false);            
                ((Row)getFellow("row_OrganisationExt")).setVisible(false);        
                ((Row)getFellow("row_EmailExt")).setVisible(false);               
                ((Row)getFellow("row_PhoneExt")).setVisible(false);    
            }
        }
    }

    public void onOkClicked(){
        String mailResponse;
        if(!enquiry.getClosedFlag()){
            try{
                if((userExt.getUsername() != null && userExt.getUsername().length() != 0) ||
                    (userExt.getFirstName() != null && userExt.getFirstName().length() != 0) ||
                    (userExt.getName() != null && userExt.getName().length() != 0) ||
                    (userExt.getOrganisation().getOrganisation() != null && userExt.getOrganisation().getOrganisation().length() != 0) ||
                    (userExt.getEmail() != null && userExt.getEmail().length() != 0) ||     
                    (userExt.getPhone() != null && userExt.getPhone().length() != 0)){

                    if( userExt.getUsername() == null || userExt.getUsername().length() == 0 || 
                        userExt.getFirstName() == null || userExt.getFirstName().length() == 0 ||
                        userExt.getName() == null || userExt.getName().length() == 0 ||
                        userExt.getOrganisation().getOrganisation() == null || userExt.getOrganisation().getOrganisation().length() == 0 ||
                        userExt.getEmail() == null || userExt.getEmail().length() == 0 || userExt.getEmail().contains("@") == false ||
                        userExt.getPhone() == null || userExt.getPhone().length() == 0){

                        Messagebox.show("Wenn Sie den Inhaltsverwalter extra angeben, \n dann bitte alle Felder ausf�llen!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
                        return;
                    }
                    else
                        enquiry.setCollaborationuserExtPerson(userExt);
                }

                Session hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();

                try{
                    if(userTransfer.isChecked()){
                        enquiry.getCollaborationuser().setEnabled(true);
                        enquiry.getCollaborationuser().setHidden(false);
                        activateUser = true;
                        closedIssue.setChecked(true);
                    }
                    else if(enquiry.getCollaborationuserExtPerson() != null && userExTransfer.isChecked()){
                        enquiry.getCollaborationuserExtPerson().setEnabled(true);
                        enquiry.getCollaborationuserExtPerson().setHidden(false);
                        enquiry.getCollaborationuser().setEnabled(false);
                        enquiry.getCollaborationuser().setHidden(true);
                        activateUserEx = true;
                        closedIssue.setChecked(true);
                    }
                    else if(enquiry.getCollaborationuserExtPerson() != null && !userExTransfer.isChecked()){
                        enquiry.getCollaborationuserExtPerson().setEnabled(false);
                        enquiry.getCollaborationuserExtPerson().setHidden(true);
                        enquiry.getCollaborationuser().setEnabled(false);
                        enquiry.getCollaborationuser().setHidden(true);
                    }
                    else{
                        enquiry.getCollaborationuser().setEnabled(false);
                        enquiry.getCollaborationuser().setHidden(true);
                    }

                    enquiry.setClosedFlag(closedIssue.isChecked());

                    if(enquiry.getCollaborationuserExtPerson() != null){
                        if(enquiry.getCollaborationuserExtPerson().getOrganisation() != null)
                            hb_session.merge(enquiry.getCollaborationuserExtPerson().getOrganisation());

                        if(activateUserEx){
                            newPW = Password.generateRandomPassword(8);
                            String salt = Password.generateRandomSalt();
                            enquiry.getCollaborationuserExtPerson().setPassword(Password.getSaltedPassword(newPW, salt, enquiry.getCollaborationuserExtPerson().getUsername()));
                            enquiry.getCollaborationuserExtPerson().setSalt(salt);
                            enquiry.getCollaborationuserExtPerson().setActivated(false);
                            enquiry.getCollaborationuserExtPerson().setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
                            enquiry.getCollaborationuserExtPerson().setActivationTime(new Date());
                        }
                        hb_session.merge(enquiry.getCollaborationuserExtPerson());
                    }

                    if(activateUser){
                        newPW = Password.generateRandomPassword(8);
                        String salt = Password.generateRandomSalt();
                        enquiry.getCollaborationuser().setPassword(Password.getSaltedPassword(newPW, salt, enquiry.getCollaborationuser().getUsername()));
                        enquiry.getCollaborationuser().setSalt(salt);
                        enquiry.getCollaborationuser().setActivated(false);
                        enquiry.getCollaborationuser().setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
                        enquiry.getCollaborationuser().setActivationTime(new Date());
                    }

                    hb_session.merge(enquiry.getCollaborationuser().getOrganisation());
                    hb_session.merge(enquiry.getCollaborationuser());
                    hb_session.merge(enquiry);

                    if(activateUser || activateUserEx){
                        Collaborationuser user = null;
                        if(activateUser)
                            user = enquiry.getCollaborationuser();
                        if(activateUserEx)
                            user = enquiry.getCollaborationuserExtPerson();

                        if(user != null){
                            //Sending notification
                            mailResponse = Mail.sendMailCollaborationNewUser(user.getUsername(), newPW,
                            user.getEmail(),user.getActivationMd5());
                            
                            if (mailResponse.length() == 0 && !hb_session.getTransaction().wasCommitted())
                                hb_session.getTransaction().commit();
                            else
                                throw new Exception("Mail konnte nicht versandt werden!");
                            newPW = "                       ";
                        }
                    }
                    else if(!hb_session.getTransaction().wasCommitted()){
                        hb_session.getTransaction().commit();
                    }
                }
                catch (Exception e){
                    LOGGER.error("Error [0080]", e);
                    if(!hb_session.getTransaction().wasRolledBack())
                        hb_session.getTransaction().rollback();
                }
                finally{
                    if(hb_session.isOpen())
                        hb_session.close();
                }
          
                if((activateUser && enquiry.getCollaborationuser().getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER)) ||
                    (activateUserEx && 
                    enquiry.getCollaborationuserExtPerson() != null && 
                    enquiry.getCollaborationuserExtPerson().getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER))){
                    
                    Messagebox.show("Der Benutzer wurde mit der Rolle Inhaltsverwalter \nin der Kollaboration angelegt!\n "
                    + "Bitte legen sie nun den Benutzer manuell im Terminologiebereich an!",
                    "ACHTUNG", Messagebox.OK, Messagebox.INFORMATION, (Event evt) -> {
                        if (evt.getName().equals("onOK")) {
                            EnquiryDetails.this.setVisible(false);
                            EnquiryDetails.this.detach();
                        } });
                    }
                else{
                    this.setVisible(false);
                    this.detach();
                }
         
                if (updateListInterface != null)
                    updateListInterface.update(enquiry, true);
            }
            catch (Exception e){
                LOGGER.error("Error [0083]", e);
            }
        }
    }
  
    public void onCancelClicked(){
        this.setVisible(false);
        this.detach();
    }

    public de.fhdo.collaboration.db.classes.Enquiry getEnquiry(){
        return enquiry;
    }

    public void setEnquiry(de.fhdo.collaboration.db.classes.Enquiry enquiry){
        this.enquiry = enquiry;
    }

    public void setUpdateListInterface(IUpdateModal updateListInterface){
        this.updateListInterface = updateListInterface;
    }
  
    public Collaborationuser getUserExt() {
        return userExt;
    }

    public void setUserExt(Collaborationuser userExt) {
        this.userExt = userExt;
    }
}