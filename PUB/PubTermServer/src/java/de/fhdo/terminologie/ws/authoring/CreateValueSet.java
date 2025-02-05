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
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class CreateValueSet
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  
  public CreateValueSetResponseType CreateValueSet(CreateValueSetRequestType parameter)
  {
    return CreateValueSet(parameter, null);
  }

  public CreateValueSetResponseType CreateValueSet(CreateValueSetRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateValueSet gestartet ======");

    boolean createHibernateSession = (session == null);
    logger.debug("createHibernateSession: " + createHibernateSession);

    // Return-Informationen anlegen
    CreateValueSetResponseType response = new CreateValueSetResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter pr�fen
    if (validateParameter(parameter, response) == false)
    {
      logger.debug("Parameter falsch");
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt f�r jeden Webservice)    
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true, session) == false)
      {
        logger.warn("Nicht eingeloggt!");
        return response;
      }
    }

    try
    {
      // Hibernate-Block, Session �ffnen
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

      // Die R�ckgabevariablen erzeugen
      ValueSet vs_return = new ValueSet();
      ValueSetVersion vsv_return = new ValueSetVersion();

      // ValueSet und ValueSetVersion zum Speichern vorbereiten          
      ValueSet vs = parameter.getValueSet();  // bereits gepr�ft, ob null

      // ValueSet anpassen und alle Attribute die nicht teil der Eingabe sind oder noch berechnet werden auf "null" setzen bzw f�llen
      vs.setCurrentVersionId(null);
      
      if(vs.getStatus() == null)
      {
        vs.setStatus(Definitions.STATUS_CODES.INACTIVE.getCode());
      }
      vs.setStatusDate(new java.util.Date());

      //ValueSetVersion vsv = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];  // einfach m�glich, da bereits gepr�ft ist, ob die Version existiert
      ValueSetVersion vsv = null;
      if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() > 0)
        vsv = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];

      // ValueSetVersion anpassen und alle Attribute die nicht teil der Eingabe sind oder noch berechnet werden auf "null" setzen bzw f�llen
      if (vsv == null)
      {
        logger.debug("Neue Value Set-Version wird erstellt");
        vsv = new ValueSetVersion();
        vsv.setName(sdf.format(new Date()));
        vsv.setValidityRange(238l);
      }

      if(vsv.getName() == null || vsv.getName().length() <= 0)
          vsv.setName(sdf.format(new Date()));
      
      vsv.setConceptValueSetMemberships(null);
      vsv.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum
      
      if(vsv.getStatus() == null){
        vsv.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
      }
      vsv.setStatusDate(new java.util.Date()); // Aktuelles Datum      

      try
      { // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern        
        // 1. pr�fen, ob ValueSet bereits vorhanden ist und nur die Version neu ist
        ValueSet vs_db = null;
        // versuche vs aus der DB zu laden und in vs_db zu speichern; prevVersionID = currentVersionID
        if (vs.getId() != null && vs.getId() > 0)
        {
          vs_db = (ValueSet) hb_session.get(ValueSet.class, vs.getId());
          vsv.setPreviousVersionId(vs_db.getCurrentVersionId());
        }

        // falls vs noch nicht in DB vorhanden ist
        if (vs_db == null)
        {
          vs.setValueSetVersions(null);

          // Neues ValueSet in der DB speichern. Dabei bekommt das Javaobjekt auch gleichzeitig eine ID zugewiesen
          hb_session.save(vs);

          // vsv ist das neue ValueSetVersion Objekt und bekommt ein neues ValueSet zugewiesen
          vsv.setValueSet(vs);

          // Neue ValueSet Version in der DB speichern.
          hb_session.save(vsv);

          // ValueSet mit CurrentVersion aktualisieren
          vs.setCurrentVersionId(vsv.getVersionId());

          // Änderung in DB speichern
          hb_session.update(vs);

          // Antwort setzen (unter anderem neue ID)
          vs_return.setId(vs.getId());
          vs_return.setCurrentVersionId(vs.getCurrentVersionId());
          if(vs.getName() != null)
            vs_return.setName(vs.getName());
          vsv_return.setVersionId(vsv.getVersionId());
          vsv_return.setInsertTimestamp(vsv.getInsertTimestamp());
          if(vsv.getName() != null)
              vsv_return.setName(vsv.getName());
        }
        else
        {
          // ValueSet existiert bereits, Version erstellen
          vsv.setValueSet(new ValueSet());
          vsv.getValueSet().setId(vs_db.getId());

          // Description soll in der neuen Version ge�ndert werden k�nnen
          if (vs.getDescription() != null && vs.getDescription().length() > 0)
            vs_db.setDescription(vs.getDescription());
          
          // Description soll in der neuen Version ge�ndert werden k�nnen
          if (vs.getDescriptionEng() != null && vs.getDescriptionEng().length() > 0)
            vs_db.setDescriptionEng(vs.getDescriptionEng());
          
          // Description soll in der neuen Version ge�ndert werden k�nnen
          if (vs.getWebsite()!= null && vs.getWebsite().length() > 0)
            vs_db.setWebsite(vs.getWebsite());
          
          // In DB speichern damit vsv eine ID bekommt
          hb_session.save(vsv);

          // ValueSet mit CurrentVersion aktualisieren
          vs_db.setCurrentVersionId(vsv.getVersionId());
          hb_session.update(vs_db);

          // Antwort setzen (unter anderem neue ID)
          vs_return.setId(vs_db.getId());
          vs_return.setCurrentVersionId(vs_db.getCurrentVersionId());
          vs_return.setName(vs_db.getName());
          vs_return.setAutoRelease(vs_db.getAutoRelease());
          vsv_return.setVersionId(vsv.getVersionId());
          vsv_return.setInsertTimestamp(vsv.getInsertTimestamp());
          vsv_return.setName(vsv.getName());
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateValueSet', Hibernate: " + e.getLocalizedMessage());
        logger.error(response.getReturnInfos().getMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschlie�en
        if (createHibernateSession)
        {
          if (vs_return.getId() > 0 && vsv_return.getVersionId() > 0)
          {
            hb_session.getTransaction().commit();
          }
          else
          {
            // Änderungen nicht erfolgreich
            logger.warn("[CreateValueSets.java] Änderungen nicht erfolgreich, vs_return.id: "
                    + vs_return.getId() + ", vsv_return.versionId: " + vsv_return.getVersionId());

            hb_session.getTransaction().rollback();
          }
          hb_session.close();
        }
      }

      // Antwort zusammenbauen
      vs_return.setValueSetVersions(new HashSet<ValueSetVersion>());
      vs_return.getValueSetVersions().add(vsv_return);
      response.setValueSet(vs_return);

      if (vs_return.getId() > 0 && vsv_return.getVersionId() > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("ValueSet erfolgreich erstellt");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateValueSet': " + e.getLocalizedMessage());// + " ; M�gliche Ursache: Verwendete Id(" + Long.toString(parameter.getValueSet().getId()) +  ") nicht in Datenbank.");           
      logger.error(response.getReturnInfos().getMessage());

      e.printStackTrace();
    }
    return response;
  }

  private boolean validateParameter(CreateValueSetRequestType request, CreateValueSetResponseType response)
  {
    boolean isValid = true;

    ValueSet ValueSet = request.getValueSet();
    if (ValueSet == null)
    {
      logger.debug("ValueSet darf nicht NULL sein!");
      response.getReturnInfos().setMessage("ValueSet darf nicht NULL sein!");
      isValid = false;
    }
    else
    {
      if (ValueSet.getName() == null || ValueSet.getName().length() == 0)
      {
        if (ValueSet.getId() == null || ValueSet.getId() == 0)
        {
          logger.debug("Es muss ein Name f�r das ValueSet angegeben sein!");
          response.getReturnInfos().setMessage("Es muss ein Name f�r das ValueSet angegeben sein!");
          isValid = false;
        }
      }

      /*Set<ValueSetVersion> vsvSet = ValueSet.getValueSetVersions();
       if (vsvSet != null)
       {
       if (vsvSet.size() > 1)
       {
       response.getReturnInfos().setMessage("Die ValueSet-Version-Liste darf maximal einen Eintrag haben!");
       isValid = false;
       }
       else if (vsvSet.size() == 1)
       {
       ValueSetVersion vsv = (ValueSetVersion) vsvSet.toArray()[0];

       if (vsv.getVersionId() != null && vsv.getVersionId() > 0)
       {
       response.getReturnInfos().setMessage("Die ID f�r eine neue ValueSet-Version darf nicht vergeben sein!");
       isValid = false;
       }
       }
       }*/
    }

    if (isValid == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return isValid;
  }
}