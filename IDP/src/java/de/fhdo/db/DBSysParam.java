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
import org.hibernate.SessionFactory;

/**
 * Diese Klasse ist eine Hilfsklasse zum Auslesen und Speichern von Parametern
 * in der Datenbank
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class DBSysParam {  // Konstanten

    public static final long VALIDITY_DOMAIN_ID = 60;
    public static final long VALIDITY_DOMAIN_SYSTEM = 1313;
    public static final long VALIDITY_DOMAIN_MODULE = 1314;
    public static final long VALIDITY_DOMAIN_SERVICE = 1315;
    public static final long VALIDITY_DOMAIN_USERGROUP = 1316;
    public static final long VALIDITY_DOMAIN_USER = 1317;

    private SessionFactory sf;

    public DBSysParam(SessionFactory sf) {
        this.sf = sf;
    }

    /**
     * Listet alle verfügbaren Validity-Domains auf.
     *
     * Eine Validity-Domain gibt eine DomÃ¤ne an, für die ein Parameter gültig
     * ist. Beispiele für Validity-Domains sind: 1. System 2. Modul 3. Service
     * 4. Benutzergruppe 5. Benutzer
     *
     * @return List<DomainValue> - Liste mit Validity-Domains
     */
    public List<DomainValue> getValidityDomains() {
        List<DomainValue> list = null;
        Session s = sf.openSession();
        
        try {
            org.hibernate.Query q = s.createQuery("from Domain WHERE domainId=:domain_id");
            q.setParameter("domain_id", VALIDITY_DOMAIN_ID);

            java.util.List<Domain> domainList = (java.util.List<Domain>) q.list();

            if (domainList.size() == 1) {
                list = new LinkedList<DomainValue>(domainList.get(0).getDomainValues());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        s.close();

        return list;
    }

    /**
     * Liest ein Parameter aus der Datenbank. Der Name des Parameters muss
     * angegeben werden. Validity-Domain und ObjectID sind optional. Diese
     * werden angegeben, wenn man z.B. einen Parameter für einen bestimmten
     * Benutzer lesen mÃ¶chte. In diesem Fall gibt man bei Validity-Domain die ID
     * für User an und bei ObjectID die UserID.
     *
     * @param Name Name des Parameters
     * @param ValidityDomain Validity-Domain (optional)
     * @param ObjectID Objekt-ID, z.B. User-ID (otional)
     * @return Parameter
     */
    public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID)
    {
        Session hb_session = sf.openSession();
        SysParam setting = null;
        org.hibernate.Query q;
        
        if (ValidityDomain != null && ObjectID == null) {
            q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
            q.setParameter("name", Name);
            q.setParameter("vd", ValidityDomain);
        } else if (ValidityDomain != null && ObjectID != null) {
            q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
            q.setParameter("name", Name);
            q.setParameter("vd", ValidityDomain);
            q.setParameter("objectid", ObjectID);
        } else {
            q = hb_session.createQuery("from SysParam WHERE name=:name");
            q.setParameter("name", Name);
        }
        q.setMaxResults(1);

        if(this instanceof DBSysParamAdmin)
        {
            try 
            {
                java.util.List<SysParam> paramList = (java.util.List<SysParam>) q.list();

                if (paramList.size() > 0)
                {
                    // Genau 1 Ergebnis gefunden
                        setting = paramList.get(0);
                }

                if (setting == null && ObjectID != null && ObjectID > 0) {
            // Kein Ergebnis gefunden, aber User-ID angegeben
                    // Evtl. wurde dieser Parameter jedoch nicht überschrieben
                    // also den Standard-Parameter benutzen

                    hb_session.close();

            // TODO eigentlich müsste man 1 Ebene hÃ¶her prüfen
                    // aber die ID ist ja nicht bekannt
                    // Bsp: Wenn User-Parameter nicht gefunden, dann müsste
                    //      in Usergroup gesucht werden
                    //      die Usergroup-ID ist jedoch nicht bekannt
                    return getValue(Name, null, null);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally
            {
                hb_session.close();
            }

            resolveDatatype(setting);

        }
        else if(this instanceof DBSysParamCollab)
        {
            de.fhdo.collaboration.db.classes.SysParam settingCollab = null;
            try 
            {
                java.util.List<de.fhdo.collaboration.db.classes.SysParam> paramList = (java.util.List<de.fhdo.collaboration.db.classes.SysParam>) q.list();

                if (paramList.size() > 0)
                {
                    // Genau 1 Ergebnis gefunden
                        settingCollab = paramList.get(0);
                }

                if (settingCollab == null && ObjectID != null && ObjectID > 0) {
            // Kein Ergebnis gefunden, aber User-ID angegeben
                    // Evtl. wurde dieser Parameter jedoch nicht überschrieben
                    // also den Standard-Parameter benutzen

                    hb_session.close();

            // TODO eigentlich müsste man 1 Ebene hÃ¶her prüfen
                    // aber die ID ist ja nicht bekannt
                    // Bsp: Wenn User-Parameter nicht gefunden, dann müsste
                    //      in Usergroup gesucht werden
                    //      die Usergroup-ID ist jedoch nicht bekannt
                    return getValue(Name, null, null);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally
            {
                hb_session.close();
            }
            
            //parsind de.fhdo.collaboration.db.classes.SysParam to de.fhdo.db.hibernate.SysParam
            if(settingCollab != null)
            {
                setting = new SysParam();
                setting.setId(settingCollab.getId());
                setting.setJavaDatatype(settingCollab.getJavaDatatype());
                setting.setValue(settingCollab.getValue());
                setting.setName(settingCollab.getName());
                setting.setDescription(settingCollab.getDescription());
                setting.setDomainValueByModifyLevel(null);
                setting.setDomainValueByValidityDomain(null);
                setting.setObjectId(settingCollab.getObjectId());
            }

        }
        
        return setting;
    }

    public String getStringValue(String Name, Long ValidityDomain, Long ObjectID) {
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        if (param != null && param.getValue() != null) {
            return param.getValue();
        }

        return "";
    }

    public Boolean getBoolValue(String Name, Long ValidityDomain, Long ObjectID) {
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        try {
            if (param != null && param.getValue() != null) {
                return Boolean.parseBoolean(param.getValue());
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private void resolveDatatype(SysParam setting) {
        if (setting != null && setting.getJavaDatatype() != null
                && setting.getJavaDatatype().equalsIgnoreCase("password")) {
            // Passwort entschlüsseln
            setting.setValue(DES.decrypt(setting.getValue()));
        }
    }
    
    private void applyDatatype(SysParam setting) {
        if (setting != null && setting.getJavaDatatype() != null
                && setting.getJavaDatatype().equalsIgnoreCase("password")) {
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
     * Speichert einen Parameter in der Datenbank.
     *
     *
     * @param Parameter der Parameter
     * @return String mit Fehlermeldung oder leer bei Erfolg
     */
    public String setValue(SysParam Parameter) {
        String ret = "";

        Session hb_session = sf.openSession();
        hb_session.getTransaction().begin();

        try {
            applyDatatype(Parameter);

            hb_session.merge(Parameter);
        } catch (Exception ex) {
            ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
            ex.printStackTrace();
        }

        hb_session.getTransaction().commit();
        hb_session.close();

        return ret;
    }

    /**
     * LÃ¶scht einen Parameter.
     *
     * @param Parameter
     * @return String mit Fehlermeldung oder leer bei Erfolg
     */
    public String deleteValue(SysParam Parameter) {
        String ret = "";

        Session hb_session = sf.openSession();
        hb_session.getTransaction().begin();

        try {
            hb_session.delete(Parameter);
        } catch (Exception ex) {
            ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
            ex.printStackTrace();
        }

        hb_session.getTransaction().commit();
        hb_session.close();

        return ret;
    }
}
