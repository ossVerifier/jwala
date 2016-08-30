package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.SQLException;

/**
 * Implements {@link DbService}
 *
 * Created by JC043760 on 8/25/2016
 */
public class H2ServiceImpl implements DbService {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2ServiceImpl.class);

    private boolean serviceInitialized;

    private Server h2TcpServer;
    private Server h2WebServer;

    @Value("${h2.tcp.server.params:-tcpPort,9094,-tcpAllowOthers}")
    private String tcpServerParams;

    @Value("${h2.web.server.params:-webSSL,-webPort,8084,-webAllowOthers}")
    private String webServerParams;

    @Override
    public void startServer() {
        if (!serviceInitialized) {
            serviceInitialized = init();
        }

        if (serviceInitialized) {
            if (h2TcpServer != null) {
                LOGGER.info("Starting H2 TCP server...");
                try {
                    h2TcpServer = h2TcpServer.start();
                    LOGGER.info("H2 TCP server started");
                } catch (final SQLException e) {
                    LOGGER.error("Failed to start H2 TCP server!", e);
                    return; // there's no need to continue once H2 TCP server startup fails!
                }
            }

            if (h2WebServer != null) {
                LOGGER.info("Starting H2 Web server...");
                try {
                    h2WebServer = h2WebServer.start();
                    LOGGER.info("H2 Web server started");
                } catch (final SQLException e) {
                    LOGGER.error("Failed to start H2 Web server!", e);
                }
            }
        }
    }

    @Override
    public void stopServer() {
        if (h2TcpServer != null && h2TcpServer.isRunning(true)) {
            LOGGER.info("Stopping H2 TCP Server...");
            h2TcpServer.stop();
        }
        if (h2WebServer != null && h2WebServer.isRunning(true)) {
            LOGGER.info("Stopping H2 Web Server...");
            h2WebServer.stop();
        }
    }

    /**
     * Create the H2 servers
     *
     * @return true if successful
     */
    private boolean init() {
        try {
            h2TcpServer = Server.createTcpServer(tcpServerParams.replaceAll(" ", "").split(","));
            LOGGER.info("Created H2 TCP server with the following parameters: {}", tcpServerParams);
        } catch (final SQLException e) {
            LOGGER.error("Failed to create H2 TCP Server!", e);
            return false;
        }

        try {
            h2WebServer = Server.createWebServer(webServerParams.replaceAll(" ", "").split(","));
            LOGGER.info("Created H2 web server with the following parameters: {}", webServerParams);
        } catch (final SQLException e) {
            LOGGER.error("Failed to create H2 Web Server!", e);
            return false;
        }

        return true;
    }
}
