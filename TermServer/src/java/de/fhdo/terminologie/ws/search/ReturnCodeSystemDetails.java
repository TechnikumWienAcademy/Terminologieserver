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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)/ warends
 */
public class ReturnCodeSystemDetails
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Liefert alle Informationen zu einem Vokabular
   *
   * @param parameter CodeSystem(Vokabular), VersionId
   * @return Ergebnis des Webservices, gibt Details des angegebenen CodeSystems
   * und der angegebenen VersionId zurück; wurde keine VersionId angegeben
   * werden Details zu allen Versionen ausgegeben
   *
   */
  //3.2.17 added loginalreadychecked
  public ReturnCodeSystemDetailsResponseType ReturnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter)
  {

    if (logger.isInfoEnabled())
    {
      logger.info("====== ReturnCodeSystemDetails gestartet ======");
    }

    // Return-Informationen anlegen
    ReturnCodeSystemDetailsResponseType response = new ReturnCodeSystemDetailsResponseType();
    response.setReturnInfos(new ReturnType());
    
    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    //3.2.17 added loginalreadychecked check
    if (parameter != null && !parameter.getLoginAlreadyChecked() && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }
    
    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }

    try
    {
      java.util.List<CodeSystem> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct cs from CodeSystem cs";
        hql += " join fetch cs.codeSystemVersions csv";

        //  ???

        if (loggedIn)
        {
          hql += " left outer join csv.licencedUsers lu";
        }

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getCodeSystem() != null)
        {
          // Hier alle Parameter aus der Cross-Reference einfügen
          // addParameter(String Prefix, String DBField, Object Value)

          parameterHelper.addParameter("cs.", "id", parameter.getCodeSystem().getId());

          if (parameter.getCodeSystem().getCodeSystemVersions() != null
                  && parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
          {
            CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

            parameterHelper.addParameter("csv.", "versionId", csv.getVersionId());

          }
        }

        if (loggedIn == false)
        {
          // hier: immer nur aktive Vokabulare abrufen
          parameterHelper.addParameter("csv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        
          // ohne Login keine Vokabulare mit Lizenzen abrufen
          parameterHelper.addParameter("csv.", "underLicence", 0);
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        if (loggedIn)
        {
          // jetzt auf eine gültige Lizenz prüfen
          // muss manuell hinzugefügt werden (für Helper zu komplex, wg. OR)
          hql += " AND ";
          hql += " (csv.underLicence = 0 OR ";
          hql += " (lu.validFrom < '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += " AND lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += " AND lu.id.codeSystemVersionId=csv.versionId";
          hql += " AND lu.id.userId=" + loginInfoType.getTermUser().getId();
          hql += " ))";
        }
        
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: set readOnly
				q.setReadOnly(true);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        list = (java.util.List<CodeSystem>) q.list();

        if (list != null)
        {
          Iterator<CodeSystem> iterator = list.iterator();

          if (iterator.hasNext())
          {
            CodeSystem cs = iterator.next();

            if (cs.getCodeSystemVersions() != null)
            {
              Iterator<CodeSystemVersion> iteratorVV = cs.getCodeSystemVersions().iterator();

              while (iteratorVV.hasNext())
              {
                CodeSystemVersion csv = iteratorVV.next();

                // Nicht anzuzeigende Beziehungen null setzen

                if (csv.getLicenceTypes() != null)
                {
                  Iterator<LicenceType> iteratorLT = csv.getLicenceTypes().iterator();
                  while (iteratorLT.hasNext())
                  {
                    LicenceType lt = iteratorLT.next();
                    lt.setCodeSystemVersion(null);
                    lt.setLicencedUsers(null);
                  }
                }

                csv.setLicencedUsers(null);
                csv.setCodeSystemVersionEntityMemberships(null);
                csv.setCodeSystem(null);
              }
            }

            for (MetadataParameter md : cs.getMetadataParameters())
            {
              md.setCodeSystemMetadataValues(null);
              md.setCodeSystem(null);
              md.setValueSet(null);
              md.setValueSetMetadataValues(null);
            }

            cs.setDomainValues(null);

            // bereinigte Liste der Antwort beifügen
            response.setCodeSystem(cs);

            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("CodeSystemeDetails erfolgreich gelesen");
            response.getReturnInfos().setCount(1);
          }
          else
          {
            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Kein CodeSysteme gefunden. Bitte beachten Sie, dass Sie für Codesysteme, welche einen anderen Status als 1 haben, am Terminologieserver angemeldet sein müssen.");
            response.getReturnInfos().setCount(0);
          }


        }


        // Hibernate-Block wird in 'finally' geschlossen, erst danach
        // Auswertung der Daten
        // Achtung: hiernach können keine Tabellen/Daten mehr nachgeladen werden
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnCodeSystemDetails', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnCodeSystemDetails', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }

      // Ergebnis auswerten
      // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
      // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnCodeSystemDetails': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(ReturnCodeSystemDetailsRequestType Request, ReturnCodeSystemDetailsResponseType Response)
  {
    boolean erfolg = true;

    //3.2.17 added second check
    if(Request.getLogin() != null && !Request.getLoginAlreadyChecked())
    {
      if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
      {
        Response.getReturnInfos().setMessage("Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
        erfolg = false;
      }
    }


    if (Request.getCodeSystem() == null)
    {
      Response.getReturnInfos().setMessage(
              "Es muss ein CodeSystem(Vokabular) angegeben sein!");
      erfolg = false;
    }

    /*if (Request.getCodeSystem() != null)
     {
     if (Request.getCodeSystem().getId() == null || Request.getCodeSystem().getId() <= 0)
     {
     Response.getReturnInfos().setMessage(
     "Es muss eine ID für das CodeSystem(Vokabular) angegeben sein!");
     erfolg = false;
     }
     }*/
    else if (Request.getCodeSystem() != null && Request.getCodeSystem().getCodeSystemVersions() != null)
    {
      if (Request.getCodeSystem().getCodeSystemVersions().size() > 1)
      {
        Response.getReturnInfos().setMessage(
                "Es darf maximal eine CodeSystemVersion angegeben sein!");
        erfolg = false;
      }
      else
      {
        if (Request.getCodeSystem().getCodeSystemVersions().size() != 0)
        {
          CodeSystemVersion csv = (CodeSystemVersion) Request.getCodeSystem().getCodeSystemVersions().toArray()[0];
          if (csv.getVersionId() == null || csv.getVersionId() <= 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss eine ID für die CodeSystem-Version angegeben sein!");
            erfolg = false;
          }

          // folgende Parameter dürfen nicht angegeben sein:


          if (csv.getPreviousVersionId() != null)
          {
            Response.getReturnInfos().setMessage(
                    "PreviousVersionId darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getName() != null)
          {
            Response.getReturnInfos().setMessage(
                    "Name darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getStatus() != null)
          {
            Response.getReturnInfos().setMessage(
                    "Status darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getStatusDate() != null)
          {
            Response.getReturnInfos().setMessage(
                    "StautsDate darf nicht angegeben sein!");
            erfolg = false;
          }
          if (csv.getReleaseDate() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ReleaseDate darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getExpirationDate() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ExpirationDate darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getSource() != null)
          {
            Response.getReturnInfos().setMessage(
                    "Source darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getDescription() != null)
          {
            Response.getReturnInfos().setMessage(
                    "Description darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getPreferredLanguageId() != null)
          {
            Response.getReturnInfos().setMessage(
                    "PreferredLanguageId darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getValidityRange() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ValidityRange darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getOid() != null)
          {
            Response.getReturnInfos().setMessage(
                    "Oid darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getLicenceHolder() != null)
          {
            Response.getReturnInfos().setMessage(
                    "LicenceHolde darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getUnderLicence() != null)
          {
            Response.getReturnInfos().setMessage(
                    "UnderLicence darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getInsertTimestamp() != null)
          {
            Response.getReturnInfos().setMessage(
                    "InsertTimestamp darf nicht angegeben sein!");
            erfolg = false;
          }

          if (csv.getLicenceTypes() != null && csv.getLicenceTypes().size() > 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es darf keine Licence angegeben sein!");
            erfolg = false;
          }
        }
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
