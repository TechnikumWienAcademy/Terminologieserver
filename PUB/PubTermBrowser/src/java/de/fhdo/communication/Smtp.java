/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.communication;

import de.fhdo.collaboration.db.DBSysParam;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author PU
 */
public class Smtp {
    
    public static String sendMail(String[] sToAdr, String sSubject, String sText) 
    {
     
        Properties props = new Properties();
        String mailHost = DBSysParam.instance().getStringValue("mail_host", null, null);
        String mailPort = DBSysParam.instance().getStringValue("mail_port", null, null);
        String mailAuth = DBSysParam.instance().getStringValue("mail_auth", null, null);
        String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
        String password = DBSysParam.instance().getStringValue("mail_password", null, null);
        String mailSSL = DBSysParam.instance().getStringValue("mail_ssl_enable", null, null);
        String startTLS = DBSysParam.instance().getStringValue("mail_starttls", null, null);
      
     props.put("mail.smtp.host", mailHost);
     props.put("mail.stmp.user", mailSender);          
     
     if(mailSSL.equals("true")){
        //If you want you use TLS 
        props.put("mail.smtp.auth", mailAuth);
        props.put("mail.smtp.starttls.enable", startTLS);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.socketFactory.port", mailPort);
        props.put("mail.smtp.socketFactory.class",
                               "javax.net.ssl.SSLSocketFactory");
        System.out.println("Mail Props: " 
                                    + " " + mailHost 
                                    + " " + mailSender
                                    + " " + mailAuth
                                    + " " + startTLS
                                    + " " + password
                                    + " " + mailPort);
     }
     props.put("mail.smtp.port", mailPort);
     
     Session session = Session.getInstance(props, new Authenticator() {
          @Override
               protected PasswordAuthentication getPasswordAuthentication() {
                  String username = DBSysParam.instance().getStringValue("mail_user", null, null);
                  String password = DBSysParam.instance().getStringValue("mail_password", null, null);
               return new PasswordAuthentication(username,password); 
               }
        });
     
      
      MimeMessage msg = new MimeMessage(session);
         try {
           msg.setFrom(new InternetAddress(mailSender));
           Address[] addr = new Address[sToAdr.length];
           for(int i=0;i<sToAdr.length;i++){
           
               addr[i] = new InternetAddress(sToAdr[i]);
           }
           msg.setRecipients(MimeMessage.RecipientType.TO, addr);
           msg.setSubject(sSubject,"ISO-8859-1");
           msg.setText(sText, "ISO-8859-1");
           Transport transport = session.getTransport("smtps");
           transport.send(msg);
           System.out.println("E-mail sent !");
            }   catch(Exception exc) {
                return exc.getMessage();
             }
         return "";
    }
}