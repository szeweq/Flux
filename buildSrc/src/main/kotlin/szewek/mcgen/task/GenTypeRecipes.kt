package szewek.mcgen.task

import com.fasterxml.jackson.jr.ob.JSON
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.util.RecipeBatchWriter
import java.io.File
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

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    private fun recipesFromBatch(batch: File, rbw: RecipeBatchWriter) {
        val filename = batch.name
        val name = filename.substring(0, filename.length - 5)
        val batchJson = JSON.std.mapFrom(batch)
        for ((k, v) in batchJson.entries) {
            if (v is List<*>) {
                for (i in v.indices) {
                    rbw.save(name, k, v[i] as MutableMap<String, Any>, i)
                }
            } else {
                rbw.save(name, k, v as MutableMap<String, Any>, 0)
            }
        }
    }
}