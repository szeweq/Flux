package szewek.mcgen.util

import com.google.gson.JsonElement
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter

inline class JsonCreator(val jw: JsonWriter) {
    operator fun invoke(k: String): JsonCreator {
        jw.name(k)
        return this
    }
    inline fun obj(fn: JsonCreator.() -> Unit) {
        jw.beginObject()
        fn()
        jw.endObject()
    }
    inline fun arr(fn: JsonCreator.() -> Unit) {
        jw.beginArray()
        fn()
        jw.endArray()
    }
    fun key(s: String): JsonCreator {
        jw.name(s)
        return this
    }
    fun add(s: String): JsonCreator {
        jw.value(s)
        return this
    }

    inline infix fun String.obj(fn: JsonCreator.() -> Unit) {
        jw.name(this)
        this@JsonCreator.obj(fn)
    }
    inline infix fun String.arr(fn: JsonCreator.() -> Unit) {
        jw.name(this)
        this@JsonCreator.arr(fn)
    }

    infix fun String.to(v: String) {
        jw.name(this).value(v)
    }
    infix fun String.to(v: Number) {
        jw.name(this).value(v)
    }
    infix fun String.to(v: Boolean) {
        jw.name(this).value(v)
    }
    infix fun String.to(v: JsonElement) {
        jw.name(this)
        Streams.write(v, jw)
    }
}