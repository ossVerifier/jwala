package com.siemens.cto.aem.service.app;

import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

/**
 * An interface that defines application-centric external command tasks.
 *
 * Created by z003bpej on 9/9/2015.
 */
public interface ApplicationCommandService {
    CommandOutput secureCopyConfFile(String host, String sourcePath, String appConfPath,
                                RuntimeCommandBuilder rtCommandBuilder) throws CommandFailureException;
}
