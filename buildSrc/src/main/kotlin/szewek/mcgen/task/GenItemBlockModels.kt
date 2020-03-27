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

    override fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val reader = FileReader(file)
        val modelJson = JsonParser.parseReader(reader).asJsonArray
        val outObj = JsonObject()
        for (e in modelJson) {
            val v = e.asString
            outObj.addProperty("parent", "$namespace:block/$v")
            val f = File(outputDir, "$v.json")
            val fw = FileWriter(f)
            val jw = JsonWriter(fw)
            jw.isLenient = true
            Streams.write(outObj, jw)
            fw.close()
        }
        reader.close()
    }
}