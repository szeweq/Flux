package szewek.mcgen.util

import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class JsonFileWriter(private val dir: File, val namespace: String) {

    @Throws(IOException::class)
    fun create(name: String): JsonWriter {
        val file = File(dir, "$name.json")
        file.parentFile.mkdirs()
        val fw = FileWriter(file)
        val jw = JsonWriter(fw)
        jw.isLenient = true
        return jw
    }

    operator fun invoke(name: String, rf: ResourceFactory) {
        val jw = create(name)
        jw.beginObject()
        rf.create(JsonCreator(jw))
        jw.endObject()
        jw.close()
    }

}