package com.siemens.cto.toc.files;

import static com.siemens.cto.toc.files.RepositoryAction.Type.DELETED;
import static com.siemens.cto.toc.files.RepositoryAction.Type.NONE;
import static com.siemens.cto.toc.files.RepositoryAction.Type.STORED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(value=MockitoJUnitRunner.class)
public class RepositoryActionTest {

    @Test
    public void testVarious() {
        
        RepositoryAction none = RepositoryAction.none();
        assertEquals(NONE, none.getType());

        RepositoryAction noneIR = RepositoryAction.none(none);
        assertEquals(NONE, noneIR.getType());

        RepositoryAction noneIRarrayNull = RepositoryAction.none((RepositoryAction[])null);
        assertEquals(NONE, noneIRarrayNull.getType());

        RepositoryAction noneIRnull = RepositoryAction.none();
        assertEquals(NONE, noneIRnull.getType());

        Path resolvedPath = Paths.get("\\");
        Long copied = 0L;
        
        RepositoryAction stored = RepositoryAction.stored(resolvedPath, copied);
        assertEquals(STORED, stored.getType());

        RepositoryAction deleted = RepositoryAction.deleted(resolvedPath);
        assertEquals(DELETED, deleted.getType());

        assertNotNull(deleted.toString());
        assertNotNull(stored.toString());
        assertNotNull(noneIR.toString());
        assertTrue(deleted.hashCode() >0);
        assertTrue(noneIR.getCauses()[0].equals(noneIRnull));
    }

}
