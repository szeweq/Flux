package szewek.mcgen.task

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import com.electronwill.nightconfig.toml.TomlFormat
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.jr.ob.JSON
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File

open class ProcessLangFiles : AbstractProcessTask() {
    companion object {
        val json: JSON = JSON.builder().prettyPrinter(DefaultPrettyPrinter()).build()
    }

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val out = File(outputDir, file.nameWithoutExtension + ".json")
        val cfg = FileConfig.of(file, TomlFormat.instance())
        cfg.load()
        out.writer().use {
            runCatching {
                val jp = json.composeTo(it)
                val oc = jp.startObject()
                for ((k, v) in cfg.valueMap()) {
                    flatMap(k, v, oc)
                }
                oc.end().finish()
            }
        }
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/lang"

    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/lang/**.toml")
    }

    private fun flatMap(name: String, c: Any, out: ObjectComposer<*>) {
        if (c is Config) {
            for ((k, v) in c.valueMap()) {
                flatMap("$name.$k", v, out)
            }
        } else out.put(name, c.toString())
    }
}