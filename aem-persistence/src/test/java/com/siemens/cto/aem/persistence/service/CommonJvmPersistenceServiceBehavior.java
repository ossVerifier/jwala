package com.siemens.cto.aem.persistence.service;

import java.util.Collections;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;

public class CommonJvmPersistenceServiceBehavior {

    private final JvmPersistenceService jvmPersistenceService;

    public CommonJvmPersistenceServiceBehavior(final JvmPersistenceService theJvmPersistenceService) {
        jvmPersistenceService = theJvmPersistenceService;
    }

    public Jvm createJvm(final String aJvmName,
                         final String aHostName,
                         final Integer aHttpPort,
                         final Integer aHttpsPort,
                         final Integer aRedirectPort,
                         final Integer aShutdownPort,
                         final Integer aAjpPort,
                         final String aUserId,
                         final Path aStatusPath) {

        final Event<CreateJvmCommand> event = createCreateJvmEvent(aJvmName,
                                                                   aHostName,
                                                                   aHttpPort,
                                                                   aHttpsPort,
                                                                   aRedirectPort,
                                                                   aShutdownPort,
                                                                   aAjpPort,
                                                                   aUserId,
                                                                   aStatusPath);

        return jvmPersistenceService.createJvm(event);
    }

    public Jvm updateJvm(final Identifier<Jvm> aJvmId,
                         final String aNewJvmName,
                         final String aNewHostName,
                         final Integer aNewHttpPort,
                         final Integer aNewHttpsPort,
                         final Integer aNewRedirectPort,
                         final Integer aNewShutdownPort,
                         final Integer aNewAjpPort,
                         final String aUserId,
                         final Path aStatusPath) {

        final Event<UpdateJvmCommand> event = createUpdateJvmEvent(aJvmId,
                                                                   aNewJvmName,
                                                                   aNewHostName,
                                                                   aNewHttpPort,
                                                                   aNewHttpsPort,
                                                                   aNewRedirectPort,
                                                                   aNewShutdownPort,
                                                                   aNewAjpPort,
                                                                   aUserId,
                                                                   aStatusPath);

        return jvmPersistenceService.updateJvm(event);
    }

    protected Event<CreateJvmCommand> createCreateJvmEvent(final String aJvmName,
                                                           final String aJvmHostName,
                                                           final Integer httpPort,
                                                           final Integer httpsPort,
                                                           final Integer redirectPort,
                                                           final Integer shutdownPort,
                                                           final Integer ajpPort,
                                                           final String aUserId,
                                                           final Path aStatusPath) {

        final Event<CreateJvmCommand> createJvm = new Event<>(new CreateJvmCommand(aJvmName,
                                                                                   aJvmHostName,
                                                                                   httpPort,
                                                                                   httpsPort,
                                                                                   redirectPort,
                                                                                   shutdownPort,
                                                                                   ajpPort,
                                                                                   aStatusPath),
                                                              createAuditEvent(aUserId));

        return createJvm;
    }

    protected Event<UpdateJvmCommand> createUpdateJvmEvent(final Identifier<Jvm> aJvmId,
                                                           final String aNewJvmName,
                                                           final String aNewHostName,
                                                           final Integer aNewHttpPort,
                                                           final Integer aNewHttpsPort,
                                                           final Integer aNewRedirectPort,
                                                           final Integer aNewShutdownPort,
                                                           final Integer aNewAjpPort,
                                                           final String aUserId,
                                                           final Path aStatusPath) {

        final Event<UpdateJvmCommand> event = new Event<>(new UpdateJvmCommand(aJvmId,
                                                                               aNewJvmName,
                                                                               aNewHostName,
                                                                               Collections.<Identifier<Group>>emptySet(),
                                                                               aNewHttpPort,
                                                                               aNewHttpsPort,
                                                                               aNewRedirectPort,
                                                                               aNewShutdownPort,
                                                                               aNewAjpPort,
                                                                               aStatusPath),
                                                          createAuditEvent(aUserId));

        return event;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }
}