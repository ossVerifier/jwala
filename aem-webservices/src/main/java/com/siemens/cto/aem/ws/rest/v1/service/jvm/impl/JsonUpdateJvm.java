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
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonUpdateJvm.JsonUpdateJvmDeserializer.class)
public class JsonUpdateJvm {

    private final String jvmId;
    private final String jvmName;
    private final String hostName;
    private final Set<String> groupIds;

    public JsonUpdateJvm(final String theJvmId,
                         final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds) {
        jvmId = theJvmId;
        jvmName = theJvmName;
        hostName = theHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(someGroupIds));
    }

    public UpdateJvmCommand toUpdateJvmCommand() throws BadRequestException {

        final Identifier<Jvm> id = convertJvmId();
        final Set<Identifier<Group>> groupIds = convertGroupIds();

        return new UpdateJvmCommand(id,
                                    jvmName,
                                    hostName,
                                    groupIds);
    }

    protected Identifier<Jvm> convertJvmId() {
        try {
            return new Identifier<>(jvmId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    static class JsonUpdateJvmDeserializer extends AbstractJsonDeserializer<JsonUpdateJvm> {

        public JsonUpdateJvmDeserializer() {
        }

        @Override
        public JsonUpdateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateJvm(node.get("jvmId").getValueAsText(),
                                     node.get("jvmName").getTextValue(),
                                     node.get("hostName").getTextValue(),
                                     deserializeGroupIdentifiers(node));
        }
    }
}
