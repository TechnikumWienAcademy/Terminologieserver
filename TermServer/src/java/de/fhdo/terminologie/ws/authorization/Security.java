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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.types.LoginType;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Security
{

  public static final String COLLAB_SOFTWARE_NAME = "collaboration_software";  
    
  public static Session getSession(org.hibernate.Session hb_session, LoginType login)
  {
    
    if(login.getSessionID() != null && login.getSessionID().length() != 0){
        String hql = "from Session ";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "sessionId", login.getSessionID());

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        java.util.List<Session> list = (java.util.List<Session>) q.list();

        if (list != null && list.size() > 0)
        {
          return list.get(0);
        }
        return null;
    }else
        return null;
  }
  
  public static List<Session> checkForExistingSessions(org.hibernate.Session hb_session, LoginType login, TermUser user){
  
        String hql = "from Session ";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "termUserId",user.getId());

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        List<Session> list = (java.util.List<Session>) q.list();

        if (list != null && list.size() > 0)
        {
          return list;
        }
        return null;
  }
  
  public static List<Session> checkForExistingKollabSessions(org.hibernate.Session hb_session, LoginType login, TermUser user){
      
        String hql = "from Session ";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "termUserId",user.getId());
        String[] str = login.getUsername().split(":");
        parameterHelper.addParameter("", "collabUsername", str[1]);

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        List<Session> list = (java.util.List<Session>) q.list();

        if (list != null && list.size() > 0)
        {
          return list;
        }
        return null;
  }
  
  public static void checkForDeadSessions(org.hibernate.Session hb_session){
        //GetSessions for collab_software
        String hqlS = "from Session";
        HQLParameterHelper parameterHelperS = new HQLParameterHelper();
        
        hqlS += parameterHelperS.getWhere("");
        org.hibernate.Query qS = hb_session.createQuery(hqlS);
        parameterHelperS.applyParameter(qS);
        List<Session> listS = (java.util.List<Session>) qS.list();
        
        String sessionTimeStr = SysParameter.instance().getStringValue("killDeadSessionAfter", null, null);
        Long sessionTime = 0l;
        try{
            sessionTime = Long.valueOf(sessionTimeStr);
        }catch(Exception ex){
            sessionTime = 43200000l;
        }
        
        //Check for each Sessions which are older than "killDeadSessionAfter"-Time
        for(Session s:listS){
        
            Date dateOfOrigin = s.getLastTimestamp();
            Date now = new Date();
            Long difference = now.getTime() - dateOfOrigin.getTime();

            if(difference >= sessionTime){
                s.setTermUser(null);
                hb_session.delete(s);
            }
        }
    }
}
