package com.siemens.cto.aem.persistence.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class JpaWebServer extends AbstractEntity<JpaWebServer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;
    
    private String name;
    
    private Integer port;
    
    @ManyToMany
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
}
