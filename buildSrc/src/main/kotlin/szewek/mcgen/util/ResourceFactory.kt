package szewek.mcgen.util

fun interface ResourceFactory {
    fun create(jc: JsonCreator)
}