package com.siemens.cto.toc.files;

import com.siemens.cto.toc.files.impl.DefaultNameSynthesizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;

import static junit.framework.Assert.assertTrue;

@RunWith(value=MockitoJUnitRunner.class)
public class DefaultNameSynthesizerTest {

    @Test
    public void testUnique() {
        DefaultNameSynthesizer defaultNameSynthesizer = new DefaultNameSynthesizer();
        final File file = new File("./src/test/resources/testDefaultNameSynthesizer.war");
        Path result = defaultNameSynthesizer.unique(file.toPath());
        System.out.println("JMJM " + result);
        assertTrue(result.toString().length() > file.getName().length());
        assertTrue(result.toString().startsWith("testDefaultNameSynthesizer-"));
        assertTrue(result.toString().endsWith(".war"));
    }

    @Test
    public void testUniqueWithNoDot() {
        DefaultNameSynthesizer defaultNameSynthesizer = new DefaultNameSynthesizer();
        final File file = new File("./src/test/resources/testDefaultNameSynthesizer");
        Path result = defaultNameSynthesizer.unique(file.toPath());
        System.out.println("JMJM " + result);
        assertTrue(result.toString().length() > file.getName().length());
        assertTrue(result.toString().startsWith("testDefaultNameSynthesizer-"));
        assertTrue(result.toString().endsWith(""));
    }

}
