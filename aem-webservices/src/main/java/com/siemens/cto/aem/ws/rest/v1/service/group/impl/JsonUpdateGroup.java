package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

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
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;

@JsonDeserialize(using = JsonUpdateGroup.JsonUpdateGroupDeserializer.class)
public class JsonUpdateGroup {

    private String id;
    private String name;

    public JsonUpdateGroup() {
    }

    public JsonUpdateGroup(final String anId,
                           final String aName) {
        id = anId;
        name = aName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public UpdateGroupCommand toUpdateGroupCommand() throws BadRequestException {
        try {
            final Identifier<Group> groupId = new Identifier<>(id);
            return new UpdateGroupCommand(groupId,
                                          name);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          nfe.getMessage());
        }
    }

    static class JsonUpdateGroupDeserializer extends JsonDeserializer<JsonUpdateGroup> {

        public JsonUpdateGroupDeserializer() {
        }

        @Override
        public JsonUpdateGroup deserialize(final JsonParser jp,
                                           final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateGroup(node.get("id").getTextValue(),
                                       node.get("name").getTextValue());
        }
    }
}
