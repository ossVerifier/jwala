package com.siemens.cto.aem.persistence.jpa.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.siemens.cto.aem.domain.model.group.Group;

@Entity
@Table(name = "grp", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class JpaGroup extends AbstractEntity<JpaGroup, Group> {

    private static final long serialVersionUID = -2125399708516728584L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToMany
    @JoinTable(name = "GRP_JVM",
               joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
               inverseJoinColumns = {@JoinColumn(name = "JVM_ID", referencedColumnName = "ID")},
               uniqueConstraints = @UniqueConstraint(columnNames = {"GROUP_ID", "JVM_ID"}))
    private List<JpaJvm> jvms;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public List<JpaJvm> getJvms() {
        return jvms;
    }

    public void setJvms(final List<JpaJvm> jvms) {
        this.jvms = jvms;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaGroup jpaGroup = (JpaGroup) o;

        if (id != null ? !id.equals(jpaGroup.id) : jpaGroup.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
