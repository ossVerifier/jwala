package com.cerner.jwala.files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.cerner.jwala.files.NameSynthesizer;
import com.cerner.jwala.files.impl.DefaultNameSynthesizer;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.assertNotEquals;

@RunWith(value=MockitoJUnitRunner.class)
public class NameSynthesizerTest {

    NameSynthesizer cut;
    String name = "temp.war";
    
    @Before public void setUp() { 
        cut = new DefaultNameSynthesizer();
    }
    
    @Test public void test100CombinatorialAreAllUnique() {     
        
        FileSystem sys = FileSystems.getDefault();
        Path original = sys.getPath(name);
        
        Path[] paths = new Path[100];
        
        for(int i = 0; i < paths.length; ++i) {
            paths[i] = cut.unique(original);
        }
        
        for(int j = 0; j < paths.length; ++j) {
            for(int k = 0; k < paths.length; ++k) {
                if(j != k) { 
                    assertNotEquals("Paths " + j + " and " + k + " are not different: " + paths[j], paths[j], paths[k]);
                }
            }
        }
        
    }
}
