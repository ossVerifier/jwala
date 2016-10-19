package com.cerner.jwala.common.domain.model.resource;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Custom JSON deserializer from content type string to {@link ContentType}
 *
 * Created by JC043760 on 10/6/2016.
 */
public class ContentTypeStrDeserializer extends StdDeserializer<ContentType> {

    // This constructor is required because we extended StdDeserializer
    public ContentTypeStrDeserializer() {
        this(null);
    }

    protected ContentTypeStrDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ContentType deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        return ContentType.fromContentTypeStr(jp.getText());
    }
}
