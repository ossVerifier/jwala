package com.siemens.cto.aem.persistence.dao;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {TestJpaConfiguration.class})
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class GroupDaoJpaTest {

    private static final String QUERY_COUNT = "SELECT COUNT(entity.id) FROM " + Group.class.getName() + " entity";
    private static final String QUERY_COUNT_BY_NAME = "SELECT COUNT(entity.id) FROM " + Group.class.getName() + " entity where entity.name = ?1";

    private static final String QUERY_OBJECTS = "SELECT g FROM " + Group.class.getName() + " g ";
    private static final String QUERY_OBJECTS_BY_ID = "SELECT g FROM " + Group.class.getName() + " g where g.id > ?1";

    private static final String TEST_GROUP_ONE = "testGroupOne";
    private static final String TEST_GROUP_TWO = "testGroupTwo";
    private static final String TEST_GROUP_THREE = "testGroupThree";

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    private GroupDaoJpa dao;

    @Before
    public void setUp() {
        final User user = new User("testUser", "password");
        user.addToThread();
        dao = new GroupDaoJpa(em);
    }

    private Group add(final String name) {
        final Group group = new Group();
        group.setName(name);
        dao.add(group);
        final Group retrieved = dao.findByName(name);
        assertEquals(name, retrieved.getName());
        return retrieved;
    }

    @Test
    public void testAdd() {
        add(TEST_GROUP_ONE);
    }


    @Test
    public void testAddNullName() {
        final Group group = new Group();
        try {
            dao.add(group);
            fail("Should not be possible to add a group with no name");
        } catch (final Exception e) {
            final Throwable cause = e.getCause();
            assertTrue("Incorrect cause exception type: " + cause.getClass(), cause instanceof PersistenceException);
        }
    }

    @Test
    public void testAddDuplicateName() {
        testAdd();
        final Group group = new Group();
        group.setName(TEST_GROUP_ONE);
        try {
            dao.add(group);
            fail("Should not be possible to add a group with a name that already exists: " + TEST_GROUP_ONE);
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        }
    }

    @Test
    public void testRemove() {
        Group group = add(TEST_GROUP_ONE);

        dao.remove(group);

        group = dao.findByName(TEST_GROUP_TWO);
        assertNull("Should not find group " + TEST_GROUP_TWO, group);
    }

    @Test
    public void testFindAll() {
        add(TEST_GROUP_ONE);
        List<Group> groups = dao.findAll();
        assertEquals("size should be ONE", 1, groups.size());

        add(TEST_GROUP_TWO);
        groups = dao.findAll();
        assertEquals("size should be TWo", 2, groups.size());
    }

    @Test
    public void testFindByName() {
        add(TEST_GROUP_TWO);
        final AbstractEntity<Group> group = dao.findByName(TEST_GROUP_TWO);
        assertEquals(TEST_GROUP_TWO, group.getName());
    }

    @Test
    public void testFindById() {
        add(TEST_GROUP_TWO);
        Group group = dao.findByName(TEST_GROUP_TWO);
        final Long groupId = group.getId();
        group = dao.findById(groupId);
        assertEquals(TEST_GROUP_TWO, group.getName());
    }

    @Test
    public void testCount() {
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        int count = dao.count(QUERY_COUNT, (Object[]) null);
        assertEquals("Invalid count", 2, count);
        count = dao.count(QUERY_COUNT_BY_NAME, new Object[] {TEST_GROUP_ONE});
        assertEquals("Invalid count", 1, count);
    }

    @Test
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

    @Test
    public void testFlush() {
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        dao.flush();
        List<Group> groups = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        assertEquals("Invalid size", 2, groups.size());
        groups = dao.findObjects(QUERY_OBJECTS_BY_ID, new Object[] {new Long(0)});
        assertEquals("Invalid size", 2, groups.size());
    }

    @Test
    public void testUpdate() {
        add(TEST_GROUP_ONE);
        Group group = add(TEST_GROUP_TWO);
        group.setName(TEST_GROUP_THREE);
        final Group updated = dao.update(group);
        assertEquals("Incorrect name", TEST_GROUP_THREE, updated.getName());

        group = dao.findByName(TEST_GROUP_THREE);
        assertNotNull("Group " + TEST_GROUP_THREE + " not found", group);
        assertEquals("Incorrect name", TEST_GROUP_THREE, group.getName());
    }

    @Test
    public void testUpdateGroupExists() {
        add(TEST_GROUP_ONE);
        final Group group = add(TEST_GROUP_TWO);
        group.setName(TEST_GROUP_ONE);
        try {
            dao.update(group);
            fail("Update should have failed because " + TEST_GROUP_ONE + " is supposed to exist");
        } catch (final Exception e) {
            assertTrue("Incorrect cause exception type", e instanceof EntityExistsException);
        }
    }
}
