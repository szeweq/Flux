package szewek.mcgen.task

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.util.logNanoTime
import java.io.File
import java.io.IOException
import java.io.Reader

abstract class AbstractProcessTask : DefaultTask(), FileVisitor {
    @Internal
    protected val scope = CoroutineScope(Dispatchers.IO)

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
        val namespace = fvd.relativePath.segments[1] // fvd.relativePath.split("/", limit = 3)[1]
        println("NS $namespace")
        val outputDir = File(genResourcesDir, outputDirName(namespace))
        if (!outputDir.isDirectory && !outputDir.mkdirs())
            throw IOException("Could not create a directory: $outputDir")
        runBlocking(scope.coroutineContext) {
            doProcessFile(namespace, fvd.file, outputDir)
        }
    }

    abstract fun filter(pf: PatternFilterable)
    abstract fun outputDirName(namespace: String): String
    abstract suspend fun doProcessFile(namespace: String, file: File, outputDir: File)

    protected fun File.readJson(): JsonElement = reader().use { JsonParser.parseReader(it) }
}