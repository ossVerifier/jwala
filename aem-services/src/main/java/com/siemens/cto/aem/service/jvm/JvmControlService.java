package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmControlService {

    JvmControlHistory controlJvm(final ControlJvmCommand aCommand,
                                 final User aUser);
    
    interface JvmControlServiceLifecycle {
        JvmControlHistory startHistory(ControlJvmCommand aCommand, User aUser);
        void startState(ControlJvmCommand aCommand, User aUser);
        JvmControlHistory completeHistory(JvmControlHistory incompleteHistory, ControlJvmCommand aCommand,
                ExecData execData, User aUser);
    }
}
