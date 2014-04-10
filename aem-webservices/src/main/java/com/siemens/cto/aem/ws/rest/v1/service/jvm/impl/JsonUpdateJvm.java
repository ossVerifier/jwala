package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonUpdateJvm.JsonUpdateJvmDeserializer.class)
public class JsonUpdateJvm {

    private String jvmId;
    private String jvmName;
    private String hostName;
    private Set<String> groupIds;

    public JsonUpdateJvm() {
    }

    public JsonUpdateJvm(final String aJvmId,
                         final String aJvmName,
                         final String aHostName,
                         final Set<String> someGroupIds) {
        jvmId = aJvmId;
        jvmName = aJvmName;
        hostName = aHostName;
        groupIds = someGroupIds;
    }

    public String getJvmId() {
        return jvmId;
    }

    public void setJvmId(final String aJvmId) {
        jvmId = aJvmId;
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

    public void setGroupIds(final Set<String> someGroupIds) {
        groupIds = someGroupIds;
    }

    public UpdateJvmCommand toUpdateJvmCommand() throws BadRequestException {

        try {
            final Set<Identifier<Group>> groupIds = convertGroupIds();

            return new UpdateJvmCommand(new Identifier<Jvm>(jvmId),
                                        jvmName,
                                        hostName,
                                        groupIds);
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
                                         final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateJvm(node.get("jvmId").getValueAsText(),
                                     node.get("jvmName").getTextValue(),
                                     node.get("hostName").getTextValue(),
                                     deserializeGroupIdentifiers(node));
        }
    }
}
