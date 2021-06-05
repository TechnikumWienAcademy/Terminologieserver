/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.communication;

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Collaborationuser;

/**
 *
 * @author PU
 */
public class M_AUT {
 
    public static final String MAIL_START = "Sehr geehrte/r Terminologieserver BenutzerIn, \n\n";
    public static final String STATUS_CHANGE_SUBJECT = "�nderung des Status eines Vorschlags";
    public static final String SV_ASSIGNMENT_SUBJECT = "Zuweisung Inhaltsverwaltung";
    public static final String PROPOSAL_SUBJECT = "Terminologie Vorschlag";
    public static final String PROPOSAL_STATUS_SUBJECT = "Status �nderung eines Vorschlags";
    
    private String MAIL_FOOTER;
    private String WEBLINK_COLLAB;
    private static M_AUT instance = null;
    
    public M_AUT(){
    
        getWeblink();
        getFooter();
    }
    
    public String getSvAssignementText(String terminologie){
    
        String s = "Ihnen wurde die Terminologie: " + terminologie + " \n"
                    + "zur Inhaltsverwaltung zugewiesen.";
        return s;
    }
    
    public String getUserName(Collaborationuser u){
    
        String s = "";
        if(u.getFirstName() != null && !u.getFirstName().equals("") && u.getName() != null && !u.getName().equals("")){
        
            s = u.getFirstName() + " " + u.getName();
        }else{
            s = u.getUsername();
        }
        
        return s;
    }
    
    public String getMailFooter(){
    
        return this.MAIL_FOOTER;
    }
    
    private void getWeblink(){
    
        WEBLINK_COLLAB = DBSysParam.instance().getStringValue("weblink", null, null) + "/gui/info/enquiry.zul";
    }
    
    private void getFooter(){
        
        MAIL_FOOTER =       "\n\nMit freundlichen Gr��en,\n" +
                            "Ihr Terminologieserver - Team" +
                            "\n------------------------------------------------\n" +
                            "Liebe Benutzerin/lieber Benutzer,\n\n" +
                            "�ber diese E-Mail Adresse k�nnen keine Anfragen bearbeitet werden. F�r Ihre Anliegen haben wir ein Webformular eingerichtet.\n" +
                            "Daher bitten wir, Anfragen nicht per E-Mail sondern �ber das Formular zu �bermitteln: " + WEBLINK_COLLAB + "\n\n" +
                            "Mit freundlichen Gr��en,\n" +
                            "Ihr Terminologieserver - Team";
    }
    
    public String getProposalText(String vocabularyName, String contentType, String description){
    
        String s = "Ihr Vorschlag zu der Terminologie " + vocabularyName + " (" + contentType + ") \n"
                 + "wurde im System aufgenommen.\n Sollten sie NICHT der InhaltsverwalterIn der Terminologie \n"
                 + "sein, wurde auch ein eMail an den/die TerminologieverwalterIn versandt.\n\n"
                 + "Ihr Ansuchen: " + description;
                 
        return s;
    }
    
    public String getProposalSelbstVerwText(String vocabularyName, String contentType, String description){
    
        String s = "Zu der von Ihnen verwalteten Terminologie " + vocabularyName + " (" + contentType + ") \n"
                 + "wurde ein Vorschlag im System aufgenommen.\n"
                 + "Das Ansuchen: " + description;
        return s;
    }
    
    public static M_AUT getInstance()
    {
      if (instance == null)
        instance = new M_AUT();

      return instance;
    }
    
    public String getProposalStatusChangeText(String vocabularyName, String contentType, String description, String statusFrom, String statusTo, String reason){
    
        String s = "Zum folgendem Vorschlag, gab es eine Status�nderung: \n\n"
                 + "Terminologie: " + vocabularyName + " (" + contentType + ")\n\n"
                 + "Das Ansuchen: " + description + "\n\n"
                 + "Status�nderung von " + "\"" + statusFrom + "\"" + " auf " + "\"" + statusTo + "\"" + "\n\n"
                 + "Grund der �nderung: " + reason + "\n";
        return s;
    }
}
