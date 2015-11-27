package com.siemens.cto.aem.domain.model.state;

public interface KeyValueStateProvider {

    void provideState(final KeyValueStateConsumer aConsumer);
}
