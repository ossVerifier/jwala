package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class WebServer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> id;
    private final Map<Identifier<Group>, Group> groups = new ConcurrentHashMap<>();
    private final String host;
    private final String name;
    private final Integer port;

    public WebServer(final Identifier<WebServer> theId, final Collection<Group> theGroups, final String theName,
            final String theHost, final Integer thePort) {
        id = theId;
        host = theHost;
        port = thePort;
        name = theName;
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

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public Collection<Identifier<Group>> getGroupIds() {
        return groups.keySet();
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.InsufficientBranchCoverage"})
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (groups == null ? 0 : groups.hashCode());
        result = prime * result + (host == null ? 0 : host.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (port == null ? 0 : port.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebServer other = (WebServer) obj;
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (port == null) {
            if (other.port != null) {
                return false;
            }
        } else if (!port.equals(other.port)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WebServer {id=" + id + ", groups=" + groups + ", host=" + host + ", name=" + name + ", port=" + port
                + "}";
    }
}
