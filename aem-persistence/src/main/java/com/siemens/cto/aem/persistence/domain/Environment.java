package com.siemens.cto.aem.persistence.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Environment extends AbstractEntity<Environment> {

    private static final long serialVersionUID = -7743021454492330117L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "environmentId")
    public List<Server> servers;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @Column(name = "groupId")
    public AbstractEntity<Group> group;

    public String name;
    public String hhrr;
    public String version;
    public String env;
    public String dataCenterType;

    @OneToOne(mappedBy = "environment")
    public Event event;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(final List<Server> servers) {
        this.servers = servers;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getHhrr() {
        return hhrr;
    }

    public void setHhrr(final String hhrr) {
        this.hhrr = hhrr;
    }

    public AbstractEntity<Group> getGroup() {
        return group;
    }

    public void setGroup(final AbstractEntity<Group> group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(final String env) {
        this.env = env;
    }

    public String getDataCenterType() {
        return dataCenterType;
    }

    public void setDataCenterType(final String dataCenterType) {
        this.dataCenterType = dataCenterType;
    }

    public String getFullRpaEnviroment() {
        return getHhrr() + "." + getEnv() + "." + getVersion();
    }

    @Override
    public String toString() {
        return "Environment{" + "id=" + id + ", servers=" + servers + ", name='" + name + '\'' + ", hhrr='" + hhrr
                + '\'' + ", group='" + this.group + '\'' + ", version='" + version + '\'' + ", env='" + env + '\''
                + ", dataCenterType='" + dataCenterType + '}';
    }
}
