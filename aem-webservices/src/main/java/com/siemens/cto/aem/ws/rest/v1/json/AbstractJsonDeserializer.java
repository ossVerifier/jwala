package com.siemens.cto.aem.ws.rest.v1.json;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonDeserializer;

public abstract class AbstractJsonDeserializer<U> extends JsonDeserializer<U> {

    private static final String GROUP_ID = "groupId";
    private static final String GROUP_IDS = "groupIds";

    protected Set<String> deserializeGroupIdentifiers(final JsonNode node) {

        final Set<String> results = new HashSet<>();
        final JsonNode groupNode = node.get(GROUP_IDS);

        if (groupNode != null) {
            for (final JsonNode group : groupNode) {
                results.add(group.get(GROUP_ID).getValueAsText());
            }
        } else if (node.get(GROUP_ID) != null) {
            results.add(node.get(GROUP_ID).getValueAsText());
        }

        return results;
    }

}
