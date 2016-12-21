package com.cerner.jwala.service.jvm.operation;

/**
 * Defines the JVM operations
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public interface JvmOperationService {

    /**
     * Start jvm on host
     * @param jvmName
     */
    void start(String jvmName);

    /**
     * Stop jvm on host
     * @param jvmName
     */
    void stop(String jvmName);

}
