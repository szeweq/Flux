package szewek.mcgen.task

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.template.Templates
import szewek.mcgen.util.JsonFileWriter
import java.io.File
import java.io.Reader

abstract class AbstractTemplateTask(private val type: String) : AbstractProcessTask() {

    override fun filter(pf: PatternFilterable) {
        pf.include("templates/*/$type/*.json")
    }

    override fun outputDirName(namespace: String) = "data/$namespace/$type"

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val fd = file.reader().use { GSON.fromJson<FileData>(it) }
        val tmpl = Templates.byName(fd.name)
        val jfw = JsonFileWriter(outputDir, namespace)

        fd.args.map { scope.launch(Dispatchers.IO) { tmpl.process(it, jfw) } }.joinAll()
    }

    class FileData {
        lateinit var name: String
        lateinit var args: JsonArray
    }

    companion object {
        val GSON: Gson = GsonBuilder().setLenient().create()

        inline fun <reified T> Gson.fromJson(r: Reader): T = this.fromJson(r, T::class.java)
    }
}