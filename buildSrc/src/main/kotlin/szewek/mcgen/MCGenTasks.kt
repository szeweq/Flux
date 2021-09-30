package szewek.mcgen

import org.gradle.api.Action
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import szewek.mcgen.task.*
import java.io.File
import java.util.*

internal fun configureSourceSet(srcSet: SourceSet, buildDir: File, tasks: TaskContainer) {
    val genResourcesDir = File(buildDir, "genResources/" + srcSet.name)
    val subName = if (srcSet.name == "main") "" else srcSet.name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    val confSources = Action<AbstractProcessTask> { it.configureSources(srcSet.resources, srcSet.name) }

    val srcType = srcSet.resources.srcDirs.stream()
        .mapToInt(::checkDirs)
        .reduce(0, Int::or)

    val processRsrc = tasks.getByName("process${subName}Resources") as AbstractCopyTask

    if (srcType and 1 != 0) {
        val recipeTask = tasks.make<GenTypeRecipes>("genRecipesBatch", subName, confSources)
        val langTask = tasks.make<ProcessLangFiles>("processLangFiles", subName, confSources)
        val itemModelTask = tasks.make<GenDefaultModels>("genItemDefaultModels", subName, confSources)
        val itemBlockModelTask = tasks.make<GenItemBlockModels>("genItemBlockModels", subName, confSources)
        processRsrc.dependsOn(
            itemModelTask, itemBlockModelTask, langTask, recipeTask
        )
        processRsrc.doFirst { processRsrc.exclude("generators") }
    }

    if (srcType and 2 != 0) {
        val tmplRecipes = tasks.make<TemplateRecipes>("processRecipes", subName, confSources)
        val tmplTags = tasks.make<TemplateTags>("processTags", subName, confSources)
        val tmplLootTables = tasks.make<TemplateLootTables>("processLootTables", subName, confSources)
        val tmplBlockStates = tasks.make<TemplateBlockStates>("processBlockStates", subName, confSources)
        val tmplModels = tasks.make<TemplateModels>("processModels", subName, confSources)
        processRsrc.dependsOn(
            tmplRecipes, tmplTags, tmplLootTables, tmplBlockStates, tmplModels
        )
        processRsrc.doFirst { processRsrc.exclude("templates") }
    }

    srcSet.resources.srcDir(genResourcesDir)
}

private fun checkDirs(dir: File): Int {
    val numGen = if (dir.resolve("generators").isDirectory) 1 else 0
    val numTmp = if (dir.resolve("templates").isDirectory) 2 else 0
    return numGen or numTmp
}

private inline fun <reified T: AbstractProcessTask> TaskContainer.make(name: String, sub: String, action: Action<in T>) =
    this.create(if (sub != "") "${name}_$sub" else name, T::class.java, action)