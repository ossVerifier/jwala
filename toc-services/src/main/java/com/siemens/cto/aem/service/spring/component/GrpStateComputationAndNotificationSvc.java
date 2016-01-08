package com.siemens.cto.aem.service.spring.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;

/**
 * Computes group state and sends out group state change notification.
 *
 * Created by JC043760 on 1/5/2016.
 */
public interface GrpStateComputationAndNotificationSvc {

    /**
     * Compute group state and send notification about the group state.
     * @param id Either a JVM or a WebServer model.
     * @param state {@link OperationalState} e.g. JvmState or WebServerReachableState.
     */
    void computeAndNotify(Identifier id, OperationalState state);

}
