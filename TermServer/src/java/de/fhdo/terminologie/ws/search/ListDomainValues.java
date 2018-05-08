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
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**

 @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class ListDomainValues
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   Listet Domains des Terminologieservers auf

   @param parameter Die Parameter des Webservices
   @return Ergebnis des Webservices, alle gefundenen Domains mit angegebenen Filtern
   */
  public ListDomainValuesResponseType ListDomainValues(ListDomainValuesRequestType parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListDomainValues gestartet ======");

    // Return-Informationen anlegen
    ListDomainValuesResponseType response = new ListDomainValuesResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter pr�fen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    try
    {
      java.util.List<DomainValue> list = null;

      // Hibernate-Block, Session �ffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";

        // Parameter dem Helper hinzuf�gen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzuf�gen,
        // sonst sind SQL-Injections m�glich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getDomain() != null)
        {
          // Hier alle Parameter aus der Cross-Reference einf�gen
          // addParameter(String Prefix, String DBField, Object Value)
          //parameterHelper.addParameter("dmv.", "domain.domainId", parameter.getDomain().getDomainId());
          parameterHelper.addParameter("", "domainId", parameter.getDomain().getDomainId());
        }

        //parameterHelper.addParameter("dmv.", "domainValuesForDomainValueId1", "NULL");

        // Parameter hinzuf�gen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");
        hql += where;

        /*if(where.length() < 5)
         hql += " WHERE ";
         else hql += " AND ";
        
         //hql += " dmv.domainValuesForDomainValueId1 is null";
         hql += " dmv.domainValuesForDomainValueId1 is null";*/

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
				//Matthias: setReadOnly
				q.setReadOnly(true);

        // Die Parameter k�nnen erst hier gesetzt werden (�bernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchf�hren
        list = q.list();

        //tx.commit();

        // Hibernate-Block wird in 'finally' geschlossen
        // Ergebnis auswerten
        // Sp�ter wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
        // daf�r m�ssen nichtben�tigte Beziehungen gel�scht werden (auf null setzen)

        int count = 0;
        response.setDomainValues(new LinkedList<DomainValue>());

        if (list != null)
        {
          Iterator<DomainValue> iterator = list.iterator();
          logger.debug("Size: " + list.size());

          while (iterator.hasNext())
          {
            DomainValue dmv = iterator.next();

            if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
            {
              //logger.debug("CONTINUE: " + dmv.getDomainCode());
              continue;
            }
            //else logger.debug("NORMAL: " + dmv.getDomainCode());

            count += applyDomainValue(dmv, response.getDomainValues(), 0);
          }

          // Liste bereinigen
          cleanUpList(response.getDomainValues());

          response.getReturnInfos().setCount(count);

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("DomainValues erfolgreich gelesen");
        }


        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'DomainValues', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'DomainValues', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschlie�en
        hb_session.close();
      }


    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'DomainValues': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'DomainValues': " + e.getLocalizedMessage());
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

  private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // Zugeh�rige Codesysteme mit zur�ckgeben (ohne Versionen)
    if (dv.getCodeSystems() != null)
    {
      Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

      while (iteratorCS.hasNext())
      {
        CodeSystem cs = iteratorCS.next();
        cs.setCodeSystemVersions(null);
        cs.setDomainValues(null);
        cs.setCodeSystemType(null);
        cs.setDescription(null);
        cs.setCurrentVersionId(null);
        cs.setInsertTimestamp(null);
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
    // kann hier noch nicht auf null gesetzt werden, da die Liste sonst durcheinander ger�t
    //dv.setDomainValuesForDomainValueId1(null);
    //dv.setDomainValuesForDomainValueId2(null);
    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      while (iteratorDV2.hasNext())
      {
        DomainValue dv2 = iteratorDV2.next();
        count = applyDomainValue(dv2, list, sum);
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

  private boolean validateParameter(ListDomainValuesRequestType Request, ListDomainValuesResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getDomain() == null)
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
        "Werte zu domainValue d�rfen nicht angegeben sein!");
      erfolg = false;
    }


    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;

  }
}
