package com.siemens.cto.aem.service.jvm;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public interface JvmStateGateway {

    void setExplicitState(@Payload final Identifier<Jvm> anId,
                          @Header("reachableState") final CurrentState<Jvm, JvmState> anIntendedState);

    void initiateJvmStateRequest(@Payload final Jvm aJvm);
}
