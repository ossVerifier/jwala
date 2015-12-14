package com.siemens.cto.aem.common.domain.model.state;

public interface KeyValueStateProvider {

    void provideState(final KeyValueStateConsumer aConsumer);
}
