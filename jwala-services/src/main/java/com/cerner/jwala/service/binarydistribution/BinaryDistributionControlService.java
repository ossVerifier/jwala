package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

/**
 * Created by SP043299 on 9/7/2016.
 */
public interface BinaryDistributionControlService {
    CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException;

    CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException;

    CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException;

    CommandOutput unzipBinary(final String hostname, final String binaryLocation, final String destination) throws CommandFailureException;

    CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException;
}
