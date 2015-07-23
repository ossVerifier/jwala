package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;

public interface JvmStateGateway {

    void setExplicitState(@Payload final Identifier<Jvm> anId,
                          @Header("reachableState") final CurrentState<Jvm, JvmState> anIntendedState);

    void initiateJvmStateRequest(@Payload final Jvm aJvm);
}
