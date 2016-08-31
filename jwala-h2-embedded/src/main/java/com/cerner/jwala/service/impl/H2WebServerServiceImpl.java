package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbServerServiceException;
import com.cerner.jwala.service.H2ServerType;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * H2 Web Server implementation of {@link AbstractH2ServerServiceImpl}
 *
 * Created by JC043760 on 8/30/2016
 */
public class H2WebServerServiceImpl extends AbstractH2ServerServiceImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2WebServerServiceImpl.class);
    private static final String DEFAULT_WEBSERVER_PARAM = "-webSSL,-webPort,8084";

    public H2WebServerServiceImpl(final String webServerParams) {
        super(webServerParams == null ? DEFAULT_WEBSERVER_PARAM : webServerParams, H2ServerType.WEB);
        if (webServerParams == null) {
            LOGGER.warn("webServerParams is null, loading default webServerParams values \"{}\"", DEFAULT_WEBSERVER_PARAM);
        }
    }

    @Override
    protected Server createServer(final String [] serverParams) throws DbServerServiceException {
        try {
            return Server.createWebServer(serverParams);
        } catch (final SQLException e) {
            LOGGER.error("Failed to create H2 Web Server!", e);
            throw new DbServerServiceException(e);
        }
    }
}
