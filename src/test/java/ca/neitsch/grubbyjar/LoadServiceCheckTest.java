package ca.neitsch.grubbyjar;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class LoadServiceCheckTest {
    private LoadServiceCheck _check;

    @Before
    public void setUp() {
        _check = new LoadServiceCheck(null);
    }

    @Test
    public void testJavaNames() {
        assertThat(_check.javaClassNames("abc/xyz/blah_blah2"),
                containsInAnyOrder(
                        "BlahBlah2Service",
                        "xyz/BlahBlah2Service",
                        "abc/xyz/BlahBlah2Service"
                ));
    }
}
