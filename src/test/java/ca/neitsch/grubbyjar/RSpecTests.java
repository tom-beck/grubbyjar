package ca.neitsch.grubbyjar;

import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RSpecTests {
    @Test
    public void determineGemFileSpec() {
        ScriptingContainer s = new ScriptingContainer();

        Object o = s.runScriptlet("require 'rspec'\n"
                + "RSpec::Core::Runner.run(['spec/determine_gem_file_spec.rb'])\n");
        long returnValue = (Long)o;
        assertEquals("rspec return value", 0, returnValue);
    }
}
