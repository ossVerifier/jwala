package com.cerner.jwala.service.binarydistribution;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */

public interface BinaryDistributionService {
    void distributeJdk(final String hostname);

    void distributeTomcat(final String hostname);

    void distributeWebServer(final String hostname);

    void prepareUnzip(final String hostname);
}
