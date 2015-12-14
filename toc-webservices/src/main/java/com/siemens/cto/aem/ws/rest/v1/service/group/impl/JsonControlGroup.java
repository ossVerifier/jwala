package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = JsonControlGroup.JsonControlGroupDeserializer.class)
public class JsonControlGroup {
    
    private final String controlOperation;

    public JsonControlGroup(final String theControlOperation) {
        if (theControlOperation != null) {
            controlOperation = theControlOperation;
        } else {
            controlOperation = "";
        }
    }

    public GroupControlOperation toControlOperation() {
        return GroupControlOperation.convertFrom(controlOperation);
    }

    static class JsonControlGroupDeserializer extends AbstractJsonDeserializer<JsonControlGroup> {

        public JsonControlGroupDeserializer() {
        }

        @Override
        public JsonControlGroup deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode operation = rootNode.get("controlOperation");

            return new JsonControlGroup(operation.getTextValue());
        }
    }

}
