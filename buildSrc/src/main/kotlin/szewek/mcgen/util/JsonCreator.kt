package szewek.mcgen.util

import com.google.gson.JsonElement
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter

class JsonCreator(@JvmField val jw: JsonWriter) {
    fun named(name: String, fn: JsonFunc?) {
        if (fn != null) {
            jw.name(name)
            fn()
        }
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

    inline infix fun String.obj(crossinline fn: JsonFunc) = named(this) { obj(fn) }
    inline infix fun String.arr(crossinline fn: JsonFunc) = named(this) { arr(fn) }

    infix fun String.set(v: String) = named(this) { jw.value(v) }
    infix fun String.set(v: Number) = named(this) { jw.value(v) }
    infix fun String.set(v: Boolean) = named(this) { jw.value(v) }
    infix fun String.set(v: JsonElement) = named(this) { Streams.write(v, jw) }
    infix fun String.set(v: Array<out String>) = named(this) { arr { for (s in v) jw.value(s) } }
}

typealias JsonFunc = JsonCreator.() -> Unit