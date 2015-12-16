package com.siemens.cto.aem.persistence.service.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.group.Group;
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

import java.util.List;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractGroupPersistenceServiceIntegrationTest {

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    private CommonGroupPersistenceServiceBehavior groupHelper;
    private CommonJvmPersistenceServiceBehavior jvmHelper;
    private Group preCreatedGroup;
    private Jvm preCreatedJvm;
    private String userId;

    @Before
    public void setUp() throws Exception {

        groupHelper = new CommonGroupPersistenceServiceBehavior(groupPersistenceService);
        jvmHelper = new CommonJvmPersistenceServiceBehavior(jvmPersistenceService);

        userId = "Test User Id";

        preCreatedGroup = groupHelper.createGroup("Pre-Created GroupName",
                                                  userId);

        preCreatedJvm = jvmHelper.createJvm("Pre-Created JVM Name",
                                            "Pre-Created JVM Host Name",
                                            5, 4, 3, 2, 1,
                                            userId,
                                            new Path("/abc"),
                                            "EXAMPLE_OPTS=%someEnv%/someVal");
    }

    @Test
    public void testCreateGroup() {

        final String groupName = "newGroupName";

        final Group actualGroup = groupHelper.createGroup(groupName,
                                                     userId);

        assertEquals(groupName,
                     actualGroup.getName());
        assertNotNull(actualGroup.getId());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDuplicateGroup() {

        groupHelper.createGroup(preCreatedGroup.getName(),
                           userId);
    }

    @Test
    public void testUpdateGroup() {

        final String newGroupName = "My New Name";

        final Group actualGroup = groupHelper.updateGroup(preCreatedGroup.getId(),
                                                     newGroupName,
                                                     userId);



        assertEquals(newGroupName,
                     actualGroup.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupHelper.updateGroup(nonExistentGroupId,
                           "Unused",
                           userId);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateDuplicateGroup() {

        final Group newGroup = groupHelper.createGroup("Group Name to turn into a duplicate",
                                                  userId);

        groupHelper.updateGroup(newGroup.getId(),
                           preCreatedGroup.getName(),
                           userId);
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> expectedGroupIdentifier = preCreatedGroup.getId();

        final Group group = groupPersistenceService.getGroup(expectedGroupIdentifier);

        assertEquals(preCreatedGroup.getName(),
                     group.getName());
        assertEquals(expectedGroupIdentifier,
                     group.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentGroup() {

        groupPersistenceService.getGroup(new Identifier<Group>(-123456L));
    }

    @Test
    public void testGetGroups() {

        groupHelper.createGroup("Auto-constructed Group " + (1),
                           "Auto-constructed User " + (1));

        final List<Group> actualGroups = groupPersistenceService.getGroups();

        assertTrue(actualGroups.size() > 0);
    }

    @Test
    public void testFindGroups() {

        final String expectedContains = preCreatedGroup.getName().substring(3, 5);

        final List<Group> actualGroups = groupPersistenceService.findGroups(expectedContains);

        for(final Group group : actualGroups) {
            assertTrue(group.getName().contains(expectedContains));
        }
    }

    @Test
    public void testRemoveGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupPersistenceService.removeGroup(groupId);

        try {
            groupPersistenceService.getGroup(groupId);
        } catch (final NotFoundException nfe) {
            //Success (This could be declared as expected in the @Test annotation, but I want to verify
            //that removeGroup() actually succeeded and didn't throw a NotFoundException itself
            assertTrue(true);  //To avoid the empty catch block warning
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupPersistenceService.removeGroup(nonExistentGroupId);
    }

    @Test
    public void testAddJvmToGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupHelper.addJvmToGroup(groupId,
                             preCreatedJvm.getId(),
                             userId);

        final Group group = groupPersistenceService.getGroup(groupId);

        assertNotNull(group);
        assertNotNull(group.getJvms());
        assertFalse(group.getJvms().isEmpty());

        boolean foundJvm = false;
        for (final Jvm jvm : group.getJvms()) {
            if (jvm.getId().equals(preCreatedJvm.getId())) {
                foundJvm = true;
                break;
            }
        }
        assertTrue(foundJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testAddNonExistentJvmToGroup() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        groupHelper.addJvmToGroup(preCreatedGroup.getId(),
                             nonExistentJvm,
                             userId);
    }

    @Test(expected = NotFoundException.class)
    public void testAddJvmToNonExistentGroup() {

        final Identifier<Group> nonExistentGroup = new Identifier<>(-123456L);

        groupHelper.addJvmToGroup(nonExistentGroup,
                             preCreatedJvm.getId(),
                             userId);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveJvmFromNonExistentGroup() {

        final Identifier<Group> nonExistentGroup = new Identifier<>(-123456L);

        groupHelper.removeJvmFromGroup(nonExistentGroup,
                                  preCreatedJvm.getId(),
                                  userId);
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();
        final Identifier<Jvm> jvmId = preCreatedJvm.getId();

        groupHelper.addJvmToGroup(groupId,
                             jvmId,
                             userId);

        groupHelper.removeJvmFromGroup(groupId,
                                  jvmId,
                                  userId);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistentJvmFromGroup() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        groupHelper.removeJvmFromGroup(preCreatedGroup.getId(),
                                  nonExistentJvm,
                                  userId);
    }

    @Test
    public void testRemoveGroupWithRelationships() {

        final Jvm aSecondJvm = jvmHelper.createJvm("anotherJvmName",
                                                   "anotherJvmHostName",
                                                   5, 4, 3, 2, 1,
                                                   userId,
                                                   new Path("/abc"),
                                                   "EXAMPLE_OPTS=%someEnv%/someVal");

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupHelper.addJvmToGroup(groupId,
                             preCreatedJvm.getId(),
                             userId);

        groupHelper.addJvmToGroup(groupId,
                             aSecondJvm.getId(),
                             userId);

        groupPersistenceService.removeGroup(groupId);
    }
}
