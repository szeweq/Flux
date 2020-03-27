package szewek.mcgen.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.util.logNanoTime
import java.io.File
import java.io.IOException

abstract class AbstractProcessTask : DefaultTask(), FileVisitor {
    lateinit var files: FileTree
        @InputFiles @SkipWhenEmpty get

    lateinit var genResourcesDir: File
        @OutputDirectory get

    fun configureSources(srcs: SourceDirectorySet, name: String) {
        files = srcs.matching { filter(it) }
        genResourcesDir = File(project.buildDir, "genResources/$name")
    }

    @TaskAction
    @Throws(IOException::class)
    fun processFiles() = logNanoTime(name) {
        files.visit(this)
    }

    override fun visitDir(dirDetails: FileVisitDetails?) {}

    override fun visitFile(fvd: FileVisitDetails) {
        val namespace = fvd.relativePath.split("/", limit = 3)[1]
        val outputDir = File(genResourcesDir, outputDirName(namespace))
        if (!outputDir.isDirectory && !outputDir.mkdirs())
            throw IOException("Could not create a directory: $outputDir")
        doProcessFile(namespace, fvd.file, outputDir)
    }

    abstract fun filter(pf: PatternFilterable)
    abstract fun outputDirName(namespace: String): String
    abstract fun doProcessFile(namespace: String, file: File, outputDir: File)
}