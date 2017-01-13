package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.jvm.Jvm;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */

public interface BinaryDistributionService {

    void distributeJdk(final Jvm jvm);

    void distributeWebServer(final String hostname);

    void distributeUnzip(final String hostname);
}
