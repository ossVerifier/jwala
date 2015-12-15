package com.siemens.cto.aem.control.jvm.command;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public interface JvmExecCommandBuilder {

    JvmExecCommandBuilder setJvm(final JpaJvm aJvm);

    JvmExecCommandBuilder setOperation(final JvmControlOperation anOperation);

    JvmExecCommandBuilder setParameter(String... aParams);

    ExecCommand build();
}
