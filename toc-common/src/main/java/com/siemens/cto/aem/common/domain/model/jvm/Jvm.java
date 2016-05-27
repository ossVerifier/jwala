package com.siemens.cto.aem.common.domain.model.jvm;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.uri.UriBuilder;

public class Jvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Identifier<Jvm> id;
    private String jvmName;
    private String hostName;
    private Set<Group> groups;
    private Group parentGroup;

    // JVM ports
    private Integer httpPort;
    private Integer httpsPort;
    private Integer redirectPort;
    private Integer shutdownPort;
    private Integer ajpPort;

    private Path statusPath;
    private String systemProperties;
    private JvmState state;
    private String errorStatus;
    private List<Application> webApps;
    private String userName;
    private String encryptedPassword;

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
               final JvmState state,
               final String errorStatus,
               final List<Application> webApps,
               final String userName,
               final String encryptedPassword) {
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
        this.webApps = webApps;
        this.userName = userName;
        this.encryptedPassword = encryptedPassword;
    }

    public Jvm(Identifier<Jvm> id,
               String jvmName,
               String hostName,
               Set<Group> groups,
               Group parentGroup,
               Integer httpPort,
               Integer httpsPort,
               Integer redirectPort,
               Integer shutdownPort,
               Integer ajpPort,
               Path statusPath,
               String systemProperties,
               JvmState state,
               String errorStatus,
               String userName,
               String encryptedPassword) {
        this.id = id;
        this.jvmName = jvmName;
        this.hostName = hostName;
        this.groups = groups;
        this.parentGroup = parentGroup;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.redirectPort = redirectPort;
        this.shutdownPort = shutdownPort;
        this.ajpPort = ajpPort;
        this.statusPath = statusPath;
        this.systemProperties = systemProperties;
        this.state = state;
        this.errorStatus = errorStatus;
        this.userName = userName;
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public JvmState getState() {
        return state;
    }

    /**
     * The user friendly state wording.
     * @return the state e.g. STOPPED instead of the state name which is JVM_STOPPED.
     */
    public String getStateLabel() {
        return state.toStateLabel();
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public Group getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    public URI getStatusUri() {
        final UriBuilder builder = new UriBuilder().setHost(getHostName())
                                                   .setHttpsPort(getHttpsPort())
                                                   .setPort(getHttpPort())
                                                   .setPath(getStatusPath());
        return builder.buildUnchecked();
    }

    public List<Application> getWebApps() {
        return webApps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Jvm jvm = (Jvm) o;

        return id.equals(jvm.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Jvm{" +
                "id=" + id +
                ", jvmName='" + jvmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", groups=" + groups +
                ", parentGroup=" + parentGroup +
                ", httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", redirectPort=" + redirectPort +
                ", shutdownPort=" + shutdownPort +
                ", ajpPort=" + ajpPort +
                ", statusPath=" + statusPath +
                ", systemProperties='" + systemProperties + '\'' +
                ", state=" + state +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }

}
