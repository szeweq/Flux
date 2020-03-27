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

        val recipeTask = tasks.make<GenTypeRecipes>("gen${subName}RecipesFromBatch", confSources)
        val langTask = tasks.make<ProcessLangFiles>("process${subName}LangFiles", confSources)
        val itemModelTask = tasks.make<GenDefaultModels>("gen${subName}ItemDefaultModels", confSources)
        val itemBlockModelTask = tasks.make<GenItemBlockModels>("gen${subName}ItemBlockModels", confSources)
        val tmplRecipes = tasks.make<TemplateRecipes>("process${subName}Recipes", confSources)
        val tmplTags = tasks.make<TemplateTags>("process${subName}Tags", confSources)
        val tmplLootTables = tasks.make<TemplateLootTables>("process${subName}LootTables", confSources)
        val tmplBlockStates = tasks.make<TemplateBlockStates>("process${subName}BlockStates", confSources)
        val tmplModels = tasks.make<TemplateModels>("process${subName}Models", confSources)

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

    private inline fun <reified T: AbstractProcessTask> TaskContainer.make(name: String, action: Action<in T>) =
            this.create(name, T::class.java, action)
}
