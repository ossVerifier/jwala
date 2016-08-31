package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbServerService;
import com.cerner.jwala.service.DbServerServiceException;
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

    private Server server;

    private final String [] serverParams;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractH2ServerServiceImpl(final String serverParams) {
        this.serverParams = serverParams.replaceAll(" ", "").split(",");
    }

    @Override
    public void startServer() {
        if (server == null) {
            server = createServer(serverParams);
            logger.info("Created H2 server with the following parameters: {}", serverParams);
        }

        logger.info("Starting H2 server...");
        try {
            server = server.start();
            logger.info("H2 server started");
        } catch (final SQLException e) {
            logger.error("Failed to start H2 server!", e);
            throw new DbServerServiceException(e);
        }
    }

    @Override
    public void stopServer() {
        if (server != null && server.isRunning(true)) {
            logger.info("Stopping H2 Server...");
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
