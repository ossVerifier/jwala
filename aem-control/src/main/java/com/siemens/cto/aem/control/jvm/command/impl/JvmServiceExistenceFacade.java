package com.siemens.cto.aem.control.jvm.command.impl;

import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.platform.Platform;

public class JvmServiceExistenceFacade {

    public ExecCommand getServiceExistenceCommandFor(final Jvm aJvm) {
        //TODO This should come from a property on JVM once it's ready
        final PlatformCommandProvider provider = PlatformCommandProvider.lookup(Platform.WINDOWS);
        //TODO If the WindowsGenericNonControlOperation ever has anything more than just QUERY_EXISTENCE, it will need to be accounted for here (among other places)
        return provider.getGenericServiceCommandBuilder().buildCommandForService(aJvm.getJvmName());
    }
}
