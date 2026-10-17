package com.newview.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
	private static StandardServiceRegistry registry;
	private static final SessionFactory sessionFactory = createSessionFactory();
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	private static SessionFactory createSessionFactory() {
		try {

			registry = new StandardServiceRegistryBuilder()
					.configure()
                        .applySetting("hibernate.connection.password", System.getenv("NEWVIEW_DB_PASSWORD"))
                        .applySetting("hibernate.connection.url", System.getenv().getOrDefault("NEWVIEW_DB_URL", "jdbc:mysql://127.0.0.1:3309/newview_recovery?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei"))
                        .applySetting("hibernate.connection.username", System.getenv().getOrDefault("NEWVIEW_DB_USER", "newview"))
					.build();

			SessionFactory sessionFactory = new MetadataSources(registry)
					.buildMetadata()
					.buildSessionFactory();
 
			return sessionFactory;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}

	}

	public static void shutdown() {
		if (registry != null)
			StandardServiceRegistryBuilder.destroy(registry);
	}
	
}