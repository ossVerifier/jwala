package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.group.History;

import javax.persistence.*;

@Entity
@Table(name = "history", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@AttributeOverride(name="name", column=@Column(unique = false))
public class JpaHistory extends AbstractEntity<JpaHistory, History> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private JpaGroup group;

    private String event;

    public JpaHistory() {}

    public JpaHistory(final String name, final JpaGroup group, final String event, String user) {
        this.name = name;
        this.group = group;
        this.event = event;
        this.createBy = user;
    }

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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

        return id != null ? id.equals(jpaHistory.id) : jpaHistory.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
