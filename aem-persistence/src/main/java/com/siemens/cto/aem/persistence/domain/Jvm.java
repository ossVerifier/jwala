package com.siemens.cto.aem.persistence.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "jvm")
public class Jvm extends AbstractEntity<Jvm> {

    private static final long serialVersionUID = 2491659292018543404L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String jvmName;

    @ManyToOne
    @JoinColumn(name = "jvmGroupId")
    public Group group;

    public AbstractEntity<Group> getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(final String jvmName) {
        this.jvmName = jvmName;
    }

    /**
     * Overriding this to avoid dirtying up the log file with all states
     */
    @Override
    public String toString() {
        final StringBuilder toString = new StringBuilder();
        toString.append("Jvm{");
        toString.append("id=").append(id);
        toString.append(", jvmName='").append(jvmName).append('\'');
        toString.append(", groupId=").append(group.getId());
        toString.append('}');
        return toString.toString();
    }

}
