package szewek.mcgen.task

import com.google.gson.JsonParser
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.util.RecipeBatchWriter
import java.io.File
import java.io.FileReader
import java.io.IOException

open class GenTypeRecipes : AbstractProcessTask() {

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val rbw = RecipeBatchWriter(namespace, outputDir)
        runCatching { recipesFromBatch(file, rbw) }
    }

    override fun outputDirName(namespace: String) = "data/$namespace/recipes"

    override fun filter(pf: PatternFilterable) {
        pf.include("generators/*/recipetypes/**.json")
    }

    @Throws(IOException::class)
    private fun recipesFromBatch(batch: File, rbw: RecipeBatchWriter) {
        val filename = batch.name
        val name = filename.substring(0, filename.length - 5)
        val batchJson = JsonParser.parseReader(FileReader(batch)).asJsonObject
        for (e in batchJson.entrySet()) {
            val k = e.key
            val v = e.value
            if (v.isJsonArray) {
                val a = v.asJsonArray
                for (i in 0 until a.size()) {
                    rbw.save(name, k, a[i].asJsonObject, i)
                }
            } else {
                rbw.save(name, k, v.asJsonObject, 0)
            }
        }
    }
}