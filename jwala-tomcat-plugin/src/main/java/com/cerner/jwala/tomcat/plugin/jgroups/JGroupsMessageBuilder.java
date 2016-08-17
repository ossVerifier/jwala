package com.cerner.jwala.tomcat.plugin.jgroups;

import com.cerner.jwala.tomcat.plugin.MessageKey;
import org.jgroups.Address;
import org.jgroups.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * A message content builder
 *
 * Created by JC043760 on 8/15/2016
 */
public class JGroupsMessageBuilder {

    private String id;
    private String instanceId;
    private String asOf;
    private String type;
    private String state;
    private Address srcAddress;
    private Address destAddress;

    public JGroupsMessageBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public JGroupsMessageBuilder setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public JGroupsMessageBuilder setAsOf(String asOf) {
        this.asOf = asOf;
        return this;
    }

    // TODO: Verify if we still need this
    public JGroupsMessageBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public JGroupsMessageBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public JGroupsMessageBuilder setSrcAddress(Address srcAddress) {
        this.srcAddress = srcAddress;
        return this;
    }

    public JGroupsMessageBuilder setDestAddress(Address destAddress) {
        this.destAddress = destAddress;
        return this;
    }

    public Message build() {
        final Map<String, String> msgContentMap = new HashMap<>();
        msgContentMap.put(MessageKey.ID.name(), id);
        msgContentMap.put(MessageKey.AS_OF.name(), asOf);
        msgContentMap.put(MessageKey.INSTANCE_ID.name(), instanceId);
        msgContentMap.put(MessageKey.TYPE.name(), type);
        msgContentMap.put(MessageKey.STATE.name(), state);
        return new Message(destAddress, srcAddress, msgContentMap);
    }
}
