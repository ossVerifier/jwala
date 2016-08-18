package com.cerner.jwala.common.request.group;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import com.cerner.jwala.common.rule.group.GroupNameRule;

import java.io.Serializable;

public class UpdateGroupRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String newName;

    public UpdateGroupRequest(final Identifier<Group> theId,
                              final String theNewName) {
        id = theId;
        newName = theNewName;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new GroupIdRule(id),
                                new GroupNameRule(newName)).validate();
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
        UpdateGroupRequest rhs = (UpdateGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newName, rhs.newName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("newName", newName)
                .toString();
    }
}
