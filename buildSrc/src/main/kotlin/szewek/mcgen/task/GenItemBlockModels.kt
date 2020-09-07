package szewek.mcgen.task

import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File

open class GenItemBlockModels : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/models/itemmodel_blocks.json")
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/models/item"

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val modelJson = file.readJson().asJsonArray
        modelJson.map { e -> scope.launch(Dispatchers.IO) {
            val v = e.asString
            val f = File(outputDir, "$v.json")
            f.writer().use {
                val jw = JsonWriter(it)
                jw.isLenient = true
                jw.beginObject()
                jw.name("parent").value("$namespace:block/$v")
                jw.endObject()
            }
        } }.joinAll()
    }
}