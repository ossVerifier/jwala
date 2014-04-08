package com.siemens.cto.aem.ws.rest.v1.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonDeserializer;

public abstract class AbstractJsonDeserializer<U> extends JsonDeserializer<U> {

    protected List<String> deserializeGroupIdentifiers(JsonNode node) {
        List<String> results = new ArrayList<String>();

        final JsonNode groupNode = node.get("groupIds");
        if(groupNode != null) {
            Iterator<JsonNode> groupIt = groupNode.getElements();
            while(groupIt.hasNext()) {
                JsonNode groupEntry = groupIt.next();
                results.add(groupEntry.get("groupId").getValueAsText());
            }
        } else if(node.get("groupId") != null) {
            results.add(node.get("groupId").getValueAsText());
        }
        
        return results;
    }

}
