package com.cerner.jwala.service.jvm.operation;

/**
 * Created by JC043760 on 12/16/2016
 */
public interface JvmOperationService {

    void start(String jvmName);

    void stop(String jvmName);

}
