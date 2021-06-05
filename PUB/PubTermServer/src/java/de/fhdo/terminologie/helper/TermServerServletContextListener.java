package de.fhdo.terminologie.helper;

//import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import java.sql.*;
import java.util.Enumeration;
//import de.fhdo.terminologie.db.HibernateUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;
//import org.hibernate.SessionFactory;
//import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
//import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
//import org.hibernate.internal.SessionFactoryImpl;
//import org.hibernate.connection.C3P0ConnectionProvider;
//import org.hibernate.connection.ConnectionProvider;
//import org.hibernate.impl.SessionFactoryImpl;

/**
 *
 * @author Frohner
 */
@WebListener
public class TermServerServletContextListener implements ServletContextListener {

    private static org.apache.log4j.Logger logger = Logger4j.getInstance().getLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        logger.info("TermServerServletContextListener: contextInitialized");

        try
        {
            //Session session = HibernateUtil.getSessionFactory().openSession();

            //String hql_query = "sys"
        }
        catch (Exception ex)
        {
            Logger.getLogger(TermServerServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {

        /*ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()){
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl){
				try{
					DriverManager.deregisterDriver(driver);
				} catch (Exception e){
					logger.info("Unload Driver not successfull");
				}
			}
		}*/
        //LoggerContext context = (LoggerContext) LogManager.getContext();
        //Configurator.shutdown(context);
        /*
		
		Set<PooledDataSource> pooledDataSources = (Set<PooledDataSource> )C3P0Registry.getPooledDataSources();
		
		for (PooledDataSource source : pooledDataSources){
			try {
				source.hardReset();
				source.close();
			} catch (SQLException ex) {
				Logger.getLogger(TermServerServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
				//System.out.println("TermServerServletContextListener: not able to close data sources");
			}			
		}
         */
        try
        {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements())
            {
                Driver driver = drivers.nextElement();
                try
                {
                    DriverManager.deregisterDriver(driver);
                    logger.info(String.format("deregistering jdbc driver: %s", driver));
                }
                catch (SQLException e)
                {
                    logger.error(String.format("Error deregistering driver %s", driver), e);
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error1");
            logger.error("Failure: Could not clase open JDBC connections: ", ex);
            //System.out.println("TermServerServletContextLister: not able to shut down AbandonedConnectionCleanupThread");
        }

        try
        {
            logger.warn("TermServerServletContextListener: contextDestroyed - started");
            SessionFactory sf = HibernateUtil.getSessionFactory();
            if (sf instanceof SessionFactoryImpl)
            {
                SessionFactoryImpl sfi = (SessionFactoryImpl) sf;
                ConnectionProvider conn = sfi.getConnectionProvider();
                if (conn instanceof C3P0ConnectionProvider)
                {
                    ((C3P0ConnectionProvider) conn).close();
                }
            }

        }
        catch (Exception e)
        {
            System.out.println("Error2");
            logger.error("Failure: Could not close C3P0Connection");
        }

        logger.warn("TermServerServletContextListener: contextDestroyed - Hibernate Session Factory closed");

        LogManager.shutdown();

    }

}
