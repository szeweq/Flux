@file:JvmName("ResourceFactories")
package szewek.mcgen.template

import szewek.mcgen.util.*

val blockShape = arrayOf("###", "###", "###")

internal fun fluxMachine(typ: String, result: Pair<String, Int>, ingredients: ArrComposable<*>): WriteFunc = {
    put("type", "flux:$typ")
    keyResult(result)
    arr("ingredients", ingredients)
}
internal fun fluxMachineTag(type: String, result: Pair<String, Int>, ingredient: String) =
    fluxMachine(type, result) { singleObj("tag", ingredient) }
internal fun fluxMachineItem(type: String, result: Pair<String, Int>, ingredient: String) =
    fluxMachine(type, result) { singleObj("item", ingredient) }

internal fun craftingCompress(from: String, into: String) =
    craftingShaped(blockShape, mapOf("#" to from), 1 of into)
internal fun craftingUncompress(group: String, from: String, into: String) =
    craftingShapeless(group, 9 of into) { singleObj("item", from) }

internal fun craftingShaped(pattern: Array<String>, keys: Map<String, String>, result: Pair<String, Int>): WriteFunc = {
    put("type", "minecraft:crafting_shaped")
    putStrings("pattern", pattern)
    obj("key") {
        for ((t, u) in keys) {
            keyItemOrTag(t, u)
        }
    }
    keyResult(result)
}

internal fun craftingShapeless(group: String, result: Pair<String, Int>, ingredients: ArrComposable<*>): WriteFunc = {
    put("type", "minecraft:crafting_shapeless")
    if (group != "") put("group", group)
    arr("ingredients", ingredients)
    keyResult(result)
}

internal fun smelting(group: String, ingredient: String, result: Pair<String, Int>): WriteFunc = {
    put("type", "minecraft:smelting")
    if (group != "") put("group", group)
    keyItemOrTag("ingredient", ingredient)
    keyResult(result)
}

internal fun singleTag(name: String) = tagList(arrayOf(name))
internal fun tagList(names: Array<out String>): WriteFunc = {
    put("replace", false)
    putStrings("values", names)
}

internal fun variants(fn: WriteFunc): WriteFunc = { obj("variants", fn) }
internal fun lazyItem(name: String): ArrComposable<*> = { singleObj("item", name) }

internal fun singleEntryItem(name: String): ArrComposable<*> = {
    obj {
        put("type", "minecraft:item")
        put("name", name)
    }
}

internal inline fun typedLoot(typ: String, crossinline fn: ArrComposable<*>): WriteFunc = {
    put("type", typ)
    arr("pools", fn)
}