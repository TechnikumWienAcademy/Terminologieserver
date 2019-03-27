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
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

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
        String HQL_session_select = "from Session ";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "termUserId",user.getId());

        //Adding parameters (connected through AND)
        HQL_session_select += parameterHelper.getWhere("");

        //Creating query
        org.hibernate.Query Q_session_select = hb_session.createQuery(HQL_session_select);

        //Setting parameters via helper
        parameterHelper.applyParameter(Q_session_select);

        //Executing query
        List<Session> sessionList = (java.util.List<Session>) Q_session_select.list();

        if (sessionList != null && sessionList.size() > 0)
          return sessionList;
        else 
            return null;
    }
  
    public static List<Session> checkForExistingKollabSessions(org.hibernate.Session hb_session, LoginType login, TermUser user){
        String HQL_session_select = "from Session ";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "termUserId",user.getId());
        String[] usernameArray = login.getUsername().split(":");
        parameterHelper.addParameter("", "collabUsername", usernameArray[1]);

        //Adding parameters (connected through AND)
        HQL_session_select += parameterHelper.getWhere("");

        //Creating query
        org.hibernate.Query Q_session_select = hb_session.createQuery(HQL_session_select);

        //Parameters can be set now via helper
        parameterHelper.applyParameter(Q_session_select);

        //Executing query
        List<Session> sessionList = (java.util.List<Session>) Q_session_select.list();

        if (sessionList != null && sessionList.size() > 0)
          return sessionList;
        else
            return null;
    }
  
    public static void checkForDeadSessions(org.hibernate.Session hb_session){
        //GetSessions for collab_software
        String HQL_session_search = "from Session";
        HQLParameterHelper paramHelper = new HQLParameterHelper();
        
        HQL_session_search += paramHelper.getWhere("");
        org.hibernate.Query Q_session_search = hb_session.createQuery(HQL_session_search);
        paramHelper.applyParameter(Q_session_search);
        List<Session> sessionList = (java.util.List<Session>) Q_session_search.list();
        
        String sessionTimeStr = SysParameter.instance().getStringValue("killDeadSessionAfter", null, null);
        Long maxSessionTime;
        try{
            maxSessionTime = Long.valueOf(sessionTimeStr);
        }
        catch(Exception ex){
            LOGGER.error("Error [0114]", ex);
            maxSessionTime = 43200000L;
        }
        
        //Check all sessions which are older than "killDeadSessionAfter"-Time
        for(Session session:sessionList){
            Date lastTimestamp = session.getLastTimestamp();
            Date now = new Date();
            Long difference = now.getTime() - lastTimestamp.getTime();

            if(difference >= maxSessionTime){
                try{
                    session.setTermUser(null);
                    hb_session.delete(session);
                }
                catch(Exception ex){
                    LOGGER.error("Error [0147]", ex);
                }
            }
        }
    }
}
