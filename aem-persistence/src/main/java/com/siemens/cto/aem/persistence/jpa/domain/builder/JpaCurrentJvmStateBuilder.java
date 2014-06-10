package com.siemens.cto.aem.persistence.jpa.domain.builder;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmStateBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;

public class JpaCurrentJvmStateBuilder {

    public static final Chronology USE_DEFAULT_CHRONOLOGY = null;
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
        if (jvmState != null) {
            return buildActual();
        }
        return null;
    }

    CurrentJvmState buildActual() {
        final CurrentJvmStateBuilder builder = new CurrentJvmStateBuilder().setJvmId(new Identifier<Jvm>(jvmState.getId()))
                                                                           .setJvmState(JvmState.convertFrom(jvmState.getState()))
                                                                           .setAsOf(new DateTime(jvmState.getAsOf(),
                                                                                                 USE_DEFAULT_CHRONOLOGY));
        return builder.build();
    }
}
