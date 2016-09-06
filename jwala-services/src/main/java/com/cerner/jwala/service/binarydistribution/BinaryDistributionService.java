package com.cerner.jwala.service.binarydistribution;

/**
 * Created by SP043299 on 9/6/2016.
 */
public interface BinaryDistributionService {
    void distributeJdk(String hostname);

    void distributeTomcat(String hostname);

    void distributeWebServer(String hostname);

    boolean jdkExists(String hostname);

    boolean tomcatExists(String hostname);

    boolean webServerExists(String hostname);

    void zipJdk();

    void zipTomcat();

    void zipWebServer();
}
