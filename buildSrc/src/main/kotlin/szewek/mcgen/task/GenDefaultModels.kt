package szewek.mcgen.task

import com.fasterxml.jackson.jr.ob.JSON
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File

open class GenDefaultModels : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/models/itemmodel_defaults.json")
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/models/item"

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val modelJson = file.reader().use {
            JSON.std.mapFrom(it)
        }
        for ((k, v) in modelJson.entries) {
            val outObj = mutableMapOf<String, Any>()
            outObj["parent"] = "$namespace:item/default_$k"
            val out = JSON.std.composeString().addObject(outObj).finish()
            for (n in v as List<*>) {
                val f = File(outputDir, "${n.toString()}_$k.json")
                f.writeText(out)
            }
        }
    }
}