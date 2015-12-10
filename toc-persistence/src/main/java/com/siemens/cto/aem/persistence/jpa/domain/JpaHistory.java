package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.group.History;
import com.siemens.cto.aem.persistence.jpa.type.EventType;

import javax.persistence.*;

@Entity
@Table(name = "history", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQueries({
    @NamedQuery(name = JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME,
                query = "SELECT h FROM JpaHistory h WHERE h.group.name = :groupName")
})
public class JpaHistory extends AbstractEntity<JpaHistory, History> {

    public static final String QRY_GET_HISTORY_BY_GROUP_NAME = "getHistoryByGroupName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    public String serverName;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private JpaGroup group;

    private String event;

    @Column(name = "EVENTTYPE", length = 2)
    private String eventTypeValue;

    public JpaHistory() {}

    public JpaHistory(final String serverName, final JpaGroup group, final String event, final EventType eventType,
                      final String user) {
        this.serverName = serverName;
        this.group = group;
        this.event = event;
        this.eventTypeValue = eventType.toValue();
        this.createBy = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public JpaGroup getGroup() {
        return group;
    }

    public void setGroup(final JpaGroup group) {
        this.group = group;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }

    public EventType getEventType() {
        return EventType.fromValue(eventTypeValue);
    }

    public void setEventType(final EventType eventType) {
        this.eventTypeValue = eventType.toValue();
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
