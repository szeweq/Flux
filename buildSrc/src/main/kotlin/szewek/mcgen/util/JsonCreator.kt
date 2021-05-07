package szewek.mcgen.util

import com.google.gson.JsonElement
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter

class JsonCreator(@JvmField val jw: JsonWriter) {
    inline fun named(name: String, fn: JsonFunc) {
        jw.name(name)
        fn()
    }

    inline fun obj(fn: JsonFunc) {
        jw.beginObject()
        fn()
        jw.endObject()
    }
    inline fun arr(fn: JsonFunc) {
        jw.beginArray()
        fn()
        jw.endArray()
    }

    inline infix fun String.obj(fn: JsonFunc) {
        jw.name(this)
        this@JsonCreator.obj(fn)
    }
    inline infix fun String.arr(fn: JsonFunc) {
        jw.name(this)
        this@JsonCreator.arr(fn)
    }

    infix fun String.set(v: String) { jw.name(this).value(v) }
    infix fun String.set(v: Number) { jw.name(this).value(v) }
    infix fun String.set(v: Boolean) { jw.name(this).value(v) }
    infix fun String.set(v: JsonElement) { Streams.write(v, jw.name(this)) }
    infix fun String.set(v: Array<out String>) = named(this) { arr { for (s in v) jw.value(s) } }
}

typealias JsonFunc = JsonCreator.() -> Unit