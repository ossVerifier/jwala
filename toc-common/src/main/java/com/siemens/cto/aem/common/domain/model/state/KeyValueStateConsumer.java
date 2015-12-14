package com.siemens.cto.aem.common.domain.model.state;

import com.siemens.cto.aem.common.domain.model.state.message.StateKey;

public interface KeyValueStateConsumer {

    void set(final StateKey aKey,
             final String aValue);
}
