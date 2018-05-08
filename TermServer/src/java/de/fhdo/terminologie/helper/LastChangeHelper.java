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
public class LastChangeHelper
{

    public static boolean updateLastChangeDate(Boolean isCodeSystemVersion, Long id, Session hb_session)
    {
        boolean exists = true;
        if (hb_session == null)
        {
            hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            exists = false;
        }
        boolean success = false;
        try
        {
            if (isCodeSystemVersion)
            {

                CodeSystemVersion csv = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, id);
                csv.setLastChangeDate(new Date());
                //Matthias added change of Status Date
                csv.setStatusDate(new Date());
                hb_session.update(csv);

            }
            else
            {

                ValueSetVersion vsv = (ValueSetVersion) hb_session.get(ValueSetVersion.class, id);
                vsv.setLastChangeDate(new Date());
                hb_session.update(vsv);
            }

            if (!exists)

            {
                hb_session.getTransaction().commit();
            }

            success = true;
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            success = false;
        }
        finally
        {
            if (!exists)
            {
                hb_session.close();
            }
        }
        return success;
    }
}
