package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateJvm.JsonCreateJvmDeserializer.class)
public class JsonCreateJvm {

    private String jvmName;
    private String hostName;
    private Set<String> groupIds;

    public JsonCreateJvm() {
    }

    public JsonCreateJvm(final String jvmName,
                         final String hostName) {
        this(jvmName,
             hostName,
             Collections.<String>emptySet());
    }

    public JsonCreateJvm(final String aJvmName,
                         final String aHostName,
                         final Set<String> someGroupIds) {
        jvmName = aJvmName;
        hostName = aHostName;
        groupIds = someGroupIds;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(final String aJvmName) {
        jvmName = aJvmName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String aHostName) {
        hostName = aHostName;
    }

    public Set<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(final Set<String> groupIds) {
        this.groupIds = groupIds;
    }

    public boolean areGroupsPresent() {
        return !groupIds.isEmpty();
    }

    public CreateJvmCommand toCreateJvmCommand() throws BadRequestException {

        return new CreateJvmCommand(jvmName,
                                    hostName);
    }

    public CreateJvmAndAddToGroupsCommand toCreateAndAddCommand() throws BadRequestException {
        final Set<Identifier<Group>> groups = convertGroupIds();

        return new CreateJvmAndAddToGroupsCommand(jvmName,
                                                  hostName,
                                                  groups);
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    static class JsonCreateJvmDeserializer extends AbstractJsonDeserializer<JsonCreateJvm> {

        public JsonCreateJvmDeserializer() {
        }

        @Override
        public JsonCreateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode jvmNode = rootNode.get("jvmName");
            final JsonNode hostNameNode = rootNode.get("hostName");
            final Set<String> rawGroupIds = deserializeGroupIdentifiers(rootNode);

            return new JsonCreateJvm(jvmNode.getTextValue(),
                                     hostNameNode.getTextValue(),
                                     rawGroupIds);
        }
    }
}
