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
import de.fhdo.terminologie.DomainIDs;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystemsInTaxonomy
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Listet Domains des Terminologieservers auf
   *
   * @param parameter Die Parameter des Webservices
   * @return Ergebnis des Webservices, alle gefundenen Domains mit angegebenen
   * Filtern
   */
  public ListCodeSystemsInTaxonomyResponseType ListCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListCodeSystemsInTaxonomy gestartet ======");

    // Alle Codesysteme lesen
    List<CodeSystem> allCodesystemList;
    ListCodeSystemsRequestType lcsRequest = new ListCodeSystemsRequestType();
    if (parameter != null)
      lcsRequest.setLogin(parameter.getLogin());
    ListCodeSystems lcs = new ListCodeSystems();
    ListCodeSystemsResponseType lcsResponse = lcs.ListCodeSystems(lcsRequest);
    allCodesystemList = lcsResponse.getCodeSystem();

    // Return-Informationen anlegen
    ListCodeSystemsInTaxonomyResponseType response = new ListCodeSystemsInTaxonomyResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    boolean isAdmin = false;
    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;
      if (loggedIn)
        isAdmin = loginInfoType.getTermUser().isIsAdmin();
    }

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);


    try
    {
      java.util.List<DomainValue> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      
      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";

        //hql += " join fetch dmv.codeSystems cs";
        hql += " left join fetch cs.codeSystemVersions csv";

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        parameterHelper.addParameter("", "domainId", DomainIDs.CODESYSTEM_TAXONOMY);

        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");

        if (loggedIn == false)
        {
          where += " and (csv.status=" + Definitions.STATUS_CODES.ACTIVE.getCode() + " or cs is null)";
        }

        hql += where;

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        /*if (domain.getDisplayOrder() != null
         && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ID)
         {
         hql += " order by domain_value_id";
         }
         else if (domain.getDisplayOrder() != null
         && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ORDERID)
         {
         hql += " order by order_no";
         }
         else
         hql += " order by domain_display";*/

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
				//Matthias: readOnly
				q.setReadOnly(true);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        list = q.list();

        //tx.commit();

        // Hibernate-Block wird in 'finally' geschlossen
        // Ergebnis auswerten
        // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
        // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)

        int count = 0;
        response.setDomainValue(new LinkedList<DomainValue>());

        if (list != null)
        {
          Iterator<DomainValue> iterator = list.iterator();
          logger.debug("Size: " + list.size());

          while (iterator.hasNext())
          {
            DomainValue dmv = iterator.next();

            if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
            {
              // kein Root-Element
              continue;
            }
            //else logger.debug("NORMAL: " + dmv.getDomainCode());

            count += applyDomainValue(dmv, response.getDomainValue(), 0, allCodesystemList);
          }

          // Liste bereinigen
          cleanUpList(response.getDomainValue());

          if (allCodesystemList.size() > 0)
          {
            // Alle übrigens Codesysteme hinzufügen
            DomainValue otherDV = new DomainValue();
            otherDV.setDomainCode("other");
            otherDV.setDomainDisplay("Sonstige");
            otherDV.setCodeSystems(new HashSet<CodeSystem>());

            for (int i = 0; i < allCodesystemList.size(); ++i)
            {
              //otherDV.setDomainValuesForDomainValueId2(new HashSet<DomainValue>());
              otherDV.getCodeSystems().add(allCodesystemList.get(i));
            }

            response.getDomainValue().add(otherDV);
          }



          response.getReturnInfos().setCount(count);

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("CodeSysteme erfolgreich in Taxonomie gelesen");
        }


        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemsInTaxonomy', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListCodeSystemsInTaxonomy', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
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
      response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());
    }

    return response;
  }

  private void cleanUpList(List<DomainValue> list)
  {
    if (list == null || list.size() == 0)
      return;

    for (int i = 0; i < list.size(); ++i)
    {
      DomainValue dv = list.get(i);
      cleanUpEntry(dv);
    }
  }

  private void cleanUpEntry(DomainValue dv)
  {
    dv.setDomainValuesForDomainValueId1(null);

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      Iterator<DomainValue> itDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      while (itDV2.hasNext())
      {
        DomainValue dv2 = itDV2.next();
        cleanUpEntry(dv2);
      }
    }
  }

  private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum, List<CodeSystem> allCodesystemList)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // Zugehörige Codesysteme mit zurückgeben (mit Versionen)
    // TODO Berechtigung prüfen
    if (dv.getCodeSystems() != null)
    {
      Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

      while (iteratorCS.hasNext())
      {
        CodeSystem cs = iteratorCS.next();
        cs.setDomainValues(null);
        cs.setMetadataParameters(null);

        Iterator<CodeSystemVersion> iteratorCSV = cs.getCodeSystemVersions().iterator();
        while (iteratorCSV.hasNext())
        {
          CodeSystemVersion csv = iteratorCSV.next();
          csv.setCodeSystem(null);
          csv.setCodeSystemVersionEntityMemberships(null);
          csv.setLicenceTypes(null);
          csv.setLicencedUsers(null);
        }

        // In Liste entfernen
        for (int i = 0; i < allCodesystemList.size(); ++i)
        {
          if (allCodesystemList.get(i).getId().longValue() == cs.getId().longValue())
          {
            allCodesystemList.remove(i);
            break;
          }
        }
      }
    }

    logger.debug("Pruefe: " + dv.getDomainCode());

    if (dv.getDomainValuesForDomainValueId1() != null && dv.getDomainValuesForDomainValueId1().size() > 0)
    {
      logger.debug("Value1: " + ((DomainValue) dv.getDomainValuesForDomainValueId1().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value1: null");

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      logger.debug("Value2: " + ((DomainValue) dv.getDomainValuesForDomainValueId2().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value2: null");

    // Beziehungen
    boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().size() == 0);
    // kann hier noch nicht auf null gesetzt werden, da die Liste sonst durcheinander gerät
    //dv.setDomainValuesForDomainValueId1(null);
    //dv.setDomainValuesForDomainValueId2(null);
    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      while (iteratorDV2.hasNext())
      {
        DomainValue dv2 = iteratorDV2.next();
        count = applyDomainValue(dv2, list, sum, allCodesystemList);
      }
    }
    else
      dv.setDomainValuesForDomainValueId2(null);

    if (root)
    {
      list.add(dv);
    }
    count++;
    return count;
  }

  private boolean validateParameter(ListCodeSystemsInTaxonomyRequestType Request, ListCodeSystemsInTaxonomyResponseType Response)
  {
    /*boolean erfolg = true;


     if (erfolg == false)
     {
     Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
     Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
     }
     return erfolg;*
     */
    return true;

  }
}
