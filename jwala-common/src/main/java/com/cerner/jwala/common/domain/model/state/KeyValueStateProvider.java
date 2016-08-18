package com.cerner.jwala.common.domain.model.state;

public interface KeyValueStateProvider {

    void provideState(final KeyValueStateConsumer aConsumer);
}
