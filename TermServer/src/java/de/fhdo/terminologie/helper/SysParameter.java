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

import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.SysParam;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;

/**
 * 3.2.26 checked
 * This class helps save and retrieve values from the database
 * @author Robert Mützner
 */
public class SysParameter{
    private static SysParameter instance = null;
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    public static final long VALIDITY_DOMAIN_ID = 2;
    public static final long VALIDITY_DOMAIN_SYSTEM = 188;
    public static final long VALIDITY_DOMAIN_MODULE = 189;
    public static final long VALIDITY_DOMAIN_SERVICE = 190;
    public static final long VALIDITY_DOMAIN_USERGROUP = 191;
    public static final long VALIDITY_DOMAIN_USER = 192;
    
    /**
     * Returns the instance or initiates it, if it is null.
     * @return the instance
     */
    public static SysParameter instance(){
        if (instance == null)
            instance = new SysParameter();
        return instance;
    }

    /**
     * Lists all available validity-domains.
     * A validity-domain shows for which domain a parameter is valid, examples for validity-domains are:
     * 1. System
     * 2. Modul
     * 3. Service
     * 4. Usergroup
     * 5. User
     * @return List with the validity-domains
     */
    public List<DomainValue> getValidityDomains(){
        LOGGER.info("+++++ getValidityDomains started +++++");
        List<DomainValue> list = null;

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
    
        try{
            org.hibernate.Query Q_domain_search = hb_session.createQuery("from Domain WHERE domainId=:domain_id");
            Q_domain_search.setParameter("domain_id", VALIDITY_DOMAIN_ID);

            java.util.List<Domain> domainList = (java.util.List<Domain>) Q_domain_search.list();

            if (domainList.size() == 1){
                list = new LinkedList<DomainValue>(domainList.get(0).getDomainValues());
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0021]: " + ex.getLocalizedMessage());
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- getValidityDomains finished (001) -----");
        return list;
    }

    /**
     * Retrieves a value from the database, the name of the parameter has to be given.
     * Validity-domain and object-ID are optional. They are used if you want to retrieve
     * the value for a certain user (objectID) and the vailidity-domain.
     * @param Name the parameters name to be retrieved
     * @param ValidityDomain (optional) retrieves the value only for that validityDomain
     * @param ObjectID (optional) retrieves the value only for that user
     * @return the value
     */
    public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID){
        LOGGER.info("+++++ getValue started +++++");
        SysParam setting = null;

        Session hb_session = HibernateUtil.getSessionFactory().openSession();

        try{
            org.hibernate.Query Q_sysparam_search;

            if (ValidityDomain != null && ObjectID == null){
                Q_sysparam_search = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
                Q_sysparam_search.setParameter("name", Name);
                Q_sysparam_search.setParameter("vd", ValidityDomain);
            }
            else if (ValidityDomain != null && ObjectID != null){
                Q_sysparam_search = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
                Q_sysparam_search.setParameter("name", Name);
                Q_sysparam_search.setParameter("vd", ValidityDomain);
                Q_sysparam_search.setParameter("objectid", ObjectID);
            }
            else{
                Q_sysparam_search = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validityDomain");
                Q_sysparam_search.setParameter("name", Name);
            }
            Q_sysparam_search.setMaxResults(1);

            java.util.List<SysParam> paramList = (java.util.List<SysParam>) Q_sysparam_search.list();

            if (paramList.size() > 0)
                setting = paramList.get(0);

            if (setting == null && ObjectID != null && ObjectID > 0){
                if(hb_session.isOpen())
                    hb_session.close();
                return getValue(Name, null, null);
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0023]: " + ex.getLocalizedMessage());
        } 
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- getValue finished (001) -----");
        return setting;
    }

    /**
     * Calls getValue and converts the result to a string which is returned.
     * @param Name the parameters name to be retrieved
     * @param ValidityDomain (optional) see getValue
     * @param ObjectID (optional) see getValue
     * @return the string value of the parameter
     */
    public String getStringValue(String Name, Long ValidityDomain, Long ObjectID){
        LOGGER.info("+++++ getStringValue started +++++");
        
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        if (param != null && param.getValue() != null){
            LOGGER.info("----- getStringValue finished (001) -----");
            return param.getValue();
        }

        LOGGER.info("----- getStringValue finished (002) -----");
        return "";
    }

    /**
     * Calls getValue and converts the result to a boolean which is returned.
     * @param Name the parameters name to be retrieved
     * @param ValidityDomain (optional) see getValue
     * @param ObjectID (optional) see getValue
     * @return the boolean value of the parameter or null if an exception occurs
     */
    public Boolean getBoolValue(String Name, Long ValidityDomain, Long ObjectID){
        LOGGER.info("+++++ getBoolValue started +++++");
        
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        try{
            if (param != null && param.getValue() != null){
                LOGGER.info("------ getBoolValue finished (001) -----");
                return Boolean.parseBoolean(param.getValue());
            }
        }
        catch (Exception e){
            LOGGER.error("Error [0024]: " + e.getLocalizedMessage());
            LOGGER.info("------ getBoolValue finished (002) -----");
            return null;
        }
        LOGGER.info("------ getBoolValue finished (003) -----");
        return null;
    }

    /**
     * Saves a new value in the database.
     * @param Parameter the value to be saved
     * @return either an error-string or an empty string
     */
    public String setValue(SysParam Parameter){
        LOGGER.info("+++++ setValue started +++++");
        String responseInfo = "";

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try{
            hb_session.merge(Parameter);
            if(hb_session.getTransaction().wasCommitted())
                hb_session.getTransaction().commit();
        }
        catch (Exception ex){
            LOGGER.error("Error [0025]: " + ex.getLocalizedMessage());
            responseInfo = "Fehler [0025]: " + ex.getLocalizedMessage();
        } 
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- setValue finished (001) -----");
        return responseInfo;
    }

    /**
     * Deletes a parameter.
     * @param Parameter the parameter to be deleted
     * @return Infostring which is empty if the function was successful
    */
    public String deleteValue(SysParam Parameter){
        LOGGER.info("+++++ deleteValue started +++++");
        String responseInfo = "";

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try{
            hb_session.delete(Parameter);
            if(!hb_session.getTransaction().wasCommitted())
                hb_session.getTransaction().commit();
        }
        catch (Exception ex){
            LOGGER.error("Error [0022]: " + ex.getLocalizedMessage());
            responseInfo = "Fehler [0022]: " + ex.getLocalizedMessage();
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        LOGGER.info("----- deleteValue finished (001) -----");
        return responseInfo;
    }
}