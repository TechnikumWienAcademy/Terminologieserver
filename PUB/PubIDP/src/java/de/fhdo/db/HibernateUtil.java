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

import java.io.File;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Robert Mützner, Christoph KÃ¶sner
 */
public class HibernateUtil {

    private static SessionFactory adminSessFact = null;
    private static SessionFactory collabSessFact = null;

    public static final int TERM_ADMIN = 1;
    public static final int COLLAB_USER = 2;

    public static SessionFactory getSessionFactory(int type) {
        if (type == TERM_ADMIN) {
            //BEFORE COMPILE
            return adminSessFact == null ? adminSessFact = buildSessionFactory("/conf/termadminPub.hibernate.cfg.xml") : adminSessFact;
            //return adminSessFact == null ? adminSessFact = buildSessionFactory("/conf/termadmin.hibernate.cfg.xml") : adminSessFact;
        }
        if (type == COLLAB_USER) {
            //return collabSessFact == null ? collabSessFact = buildSessionFactory("/conf/kollaborationPub.hibernate.cfg.xml") : collabSessFact;
            return collabSessFact == null ? collabSessFact = buildSessionFactory("/conf/kollaboration.hibernate.cfg.xml") : collabSessFact;
        }
        return null;
    }

    private static SessionFactory buildSessionFactory(String ext) {
        try {
          
            String filePath = "";
            String path = System.getProperty("catalina.base");
      
            if(path.contains("tomcat_term1")){ //Testsystem BRZ
                filePath = "/data0/web/tomcat_term1" + ext;
            }else if(path.contains("tomcat_col")){ //Kollaborationssystem BRZ
                filePath = "/data0/web/tomcat_col" + ext;
            }else if(path.contains("tomcat_pub")){ //Publikationssystem BRZ
                filePath = "/data0/web/tomcat_pub" + ext;
            }else{ //Default Configuration
                filePath = path + ext;
            }
      
            File file = new File(filePath);
            SessionFactory sf = new AnnotationConfiguration().configure(file).buildSessionFactory();
            return sf;
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

}
