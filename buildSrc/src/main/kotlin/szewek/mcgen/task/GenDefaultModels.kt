package szewek.mcgen.task

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File

open class GenDefaultModels : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/models/itemmodel_defaults.json")
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/models/item"

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val modelJson = file.reader().use {
            JsonParser.parseReader(it).asJsonObject
        }
        for ((k, v) in modelJson.entrySet()) {
            val outObj = JsonObject()
            outObj.addProperty("parent", "$namespace:item/default_$k")
            val out = outObj.toString()
            for (n in v.asJsonArray) {
                val f = File(outputDir, "${n.asString}_$k.json")
                f.writeText(out)
            }
        }
    }
}