package szewek.mcgen

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.AbstractCopyTask
import szewek.mcgen.task.*
import java.io.File

class MCGenPlugin : Plugin<Project> {

    override fun apply(p: Project) {
        val genResourcesDir = File(p.buildDir, "genResources")

        val jpc = p.convention.getPlugin(JavaPluginConvention::class.java)
        val srcSet = jpc.sourceSets.getByName("main")

        val confSources = Action<AbstractProcessTask> { it.configureSources(srcSet.resources) }

        val recipeTask = p.tasks.create("genRecipesFromBatch", GenTypeRecipes::class.java, confSources)
        val langTask = p.tasks.create("processLangFiles", ProcessLangFiles::class.java, confSources)
        val itemModelTask = p.tasks.create("genItemDefaultModels", GenDefaultModels::class.java, confSources)
        val itemBlockModelTask = p.tasks.create("genItemBlockModels", GenItemBlockModels::class.java, confSources)
        val tmplRecipes = p.tasks.create("processTemplateRecipes", TemplateRecipes::class.java, confSources)
        val tmplTags = p.tasks.create("processTemplateTags", TemplateTags::class.java, confSources)
        val tmplLootTables = p.tasks.create("processLootTables", TemplateLootTables::class.java, confSources)
        val tmplBlockStates = p.tasks.create("processBlockStates", TemplateBlockStates::class.java, confSources)
        val tmplModels = p.tasks.create("processModels", TemplateModels::class.java, confSources)

        srcSet.resources.srcDir(genResourcesDir)
        val processResources = p.tasks.getByName("processResources") as AbstractCopyTask
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
