package szewek.mcgen.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.io.IOException

abstract class AbstractProcessTask : DefaultTask() {
    lateinit var files: FileTree
        @InputFiles get

    lateinit var genResourcesDir: File
        @OutputDirectory get

    fun configureSources(srcs: SourceDirectorySet, name: String) {
        files = srcs.matching(this::filter)
        genResourcesDir = File(project.buildDir, "genResources/$name")
    }

    @TaskAction
    @Throws(IOException::class)
    fun process() {
        val nsMap = HashMap<String, MutableSet<File>>()
        files.forEach {
            val ns = it.parentFile.parentFile.name
            val fs = nsMap[ns] ?: HashSet()
            fs.add(it)
            nsMap[ns] = fs
        }
        nsMap.forEach { (k, v) ->
            val outputDir = File(genResourcesDir, outputDirName(k))
            if (!outputDir.isDirectory && !outputDir.mkdirs())
                throw IOException("Could not create a directory: $outputDir")
            doProcessTask(k, v, outputDir)
        }
    }

    abstract fun filter(pf: PatternFilterable)
    abstract fun outputDirName(namespace: String): String
    abstract fun doProcessTask(namespace: String, files: Set<File>, outputDir: File)
}