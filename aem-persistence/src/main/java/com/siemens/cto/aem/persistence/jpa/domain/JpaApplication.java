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

import com.siemens.cto.aem.domain.model.app.Application;

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
@Table(name = "app", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
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
    ),
    @NamedQuery(
        name=JpaApplication.QUERY_BY_WEB_SERVER_NAME,
        query="SELECT a FROM JpaApplication a WHERE a.group in (SELECT ws.groups FROM JpaWebServer ws WHERE ws.name =:wsName)"),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_NAME,
            query="SELECT a FROM JpaApplication a WHERE a.name = :appName"),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_GROUP_JVM_AND_APP_NAME,
            query="SELECT a FROM JpaApplication a WHERE a.name = :appName AND a.group in " +
                  "(SELECT g FROM JpaGroup g WHERE g.name = :groupName AND g.jvms.name = :jvmName)")

    })
public class JpaApplication extends AbstractEntity<JpaApplication, Application> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String QUERY_BY_GROUP_ID = "findApplicationsByGroupId";
    public static final String QUERY_BY_GROUP_NAME = "findApplicationsByGroupName";
    public static final String QUERY_BY_JVM_ID = "findApplicationsByJvmId";
    public static final String QUERY_BY_WEB_SERVER_NAME = "findApplicationsByWebServerName";
    public static final String QUERY_BY_NAME = "findApplicationByName";
    public static final String GROUP_ID_PARAM = "groupId";
    public static final String JVM_ID_PARAM = "jvmId";
    public static final String GROUP_NAME_PARAM = "groupName";
    public static final String WEB_SERVER_NAME_PARAM = "wsName";
    public static final String QUERY_BY_GROUP_JVM_AND_APP_NAME = "findApplicationByGroupJvmAndAppName";

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

    @Column(nullable = false, unique = false)
    private String webAppContext;

    @Column(nullable = true, unique = false)
    private String warPath;
    
    @SuppressWarnings("Unused")
    @Column(nullable = true)
    private String documentRoot; // potential addition to track the static content files TODO - coverage, et al.

    private boolean secure;

    private boolean loadBalanceAcrossServers;

    @Column(nullable = true, unique = false)
    private String warName;

    public void setWarPath(String aWarPath) {
        warPath = aWarPath;
    }

    public void setWebAppContext(String aWebAppContext) {
        this.webAppContext = aWebAppContext;
    }

    public void setGroup(JpaGroup jpaGroup) {
        this.group = jpaGroup;
    }
    
    public JpaGroup getGroup() {
        return this.group;
    }

    public String getWarPath() {
        return this.warPath;
    }
    
    public String getWebAppContext() {
        return this.webAppContext;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    public void setLoadBalanceAcrossServers(boolean loadBalanceAcrossServers) {
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    public void setWarName(String warName) {
        this.warName = warName;
    }

    public String getWarName() {
        return warName;
    }
}
