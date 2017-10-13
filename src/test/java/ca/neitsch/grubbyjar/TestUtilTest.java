package ca.neitsch.grubbyjar;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUtilTest {
    @Test
    public void testGetCallingClass() {
      assertEquals(TestUtilTest.class.getName(), TestUtil.getCallingClassName(1));
    }
}
