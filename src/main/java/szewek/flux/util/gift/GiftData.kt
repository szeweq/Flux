package szewek.flux.util.gift

import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import java.util.function.Supplier
import javax.annotation.Nonnull

class GiftData(
        @JvmField val name: String,
        @JvmField val day: Int,
        @JvmField val month: Int,
        @JvmField val boxColor: Int,
        @JvmField val ribbonColor: Int,
        private val stacks: List<ItemStack>
) : IGift {
    override fun getStacks() = stacks.map(ItemStack::copy)

    override fun getText() = TranslationTextComponent("flux.gift.text.$name")

}