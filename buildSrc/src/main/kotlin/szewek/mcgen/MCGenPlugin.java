package szewek.mcgen;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;

@SuppressWarnings("unused")
public class MCGenPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final File buildDir = project.getBuildDir();
        final TaskContainer tasks = project.getTasks();
        final JavaPluginExtension javaExt = (JavaPluginExtension) project.getExtensions().getByType(JavaPluginExtension.class);
        javaExt.getSourceSets().configureEach(sourceSet -> {
                    if (!"test".equals(sourceSet.getName())) {
                        MCGenTasksKt.configureSourceSet(sourceSet, buildDir, tasks);
                    }
                });
    }
}
