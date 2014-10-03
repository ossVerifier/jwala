package com.siemens.cto.aem.domain.model.path;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Path implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern absoluteRegEx = Pattern.compile("^(([a-zA-Z]:)|([\\/])).*");
    private final String path;

    public Path(final String thePath) {
        path = thePath;
    }

    public boolean isAbsolute() {
        return absoluteRegEx.matcher(path).matches();
    }

    public String getPath() {
        return path;
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
        Path rhs = (Path) obj;
        return new EqualsBuilder()
                .append(this.path, rhs.path)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(path)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .toString();
    }
}
