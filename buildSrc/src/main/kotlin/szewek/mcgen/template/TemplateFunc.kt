package szewek.mcgen.template

import com.google.gson.JsonElement
import szewek.mcgen.util.JsonFileWriter

fun interface TemplateFunc {
    fun process(o: JsonElement, out: JsonFileWriter)
}