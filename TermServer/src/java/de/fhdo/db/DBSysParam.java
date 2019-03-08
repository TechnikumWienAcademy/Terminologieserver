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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.SysParam;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;

/**
 * Reads various system parameters from the database and returns them.
 * @author Robert Mützner
 */
public class DBSysParam{
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static DBSysParam instance = null;

    /**
     * Singleton format: If the static instance is null, it is instantiated.
     * Then the instance is returned.
     * @return the static instance of the class.
     */
    public static DBSysParam instance(){
        if (instance == null)
            instance = new DBSysParam();
        return instance;
    }
    
    /**
     * Constants
     */
    public static final long VALIDITY_DOMAIN_ID = 60;
    public static final long VALIDITY_DOMAIN_SYSTEM = 1313;
    public static final long VALIDITY_DOMAIN_MODULE = 1314;
    public static final long VALIDITY_DOMAIN_SERVICE = 1315;
    public static final long VALIDITY_DOMAIN_USERGROUP = 1316;
    public static final long VALIDITY_DOMAIN_USER = 1317;

    /**
     * Lists all available validity-domains and returns them.
     * A validity-domain is a domain, in which a parameter is valid.
     * Examples: system, module, service, usergroup, user.
     * @return the list of validity domains.
     */
    public List<DomainValue> getValidityDomains(){
        
        List<DomainValue> domainValueList = null;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();

        try{
            org.hibernate.Query Q_domain_select = hb_session.createQuery("from Domain WHERE domainId=:domain_id");
            Q_domain_select.setParameter("domain_id", VALIDITY_DOMAIN_ID);

            java.util.List<Domain> domainList = (java.util.List<Domain>) Q_domain_select.list();

            if (domainList.size() == 1)
                domainValueList = new LinkedList<DomainValue>(domainList.get(0).getDomainValues());
        }
        catch (Exception ex){
            LOGGER.error("Error [0137]", ex);
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
        
        return domainValueList;
    }

    /**
     * Reads a parameter from the database, the name of the parameter has to be
     * given. Validity-domain and object-ID are optional. They can be set if you
     * want to read a parameter for the given validity-domain and the object
     * with the given object-ID. (For example user as validity-domain and the
     * user's ID as object-ID.)
     * @param Name the name of the parameter to be read.
     * @param ValidityDomain the validity-domain for which to filter (optional).
     * @param ObjectID the ID of the object for which to filter the domain (optional).
     * @return the value which was read from the databse.
     */
    public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID){
        
        SysParam returnValue = null;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
    
        try{
            org.hibernate.Query Q_sysParam_select;

            if (ValidityDomain != null && ObjectID == null){
                Q_sysParam_select = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
                Q_sysParam_select.setParameter("name", Name);
                Q_sysParam_select.setParameter("vd", ValidityDomain);
            }
            else if (ValidityDomain != null && ObjectID != null){
                Q_sysParam_select = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
                Q_sysParam_select.setParameter("name", Name);
                Q_sysParam_select.setParameter("vd", ValidityDomain);
                Q_sysParam_select.setParameter("objectid", ObjectID);
            }
            else{
                Q_sysParam_select = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validityDomain");
                Q_sysParam_select.setParameter("name", Name);
            }
            Q_sysParam_select.setMaxResults(1);

            java.util.List<SysParam> paramList = (java.util.List<SysParam>) Q_sysParam_select.list();

            if (paramList.size() > 0)
                returnValue = paramList.get(0);

            if (returnValue == null && ObjectID != null && ObjectID > 0){
                //No result found, user-ID given: maybe that parameter has not been
                //overwritten -> using standard parameter
                if(hb_session.isOpen())
                    hb_session.close();
                
                return getValue(Name, null, null);
            }
        }
        catch (Exception ex){
            LOGGER.error("Error [0138]", ex);
        }
        finally{
            if(hb_session.isOpen())
                hb_session.close();
        }
    
        return returnValue;
    }

    /**
     * Calls getValue with the parameters given to this function.
     * @param Name see getValue.
     * @param ValidityDomain see getValue.
     * @param ObjectID see getValue.
     * @return the result of the call if it is not null, otherwise "".
     */
    public String getStringValue(String Name, Long ValidityDomain, Long ObjectID){
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        if (param != null && param.getValue() != null)
            return param.getValue();

        return "";
    }

    /**
     * Calls getValue with the parameters given to this function and parses the
     * returned value to a boolean.
     * @param Name see getValue.
     * @param ValidityDomain see getValue.
     * @param ObjectID see getValue.
     * @return the result of the call, parsed to boolean or null if that fails.
     */
    public Boolean getBoolValue(String Name, Long ValidityDomain, Long ObjectID){
        SysParam param = getValue(Name, ValidityDomain, ObjectID);
        try{
            if (param != null && param.getValue() != null)
                return Boolean.parseBoolean(param.getValue());
        }
        catch (Exception e){
            LOGGER.error("Error [0139]", e);
        }
        
        return null;
    }
}