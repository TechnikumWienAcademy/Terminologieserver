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
package de.fhdo.db;

import de.fhdo.db.hibernate.Domain;
import de.fhdo.db.hibernate.DomainValue;
import de.fhdo.db.hibernate.SysParam;
import de.fhdo.helper.DES;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;

/**
 Diese Klasse ist eine Hilfsklasse zum Auslesen und Speichern von
 Parametern in der Datenbank

 @author Robert M�tzner (robert.muetzner@fh-dortmund.de)
 */
public class DBSysParam
{
  // Singleton-Muster

  private static DBSysParam instance = null;

  public static DBSysParam instance()
  {
    //if (instance == null)
    //{
      instance = new DBSysParam();
    //}
    return instance;
  }
  // Konstanten
  public static final long VALIDITY_DOMAIN_ID = 60;
  public static final long VALIDITY_DOMAIN_SYSTEM = 1313;
  public static final long VALIDITY_DOMAIN_MODULE = 1314;
  public static final long VALIDITY_DOMAIN_SERVICE = 1315;
  public static final long VALIDITY_DOMAIN_USERGROUP = 1316;
  public static final long VALIDITY_DOMAIN_USER = 1317;

  public DBSysParam()
  {
  }

  /**
   Listet alle verf�gbaren Validity-Domains auf.

   Eine Validity-Domain gibt eine Dom�ne an, f�r die ein Parameter
   g�ltig ist. Beispiele f�r Validity-Domains sind:
   1. System
   2. Modul
   3. Service
   4. Benutzergruppe
   5. Benutzer

   @return List<DomainValue> - Liste mit Validity-Domains
   */
  public List<DomainValue> getValidityDomains()
  {
    List<DomainValue> list = null;

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      org.hibernate.Query q = hb_session.createQuery("from Domain WHERE domainId=:domain_id");
      q.setParameter("domain_id", VALIDITY_DOMAIN_ID);

      java.util.List<Domain> domainList = (java.util.List<Domain>) q.list();

      if (domainList.size() == 1)
      {
        list = new LinkedList<DomainValue>(domainList.get(0).getDomainValues());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    hb_session.close();

    return list;
  }

  /**
   Liest ein Parameter aus der Datenbank.
   Der Name des Parameters muss angegeben werden.
   Validity-Domain und ObjectID sind optional. Diese werden angegeben,
   wenn man z.B. einen Parameter f�r einen bestimmten Benutzer lesen
   m�chte. In diesem Fall gibt man bei Validity-Domain die ID f�r User an
   und bei ObjectID die UserID.

   @param Name Name des Parameters
   @param ValidityDomain Validity-Domain (optional)
   @param ObjectID Objekt-ID, z.B. User-ID (otional)
   @return Parameter
   */
  public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam setting = null;

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      org.hibernate.Query q;

      if (ValidityDomain != null && ObjectID == null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
      }
      else if (ValidityDomain != null && ObjectID != null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
        q.setParameter("objectid", ObjectID);
      }
      else
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validityDomain");
        q.setParameter("name", Name);
      }
      q.setMaxResults(1);

      java.util.List<SysParam> paramList = (java.util.List<SysParam>) q.list();

      if (paramList.size() > 0)
      {
        // Genau 1 Ergebnis gefunden
        setting = paramList.get(0);
      }

      if (setting == null && ObjectID != null && ObjectID > 0)
      {
        // Kein Ergebnis gefunden, aber User-ID angegeben
        // Evtl. wurde dieser Parameter jedoch nicht �berschrieben
        // also den Standard-Parameter benutzen

        
        hb_session.close();

        // TODO eigentlich m�sste man 1 Ebene h�her pr�fen
        // aber die ID ist ja nicht bekannt
        // Bsp: Wenn User-Parameter nicht gefunden, dann m�sste
        //      in Usergroup gesucht werden
        //      die Usergroup-ID ist jedoch nicht bekannt
        return getValue(Name, null, null);
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    
    hb_session.close();

    resolveDatatype(setting);

    return setting;
  }

  public String getStringValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam param = getValue(Name, ValidityDomain, ObjectID);
    if (param != null && param.getValue() != null)
      return param.getValue();

    return "";
  }

  public Boolean getBoolValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam param = getValue(Name, ValidityDomain, ObjectID);
    try
    {
      if (param != null && param.getValue() != null)
        return Boolean.parseBoolean(param.getValue());
    }
    catch (Exception e)
    {
      return null;
    }

    return null;
  }

  private void resolveDatatype(SysParam setting)
  {
    if (setting != null && setting.getJavaDatatype() != null
      && setting.getJavaDatatype().equalsIgnoreCase("password"))
    {
      // Passwort entschl�sseln
      setting.setValue(DES.decrypt(setting.getValue()));
    }
  }

  private void applyDatatype(SysParam setting)
  {
    if (setting != null && setting.getJavaDatatype() != null
      && setting.getJavaDatatype().equalsIgnoreCase("password"))
    {
      // Passwort entschl�sseln
      setting.setValue(DES.encrypt(setting.getValue()));
    }
  }

  /* public String setValue(String Name, Long ValidityDomain, Long ObjectID)
   {
   SysParam param = new SysParam();
   param.setName(Name);
   param.setDomainValueByValidityDomain(new DomainValue());
   param.getDomainValueByValidityDomain().setDomainValueId(ValidityDomain);
   param.setObjectId(ObjectID);
   } */
  /**
   Speichert einen Parameter in der Datenbank.


   @param Parameter der Parameter
   @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String setValue(SysParam Parameter)
  {
    String ret = "";

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      applyDatatype(Parameter);

      hb_session.merge(Parameter);
    }
    catch (Exception ex)
    {
      ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    hb_session.getTransaction().commit();
    hb_session.close();

    return ret;
  }

  /**
   L�scht einen Parameter.

   @param Parameter
   @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String deleteValue(SysParam Parameter)
  {
    String ret = "";

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      hb_session.delete(Parameter);
    }
    catch (Exception ex)
    {
      ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    hb_session.getTransaction().commit();
    hb_session.close();

    return ret;
  }
}
