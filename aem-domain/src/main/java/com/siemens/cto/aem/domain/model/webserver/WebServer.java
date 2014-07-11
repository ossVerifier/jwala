package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class WebServer implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String NO_USER_INFO = null;
    public static final String NO_QUERY = null;
    public static final String NO_FRAGMENT = null;

    private final Identifier<WebServer> id;
    private final Map<Identifier<Group>, Group> groups = new ConcurrentHashMap<>();
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;

    public WebServer(final Identifier<WebServer> theId, final Collection<Group> theGroups, final String theName,
            final String theHost, final Integer thePort, final Integer theHttpsPort) {
        id = theId;
        host = theHost;
        port = thePort;
        name = theName;
        httpsPort = theHttpsPort;
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

    public String getStatusPath() {
        //TODO This should eventually be user-configurable
        return "/jk/status";
    }

    public URI getStatusUri() {
        try {
            final URI uri = new URI("http",
                                    NO_USER_INFO,
                                    getHost(),
                                    getPort(),
                                    getStatusPath(),
                                    NO_QUERY,
                                    NO_FRAGMENT);
            return uri;
        } catch (final URISyntaxException urise) {
            throw new RuntimeException("Unable to construct the URI for WebServer Status", urise);
        }
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
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("groups", groups)
                .append("host", host)
                .append("name", name)
                .append("port", port)
                .append("httpsPort", httpsPort)
                .toString();
    }
}
