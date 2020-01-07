package szewek.mcgen.util

import com.google.gson.JsonElement
import com.google.gson.internal.Streams
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
        return JsonWriter(fw)
    }

    inline operator fun invoke(name: String, fn: JsonCreator.() -> Unit) {
        val jw = create(name)
        jw.isLenient = true
        JsonCreator(jw).fn()
        jw.close()
    }

}