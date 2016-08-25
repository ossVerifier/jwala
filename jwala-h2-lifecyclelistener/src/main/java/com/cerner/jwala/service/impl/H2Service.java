package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link DbService}
 *
 * Created by JC043760 on 8/25/2016
 */
public class H2Service implements DbService {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2Service.class);

    public H2Service() {
        LOGGER.info("H2 service initialized");
    }

    @Override
    public void startServer() {
        LOGGER.info("Starting H2...");
    }

    @Override
    public void stopServer() {
        LOGGER.info("Stopping H2...");
    }
}
