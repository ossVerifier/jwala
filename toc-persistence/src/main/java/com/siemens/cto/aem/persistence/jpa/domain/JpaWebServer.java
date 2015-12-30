package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaWebServer.FIND_WEB_SERVER_BY_QUERY,
                query ="SELECT ws FROM JpaWebServer ws WHERE ws.name = :wsName"),
    @NamedQuery(name = JpaWebServer.FIND_JVMS_QUERY,
                query = "SELECT DISTINCT jvm FROM JpaJvm jvm JOIN jvm.groups g " +
                        "WHERE g.id IN (SELECT a.group FROM JpaApplication a " +
                        "WHERE a.group IN (:groups))")
})
public class JpaWebServer extends AbstractEntity<JpaWebServer> {

    private static final long serialVersionUID = 1L;
    public static final String WEB_SERVER_PARAM_NAME = "wsName";
    public static final String FIND_WEB_SERVER_BY_QUERY = "findWebServerByNameQuery";
    public static final String FIND_JVMS_QUERY = "findJvmsQuery";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;

    private String name;

    private Integer port;

    private Integer httpsPort;

    @Column(nullable = false)
    private String statusPath;

    @Column(nullable = false)
    private String httpConfigFile;

    @Column(nullable = false)
    private String svrRoot;

    @Column(nullable = false)
    private String docRoot;

    @ManyToMany(mappedBy = "webServers")
    private List<JpaGroup> groups = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public List<JpaGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<JpaGroup> newGroups) {
        groups = newGroups;
	}

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(final String statusPath) {
        this.statusPath = statusPath;
    }

    public String getHttpConfigFile() {
        return httpConfigFile;
    }

    public void setHttpConfigFile(String httpConfigFile) {
        this.httpConfigFile = httpConfigFile;
    }

    public String getSvrRoot() {
        return svrRoot;
    }

    public void setSvrRoot(String svrRoot) {
        this.svrRoot = svrRoot;
    }

    public String getDocRoot() {
        return docRoot;
    }

    public void setDocRoot(String docRoot) {
        this.docRoot = docRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaWebServer that = (JpaWebServer) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
