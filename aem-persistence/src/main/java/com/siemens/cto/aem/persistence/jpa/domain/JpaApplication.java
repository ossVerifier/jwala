package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


/**
 * An application is usually a web application stored in a war file
 *
 * The war file may be deployed to any number of JVMs, which happens
 * through the deploying the owning group to JVMs.
 *
 * Each Application is created and assigned to a group.
 *
 * For Health Check, where it might be deployed alongside another application,
 * the caller must create a group for health check, and a group for the
 * other application so that they can be deployed and managed.
 *
 * @author horspe00
 *
 */
@Entity
@Table(name = "app", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "webAppContext"})})
@NamedQueries({
    @NamedQuery(
        name=JpaApplication.QUERY_BY_GROUP_NAME,
        query="SELECT a FROM JpaApplication a WHERE a.group.name = :groupName"
    ),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_JVM_ID,
            query="SELECT a FROM JpaApplication a WHERE a.group in (SELECT g FROM JpaGroup g WHERE g.jvms.id = :jvmId)"
    ),
    @NamedQuery(
        name=JpaApplication.QUERY_BY_GROUP_ID,
        query="SELECT a FROM JpaApplication a WHERE a.group.id= :groupId"
    )})
public class JpaApplication extends AbstractEntity<JpaGroup> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String QUERY_BY_GROUP_ID = "findApplicationsByGroupId";
    public static final String QUERY_BY_GROUP_NAME = "findApplicationsByGroupName";
    public static final String QUERY_BY_JVM_ID = "findApplicationsByJvmId";
    public static final String GROUP_ID_PARAM = "groupId";
    public static final String JVM_ID_PARAM = "jvmId";
    public static final String GROUP_NAME_PARAM = "groupName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public Long id;

    /**
     * Relationship stored in app.group to allow operations across a group
     * BUT, this does mean that a JpaApplication represents a single
     * (potentially deployed) application instance.
     */
    @ManyToOne(optional=true) public JpaGroup group;

    @Column(nullable = false, unique = true)
    public String webAppContext;

    @Column(nullable = false, unique = false)
    public String warPath;

    public void setWarPath(String aWarPath) {
        warPath = aWarPath;
    }

    public void setWebAppContext(String aWebAppContext) {
        this.webAppContext = aWebAppContext;
    }

    public void setGroup(JpaGroup jpaGroup) {
        this.group = jpaGroup;
    }

}
