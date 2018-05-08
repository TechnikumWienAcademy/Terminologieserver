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
 */package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author Nico Hänsch
 */
public class ListConceptAssociations
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListConceptAssociationsResponseType ListConceptAssociations(ListConceptAssociationsRequestType parameter)
  {
    return ListConceptAssociations(parameter, null);
  }

  /**
   * Verbindungen zwischen Begriffen auflisten
   *
   * @param parameter
   * @return Liste von Entity-Assoziationen
   */
  public ListConceptAssociationsResponseType ListConceptAssociations(ListConceptAssociationsRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListConceptAssociation gestartet ======");

    boolean createHibernateSession = (session == null);

    logger.debug("createHibernateSession: " + createHibernateSession);

    // Return-Informationen anlegen
    ListConceptAssociationsResponseType response = new ListConceptAssociationsResponseType();
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
      logger.debug("check Login");

      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin(), session);
      loggedIn = loginInfoType != null;
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }


    try
    {
      java.util.List<CodeSystemConcept> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = session;

      if (createHibernateSession || hb_session == null)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
      }

      try //Try-Catch-Block um Hibernate-Fehler abzufangen
      {
        CodeSystemEntityVersion csev_parameter = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
        long cse_versionId = csev_parameter.getVersionId();

        // TODO leftID korrekt implementieren
        // directionBoth implementieren (reverse funktioniert)
        if (parameter.getDirectionBoth() == null)
        {
          parameter.setDirectionBoth(false);
        }

        if (parameter.getReverse() == null || parameter.getDirectionBoth())
        {
          parameter.setReverse(false);
        }

        //Hibernate Query Language erstellen
        String hql = "select distinct term from CodeSystemConcept term";
        hql += " join fetch term.codeSystemEntityVersion csev";

        hql += " join fetch csev.codeSystemEntity cse";

        // je nach Richtung wird die 1 oder 2 angehangen
        hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId";
        if (parameter.getReverse())
        {
          hql += "1";
        }
        else
        {
          hql += "2";
        }
        hql += " cseva";
        hql += " join fetch cseva.associationType at";

        hql += " join cseva.codeSystemEntityVersionByCodeSystemEntityVersionId";
        if (parameter.getReverse())
        {
          hql += "2";
        }
        else
        {
          hql += "1";
        }
        hql += " csev_source";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        parameterHelper.addParameter("csev_source.", "versionId", cse_versionId);

        if (parameter.getCodeSystemEntityVersionAssociation() != null)
        {
          parameterHelper.addParameter("cseva.", "associationKind", parameter.getCodeSystemEntityVersionAssociation().getAssociationKind());
        }

        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        if (logger.isDebugEnabled())
        {
          logger.debug("HQL: " + hql);
        }

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        list = (java.util.List<CodeSystemConcept>) q.list();

        List<CodeSystemEntityVersionAssociation> returnList = new LinkedList<CodeSystemEntityVersionAssociation>();

        //if (createHibernateSession)
        //  tx.commit();


        // Ergebnisliste befüllen
        Iterator<CodeSystemConcept> it = list.iterator();
        while (it.hasNext())
        {
          // CodeSystemConcept holen
          CodeSystemConcept term = it.next();

          // CodeSystemEntityVersion lesen
          CodeSystemEntityVersion csev = term.getCodeSystemEntityVersion();

          if (csev != null)
          {
            csev.setAssociationTypes(null);
            //csev.setCodeSystemEntity(null);
            csev.setConceptValueSetMemberships(null);
            csev.setPropertyVersions(null);
            csev.setCodeSystemMetadataValues(null);
            csev.setValueSetMetadataValues(null);
            
            if (parameter != null && parameter.getReverse())
            {
              if (parameter != null && parameter.getLookForward() != null && parameter.getLookForward())
              {
                // Gibt immer die nächste Verbindung mit zurück
                Iterator<CodeSystemEntityVersionAssociation> itTemp = csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().iterator();

                while (itTemp.hasNext())
                {
                  CodeSystemEntityVersionAssociation csevaTemp = itTemp.next();
                  if (csevaTemp.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
                  {
                    csevaTemp.setAssociationType(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  }
                  else
                  {
                    itTemp.remove();
                  }
                }
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null
                        && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size() == 0)
                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              }
              else
              {
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              }
            }
            else
            {
              if (parameter != null && parameter.getLookForward() != null && parameter.getLookForward())
              {
                logger.debug("lookForward");
                // Gibt immer die nächste Verbindung mit zurück
                Iterator<CodeSystemEntityVersionAssociation> itTemp = csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().iterator();

                while (itTemp.hasNext())
                {
                  logger.debug("Verbindung prüfen, iterator.next()");
                  CodeSystemEntityVersionAssociation csevaTemp = itTemp.next();
                  if (csevaTemp.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
                  {
                    csevaTemp.setAssociationType(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                    logger.debug("Verbindung taxonomisch, drin lassen");
                  }
                  else
                  {
                    logger.debug("Verbindung löschen, iterator.remove()");
                    itTemp.remove();
                  }
                }

                logger.debug("Anzahl: " + csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size());
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                        && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() == 0)
                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              }
              else
              {
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              }
              //csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
            }

            if (csev.getCodeSystemEntity() != null)
            {
              csev.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(null);
              csev.getCodeSystemEntity().setCodeSystemEntityVersions(null);
            }

            // der Version wieder das Concept hinzufügen und die Verbindungen null setzen
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            term.setCodeSystemEntityVersion(null);
            term.setCodeSystemConceptTranslations(null);
            csev.getCodeSystemConcepts().add(term);

            // Assoziation lesen und Verbindungen auf null setzen
            if (parameter.getReverse())
            {
              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() == 1)
              {
                CodeSystemEntityVersionAssociation association = (CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().toArray()[0];

                //association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                //association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);

                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                }

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                returnList.add(association);
              }
            }
            else
            {
              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size() == 1)
              {
                CodeSystemEntityVersionAssociation association = (CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().toArray()[0];

                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                returnList.add(association);
              }
            }
          }
        }

        // DirectionBoth
        if (parameter.getDirectionBoth())
        {
          // Hibernate Query Language erstellen
          hql = "select distinct term from CodeSystemConcept term";
          hql += " join fetch term.codeSystemEntityVersion csev";
          hql += " join fetch csev.codeSystemEntity cse";
          hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 cseva";
          hql += " join fetch cseva.associationType at";
          hql += " join cseva.codeSystemEntityVersionByCodeSystemEntityVersionId2 csev_source";

          parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("csev_source.", "versionId", cse_versionId);

          if (parameter.getCodeSystemEntityVersionAssociation() != null)
          {
            parameterHelper.addParameter("cseva.", "associationKind", parameter.getCodeSystemEntityVersionAssociation().getAssociationKind());
          }

          if (loggedIn == false)
          {
            parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
          }

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");

          if (logger.isDebugEnabled())
          {
            logger.debug("HQL#2 (DirectionBoth): " + hql);
            logger.debug("CSEV-VersionId: " + cse_versionId);
          }
          
          // Query erstellen
          Query q2 = hb_session.createQuery(hql);

          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q2);

          // Datenbank-Aufruf durchführen
          java.util.List<CodeSystemConcept> list2 = (java.util.List<CodeSystemConcept>) q2.list();

          // Ergebnisliste befüllen
          for (CodeSystemConcept term : list2)
          {
            // CodeSystemConcept holen
            // CodeSystemEntityVersion lesen
            //logger.error("Term: " + term.getCodeSystemEntityVersionId() + ", " + term.getCode());

            CodeSystemEntityVersion csev = term.getCodeSystemEntityVersion();
            if (csev != null)
            {
              csev.setAssociationTypes(null);
              csev.setConceptValueSetMemberships(null);
              csev.setPropertyVersions(null);
              csev.setCodeSystemMetadataValues(null);
              csev.setValueSetMetadataValues(null);
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);

              if (csev.getCodeSystemEntity() != null)
              {
                csev.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(null);
                csev.getCodeSystemEntity().setCodeSystemEntityVersions(null);
              }

              // der Version wieder das Concept hinzufügen und die Verbindungen null setzen
              csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
              term.setCodeSystemEntityVersion(null);
              term.setCodeSystemConceptTranslations(null);
              csev.getCodeSystemConcepts().add(term);

              // Assoziation lesen und Verbindungen auf null setzen
              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() == 1)
              {
                //CodeSystemEntityVersionAssociation association = 
                //(CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().toArray()[0];
                CodeSystemEntityVersionAssociation association =
                        csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().iterator().next();

                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                }

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                returnList.add(association);
              }
            }
            else
            {
              logger.warn("ListConceptAssociations.java: CodeSystemEntityVersion ist null");
            }
          }


        }
        // Direction Both Ende



        response.setCodeSystemEntityVersionAssociation(returnList);
        if (returnList.isEmpty())
        {
          response.getReturnInfos().setMessage("Keine passenden Assoziationen vorhanden!");
        }
        else
        {
          response.getReturnInfos().setMessage("Assoziationen erfolgreich gelesen, Anzahl: " + returnList.size());
          response.getReturnInfos().setCount(returnList.size());
        }
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung (Hibernate) an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociation', Hibernate: " + e.getLocalizedMessage());
        logger.error("Fehler bei 'ListConceptAssociation', Hibernate: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          logger.debug("Schließe Hibernate-Session (ListConceptAssociations.java)");
          hb_session.close();
        }
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten            
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociation': " + e.getLocalizedMessage());
      logger.error("Fehler bei 'ListConceptAssociation': " + e.getLocalizedMessage());
    }
    return response;
  }

  private boolean validateParameter(ListConceptAssociationsRequestType Request, ListConceptAssociationsResponseType Response)
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


    // Prüfen ob eine CodeSystemEntity mitgegeben wurde (MUSS) 
    if (Request.getCodeSystemEntity() == null)
    {
      Response.getReturnInfos().setMessage("Es muss eine CodeSystemEntity übergeben werden.");
      parameterValidiert = false;
    }
    else
    {
      CodeSystemEntity codeSystemEntity = Request.getCodeSystemEntity();
      //Prüfen ob genau eine codeSystemEntityVersions mitgegeben wurde (MUSS)
      if (codeSystemEntity.getCodeSystemEntityVersions() == null || codeSystemEntity.getCodeSystemEntityVersions().size() != 1)
      {
        Response.getReturnInfos().setMessage("Es muss genau eine CodeSystemEntityVersions angegeben sein.");
        parameterValidiert = false;
      }
      else
      {
        //Prüfe ob eine versionID angegeben wurde (MUSS)
        CodeSystemEntityVersion vcsev = (CodeSystemEntityVersion) codeSystemEntity.getCodeSystemEntityVersions().toArray()[0];
        if (vcsev.getVersionId() == null || vcsev.getVersionId() <= 0)
        {
          Response.getReturnInfos().setMessage("Es muss eine ID für die CodeSystemEntity-Version angegeben sein!");
          parameterValidiert = false;
        }
      }

    }

    //Prüfen ob CodeSystemEntityVersionAssociation angegeben wurde (KANN)
    if (Request.getCodeSystemEntityVersionAssociation() != null)
    {
      //Prüfen ob AssociationKind angegeben wurde (MUSS)
      if (Request.getCodeSystemEntityVersionAssociation().getAssociationKind() == null
              || Definitions.isAssociationKindValid(Request.getCodeSystemEntityVersionAssociation().getAssociationKind()) == false)
      {
        Response.getReturnInfos().setMessage("AssociationKind darf nicht leer sein, wenn CodeSystemEntityVersionAssociation angegeben ist und muss einen der folgenden Werte haben: " + Definitions.readAssociationKinds());
        parameterValidiert = false;
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
