package com.newview.recovery;
import javax.servlet.*;import com.newview.util.HibernateUtil;
public class RecoveryLifecycle implements ServletContextListener {
 public void contextInitialized(ServletContextEvent e){HibernateUtil.getSessionFactory();}
 public void contextDestroyed(ServletContextEvent e){
  SeatStore.close();
  HibernateUtil.shutdown();
  com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
  java.util.Enumeration<java.sql.Driver> drivers=java.sql.DriverManager.getDrivers();
  while(drivers.hasMoreElements()){java.sql.Driver driver=drivers.nextElement();if(driver.getClass().getClassLoader()==getClass().getClassLoader())try{java.sql.DriverManager.deregisterDriver(driver);}catch(java.sql.SQLException ignored){}}
 }
}