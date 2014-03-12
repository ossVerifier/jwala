package com.siemens.cto.aem.persistence.dao;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private DataSource dataSource;

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

        final List<Group> actualGroups = dao.findAll();
        final int actualGroupSize = actualGroups.size();
        assertTrue("size should be at least ONE", actualGroupSize > 0);

        add(TEST_GROUP_TWO);
        final int expectedGroupSize = actualGroupSize + 1;
        final List<Group> actualGroupsPlusOne = dao.findAll();
        assertEquals("size should be one greater", expectedGroupSize, actualGroupsPlusOne.size());
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

        final int initialTotalCount = dao.count(QUERY_COUNT, (Object[]) null);
        final int initialByNameCount = dao.count(QUERY_COUNT_BY_NAME, TEST_GROUP_ONE);

        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);

        final int actualTotalCount = dao.count(QUERY_COUNT, (Object[]) null);
        final int actualByNameCount = dao.count(QUERY_COUNT_BY_NAME, TEST_GROUP_ONE);
        assertEquals("Invalid count", initialTotalCount + 2, actualTotalCount);
        assertEquals("Invalid count by name", initialByNameCount + 1, actualByNameCount);
    }

    @Test
    public void testFindObjects() {

        final int numberOfExistingGroups = dao.findObjects(QUERY_OBJECTS, (Object[]) null).size();

        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);

        final List<Group> allActualGroups = dao.findObjects(QUERY_OBJECTS, (Object[]) null);
        final int expectedNumberOfExistingGroups = numberOfExistingGroups + 2;
        assertEquals("Invalid size", expectedNumberOfExistingGroups, allActualGroups.size());

        final List<Group> allActualGroupsById = dao.findObjects(QUERY_OBJECTS_BY_ID, 0L);
        assertEquals("Invalid size", expectedNumberOfExistingGroups, allActualGroupsById.size());
    }

    @Test
    public void testFlush() {
        final int initialCount = dao.findObjects(QUERY_OBJECTS, (Object[]) null).size();
        add(TEST_GROUP_ONE);
        add(TEST_GROUP_TWO);
        dao.flush();
        final int expectedCount = initialCount + 2;

        final JdbcTemplate template = new JdbcTemplate(dataSource);
        final int actualCount = template.queryForObject("SELECT COUNT(*) FROM GRP", Integer.class);
        assertEquals("Count mismatch between EntityManager and Database", expectedCount, actualCount);
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
