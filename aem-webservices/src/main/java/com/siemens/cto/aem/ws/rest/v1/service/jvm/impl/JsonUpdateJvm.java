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
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;

@JsonDeserialize(using = JsonUpdateJvm.JsonUpdateJvmDeserializer.class)
public class JsonUpdateJvm {

    private String jvmId;
    private String jvmName;
    private String hostName;

    public JsonUpdateJvm() {
    }

    public JsonUpdateJvm(final String aJvmId,
                         final String aJvmName,
                         final String aHostName) {
        jvmId = aJvmId;
        jvmName = aJvmName;
        hostName = aHostName;
    }

    public String getJvmId() {
        return jvmId;
    }

    public void setJvmId(final String jvmId) {
        this.jvmId = jvmId;
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

    public UpdateJvmCommand toUpdateJvmCommand() throws BadRequestException {

        try {

            return new UpdateJvmCommand(new Identifier<Jvm>(jvmId),
                                        jvmName,
                                        hostName);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    static class JsonUpdateJvmDeserializer extends JsonDeserializer<JsonUpdateJvm> {

        public JsonUpdateJvmDeserializer() {
        }

        @Override
        public JsonUpdateJvm deserialize(final JsonParser jp,
                                           final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateJvm(node.get("jvmId").getTextValue(),
                                     node.get("jvmName").getTextValue(),
                                     node.get("hostName").getTextValue());
        }
    }
}
