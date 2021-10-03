package szewek.mcgen.task

import com.fasterxml.jackson.jr.ob.JSON
import com.fasterxml.jackson.jr.stree.JrsArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.gradle.api.tasks.util.PatternFilterable
import szewek.mcgen.template.TemplateFunc
import szewek.mcgen.util.JsonFileWriter
import java.io.File

abstract class AbstractTemplateTask(private val type: String) : AbstractProcessTask() {

    override fun filter(pf: PatternFilterable) {
        pf.include("templates/*/$type/*.json")
    }

    override fun outputDirName(namespace: String) = "data/$namespace/$type"

    override suspend fun doProcessFile(namespace: String, file: File, outputDir: File) {
        val fd = file.reader().use { json.beanFrom(FileData::class.java, it) }
        val tmpl = TemplateFunc.byName(fd.name)
        val jfw = JsonFileWriter(outputDir, namespace)

        fd.args.map { scope.launch(Dispatchers.IO) { tmpl.process(it, jfw) } }.joinAll()
    }

    class FileData {
        lateinit var name: String
        lateinit var args: List<Any>
    }
}