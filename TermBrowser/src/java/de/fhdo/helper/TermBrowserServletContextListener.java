/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.logging.Logger4j;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
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

/**
 *
 * @author puraner
 */
@WebListener
public class TermBrowserServletContextListener implements ServletContextListener {

    private static org.apache.log4j.Logger logger = Logger4j.getInstance().getLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        logger.info("TermBrowserServletContextListener: contextInitialized");

        try
        {
            //Session session = HibernateUtil.getSessionFactory().openSession();

            //String hql_query = "sys"
        }
        catch (Exception ex)
        {
            Logger.getLogger(TermBrowserServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        logger.warn("TermBrowserServletContextListener: contextDestroyed - started");
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

        logger.warn("TermBrowserServletContextListener: contextDestroyed - Hibernate Session Factory closed");

        LogManager.shutdown();

    }

}
