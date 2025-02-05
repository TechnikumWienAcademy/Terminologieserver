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
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

/**
 *
 * @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class ListValueSetContentsByTermOrCode
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListValueSetContentsByTermOrCodeResponseType ListValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListValueSetContentsByTermOrCode gestartet ======");

    // Return-Informationen anlegen
    ListValueSetContentsByTermOrCodeResponseType response = new ListValueSetContentsByTermOrCodeResponseType();
    response.setReturnInfos(new ReturnType());
		
    // Parameter pr�fen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt f�r jeden Webservice)
    boolean loggedIn = false;

    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }

    try
    {
      // Hibernate-Block, Session �ffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
			
      List<CodeSystemEntity> entityList = null;//new LinkedList<CodeSystemEntity>();

      try
      {
        String hql = "select distinct cse from CodeSystemEntity cse";
        hql += " join fetch cse.codeSystemEntityVersions csev";
        hql += " join fetch csev.codeSystemConcepts term";
        hql += " join fetch csev.conceptValueSetMemberships ms";
        hql += " join fetch ms.valueSetVersion vsv";
				
			
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter.getValueSet().getValueSetVersions() != null)
        {
          ValueSetVersion vsv = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];
          //parameterHelper.addParameter("ms.", "id.valuesetVersionId", vsv.getVersionId());
          parameterHelper.addParameter("vsv.", "versionId", vsv.getVersionId());

          if (!parameter.getSearchTerm().equals(""))
          {
            parameterHelper.addParameter("term.", "term", parameter.getSearchTerm());
          }

          if (!parameter.getSearchCode().equals(""))
          {
            parameterHelper.addParameter("term.", "code", parameter.getSearchCode());
          }

          if (vsv.getConceptValueSetMemberships() != null && vsv.getConceptValueSetMemberships().size() > 0)
          {
            ConceptValueSetMembership cvsm = vsv.getConceptValueSetMemberships().iterator().next();
            if (cvsm.getStatusDate() != null)
              parameterHelper.addParameter("cvsm.", "statusDate", cvsm.getStatusDate());
          }
        }
        else
        {
          // TODO ValueSet und currentVersion lesen, dann als Parameter hinzuf�gen
        }

        if (!loggedIn)
        {
          parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzuf�gen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: set readonly
				q.setReadOnly(true);

        // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
        parameterHelper.applyParameter(q);

        entityList = q.list();
				
				if (parameter.getSearchTerm()!= null && !parameter.getSearchTerm().equals("")){
					List<CodeSystemEntity> translationList = queryForTranslation(parameter.getSearchTerm(), ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId(), hb_session);
					
					Iterator<CodeSystemEntity> it = translationList.iterator();
					while (it.hasNext()){
						CodeSystemEntity cse = it.next();
						if (!entityList.contains(cse)){
							entityList.add(cse);
						}
					}
					
				}
        if (entityList != null)
        {
          Iterator<CodeSystemEntity> itEntities = entityList.iterator();

          while (itEntities.hasNext())
          {
            CodeSystemEntity codeSystemEntity = itEntities.next();

            // Zugeh�rigkeit zu einer CSV
//            codeSystemEntity.setCodeSystemVersionEntityMemberships(null);
            CodeSystemVersionEntityMembership csevm = (CodeSystemVersionEntityMembership) codeSystemEntity.getCodeSystemVersionEntityMemberships().toArray()[0];
            CodeSystemVersion csv = new CodeSystemVersion();
            csv.setVersionId(csevm.getCodeSystemVersion().getVersionId());
            csevm.setCodeSystemEntity(null);
            csevm.setCodeSystemVersion(csv);

            if (codeSystemEntity.getCodeSystemEntityVersions() != null)
            {
              Iterator<CodeSystemEntityVersion> itVersions = codeSystemEntity.getCodeSystemEntityVersions().iterator();

              while (itVersions.hasNext())
              {
                CodeSystemEntityVersion csev = itVersions.next();

                csev.setCodeSystemEntity(null);
                //csev.setConceptValueSetMemberships(null);
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
                csev.setCodeSystemMetadataValues(null); // TODO
                csev.setValueSetMetadataValues(null);
                csev.setAssociationTypes(null);

                // Verbindung (evtl. �berschriebenen Code anzeigen)
                if (csev.getConceptValueSetMemberships() != null)
                {
                  Iterator<ConceptValueSetMembership> itMs = csev.getConceptValueSetMemberships().iterator();

                  while (itMs.hasNext())
                  {
                    ConceptValueSetMembership membership = itMs.next();
                    membership.setCodeSystemEntityVersion(null);
                    membership.setValueSetVersion(null);
                  }
                }

                // Konzepte (Terms)
                if (csev.getCodeSystemConcepts() != null)
                {
                  Iterator<CodeSystemConcept> itConcepts = csev.getCodeSystemConcepts().iterator();

                  while (itConcepts.hasNext())
                  {
                    CodeSystemConcept term = itConcepts.next();

                    term.setCodeSystemEntityVersion(null);

                    // Translations
                    Iterator<CodeSystemConceptTranslation> itTranslations = term.getCodeSystemConceptTranslations().iterator();

                    while (itTranslations.hasNext())
                    {
                      CodeSystemConceptTranslation translation = itTranslations.next();

                      translation.setCodeSystemConcept(null);
                    }
                  }
                }

                // Properties
                if (csev.getPropertyVersions() != null)
                {
                  Iterator<PropertyVersion> itPropVersions = csev.getPropertyVersions().iterator();

                  while (itPropVersions.hasNext())
                  {
                    PropertyVersion propVersion = itPropVersions.next();

                    propVersion.setCodeSystemEntityVersion(null);

                    if (propVersion.getProperty() != null)
                    {
                      propVersion.getProperty().setPropertyVersions(null);
                    }
                  }
                }
              }
            }
          }

          response.setCodeSystemEntity(entityList);
          response.getReturnInfos().setCount(entityList.size());
        }

        if (entityList == null)
        {
          response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet wurden keine Konzepte gefunden!");
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        else
        {
          response.getReturnInfos().setMessage("Konzepte zu einem ValueSet erfolgreich gelesen");
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
        }
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListValueSetContents': " + e.getLocalizedMessage());
    }

    return response;
  }
	
	private List<CodeSystemEntity> queryForTranslation(String searchTerm, Long versionId, Session session){
			
		String hql = "select distinct cse from CodeSystemEntity cse";
			hql += " join fetch cse.codeSystemEntityVersions csev";
			hql += " join fetch csev.codeSystemConcepts term";
			hql += " join fetch csev.conceptValueSetMemberships ms";
			hql += " join fetch ms.valueSetVersion vsv";
			hql += " join fetch term.codeSystemConceptTranslations csct";
			hql += " where vsv.versionId = :vId";
			hql += " and csct.term like :searchTerm";


		Query cseQuery = session.createQuery(hql);
		cseQuery.setLong("vId", versionId);
		cseQuery.setString("searchTerm", "%"+searchTerm+"%");

		return cseQuery.list();

	}

  private boolean validateParameter(ListValueSetContentsByTermOrCodeRequestType Request, ListValueSetContentsByTermOrCodeResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getLogin() != null)
    {
      if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
        erfolg = false;
      }
    }

    if (Request.getValueSet() == null)
    {
      Response.getReturnInfos().setMessage(
              "ValueSet darf nicht NULL sein!");
      erfolg = false;
    }
    else if (Request.getValueSet().getId() == null || Request.getValueSet().getId() == 0)
    {
      Response.getReturnInfos().setMessage(
              "Die ID im ValueSet darf nicht NULL oder 0 sein!");
      erfolg = false;
    }
    else if (Request.getValueSet().getValueSetVersions() != null)
    {
      if (Request.getValueSet().getValueSetVersions().size() != 1)
      {
        Response.getReturnInfos().setMessage(
                "Die ValueSetVersion-Liste muss genau einen Eintrag haben oder die Liste ist NULL!");
        erfolg = false;
      }
      else
      {
        ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];
        if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
        {
          Response.getReturnInfos().setMessage(
                  "Die ValueSetVersion muss eine ID gr��er als 0 beinhalten!");
          erfolg = false;
        }
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return true;
  }
}
