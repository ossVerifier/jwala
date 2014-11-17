package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.uri.UriBuilder;

public class Jvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> id;
    private final String jvmName;
    private final String hostName;
    private final Set<LiteGroup> groups;

    // JVM ports
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer redirectPort;
    private final Integer shutdownPort;
    private final Integer ajpPort;

    private final Path statusPath;

    /**
     * Constructor for a bare minimum Jvm with group details.
     * @param theId the id
     * @param theName the jvm name
     * @param theGroups the groups in which the web server is assigned to.
     */
    public Jvm(final Identifier<Jvm> theId, final String theName, final Set<LiteGroup> theGroups) {
        id = theId;
        jvmName = theName;
        hostName = null;
        groups = Collections.unmodifiableSet(new HashSet<>(theGroups));
        httpPort = null;
        httpsPort = null;
        redirectPort = null;
        shutdownPort = null;
        ajpPort = null;
        statusPath = null;
    }

    public Jvm(final Identifier<Jvm> theId,
               final String theName,
               final String theHostName,
               final Set<LiteGroup> theGroups,
               final Integer theHttpPort,
               final Integer theHttpsPort,
               final Integer theRedirectPort,
               final Integer theShutdownPort,
               final Integer theAjpPort,
               final Path theStatusPath) {
        id = theId;
        jvmName = theName;
        hostName = theHostName;
        groups = Collections.unmodifiableSet(new HashSet<>(theGroups));
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
    }

    public Identifier<Jvm> getId() {
        return id;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    public Set<LiteGroup> getGroups() {
        return groups;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public URI getStatusUri() {
        final UriBuilder builder = new UriBuilder().setHost(getHostName())
                                                   .setHttpsPort(getHttpsPort())
                                                   .setPort(getHttpPort())
                                                   .setPath(getStatusPath());
        return builder.buildUnchecked();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Jvm rhs = (Jvm) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.jvmName, rhs.jvmName)
                .append(this.hostName, rhs.hostName)
                .append(this.statusPath, rhs.statusPath)
                .append(this.groups, rhs.groups)
                .append(this.httpPort, rhs.httpPort)
                .append(this.httpsPort, rhs.httpsPort)
                .append(this.redirectPort, rhs.redirectPort)
                .append(this.shutdownPort, rhs.shutdownPort)
                .append(this.ajpPort, rhs.ajpPort)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(jvmName)
                .append(hostName)
                .append(statusPath)
                .append(groups)
                .append(httpPort)
                .append(httpsPort)
                .append(redirectPort)
                .append(shutdownPort)
                .append(ajpPort)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("jvmName", jvmName)
                .append("hostName", hostName)
                .append("statusPath", statusPath)
                .append("groups", groups)
                .append("httpPort", httpPort)
                .append("httpsPort", httpsPort)
                .append("redirectPort", redirectPort)
                .append("shutdownPort", shutdownPort)
                .append("ajpPort", ajpPort)
                .toString();
    }
}
