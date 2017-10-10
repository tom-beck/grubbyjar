package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class GemTest {
    @Ignore
    @Test
    public void testBasics() {
        ShadowJar j = mock(ShadowJar.class);

        Gem g = new Gem(ImmutableMap.of(
        ));

        g.configure(j, null);
    }
}
