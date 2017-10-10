package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.Lists;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Gem {
    private static final String DETERMINE_GEM_FILES_RB = "determine_gem_files.rb";

    private RubyHash _hash;

    private Gem(RubyHash h) {
        _hash = h;
    }

    static List<Gem> loadGemDeps(Project project) {
        File gemfile = project.file("Gemfile");
        if (!gemfile.exists())
            return Collections.emptyList();

        File gemfileLock = project.file("Gemfile.lock");
        if (!gemfileLock.exists()) {
            throw new GradleException("Gemfile exists but Gemfile.lock does not; please run `bundle install`.");
        }

        ScriptingContainer s = new ScriptingContainer();

        s.runScriptlet(
                Gem.class.getResourceAsStream(DETERMINE_GEM_FILES_RB),
                DETERMINE_GEM_FILES_RB);

        RubyArray gems = (RubyArray)s.callMethod(null, "determine_gem_files",
                gemfile.toString(), gemfileLock.toString());

        List<Gem> gemList = Lists.newArrayList();
        for (Object o: gems) {
            gemList.add(new Gem((RubyHash)o));
        }
        return gemList;
    }

    void configure(ShadowJar jar) {
        jar.from(_hash.get("gemspec"),
                copyspec -> copyspec.into("specifications"));
        jar.from(_hash.get("install_path"),
                (copyspec) -> {
                    copyspec.into("gems/" + _hash.get("full_name"));
                    // Shadow’s getPath() and getName() are switched here,
                    // so we can’t rely on either in case it gets fixed.
                    copyspec.include((e) -> !e.getRelativePath().getLastName().equals("MANIFEST.MF"));
                });

    }
}
