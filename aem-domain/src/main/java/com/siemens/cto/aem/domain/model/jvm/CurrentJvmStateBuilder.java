package com.siemens.cto.aem.domain.model.jvm;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class CurrentJvmStateBuilder {

    private Identifier<Jvm> jvmId;
    private JvmState jvmState;
    private DateTime asOf;

    public CurrentJvmStateBuilder() {
    }

    public CurrentJvmStateBuilder setJvmId(final Identifier<Jvm> aJvmId) {
        jvmId = aJvmId;
        return this;
    }

    public CurrentJvmStateBuilder setJvmState(final JvmState aJvmState) {
        jvmState = aJvmState;
        return this;
    }

    public CurrentJvmStateBuilder setAsOf(final DateTime anAsOf) {
        asOf = anAsOf;
        return this;
    }

    public CurrentJvmState build() {
        return new CurrentJvmState(jvmId,
                                   jvmState,
                                   asOf);
    }
}
