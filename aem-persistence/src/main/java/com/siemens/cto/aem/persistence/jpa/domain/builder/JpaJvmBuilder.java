package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public class JpaJvmBuilder {

    private JpaJvm jvm;

    public JpaJvmBuilder() {
    }

    public JpaJvmBuilder(final JpaJvm aJvm) {
        jvm = aJvm;
    }

    public JpaJvmBuilder setJvm(final JpaJvm aJvm) {
        jvm = aJvm;
        return this;
    }

    public Jvm build() {
        final JvmBuilder builder = new JvmBuilder().setId(new Identifier<Jvm>(jvm.getId()))
                                                   .setName(jvm.getName())
                                                   .setHostName(jvm.getHostName());
        return builder.build();
    }
}
