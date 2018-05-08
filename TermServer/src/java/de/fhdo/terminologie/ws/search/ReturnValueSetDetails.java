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
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnValueSetDetails
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnValueSetDetailsResponseType ReturnValueSetDetails(ReturnValueSetDetailsRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnValueSetDetails gestartet ======");

    // Return-Informationen anlegen
    ReturnValueSetDetailsResponseType response = new ReturnValueSetDetailsResponseType();
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
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
    }

    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      ValueSet valueSet = null;

      try
      {
        String hql = "select distinct vs from ValueSet vs";
        hql += " join fetch vs.valueSetVersions vsv";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getValueSet() != null)
        {
          parameterHelper.addParameter("vs.", "id", parameter.getValueSet().getId());

          if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() > 0)
          {
            ValueSetVersion vsvFilter = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];

            parameterHelper.addParameter("vsv.", "versionId", vsvFilter.getVersionId());
          }else{
              //Return current Version if no version was choosen!
              ValueSet vs = (ValueSet)hb_session.get(ValueSet.class, parameter.getValueSet().getId());
              parameterHelper.addParameter("vsv.", "versionId", vs.getCurrentVersionId());
          }
        }

        if (!loggedIn)
        {
          parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
          parameterHelper.addParameter("vs.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: set read Only
				q.setReadOnly(true);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        List<ValueSet> liste = q.list();

        if (liste != null && liste.size() > 0)
          valueSet = liste.get(0);
        
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnValueSetDetails', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnValueSetDetails', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      if (valueSet != null)
      {
        valueSet.setStatus(null);
        valueSet.setStatusDate(null);
        valueSet.setMetadataParameters(null);

        if (valueSet.getValueSetVersions() != null)
        {
          Iterator<ValueSetVersion> itVSV = valueSet.getValueSetVersions().iterator();

          while (itVSV.hasNext())
          {
            ValueSetVersion vsv = itVSV.next();

            vsv.setValueSet(null);
            vsv.setConceptValueSetMemberships(null);
          }
        }

        // Liste der Response beifügen
        response.setValueSet(valueSet);
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("ValueSet-Details erfolgreich gelesen");
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnValueSetDetails': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnValueSetDetails': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnValueSetDetailsRequestType Request, ReturnValueSetDetailsResponseType Response)
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

    if (Request.getValueSet() != null)
    {
      
    }

    if (Request.getValueSet() != null && Request.getValueSet().getValueSetVersions() != null)
    {
      if (Request.getValueSet().getValueSetVersions().size() > 1)
      {
        Response.getReturnInfos().setMessage(
          "Es darf maximal eine ValueSetVersion angegeben sein!");
        erfolg = false;
      }
      else
      { //Return latest version if no verson..
        /*ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];
        if(vsv.getVersionId() == null || vsv.getVersionId() <= 0)
        {
          Response.getReturnInfos().setMessage(
            "Es muss eine ID für die ValueSet-Version angegeben sein!");
          erfolg = false;
        }*/
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
