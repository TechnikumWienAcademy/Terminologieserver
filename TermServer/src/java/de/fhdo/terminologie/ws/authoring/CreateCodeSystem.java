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
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateCodeSystem
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateCodeSystemResponseType CreateCodeSystem(CreateCodeSystemRequestType parameter)
  {
    return CreateCodeSystem(parameter, null);
  }

  /**
   * Erstellt ein neues CodeSystem mit den angegebenen Parametern
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public CreateCodeSystemResponseType CreateCodeSystem(CreateCodeSystemRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateCodeSystems gestartet ======");

    boolean createHibernateSession = (session == null);

    // Return-Informationen anlegen
    CreateCodeSystemResponseType response = new CreateCodeSystemResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)    
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
        return response;
    }

    try
    {
      /* 
       * Das zurückgegebene CS und die CSV enthalten nur die CS-id, CS-currentVersionId 
       * und CSV-versionId. Alle anderen Angaben wie name, source, etc werden 
       * nicht als Antwort zurückgegeben.
       */
      CodeSystem cs_return = new CodeSystem();
      CodeSystemVersion csv_return = new CodeSystemVersion();

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;
      org.hibernate.Transaction tx = null;

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

      // CodeSystem und CodeSystem-Version zum Speichern vorbereiten
      CodeSystem cs_parameter = parameter.getCodeSystem();
      CodeSystemVersion csv_parameter = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];  // einfach möglich, da bereits geprüft ist, ob die Version existiert

      // CodeSystem anpassen
      cs_parameter.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum
      cs_parameter.setCurrentVersionId(null);

      // CodeSystem-Version anpassen
      csv_parameter.setCodeSystemVersionEntityMemberships(null);
      csv_parameter.setLicencedUsers(null);
      csv_parameter.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum
      
      if(csv_parameter.getStatus() == null)
      {
        csv_parameter.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
      }
      csv_parameter.setStatusDate(new java.util.Date()); // Aktuelles Datum

      if (csv_parameter.getUnderLicence() == null)
        csv_parameter.setUnderLicence(false);

      csv_parameter.setPreviousVersionId(null);

      // LicenceTypes
      Set<LicenceType> licenceTypes = csv_parameter.getLicenceTypes();
      csv_parameter.setLicenceTypes(null);

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // CodeSystem in der Datenbank speichern

        // 1. prüfen, ob CodeSystem bereits vorhanden ist und nur die Version neu ist
        CodeSystem cs_db = null;
        if (cs_parameter.getId() != null && cs_parameter.getId() > 0)
        {
          // CodeSystem existiert bereits
          cs_db = (CodeSystem) hb_session.get(CodeSystem.class, cs_parameter.getId());

          // vorherige Version speichern
          if (cs_db != null)
            csv_parameter.setPreviousVersionId(cs_db.getCurrentVersionId());
        }

        // CodeSystem existiert noch nicht
        if (cs_db == null)
        {
          cs_parameter.setCodeSystemVersions(null);

          // CodeSystem in DB speichern damit es eine Id erhält
          hb_session.save(cs_parameter);

          // CodeSystemVersion bekommt ein CS mit der gerade neu erhaltenen Id
          csv_parameter.setCodeSystem(new CodeSystem());
          csv_parameter.getCodeSystem().setId(cs_parameter.getId());

          // CodeSystemVersion spiechern damit es eine Id bekommt
          hb_session.save(csv_parameter);

          // CurrentVersion des CodeSystem setzen und erneut speichern
          cs_parameter.setCurrentVersionId(csv_parameter.getVersionId());
          hb_session.update(cs_parameter);

          // Antwort setzen (unter anderem neue ID)
          cs_return.setId(cs_parameter.getId());
          cs_return.setCurrentVersionId(cs_parameter.getCurrentVersionId());
          csv_return.setVersionId(csv_parameter.getVersionId());
        }
        else // CodeSystem existiert bereits, Version erstellen
        {
          csv_parameter.setCodeSystem(new CodeSystem());
          csv_parameter.getCodeSystem().setId(cs_db.getId());
          csv_parameter.setValidityRange(238l);
          
          // CSV in DB speichern um neue Id zu erhalten
          hb_session.save(csv_parameter);

          // CodeSystem mit CurrentVersion aktualisieren und speichern
          cs_db.setCurrentVersionId(csv_parameter.getVersionId());
          hb_session.update(cs_db);

          // Antwort setzen (unter anderem neue ID)
          cs_return.setId(cs_db.getId());
          cs_return.setCurrentVersionId(cs_db.getCurrentVersionId());
          csv_return.setVersionId(csv_parameter.getVersionId());
          csv_return.setName(csv_parameter.getName());
        }

        // LicenceTypes speichern
        if (licenceTypes != null && csv_return.getVersionId() > 0)
        {
          Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
          while (itLicenceType.hasNext())
          {
            LicenceType lt = itLicenceType.next();
            lt.setCodeSystemVersion(new CodeSystemVersion());
            lt.getCodeSystemVersion().setVersionId(csv_return.getVersionId());
            lt.setLicencedUsers(null);
            hb_session.save(lt);
          }
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateCodeSystems', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'CreateCodeSystems', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          if (cs_return.getId() > 0 && csv_return.getVersionId() > 0)
          {
            hb_session.getTransaction().commit();
          }
          else
          {
            // Ã„nderungen nicht erfolgreich
            logger.warn("[CreateCodeSystems.java] Ã„nderungen nicht erfolgreich, cs_return.id: "
                    + cs_return.getId() + ", csv_return.versionId: " + csv_return.getVersionId());

            hb_session.getTransaction().rollback();
          }
          hb_session.close();
        }
      }

      // Antwort zusammenbauen
      cs_return.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
      cs_return.getCodeSystemVersions().add(csv_return);
      //Matthias: name added
      cs_return.setName(parameter.getCodeSystem().getName());
      cs_return.setAutoRelease(parameter.getCodeSystem().getAutoRelease());
      response.setCodeSystem(cs_return);

      if (cs_return.getId() != null && cs_return.getId() > 0 && csv_return.getVersionId() != null && csv_return.getVersionId() > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("CodeSystem erfolgreich erstellt");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateCodeSystems': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'CreateCodeSystems': " + e.getLocalizedMessage());
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
  private boolean validateParameter(CreateCodeSystemRequestType Request, CreateCodeSystemResponseType Response)
  {
    boolean erfolg = true;
    String sErrorMessage = "";

    if (Response == null)
    {
      sErrorMessage = "Kein Responseparameter vorhanden!";
      erfolg = false;
    }
    if (Request == null)
    {
      sErrorMessage = "Kein Requestparameter angegeben!";
      erfolg = false;
    }

    if (erfolg)
    {
      CodeSystem cs = Request.getCodeSystem();
      if (cs == null)
      {
        sErrorMessage = "CodeSystem darf nicht NULL sein!";
        erfolg = false;
      }
      else
      {
        if (cs.getName() == null || cs.getName().isEmpty())
        {
          sErrorMessage = "Es muss ein Name für das CodeSystem angegeben werden!";
          erfolg = false;
        }
        Set<CodeSystemVersion> csvSet = cs.getCodeSystemVersions();
        if (csvSet == null || csvSet.isEmpty() || csvSet.size() > 1)
        {
          sErrorMessage = "Es muss genau eine CodeSystem-Version angegeben werden!";
          erfolg = false;
        }
        else
        {
          CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];
          if (csv == null)
          {
            sErrorMessage = "CodeSystem-Version fehlerhaft!";
            erfolg = false;
          }
          else
          {
            if (csv.getName() == null || csv.getName().isEmpty())
            {
              sErrorMessage = "Es muss ein Name für die CodeSystem-Version angegeben sein!";
              erfolg = false;
            }
//                    if(csv.getUnderLicence() == null){
//                        sErrorMessage = "Es muss angegeben werden, ob die CodeSystem-Version lizensiert ist oder nicht!";
//                        erfolg = false;
//                    }
//                    if (csv.getVersionId() != null){
//                        sErrorMessage += eol + "Es darf keine Id für die CodeSystem-Version vergeben sein!";
//                        erfolg = false;
//                    }

            // Check for licenceTypes (and their typeTxt)
            Set<LicenceType> ltSet = csv.getLicenceTypes();
            if (ltSet != null)
            {
              Iterator<LicenceType> itLt = ltSet.iterator();
              LicenceType lt;
              while (itLt.hasNext())
              {
                if (itLt.next().getTypeTxt().isEmpty())
                {
                  sErrorMessage = "At least one licenceType has no typeTxt!";
                  erfolg = false;
                  break;
                }
              }
            }
          }
        }
      }
    }
    if (erfolg == false)
    {
      Response.getReturnInfos().setMessage(sErrorMessage);
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;
  }
}