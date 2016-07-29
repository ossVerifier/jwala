package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

import java.util.*;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;

public class MockGroup {

    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private String groupName = "mygroupName";

    public Group getGroup() {
        getJvms();
        getApplications();
        getWebServers();
        Group group = new Group(groupId,
                groupName,
                jvms,
                webServers,
                null,
                null,
                applications);
        return group;
    }

    public void getJvms() {
        Group myGroup = new Group(groupId, groupName);
        groups.add(myGroup);
        Jvm jvm = new Jvm(id(0L, Jvm.class),
                "jvmname",
                "USMLVV1CDS0049",
                groups,
                myGroup,
                9100,
                9101,
                9102,
                -1,
                9103,
                new Path("statusPath"),
                "systemProperties",
                JvmState.JVM_START,
                "errorStatus",
                Calendar.getInstance(),
                "username",
                "encryptedpassword");
        jvms.add(jvm);
    }

    public void getWebServers() {
        Group myGroup = new Group(groupId, groupName);
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "USMLVV1CDS0049",
                "myWebServerName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        webServers.add(webServer);
    }

    public WebServer getWebServer(final String webServerName) {
        Group myGroup = new Group(groupId, groupName);
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "USMLVV1CDS0049",
                "myWebSererName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        return webServer;
    }

    public void getApplications() {
        Group myGroup = new Group(groupId, groupName);
        Application application = new Application(id(0L, Application.class),
                "HEALTH-CHECK-4.0",
                "myaWarPath",
                "/hct",
                myGroup,
                true,
                false,
                false,
                "myWarName");
        List<Jvm> jvmsList = new LinkedList<>();
        for(Jvm jvm: jvms){
            jvmsList.add(jvm);
        }
        application.setJvms(jvmsList);
        applications.add(application);
    }

    public List<Application> findApplications(){
        Group myGroup = new Group(groupId, groupName);
        List<Application> applications = new LinkedList<>();
        Application application = new Application(id(0L, Application.class),
                "HEALTH-CHECK-4.0",
                "myaWarPath",
                "/hct",
                myGroup,
                true,
                false,
                false,
                "myWarName");
        applications.add(application);
        return applications;
    }

    public List<WebServer> findWebServers(){
        Group myGroup = new Group(groupId, groupName);
        List<WebServer> webservers = new LinkedList<>();
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "USMLVV1CDS0049",
                "myWebServerName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        webservers.add(webServer);
        return webservers;
    }

    private Set<Group> groups = new HashSet<>();
    private Set<Jvm> jvms = new HashSet<>();
    private Set<Application> applications = new HashSet<>();
    private Set<WebServer> webServers = new HashSet<>();
}
