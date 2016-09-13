package com.cerner.jwala.service.binarydistribution;

public interface BinaryDistributionService {
    void distributeJdk(final String hostname);

    void distributeTomcat(final String hostname);

    void distributeWebServer(final String hostname);

    String zipBinary(final String location);

    void prepareUnzip(final String hostname);
}
