package com.siemens.cto.aem.persistence.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "aem_jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Jvm extends AbstractEntity<Jvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String jvmName;

    @ManyToOne
    @JoinColumn(name = "jvmGroupId")
    public Group group;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
