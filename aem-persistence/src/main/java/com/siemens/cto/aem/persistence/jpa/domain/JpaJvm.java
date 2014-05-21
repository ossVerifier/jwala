package com.siemens.cto.aem.persistence.jpa.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.siemens.cto.aem.domain.model.jvm.Jvm;

@Entity
@Table(name = "jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class JpaJvm extends AbstractEntity<JpaJvm, Jvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostName;

    @ManyToMany(mappedBy = "jvms")
    private List<JpaGroup> groups;

    public Long getId() {
        return id;
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

    public List<JpaGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<JpaGroup> groups) {
        this.groups = groups;
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

        if (id != null ? !id.equals(jpaJvm.id) : jpaJvm.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
