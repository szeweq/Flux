package szewek.mcgen.task

import com.fasterxml.jackson.jr.stree.JrsArray
import com.fasterxml.jackson.jr.stree.JrsString
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
        val modelJson = file.readJson() as JrsArray
        modelJson.elements().asSequence().map { e -> scope.launch(Dispatchers.IO) {
            val v = (e as JrsString).value
            val f = File(outputDir, "$v.json")
            f.writer().use {
                json.composeTo(it).startObject()
                    .put("parent", "$namespace:block/$v")
                    .end().finish()
            }
        } }.toList().joinAll()
    }
}