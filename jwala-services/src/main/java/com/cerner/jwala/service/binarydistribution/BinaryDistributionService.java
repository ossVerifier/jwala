package com.cerner.jwala.service.binarydistribution;

/**
 * Created by SP043299 on 9/6/2016.
 */
public interface BinaryDistributionService {
    void distributeJdk(final String hostname);

    void distributeTomcat(final String hostname);

    void distributeWebServer(final String hostname);

    String zipBinary(final String location);

    void prepareUnzip(final String hostname);
}
