package com.siemens.cto.aem.persistence.jpa.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.jpa.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class JpaWebServer extends AbstractEntity<JpaWebServer, WebServer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;

    private String name;

    private Integer port;

    private Integer httpsPort;

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
}
