package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.request.group.AddJvmsToGroupRequest;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@JsonDeserialize(using = JsonJvms.JsonJvmsDeserializer.class)
public class JsonJvms {

    private final Set<String> jvmIds;

    public JsonJvms(final Set<String> someJvmIds) {
        jvmIds = Collections.unmodifiableSet(new HashSet<>(someJvmIds));
    }

    public AddJvmsToGroupRequest toCommand(final Identifier<Group> aGroupId) {
        return new AddJvmsToGroupRequest(aGroupId,
                                         convertJvmIds());
    }

    protected Set<Identifier<Jvm>> convertJvmIds() {
        return new IdentifierSetBuilder(jvmIds).build();
    }

    static class JsonJvmsDeserializer extends AbstractJsonDeserializer<JsonJvms> {

        public JsonJvmsDeserializer() {
        }

        @Override
        public JsonJvms deserialize(final JsonParser jp,
                                    final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final Set<String> rawJvmIds = deserializeJvmIdentifiers(rootNode);

            return new JsonJvms(rawJvmIds);
        }
    }
}
