package com.siemens.cto.aem.ws.rest.v2.service.jvm;

import com.siemens.cto.aem.service.jvm.JvmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * Implements {JvmServiceRest}
 *
 * Created by JC043760 on 8/9/2016.
 */
public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);

    private final JvmService jvmService;

    public JvmServiceRestImpl(final JvmService jvmService) {
        this.jvmService = jvmService;
    }

    @Override
    public Response getJvm(final String name) {
        LOGGER.debug("Get JVM requested: {}", name);
        return Response.ok(jvmService.getJvm(name)).build();
    }

    @Override
    public Response createJvm(final JvmRequestData jvmRequestData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response updateJvm(final JvmRequestData jvmRequestData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response removeJvm(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response controlJvm(final String name, final JvmControlDataRequest jvmControlDataRequest) {
        throw new UnsupportedOperationException();
    }
}
