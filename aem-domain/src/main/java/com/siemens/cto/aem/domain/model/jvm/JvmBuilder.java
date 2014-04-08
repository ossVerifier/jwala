package com.siemens.cto.aem.domain.model.jvm;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JvmBuilder {

    private Identifier<Jvm> id;
    private Group group;
    private String name;
    private String hostName;

    public JvmBuilder setId(final Identifier<Jvm> id) {
        this.id = id;
        return this;
    }

    public JvmBuilder setGroup(final Group group) {
        this.group = group;
        return this;
    }

    public JvmBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public JvmBuilder setHostName(final String hostName) {
        this.hostName = hostName;
        return this;
    }

    public Jvm build() {
        return new Jvm(id,
                       name,
                       hostName);
    }
}