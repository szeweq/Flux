package szewek.mcgen.task

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import com.google.gson.JsonObject
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.io.FileWriter

open class ProcessLangFiles : AbstractProcessTask() {

    override fun doProcessTask(namespace: String, files: Set<File>, outputDir: File) {
        files.forEach {
            val outJson = JsonObject()
            val out = File(outputDir, it.nameWithoutExtension + ".json")
            val cfg = FileConfig.of(it)
            cfg.load()
            cfg.valueMap().forEach { (k, v) ->
                flatMap(k, v, outJson)
            }
            val writer = FileWriter(out)
            val jsonWriter = JsonWriter(writer)
            jsonWriter.isLenient = true
            jsonWriter.setIndent(" ")
            Streams.write(outJson, jsonWriter)
            writer.close()
        }
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/lang"

    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/lang/**.toml")
    }

    private fun flatMap(name: String, v: Any, out: JsonObject) {
        if (v is Config) {
            v.valueMap().forEach{ (k, v) ->
                flatMap("$name.$k", v, out)
            }
        } else out.addProperty(name, v.toString())
    }
}