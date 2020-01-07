package szewek.mcgen.task

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.io.FileReader
import java.io.FileWriter

open class GenItemBlockModels : AbstractProcessTask() {
    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/models/itemmodel_blocks.json")
    }

    override fun outputDirName(namespace: String) = "assets/$namespace/models/item"

    override fun doProcessTask(namespace: String, files: Set<File>, outputDir: File) {
        files.forEach {
            val modelJson = JsonParser.parseReader(FileReader(it)).asJsonArray
            val outObj = JsonObject()
            modelJson.forEach { e ->
                val v = e.asString
                outObj.addProperty("parent", "$namespace:block/$v")
                val file = File(outputDir, "$v.json")
                val fw = FileWriter(file)
                val jw = JsonWriter(fw)
                jw.isLenient = true
                Streams.write(outObj, jw)
                fw.close()
            }
        }
    }
}