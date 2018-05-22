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

import com.csvreader.CsvReader;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypes;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCSV_FHDo
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;

  public ImportCSV_FHDo(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCSV - FH Dortmund - gestartet ======");

    parameter = _parameter;
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }

  private class RelationMapType
  {

    private long entityID, entityVersionID;

    public RelationMapType(long entityID, long entityVersionID)
    {
      this.entityID = entityID;
      this.entityVersionID = entityVersionID;
    }

    /**
     * @return the entityID
     */
    public long getEntityID()
    {
      return entityID;
    }

    /**
     * @param entityID the entityID to set
     */
    public void setEntityID(long entityID)
    {
      this.entityID = entityID;
    }

    /**
     * @return the entityVersionID
     */
    public long getEntityVersionID()
    {
      return entityVersionID;
    }

    /**
     * @param entityVersionID the entityVersionID to set
     */
    public void setEntityVersionID(long entityVersionID)
    {
      this.entityVersionID = entityVersionID;
    }
  }

  public String importCSV(ImportCodeSystemResponseType reponse)
  {
    String s = "";

    int count = 0, countFehler = 0;

    CsvReader csv;
    try
    {
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);

      //csv = new CsvReader("C:\\Temp\\notfallrel_diagnosen.csv");
      //DABACA
            //csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
            csv = new CsvReader(is, Charset.forName("UTF-8"));
      csv.setDelimiter(';');
      csv.setTextQualifier('"');
      csv.setUseTextQualifier(true);


      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      // Sprachen identifizieren
      // Metadaten identifizieren
      Map<Integer, Long> headerTranslations = new HashMap<Integer, Long>();
      Map<Integer, String> headerMetadata = new HashMap<Integer, String>();
      String[] header = csv.getHeaders();

      for (int i = 0; i < csv.getHeaderCount(); ++i)
      {
        try
        {
          if (header[i].contains("translation_"))
          {
            long languageID = Long.parseLong(header[i].replace("translation_", ""));
            headerTranslations.put(i, languageID);
          }
        }
        catch (Exception e)
        {
        }

        try
        {
          if (header[i].contains("metadata_"))
          {
            String mdName = header[i].replace("metadata_", "");
            headerMetadata.put(i, mdName);
          }
        }
        catch (Exception e)
        {
        }
      }



      // Hibernate-Block, Session �ffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createCodeSystem(hb_session) == false)
        {
          // Fehlermeldung
          tx.commit();
          HibernateUtil.getSessionFactory().close();
          return "CodeSystem konnte nicht erstellt werden!";
        }

        // Metadaten speichern
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (String mdText : headerMetadata.values())
        {
          MetadataParameter mp = new MetadataParameter();
          mp.setParamName(mdText);
          mp.setCodeSystem(parameter.getCodeSystem());
          hb_session.save(mp);

          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher Metadata-Parameter: " + mdText + " mit Codesystem-ID: " + mp.getCodeSystem().getId() + ", MD-ID: " + mp.getId());
        }

        Map<String, RelationMapType> relationMap = new HashMap<String, RelationMapType>();

        while (csv.readRecord())
        {
          CreateConceptAssociationRequestType requestAssociation = null;
          CreateConceptRequestType request = new CreateConceptRequestType();

          request.setLogin(parameter.getLogin());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(csv.get("code"));
          csc.setIsPreferred(true);
          csc.setTerm(csv.get("term"));
          csc.setTermAbbrevation(csv.get("term_abbrevation"));

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          // Weitere Attribute pr�fen
          String s_temp;
          s_temp = csv.get("description");
          if (s_temp != null && s_temp.length() > 0)
            csc.setDescription(s_temp);

          s_temp = csv.get("is_preferred");
          if (s_temp != null)
          {
            if (s_temp.equals("1") || s_temp.equals("true"))
              csc.setIsPreferred(true);
            else
              csc.setIsPreferred(false);
          }


          boolean membershipChanged = false;
          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          s_temp = csv.get("is_axis");
          if (s_temp != null && (s_temp.equals("1") || s_temp.equals("true")))
          {
            membership.setIsAxis(true);
            membershipChanged = true;
          }
          s_temp = csv.get("is_mainclass");
          if (s_temp != null && (s_temp.equals("1") || s_temp.equals("true")))
          {
            membership.setIsMainClass(true);
            membershipChanged = true;
          }


          if (membershipChanged)
          {
            request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);
          }

          // Beziehung pr�fen
          s_temp = csv.get("relation");
          if (s_temp != null && s_temp.length() > 0)
          {
            if (relationMap.containsKey(s_temp))
            {
              RelationMapType mapEntry = relationMap.get(s_temp);

              // Es gibt eine Beziehung zu einem anderen Term
              requestAssociation = new CreateConceptAssociationRequestType();
              requestAssociation.setLogin(parameter.getLogin());
              requestAssociation.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
              requestAssociation.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
              requestAssociation.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(mapEntry.getEntityVersionID());

              s_temp = csv.get("association_kind");
              if (s_temp != null)
                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationKind(Integer.parseInt(s_temp));
              else
                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationKind(2);

              s_temp = csv.get("association_type");
              if (s_temp != null)
              {
                String s_temp2 = csv.get("association_type_reverse");
                String reverse = "";
                if (s_temp2 != null)
                  reverse = s_temp2;

                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationType(CreateAssociationType(s_temp, reverse));
              }
            }

          }

          // Sprachen pr�fen
          if (headerTranslations != null && headerTranslations.size() > 0)
          {
            csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());

            Set<Integer> spalten = headerTranslations.keySet();
            Iterator<Integer> itSpalten = spalten.iterator();

            while (itSpalten.hasNext())
            {
              Integer spalte = itSpalten.next();
              CodeSystemConceptTranslation translation = new CodeSystemConceptTranslation();
              translation.setLanguageId(headerTranslations.get(spalte));
              translation.setTerm(csv.get(spalte));
              csc.getCodeSystemConceptTranslations().add(translation);

              logger.debug("Translation hinzuf�gen: " + translation.getLanguageId() + "," + translation.getTerm());
            }
          }


          if (csc.getCode().length() == 0)
          {
            if (csc.getTerm().length() > 0)
            {
              if (csc.getTerm().length() > 98)
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
            }
          }
          else if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          if (csc.getCode().length() > 0)
          {
            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatus(1);
            csev.setIsLeaf(true);

            // Entity-Version dem Request hinzuf�gen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einf�gen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session);

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              RelationMapType newMapEntry = new RelationMapType(response.getCodeSystemEntity().getId(), response.getCodeSystemEntity().getCurrentVersionId());
              relationMap.put(csc.getCode(), newMapEntry);

              count++;

              if (requestAssociation != null)
              {
                // Beziehung ebenfalls abspeichern
                requestAssociation.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                requestAssociation.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(
                        response.getCodeSystemEntity().getCurrentVersionId());

                // Dienst aufrufen (Beziehung erstellen)
                CreateConceptAssociation cca = new CreateConceptAssociation();
                CreateConceptAssociationResponseType responseAssociation = cca.CreateConceptAssociation(requestAssociation, hb_session);

                if (responseAssociation.getReturnInfos().getStatus() == ReturnType.Status.OK)
                {
                }
                else
                  logger.debug("Beziehung nicht gespeichert: " + responseAssociation.getReturnInfos().getMessage());
              }

              if (headerMetadata != null && headerMetadata.size() > 0)
              {
                // Metadaten einf�gen
                for (Integer spalte : headerMetadata.keySet())
                {
                  String mdValue = csv.get(spalte);
                  if (mdValue != null && mdValue.length() > 0)
                  {
                    CodeSystemMetadataValue csmv = new CodeSystemMetadataValue(mdValue);
                    csmv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                    csmv.getCodeSystemEntityVersion().setVersionId(response.getCodeSystemEntity().getId());
                    csmv.setMetadataParameter(new MetadataParameter());
                    csmv.getMetadataParameter().setId(headerMetadataIDs.get(headerMetadata.get(spalte)));

                    logger.debug("Metadaten einf�gen, MP-ID " + csmv.getMetadataParameter().getId() + ", CSEV-ID " + csmv.getCodeSystemEntityVersion().getVersionId() + ", Wert: " + csmv.getParameterValue());

                    hb_session.save(csmv);
                  }
                }
              }

            }
            else
              countFehler++;

          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }

        }



        if (count == 0)
        {
          tx.rollback();
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          tx.commit();
          countImported = count;
          reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler beim Import einer CSV-Datei: " + ex.getLocalizedMessage();

        try
        {
          tx.rollback();
          logger.info("[ImportCSV.java] Rollback durchgef�hrt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportCSV.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schlie�en
        HibernateUtil.getSessionFactory().close();
      }


    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim Import: " + ex.getLocalizedMessage();
      logger.error(s);
      //ex.printStackTrace();
    }

    return s;
  }

  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    // TODO zun�chst pr�fen, ob CodeSystem bereits existiert
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
  private Map<String, AssociationType> associationTypeMap = null;

  private void initAssociationTypes()
  {
    if (associationTypeMap == null)
    {
      logger.debug("initAssociationTypes()");

      associationTypeMap = new HashMap();
      ListConceptAssociationTypesRequestType request = new ListConceptAssociationTypesRequestType();
      request.setLogin(parameter.getLogin());

      ListConceptAssociationTypes lcat = new ListConceptAssociationTypes();
      ListConceptAssociationTypesResponseType response = lcat.ListConceptAssociationTypes(request);

      if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
      {
        for (int i = 0; i < response.getCodeSystemEntity().size(); ++i)
        {
          CodeSystemEntity entity = response.getCodeSystemEntity().get(i);
          Iterator<CodeSystemEntityVersion> it = entity.getCodeSystemEntityVersions().iterator();
          while (it.hasNext())
          {
            CodeSystemEntityVersion csev = it.next();
            AssociationType assType = (AssociationType) csev.getAssociationTypes().toArray()[0];
            String code = assType.getForwardName() + assType.getReverseName();

            associationTypeMap.put(code, assType);
            logger.debug("put(" + code + ")");
          }
        }
      }
    }

  }

  private AssociationType CreateAssociationType(String forwardName, String reverseName)
  {
    initAssociationTypes();
    String key = forwardName + reverseName;

    if (associationTypeMap.containsKey(key) == false)
    {
      // TODO diese Beziehung einpflegen
      return null;
    }
    else
    {
      return associationTypeMap.get(key);
    }

  }
}
