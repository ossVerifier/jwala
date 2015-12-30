package com.siemens.cto.aem.persistence.service.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.persistence.service.CommonGroupPersistenceServiceBehavior;
import com.siemens.cto.aem.persistence.service.CommonJvmPersistenceServiceBehavior;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractJvmPersistenceServiceTest {

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    private CommonJvmPersistenceServiceBehavior jvmHelper;
    private CommonGroupPersistenceServiceBehavior groupHelper;
    private String userId;

    @Before
    public void setup() {
        jvmHelper = new CommonJvmPersistenceServiceBehavior(jvmPersistenceService);
        groupHelper = new CommonGroupPersistenceServiceBehavior(groupPersistenceService);
        userId = "TestUserId";
    }

    @Test
    public void testCreateJvm() {

        final String jvmName = "A Jvm Name";
        final String hostName = "A Host Name";

        final Jvm jvm = jvmHelper.createJvm(jvmName,
                                            hostName,
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        assertNotNull(jvm);
        assertNotNull(jvm.getId());
        assertEquals(jvmName,
                     jvm.getJvmName());
        assertEquals(hostName,
                     jvm.getHostName());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateJvmWithDuplicateName() {

        final Jvm existingJvm = jvmHelper.createJvm("A Jvm Name",
                                                    "A Host Name",
                                                    5, 4, 3, 2, 1,
                                                    userId,
                                                    new Path("/abc"),
                                                    "EXAMPLE_OPTS=%someEnv%/someVal");

        final Jvm duplicateNameJvm = jvmHelper.createJvm(existingJvm.getJvmName(),
                                                         "A different Host Name",
                                                         5, 4, 3, 2, 1,
                                                         userId,
                                                         new Path("/abc"),
                                                         "EXAMPLE_OPTS=%someEnv%/someVal");
    }

    @Test
    public void testUpdateJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                            "A Host Name",
                                            10, 9, 8, 7, 6,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        final String newJvmName = "A New Jvm Name";
        final String newHostName = "A New Host Name";
        final Integer newHttpPort = 5;
        final Integer newHttpsPort = 4;
        final Integer newRedirectPort = 3;
        final Integer newShutdownPort = 2;
        final Integer newAjpPort = 1;
        final Path newStatusPath = new Path("/def");
        final String newSystemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";

        final Jvm updatedJvm = jvmHelper.updateJvm(jvm.getId(),
                                                   newJvmName,
                                                   newHostName,
                                                   newHttpPort,
                                                   newHttpsPort,
                                                   newRedirectPort,
                                                   newShutdownPort,
                                                   newAjpPort,
                                                   userId,
                                                   newStatusPath,
                                                   newSystemProperties);

        assertEquals(jvm.getId(),
                     updatedJvm.getId());
        assertEquals(newJvmName,
                     updatedJvm.getJvmName());
        assertEquals(newHostName,
                     updatedJvm.getHostName());
        assertEquals(newHttpPort,
                     updatedJvm.getHttpPort());
        assertEquals(newHttpsPort,
                     updatedJvm.getHttpsPort());
        assertEquals(newRedirectPort,
                     updatedJvm.getRedirectPort());
        assertEquals(newShutdownPort,
                     updatedJvm.getShutdownPort());
        assertEquals(newAjpPort,
                     updatedJvm.getAjpPort());
        assertEquals(newStatusPath,
                     updatedJvm.getStatusPath());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateJvmWithDuplicateName() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                            "A Host Name",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        final Jvm secondJvm = jvmHelper.createJvm("A different Jvm Name",
                                                  "A different Host Name",
                                                  5, 4, 3, 2, 1,
                                                  userId,
                                                  new Path("/abc"),
                                                  "EXAMPLE_OPTS=%someEnv%/someVal");

        jvmHelper.updateJvm(secondJvm.getId(),
                            jvm.getJvmName(),
                            "Some different Host Name",
                            5, 4, 3, 2, 1,
                            userId,
                            new Path("/abc"),
                            "EXAMPLE_OPTS=%someEnv%/someVal");
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistentJvm() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        jvmHelper.updateJvm(nonExistentJvm,
                            "New Jvm Name",
                            "New Host Name",
                            5, 4, 3, 2, 1,
                            userId,
                            new Path("/abc"),
                            "EXAMPLE_OPTS=%someEnv%/someVal");
    }

    @Test
    public void testGetJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                            "A Host Name",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        final Jvm theSameJvm = jvmPersistenceService.getJvm(jvm.getId());

        assertEquals(jvm,
                     theSameJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentJvm() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        jvmPersistenceService.getJvm(nonExistentJvm);
    }

    @Test
    public void testGetJvms() {

        final int numberToCreate = 10;

        for (int i = 1; i <= numberToCreate; i++) {
            jvmHelper.createJvm("Auto-created JVM Name " + i,
                                "Auto-created Host Name " + i,
                                5, 4, 3, 2, 1,
                                userId,
                                new Path("/abc"),
                                "EXAMPLE_OPTS=%someEnv%/someVal");
        }

        final List<Jvm> jvms = jvmPersistenceService.getJvms();

        assertTrue(jvms.size() >= numberToCreate);
    }

    @Test
    public void testFindJvms() {

        final int numberToCreate = 10;
        final int numberToFind = 5;
        final String findable = "Findable";
        final String hidden = "Hidden";

        for (int i = 1; i <= numberToCreate; i++) {
            final String name;
            if (i <= numberToFind) {
                name = findable;
            } else {
                name = hidden;
            }
            jvmHelper.createJvm("Auto-created JVM " + name + i,
                                "Auto-created Host Name " + name + i,
                                5, 4, 3, 2, 1,
                                userId,
                                new Path("/abc"),
                                "EXAMPLE_OPTS=%someEnv%/someVal");
        }

        final List<Jvm> jvms = jvmPersistenceService.findJvms(findable);

        assertTrue(jvms.size() >= numberToFind);
        for (final Jvm jvm : jvms) {
            assertFalse(jvm.getJvmName().contains(hidden));
            assertTrue(jvm.getJvmName().contains(findable));
        }
    }

    @Test
    public void testFindJvmsBelongingTo() {

        final Group group = groupHelper.createGroup("Pre-created Group",
                                                    userId);
        final Identifier<Group> groupId = group.getId();
        final int numberToCreate = 10;
        final Set<Identifier<Jvm>> assignedJvms = new HashSet<>();

        for (int i = 1; i <= numberToCreate; i++) {
            final Jvm jvm = jvmHelper.createJvm("Auto-created JVM Name " + i,
                                                "Auto-crated Host Name " + i,
                                                5, 4, 3, 2, 1,
                                                userId,
                                                new Path("/abc"),
                                                "EXAMPLE_OPTS=%someEnv%/someVal");
            final Identifier<Jvm> jvmId = jvm.getId();

            assignedJvms.add(jvmId);
            groupHelper.addJvmToGroup(groupId,
                                      jvmId,
                                      userId);
        }

        final List<Jvm> jvms = jvmPersistenceService.findJvmsBelongingTo(groupId);

        assertTrue(jvms.size() >= numberToCreate);

        final Set<Identifier<Jvm>> foundJvms = new HashSet<>();
        for (final Jvm jvm : jvms) {
            foundJvms.add(jvm.getId());
        }
        for (final Identifier<Jvm> id : assignedJvms) {
            assertTrue(foundJvms.contains(id));
        }
    }

    @Test
    public void testRemoveJvm() {

        final Jvm jvm = jvmHelper.createJvm("JVM to Remove",
                                            "Hostname to Remove",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        jvmPersistenceService.removeJvm(jvm.getId());

        try {
            jvmPersistenceService.getJvm(jvm.getId());
            fail("JVM should not have been found");
        } catch (final NotFoundException nfe) {
            assertTrue(true);
        }
    }

    @Test
    public void testRemoveJvmAssignedToAGroup() {

        final Jvm jvm = jvmHelper.createJvm("JVM assigned to group to Remove",
                                            "Hostname to Remove",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");

        final Group group = groupHelper.createGroup("Group to assign JVMs to",
                                                    userId);

        final Identifier<Jvm> jvmId = jvm.getId();

        groupHelper.addJvmToGroup(group.getId(),
                                  jvmId,
                                  userId);

        jvmPersistenceService.removeJvm(jvmId);
    }

    @Test
    public void testRemoveJvmFromAllGroups() {

        final Jvm jvm = jvmHelper.createJvm("A new JVM",
                                            "A host name",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");
        final Identifier<Jvm> jvmId = jvm.getId();
        final Group firstGroup = groupHelper.createGroup("Group 1",
                                                         userId);
        final Group secondGroup = groupHelper.createGroup("Group 2",
                                                          userId);

        groupHelper.addJvmToGroup(firstGroup.getId(),
                                  jvmId,
                                  userId);
        groupHelper.addJvmToGroup(secondGroup.getId(),
                                  jvmId,
                                  userId);

        final Set<Identifier<Group>> assignedGroups = new HashSet<>();
        assignedGroups.add(firstGroup.getId());
        assignedGroups.add(secondGroup.getId());

        final Jvm jvmWithGroups = jvmPersistenceService.getJvm(jvmId);

        assertFalse(jvmWithGroups.getGroups().isEmpty());
        for (final Group group : jvmWithGroups.getGroups()) {
            assertTrue(assignedGroups.contains(group.getId()));
        }

        jvmPersistenceService.removeJvmFromGroups(jvmId);

        final Jvm jvmWithoutGroups = jvmPersistenceService.getJvm(jvmId);

        assertTrue(jvmWithoutGroups.getGroups().isEmpty());
    }
}
