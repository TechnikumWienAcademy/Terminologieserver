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
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import java.util.Date;
import org.hibernate.Session;

/**
 *
 * @author Philipp Urbauer
 */
public class LastChangeHelper{

    final private static org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    public static boolean updateLastChangeDate(Boolean isCodeSystemVersion, Long id, Session hb_session){
        boolean hbOpened = false;
        if (hb_session == null){
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            hbOpened = true;
        }
        boolean success = false;
        try{
            if (isCodeSystemVersion){
                CodeSystemVersion CSversion = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, id);
                CSversion.setLastChangeDate(new Date());
                CSversion.setStatusDate(new Date());
                hb_session.update(CSversion);
            }
            else{
                ValueSetVersion VSversion = (ValueSetVersion) hb_session.get(ValueSetVersion.class, id);
                VSversion.setLastChangeDate(new Date());
                VSversion.setStatusDate(new Date());
                hb_session.update(VSversion);
            }

            if (hbOpened && !hb_session.getTransaction().wasCommitted())
                hb_session.getTransaction().commit();

            success = true;
        }
        catch (Exception ex){
            LOGGER.error("Error [0119]", ex);
            success = false;
            try{
                if(!hb_session.getTransaction().wasRolledBack())
                    hb_session.getTransaction().rollback();
            }
            catch(Exception e){
                LOGGER.error("Error [0120]: Rollback failed", e);
            }
        }
        finally{
            if (hbOpened && hb_session.isOpen())
                hb_session.close();
        }
        return success;
    }
}
