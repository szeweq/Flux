@file:JvmName("ResourceFactories")
package szewek.mcgen.template

import szewek.mcgen.util.JsonFunc

val blockShape = arrayOf("###", "###", "###")

internal fun fluxMachine(type: String, result: Pair<String, Int>, ingredients: JsonFunc): JsonFunc = {
    "type" set "flux:$type"
    keyResult(result)
    "ingredients" arr ingredients
}

internal fun craftingCompress(from: String, into: String) = craftingShaped(blockShape, mapOf("#" to from), 1 of into)
internal fun craftingUncompress(group: String, from: String, into: String) = craftingShapeless(group, 9 of into, lazyItem(from))

internal fun craftingShaped(pattern: Array<String>, keys: Map<String, String>, result: Pair<String, Int>): JsonFunc = {
    typed("minecraft:crafting_shaped")
    "pattern" set pattern
    "key" obj {
        for ((t, u) in keys) {
            keyItemOrTag(t, u)
        }
    }
    keyResult(result)
}

internal fun craftingShapeless(group: String, result: Pair<String, Int>, ingredients: JsonFunc): JsonFunc = {
    typed("minecraft:crafting_shapeless")
    if (group != "") "group" set group
    "ingredients" arr ingredients
    keyResult(result)
}

internal fun smelting(group: String, ingredient: String, result: Pair<String, Int>): JsonFunc = {
    typed("minecraft:smelting")
    if (group != "") "group" set group
    keyItemOrTag("ingredient", ingredient)
    keyResult(result)
}

internal fun singleTag(name: String) = tagList(arrayOf(name))
internal fun tagList(names: Array<out String>): JsonFunc = {
    "replace" set false
    "values" set names
}

internal fun variants(fn: JsonFunc): JsonFunc = { "variants" obj fn }
internal fun lazyTag(name: String): JsonFunc = { singleObj("tag", name) }
internal fun lazyItem(name: String): JsonFunc = { singleObj("item", name) }

internal fun singleEntryItem(name: String): JsonFunc = {
    obj {
        "type" set "minecraft:item"
        "name" set name
    }
}

internal inline fun typedRecipe(type: String, crossinline fn: JsonFunc): JsonFunc = {
    typed(type)
    fn()
}
internal inline fun typedLoot(type: String, crossinline fn: JsonFunc): JsonFunc = {
    typed(type)
    "pools" arr fn
}