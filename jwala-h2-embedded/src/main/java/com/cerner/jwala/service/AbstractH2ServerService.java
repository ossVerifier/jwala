package com.cerner.jwala.service;

import com.cerner.jwala.service.DbServerServiceException;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * A partial contract for H2 database server functionality
 *
 * Created by JC043760 on 8/30/2016
 */
public abstract class AbstractH2ServerService {

    private Server server;

    private final String [] serverParams;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractH2ServerService(final String serverParams) {
        this.serverParams = serverParams.replaceAll(" ", "").split(",");
    }

    public void startServer() {
        if (server == null) {
            server = createServer(serverParams);
            logger.info("Created H2 server with the following parameters: {}", Arrays.toString(serverParams));
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

    public void stopServer() {
        if (server != null && server.isRunning(true)) {
            logger.info("Stopping H2 Server...");
            server.stop();
        }
    }

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
