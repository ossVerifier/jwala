package com.cerner.jwala.files;

import org.junit.Test;

import com.cerner.jwala.files.RepositoryFileInformation;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.cerner.jwala.files.RepositoryFileInformation.Type.*;
import static org.junit.Assert.*;

public class RepositoryFileInformationTest {

    @Test
    public void testVarious() {
        
        RepositoryFileInformation none = RepositoryFileInformation.none();
        assertEquals(NONE, none.getType());

        RepositoryFileInformation noneIR = RepositoryFileInformation.none(none);
        assertEquals(NONE, noneIR.getType());

        RepositoryFileInformation noneIRarrayNull = RepositoryFileInformation.none((RepositoryFileInformation[]) null);
        assertEquals(NONE, noneIRarrayNull.getType());

        RepositoryFileInformation noneIRnull = RepositoryFileInformation.none();
        assertEquals(NONE, noneIRnull.getType());

        Path resolvedPath = Paths.get("\\");
        Long copied = 0L;
        
        RepositoryFileInformation stored = RepositoryFileInformation.stored(resolvedPath, copied);
        assertEquals(STORED, stored.getType());

        RepositoryFileInformation deleted = RepositoryFileInformation.deleted(resolvedPath);
        assertEquals(DELETED, deleted.getType());

        assertNotNull(deleted.toString());
        assertNotNull(stored.toString());
        assertNotNull(noneIR.toString());
        assertTrue(deleted.hashCode() != 0);
        assertTrue(noneIR.getCauses()[0].equals(noneIRnull));

        // check consistency between construction mechanisms.
        assertEquals(RepositoryFileInformation.deleted(resolvedPath),new RepositoryFileInformation(DELETED, resolvedPath, null, new RepositoryFileInformation[0]));
        assertEquals(RepositoryFileInformation.deleted(resolvedPath).hashCode(),new RepositoryFileInformation(DELETED, resolvedPath, null, new RepositoryFileInformation[0]).hashCode());

    }

}
