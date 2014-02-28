package com.siemens.cto.aem.persistence.dao;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Jvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {TestJpaConfiguration.class})
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
public class JvmDaoJpaTest {

    private static final String QUERY_COUNT = "SELECT COUNT(entity.id) FROM " + Jvm.class.getName() + " entity";
    private static final String QUERY_COUNT_BY_NAME = "SELECT COUNT(entity.id) FROM " + Jvm.class.getName() + " entity where entity.name = ?1";

    private static final String QUERY_OBJECTS = "SELECT g FROM " + Jvm.class.getName() + " g ";
    private static final String QUERY_OBJECTS_BY_ID = "SELECT g FROM " + Jvm.class.getName() + " g where g.id > ?1";

    private static final String TEST_JVM_ONE = "testJvmOne";
    private static final String TEST_JVM_TWO = "testJvmTwo";
    private static final String TEST_JVM_THREE = "testJvmThree";

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    private JvmDaoJpa dao;

    @Before
    public void setUp() {
        final User user = new User("testUser", "password");
        user.addToThread();
        dao = new JvmDaoJpa(em);
    }

    private Jvm add(final String name) {
        final Jvm jvm = new Jvm();
        jvm.setName(name);
        dao.add(jvm);
        final Jvm retrieved = dao.findByName(name);
        assertEquals(name,
                     retrieved.getName());
        return retrieved;
    }

    @Test
    public void testAdd() {
        add(TEST_JVM_ONE);
    }

    @Test(expected = PersistenceException.class)
    public void testAddNullName() {
        final Jvm jvm = new Jvm();
        dao.add(jvm);
        fail("Should not be possible to add a jvm with no name");
    }

    @Test(expected = EntityExistsException.class)
    public void testAddDuplicateName() {
        testAdd();
        final Jvm jvm = new Jvm();
        jvm.setName(TEST_JVM_ONE);
        dao.add(jvm);
        fail("Should not be possible to add a jvm with a name that already exists: " + TEST_JVM_ONE);
    }

    @Test
    public void testRemove() {
        Jvm jvm = add(TEST_JVM_ONE);

        dao.remove(jvm);

        jvm = dao.findByName(TEST_JVM_TWO);
        assertNull("Should not find jvm " + TEST_JVM_TWO, jvm);
    }

    @Test
    public void testFindAll() {
        add(TEST_JVM_ONE);
        List<Jvm> jvms = dao.findAll();
        assertEquals("size should be ONE", 1, jvms.size());

        add(TEST_JVM_TWO);
        jvms = dao.findAll();
        assertEquals("size should be TWo", 2, jvms.size());
    }

    @Test
    public void testFindByName() {
        add(TEST_JVM_TWO);
        final AbstractEntity<Jvm> jvm = dao.findByName(TEST_JVM_TWO);
        assertEquals(TEST_JVM_TWO, jvm.getName());
    }

    @Test
    public void testFindById() {
        add(TEST_JVM_TWO);
        Jvm jvm = dao.findByName(TEST_JVM_TWO);
        final Long jvmId = jvm.getId();
        jvm = dao.findById(jvmId);
        assertEquals(TEST_JVM_TWO, jvm.getName());
    }

    @Test
    public void testCount() {
        add(TEST_JVM_ONE);
        add(TEST_JVM_TWO);
        int count = dao.count(QUERY_COUNT, (Object[]) null);
        assertEquals("Invalid count", 2, count);
        count = dao.count(QUERY_COUNT_BY_NAME, new Object[] {TEST_JVM_ONE});
        assertEquals("Invalid count", 1, count);
    }

    @Test
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

    @Test
    public void testFlush() {
        add(TEST_JVM_ONE);
        add(TEST_JVM_TWO);
        dao.flush();
        List<Jvm> jvms = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, jvms.size());
        jvms = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, jvms.size());
    }

    @Test
    public void testUpdate() {
        add(TEST_JVM_ONE);
        Jvm jvm = add(TEST_JVM_TWO);
        jvm.setName(TEST_JVM_THREE);
        final Jvm updated = dao.update(jvm);
        assertEquals("Incorrect name", TEST_JVM_THREE, updated.getName());

        jvm = dao.findByName(TEST_JVM_THREE);
        assertEquals("Incorrect name", TEST_JVM_THREE, jvm.getName());
    }

    @Test(expected = EntityExistsException.class)
    public void testUpdateJvmExists() {
        add(TEST_JVM_ONE);
        final Jvm jvm = add(TEST_JVM_TWO);
        jvm.setName(TEST_JVM_ONE);
        dao.update(jvm);
        fail("Update should have failed because " + TEST_JVM_ONE + " is supposed to exist");
    }
}
