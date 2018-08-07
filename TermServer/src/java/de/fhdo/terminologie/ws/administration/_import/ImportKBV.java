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
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import ehd._001.KeytabsTyp;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.apache.log4j.Logger;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportKBV
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;

  public ImportKBV(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportKBV gestartet ======");

    parameter = _parameter;
  }

  public String importXML(ImportCodeSystemResponseType reponse)
  {
    String s = "";

    int count = 0, countFehler = 0;

    // Hibernate-Block, Session öffnen
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try // try-catch-Block zum Abfangen von Hibernate-Fehlern
    {
      if (createCodeSystem(hb_session) == false)
      {
        // Fehlermeldung
        hb_session.getTransaction().commit();
        hb_session.close();
        return "CodeSystem konnte nicht erstellt werden! CodeSystem und Version existieren möglicherweise schon.";
      }

      // XML-Datei laden
      JAXBContext jc = JAXBContext.newInstance("ehd._001");
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader xsr = factory.createXMLStreamReader(new ByteArrayInputStream(parameter.getImportInfos().getFilecontent()));

      //ByteArrayInputStream input = new ByteArrayInputStream(parameter.getImportInfos().getFilecontent());
      Object o = unmarshaller.unmarshal(xsr);

      logger.debug("Type: " + o.getClass().getCanonicalName());
      logger.debug("Object: " + o.toString());

      JAXBElement<ehd._001.KeytabsTyp> doc = (JAXBElement<ehd._001.KeytabsTyp>) o;
      //ehd._001.KeytabsTyp root = (ehd._001.KeytabsTyp) o;

      // Daten auswerten
      if (doc != null)
      {
        KeytabsTyp root = doc.getValue();

        for (ehd._001.KeytabTyp keytab : root.getKeytab())
        {
          for (ehd._001.Key key : keytab.getKey())
          {
            logger.debug("Code: " + key.getV() + ", Wert: " + key.getDN());

            // Request-Parameter vorbereiten
            CreateConceptRequestType request = new CreateConceptRequestType();

            request.setLogin(parameter.getLogin());
            request.setCodeSystem(parameter.getCodeSystem());
            request.setCodeSystemEntity(new CodeSystemEntity());
            request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

            CodeSystemConcept csc = new CodeSystemConcept();

            csc.setCode(key.getV());
            csc.setIsPreferred(true);
            csc.setTerm(key.getDN());
            //csc.setTermAbbrevation(csv.get("term_abbrevation"));

            if (csc.getCode().length() > 98)
            {
              csc.setCode(csc.getCode().substring(0, 98));
            }

            logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatus(1);
            csev.setIsLeaf(true);

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session);

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              count++;
            }
          }
        }
      }

      //input.close();

      if (count == 0)
      {
        hb_session.getTransaction().rollback();
        reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
      }
      else
      {
        hb_session.getTransaction().commit();
        countImported = count;
        reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
      }
    }
    catch (Exception ex)
    {
      //ex.printStackTrace();
      logger.error(ex.getMessage());
      s = "Fehler beim Import einer KBV XML-Datei: " + ex.getLocalizedMessage();

      try
      {
        //DABACA
        if(!hb_session.getTransaction().wasRolledBack())
            hb_session.getTransaction().rollback();
        logger.info("[ImportCSV.java] Rollback durchgeführt!");
      }
      catch (Exception exRollback)
      {
        logger.info(exRollback.getMessage());
        logger.info("[ImportCSV.java] Rollback fehlgeschlagen!");
      }
    }
    finally
    {
      // Session schließen
      hb_session.close();
    }

    return s;

  }

  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    // TODO zunächst prüfen, ob CodeSystem bereits existiert
    try
    {
      Query q = hb_session.createQuery("from CodeSystem where name=:name");
      q.setParameter("name", parameter.getCodeSystem().getName());
      List<CodeSystem> cs_list = q.list();
      if (cs_list != null && cs_list.size() > 0)
      {
        CodeSystem cs = cs_list.get(0);
        if (cs != null)
        {
          logger.debug("Codesystem existiert bereits, nur neue Version anlegen");
          parameter.getCodeSystem().setId(cs.getId());

          // Version auch prüfen
          q = hb_session.createQuery("from CodeSystemVersion where name=:name");
          q.setParameter("name", ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getName());
          List<CodeSystemVersion> csv_list = q.list();

          if (csv_list != null && csv_list.size() > 0)
          {
            CodeSystemVersion csv = csv_list.get(0);
            if (csv.getStatus() == 1)
            {
              // Fehler, version existiert bereits
              return false;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
    }

    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
    request.setCodeSystem(parameter.getCodeSystem());
    request.setLogin(parameter.getLogin());

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session);

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      return false;
    }
    parameter.setCodeSystem(resp.getCodeSystem());

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return true;
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
}
