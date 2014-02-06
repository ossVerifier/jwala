package com.siemens.cto.aem.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Utilities {

    private static final String propertiesName = "datasource.properties";

    private static Properties loadProperties() {
        final Class<? extends Utilities> callingClass = Utilities.class;
        final ClassLoader classLoader = callingClass.getClassLoader();

        final Properties p = new Properties();
        // InputStream resourceAsStream = null;
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(propertiesName);) {
            if (null == resourceAsStream) {
                throw new RuntimeException("Tests can not run, the datasource.properties could not be loaded");
            }
            p.load(resourceAsStream);
        } catch (final IOException e) {
            throw new RuntimeException("Tests can not run, the datasource.properties could not be loaded");
        }
        return p;
    }

    private static final Properties properties = loadProperties();
    private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("aem-unit", properties);

    public static Properties getProperties() {
        return properties;
    }

    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }

    private Utilities() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }
}
