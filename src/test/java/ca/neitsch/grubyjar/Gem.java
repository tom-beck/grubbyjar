package ca.neitsch.grubyjar;

import com.google.common.base.MoreObjects;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;

public class Gem {
    private String _installName;
    private String _requireName;

    public Gem(String installName, String requireName) {
        _installName = installName;
        _requireName = requireName;
    }

    public boolean isInstalled() {
        ScriptingContainer s = new ScriptingContainer();
        try {
            Object o = s.runScriptlet("require '" + _requireName + "'");
            return true;
        } catch (EvalFailedException e) {
        }
        return false;
    }

    public void tryInstall() {
        ScriptingContainer s = new ScriptingContainer();
        s.runScriptlet(
                "require 'rubygems/gem_runner'\n"
                        + "Gem::GemRunner.new.run"
                        + " ['install', '--user', '" + _installName+ "']\n");
    }

    public void ensureInstalled() {
        if (isInstalled())
            return;
        tryInstall();;
        if (!isInstalled())
            throw new RuntimeException("Unable to install gem " + this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("installName", _installName)
                .add("reqiureName", _requireName)
                .toString();
    }
}
