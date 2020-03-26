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

        val recipeTask = tasks.create("gen${subName}RecipesFromBatch", GenTypeRecipes::class.java, confSources)
        val langTask = tasks.create("process${subName}LangFiles", ProcessLangFiles::class.java, confSources)
        val itemModelTask = tasks.create("gen${subName}ItemDefaultModels", GenDefaultModels::class.java, confSources)
        val itemBlockModelTask = tasks.create("gen${subName}ItemBlockModels", GenItemBlockModels::class.java, confSources)
        val tmplRecipes = tasks.create("process${subName}Recipes", TemplateRecipes::class.java, confSources)
        val tmplTags = tasks.create("process${subName}Tags", TemplateTags::class.java, confSources)
        val tmplLootTables = tasks.create("process${subName}LootTables", TemplateLootTables::class.java, confSources)
        val tmplBlockStates = tasks.create("process${subName}BlockStates", TemplateBlockStates::class.java, confSources)
        val tmplModels = tasks.create("process${subName}Models", TemplateModels::class.java, confSources)

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
}
