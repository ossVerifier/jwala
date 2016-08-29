package com.cerner.jwala.tomcat.listener.db;

/**
 * Contract for Db related operations
 *
 * Created by JC043760 on 8/25/2016
 */
public interface DbService {

    void startServer();

    void stopServer();

    boolean isRunning();
}
