package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.db.hibernate.SysParam;
import de.fhdo.terminologie.helper.SysParameter;

/**
 *
 * @author puraner
 */


public class StaticExportStatus
{
    private static int MAX_SESSIONS = 0;
    private static int activeSessions = 0;
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public StaticExportStatus()
    {
        
    }

    public static int getMAX_SESSIONS()
    {
        if(MAX_SESSIONS == 0)
        {
            String maxSessions = SysParameter.instance().getStringValue("max_export_sessions", null, null);
            if(maxSessions.equals(""))
            {
                //setting default value if DB Param is not set
                maxSessions = "3";
            }
            StaticExportStatus.MAX_SESSIONS = Integer.parseInt(maxSessions);
        }
        return MAX_SESSIONS;
    }
    
    public static void increaseAvtiveSessions()
    {
        logger.info("====== Starting Export Session =====");
        activeSessions++;
        logger.info("Active Sessions: "+activeSessions);
    }
    
    public static void decreaseAvtiveSessions()
    {
        logger.info("====== Releasing Export Session =====");
        activeSessions--;
        logger.info("Active Sessions: "+activeSessions);
    }

    public static int getActiveSessions()
    {
        return activeSessions;
    }
}
