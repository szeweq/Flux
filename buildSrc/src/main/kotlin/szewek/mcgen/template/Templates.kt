package szewek.mcgen.template

import com.google.gson.JsonElement
import szewek.mcgen.util.JsonCreator
import szewek.mcgen.util.JsonFileWriter

object Templates {
    private val nameMap = HashMap<String, TemplateFunc>()

    fun byName(name: String): TemplateFunc {
        return nameMap[name] ?: throw NotImplementedError("Template not implemented")
    }

    private fun add(name: String, fn: TemplateFunc) {
        nameMap[name] = fn
    }

    init {
        add("metalRecipes", ::metalRecipes)
        add("metalRecipesTagged", ::metalRecipesTagged)
        add("colorRecipes", ::colorRecipes)
        add("toolRecipes", ::toolRecipes)
        add("metalTags", ::metalTags)
        add("typeTags", ::typeTags)
        add("containerLootTables", ::containerLootTables)
        add("metalLootTables", ::metalLootTables)
        add("blockLootTables", ::blockLootTables)
        add("machineBlockStates", ::machineBlockStates)
        add("activeBlockStates", ::activeBlockStates)
        add("defaultBlockStates", ::defaultBlockStates)
    }

    private fun machineBlockStates(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        val dirs = arrayOf("north", "east", "south", "west")
        val lit = arrayOf("false", "true")
        out(item) {
            variants {
                for (i in lit.indices) for (j in dirs.indices) {
                    "facing=${dirs[j]},lit=${lit[i]}" obj {
                        "model" to (if (i == 0) "$ns:block/$item" else "$ns:block/${item}_on")
                        if (j > 0) "y" to (90*j)
                    }
                }
            }
        }
    }
    private fun activeBlockStates(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        out(item) {
            variants {
                "lit=false" obj { "model" to "$ns:block/$item" }
                "lit=true" obj { "model" to "$ns:block/${item}_on" }
            }
        }
    }
    private fun defaultBlockStates(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        out(item) {
            variants { "" obj { "model" to "$ns:block/$item" } }
        }
    }

    private fun metalRecipes(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        if (!isVanilla(item)) {
            out("${item}_block") {
                craftingShaped(arrayOf("###", "###", "###"), mapOf(Pair("#", "$ns:${item}_ingot")), "$ns:${item}_block")
            }
            out("${item}_ingot_from_${item}_block") {
                typed("minecraft:crafting_shapeless") {
                    "group" to "${item}_ingot"
                    ingredients {
                        item("$ns:${item}_block")
                    }
                    result("$ns:${item}_ingot", 9)
                }
            }
            if (!isAlloy(item)) out("${item}_ingot_smelting_ore") {
                typed("minecraft:smelting") {
                    "group" to "${item}_ingot"
                    key("ingredient").tag("forge:ores/${item}")
                    result("$ns:${item}_ingot")
                }
            }
        }
        if (!isAlloy(item)) {
            out("${item}_dust_grinding_ore") {
                typed("flux:grinding") {
                    ingredients {
                        tag("forge:ores/${item}")
                    }
                    result("$ns:${item}_dust", 2)
                }
            }
            out("${item}_grit_washing_ore") {
                typed("flux:washing") {
                    ingredients {
                        tag("forge:ores/${item}")
                    }
                    result("$ns:${item}_grit", 3)
                }
            }
            out("${item}_dust_grinding_grit") {
                typed("flux:grinding") {
                    ingredients {
                        tag("forge:grits/${item}")
                    }
                    result("$ns:${item}_dust")
                }
            }
        }
        out("${item}_dust_grinding_ingot") {
            typed("flux:grinding") {
                ingredients {
                    tag("forge:ingots/${item}")
                }
                result("$ns:${item}_dust")
            }
        }
        out("${item}_ingot_smelting_dust") {
            typed("minecraft:smelting") {
                "group" to "${item}_ingot"
                key("ingredient").tag("forge:dusts/${item}")
                result((if (isVanilla(item)) "minecraft" else ns) + ":${item}_ingot")
            }
        }
    }

    private fun metalRecipesTagged(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        out("${item}_dust_grinding_ore") {
            typed("flux:grinding") {
                ingredients {
                    tag("forge:ores/$item")
                }
                resultTag("forge:dusts/$item", 2)
            }
        }
        out("${item}_dust_grinding_grit") {
            typed("flux:grinding") {
                ingredients {
                    tag("forge:grits/$item")
                }
                resultTag("forge:dusts/$item")
            }
        }
        out("${item}_dust_grinding_ingot") {
            typed("flux:grinding") {
                ingredients {
                    tag("forge:ingots/$item")
                }
                resultTag("forge:dusts/$item")
            }
        }
        out("${item}_grit_washing_ore") {
            typed("flux:washing") {
                ingredients {
                    tag("forge:ores/$item")
                }
                resultTag("forge:grits/$item", 3)
            }
        }
    }

    private fun colorRecipes(v: JsonElement, out: JsonFileWriter) {
        val o = v.asJsonObject
        val from = o["from"].asString
        o.remove("from")
        val into = o["into"].asString
        o.remove("into")
        val type = o["type"].asString
        o.remove("type")
        val colors = arrayOf(
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        )
        for(col in colors) out("${col}_${into}_$type") {
            typed("${out.namespace}:$type") {
                ingredients { item("${col}_$from") }
                result("${col}_$into")
                for ((k, je) in o.entrySet()) k to je
            }
        }
    }

    private fun toolRecipes(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        val mapKeys = mapOf("X" to "$ns:${item}_ingot", "#" to "minecraft:stick")
        val mapShapes = mapOf(
                "sword" to arrayOf("X", "X", "#"),
                "shovel" to arrayOf("X", "#", "#"),
                "pickaxe" to arrayOf("XXX", " # ", " # "),
                "axe" to arrayOf("XX", "X#", " #"),
                "hoe" to arrayOf("XX", " #", " #")
        )
        for((name, pat) in mapShapes) out("${item}_$name") {
            craftingShaped(pat, mapKeys, "$ns:${item}_$name")
        }
    }

    private fun metalTags(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val ns = out.namespace
        if (!isVanilla(item)) {
            if (!isAlloy(item)) {
                out("items/ores/${item}") {
                    tagList("$ns:${item}_ore")
                }
                out("blocks/ores/${item}") {
                    tagList("$ns:${item}_ore")
                }
            }
            out("items/ingots/${item}") {
                tagList("$ns:${item}_ingot")
            }
            out("items/storage_blocks/${item}") {
                tagList("$ns:${item}_block")
            }
            out("blocks/storage_blocks/${item}") {
                tagList("$ns:${item}_block")
            }
        }
        out("items/dusts/${item}") {
            tagList("$ns:${item}_dust")
        }
        if (!isAlloy(item)) out("items/grits/${item}") {
            tagList("$ns:${item}_grit")
        }
    }

    private fun typeTags(v: JsonElement, out: JsonFileWriter) {
        val o = v.asJsonObject
        val tag = o["tag"].asString
        val blocks = o["blocks"].asBoolean
        val items = o["items"].asJsonArray

        out("items/$tag") {
            tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
        }
        if (blocks) out("blocks/$tag") {
            tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
        }
        when (tag) {
            "storage_blocks" -> {
                out("blocks/supports_beacon") {
                    tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
                }
                out("../../minecraft/tags/blocks/beacon_base_blocks") {
                    tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
                }
            }
            "ingots" -> {
                out("items/beacon_payment") {
                    tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
                }
                out("../../minecraft/tags/items/beacon_payment_items") {
                    tagList(*items.map{"#forge:$tag/${it.asString}"}.toTypedArray())
                }
            }
        }
    }

    private fun containerLootTables(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        out("blocks/$item") {
            typed("minecraft:block") {
                "pools" arr {
                    obj {
                        "rolls" to 1
                        "entries" arr {
                            typed("minecraft:item") {
                                "functions" arr {
                                    obj {
                                        "function" to "minecraft:copy_name"
                                        "source" to "block_entity"
                                    }
                                }
                                "name" to "${out.namespace}:${item}"
                            }
                        }
                        "conditions" arr {
                            obj { "condition" to "minecraft:survives_explosion" }
                        }
                    }
                }
            }
        }
    }

    private fun metalLootTables(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        val types = if (isAlloy(item)) arrayOf("block") else arrayOf("ore", "block")
        for (typ in types) out("blocks/${item}_$typ") {
            typed("minecraft:block") {
                "pools" arr {
                    obj {
                        "rolls" to 1
                        "entries" arr {
                            typed("minecraft:item") {
                                "name" to "${out.namespace}:${item}_$typ"
                            }
                        }
                        "conditions" arr {
                            obj { "condition" to "minecraft:survives_explosion" }
                        }
                    }
                }
            }
        }
    }

    private fun blockLootTables(v: JsonElement, out: JsonFileWriter) {
        val item = v.asString
        out("blocks/$item") {
            typed("minecraft:block") {
                "pools" arr {
                    obj {
                        "rolls" to 1
                        "entries" arr {
                            typed("minecraft:item") {
                                "name" to "${out.namespace}:$item"
                            }
                        }
                        "conditions" arr {
                            obj { "condition" to "minecraft:survives_explosion" }
                        }
                    }
                }
            }
        }
    }

    private fun isVanilla(name: String) = name == "iron" || name == "gold"
    private fun isAlloy(name: String) = name == "bronze" || name == "steel"

    private inline fun JsonCreator.typed(type: String, fn: JsonCreator.() -> Unit) = obj {
        "type" to type
        fn()
    }
    private inline fun JsonCreator.ingredients(fn: JsonCreator.() -> Unit) = "ingredients" arr fn
    private fun JsonCreator.result(item: String, count: Int = 1) = "result" obj {
        "item" to item
        if (count > 1) "count" to count
    }
    private fun JsonCreator.resultTag(tag: String, count: Int = 1) = "result" obj {
        "tag" to tag
        if (count > 1) "count" to count
    }
    private fun JsonCreator.item(name: String) = obj { "item" to name }
    private fun JsonCreator.tag(name: String) = obj { "tag" to name }
    private fun JsonCreator.tagList(vararg names: String) = obj {
        "replace" to false
        "values" arr {
            for(name in names) add(name)
        }
    }
    private inline fun JsonCreator.variants(fn: JsonCreator.() -> Unit) = obj {
        "variants" obj fn
    }
    private fun JsonCreator.craftingShaped(pattern: Array<String>, keys: Map<String, String>, result: String, count: Int = 1) =
            typed("minecraft:crafting_shaped") {
                "pattern" to pattern
                "key" obj {
                    for ((t, u) in keys) key(t).item(u)
                }
                result(result, count)
            }
}
