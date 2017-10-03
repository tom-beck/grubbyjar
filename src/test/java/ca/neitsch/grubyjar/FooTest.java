package ca.neitsch.grubyjar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FooTest {
    public @Test void testFoo() {
        System.out.println("unit test");
        assertEquals(2, 1 + 1);
    }
}
