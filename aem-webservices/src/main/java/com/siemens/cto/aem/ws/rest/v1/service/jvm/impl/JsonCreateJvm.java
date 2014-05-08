package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateJvm.JsonCreateJvmDeserializer.class)
public class JsonCreateJvm {

    private final String jvmName;
    private final String hostName;
    private final Set<String> groupIds;

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName) {
        this(theJvmName,
             theHostName,
             Collections.<String>emptySet());
    }

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds) {
        jvmName = theJvmName;
        hostName = theHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(someGroupIds));
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
                                         final DeserializationContext ctxt) throws IOException {

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
