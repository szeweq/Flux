package szewek.mcgen.task

import com.google.gson.JsonParser
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.template.Templates
import szewek.mcgen.util.JsonFileWriter
import java.io.File
import java.io.FileReader

abstract class AbstractTemplateTask(private val type: String) : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("templates/*/$type/*.json")
    }

    override fun outputDirName(namespace: String) = "data/$namespace/$type"

    override fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val fr = FileReader(file)
        val obj = JsonParser.parseReader(fr).asJsonObject
        fr.close()
        val tname = obj["name"].asString
        val arg = obj["args"].asJsonArray
        val tmpl = Templates.byName(tname)
        val jfw = JsonFileWriter(outputDir, namespace)
        for (it in arg) {
            tmpl(it, jfw)
        }
    }
}