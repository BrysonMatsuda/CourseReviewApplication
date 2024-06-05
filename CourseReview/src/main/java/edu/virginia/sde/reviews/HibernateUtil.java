package edu.virginia.sde.reviews;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


public class HibernateUtil {
        private static SessionFactory sessionFactory = buildSessionFactory();

        private static SessionFactory buildSessionFactory() {
            try {
                if (sessionFactory == null) {
                    var standardRegistry = new StandardServiceRegistryBuilder()
                            .configure("hibernate.cfg.xml").build();

                    var metaData = new MetadataSources(standardRegistry)
                            .getMetadataBuilder()
                            .build();
                    sessionFactory = metaData.getSessionFactoryBuilder().build();
                }
                return sessionFactory;
            } catch (HibernateException ex) {
                System.err.println("Initial sessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }

        public static SessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public static void shutdown() {
            getSessionFactory().close();
        }
    }
