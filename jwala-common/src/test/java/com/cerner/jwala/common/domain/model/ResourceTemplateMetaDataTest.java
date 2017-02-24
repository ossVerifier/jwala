package com.cerner.jwala.common.domain.model;

import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static groovy.util.GroovyTestCase.assertEquals;

/**
 * Unit test for {@link com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData}
 *
 * Created by JC043760 on 10/6/2016.
 */
public class ResourceTemplateMetaDataTest {

    private static final String META_DATA_JSON = "{\n" +
            "    \"templateName\": \"SetenvBatTemplate.tpl\",\n" +
            "    \"contentType\": \"text/plain\",\n" +
            "    \"deployFileName\": \"setenv.bat\",\n" +
            "    \"deployPath\": \"${vars.'remote.paths.instances'}/${jvm.jvmName}/bin\",\n" +
            "    \"entity\": {\n" +
            "        \"type\": \"GROUPED_JVMS\",\n" +
            "        \"group\": \"HEALTH CHECK 4.0\",\n" +
            "        \"target\": \"HEALTH CHECK 4.0\",\n" +
            "        \"parentName\": null,\n" +
            "        \"deployToJvms\": true\n" +
            "    },\n" +
            "    \"unpack\": false\n" +
            "}";

    private static final String EXPECTED_META_DATA_STR = "ResourceTemplateMetaData{templateName='SetenvBatTemplate.tpl', " +
            "contentType=PLAIN_TEXT_UTF_8, deployFileName='setenv.bat', deployPath='${vars.'remote.paths.instances'}/${jvm.jvmName}/bin', " +
            "entity=Entity{type='GROUPED_JVMS', group='HEALTH CHECK 4.0', target='HEALTH CHECK 4.0', parentName='null', deployToJvms=true}, " +
            "unpack=false, overwrite=true}";

    private static final String EXPECTED_META_DATA_JSON = "{\"templateName\":\"SetenvBatTemplate.tpl\",\"contentType\"" +
            ":\"text/plain\",\"deployFileName\":\"setenv.bat\",\"deployPath\":\"${vars.'remote.paths.instances'}/" +
            "${jvm.jvmName}/bin\",\"entity\":{\"type\":\"GROUPED_JVMS\",\"group\":\"HEALTH CHECK 4.0\",\"target\":\"" +
            "HEALTH CHECK 4.0\",\"parentName\":null,\"deployToJvms\":true},\"unpack\":false,\"overwrite\":true}";

    @Test
    public void testCreateMetaData() throws IOException {
        final ResourceTemplateMetaData metaData = ResourceTemplateMetaData.createFromJsonStr(META_DATA_JSON);
        assertEquals(EXPECTED_META_DATA_STR, metaData.toString());

        // test serialization too!
        assertEquals(EXPECTED_META_DATA_JSON, new ObjectMapper().writeValueAsString(metaData));
    }
}
