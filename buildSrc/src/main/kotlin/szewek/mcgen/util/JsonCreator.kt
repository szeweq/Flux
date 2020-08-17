package szewek.mcgen.util

import com.google.gson.JsonElement
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter

inline class JsonCreator(val jw: JsonWriter) {

    inline fun wrap(begin: () -> Unit, end: () -> Unit, fn: JsonFunc) {
        begin()
        fn()
        end()
    }
    inline fun named(name: String, fn: JsonFunc) {
        jw.name(name)
        fn()
    }

    inline fun obj(fn: JsonFunc) = wrap(jw::beginObject, jw::endObject, fn)
    inline fun arr(fn: JsonFunc) = wrap(jw::beginArray, jw::endArray, fn)

    inline infix fun String.obj(fn: JsonFunc) = named(this) { obj(fn) }
    inline infix fun String.arr(fn: JsonFunc) = named(this) { arr(fn) }

    infix fun String.set(v: String) = named(this) { jw.value(v) }
    infix fun String.set(v: Number) = named(this) { jw.value(v) }
    infix fun String.set(v: Boolean) = named(this) { jw.value(v) }
    infix fun String.set(v: JsonElement) = named(this) { Streams.write(v, jw) }
    infix fun String.set(v: Array<out String>) = named(this) { arr { for (s in v) jw.value(s) } }
}

typealias JsonFunc = JsonCreator.() -> Unit