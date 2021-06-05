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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) / warends
 */
public class ListMetadataParameter
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListMetadataParameterResponseType ListMetadataParameter(ListMetadataParameterRequestType parameter)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ListMetadataParameter gestartet ======");
    }

    // Return-Informationen anlegen
    ListMetadataParameterResponseType response = new ListMetadataParameterResponseType();
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

      java.util.List<MetadataParameter> liste = null;

      try
      {
        String hql = "select distinct mp from MetadataParameter mp";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: set readonly
				q.setReadOnly(true);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        liste = q.list();
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListMetadataParameters', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListMetadataParameter', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      int anzahl = 0;
      if (liste != null)
      {
        anzahl = liste.size();
        Iterator<MetadataParameter> itMP = liste.iterator();

        while (itMP.hasNext())
        {
          MetadataParameter mp = itMP.next();

          if (mp.getCodeSystemMetadataValues() != null){
                mp.setCodeSystemMetadataValues(null);
                mp.setCodeSystem(null);
          }
          if(mp.getValueSetMetadataValues() != null){
                mp.setValueSetMetadataValues(null);
                mp.setValueSet(null);
          }
        }

        // Liste der Response beifügen
        response.setMetadataParameter(liste);
        response.getReturnInfos().setCount(liste.size());
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("MetadataParameter erfolgreich gelesen, Anzahl: " + anzahl);
      response.getReturnInfos().setCount(anzahl);
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListMetadataParameter': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListMetadataParameter': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(ListMetadataParameterRequestType Request, ListMetadataParameterResponseType Response)
  {
    boolean erfolg = true;
    if (Request != null)
    {

      if (Request.getLogin() != null)
      {
        if (Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
        {
          Response.getReturnInfos().setMessage(
                  "Die Session-ID darf nicht leer sein, wenn ein Login-Type angegeben ist!");
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