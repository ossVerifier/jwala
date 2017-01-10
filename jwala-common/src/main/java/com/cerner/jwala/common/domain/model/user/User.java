package com.cerner.jwala.common.domain.model.user;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class User implements Serializable {

    private static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<User>();

    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

    public static User getSystemUser() {
        return new User("systemUser");
    }

    private final String id;

    public User(final String theId) {
        id = theId;
    }

    public String getId() {
        return id;
    }

    public void addToThread() {
        User.USER_THREAD_LOCAL.set(this);
    }

    public static User getThreadLocalUser() {
        return User.USER_THREAD_LOCAL.get();
    }

    public void invalidate() {
        User.USER_THREAD_LOCAL.set(null);
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
        User rhs = (User) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .toString();
    }
}
