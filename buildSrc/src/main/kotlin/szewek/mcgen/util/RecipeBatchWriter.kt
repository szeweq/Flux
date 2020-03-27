package szewek.mcgen.util

import com.google.gson.JsonObject
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class RecipeBatchWriter(
        private val namespace: String,
        private val outputDir: File
) {

    @Throws(IOException::class)
    fun save(name: String, item: String, v: JsonObject, i: Int) {
        val recipe = JsonObject()
        recipe.addProperty("type", "$namespace:$name")
        val result = JsonObject()
        result.addProperty("item", item)
        if (v.has("count")) {
            val c = v.get("count").asInt
            v.remove("count")
            result.addProperty("count", c)
        }
        recipe.add("result", result)
        for (e in v.entrySet()) {
            recipe.add(e.key, e.value)
        }
        val subName = item.substring(item.indexOf(':') + 1) + "_from_" + name
        val checkedName = (if (i > 0) subName + '_' + i else subName) + ".json"
        val outputFile = File(outputDir, checkedName)
        val writer = FileWriter(outputFile)
        val jsonWriter = JsonWriter(writer)
        jsonWriter.isLenient = true
        jsonWriter.setIndent(" ")
        Streams.write(recipe, jsonWriter)
        writer.close()
    }
}
