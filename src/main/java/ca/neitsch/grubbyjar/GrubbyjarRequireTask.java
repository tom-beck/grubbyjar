package ca.neitsch.grubbyjar;

import org.apache.commons.io.IOUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Sync;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import static ca.neitsch.grubbyjar.TaskUtil.doLastRethrowing;
import static com.google.common.collect.Lists.newArrayList;

public class GrubbyjarRequireTask
        extends Sync
{
    private Jar _jar = null;

    public GrubbyjarRequireTask() {
        from(GradleUtil.getRuntimeConfiguration(getProject()));
        into("lib/ext");
        if(projectHasSource()) {
            _jar = (Jar)getProject().getTasks().findByPath("jar");
            from(_jar);
        }

        doLastRethrowing(this, this::createRequiresFile);
    }

    private void createRequiresFile() throws IOException {
        File libDir = getProject().file("lib");
        libDir.mkdir();
        File requiresFile = new File(libDir, getProject().getName() + "_jars.rb");

        List<String> depJars = newArrayList();

        if (projectHasSource()) {
            depJars.add(_jar.getArchiveName());
        }

        Configuration runtime = GradleUtil.getRuntimeConfiguration(getProject());

        runtime.forEach(f -> {
            depJars.add(f.getName());
        });
        depJars.sort(String::compareTo);

        for (Dependency d: runtime.getAllDependencies()) {
            if (d.getGroup().equals("org.jruby")
                    && d.getName().equals("jruby-complete"))
            {
                runtime.files(d).forEach(f ->
                        depJars.remove(f.getName()));
            }
        }

        StringBuilder requires = new StringBuilder();
        for (String depJar: depJars) {
            requires.append("require_relative 'ext/" + depJar + "'\n");
        }

        String template = IOUtils.toString(getClass().getResource("jars_template.rb"),
                StandardCharsets.UTF_8);

        template = template.replace("__requires_go_here__\n", requires.toString());

        Util.writeTextToFile(template, requiresFile);
    }

    Jar getJar() {
        return _jar;
    }

    private boolean projectHasSource() {
        JavaPluginConvention java = getProject().getConvention()
                .getPlugin(JavaPluginConvention.class);
        Iterator<File> iterator = java.getSourceSets().getByName("main")
                .getAllSource().iterator();
        return iterator.hasNext();
    }
}
