package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.uri.UriBuilder;

public class WebServer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> id;
    private final Map<Identifier<Group>, Group> groups = new ConcurrentHashMap<>();
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;
    private final Path statusPath;
    private final FileSystemPath httpConfigFile;

    /**
     * Constructor for a bare minimum web server with group details.
     * @param theId the id
     * @param theGroups the groups that the web server is assigned to.
     * @param theName the name of the web server.
     */
    public WebServer(final Identifier<WebServer> theId,
                     final Collection<Group> theGroups,
                     final String theName) {
        id = theId;
        host = null;
        port = null;
        name = theName;
        httpsPort = null;
        statusPath = null;
        httpConfigFile = null;
        for (final Group grp : theGroups) {
            groups.put(grp.getId(), grp);
        }
    }

    public WebServer(final Identifier<WebServer> theId,
                     final Collection<Group> theGroups,
                     final String theName,
                     final String theHost,
                     final Integer thePort,
                     final Integer theHttpsPort,
                     final Path theStatusPath,
                     final FileSystemPath theHttpConfigFile) {
        id = theId;
        host = theHost;
        port = thePort;
        name = theName;
        httpsPort = theHttpsPort;
        statusPath = theStatusPath;
        httpConfigFile = theHttpConfigFile;
        for (final Group grp : theGroups) {
            groups.put(grp.getId(), grp);
        }
    }

    public Identifier<WebServer> getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public Collection<Identifier<Group>> getGroupIds() {
        return groups.keySet();
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public FileSystemPath getHttpConfigFile() {
        return httpConfigFile;
    }

    public URI getStatusUri() {
        final UriBuilder builder = new UriBuilder().setHost(getHost())
                                                   .setPort(getPort())
                                                   .setHttpsPort(getHttpsPort())
                                                   .setPath(getStatusPath());
        return builder.buildUnchecked();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WebServer rhs = (WebServer) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.groups, rhs.groups)
                .append(this.host, rhs.host)
                .append(this.name, rhs.name)
                .append(this.port, rhs.port)
                .append(this.httpsPort, rhs.httpsPort)
                .append(this.statusPath, rhs.statusPath)
                .append(this.httpConfigFile, rhs.httpConfigFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(groups)
                .append(host)
                .append(name)
                .append(port)
                .append(httpsPort)
                .append(statusPath)
                .append(httpConfigFile)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("host", host)
                .append("name", name)
                .append("port", port)
                .append("httpsPort", httpsPort)
                .append("statusPath", statusPath)
                .append("httpConfigFile", httpConfigFile)
                .append("groups", groups)
                .toString();
    }
}
