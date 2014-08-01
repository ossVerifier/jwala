package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.webserver.WebServer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaWebServer.FIND_APPLICATIONS_QUERY,
                query = "SELECT a FROM JpaApplication a WHERE a.group in " +
                        "(SELECT ws.groups FROM JpaWebServer ws WHERE ws.name =:wsName)"),
    @NamedQuery(name = JpaWebServer.FIND_WEB_SERVER_BY_QUERY,
                query ="SELECT ws FROM JpaWebServer ws WHERE ws.name =:wsName"),
    @NamedQuery(name = JpaWebServer.FIND_JVMS_QUERY,
                query = "SELECT DISTINCT jvm FROM JpaJvm jvm JOIN jvm.groups g " +
                        "WHERE g.id IN (SELECT a.group FROM JpaApplication a " +
                        "WHERE a.group IN (SELECT w.groups FROM JpaWebServer w WHERE w.name = :wsName))"),
    @NamedQuery(name=JpaWebServer.FIND_WEB_SERVER_BY_GROUP_QUERY,
                query="SELECT ws FROM JpaWebServer ws WHERE :groupId MEMBER OF ws.groups.id ORDER BY ws.name")
})
public class JpaWebServer extends AbstractEntity<JpaWebServer, WebServer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;

    private String name;

    private Integer port;

    private Integer httpsPort;

    @Column(nullable = false)
    private String statusPath;

    public static final String FIND_APPLICATIONS_QUERY = "findApplicationsQuery";
    public static final String WEB_SERVER_PARAM_NAME = "wsName";
    public static final String FIND_WEB_SERVER_BY_QUERY = "findWebServerByNameQuery";
    public static final String FIND_JVMS_QUERY = "findJvmsQuery";
    public static final String FIND_WEB_SERVER_BY_GROUP_QUERY = "findWebServerByGroupQuery";

    @ManyToMany
    @JoinTable(name = "WEBSERVER_GRP",
               joinColumns = {@JoinColumn(name = "WEBSERVER_ID", referencedColumnName = "ID")},
               inverseJoinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
               uniqueConstraints = @UniqueConstraint(columnNames = {"WEBSERVER_ID", "GROUP_ID"}))
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
	    groups.clear();
	    groups.addAll(newGroups);
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
}
