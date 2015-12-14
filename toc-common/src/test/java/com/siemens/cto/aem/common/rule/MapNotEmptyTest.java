package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class MapNotEmptyTest {

    private Map<String, String> testMap = new HashMap<>();
    private Map<String, String> emptyMap = new HashMap<>();


    @Before
    public void setupMap() {
        testMap.put("Attribute_key", "Attribute_value");
    }

    @Test
    public void testGetMessageResponseStatus() {
        MapNotEmptyRule ruleToTest = new MapNotEmptyRule(testMap);
        ruleToTest.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testNullMap() {
        MapNotEmptyRule ruleToTest_bad = new MapNotEmptyRule(null);
        ruleToTest_bad.validate();
    }
    @Test(expected = BadRequestException.class)
    public void testEmptyMap() {
        MapNotEmptyRule rule = new MapNotEmptyRule(emptyMap);
        rule.validate();
    }
}
