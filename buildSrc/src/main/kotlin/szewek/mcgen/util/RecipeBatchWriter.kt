package szewek.mcgen.util

import com.fasterxml.jackson.jr.ob.JSON
import java.io.File
import java.io.IOException

class RecipeBatchWriter(
        private val namespace: String,
        private val outputDir: File
) {

    @Throws(IOException::class)
    fun save(name: String, item: String, v: MutableMap<String, Any>, i: Int) {
        val recipe = mutableMapOf<String, Any>()
        recipe["type"] = "$namespace:$name"
        val result = mutableMapOf<String, Any>("item" to item)
        if (v.containsKey("count")) {
            result["count"] = v["count"] as Any
            v.remove("count")
        }
        recipe["result"] = result
        for (e in v.entries) {
            recipe[e.key] = e.value
        }
        val subName = item.substring(item.indexOf(':') + 1) + '_' + name
        val checkedName = (if (i > 0) subName + '_' + i else subName) + ".json"
        val outputFile = File(outputDir, checkedName)
        JSON.std.composeTo(outputFile).addObject(recipe).finish().close()
//        val writer = FileWriter(outputFile)
//        val jsonWriter = JsonWriter(writer)
//        jsonWriter.isLenient = true
//        Streams.write(recipe, jsonWriter)
//        writer.close()
    }
}
