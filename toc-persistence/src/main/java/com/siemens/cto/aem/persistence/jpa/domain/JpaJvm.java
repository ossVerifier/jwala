package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.jvm.JvmState;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME,
                query = "SELECT j FROM JpaJvm j WHERE j.name = :jvmName AND j.groups.name = :groupName"),
    @NamedQuery(name = JpaJvm.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaJvm j SET j.stateName = :state WHERE j.id = :id"),
    @NamedQuery(name = JpaJvm.QUERY_UPDATE_ERROR_STATUS_BY_ID, query = "UPDATE JpaJvm j SET j.errorStatus = :errorStatus WHERE j.id = :id"),
    @NamedQuery(name = JpaJvm.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID, query = "UPDATE JpaJvm j SET j.stateName = :state, j.errorStatus = :errorStatus WHERE j.id = :id")
})
public class JpaJvm extends AbstractEntity<JpaJvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    public static final String QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME = "findJvmByGroupAndJvmName";
    public static final String QUERY_UPDATE_STATE_BY_ID = "updateStateById";
    public static final String QUERY_UPDATE_ERROR_STATUS_BY_ID = "updateErrorStatusById";
    public static final String QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID = "updateStateAndErrStsById";

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

    @Transient
    private JvmState state;

    @Column(name = "STATE")
    private String stateName;

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

    public JvmState getState() {
        return state;
    }

    public void setState(JvmState state) {
        this.state = state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        if (state == null) {
            state = JvmState.JVM_NEW;
        }
    }

    @PostLoad
    private void postLoad() {
        // If we are using JPA 2.1, we can use converters instead.
        // This is to avoid errors when the state column has a value that is not in the enum (It happens!).
        state = JvmState.convertFrom(stateName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaJvm jpaJvm = (JpaJvm) o;

        return id.equals(jpaJvm.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}