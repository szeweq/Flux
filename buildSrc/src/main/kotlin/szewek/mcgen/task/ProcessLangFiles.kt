package szewek.mcgen.task

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import com.electronwill.nightconfig.toml.TomlFormat
import com.google.gson.stream.JsonWriter
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.io.FileWriter

open class ProcessLangFiles : AbstractProcessTask() {

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val out = File(outputDir, file.nameWithoutExtension + ".json")
        val cfg = FileConfig.of(file, TomlFormat.instance())
        cfg.load()
        out.writer().use {
            val jsonWriter = JsonWriter(it)
            jsonWriter.isLenient = true
            jsonWriter.setIndent(" ")
            runCatching {
                jsonWriter.beginObject()
                for ((k, v) in cfg.valueMap()) {
                    flatMap(k, v, jsonWriter)
                }
                jsonWriter.endObject()
            }
        }
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/lang"

    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/lang/**.toml")
    }

    private fun flatMap(name: String, c: Any, out: JsonWriter) {
        if (c is Config) {
            for ((k, v) in c.valueMap()) {
                flatMap("$name.$k", v, out)
            }
        } else out.name(name).value(c.toString())
    }
}