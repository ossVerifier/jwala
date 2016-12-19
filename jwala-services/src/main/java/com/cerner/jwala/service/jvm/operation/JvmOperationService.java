package com.cerner.jwala.service.jvm.operation;

/**
 * Defines the JVM operations
 *
 * Created by JC043760 on 12/16/2016
 */
public interface JvmOperationService {

    void start(String jvmName);

    void stop(String jvmName);

}
