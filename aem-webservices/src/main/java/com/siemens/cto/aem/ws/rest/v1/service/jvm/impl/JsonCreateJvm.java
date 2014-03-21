package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;

@JsonDeserialize(using = JsonCreateJvm.JsonCreateJvmDeserializer.class)
public class JsonCreateJvm {

    private String groupId;
    private String jvmName;
    private String hostName;

    public JsonCreateJvm() {
    }

    public JsonCreateJvm(final String aGroupId,
                         final String aJvmName,
                         final String aHostName) {
        groupId = aGroupId;
        jvmName = aJvmName;
        hostName = aHostName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String aGroupId) {
        groupId = aGroupId;
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

    public CreateJvmCommand toCreateJvmCommand() throws BadRequestException {

        try {
            return new CreateJvmCommand(new Identifier<Group>(groupId),
                                        jvmName,
                                        hostName);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    static class JsonCreateJvmDeserializer extends JsonDeserializer<JsonCreateJvm> {

        public JsonCreateJvmDeserializer() {
        }

        @Override
        public JsonCreateJvm deserialize(final JsonParser jp,
                                           final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            final JsonNode g = node.get("groupId");
            final JsonNode j = node.get("jvmName");
            final JsonNode h = node.get("hostName");

            return new JsonCreateJvm(node.get("groupId").getValueAsText(),
                                     node.get("jvmName").getTextValue(),
                                     node.get("hostName").getTextValue());
        }
    }
}
