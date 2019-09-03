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

 @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class DBSysParam{
    public static final long VALIDITY_DOMAIN_ID = 60;
    public static final long VALIDITY_DOMAIN_SYSTEM = 1313;
    public static final long VALIDITY_DOMAIN_MODULE = 1314;
    public static final long VALIDITY_DOMAIN_SERVICE = 1315;
    public static final long VALIDITY_DOMAIN_USERGROUP = 1316;
    public static final long VALIDITY_DOMAIN_USER = 1317;
    
    private static DBSysParam instance = null;
    
    /**
     * Returns the DBSysParam instance if it is not null, otherwise it will
     * be newly instantiated and then returned.
     * @return the instance of the DBSysParam class
     */
    public static DBSysParam getInstance(){
        if (instance == null)
            instance = new DBSysParam();
        return instance;
    }
    
  /**
   Listet alle verfügbaren Validity-Domains auf.

   Eine Validity-Domain gibt eine Domäne an, für die ein Parameter
   gültig ist. Beispiele für Validity-Domains sind:
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
    * Gets a value from the database based on the parameters.
    * @param name has to be given
    * @param ValidityDomain optional, the domain in which you want to search
    * @param ObjectID optional, the ID of the object in the domain you want to get
    * @return the retrieved value
    */
    public SysParam getValue(String name, Long ValidityDomain, Long ObjectID){
        SysParam setting = null;

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try{
            org.hibernate.Query dbQuery;

            if (ValidityDomain != null && ObjectID == null){
              dbQuery = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
              dbQuery.setParameter("name", name);
              dbQuery.setParameter("vd", ValidityDomain);
            }
            else if (ValidityDomain != null && ObjectID != null){
                dbQuery = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
                dbQuery.setParameter("name", name);
                dbQuery.setParameter("vd", ValidityDomain);
                dbQuery.setParameter("objectid", ObjectID);
            }
            else{
                dbQuery = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validityDomain");
                dbQuery.setParameter("name", name);
            }
            
            dbQuery.setMaxResults(1);
            java.util.List<SysParam> paramList = (java.util.List<SysParam>) dbQuery.list();

            if (!paramList.isEmpty()){
                setting = paramList.get(0);
            }

            if (setting == null && ObjectID != null && ObjectID > 0){
                // Kein Ergebnis gefunden, aber User-ID angegeben
                // Evtl. wurde dieser Parameter jedoch nicht überschrieben
                // also den Standard-Parameter benutzen

                if(hb_session.isOpen())
                    hb_session.close();

                // TODO eigentlich müsste man 1 Ebene höher prüfen
                // aber die ID ist ja nicht bekannt
                // Bsp: Wenn User-Parameter nicht gefunden, dann müsste
                //      in Usergroup gesucht werden
                //      die Usergroup-ID ist jedoch nicht bekannt
                return getValue(name, null, null);
            }
        }
        catch (Exception ex){
            System.out.println("Error [0005]");
            ex.printStackTrace();
        }

        if(hb_session.isOpen())
            hb_session.close();

        resolveAndSetPassword(setting);

        return setting;
    }

    /**
     * Calls getValue witht the parameters and retrieves a SysParam object from
     * that call. Then the getValue() return-value of that object is returned
     * if the object and its value are not null.
     * @param name the name of the object to be retrieved
     * @param ValidityDomain optional, the domain in which to search
     * @param ObjectID optional, the ID of the object within the domain
     * @return Either the getValue() return-value of the object or ""
     */
    public String getStringValue(String name, Long ValidityDomain, Long ObjectID){
        SysParam param = getValue(name, ValidityDomain, ObjectID);
        if (param != null && param.getValue() != null)
            return param.getValue();
        else
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

    /**
     * If the setting parameter is a password, it will be decrypted and set.
     * @param passwordSetting the password setting to be descrypted and set
     */
    private void resolveAndSetPassword(SysParam passwordSetting){
        if (passwordSetting != null && passwordSetting.getJavaDatatype() != null && passwordSetting.getJavaDatatype().equalsIgnoreCase("password")){
            passwordSetting.setValue(DES.decrypt(passwordSetting.getValue()));
        }
    }

  private void applyDatatype(SysParam setting)
  {
    if (setting != null && setting.getJavaDatatype() != null
      && setting.getJavaDatatype().equalsIgnoreCase("password"))
    {
      // Passwort entschlüsseln
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
   Löscht einen Parameter.

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
