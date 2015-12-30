package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME,
                query = "SELECT j FROM JpaJvm j WHERE j.name = :jvmName AND j.groups.name = :groupName")
})
public class JpaJvm extends AbstractEntity<JpaJvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    public static final String QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME = "findJvmByGroupAndJvmName";

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaJvm jpaJvm = (JpaJvm) o;

        return id != null ? id.equals(jpaJvm.id) : jpaJvm.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}