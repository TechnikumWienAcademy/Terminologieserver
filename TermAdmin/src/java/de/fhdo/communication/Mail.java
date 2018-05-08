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
package de.fhdo.communication;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.DBSysParam;


/**
 *
 * @author Robert Mützner
 */
public class Mail
{
    
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private static String getAppLink()
  {
    return DBSysParam.instance().getStringValue("weblink", null, null);
  }
  
  private static String getAppLinkCollaboration()
  {
    return de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("weblink", null, null);
  }
  
  private static String getTermBrowserLink(){
      return DBSysParam.instance().getStringValue("termbrowserlink", null, null);
  }

  public static String sendNewPassword(String Benutzername, String Passwort, String BenutzerEmail)
  {
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      String betreff = "", text = "";

      //String link = getAppLink() + "index.zul?user=" + Benutzername;
      

        betreff = "Terminologieserver - Passwort";
      
        text = "Ihr Passwort lautet " + Passwort + "\nBitte ändern Sie dieses nach der ersten Anmeldung über Benutzer->Bearbeiten oder durch einen Klick auf den Benutzer unten links im Fenster!\n"
             + "Ihr Benutzername ist '" + Benutzername + "'\n\n"
             /* + "Ihr Aktivierungscode '" + ActivationMD5 + "'\n\n" */
             + "Unter folgendem Link erreichen Sie den Terminologieserver: "
             + getTermBrowserLink() + "\n\n"
             + "Kopieren Sie den kompletten Link notfalls in Ihren Browser, falls ein Klick auf den Link nicht funktioniert.";
      
      //text += LeseFusszeile();

      Smtp smtp = new Smtp();
      String[] adr = new String[1];
      adr[0]= BenutzerEmail;
      smtp.sendMail(adr, betreff, M_AUT.MAIL_START + text + M_AUT.getInstance().getMailFooter());
    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);
      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage());

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return "";
  }

  public static String sendMailNewUser(String Benutzername, String Passwort, String ActivationMD5, String BenutzerEmail)
  {
    String s = "";
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      String betreff = "", text = "";

      String link = getAppLink() + "/index.zul?reg=" + ActivationMD5
              + "\u0026user=" + Benutzername;

      betreff = "Terminologieserver: Verwaltungsumgebung - Registrierung";
      text =    "Sie wurden erfolgreich als neuer Benutzer registriert.\n\n"
              + "Ihr Passwort lautet " + Passwort + "\nBitte ändern Sie dieses nach der ersten Anmeldung über Benutzer-Details unten links im Fenster!\n"
              + "Ihr Benutzername ist '" + Benutzername + "'\n\n"
              /* + "Ihr Aktivierungscode '" + ActivationMD5 + "'\n\n" */
              + "Bestätigen Sie Ihre Email-Adresse unter folgendem Link: "
              + link + "\n\n"
              + "Dieser Link ist 72h gültig! Danach kann dieser nicht mehr verwendet werden!\n\n"
              + "Kopieren Sie den kompletten Link notfalls in Ihren Browser, falls ein Klick auf den Link nicht funktioniert.\n\n"
              + "Die Anwendung selbst finden Sie unter diesem Link: " + getTermBrowserLink();

      //text += LeseFusszeile();
      
      Smtp smtp = new Smtp();
      String[] adr = new String[1];
      adr[0]= BenutzerEmail;
      s = smtp.sendMail(adr,betreff, M_AUT.MAIL_START + text + M_AUT.getInstance().getMailFooter());


    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return s;
  }
  
  public static String sendMailCollaborationNewUser(String Benutzername, String Passwort, String BenutzerEmail, String ActivationMD5)
  {
    String s = "";
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      String betreff = "", text = "";

      String link = getTermBrowserLink() + "/activation.zul?reg=" + ActivationMD5;

      betreff = "Terminologieserver: Kollaborationsumgebung - Registrierung";
      text =    "Sie wurden erfolgreich als neuer Benutzer registriert.\n\n"
              + "Ihr Passwort lautet " + Passwort + "\nBitte ändern Sie dieses nach der ersten Anmeldung über Benutzer-Details unten links im Fenster!\n"
              + "Ihr Benutzername ist '" + Benutzername + "_tadm" + "'\n\n"
              /* + "Ihr Aktivierungscode '" + ActivationMD5 + "'\n\n" */
              + "Bestätigen Sie Ihre Email-Adresse unter folgendem Link: "
              + link + "\n\n"
              + "Dieser Link ist 72h gültig! Danach kann dieser nicht mehr verwendet werden!\n\n"
              + "Kopieren Sie den kompletten Link notfalls in Ihren Browser, falls ein Klick auf den Link nicht funktioniert.\n\n"
              + "Die Anwendung selbst finden Sie unter diesem Link: " + getTermBrowserLink();


      //text += LeseFusszeile();
      
      Smtp smtp = new Smtp();
      String[] adr = new String[1];
      adr[0]= BenutzerEmail;
      s = smtp.sendMail(adr, betreff, M_AUT.MAIL_START + text + M_AUT.getInstance().getMailFooter());
    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailCollaborationNewUser()': " + e.getMessage() + ", " + fehler);

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return s;
  }

  public static String sendMail(String Benutzername, String Betreff, String Text, String BenutzerEmail)
  {
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      Smtp smtp = new Smtp();
      String[] adr = new String[1];
      adr[0]= BenutzerEmail;
      smtp.sendMail(adr, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return "";
  }
  
  public static Boolean sendMailAUT(Collaborationuser user, String Betreff, String Text)
  {
      
    if(user.getSendMail()){     
        try
        {
          if (logger.isDebugEnabled())
            logger.debug("Neue Email an user senden...");

          Smtp smtp = new Smtp();
          String[] adr = new String[1];
            adr[0]= user.getEmail();
          smtp.sendMail(adr, Betreff,  M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

        }
        catch (Exception e)
        {
          String fehler = "";
          for (int i = 0; i < e.getStackTrace().length; ++i)
          {
            fehler += e.getStackTrace()[i].toString() + "\n";
          }
          logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

          return false;
        }
    }
    
    return true;
  }
  
   public static Boolean sendMailAUTMultiUser(String[] mailAddress, String Betreff, String Text)
  {
      
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      Smtp.sendMail(mailAddress, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

    }
    catch (Exception e)
    {
      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return false;
    }

    return true;
  }
   
   public static Boolean sendMailAUT_MultiUser(String[] mailAddress, String Betreff, String Text)
  {
      
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      Smtp.sendMail(mailAddress, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

    }
    catch (Exception e)
    {
      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return false;
    }

    return true;
  }
   
    public static Boolean sendMailAUT(String[] mailAddress, String Betreff, String Text)
    {

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Neue Email an user senden...");
                logger.debug(mailAddress);
                logger.debug(Betreff);
            }

            Smtp.sendMail(mailAddress, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

        }
        catch (Exception e)
        {
            String fehler = "";
            for (int i = 0; i < e.getStackTrace().length; ++i)
            {
                fehler += e.getStackTrace()[i].toString() + "\n";
            }
            logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

            return false;
        }

        return true;
    }
}
