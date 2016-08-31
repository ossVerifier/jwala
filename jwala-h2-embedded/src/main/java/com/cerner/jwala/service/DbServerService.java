package com.cerner.jwala.service;

/**
 * Contract for database server related operations
 *
 * Created by JC043760 on 8/25/2016
 */
public interface DbServerService {

    void startServer();

    void stopServer();

    boolean isServerRunning();
}
