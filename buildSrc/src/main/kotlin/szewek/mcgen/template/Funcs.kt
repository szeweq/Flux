package szewek.mcgen.template

import com.fasterxml.jackson.jr.ob.comp.ArrayComposer
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer
import szewek.mcgen.util.*

internal infix fun Int.of(that: String) = Pair(that, this)

internal fun ObjectComposer<*>.keyResult(pair: Pair<String, Int>) = obj("result") { result(pair) }
internal fun ObjectComposer<*>.result(pair: Pair<String, Int>) {
    itemOrTag(pair.first)
    if (pair.second > 1) put("count", pair.second)
}
internal fun ObjectComposer<*>.keyItemOrTag(key: String, name: String) = obj(key) { itemOrTag(name) }
internal fun ObjectComposer<*>.itemOrTag(n: String) {
    if (n[0] == '#') {
        put("tag", n.substring(1))
    } else {
        put("item", n)
    }
}

internal inline fun ArrayComposer<*>.typed(t: String, crossinline fn: WriteFunc) = obj {
    put("type", t)
    fn()
}

internal fun ArrayComposer<*>.pool(rolls: Int = 1, entries: ArrComposable<*>, conditions: ArrComposable<*>? = null) = obj {
    put("rolls", rolls)
    arr("entries", entries)
    if (conditions != null) arr("conditions", conditions)
}


val condition_survivesExplosion: ArrComposable<*> = { singleObj("condition", "minecraft:survives_explosion") }