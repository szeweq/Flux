package szewek.mcgen.template

import com.google.gson.JsonElement
import szewek.mcgen.util.JsonFileWriter

typealias TemplateFunc = (o: JsonElement, out: JsonFileWriter) -> Unit