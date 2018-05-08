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
package de.fhdo.terminologie.ws.sso;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.sso.types.MPseudonymRequestType;
import de.fhdo.terminologie.ws.sso.types.MPseudonymResponseType;
import de.fhdo.terminologie.ws.types.MPseudonymInputType;
import de.fhdo.terminologie.ws.types.MPseudonymOutputType;
import de.fhdo.terminologie.ws.types.ReturnType;
import org.hibernate.Query;

/**
 *
 * @author Philipp Urbauer
 */
public class MPseudonym {
    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    public MPseudonymResponseType manipulate(MPseudonymRequestType parameter)
    {
        
        MPseudonymResponseType response = new MPseudonymResponseType();
        response.setmPseudonymOutputType(new MPseudonymOutputType());
        response.setReturnType(new ReturnType());
        
        response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnType().setStatus(ReturnType.Status.FAILURE);
        response.getReturnType().setMessage("WebService nicht mehr verfügbar.");
        
        return response;
        /*
        if (logger.isInfoEnabled())
            logger.info("====== Login gestartet ======");
        
        // Return-Informationen anlegen
        MPseudonymResponseType response = new MPseudonymResponseType();
        response.setmPseudonymOutputType(new MPseudonymOutputType());
        response.setReturnType(new ReturnType());

        MPseudonymInputType inputType = parameter.getmPseudonymInputType();
        
        try
        {
          // Hibernate-Block, Session öffnen
          org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
          hb_session.getTransaction().begin();

          try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
          {
              
            if(inputType.getType().equals("delete")){
            
                Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1");
                q.setString("p_user", inputType.getUsername());
                java.util.List<TermUser> userList = q.list();

                logger.debug("User-List-length: " + userList.size());

                if (userList.size() == 1)
                {
                  try
                  {
                    TermUser user = userList.get(0);
                    user.setPseudonym("");
                    hb_session.saveOrUpdate(user);
                  }
                  catch (Exception e)
                  {
                    e.printStackTrace();
                  }
                }
                
                response.getmPseudonymOutputType().setPseudonym("");
                response.getmPseudonymOutputType().setStatus("success");
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnType().setStatus(ReturnType.Status.OK);
                response.getReturnType().setMessage("Pseudonym erfolgreich gelöscht!");
            }
            if(inputType.getType().equals("get")){
                
                String pseudonym = "";
                String hash = "";
                Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1");
                q.setString("p_user", inputType.getUsername());
                java.util.List<TermUser> userList = q.list();

                logger.debug("User-List-length: " + userList.size());

                if (userList.size() == 1)
                {
                  try
                  {
                    TermUser user = userList.get(0);
                    pseudonym = user.getPseudonym();
                    hash = user.getPassw();
                  }
                  catch (Exception e)
                  {
                    e.printStackTrace();
                  }
                }
                
                response.getmPseudonymOutputType().setPseudonym(pseudonym);
                response.getmPseudonymOutputType().setHash(hash);
                response.getmPseudonymOutputType().setStatus("success");
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnType().setStatus(ReturnType.Status.OK);
                response.getReturnType().setMessage("Pseudonym erfolgreich abgerufen!");
            }
            if(inputType.getType().equals("set")){
                
                Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1");
                q.setString("p_user", inputType.getUsername());
                java.util.List<TermUser> userList = q.list();

                logger.debug("User-List-length: " + userList.size());

                if (userList.size() == 1)
                {
                  try
                  {
                    TermUser user = userList.get(0);
                    user.setPseudonym(inputType.getPseudonym());
                    hb_session.saveOrUpdate(user);
                  }
                  catch (Exception e)
                  {
                    e.printStackTrace();
                  }
                }
                
                response.getmPseudonymOutputType().setPseudonym("");
                response.getmPseudonymOutputType().setStatus("success");
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnType().setStatus(ReturnType.Status.OK);
                response.getReturnType().setMessage("Pseudonym erfolgreich zugeordnet!");
            }
            hb_session.getTransaction().commit();
          }catch(Exception e){
            
            hb_session.getTransaction().rollback();
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnType().setStatus(ReturnType.Status.FAILURE);
            response.getReturnType().setMessage("Fehler bei 'MPseudonym', Hibernate: " + e.getLocalizedMessage());

            logger.error("Fehler bei 'MPseudonym', Hibernate: " + e.getLocalizedMessage());
          }
          finally
          {
            // Transaktion abschließen
            hb_session.close();
          }
        }
        catch (Exception e)
        {
          // Fehlermeldung an den Aufrufer weiterleiten
          response.getReturnType().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          response.getReturnType().setStatus(ReturnType.Status.FAILURE);
          response.getReturnType().setMessage("Fehler bei 'MPseudonym': " + e.getLocalizedMessage());

          logger.error("Fehler bei 'MPseudonym': " + e.getLocalizedMessage());
        }

        return response;*/
    }
}
