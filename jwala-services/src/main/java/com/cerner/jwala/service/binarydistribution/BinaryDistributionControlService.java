package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.exec.CommandOutput;

/**
 * Created by SP043299 on 9/7/2016.
 */
public interface BinaryDistributionControlService {
    CommandOutput secureCopyFile(final String hostname, final String source, final String destination);

    CommandOutput createDirectory(final String hostname, final String destination);

    CommandOutput checkFileExists(final String hostname, final String destination);

    CommandOutput unzipBinary(final String hostname, final String binaryLocation);

    CommandOutput deleteBinary(final String hostname, final String destination);
}
