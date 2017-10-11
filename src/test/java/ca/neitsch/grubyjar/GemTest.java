package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GemTest {
    @Test
    public void testOnlySpecifiedFilesIncluded() {
        ShadowJar j = mock(ShadowJar.class);

        String includedFile = "foo/bar";
        String notIncludedFile = "foo/baz";

        Gem g = new Gem(ImmutableMap.of(
                Gem.FILES, new String[] {
                        includedFile
                }));

        assertTrue(g.include(fileTreeElement(includedFile)));
        assertFalse(g.include(fileTreeElement(notIncludedFile)));
    }

    static FileTreeElement fileTreeElement(String path) {
        FileTreeElement e = mock(FileTreeElement.class);
        RelativePath p = new RelativePath(false, path.split("/"));
        when(e.getRelativePath()).thenReturn(p);
        return e;
    }
}
