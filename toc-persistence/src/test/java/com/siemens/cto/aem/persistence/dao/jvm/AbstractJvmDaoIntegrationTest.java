package com.siemens.cto.aem.persistence.dao.jvm;

import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
// TODO: Use as a reference for the jvm CRUD integration test.
public abstract class AbstractJvmDaoIntegrationTest {

//    @Autowired
//    private JvmDao jvmDao;
//
//    private Jvm preCreatedJvm;
//    private String userName;
//
//    @Before
//    public void setup() {
//        userName = "jvmTestUserName";
//
//        preCreatedJvm =
//                jvmDao.createJvm(createCreateJvmCommand("Pre-created JVM Name",
//                                                        "Pre-created Host Name",
//                                                         5,
//                                                         4,
//                                                         3,
//                                                         2,
//                                                         1,
//                                                         userName,
//                                                         new Path("/abc"),
//                                                         "EXAMPLE_OPTS=%someEnv%/someVal"));
//    }
//
//    @Test
//    public void testCreateNewJvm() {
//        final Event<CreateJvmRequest> command = createCreateJvmCommand("New Jvm Name",
//                                                                       "New Host Name",
//                                                                        5,
//                                                                        4,
//                                                                        3,
//                                                                        2,
//                                                                        1,
//                                                                        userName,
//                                                                        new Path("/abc"),
//                                                                        "EXAMPLE_OPTS=%someEnv%/someVal");
//        final Jvm createdJvm = jvmDao.createJvm(command);
//
//        assertEquals(command.getRequest().getJvmName(), createdJvm.getJvmName());
//    }
//
//    @Test(expected = BadRequestException.class)
//    public void testCreateDuplicateJvm() {
//
//        final Event<CreateJvmRequest> commandEvent =
//                createCreateJvmCommand(preCreatedJvm.getJvmName(),
//                                       preCreatedJvm.getHostName(),
//                                       preCreatedJvm.getHttpPort(),
//                                       preCreatedJvm.getHttpsPort(),
//                                       preCreatedJvm.getRedirectPort(),
//                                       preCreatedJvm.getShutdownPort(),
//                                       preCreatedJvm.getAjpPort(),
//                                       userName,
//                                       preCreatedJvm.getStatusPath(),
//                                       preCreatedJvm.getSystemProperties());
//        jvmDao.createJvm(commandEvent);
//    }
//
//    @Test(expected = NotFoundException.class)
//    public void testRemoveJvm() {
//        jvmDao.removeJvm(preCreatedJvm.getId());
//
//        jvmDao.getJvm(preCreatedJvm.getId());
//        fail("JVM should not exist");
//    }
//
//    @Test
//    public void testUpdateJvm() {
//        final Event<UpdateJvmRequest> update =
//                createUpdateJvmCommand(preCreatedJvm.getId(), "New Jvm Name", "New Host Name", 5, 4, 3, 2, 1, userName,
//                        new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal");
//        final Jvm actualJvm = jvmDao.updateJvm(update);
//
//        assertEquals(update.getRequest().getNewJvmName(), actualJvm.getJvmName());
//        assertEquals(update.getRequest().getNewHostName(), actualJvm.getHostName());
//        assertEquals(update.getRequest().getNewHttpPort(), actualJvm.getHttpPort());
//        assertEquals(update.getRequest().getNewHttpsPort(), actualJvm.getHttpsPort());
//        assertEquals(update.getRequest().getNewRedirectPort(), actualJvm.getRedirectPort());
//        assertEquals(update.getRequest().getNewShutdownPort(), actualJvm.getShutdownPort());
//        assertEquals(update.getRequest().getNewAjpPort(), actualJvm.getAjpPort());
//        assertEquals(update.getRequest().getNewStatusPath(), actualJvm.getStatusPath());
//    }
//
//    @Test(expected = BadRequestException.class)
//    public void testUpdateDuplicateJvm() {
//        final Jvm newJvm =
//                jvmDao.createJvm(createCreateJvmCommand("Eventually duplicate JVM name", "Unused", 5, 4, 3, 2, 1,
//                        userName, new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal"));
//
//        jvmDao.updateJvm(createUpdateJvmCommand(newJvm.getId(),
//                                                preCreatedJvm.getJvmName(),
//                                                preCreatedJvm.getHostName(),
//                                                preCreatedJvm.getHttpPort(),
//                                                preCreatedJvm.getHttpsPort(),
//                                                preCreatedJvm.getRedirectPort(),
//                                                preCreatedJvm.getShutdownPort(),
//                                                preCreatedJvm.getAjpPort(),
//                                                userName,
//                                                preCreatedJvm.getStatusPath(),
//                                                preCreatedJvm.getSystemProperties()));
//    }
//
//    @Test
//    public void testFindJvmsByName() {
//        final int numberToCreate = 10;
//        final int numberActive = 4;
//
//        final String activeSuffix = "Active";
//        final String passiveSuffix = "Passive";
//        final Path statusPath = new Path("/abc");
//
//        createMultipleJvms(numberActive, activeSuffix, activeSuffix, statusPath);
//        createMultipleJvms(numberToCreate - numberActive, passiveSuffix, passiveSuffix, statusPath);
//
//        final List<Jvm> activeJvms = jvmDao.findJvms(activeSuffix);
//        final List<Jvm> passiveJvms = jvmDao.findJvms(passiveSuffix);
//
//        verifyBulkJvmAssertions(activeJvms, numberActive, activeSuffix, activeSuffix, 5, 4, 3, 2, 1, statusPath);
//        verifyBulkJvmAssertions(passiveJvms, numberToCreate - numberActive, passiveSuffix, passiveSuffix, 5, 4, 3, 2, 1, statusPath);
//    }
//
//    @Test
//    public void testGetJvms() {
//        final int numberToCreate = 7;
//        final String suffix = "GET_JVM_SUFFIX";
//        final Path statusPath = new Path("/abc");
//
//        createMultipleJvms(numberToCreate, suffix, suffix, statusPath);
//        final List<Jvm> jvms = jvmDao.getJvms();
//
//        assertTrue(jvms.size() > numberToCreate);
//    }
//
//    protected Event<CreateGroupRequest> createGroupCommandEvent(final String aGroupName, final String aUserName) {
//        return new Event<>(new CreateGroupRequest(aGroupName), AuditEvent.now(new User(aUserName)));
//    }
//
//    protected Event<CreateJvmRequest> createCreateJvmCommand(final String aJvmName,
//                                                             final String aHostName,
//                                                             final Integer httpPort,
//                                                             final Integer httpsPort,
//                                                             final Integer redirectPort,
//                                                             final Integer shutdownPort,
//                                                             final Integer ajpPort,
//                                                             final String aUserName,
//                                                             final Path aStatusPath,
//                                                             final String aSystemProperties) {
//        return new Event<>(new CreateJvmRequest(aJvmName,
//                                                aHostName,
//                                                httpPort,
//                                                httpsPort,
//                                                redirectPort,
//                                                shutdownPort,
//                                                ajpPort,
//                                                aStatusPath,
//                                                aSystemProperties),
//                           AuditEvent.now(new User(aUserName)));
//    }
//
//    protected Event<UpdateJvmRequest> createUpdateJvmCommand(final Identifier<Jvm> aJvmId,
//                                                             final String aNewJvmName,
//                                                             final String aNewHostName,
//                                                             final Integer aNewHttpPort,
//                                                             final Integer aNewHttpsPort,
//                                                             final Integer aNewRedirectPort,
//                                                             final Integer aNewShutdownPort,
//                                                             final Integer aNewAjpPort,
//                                                             final String aUserName,
//                                                             final Path aStatusPath,
//                                                             final String aSystemProperties) {
//        return new Event<>(new UpdateJvmRequest(aJvmId,
//                                                aNewJvmName,
//                                                aNewHostName,
//                                                Collections.<Identifier<Group>> emptySet(),
//                                                aNewHttpPort,
//                                                aNewHttpsPort,
//                                                aNewRedirectPort,
//                                                aNewShutdownPort,
//                                                aNewAjpPort,
//                                                aStatusPath,
//                                                aSystemProperties),
//                           AuditEvent.now(new User(aUserName)));
//    }
//
//    protected void verifyBulkJvmAssertions(final List<Jvm> someJvms,
//                                           final int anExpectedSize,
//                                           final String anExpectedJvmSuffix,
//                                           final String anExpectedHostNameSuffix,
//                                           final Integer httpPort,
//                                           final Integer httpsPort,
//                                           final Integer redirectPort,
//                                           final Integer shutdownPort,
//                                           final Integer ajpPort,
//                                           final Path aStatusPath) {
//
//        assertEquals(anExpectedSize, someJvms.size());
//        for (final Jvm jvm : someJvms) {
//            assertTrue(jvm.getJvmName().contains(anExpectedJvmSuffix));
//            assertTrue(jvm.getHostName().contains(anExpectedHostNameSuffix));
//
//            assertEquals(httpPort, jvm.getHttpPort());
//            assertEquals(httpsPort, jvm.getHttpsPort());
//            assertEquals(redirectPort, jvm.getRedirectPort());
//            assertEquals(shutdownPort, jvm.getShutdownPort());
//            assertEquals(ajpPort, jvm.getAjpPort());
//            assertEquals(aStatusPath, jvm.getStatusPath());
//        }
//    }
//
//    protected void createMultipleJvms(final int aNumberToCreate,
//                                      final String aJvmNameSuffix,
//                                      final String aHostNameSuffix,
//                                      final Path aStatusPath) {
//        for (int i = 1; i <= aNumberToCreate; i++) {
//            jvmDao.createJvm(createCreateJvmCommand("JVM" + i + aJvmNameSuffix, "HostName" + i + aHostNameSuffix,
//                                                    5,
//                                                    4,
//                                                    3,
//                                                    2,
//                                                    1,
//                                                    userName,
//                                                    aStatusPath,
//                                                    "EXAMPLE_OPTS=%someEnv%/someVal"));
//        }
//    }
}
