package ca.neitsch.grubyjar;

import org.apache.commons.io.IOUtils;
import org.gradle.api.tasks.Sync;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ca.neitsch.grubyjar.TaskUtil.doLastRethrowing;
import static com.google.common.collect.Lists.newArrayList;

public class GrubyjarRequireTask
        extends Sync
{
    public GrubyjarRequireTask() {
        from(GradleUtil.getRuntimeConfiguration(getProject()));
        into("lib/ext");

        doLastRethrowing(this, this::createRequiresFile);
    }

    private void createRequiresFile() throws IOException {
        File libDir = getProject().file("lib");
        libDir.mkdir();
        File requiresFile = new File(libDir, getProject().getName() + "_jars.rb");

        List<String> depJars = newArrayList();
        GradleUtil.getRuntimeConfiguration(getProject()).forEach(f -> {
            depJars.add(f.getName());
        });
        depJars.sort(String::compareTo);

        StringBuilder requires = new StringBuilder();
        for (String depJar: depJars) {
          requires.append("    require_relative 'ext/" + depJar + "'\n");
        }

        String template = IOUtils.toString(getClass().getResource("jars_template.rb"),
                StandardCharsets.UTF_8);

        template = template.replace("__requires_go_here__\n", requires.toString());

        Util.writeTextToFile(template, requiresFile);
    }
}
