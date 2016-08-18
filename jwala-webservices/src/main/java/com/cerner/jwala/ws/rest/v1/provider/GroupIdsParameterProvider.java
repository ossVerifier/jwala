package com.cerner.jwala.ws.rest.v1.provider;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.group.Group;

import javax.ws.rs.QueryParam;
import java.util.HashSet;
import java.util.Set;

public class GroupIdsParameterProvider extends AbstractIdsParameterProvider<Group> {

    @QueryParam("groupId")
    private Set<String> groupIds;

    public GroupIdsParameterProvider(final Set<String> someGroupIds) {
        this();
        groupIds = new HashSet<>(someGroupIds);
    }

    public GroupIdsParameterProvider() {
        super("Invalid Group Identifier specified");
    }

    @Override
    protected Set<String> getIds() {
        return groupIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupIds", groupIds)
                .toString();
    }
}
