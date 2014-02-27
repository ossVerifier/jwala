package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Group;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.*;
import java.util.List;

public class GroupDaoJpaTest extends TestCase {

    private final EntityManager em;
    private EntityTransaction transaction;

    private static final String QUERY_COUNT = "SELECT COUNT(entity.id) FROM " + Group.class.getName() + " entity";
    private static final String QUERY_COUNT_BY_NAME = "SELECT COUNT(entity.id) FROM " + Group.class.getName() + " entity where entity.name = ?1";

    private static final String QUERY_OBJECTS = "SELECT g FROM " + Group.class.getName() + " g ";
    private static final String QUERY_OBJECTS_BY_ID = "SELECT g FROM " + Group.class.getName() + " g where g.id > ?1";

    private static final String TEST_GROUP_ONE = "testGroupOne";
    private static final String TEST_GROUP_TWO = "testGroupTwo";
    private static final String TEST_GROUP_THREE = "testGroupThree";

    private final GroupDaoJpa dao;

    private final ApplicationContext context;

    public GroupDaoJpaTest() {
        context = new ClassPathXmlApplicationContext("META-INF/persistence-context.xml");
        EntityManagerFactory emf = (EntityManagerFactory) context.getBean("entityManagerFactory");
        this.em = emf.createEntityManager();
        dao = new GroupDaoJpa(em);
    }

    private void cleanup() {
        transaction = em.getTransaction();
        final List<Group> groups = dao.findAll();
        transaction.begin();
        dao.removeAllEntities(groups);
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

    private Group add(final String name) {
        final Group group = new Group();
        group.setName(name);
        transaction.begin();
        dao.add(group);
        transaction.commit();
        final Group retrieved = dao.findByName(name);
        assertEquals(name, retrieved.getName());
        return retrieved;
    }

    public void testAdd() {
        add(TEST_GROUP_ONE);
    }

    public void testAddNullName() {
        final Group group = new Group();
        transaction.begin();
        try {
            dao.add(group);
            fail("Should not be possible to add a group with no name");
        } catch (final Exception e) {
            final Throwable cause = e.getCause();
            assertTrue("Incorrect cause exception type: " + cause.getClass(), cause instanceof PersistenceException);
        } finally {
            transaction.rollback();
        }
    }

    public void testAddDuplicateName() {
        testAdd();
        final Group group = new Group();
        group.setName(TEST_GROUP_ONE);
        transaction.begin();
        try {
            dao.add(group);
            fail("Should not be possible to add a group with a name that already exists: " + TEST_GROUP_ONE);
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        } finally {
            transaction.rollback();
        }
    }

    public void testRemove() {
        Group group = add(TEST_GROUP_ONE);

        transaction.begin();
        dao.remove(group);
        transaction.commit();

        group = dao.findByName(TEST_GROUP_TWO);
        assertNull("Should not find group " + TEST_GROUP_TWO, group);
    }

    public void testFindAll() {
        add(TEST_GROUP_ONE);
        List<Group> groups = dao.findAll();
        assertEquals("size should be ONE", 1, groups.size());

        add(TEST_GROUP_TWO);
        groups = dao.findAll();
        assertEquals("size should be TWo", 2, groups.size());
    }

    public void testFindByName() {
        add(TEST_GROUP_TWO);
        final AbstractEntity<Group> group = dao.findByName(TEST_GROUP_TWO);
        assertEquals(TEST_GROUP_TWO, group.getName());
    }

    public void testFindById() {
        add(TEST_GROUP_TWO);
        Group group = dao.findByName(TEST_GROUP_TWO);
        final Long groupId = group.getId();
        group = dao.findById(groupId);
        assertEquals(TEST_GROUP_TWO, group.getName());
    }

    public void testCount() {
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        int count = dao.count(QUERY_COUNT, (Object[]) null);
        assertEquals("Invalid count", 2, count);
        count = dao.count(QUERY_COUNT_BY_NAME, new Object[] {TEST_GROUP_ONE});
        assertEquals("Invalid count", 1, count);
    }

    public void testFindObjects() {
        List<Group> groups = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("List of objects should be empty, it is: " + groups, 0, groups.size());
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        groups = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, groups.size());
        groups = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, groups.size());
    }

    public void testFlush() {
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        transaction.begin();
        dao.flush();
        transaction.commit();
        List<Group> groups = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, groups.size());
        groups = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, groups.size());
    }

    public void testUpdate() {
        add(TEST_GROUP_ONE);
        Group group = add(TEST_GROUP_TWO);
        group.setName(TEST_GROUP_THREE);
        transaction.begin();
        final Group updated = dao.update(group);
        transaction.commit();
        assertEquals("Incorrect name", TEST_GROUP_THREE, updated.getName());

        group = dao.findByName(TEST_GROUP_THREE);
        assertNotNull("Group " + TEST_GROUP_THREE + " not found", group);
        assertEquals("Incorrect name", TEST_GROUP_THREE, group.getName());
    }

    public void testUpdateGroupExists() {
        add(TEST_GROUP_ONE);
        final Group group = add(TEST_GROUP_TWO);
        group.setName(TEST_GROUP_ONE);
        try {
            transaction.begin();
            dao.update(group);
            transaction.commit();
            fail("Update should have failed because " + TEST_GROUP_ONE + " is supposed to exist");
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        } finally {
            transaction.rollback();
        }
    }
}
