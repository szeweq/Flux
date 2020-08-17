package szewek.mcgen.template

import szewek.mcgen.util.JsonCreator
import szewek.mcgen.util.JsonFunc

internal fun JsonCreator.item(name: String) = obj { "item" set name }
internal fun JsonCreator.tag(name: String) = obj { "tag" set name }
internal inline fun JsonCreator.ingredients(fn: JsonFunc) = "ingredients" arr fn
internal inline fun JsonCreator.typed(type: String, fn: JsonFunc) = obj(fn after { "type" set type })
internal fun JsonCreator.keyResult(name: String, count: Int = 1) = "result" obj { result(name, count) }
internal fun JsonCreator.result(name: String, count: Int = 1) {
    itemOrTag(name)
    if (count > 1) "count" set count
}
internal fun JsonCreator.keyItemOrTag(key: String, name: String) = key obj { itemOrTag(name) }
internal fun JsonCreator.itemOrTag(name: String) {
    if (name[0] == '#') {
        "tag" set name.substring(1)
    } else {
        "item" set name
    }
}

internal inline fun JsonCreator.pool(rolls: Int = 1, fn: JsonFunc) = obj(fn after {"rolls" set rolls})

inline infix fun JsonFunc.then(crossinline fn: JsonFunc): JsonFunc = {
    this@then()
    fn()
}
inline infix fun JsonFunc.after(fn: JsonFunc): JsonFunc = fn then this