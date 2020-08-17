package szewek.mcgen.template

import szewek.mcgen.util.JsonFunc

internal fun fluxMachine(type: String, result: Pair<String, Int>, ingredients: JsonFunc): JsonFunc = {
    "type" set "flux:$type"
    keyResult(result)
    "ingredients" arr ingredients
}

internal fun craftingShaped(pattern: Array<String>, keys: Map<String, String>, result: Pair<String, Int>): JsonFunc = {
    "type" set "minecraft:crafting_shaped"
    "pattern" set pattern
    "key" obj {
        for ((t, u) in keys) {
            keyItemOrTag(t, u)
        }
    }
    keyResult(result)
}

internal fun craftingShapeless(group: String, result: Pair<String, Int>, ingredients: JsonFunc): JsonFunc = {
    "type" set "minecraft:crafting_shapeless"
    if (group != "") "group" set group
    "ingredients" arr ingredients
    keyResult(result)
}

internal fun smelting(group: String, ingredient: String, result: Pair<String, Int>): JsonFunc = {
    "type" set "minecraft:smelting"
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

internal fun typedRecipe(type: String, fn: JsonFunc): JsonFunc = fn after { "type" set type }
internal fun typedLoot(type: String, fn: JsonFunc): JsonFunc = {
    "type" set type
    "pools" arr fn
}