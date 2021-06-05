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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.types.LogoutRequestType;
import de.fhdo.terminologie.ws.authorization.types.LogoutResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Logout
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public LogoutResponseType Logout(LogoutRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== Logout gestartet ======");
    
    // Return-Informationen anlegen
    LogoutResponseType response = new LogoutResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if(validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    
    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    if (parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }

    

    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      
      
      if(loggedIn == false)  // TODO funktioniert nicht
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, damit Sie sich ausloggen können!");
          Security.checkForDeadSessions(hb_session);
          hb_session.getTransaction().commit();
          hb_session.close();
          return response;
        }
      
      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // prüfen, ob Session existiert
        Security.checkForDeadSessions(hb_session);
        Session session = Security.getSession(hb_session, parameter.getLogin());
        
        if(session != null)
        {
          // Session löschen
          session.setTermUser(null); 
          hb_session.delete(session);
          
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Logout erfolgreich");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Benutzer ist nicht eingeloggt");
        }
        
        // Hibernate-Block wird in 'finally' geschlossen, erst danach
        // Auswertung der Daten
        // Achtung: hiernach können keine Tabellen/Daten mehr nachgeladen werden
        hb_session.getTransaction().commit();
      }
      catch(Exception e)
      {
        hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'Logout', Hibernate: " + e.getLocalizedMessage());
        
        logger.error("Fehler bei 'Logout', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'Logout': " + e.getLocalizedMessage());
      
      logger.error("Fehler bei 'Logout': " + e.getLocalizedMessage());
    }

    return response;
  }
  
  private boolean validateParameter(
          LogoutRequestType Request,
          LogoutResponseType Response)
  {
    boolean erfolg = true;

    LoginType login = Request.getLogin();

    if (login == null)
    {
      Response.getReturnInfos().setMessage("LoginType darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      if (login.getSessionID() == null || login.getSessionID().length() == 0)
      {
        Response.getReturnInfos().setMessage("SessionID darf nicht NULL sein!");
        erfolg = false;
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
