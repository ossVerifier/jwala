package com.siemens.cto.aem.persistence.service;

import java.util.Collections;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;

public class CommonJvmPersistenceServiceBehavior {

    private final JvmPersistenceService jvmPersistenceService;

    public CommonJvmPersistenceServiceBehavior(final JvmPersistenceService theJvmPersistenceService) {
        jvmPersistenceService = theJvmPersistenceService;
    }

    public Jvm createJvm(final String aJvmName,
                         final String aHostName,
                         final String aUserId) {

        final Event<CreateJvmCommand> event = createCreateJvmEvent(aJvmName,
                                                                   aHostName,
                                                                   aUserId);

        return jvmPersistenceService.createJvm(event);
    }

    public Jvm updateJvm(final Identifier<Jvm> aJvmId,
                         final String aNewJvmName,
                         final String aNewHostName,
                         final String aUserId) {

        final Event<UpdateJvmCommand> event = createUpdateJvmEvent(aJvmId,
                                                                   aNewJvmName,
                                                                   aNewHostName,
                                                                   aUserId);

        return jvmPersistenceService.updateJvm(event);
    }

    protected Event<CreateJvmCommand> createCreateJvmEvent(final String aJvmName,
                                                           final String aJvmHostName,
                                                           final String aUserId) {

        final Event<CreateJvmCommand> createJvm = new Event<>(new CreateJvmCommand(aJvmName,
                                                                                   aJvmHostName),
                                                              createAuditEvent(aUserId));

        return createJvm;
    }

    protected Event<UpdateJvmCommand> createUpdateJvmEvent(final Identifier<Jvm> aJvmId,
                                                           final String aNewJvmName,
                                                           final String aNewHostName,
                                                           final String aUserId) {

        final Event<UpdateJvmCommand> event = new Event<>(new UpdateJvmCommand(aJvmId,
                                                                               aNewJvmName,
                                                                               aNewHostName,
                                                                               Collections.<Identifier<Group>>emptySet()),
                                                          createAuditEvent(aUserId));

        return event;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }
}