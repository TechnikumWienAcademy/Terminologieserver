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
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListDomainsRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**

 @author Robert Mützner (robert.muetzner@fh-dortmund.de) / warends
 */
public class ListDomains
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   Listet Domains des Terminologieservers auf

   @param parameter Die Parameter des Webservices
   @return Ergebnis des Webservices, alle gefundenen Domains mit angegebenen Filtern
   */
  public ListDomainsResponseType ListDomains(ListDomainsRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListDomains gestartet ======");

    // Return-Informationen anlegen
    ListDomainsResponseType response = new ListDomainsResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    try
    {
      java.util.List<Domain> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct dm from Domain dm";

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getDomain() != null)
        {
          // Hier alle Parameter aus der Cross-Reference einfügen
          // addParameter(String Prefix, String DBField, Object Value)
          parameterHelper.addParameter("dm.", "domainId", parameter.getDomain().getDomainId());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");


        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: set read Only
				q.setReadOnly(true);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        list = (java.util.List<Domain>) q.list();

        //tx.commit();

        // Hibernate-Block wird in 'finally' geschlossen
        // Ergebnis auswerten
        // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
        // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)

        int count = 0;

        if (list != null)
        {
          Iterator<Domain> iterator = list.iterator();

          while (iterator.hasNext())
          {
            Domain dm = iterator.next();

            dm.setDisplayOrder(null);
            dm.setDomainValues(null);
          }

          // bereinigte Liste der Antwort beifügen
          response.setDomain(list);
          response.getReturnInfos().setCount(list.size());

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Domains erfolgreich gelesen");
        }


        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'Domains', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'Domains', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }


    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'Domains': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'Domains': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ListDomainsRequestType Request, ListDomainsResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getDomain() != null && Request.getDomain().getDomainId() == null)
    {
      Response.getReturnInfos().setMessage(
        "Es muss eine DomainId angegeben sein, wenn eine Domain gegeben ist!");
      erfolg = false;
    }

    /*if (Request.getDomain() == null)
     {
     Response.getReturnInfos().setMessage(
     "Es muss eine Domain angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDomainId() == null)
     {
     Response.getReturnInfos().setMessage(
     "Es muss eine DomainId angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDomainName() != null)
     {
     Response.getReturnInfos().setMessage(
     "DomainName darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDomainOid() != null)
     {
     Response.getReturnInfos().setMessage(
     "DomainOid darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDescription() != null)
     {
     Response.getReturnInfos().setMessage(
     "Description darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDisplayText() != null)
     {
     Response.getReturnInfos().setMessage(
     "DisplayText darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getIsOptional() != null)
     {
     Response.getReturnInfos().setMessage(
     "IsOptional darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDefaultValue() != null)
     {
     Response.getReturnInfos().setMessage(
     "DefaultValue darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDomainType() != null)
     {
     Response.getReturnInfos().setMessage(
     "DomainType darf nicht angegeben sein!");
     erfolg = false;
     }
     else if (Request.getDomain().getDisplayOrder() != null)
     {
     Response.getReturnInfos().setMessage(
     "DisplayOrder darf nicht angegeben sein!");
     erfolg = false;
     }
     else if ((Request.getDomain().getDomainValues() != null)
     && (Request.getDomain().getDomainValues().size() > 0))
     {
     Response.getReturnInfos().setMessage(
     "Werte zu domainValue dürfen nicht angegeben sein!");
     erfolg = false;
     }
     */


    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;

  }
}
