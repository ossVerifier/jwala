package com.cerner.jwala.common.rule;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.PathRule;

import static org.junit.Assert.assertTrue;

/**
 * Created by Z003BPEJ on 1/13/15.
 */
public class PathRuleTest {

    private PathRule pathRule;

    @Test
    public void testIsValidPathLowerCase() {
        pathRule = new PathRule(new Path("htdocs"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testIsValidPathUpperCase() {
        pathRule = new PathRule(new Path("HTDOCS"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testIsValidPathMixedCase() {
        pathRule = new PathRule(new Path("hTDOCs"));
        assertTrue(pathRule.isValid());
    }

    @Test(expected = BadRequestException.class)
    public void testPathIsNull() {
        pathRule = new PathRule(new Path(null));
        pathRule.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testPathInvalidChars() {
        pathRule = new PathRule(new Path("ht#docs"));
        pathRule.validate();
    }

    @Test
    public void testPathWithSpace() {
        pathRule = new PathRule(new Path("~/some\\ Dir"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testPathWithSpaces() {
        pathRule = new PathRule(new Path("~/some\\ Dir1/some\\ Dir2"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testPathHasForwardSlash() {
        pathRule = new PathRule(new Path("./"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testPathHasForwardSlashes() {
        pathRule = new PathRule(new Path("/someDir1/someDir2"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testPathHasBackSlash() {
        pathRule = new PathRule(new Path("\\someDir"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testPathHasBackSlashes() {
        pathRule = new PathRule(new Path("\\someDir1\\someDir2"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testLocalPath() {
        pathRule = new PathRule(new Path("c:\\someDir1\\someDir2"));
        assertTrue(pathRule.isValid());
    }

    @Test
    public void testComplicatedPath() {
        pathRule = new PathRule(new Path("c:\\some-V+al1_\\some/Val2?"));
        assertTrue(pathRule.isValid());
    }

}
