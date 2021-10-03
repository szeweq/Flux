@file:JvmName("Templates")
@file:Suppress("unused")
package szewek.mcgen.template

import szewek.mcgen.util.*

private val colors = arrayOf(
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
)
private val dirs = arrayOf("north", "east", "south", "west")
private val lit = arrayOf("false", "true")
private val mapShapes = mapOf(
    "sword" to arrayOf("X", "X", "#"),
    "shovel" to arrayOf("X", "#", "#"),
    "pickaxe" to arrayOf("XXX", " # ", " # "),
    "axe" to arrayOf("XX", "X#", " #"),
    "hoe" to arrayOf("XX", " #", " #")
)

private fun machineBlockStates(v: Any, out: JsonFileWriter) {
    val item = v as String
    val ns = out.namespace
    out(item, variants {
        for (i in lit.indices) for (j in dirs.indices) {
            obj("facing=${dirs[j]},lit=${lit[i]}") {
                put("model", if (i == 0) "$ns:block/$item" else "$ns:block/${item}_on")
                if (j > 0) put("y", 90*j)
            }
        }
    })
}
private fun activeBlockStates(v: Any, out: JsonFileWriter) {
    val item = v as String
    val ns = out.namespace
    out(item, variants {
        singleObj("lit=false", "model", "$ns:block/$item")
        singleObj("lit=true", "model", "$ns:block/${item}_on")
    })
}
private fun defaultBlockStates(v: Any, out: JsonFileWriter) {
    val item = v as String
    val ns = out.namespace
    out(item, variants { singleObj("", "model", "$ns:block/$item") })
}

private fun metalRecipes(v: Any, out: JsonFileWriter) {
    val item = v as String
    val ns = out.namespace
    val itemIngot = "$ns:${item}_ingot"
    val itemNugget = "$ns:${item}_nugget"
    val itemBlock = "$ns:${item}_block"
    if (!isVanilla(item)) {
        out("${item}_block", craftingCompress(itemIngot, itemBlock))
        out("${item}_ingot", craftingCompress(itemNugget, itemIngot))
        out("${item}_nugget", craftingUncompress("${item}_nugget", itemIngot, itemNugget))
        out("${item}_ingot_from_block", craftingUncompress("${item}_ingot", itemBlock, itemIngot))
        if (!isAlloy(item)) out("${item}_ingot_smelting_ore", smelting(
                "${item}_ingot",
                "#forge:ores/${item}",
                1 of itemIngot
        ))
    }
    if (!isAlloy(item)) {
        val oreLazyTag = "forge:ores/${item}"
        out("${item}_dust_grinding_ore",
                fluxMachineTag("grinding", 2 of "$ns:${item}_dust", oreLazyTag)
        )
        out("${item}_grit_washing_ore",
                fluxMachineTag("washing", 3 of "$ns:${item}_grit", oreLazyTag)
        )
        out("${item}_dust_grinding_grit",
                fluxMachineTag("grinding", 1 of "$ns:${item}_dust", "forge:grits/${item}")
        )
    }
    out("${item}_dust_grinding_ingot",
            fluxMachineTag("grinding", 1 of "$ns:${item}_dust", "forge:ingots/${item}")
    )
    out("${item}_ingot_smelting_dust", smelting(
            "${item}_ingot",
            "#forge:dusts/${item}",
            1 of (if (isVanilla(item)) "minecraft" else ns) + ":${item}_ingot"
    ))
    if (item != "netherite") out("${item}_gear", craftingShaped(
            arrayOf("X#X", "# #", "X#X"),
            mapOf("#" to "#forge:ingots/$item", "X" to "#forge:nuggets/$item"),
            2 of "$ns:${item}_gear"
    ))
    out("${item}_plate", craftingShaped(
            arrayOf("#", "#"),
            mapOf("#" to "#forge:ingots/$item"),
            1 of "$ns:${item}_plate"
    ))
    out("${item}_plate_compacting_ingot",
            fluxMachineTag("compacting", 1 of "$ns:${item}_plate", "forge:ingots/${item}")
    )
}

private fun metalRecipesTagged(v: Any, out: JsonFileWriter) {
    val item = v as String
    val dustsTag = "#forge:dusts/$item"
    out("${item}_dust_grinding_ore",
            fluxMachineTag("grinding", 2 of dustsTag, "forge:ores/$item")
    )
    out("${item}_dust_grinding_grit",
            fluxMachineTag("grinding", 1 of dustsTag, "forge:grits/$item")
    )
    out("${item}_dust_grinding_ingot",
            fluxMachineTag("grinding", 1 of dustsTag, "forge:ingots/$item")
    )
    out("${item}_grit_washing_ore",
            fluxMachineTag("washing", 3 of "#forge:grits/$item", "forge:ores/$item")
    )
}

private fun colorRecipes(v: Any, out: JsonFileWriter) {
    val o = v as MutableMap<*, *>
    val from = o["from"] as String
    val into = o["into"] as String
    val typ = o["type"] as String
    o.remove("from")
    o.remove("into")
    o.remove("type")
    for(col in colors) out("${col}_${into}_$typ") {
        put("type", "${out.namespace}:$typ")
        arr("ingredients", lazyItem("${col}_$from"))
        keyResult(1 of "${col}_$into")
        for ((k, jv) in o) putObject(k as String, jv) //setElement(k, je)
    }
}
private fun colorCopyingRecipes(v: Any, out: JsonFileWriter) {
    val o = v as MutableMap<*, *>
    val from = o["from"] as String
    val into = o["into"] as String
    o.remove("from")
    o.remove("into")
    for(col in colors) out("copying_${col}_$into") {
        put("type", "flux:copying")
        keyItemOrTag("source", "${col}_$into")
        keyItemOrTag("material", "${col}_$from")
        
        for ((k, jv) in o) putObject(k as String, jv) //setElement(k, je)
    }
}

private fun toolRecipes(v: Any, out: JsonFileWriter) {
    val item = v as String
    val ns = out.namespace
    val mapKeys = mapOf("X" to "$ns:${item}_ingot", "#" to "minecraft:stick")
    for((name, pat) in mapShapes) out("${item}_$name", craftingShaped(pat, mapKeys,1 of "$ns:${item}_$name"))
}

private fun metalTags(v: Any, out: JsonFileWriter) {
    val item = v as String
    val nsItem = "${out.namespace}:$item"
    if (!isVanilla(item)) {
        if (!isAlloy(item)) {
            val ore = "${nsItem}_ore"
            out.itemBlockTags("ores/$item", singleTag(ore))
        }
        val block = "${nsItem}_block"
        out("items/ingots/$item", singleTag("${nsItem}_ingot"))
        out("items/nuggets/$item", singleTag("${nsItem}_nugget"))
        out.itemBlockTags("storage_blocks/$item", singleTag(block))
    }
    out("items/dusts/$item", singleTag("${nsItem}_dust"))
    if (!isAlloy(item)) out("items/grits/$item", singleTag("${nsItem}_grit"))
    out("items/gears/$item", singleTag("${nsItem}_gear"))
    out("items/plates/$item", singleTag("${nsItem}_plate"))
}

private fun typeTags(v: Any, out: JsonFileWriter) {
    val o = v as Map<*, *>
    val tag = o["tag"] as String
    val blocks = o["blocks"] as Boolean
    val items = (o["items"] as List<*>).map{ "#forge:$tag/${it as String}" }
        .toTypedArray()
    val tagListFunc = tagList(items)

    out("items/$tag", tagListFunc)
    if (blocks) out("blocks/$tag", tagListFunc)
    when (tag) {
        "storage_blocks" -> {
            out("blocks/supports_beacon", tagListFunc)
            out("../../minecraft/tags/blocks/beacon_base_blocks", tagListFunc)
        }
        "ingots" -> {
            out("items/beacon_payment", tagListFunc)
            out("../../minecraft/tags/items/beacon_payment_items", tagListFunc)
        }
    }
}

private fun containerLootTables(v: Any, out: JsonFileWriter) {
    val item = v as String
    out("blocks/$item", typedLoot("minecraft:block") {
        pool(
                entries = {
                    typed("minecraft:item") {
                        arr("functions") {
                            obj {
                                put("function", "minecraft:copy_name")
                                put("source", "block_entity")
                            }
                        }
                        put("name", "${out.namespace}:${item}")
                    }
                },
                conditions = condition_survivesExplosion
        )
    })
}

private fun metalLootTables(v: Any, out: JsonFileWriter) {
    val item = v as String
    val types = if (isAlloy(item)) arrayOf("block") else arrayOf("ore", "block")
    for (typ in types) out("blocks/${item}_$typ", typedLoot("minecraft:block") {
        pool(
                entries = singleEntryItem("${out.namespace}:${item}_$typ"),
                conditions = condition_survivesExplosion
        )
    })
}

private fun blockLootTables(v: Any, out: JsonFileWriter) {
    val item = v as String
    out("blocks/$item", typedLoot("minecraft:block") {
        pool(
                entries = singleEntryItem("${out.namespace}:$item"),
                conditions = condition_survivesExplosion
        )
    })
}

private fun fluxGifts(v: Any, out: JsonFileWriter) {
    val o = (v as Map<*, *>)
    val n = o["name"] as String
    val table = o["table"] as String
    val box = (o["box"] as String).toInt(16)
    val ribbon = (o["ribbon"] as String).toInt(16)
    out("gifts/$n", typedLoot("minecraft:gift") {
        pool(
            entries = {
                typed("minecraft:item") {
                    put("name", "flux:gift")
                    arr("functions") {
                        obj {
                            put("function", "minecraft:set_nbt")
                            put("tag", "{\"Box\":${box},\"Ribbon\":${ribbon},\"LootTable\":\"${table}\"}")
                        }
                    }
                }
            }
        )
    })
}

private fun JsonFileWriter.itemBlockTags(name: String, fn: WriteFunc) {
    invoke("items/$name", fn)
    invoke("blocks/$name", fn)
    
}

private fun isVanilla(name: String) = name == "iron" || name == "gold" || name == "netherite" // name == "copper"
private fun isAlloy(name: String) = name == "bronze" || name == "steel" || name == "netherite"
