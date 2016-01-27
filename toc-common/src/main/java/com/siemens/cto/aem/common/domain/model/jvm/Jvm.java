package com.siemens.cto.aem.common.domain.model.jvm;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.uri.UriBuilder;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Jvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Identifier<Jvm> id;
    private String jvmName;
    private String hostName;
    private Set<Group> groups;

    // JVM ports
    private Integer httpPort;
    private Integer httpsPort;
    private Integer redirectPort;
    private Integer shutdownPort;
    private Integer ajpPort;

    private Path statusPath;
    private String systemProperties;
    private String state;
    private String errorStatus;

    /**
     * Constructor for a bare minimum Jvm with group details.
     * @param id the id
     * @param name the jvm name
     * @param groups the groups in which the web server is assigned to.
     */
    public Jvm(final Identifier<Jvm> id, final String name, final Set<Group> groups) {
        this.id = id;
        this.jvmName = name;
        this.groups = Collections.unmodifiableSet(new HashSet<>(groups));
    }

    public Jvm(final Identifier<Jvm> id,
               final String name,
               final String hostName,
               final Set<Group> groups,
               final Integer httpPort,
               final Integer httpsPort,
               final Integer redirectPort,
               final Integer shutdownPort,
               final Integer ajpPort,
               final Path statusPath,
               final String systemsProperties,
               final String state,
               final String errorStatus) {
        this.id = id;
        this.jvmName = name;
        this.hostName = hostName;
        this.groups = Collections.unmodifiableSet(new HashSet<>(groups));
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.redirectPort = redirectPort;
        this.shutdownPort = shutdownPort;
        this.ajpPort = ajpPort;
        this.statusPath = statusPath;
        this.systemProperties = systemsProperties;
        this.state = state;
        this.errorStatus = errorStatus;
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

    public Set<Group> getGroups() {
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

    public String getSystemProperties() {
        return systemProperties;
    }

    public String getState() {
        return state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public URI getStatusUri() {
        final UriBuilder builder = new UriBuilder().setHost(getHostName())
                                                   .setHttpsPort(getHttpsPort())
                                                   .setPort(getHttpPort())
                                                   .setPath(getStatusPath());
        return builder.buildUnchecked();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Jvm jvm = (Jvm) o;

        if (!id.equals(jvm.id)) return false;
        if (!jvmName.equals(jvm.jvmName)) return false;
        if (hostName != null ? !hostName.equals(jvm.hostName) : jvm.hostName != null) return false;
        if (groups != null ? !groups.equals(jvm.groups) : jvm.groups != null) return false;
        if (httpPort != null ? !httpPort.equals(jvm.httpPort) : jvm.httpPort != null) return false;
        if (httpsPort != null ? !httpsPort.equals(jvm.httpsPort) : jvm.httpsPort != null) return false;
        if (redirectPort != null ? !redirectPort.equals(jvm.redirectPort) : jvm.redirectPort != null) return false;
        if (shutdownPort != null ? !shutdownPort.equals(jvm.shutdownPort) : jvm.shutdownPort != null) return false;
        if (ajpPort != null ? !ajpPort.equals(jvm.ajpPort) : jvm.ajpPort != null) return false;
        if (statusPath != null ? !statusPath.equals(jvm.statusPath) : jvm.statusPath != null) return false;
        if (systemProperties != null ? !systemProperties.equals(jvm.systemProperties) : jvm.systemProperties != null)
            return false;
        if (state != null ? !state.equals(jvm.state) : jvm.state != null) return false;
        return !(errorStatus != null ? !errorStatus.equals(jvm.errorStatus) : jvm.errorStatus != null);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + jvmName.hashCode();
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (httpPort != null ? httpPort.hashCode() : 0);
        result = 31 * result + (httpsPort != null ? httpsPort.hashCode() : 0);
        result = 31 * result + (redirectPort != null ? redirectPort.hashCode() : 0);
        result = 31 * result + (shutdownPort != null ? shutdownPort.hashCode() : 0);
        result = 31 * result + (ajpPort != null ? ajpPort.hashCode() : 0);
        result = 31 * result + (statusPath != null ? statusPath.hashCode() : 0);
        result = 31 * result + (systemProperties != null ? systemProperties.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (errorStatus != null ? errorStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Jvm{" +
                "id=" + id +
                ", jvmName='" + jvmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", groups=" + groups +
                ", httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", redirectPort=" + redirectPort +
                ", shutdownPort=" + shutdownPort +
                ", ajpPort=" + ajpPort +
                ", statusPath=" + statusPath +
                ", systemProperties='" + systemProperties + '\'' +
                ", state='" + state + '\'' +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }

}
