package com.cerner.jwala.service.jvm.operation.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exec.ExecCommand;

/**
 * The start operation command
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public class Start extends AbstractOperation {

    @Override
    ExecCommand getCommand(final Jvm jvm) {
        return new ExecCommand(remoteJvmInstanceDir + "/" + jvm.getJvmName() + "/bin/start-service.sh");
    }

}
