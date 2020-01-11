package szewek.flux.util.gift

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import szewek.ktutils.of

object Gifts {
    private val GIFT_MAP: Int2ObjectMap<GiftData> = Int2ObjectOpenHashMap()
    private fun add(day: Int, month: Int, name: String, boxColor: Int, ribbonColor: Int, stacks: List<ItemStack>) {
        GIFT_MAP[month * 32 + day] = GiftData(name, day, month, boxColor, ribbonColor, stacks)
    }

    @JvmStatic
    operator fun get(xday: Int): GiftData? {
        return GIFT_MAP[xday]
    }

    @JvmStatic
    fun colorByGift(stack: ItemStack, pass: Int): Int {
        val tag = stack.tag ?: return 0x808080
        val xday = tag.getInt("xDay")
        val gd = GIFT_MAP[xday] ?: return 0x404040
        return if (pass == 0) gd.boxColor else gd.ribbonColor
    }

    init {
        add(1, 1, "newyear", 0x2020F0, 0xF0F020, listOf(
                1 of Items.NETHER_STAR,
                16 of Items.FIREWORK_ROCKET
        ))
    }
}