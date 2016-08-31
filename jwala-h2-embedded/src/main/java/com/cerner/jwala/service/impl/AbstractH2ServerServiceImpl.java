package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbServerService;
import com.cerner.jwala.service.DbServerServiceException;
import com.cerner.jwala.service.H2ServerType;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Implement {@link DbServerService}
 *
 * Created by JC043760 on 8/30/2016
 */
abstract class AbstractH2ServerServiceImpl implements DbServerService {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2TcpServerServiceImpl.class);

    private Server server;

    private final String [] serverParams;
    private final H2ServerType serverType;

    public AbstractH2ServerServiceImpl(final String serverParams, final H2ServerType serverType) {
        this.serverParams = serverParams.replaceAll(" ", "").split(",");
        this.serverType = serverType;
    }

    @Override
    public void startServer() {
        if (server == null) {
            server = createServer(serverParams);
            LOGGER.info("Created H2 {} server with the following parameters: {}", serverType, serverParams);
        }

        LOGGER.info("Starting H2 {} server...", serverType);
        try {
            server = server.start();
            LOGGER.info("H2 {} server started", serverType);
        } catch (final SQLException e) {
            LOGGER.error("Failed to start H2 {} server!", serverType, e);
            throw new DbServerServiceException(e);
        }
    }

    @Override
    public void stopServer() {
        if (server != null && server.isRunning(true)) {
            LOGGER.info("Stopping H2 {} Server...", serverType);
            server.stop();
        }
    }

    @Override
    public boolean isServerRunning() {
        return server != null && server.isRunning(true);
    }

    /**
     * Creates the H2 server
     *
     * @param serverParams array that contains server parameters
     * @return the {@link Server}
     * @throws DbServerServiceException
     */
    protected abstract Server createServer(final String [] serverParams) throws DbServerServiceException;
}
