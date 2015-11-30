package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.group.History;

import javax.persistence.*;

@Entity
@Table(name = "history", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class JpaHistory extends AbstractEntity<JpaHistory, History> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private JpaGroup group;

    private String history;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public JpaGroup getGroup() {
        return group;
    }

    public void setGroup(JpaGroup group) {
        this.group = group;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaHistory jpaHistory = (JpaHistory) o;

        if (id != null ? !id.equals(jpaHistory.id) : jpaHistory.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
