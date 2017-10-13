package ca.neitsch.grubbyjar.test;

import ca.neitsch.grubbyjar.TestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestUtilTest {
  @Test
  public void testReadResource() {
    assertEquals("Hello world\n", TestUtil.readResource("foo.txt"));
  }
}
