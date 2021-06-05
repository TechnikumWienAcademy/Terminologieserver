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
package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class UpdateConceptAssociationStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(UpdateConceptAssociationStatusRequestType parameter)
  {
    return UpdateConceptAssociationStatus(parameter, null);
  }

  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(UpdateConceptAssociationStatusRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== UpdateConceptAssociationStatus gestartet ======");

    boolean createHibernateSession = (session == null);

    // Return-Informationen anlegen
    UpdateConceptAssociationStatusResponseType response = new UpdateConceptAssociationStatusResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin(), session);
      loggedIn = loginInfoType != null;
    }

    // TODO Lizenzen prüfen (?)

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return response;
    }

    try
    {
      long associationId = 0;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;
      

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
      }
      else
      {
        hb_session = session;
        //hb_session.getTransaction().begin();
      }


      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        CodeSystemEntityVersionAssociation association_param = parameter.getCodeSystemEntityVersionAssociation();
        long id = association_param.getId();

        CodeSystemEntityVersionAssociation association = (CodeSystemEntityVersionAssociation) hb_session.get(CodeSystemEntityVersionAssociation.class, id);
        association.setStatus(parameter.getCodeSystemEntityVersionAssociation().getStatus());
        association.setStatusDate(new Date());

        hb_session.update(association);
        
        if(createHibernateSession)
            hb_session.getTransaction().commit();
        
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Status der Beziehung aktualisiert.");
      }
      catch (Exception e)
      {
        if(createHibernateSession)
          hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptAssociationStatus', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateConceptAssociationStatus', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        if (createHibernateSession)
          hb_session.close();
      }

      

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptAssociationStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateConceptAssociationStatus': " + e.getLocalizedMessage());
    }

    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   * 
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(UpdateConceptAssociationStatusRequestType Request,
                                    UpdateConceptAssociationStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystemEntityVersionAssociation association = Request.getCodeSystemEntityVersionAssociation();

    if (association == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      if(Definitions.STATUS_CODES.isStatusCodeValid(association.getStatus()) == false)
      {
        Response.getReturnInfos().setMessage(
          "Der Status-Code ist kein gültiger Code. Folgende Werte sid zulässig: " + Definitions.STATUS_CODES.readStatusCodes());
        erfolg = false;
      }
      
      if (association.getId() == null
        || association.getId() == 0)
      {
        Response.getReturnInfos().setMessage(
          "Es muss eine ID der Beziehung angegeben sein!");
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
