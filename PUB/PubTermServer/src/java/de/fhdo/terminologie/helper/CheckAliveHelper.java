/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import org.hibernate.Session;

/**
 * This class provides a quick check if the database connection is alive. It is called by the /TermServer/checkAlive.jsp Website
 * which was implemented to provide a testing URL to ensure that the service is available.
 * @author dabac
 */
public class CheckAliveHelper {
    
    //Checks if the database is connected. See checkAlive.jsp for the corresponding site.
    public String checkAlive(){        
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        if(hb_session.isConnected()){
            hb_session.close();
            return "Service verfügbar.";
        }
        else{
            hb_session.close();
            return "Service nicht verfügbar.";
        }
    }
}
