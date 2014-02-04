package com.siemens.cto.aem.persistence.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import junit.framework.TestCase;

import com.siemens.cto.aem.common.User;

public class GroupTest extends TestCase {
    private final Properties datasourceProperties = getProperties();
    private final Group group = new Group();
    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistence-unit",
            datasourceProperties);
    private EntityManager em;

    protected Properties getProperties() {
        final Properties properties = new Properties();
        InputStream resourceAsStream = null;
        try {
            final Class<? extends GroupTest> callingClass = this.getClass();
            final ClassLoader classLoader = callingClass.getClassLoader();
            resourceAsStream = classLoader.getResourceAsStream("datasource.properties");
            properties.load(resourceAsStream);
        } catch (final IOException e) {
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (final IOException e) {
                    // We tried
                }
            }
        }
        return properties;
    }

    @Override
    protected void setUp() {
        final User user = new User("testuser", "password");
        user.addToThread();
        em = factory.createEntityManager();

        em.getTransaction().begin();
        em.persist(group);
        em.getTransaction().commit();
    }

    public void testToString() {
        final Query query = em.createQuery("Select g from Group g");

        final List<Group> groups = query.getResultList();
        for (final AbstractEntity<Group> group : groups) {
            System.err.println(group);
        }

        assertEquals("Group{id=1,Name=null}", group.toString());
    }

    // public void testGetName() {
    // fail("Not yet implemented");
    // }
    //
    // public void testSetName() {
    // fail("Not yet implemented");
    // }
    //
}
