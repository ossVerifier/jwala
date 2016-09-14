package com.cerner.jwala.service.binarydistribution;

public interface BinaryDistributionService {
    void distributeJdk(final String hostname);

    void distributeTomcat(final String hostname);

    void distributeWebServer(final String hostname);

    void prepareUnzip(final String hostname);
}
