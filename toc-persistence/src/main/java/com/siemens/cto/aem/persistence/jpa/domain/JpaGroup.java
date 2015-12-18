package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "grp", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUP_ID, query = "SELECT g.id FROM JpaGroup g WHERE g.name = :name"),
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUP, query = "SELECT g FROM JpaGroup g WHERE g.id = :groupId"),
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUPS_WITH_WEBSERVER, query = "SELECT g FROM JpaGroup g WHERE :webServer MEMBER OF g.webServers")
})
public class JpaGroup extends AbstractEntity<JpaGroup, Group> {

    private static final long serialVersionUID = -2125399708516728584L;

    static public final String QUERY_GET_GROUP_ID = "getGroupId";
    static public final String QUERY_GET_GROUP = "getGroup";
    static public final String QUERY_GET_GROUPS_WITH_WEBSERVER = "getGroupWithWebServer";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(nullable = false, unique = true)
    public String name;

    @ManyToMany
    @JoinTable(name = "GRP_JVM",
               joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
               inverseJoinColumns = {@JoinColumn(name = "JVM_ID", referencedColumnName = "ID")},
               uniqueConstraints = @UniqueConstraint(columnNames = {"GROUP_ID", "JVM_ID"}))
    private List<JpaJvm> jvms;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private GroupState state;
    
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar stateUpdated;

    @ManyToMany
    @JoinTable(name = "WEBSERVER_GRP",
            joinColumns = {@JoinColumn(name = "GROUP_ID")},
            inverseJoinColumns = {@JoinColumn(name = "WEBSERVER_ID")})
    private List<JpaWebServer> webServers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "groupId")
    private List<JpaHistory> history;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JpaJvm> getJvms() {
        return jvms;
    }

    public void setJvms(final List<JpaJvm> jvms) {
        this.jvms = jvms;
    }
    
    public GroupState getState() {
        return state;
    }

    public void setState(GroupState state) {
        this.state = state;
    }

    public Calendar getStateUpdated() {
        return stateUpdated;
    }

    public void setStateUpdated(Calendar stateUpdated) {
        this.stateUpdated = stateUpdated;
    }

    public List<JpaWebServer> getWebServers() {
        return webServers;
    }

    public void setWebServers(List<JpaWebServer> webServers) {
        this.webServers = webServers;
    }

    public List<JpaHistory> getHistory() {
        return history;
    }

    public void setHistory(List<JpaHistory> history) {
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

        final JpaGroup jpaGroup = (JpaGroup) o;

        return id != null ? id.equals(jpaGroup.id) : jpaGroup.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
