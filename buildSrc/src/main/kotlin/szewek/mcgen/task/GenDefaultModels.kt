package szewek.mcgen.task

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.io.FileReader
import java.io.FileWriter

open class GenDefaultModels : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/models/itemmodel_defaults.json")
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/models/item"

    override fun doProcessTask(namespace: String, files: Set<File>, outputDir: File) {
        files.forEach {
            val modelJson = JsonParser.parseReader(FileReader(it)).asJsonObject
            modelJson.entrySet().forEach { (k, v) ->
                val outObj = JsonObject()
                outObj.addProperty("parent", "$namespace:item/default_$k")
                val out = outObj.toString()
                v.asJsonArray.forEach { n ->
                    val file = File(outputDir, "${n.asString}_$k.json")
                    val fw = FileWriter(file)
                    fw.write(out)
                    fw.close()
                }
            }
        }
    }

}