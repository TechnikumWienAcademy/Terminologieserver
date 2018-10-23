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
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.Property;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConcept
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter)
  {
    return CreateConcept(parameter, null);
  }

  public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateConcept gestartet ======");

    // Return-Informationen anlegen
    CreateConceptResponseType response = new CreateConceptResponseType();
    response.setReturnInfos(new ReturnType());
    
    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
        return response; // Fehler bei den Parametern
    }
   
    LoginType paramLogin = null;
    CodeSystem paramCodeSystem = null;
    CodeSystemEntity paramCodeSystemEntity = null;
    List<Property> paramProperty = null;
    //3.2.17 added
    boolean loginAlreadyChecked = false;
            
    if (parameter != null)
    {
      paramLogin = parameter.getLogin();
      paramCodeSystem = parameter.getCodeSystem();
      paramCodeSystemEntity = parameter.getCodeSystemEntity();
      paramProperty = parameter.getProperty();
      //3.2.17
      loginAlreadyChecked = parameter.isLoginAlreadyChecked();
    }

    //3.2.17 added last parameter
    CreateConceptOrAssociationType(response, paramLogin, paramCodeSystem, paramCodeSystemEntity, paramProperty, session, loginAlreadyChecked);
    
    return response;
  }

  //3.2.17 added boolean loginAlreadyChecked
  public void CreateConceptOrAssociationType(CreateConceptResponseType response,
          LoginType paramLogin, CodeSystem paramCodeSystem,
          CodeSystemEntity paramCodeSystemEntity,
          List<Property> paramProperty,
          org.hibernate.Session session,
          boolean loginAlreadyChecked)
  {
    boolean createHibernateSession = (session == null);
    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    //3.2.17 added second check
    if (paramLogin != null && !loginAlreadyChecked)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(paramLogin, session);
      loggedIn = loginInfoType != null;
    }

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    //3.2.17 added first check
    if (!loginAlreadyChecked && loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return;
      
    }

    // TODO Lizenzen prüfen (?)
    try
    {
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
				if(hb_session.getTransaction().isActive()){
					boolean halt = true;
				} else {
					boolean halt = false;
				}
        //hb_session.getTransaction().begin();
      }

      // CodeSystem und CodeSystem-Version zum Speichern vorbereiten
      long codeSystemVersionId = 0;
      long codeSystemEntityVersionId = 0;

      if (paramCodeSystem != null && paramCodeSystem.getCodeSystemVersions() != null
              && paramCodeSystem.getCodeSystemVersions().size() > 0)
      {
        codeSystemVersionId = ((CodeSystemVersion) paramCodeSystem.getCodeSystemVersions().toArray()[0]).getVersionId();
      }

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        logger.info("DABACA 1");

        // Neue Entity und Entity-Version erstellen
        CodeSystemEntity entity = new CodeSystemEntity();
        hb_session.save(entity);

        logger.info("DABACA 2");
        
        CodeSystemEntityVersion entityVersion = (CodeSystemEntityVersion) paramCodeSystemEntity.getCodeSystemEntityVersions().toArray()[0];

        CodeSystemConcept concept = null;
        if (entityVersion.getCodeSystemConcepts() != null && entityVersion.getCodeSystemConcepts().size() > 0)
          concept = (CodeSystemConcept) entityVersion.getCodeSystemConcepts().toArray()[0];

        AssociationType assType = null;
        if (entityVersion.getAssociationTypes() != null && entityVersion.getAssociationTypes().size() > 0)
          assType = (AssociationType) entityVersion.getAssociationTypes().toArray()[0];

        logger.info("DABACA 3");
        
        //entityVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        //entityVersion.setStatus();  
        if(entityVersion.getStatus() == null)
          entityVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        entityVersion.setStatusDate(new Date());
        entityVersion.setInsertTimestamp(new Date());
        entityVersion.setCodeSystemEntity(entity);
        entityVersion.setCodeSystemConcepts(null);
        entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
        entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
        entityVersion.setCodeSystemMetadataValues(null);
        entityVersion.setValueSetMetadataValues(null);
        entityVersion.setConceptValueSetMemberships(null);
        entityVersion.setAssociationTypes(null);
        entityVersion.setPropertyVersions(null);

        logger.info("DABACA 4");
        
        hb_session.save(entityVersion);

        logger.info("DABACA 5");
        
        // Antwort erstellen
        codeSystemEntityVersionId = entityVersion.getVersionId();

        CodeSystemEntityVersion entityVersionReturn = new CodeSystemEntityVersion();
        entityVersionReturn.setVersionId(codeSystemEntityVersionId);
        //entityVersionReturn.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        entityVersionReturn.setStatus(entityVersion.getStatus());

        response.setCodeSystemEntity(new CodeSystemEntity());
        response.getCodeSystemEntity().setId(entity.getId());
        response.getCodeSystemEntity().setCurrentVersionId(entityVersion.getVersionId());
        response.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        response.getCodeSystemEntity().getCodeSystemEntityVersions().add(entityVersionReturn);

        logger.debug("EntityId: " + entity.getId());
        logger.debug("EntityVersionId: " + codeSystemEntityVersionId);

        logger.info("DABACA 6");
        
        // CurrentVersion in der Entity speichern
        entity.setCurrentVersionId(entityVersion.getVersionId());
        hb_session.update(entity);

        logger.info("DABACA 7");
        
        logger.debug("CurrentVersionId: " + entity.getCurrentVersionId());

        // Konzept speichern (inkl. Translations)
        if (concept != null)
        {
          logger.info("DABACA 8");
          concept.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
          concept.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
          concept.setCodeSystemEntityVersionId(codeSystemEntityVersionId);

          Iterator<CodeSystemConceptTranslation> itTranslation = concept.getCodeSystemConceptTranslations().iterator();
          while (itTranslation.hasNext())
          {
            CodeSystemConceptTranslation cTranslation = itTranslation.next();
            cTranslation.setCodeSystemConcept(concept);
          }

          hb_session.save(concept);
          logger.info("DABACA 9");
        }
        if (assType != null)
        {
          assType.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
          assType.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
          assType.setCodeSystemEntityVersionId(codeSystemEntityVersionId);

          logger.info("DABACA 10");
          
          hb_session.save(assType);
          logger.info("DABACA 11");
        }

        // Beziehung zum Vokabular speichern
        if (codeSystemVersionId > 0)
        {
          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();

          membership.setId(new CodeSystemVersionEntityMembershipId());
          membership.getId().setCodeSystemEntityId(entity.getId());
          membership.getId().setCodeSystemVersionId(codeSystemVersionId);

          membership.setIsAxis(Boolean.FALSE);
          membership.setIsMainClass(Boolean.FALSE);

          if (paramCodeSystemEntity.getCodeSystemVersionEntityMemberships() != null
                  && paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().size() > 0)
          {
            CodeSystemVersionEntityMembership memberRequest =
                    (CodeSystemVersionEntityMembership) paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().toArray()[0];

            if (memberRequest != null)
            {
              membership.setIsAxis(memberRequest.getIsAxis());
              membership.setIsMainClass(memberRequest.getIsMainClass());
            }
          }

          logger.info("DABACA 12");
          hb_session.save(membership);
          logger.info("DABACA 13");
        }

        // Property speichern
        if (paramProperty != null)
        {
          Iterator<Property> itProperty = paramProperty.iterator();
          while (itProperty.hasNext())
          {
            Property property = itProperty.next();

            PropertyVersion lastPropVersion = null;
            Iterator<PropertyVersion> itPropertyVersion = property.getPropertyVersions().iterator();
            while (itPropertyVersion.hasNext())
            {
              // Entity Version ID setzen
              PropertyVersion propertyVersion = itPropertyVersion.next();
              propertyVersion.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
              propertyVersion.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);

              propertyVersion.setProperty(property);

              propertyVersion.setInsertTimestamp(new Date());
              propertyVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
              propertyVersion.setStatusDate(new Date());

              lastPropVersion = propertyVersion;
            }

            logger.info("DABACA 14");
            hb_session.save(property);
logger.info("DABACA 15");
            
            // current-Version-ID setzen
            if (lastPropVersion != null)
            {
              property.setCurrentVersionId(lastPropVersion.getVersionId());
              logger.info("DABACA 16");
              hb_session.update(property);
              logger.info("DABACA 17");
            }

            lastPropVersion = null;
            itPropertyVersion = property.getPropertyVersions().iterator();
            while (itPropertyVersion.hasNext())
            {
              PropertyVersion propertyVersion = itPropertyVersion.next();

              if (lastPropVersion != null)
              {
                propertyVersion.setPreviousVersionId(lastPropVersion.getVersionId());
              }

              lastPropVersion = propertyVersion;

              logger.info("DABACA 18");
              hb_session.update(propertyVersion);
              logger.info("DABACA 19");
            }
          }
        }

        logger.info("DABACA 20");
        if (paramCodeSystem != null && paramCodeSystem.getId() != null)
        {  
            //Check ob MetadataParameter default Values angelegt werden müssen
            String hql = "select distinct mp from MetadataParameter mp";
            hql += " join fetch mp.codeSystem cs";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("cs.", "id", paramCodeSystem.getId());

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);
            parameterHelper.applyParameter(q);

            List<MetadataParameter> mpList = q.list();
            if (!mpList.isEmpty())
            {
              Iterator<MetadataParameter> iter = mpList.iterator();
              while (iter.hasNext())
              {
                MetadataParameter mp = (MetadataParameter) iter.next();
                if(mp.getCodeSystem() != null && mp.getCodeSystem().getName() != null){
                    if(!mp.getCodeSystem().getName().equals("LOINC")){
                        CodeSystemMetadataValue csmv = new CodeSystemMetadataValue();

                        csmv.setParameterValue("");
                        csmv.setMetadataParameter(mp);
                        csmv.setCodeSystemEntityVersion(entityVersion);

                        logger.info("DABACA 21");
                        
                        hb_session.save(csmv);
                        logger.info("DABACA 22");
                    }
                }
              }
            }
          }
        }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateConcept', Hibernate: " + e.getLocalizedMessage());
        response.setCodeSystemEntity(null);
        codeSystemEntityVersionId = 0;

        logger.error("Fehler bei 'CreateConcept', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          if (codeSystemEntityVersionId > 0)
          {
            
            if(codeSystemVersionId > 0){
                LastChangeHelper.updateLastChangeDate(true, codeSystemVersionId,hb_session);
            }
            hb_session.getTransaction().commit();
          }
          else
          {
            // Ã„nderungen nicht erfolgreich
            logger.warn("[CreateConcept.java] Ã„nderungen nicht erfolgreich, codeSystemEntityVersionId: "
                    + codeSystemEntityVersionId);

            if(!hb_session.getTransaction().wasRolledBack())
                hb_session.getTransaction().rollback();

            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Konzept konnte nicht erstellt werden!");
            response.setCodeSystemEntity(null);
          }
          hb_session.close();
        }
      }

      // Antwort zusammenbauen

      if (codeSystemEntityVersionId > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Konzept erfolgreich erstellt");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateConcept': " + e.getLocalizedMessage());
      response.setCodeSystemEntity(null);

      logger.error("Fehler bei 'CreateConcept': " + e.getLocalizedMessage());

      e.printStackTrace();
    }
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(CreateConceptRequestType Request, CreateConceptResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      /*if (codeSystem.getId() == null || codeSystem.getId() == 0)
       {
       Response.getReturnInfos().setMessage(
       "Es muss eine ID für das CodeSystem angegeben sein, in welchem Sie das Konzept einfügen möchten!");
       erfolg = false;
       }*/

      Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
      if (csvSet != null)
      {
        if (csvSet.size() > 1)
        {
          Response.getReturnInfos().setMessage(
                  "Die CodeSystem-Version-Liste darf maximal einen Eintrag haben!");
          erfolg = false;
        }
        else if (csvSet.size() == 1)
        {
          CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

          if (csv.getVersionId() == null || csv.getVersionId() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss eine ID für die CodeSystem-Version angegeben sein, in welcher Sie das Konzept einfügen möchten!");
            erfolg = false;
          }
        }
      }

      //Request.getCodeSystemEntity()
    }

    CodeSystemEntity codeSystemEntity = Request.getCodeSystemEntity();
    if (codeSystemEntity == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem-Entity darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      Set<CodeSystemEntityVersion> csevSet = codeSystemEntity.getCodeSystemEntityVersions();
      if (csevSet != null)
      {
        if (csevSet.size() > 1)
        {
          Response.getReturnInfos().setMessage(
                  "Die CodeSystem-Entity-Version-Liste darf maximal einen Eintrag haben!");
          erfolg = false;
        }
        else if (csevSet.size() == 1)
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csevSet.toArray()[0];

          Set<CodeSystemConcept> conceptSet = csev.getCodeSystemConcepts();
          if (conceptSet != null && conceptSet.size() == 1)
          {
            CodeSystemConcept concept = (CodeSystemConcept) conceptSet.toArray()[0];

            if (concept.getCode() == null || concept.getCode().isEmpty())
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Code für das Konzept angeben!");
              erfolg = false;
            }
            else if (concept.getIsPreferred() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen 'isPreferred' für das Konzept angeben!");
              erfolg = false;
            }

            if (concept.getCodeSystemConceptTranslations() != null)
            {
              Iterator<CodeSystemConceptTranslation> itTrans = concept.getCodeSystemConceptTranslations().iterator();

              while (itTrans.hasNext())
              {
                CodeSystemConceptTranslation translation = itTrans.next();
                if (translation.getTerm() == null)
                {
                  Response.getReturnInfos().setMessage("Sie müssen einen Term für eine Konzept-Übersetzung angeben!");
                  erfolg = false;
                }
                else if (translation.getLanguageId() == 0)
                {
                  Response.getReturnInfos().setMessage("Sie müssen eine 'LanguageID' für eine Konzept-Übersetzung angeben!");
                  erfolg = false;
                }
              }
            }
          }
          else
          {
            Response.getReturnInfos().setMessage("CodeSystemConcept-Liste darf nicht NULL sein und muss genau 1 Eintrag haben!");
            erfolg = false;
          }
        }
      }
      else
      {
        Response.getReturnInfos().setMessage("CodeSystemEntityVersion darf nicht NULL sein!");
        erfolg = false;
      }
    }

    if (Request.getProperty() != null)
    {
      Iterator<Property> itProp = Request.getProperty().iterator();

      while (itProp.hasNext())
      {
        Property property = itProp.next();

        if (property.getPropertyVersions() != null)
        {
          Iterator<PropertyVersion> itPropVersion = property.getPropertyVersions().iterator();

          while (itPropVersion.hasNext())
          {
            PropertyVersion propertyVersion = itPropVersion.next();

            if (propertyVersion.getName() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Namen für eine Property-Version angeben!");
              erfolg = false;
            }
          }
        }
        else
        {
          Response.getReturnInfos().setMessage("Bei einer Property muss immer eine Property-Version angegeben sein!");
          erfolg = false;
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
