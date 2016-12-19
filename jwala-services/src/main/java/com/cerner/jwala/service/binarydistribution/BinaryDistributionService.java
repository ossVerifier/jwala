package com.cerner.jwala.service.binarydistribution;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionService {
    /**
     * Distribute jwala JDK to remote host
     * @param hostname remote host name
     *
     */
    void distributeJdk(final String hostname);

    /**
     * Distribute jwala tomcat binaries to remote host
     * @param hostname
     */
    void distributeTomcat(final String hostname);

    /**
     * Distribute jwala Apache http webserver to remote host
     * @param hostname
     */
    void distributeWebServer(final String hostname);

    /**
     * This method copies unzip.exe to remote host
     * @param hostname
     */
    void prepareUnzip(final String hostname);
}
