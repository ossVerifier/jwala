package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateApplication.JsonCreateJvmDeserializer.class)
public class JsonCreateApplication {

    public JsonCreateApplication() {
    }

    public CreateApplicationCommand toCreateCommand() throws BadRequestException {

        return new CreateApplicationCommand(Identifier.id(0L, Group.class),"","");
    }

    static class JsonCreateJvmDeserializer extends AbstractJsonDeserializer<JsonCreateApplication> {

        public JsonCreateJvmDeserializer() {
        }

        @Override
        public JsonCreateApplication deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
//          final JsonNode jvmNode = rootNode.get("jvmName");
//          final JsonNode hostNameNode = rootNode.get("hostName");
//          final Set<String> rawGroupIds = deserializeGroupIdentifiers(rootNode);

            return new JsonCreateApplication();
        }
    }
}
