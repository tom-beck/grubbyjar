package ca.neitsch.grubyjar.test;

import ca.neitsch.grubyjar.TestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestUtilTest {
  @Test
  public void testReadResource() {
    assertEquals("Hello world\n", TestUtil.readResource("foo.txt"));
  }
}
