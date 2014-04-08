package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class Jvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> id;
    private final String jvmName;
    private final String hostName;

    public Jvm(final Identifier<Jvm> theId,
               final String theName,
               final String theHostName) {
        id = theId;
        jvmName = theName;
        hostName = theHostName;
    }

    public Identifier<Jvm> getId() {
        return id;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Jvm jvm = (Jvm) o;

        if (hostName != null ? !hostName.equals(jvm.hostName) : jvm.hostName != null) {
            return false;
        }
        if (id != null ? !id.equals(jvm.id) : jvm.id != null) {
            return false;
        }
        if (jvmName != null ? !jvmName.equals(jvm.jvmName) : jvm.jvmName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmName != null ? jvmName.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Jvm{" +
               "id=" + id +
               ", jvmName='" + jvmName + '\'' +
               ", hostName='" + hostName + '\'' +
               '}';
    }
}
