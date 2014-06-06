package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JvmBuilder {

    private Identifier<Jvm> id;
    private String name;
    private String hostName;
    private Set<LiteGroup> groups = new HashSet<>();
    private Integer httpPort;
    private Integer httpsPort;
    private Integer redirectPort;
    private Integer shutdownPort;
    private Integer ajpPort;

    public JvmBuilder setId(final Identifier<Jvm> anId) {
        id = anId;
        return this;
    }

    public JvmBuilder setGroups(final Set<LiteGroup> someGroups) {
        groups = someGroups;
        return this;
    }

    public JvmBuilder setName(final String aName) {
        name = aName;
        return this;
    }

    public JvmBuilder setHostName(final String aHostName) {
        hostName = aHostName;
        return this;
    }

    public JvmBuilder setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public JvmBuilder setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }

    public JvmBuilder setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
        return this;
    }

    public JvmBuilder setShutdownPort(Integer shutdownPort) {
        this.shutdownPort = shutdownPort;
        return this;
    }

    public JvmBuilder setAjpPort(Integer ajpPort) {
        this.ajpPort = ajpPort;
        return this;
    }

    public Jvm build() {
        return new Jvm(id,
                       name,
                       hostName,
                       groups,
                       httpPort,
                       httpsPort,
                       redirectPort,
                       shutdownPort,
                       ajpPort);
    }
}