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
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import org.hibernate.ObjectNotFoundException;

/**
 *
 * @author Nico Hänsch
 */
public class MaintainConceptAssociation
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   *  Verändert eine Beziehung zwischen 2 Konzepten.
   * 
   * @param parameter
   * @return 
   */
  public MaintainConceptAssociationResponseType MaintainConceptAssociation(MaintainConceptAssociationRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== MaintainConceptAssociation gestartet ======");

    // Return-Informationen anlegen
    MaintainConceptAssociationResponseType response = new MaintainConceptAssociationResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }


    // Login-Informationen auswerten
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }

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
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      // Neue CodeSystemEntityVersionAssociation
      CodeSystemEntityVersionAssociation cseva_New = parameter.getCodeSystemEntityVersionAssociation().get(0);
      CodeSystemEntityVersionAssociation cseva_db = new CodeSystemEntityVersionAssociation();

      try
      { // 2. Try-Catch-Block zum Abfangen von Hibernate-Fehlern 

        //Origianl CSEVA aus DB laden
        //TODO Prüfen, ob Id vorhanden ist (Catch funktioniert nicht richtig, Fehler wird zu spät gefangen)
        try
        {
          cseva_db = (CodeSystemEntityVersionAssociation) hb_session.load(CodeSystemEntityVersionAssociation.class, cseva_New.getId());
        }
        catch (ObjectNotFoundException e)
        {
          // Fehlermeldung an den Aufrufer weiterleiten
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Keine CodeSystemEntityVersionAssociation mit der angegeben ID vorhanden.");
          logger.error(response.getReturnInfos().getMessage());
        }


        // Beziehungen ändern                
        // codeSystemEntityVersionByCodeSystemEntityVersionIdX neu erstellen
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null); -> nicht mehr möglich!
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null); -> nicht mehr möglich!

        // codeSystemEntityVersionByCodeSystemEntityVersionIdX zuweisen -> nicht mehr möglich!
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        //cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(cseva_New.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId());
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        //cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(cseva_New.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId());

        // Attribute ändern
        // cseva_db.setLeftId(cseva_New.getLeftId()); -> nicht mehr möglich!
        //cseva_db.setAssociationKind(cseva_New.getAssociationKind()); -> nicht mehr möglich!

        // AssociationType ändern, falls angegeben
        if (cseva_New.getAssociationType() != null)
        {
          cseva_db.getAssociationType().setCodeSystemEntityVersionId(cseva_New.getAssociationType().getCodeSystemEntityVersionId());
        }

        // prüfen, ob AssociationTypeId auch eine Association ist
        if (hb_session.get(AssociationType.class, cseva_db.getAssociationType().getCodeSystemEntityVersionId()) == null)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Sie müssen eine gültige ID für ein AssociationType angeben. Das Konzept mit der ID '" + cseva_db.getAssociationType().getCodeSystemEntityVersionId() + "' ist kein AssociationType!");

          logger.info("ungültige ID für AssociationType");
        }
        else
        {
          // Beziehung abspeichern
          hb_session.save(cseva_db);
          associationId = cseva_db.getId();
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociation', Hibernate: " + e.getLocalizedMessage());
        logger.error(response.getReturnInfos().getMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (associationId > 0)
        {
          hb_session.getTransaction().commit();
        }
        else
        {
          // Ã„nderungen nicht erfolgreich
          logger.warn("[MaintainConceptAssociation.java] Ã„nderungen nicht erfolgreich");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
      if (associationId > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation erfolgreich geändert. ");
        logger.info(response.getReturnInfos().getMessage());
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociation': " + e.getLocalizedMessage());
      logger.error(response.getReturnInfos().getMessage());
    }
    return response;
  }

  private boolean validateParameter(MaintainConceptAssociationRequestType Request, MaintainConceptAssociationResponseType Response)
  {
    boolean parameterValidiert = true;


    //Prüfen ob Login übergeben wurde (KANN)
    if (Request.getLogin() != null)
    {
      //Prüfen ob Session-ID übergeben wurde (MUSS)
      if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
      {
        Response.getReturnInfos().setMessage("Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
        parameterValidiert = false;
      }
    }


    if (Request.getCodeSystemEntityVersionAssociation() == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein.");
      parameterValidiert = false;
    }
    else
    {
      if (Request.getCodeSystemEntityVersionAssociation().size() > 1)
      {
        Response.getReturnInfos().setMessage("Es muss genau eine CodeSystemEntityVersionAssociation angegeben sein.");
        parameterValidiert = false;
      }
      else
      {
        CodeSystemEntityVersionAssociation csev = Request.getCodeSystemEntityVersionAssociation().get(0);
        if (csev.getId() == null || csev.getId() == 0)
        {
          Response.getReturnInfos().setMessage("Es muss eine ID der Beziehung angegeben sein!");
          parameterValidiert = false;
        }
        else if ((csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() == null
          || csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == null
          || csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == 0))
        {
          Response.getReturnInfos().setMessage("Es muss eine ID für die 1. CodeSystemEntityVersion angegeben sein!");
          parameterValidiert = false;
        }
        else if ((csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() == null
          || csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == null
          || csev.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == 0))
        {
          Response.getReturnInfos().setMessage("Es muss eine ID für die 2. CodeSystemEntityVersion angegeben sein!");
          parameterValidiert = false;
        }
        else if (csev.getLeftId() == null || csev.getLeftId() == 0)
        {
          Response.getReturnInfos().setMessage("Es muss eine LeftId angegeben werden.");
          parameterValidiert = false;
        }
        else if (csev.getAssociationKind() == null || Definitions.isAssociationKindValid(csev.getAssociationKind()) == false)
        {
          Response.getReturnInfos().setMessage("Es muss ein Association-Kind angegeben sein, mögliche Werte: " + Definitions.readAssociationKinds());
          parameterValidiert = false;
        }
        else if (csev.getAssociationType() != null && csev.getAssociationType().getCodeSystemEntityVersionId() == 0)
        {
          Response.getReturnInfos().setMessage("Wenn ein AssociationType angegeben wird muss auch eine codeSystemEntityVersionId angegeben werden.");
          parameterValidiert = false;
        }
      }
    }





    if (parameterValidiert == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return parameterValidiert;
  }
}
