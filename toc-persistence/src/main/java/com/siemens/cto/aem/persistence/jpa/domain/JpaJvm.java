package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME,
                query = "SELECT j FROM JpaJvm j WHERE j.name = :jvmName AND j.groups.name = :groupName"),
    @NamedQuery(name = JpaJvm.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaJvm j SET j.state = :state WHERE j.id = :id"),
    @NamedQuery(name = JpaJvm.QUERY_UPDATE_ERROR_STATUS_BY_ID, query = "UPDATE JpaJvm j SET j.errorStatus = :errorStatus WHERE j.id = :id")
})
public class JpaJvm extends AbstractEntity<JpaJvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    public static final String QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME = "findJvmByGroupAndJvmName";
    public static final String QUERY_UPDATE_STATE_BY_ID = "updateStateById";
    public static final String QUERY_UPDATE_ERROR_STATUS_BY_ID = "updateErrorStatusById";

    public static final String QUERY_PARAM_ID = "id";
    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_ERROR_STATUS = "errorStatus";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    public String name;

    private String hostName;

    @ManyToMany(mappedBy = "jvms")
    private List<JpaGroup> groups;

    @Column(nullable = false)
    private Integer httpPort;

    private Integer httpsPort;

    @Column(nullable = false)
    private Integer redirectPort;

    @Column(nullable = false)
    private Integer shutdownPort;

    @Column(nullable = false)
    private Integer ajpPort;

    @Column(nullable = false)
    private String statusPath;

    private String systemProperties;

    private String state;

    @Column(name = "ERR_STS", length = 2147483647)
    private String errorStatus = "";

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(final String statusPath) {
        this.statusPath = statusPath;
    }

    public List<JpaGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<JpaGroup> groups) {
        this.groups = groups;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public void setShutdownPort(Integer shutdownPort) {
        this.shutdownPort = shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(Integer ajpPort) {
        this.ajpPort = ajpPort;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaJvm jpaJvm = (JpaJvm) o;

        if (!id.equals(jpaJvm.id)) return false;
        if (!name.equals(jpaJvm.name)) return false;
        if (hostName != null ? !hostName.equals(jpaJvm.hostName) : jpaJvm.hostName != null) return false;
        if (groups != null ? !groups.equals(jpaJvm.groups) : jpaJvm.groups != null) return false;
        if (httpPort != null ? !httpPort.equals(jpaJvm.httpPort) : jpaJvm.httpPort != null) return false;
        if (httpsPort != null ? !httpsPort.equals(jpaJvm.httpsPort) : jpaJvm.httpsPort != null) return false;
        if (redirectPort != null ? !redirectPort.equals(jpaJvm.redirectPort) : jpaJvm.redirectPort != null)
            return false;
        if (shutdownPort != null ? !shutdownPort.equals(jpaJvm.shutdownPort) : jpaJvm.shutdownPort != null)
            return false;
        if (ajpPort != null ? !ajpPort.equals(jpaJvm.ajpPort) : jpaJvm.ajpPort != null) return false;
        if (statusPath != null ? !statusPath.equals(jpaJvm.statusPath) : jpaJvm.statusPath != null) return false;
        if (systemProperties != null ? !systemProperties.equals(jpaJvm.systemProperties) : jpaJvm.systemProperties != null)
            return false;
        if (state != null ? !state.equals(jpaJvm.state) : jpaJvm.state != null) return false;
        return !(errorStatus != null ? !errorStatus.equals(jpaJvm.errorStatus) : jpaJvm.errorStatus != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
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

}