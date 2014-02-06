package com.siemens.cto.aem.persistence.dao;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.Utilities;
import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Jvm;

public class JvmDaoJpaTest extends TestCase {

    private final EntityManager em = Utilities.getEntityManager();
    private EntityTransaction transaction = em.getTransaction();

    private static final String QUERY_COUNT = "SELECT COUNT(entity.id) FROM " + Jvm.class.getName() + " entity";
    private static final String QUERY_COUNT_BY_NAME = "SELECT COUNT(entity.id) FROM " + Jvm.class.getName() + " entity where entity.name = ?1";

    private static final String QUERY_OBJECTS = "SELECT g FROM " + Jvm.class.getName() + " g ";
    private static final String QUERY_OBJECTS_BY_ID = "SELECT g FROM " + Jvm.class.getName() + " g where g.id > ?1";

    private static final String TEST_JVM_ONE = "testJvmOne";
    private static final String TEST_JVM_TWO = "testJvmTwo";
    private static final String TEST_JVM_THREE = "testJvmThree";

    static final JvmDaoJpa containerDao = new JvmDaoJpa();
    private final JvmDaoJpa dao = new JvmDaoJpa(em);

    private void cleanup() {
        transaction = em.getTransaction();
        final List<Jvm> jvms = dao.findAll();
        transaction.begin();
        dao.removeAllEntities(jvms);
        transaction.commit();
    }

    @Override
    protected void setUp() {
        cleanup();
        final User user = new User("testUser", "password");
        user.addToThread();
        transaction = em.getTransaction();
    }

    @Override
    protected void tearDown() {
        cleanup();
    }

    private Jvm add(final String name) {
        final Jvm jvm = new Jvm();
        jvm.setName(name);
        transaction.begin();
        dao.add(jvm);
        transaction.commit();
        final Jvm retrieved = dao.findByName(name);
        assertEquals(name, retrieved.getName());
        return retrieved;
    }

    public void testAdd() {
        add(TEST_JVM_ONE);
    }

    public void testAddNullName() {
        final Jvm jvm = new Jvm();
        transaction.begin();
        try {
            dao.add(jvm);
            fail("Should not be possible to add a jvm with no name");
        } catch (final Exception e) {
            final Throwable cause = e.getCause();
            assertTrue("Incorrect cause exception type: " + cause.getClass(), cause instanceof PersistenceException);
        } finally {
            transaction.rollback();
        }
    }

    public void testAddDuplicateName() {
        testAdd();
        final Jvm jvm = new Jvm();
        jvm.setName(TEST_JVM_ONE);
        transaction.begin();
        try {
            dao.add(jvm);
            fail("Should not be possible to add a jvm with a name that already exists: " + TEST_JVM_ONE);
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        } finally {
            transaction.rollback();
        }
    }

    public void testRemove() {
        Jvm jvm = add(TEST_JVM_ONE);

        transaction.begin();
        dao.remove(jvm);
        transaction.commit();

        jvm = dao.findByName(TEST_JVM_TWO);
        assertNull("Should not find jvm " + TEST_JVM_TWO, jvm);
    }

    public void testFindAll() {
        add(TEST_JVM_ONE);
        List<Jvm> jvms = dao.findAll();
        assertEquals("size should be ONE", 1, jvms.size());

        add(TEST_JVM_TWO);
        jvms = dao.findAll();
        assertEquals("size should be TWo", 2, jvms.size());
    }

    public void testFindByName() {
        add(TEST_JVM_TWO);
        final AbstractEntity<Jvm> jvm = dao.findByName(TEST_JVM_TWO);
        assertEquals(TEST_JVM_TWO, jvm.getName());
    }

    public void testFindById() {
        add(TEST_JVM_TWO);
        Jvm jvm = dao.findByName(TEST_JVM_TWO);
        final Long jvmId = jvm.getId();
        jvm = dao.findById(jvmId);
        assertEquals(TEST_JVM_TWO, jvm.getName());
    }

    public void testCount() {
        add(TEST_JVM_ONE);
        add(TEST_JVM_TWO);
        int count = dao.count(QUERY_COUNT, (Object[]) null);
        assertEquals("Invalid count", 2, count);
        count = dao.count(QUERY_COUNT_BY_NAME, new Object[] {TEST_JVM_ONE});
        assertEquals("Invalid count", 1, count);
    }

    public void testFindObjects() {
        List<Jvm> jvms = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("List of objects should be empty, it is: " + jvms, 0, jvms.size());
        add(TEST_JVM_ONE);
        add(TEST_JVM_TWO);
        jvms = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, jvms.size());
        jvms = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, jvms.size());
    }

    public void testFlush() {
        add(TEST_JVM_ONE);
        add(TEST_JVM_TWO);
        transaction.begin();
        dao.flush();
        transaction.commit();
        List<Jvm> jvms = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, jvms.size());
        jvms = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, jvms.size());
    }

    public void testUpdate() {
        add(TEST_JVM_ONE);
        Jvm jvm = add(TEST_JVM_TWO);
        jvm.setName(TEST_JVM_THREE);
        transaction.begin();
        final Jvm updated = dao.update(jvm);
        transaction.commit();
        assertEquals("Incorrect name", TEST_JVM_THREE, updated.getName());

        jvm = dao.findByName(TEST_JVM_THREE);
        assertNotNull("Jvm " + TEST_JVM_THREE + " not found", jvm);
        assertEquals("Incorrect name", TEST_JVM_THREE, jvm.getName());
    }

    public void testUpdateJvmExists() {
        add(TEST_JVM_ONE);
        final Jvm jvm = add(TEST_JVM_TWO);
        jvm.setName(TEST_JVM_ONE);
        try {
            transaction.begin();
            dao.update(jvm);
            transaction.commit();
            fail("Update should have failed because " + TEST_JVM_ONE + " is supposed to exist");
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        } finally {
            transaction.rollback();
        }
    }
}
