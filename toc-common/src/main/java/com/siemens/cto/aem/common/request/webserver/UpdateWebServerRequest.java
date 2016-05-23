package com.siemens.cto.aem.common.request.webserver;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.*;
import com.siemens.cto.aem.common.rule.group.GroupIdsRule;
import com.siemens.cto.aem.common.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.common.rule.webserver.WebServerNameRule;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class UpdateWebServerRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> id;
    private final Collection<Identifier<Group>> newGroupIds; //TODO (Corey) Peter Agrees: Change this to a Set all the way down the line...
    private final String newHost;
    private final String newName;
    private final Integer newPort;
    private final Integer newHttpsPort;
    private final Path newStatusPath;
    private final Path newSvrRoot;
    private final Path newDocRoot;
    private final FileSystemPath newHttpConfigFile;
    private final WebServerReachableState state;
    private final String errorStatus;

    public UpdateWebServerRequest(final Identifier<WebServer> theId,
                                  final Collection<Identifier<Group>> theNewGroupIds,
                                  final String theNewName,
                                  final String theNewHost,
                                  final Integer theNewPort,
                                  final Integer theNewHttpsPort,
                                  final Path theNewStatusPath,
                                  final FileSystemPath theNewHttpConfigFile,
                                  final Path theSvrRoot,
                                  final Path theDocRoot,
                                  final WebServerReachableState state,
                                  final String errorStatus) {
        id = theId;
        newHost = theNewHost;
        newPort = theNewPort;
        newHttpsPort = theNewHttpsPort;
        newName = theNewName;
        newGroupIds = Collections.unmodifiableCollection(new HashSet<>(theNewGroupIds));
        newStatusPath = theNewStatusPath;
        newHttpConfigFile = theNewHttpConfigFile;
        newSvrRoot = theSvrRoot;
        newDocRoot = theDocRoot;
        this.state = state;
        this.errorStatus = errorStatus;
    }

    public Identifier<WebServer> getId() {
        return id;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewHost() {
        return newHost;
    }

    public Integer getNewPort() {
        return newPort;
    }

    public Integer getNewHttpsPort() {
        return newHttpsPort;
    }

    public Collection<Identifier<Group>> getNewGroupIds() {
        return newGroupIds;
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    public FileSystemPath getNewHttpConfigFile() {
        return newHttpConfigFile;
    }

    public Path getNewSvrRoot() {
        return newSvrRoot;
    }

    public Path getNewDocRoot() {
        return newDocRoot;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    @Override
    public void validate() {
        final MultipleRules mr =
                new MultipleRules(new WebServerNameRule(newName),
                                  new HostNameRule(newHost),
                                  new PortNumberRule(newPort, AemFaultType.INVALID_WEBSERVER_PORT),
                                  new PortNumberRule(newHttpsPort, AemFaultType.INVALID_WEBSERVER_PORT, true),
                                  new WebServerIdRule(id),
                                  new GroupIdsRule(newGroupIds),
                                  new StatusPathRule(newStatusPath));

        mr.validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateWebServerRequest that = (UpdateWebServerRequest) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UpdateWebServerRequest{" +
                "id=" + id +
                ", newGroupIds=" + newGroupIds +
                ", newHost='" + newHost + '\'' +
                ", newName='" + newName + '\'' +
                ", newPort=" + newPort +
                ", newHttpsPort=" + newHttpsPort +
                ", newStatusPath=" + newStatusPath +
                ", newSvrRoot=" + newSvrRoot +
                ", newDocRoot=" + newDocRoot +
                ", newHttpConfigFile=" + newHttpConfigFile +
                ", state=" + state +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }

}