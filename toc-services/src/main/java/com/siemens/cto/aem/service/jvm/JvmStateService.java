package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;

/**
 * The contract for a JVM state related services.
 *
 * Created by JC043760 on 3/22/2016.
 */
public interface JvmStateService {

    /**
     * Check all JVM states individually then update the state persistence/in-memory context when there's a state change.
     */
    void checkAndUpdateStates();

    /**
     * Set persistence context and in-memory state.
     * @param jvm the {@link Jvm} object.
     * @param state {@link JvmState}
     * @param errMsg the error message.
     */
    void setState(final Jvm jvm, final JvmState state, final String errMsg);

}
