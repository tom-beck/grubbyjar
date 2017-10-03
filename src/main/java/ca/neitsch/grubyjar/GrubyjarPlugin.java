package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ApplicationPluginConvention;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class GrubyjarPlugin
        implements Plugin<Project> {

    @VisibleForTesting
    public static final String GRUBYJAR_MAIN_RB = "grubyjar_main.rb";
    private static final String GRUBYJAR_MAIN = "GrubyjarMain";

    @Override
    public void apply(Project project) {
        project.apply(ImmutableMap.of("plugin", "application"));
        project.apply(ImmutableMap.of("plugin", "com.github.johnrengelman.shadow"));

        Map<String, Object> config = ImmutableMap.of(
                "type", ShadowJar.class);

        ApplicationPluginConvention pluginConvention = (ApplicationPluginConvention)project.getConvention().getPlugins().get("application");
        pluginConvention.setMainClassName(GRUBYJAR_MAIN);

        Task grubyJarTask = project.task("grubyjar");
        ShadowJar shadowJarTask = (ShadowJar)project.getTasks().getByName("shadowJar");
        grubyJarTask.dependsOn(shadowJarTask);

        // It is a compile time error to import a type from the unnamed package.
        // https://stackoverflow.com/a/2193298
        Class<?> grubyJarMainClass = null;
        try {
            grubyJarMainClass = Class.forName(GRUBYJAR_MAIN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        InputStream mainClass = getClassDefinition(grubyJarMainClass);

        File grubyjarDir = new File(project.getBuildDir(),
                "grubyjar");
        rethrowing(() -> {
            FileUtils.deleteDirectory(grubyjarDir);
        });
        grubyjarDir.mkdirs();
        File mainClassTarget = new File(grubyjarDir, GRUBYJAR_MAIN + ".class");
        rethrowing(() -> {
            FileUtils.copyInputStreamToFile(mainClass, mainClassTarget);
        });

        rethrowing(() -> {
            FileUtils.copyFile(project.file("foo.rb"),
                    new File(grubyjarDir, GRUBYJAR_MAIN_RB));
        });

        shadowJarTask.from(grubyjarDir);

        shadowJarTask.setArchiveName(getArchiveName());
    }

    private String getArchiveName() {
        return "foo.jar";
    }

    private InputStream getClassDefinition(Class<?> c) {
        String path = c.getName().replace(".", "/") + ".class";
        InputStream ret = c.getResourceAsStream(path);
        if (ret == null) {
            throw new RuntimeException("Could not find " + path);
        }
        return ret;
    }

    private static <E extends Exception> void  rethrowing (Exceptionable<E> r) {
        try {
            r.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
