package szewek.mcgen.util

import com.fasterxml.jackson.jr.ob.JSON
import com.fasterxml.jackson.jr.ob.JSONComposer
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer
import java.io.File
import java.io.IOException
import java.io.OutputStream

class JsonFileWriter(private val dir: File, val namespace: String) {
    companion object {
        val json: JSON = JSON.builder().build()
    }

    @Throws(IOException::class)
    fun create(name: String): JSONComposer<OutputStream> {
        val file = File(dir, "$name.json")
        file.parentFile.mkdirs()
        return json.composeTo(file)
    }

    operator fun invoke(name: String, jf: WriteFunc) {
        val jc = create(name)
        val oc: ObjectComposer<*> = jc.startObject()
        jf(oc)
        oc.end()
        jc.finish().close()
    }

}