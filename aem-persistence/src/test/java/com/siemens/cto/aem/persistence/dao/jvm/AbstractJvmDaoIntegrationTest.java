package com.siemens.cto.aem.persistence.dao.jvm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Transactional
public abstract class AbstractJvmDaoIntegrationTest {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private JvmDao jvmDao;

    private Group preCreatedGroup;
    private Jvm preCreatedJvm;
    private String userName;

    @Before
    public void setup() {

        userName = "jvmTestUserName";

        preCreatedGroup = groupDao.createGroup(createGroupCommandEvent("Pre-created Group Name",
                                                                       userName));

        preCreatedJvm = jvmDao.createJvm(createCreateJvmCommand(
                "Pre-created JVM Name",
                                                                "Pre-created Host Name",
                                                                userName));
    }

    @Test
    public void testCreateNewJvm() {

        final Event<CreateJvmCommand> command = createCreateJvmCommand(
                "New Jvm Name",
                                                                       "New Host Name",
                                                                       userName);

        final Jvm createdJvm = jvmDao.createJvm(command);

        assertEquals(command.getCommand().getJvmName(),
                     createdJvm.getJvmName());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDuplicateJvm() {

        final Event<CreateJvmCommand> commandEvent = createCreateJvmCommand(
                preCreatedJvm.getJvmName(),
                                                                            preCreatedJvm.getHostName(),
                                                                            userName);
        jvmDao.createJvm(commandEvent);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveJvm() {

        jvmDao.removeJvm(preCreatedJvm.getId());

        final Jvm jvm = jvmDao.getJvm(preCreatedJvm.getId());
        fail("JVM should not exist");
    }

    @Test
    public void testRemoveJvmsFromGroup() {

        final Jvm secondJvm = jvmDao.createJvm(createCreateJvmCommand("Second JVM",
                                                                      "Second Host Name",
                                                                      userName));

        jvmDao.removeJvmsBelongingTo(preCreatedGroup.getId());

        final List<Identifier<Jvm>> jvms = Arrays.asList(preCreatedJvm.getId(),
                                                         secondJvm.getId());
        for (final Identifier<Jvm> jvm : jvms) {
            try {
                jvmDao.getJvm(jvm);
                fail("JVM should not exist");
            } catch(final NotFoundException nfe) {
                //Success
                continue;
            }
        }
    }

    @Test
    public void testUpdateJvm() {

        final Event<UpdateJvmCommand> update = createUpdateJvmCommand(preCreatedJvm.getId(),
                                                                      "New Jvm Name",
                                                                      "New Host Name",
                                                                      userName);

        final Jvm actualJvm = jvmDao.updateJvm(update);

        assertEquals(update.getCommand().getNewJvmName(),
                     actualJvm.getJvmName());
        assertEquals(update.getCommand().getNewHostName(),
                     actualJvm.getHostName());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateDuplicateJvm() {

        final Jvm newJvm = jvmDao.createJvm(createCreateJvmCommand(
                "Eventually duplicate JVM name",
                                                                   "Unused",
                                                                   userName));

        jvmDao.updateJvm(createUpdateJvmCommand(newJvm.getId(),
                                                preCreatedJvm.getJvmName(),
                                                preCreatedJvm.getHostName(),
                                                userName));
    }

    @Test
    public void testFindJvmsByName() {

        final int numberToCreate = 10;
        final int numberActive = 4;

        final String activeSuffix = "Active";
        final String passiveSuffix = "Passive";

        createMultipleJvms(
                numberActive,
                           activeSuffix,
                           activeSuffix);
        createMultipleJvms(
                numberToCreate - numberActive,
                           passiveSuffix,
                           passiveSuffix);

        final List<Jvm> activeJvms = jvmDao.findJvms(activeSuffix,
                                                     new PaginationParameter(0, numberToCreate));

        final List<Jvm> passiveJvms = jvmDao.findJvms(passiveSuffix,
                                                      new PaginationParameter(0, numberToCreate));

        verifyBulkJvmAssertions(activeJvms,
                                numberActive,
                                activeSuffix,
                                activeSuffix);
        verifyBulkJvmAssertions(passiveJvms,
                                numberToCreate - numberActive,
                                passiveSuffix,
                                passiveSuffix);
    }

    @Test
    public void testFindJvmsBelongingTo() {

        final int numberInFirstGroup = 9;
        final int numberInFirstGroupToFind = numberInFirstGroup - 3;
        final int numberInSecondGroup = 7;
        final String firstGroupSuffix = "FirstGroup";
        final String secondGroupSuffix = "SecondGroup";

        final Group firstGroup = groupDao.createGroup(createGroupCommandEvent("First Group",
                                                                              userName));
        final Group secondGroup = groupDao.createGroup(createGroupCommandEvent("Second Group",
                                                                               userName));

        createMultipleJvms(
                numberInFirstGroup,
                           firstGroupSuffix,
                           firstGroupSuffix);
        createMultipleJvms(
                numberInSecondGroup,
                           secondGroupSuffix,
                           secondGroupSuffix);

        final List<Jvm> firstGroupJvms = jvmDao.findJvmsBelongingTo(firstGroup.getId(),
                                                                    new PaginationParameter(0, numberInFirstGroupToFind));
        verifyBulkJvmAssertions(firstGroupJvms,
                                numberInFirstGroupToFind,
                                firstGroupSuffix,
                                firstGroupSuffix);

        final List<Jvm> secondGroupJvms = jvmDao.findJvmsBelongingTo(secondGroup.getId(),
                                                                     new PaginationParameter((numberInSecondGroup - 1), numberInSecondGroup));

        verifyBulkJvmAssertions(secondGroupJvms,
                                1,
                                secondGroupSuffix,
                                secondGroupSuffix);
    }

    @Test
    public void testGetJvms() {

        final int numberToCreate = 7;
        final String suffix = "GET_JVM_SUFFIX";

        final Group group = groupDao.createGroup(createGroupCommandEvent("Get JVMs Group",
                                                                         userName));

        createMultipleJvms(
                numberToCreate,
                           suffix,
                           suffix);

        final List<Jvm> jvms = jvmDao.getJvms(new PaginationParameter(0, numberToCreate));

        assertEquals(numberToCreate,
                     jvms.size());
    }

    protected Event<CreateGroupCommand> createGroupCommandEvent(final String aGroupName,
                                                                final String aUserName) {
        return new Event<>(new CreateGroupCommand(aGroupName),
                           AuditEvent.now(new User(aUserName)));
    }

    protected Event<CreateJvmCommand> createCreateJvmCommand(final String aJvmName,
                                                             final String aHostName,
                                                             final String aUserName) {
        return new Event<>(new CreateJvmCommand(aJvmName,
                                                aHostName),
                           AuditEvent.now(new User(aUserName)));
    }

    protected Event<UpdateJvmCommand> createUpdateJvmCommand(final Identifier<Jvm> aJvmId,
                                                             final String aNewJvmName,
                                                             final String aNewHostName,
                                                             final String aUserName) {
        return new Event<>(new UpdateJvmCommand(aJvmId,
                                                aNewJvmName,
                                                aNewHostName,
                                                Collections.<Identifier<Group>>emptySet()),
                           AuditEvent.now(new User(aUserName)));
    }

    protected void verifyBulkJvmAssertions(final List<Jvm> someJvms,
                                           final int anExpectedSize,
                                           final String anExpectedJvmSuffix,
                                           final String anExpectedHostNameSuffix) {

        assertEquals(anExpectedSize,
                     someJvms.size());
        for (final Jvm jvm : someJvms) {
            assertTrue(jvm.getJvmName().contains(anExpectedJvmSuffix));
            assertTrue(jvm.getHostName().contains(anExpectedHostNameSuffix));
        }
    }

    protected void createMultipleJvms(final int aNumberToCreate,
                                      final String aJvmNameSuffix,
                                      final String aHostNameSuffix) {
        for (int i=1; i <= aNumberToCreate; i++) {
            jvmDao.createJvm(createCreateJvmCommand("JVM" + i + aJvmNameSuffix,
                                                    "HostName" + i + aHostNameSuffix,
                                                    userName));
        }
    }
}
