package com.siemens.cto.aem.common.domain.model.resource;

/**
 * Content type enumeration e.g. application/xml etc...
 * Note: Maybe a better alternative is to use http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/net/MediaType.html.
 *       Anyways this will do for now.
 *
 * Created by JC043760 on 3/31/2016.
 */
public enum ContentType {
    XML_UTF_8("text/xml"), PLAIN_TEXT_UTF_8("text/plain"), APPLICATION_BINARY("application/binary"), UNDEFINED(null);

    public final String contentTypeStr;

    ContentType(final String contentTypeStr) {
        this.contentTypeStr = contentTypeStr;
    }

    /**
     * Convert's a String content type to {@link ContentType} enum.
     * @param contentTypeStr the content type in string format e.g. text/plain
     * @return {@link ContentType} if contentTypeStr has a match, null if there's none
     */
    public static ContentType fromContentTypeStr(final String contentTypeStr) {
        for (final ContentType contentType: ContentType.values()) {
            if (contentType.contentTypeStr.equalsIgnoreCase(contentTypeStr)) {
                return contentType;
            }
        }
        return UNDEFINED;
    }
}
