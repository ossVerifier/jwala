package com.siemens.cto.aem.persistence.service.jvm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.service.CommonGroupPersistenceServiceBehavior;
import com.siemens.cto.aem.persistence.service.CommonJvmPersistenceServiceBehavior;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
                                         userId);

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
                                                    userId);

        final Jvm duplicateNameJvm = jvmHelper.createJvm(existingJvm.getJvmName(),
                                                         "A different Host Name",
                                                         userId);
    }

    @Test
    public void testUpdateJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                         "A Host Name",
                                         userId);

        final String newJvmName = "A New Jvm Name";
        final String newHostName = "A New Host Name";

        final Jvm updatedJvm = jvmHelper.updateJvm(jvm.getId(),
                                                newJvmName,
                                                newHostName,
                                                userId);

        assertEquals(jvm.getId(),
                     updatedJvm.getId());
        assertEquals(newJvmName,
                     updatedJvm.getJvmName());
        assertEquals(newHostName,
                     updatedJvm.getHostName());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateJvmWithDuplicateName() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                            "A Host Name",
                                            userId);

        final Jvm secondJvm = jvmHelper.createJvm("A different Jvm Name",
                                                  "A different Host Name",
                                                  userId);

        jvmHelper.updateJvm(secondJvm.getId(),
                            jvm.getJvmName(),
                            "Some different Host Name",
                            userId);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistentJvm() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        jvmHelper.updateJvm(nonExistentJvm,
                         "New Jvm Name",
                         "New Host Name",
                         userId);
    }

    @Test
    public void testGetJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name",
                                         "A Host Name",
                                         userId);

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
                             userId);
        }

        final List<Jvm> jvms = jvmPersistenceService.getJvms(PaginationParameter.all());

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
                             userId);
        }

        final List<Jvm> jvms = jvmPersistenceService.findJvms(findable,
                                                              PaginationParameter.all());

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
                                                userId);
            final Identifier<Jvm> jvmId = jvm.getId();

            assignedJvms.add(jvmId);
            groupHelper.addJvmToGroup(groupId,
                                      jvmId,
                                      userId);
        }

        final List<Jvm> jvms = jvmPersistenceService.findJvmsBelongingTo(groupId,
                                                                         PaginationParameter.all());

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
                                            userId);

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
                                            userId);

        final Group group = groupHelper.createGroup("Group to assign JVMs to",
                                                    userId);

        final Identifier<Jvm> jvmId = jvm.getId();

        groupHelper.addJvmToGroup(group.getId(),
                                  jvmId,
                                  userId);

        jvmPersistenceService.removeJvm(jvmId);
    }
}
