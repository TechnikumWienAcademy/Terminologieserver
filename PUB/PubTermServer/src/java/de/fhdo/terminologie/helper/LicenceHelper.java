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

package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicencedUser;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LicenceHelper
{
  // Singleton

  private static LicenceHelper instance;

  public static LicenceHelper getInstance()
  {
    if (instance == null)
      instance = new LicenceHelper();
    return instance;
  }
  // Klasse
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Map<String, LicencedUser> licenceMap;

  public LicenceHelper()
  {
    licenceMap = new HashMap<String, LicencedUser>();
  }

  public boolean userHasLicence(LoginInfoType login, long codeSystemVersionId)
  {
    boolean valid = false;
    
    // Versuchen Lizenz zu laden
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      valid = userHasLicence(login, codeSystemVersionId, hb_session);
      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      logger.error("Fehler bei 'userHasLicence' [LicenceHelper.java], Hibernate: " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }
    
    return valid;
  }

  public boolean userHasLicence(LoginInfoType login, long codeSystemVersionId, Session hb_session)
  {
    if(isCodeSystemVersionUnderLicence(codeSystemVersionId, hb_session) == false)
    {
      return true;
    }
    
    if (codeSystemVersionId <= 0 || login == null || login.getTermUser() == null)
    {
      logger.debug("codeSystemVersionId <= 0 oder login == null || login.getTermUser() == null");
      return false;
    }

    String key = getMapKey(codeSystemVersionId, login.getTermUser().getId());

    if (licenceMap.containsKey(key))
    {
      // Lizenz erneut prüfen
      LicencedUser lu = licenceMap.get(key);
      boolean valid = isLicenceValid(lu);

      if (valid == false)
      {
        // Lizenz entfernen und neue DB-Abfrage
        licenceMap.remove(key);

        return userHasLicence(login, codeSystemVersionId);
      }
      else
        return true;  // Lizenz gültig
    }
    else
    {
      boolean valid = false;


      String hql = "from LicencedUser lu";

      HQLParameterHelper parameterHelper = new HQLParameterHelper();

      parameterHelper.addParameter("", "userId", login.getTermUser().getId());
      parameterHelper.addParameter("", "codeSystemVersionId", codeSystemVersionId);

      // Parameter hinzufügen (immer mit AND verbunden)
      hql += parameterHelper.getWhere("");

      logger.debug("HQL: " + hql);

      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      parameterHelper.applyParameter(q);

      List<LicencedUser> liste = q.list();

      if (liste != null && liste.size() > 0)
      {
        LicencedUser lu = liste.get(0);

        if (isLicenceValid(lu))
        {
          licenceMap.put(key, lu);
          valid = true;
        }
      }


      return valid;
    }
  }
  
  private boolean isCodeSystemVersionUnderLicence(long codeSystemVersionId, Session hb_session)
  {
    CodeSystemVersion csv = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, codeSystemVersionId);
    if(csv != null)
    {
      return csv.getUnderLicence();
    }
    
    return false;
  }

  private boolean isLicenceValid(LicencedUser licence)
  {
    if (licence == null)
      return false;

    Date now = new Date();

    if (licence.getValidFrom() != null
      && licence.getValidFrom().after(now))
    {
      return false;  // Lizenz noch nicht gültig
    }

    if (licence.getValidTo() != null
      && licence.getValidTo().before(now))
    {
      return false;  // Lizenz nicht mehr gültig
    }

    return true; // Lizenz ist noch gültig
  }

  private String getMapKey(long codeSystemVersionId, long userId)
  {
    String s = codeSystemVersionId + "_" + userId;
    return s;
  }
}
