package com.siemens.cto.aem.persistence.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "aem_group", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Group extends AbstractEntity<Group> {

    private static final long serialVersionUID = -2125399708516728584L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
