package com.siemens.cto.aem.persistence.jpa.domain.builder;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmStateBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;

public class JpaCurrentJvmStateBuilder {

    private JpaCurrentJvmState jvmState;

    public JpaCurrentJvmStateBuilder() {
    }

    public JpaCurrentJvmStateBuilder(final JpaCurrentJvmState aState) {
        jvmState = aState;
    }

    public JpaCurrentJvmStateBuilder setJvmState(final JpaCurrentJvmState aState) {
        jvmState = aState;
        return this;
    }

    public CurrentJvmState build() {
        final CurrentJvmStateBuilder builder = new CurrentJvmStateBuilder().setJvmId(new Identifier<Jvm>(jvmState.getId()))
                                                                           .setJvmState(JvmState.valueOf(jvmState.getState()))
                                                                           .setAsOf(new DateTime(jvmState.getAsOf()));

        return builder.build();
    }
}
