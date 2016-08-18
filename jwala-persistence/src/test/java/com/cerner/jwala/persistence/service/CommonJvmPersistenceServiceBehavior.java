package com.cerner.jwala.persistence.service;

import java.util.Collections;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.persistence.service.JvmPersistenceService;

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
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword) {

        final CreateJvmRequest createJvmRequest = createCreateJvmRequest(aJvmName,
                aHostName,
                aHttpPort,
                aHttpsPort,
                aRedirectPort,
                aShutdownPort,
                aAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword);

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
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword) {

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
                aSystemProperties,
                aUserName,
                anEncryptedPassword);

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
                                                      final String aSystemProperties,
                                                      final String aUserName,
                                                      final String anEncryptedPassword) {

        return new CreateJvmRequest(aJvmName,
                aJvmHostName,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword);
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
                                                      final String systemProperties,  
                                                      final String aUserName,
                                                      final String anEncryptedPassword) {

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
                systemProperties, 
                aUserName,
                anEncryptedPassword);
    }
}