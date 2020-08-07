package szewek.mcgen

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import szewek.mcgen.task.*
import java.io.File

@Suppress("UnstableApiUsage")
class MCGenPlugin : Plugin<Project> {

    override fun apply(p: Project) {
        val jpc = p.convention.getPlugin(JavaPluginConvention::class.java)

        jpc.sourceSets.configureEach {
            configureSourceSet(it, p.buildDir, p.tasks)
        }
    }

    private fun configureSourceSet(srcSet: SourceSet, buildDir: File, tasks: TaskContainer) {
        val genResourcesDir = File(buildDir, "genResources/" + srcSet.name)
        val subName = if (srcSet.name == "main") "" else srcSet.name.capitalize()
        val confSources = Action<AbstractProcessTask> { it.configureSources(srcSet.resources, srcSet.name) }

        val recipeTask = tasks.make<GenTypeRecipes>("genRecipesFromBatch", subName, confSources)
        val langTask = tasks.make<ProcessLangFiles>("processLangFiles", subName, confSources)
        val itemModelTask = tasks.make<GenDefaultModels>("genItemDefaultModels", subName, confSources)
        val itemBlockModelTask = tasks.make<GenItemBlockModels>("genItemBlockModels", subName, confSources)
        val tmplRecipes = tasks.make<TemplateRecipes>("processRecipes", subName, confSources)
        val tmplTags = tasks.make<TemplateTags>("processTags", subName, confSources)
        val tmplLootTables = tasks.make<TemplateLootTables>("processLootTables", subName, confSources)
        val tmplBlockStates = tasks.make<TemplateBlockStates>("processBlockStates", subName, confSources)
        val tmplModels = tasks.make<TemplateModels>("processModels", subName, confSources)

        srcSet.resources.srcDir(genResourcesDir)
        val processResources = tasks.getByName("process${subName}Resources") as AbstractCopyTask
        processResources.doFirst {
            processResources.exclude("generators")
            processResources.exclude("templates")
        }
        processResources.dependsOn(
                itemModelTask, itemBlockModelTask, langTask, recipeTask,
                tmplRecipes, tmplTags, tmplLootTables, tmplBlockStates, tmplModels
        )
    }

    private inline fun <reified T: AbstractProcessTask> TaskContainer.make(name: String, sub: String, action: Action<in T>) =
            this.create(if (sub != "") "${name}_$sub" else name, T::class.java, action)
}
