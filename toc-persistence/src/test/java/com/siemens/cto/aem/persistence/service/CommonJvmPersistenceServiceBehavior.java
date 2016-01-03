package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;

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

        final CreateJvmRequest createJvmRequest = createCreateJvmRequest(aJvmName,
                aHostName,
                aHttpPort,
                aHttpsPort,
                aRedirectPort,
                aShutdownPort,
                aAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties);

        return jvmPersistenceService.createJvm(createJvmRequest);
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

        final UpdateJvmRequest updateJvmRequest = createUpdateJvmRequest(aJvmId,
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

        return jvmPersistenceService.updateJvm(updateJvmRequest);
    }

    protected CreateJvmRequest createCreateJvmRequest(final String aJvmName,
                                                      final String aJvmHostName,
                                                      final Integer httpPort,
                                                      final Integer httpsPort,
                                                      final Integer redirectPort,
                                                      final Integer shutdownPort,
                                                      final Integer ajpPort,
                                                      final String aUserId,
                                                      final Path aStatusPath,
                                                      final String aSystemProperties) {

        return new CreateJvmRequest(aJvmName,
                aJvmHostName,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                aStatusPath,
                aSystemProperties
        );
    }

    protected UpdateJvmRequest createUpdateJvmRequest(final Identifier<Jvm> aJvmId,
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

        return new UpdateJvmRequest(aJvmId,
                aNewJvmName,
                aNewHostName,
                Collections.<Identifier<Group>>emptySet(),
                aNewHttpPort,
                aNewHttpsPort,
                aNewRedirectPort,
                aNewShutdownPort,
                aNewAjpPort,
                aStatusPath,
                systemProperties);
    }
}