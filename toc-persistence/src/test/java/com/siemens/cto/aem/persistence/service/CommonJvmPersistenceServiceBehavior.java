package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;

import java.util.Collections;

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
                         final Path aStatusPath,
                         final String aSystemProperties) {

        final Event<CreateJvmRequest> event = createCreateJvmEvent(aJvmName,
                                                                   aHostName,
                                                                   aHttpPort,
                                                                   aHttpsPort,
                                                                   aRedirectPort,
                                                                   aShutdownPort,
                                                                   aAjpPort,
                                                                   aUserId,
                                                                   aStatusPath,
                                                                   aSystemProperties);

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
                         final Path aStatusPath,
                         final String aSystemProperties) {

        final Event<UpdateJvmRequest> event = createUpdateJvmEvent(aJvmId,
                                                                   aNewJvmName,
                                                                   aNewHostName,
                                                                   aNewHttpPort,
                                                                   aNewHttpsPort,
                                                                   aNewRedirectPort,
                                                                   aNewShutdownPort,
                                                                   aNewAjpPort,
                                                                   aUserId,
                                                                   aStatusPath,
                                                                   aSystemProperties);

        return jvmPersistenceService.updateJvm(event);
    }

    protected Event<CreateJvmRequest> createCreateJvmEvent(final String aJvmName,
                                                           final String aJvmHostName,
                                                           final Integer httpPort,
                                                           final Integer httpsPort,
                                                           final Integer redirectPort,
                                                           final Integer shutdownPort,
                                                           final Integer ajpPort,
                                                           final String aUserId,
                                                           final Path aStatusPath,
                                                           final String aSystemProperties) {

        final Event<CreateJvmRequest> createJvm = new Event<>(new CreateJvmRequest(aJvmName,
                                                                                   aJvmHostName,
                                                                                   httpPort,
                                                                                   httpsPort,
                                                                                   redirectPort,
                                                                                   shutdownPort,
                                                                                   ajpPort,
                                                                                   aStatusPath,
                                                                                   aSystemProperties
                                                                            ),
                                                              createAuditEvent(aUserId));

        return createJvm;
    }

    protected Event<UpdateJvmRequest> createUpdateJvmEvent(final Identifier<Jvm> aJvmId,
                                                           final String aNewJvmName,
                                                           final String aNewHostName,
                                                           final Integer aNewHttpPort,
                                                           final Integer aNewHttpsPort,
                                                           final Integer aNewRedirectPort,
                                                           final Integer aNewShutdownPort,
                                                           final Integer aNewAjpPort,
                                                           final String aUserId,
                                                           final Path aStatusPath,
                                                           final String systemProperties) {

        final Event<UpdateJvmRequest> event = new Event<>(new UpdateJvmRequest(aJvmId,
                                                                               aNewJvmName,
                                                                               aNewHostName,
                                                                               Collections.<Identifier<Group>>emptySet(),
                                                                               aNewHttpPort,
                                                                               aNewHttpsPort,
                                                                               aNewRedirectPort,
                                                                               aNewShutdownPort,
                                                                               aNewAjpPort,
                                                                               aStatusPath,
                                                                               systemProperties),
                                                          createAuditEvent(aUserId));

        return event;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }
}