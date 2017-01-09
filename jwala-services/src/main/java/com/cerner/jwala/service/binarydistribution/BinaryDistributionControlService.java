package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */

public interface BinaryDistributionControlService {
    CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException;

    CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException;

    CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException;

    CommandOutput unzipBinary(final String hostname, final String zipPath, final String binaryLocation, final String destination, final String exclude) throws CommandFailureException;

    CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException;

    CommandOutput changeFileMode(final String hostname, final String mode, final String targetDir, final String target) throws CommandFailureException;
}
