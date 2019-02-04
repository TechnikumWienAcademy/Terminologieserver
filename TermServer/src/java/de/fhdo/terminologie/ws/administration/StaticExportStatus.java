package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.helper.SysParameter;

/**
 * V 3.3 OK
 * @author Stefan Puraner
 */
public class StaticExportStatus{
    private static int MAX_SESSIONS = 0;
    private static int activeSessions = 0;
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Gets the system parameter "max_export_sessions" and uses it to set the maximum number of active sessions.
     * If that is not possible the default value 3 will be used.
     * @return the maximum number of active export sessions.
     */
    public static int getMAX_SESSIONS(){
        if(MAX_SESSIONS == 0){
            String maxSessions = SysParameter.instance().getStringValue("max_export_sessions", null, null);
            //Default value
            if(maxSessions.equals(""))
                maxSessions = "3";
            //TODO parseError absichern
            MAX_SESSIONS = Integer.parseInt(maxSessions);
        }
        return MAX_SESSIONS;
    }
    
    /**
     * Increases the number of active sessions by 1.
     */
    public static void increaseActiveSessions(){
        activeSessions++;
        LOGGER.info("Active sessions: " + activeSessions);
    }
    
    /**
     * Decreases the number of active sessions by 1.
     */
    public static void decreaseActiveSessions(){
        activeSessions--;
        LOGGER.info("Active sessions: "+activeSessions);
    }

    /**
     * Returns the activeSessions field.
     * @return the number of active sessions. 
     */
    public static int getActiveSessions(){
        return activeSessions;
    }
}
