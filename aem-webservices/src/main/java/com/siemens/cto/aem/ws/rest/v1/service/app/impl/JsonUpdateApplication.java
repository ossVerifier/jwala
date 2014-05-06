package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonUpdateApplication.JsonUpdateJvmDeserializer.class)
public class JsonUpdateApplication {

    public JsonUpdateApplication() {
    }

    public UpdateApplicationCommand toUpdateCommand() throws BadRequestException {

        return new UpdateApplicationCommand(Identifier.id(0L, Application.class),Identifier.id(0L, Group.class),"","");
    }

    static class JsonUpdateJvmDeserializer extends AbstractJsonDeserializer<JsonUpdateApplication> {

        public JsonUpdateJvmDeserializer() {
        }

        @Override
        public JsonUpdateApplication deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
//          final JsonNode jvmNode = rootNode.get("jvmName");
//          final JsonNode hostNameNode = rootNode.get("hostName");
//          final Set<String> rawGroupIds = deserializeGroupIdentifiers(rootNode);

            return new JsonUpdateApplication();
        }
    }
}
