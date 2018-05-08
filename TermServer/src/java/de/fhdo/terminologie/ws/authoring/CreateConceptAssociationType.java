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

import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.Property;
import de.fhdo.terminologie.db.hibernate.PropertyVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConceptAssociationType
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(
    CreateConceptAssociationTypeRequestType parameter)
  {
    return CreateConceptAssociationType(parameter, null);
  }
  
  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(
    CreateConceptAssociationTypeRequestType parameter, org.hibernate.Session session)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateConceptAssociationType gestartet ======");
    
    // Return-Informationen anlegen
    CreateConceptAssociationTypeResponseType response = new CreateConceptAssociationTypeResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    
    
    CreateConcept cc = new CreateConcept();
    CreateConceptResponseType responseCC = new CreateConceptResponseType();
    responseCC.setReturnInfos(response.getReturnInfos());
    
    LoginType paramLogin = null;
    CodeSystem paramCodeSystem = null;
    CodeSystemEntity paramCodeSystemEntity = null;
    List<Property> paramProperty = null;
    if(parameter != null)
    {
      paramLogin = parameter.getLogin();
      paramCodeSystem = parameter.getCodeSystem();
      paramCodeSystemEntity = parameter.getCodeSystemEntity();
      paramProperty = parameter.getProperty();
    }
    
    cc.CreateConceptOrAssociationType(responseCC, paramLogin, paramCodeSystem, paramCodeSystemEntity, paramProperty, session);
    
    response.setReturnInfos(responseCC.getReturnInfos());
    response.setCodeSystemEntity(responseCC.getCodeSystemEntity());
    
    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   * 
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(CreateConceptAssociationTypeRequestType Request,
                                    CreateConceptAssociationTypeResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem != null)
    {
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
              "Es muss eine ID für die CodeSystem-Version angegeben sein, in welchem Sie das Konzept einfügen möchten!");
            erfolg = false;
          }
        }
      }
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

          Set<AssociationType> assTypesSet = csev.getAssociationTypes();
          if (assTypesSet != null && assTypesSet.size() == 1)
          {
            AssociationType assType = (AssociationType) assTypesSet.toArray()[0];

            if (assType.getForwardName() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Forward-Namen für das Konzept vergeben!");
              erfolg = false;
            }
            else if (assType.getReverseName() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Reverse-Namen für das Konzept vergeben!");
              erfolg = false;
            }
          }
          else
          {
            Response.getReturnInfos().setMessage("AssociationType-Liste darf nicht NULL sein und muss genau 1 Eintrag haben!");
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
