package szewek.ktutils

import net.minecraft.item.ItemStack
import net.minecraft.util.IItemProvider
import kotlin.internal.InlineOnly

@InlineOnly
inline val IItemProvider.asStack get() = ItemStack(this)

@InlineOnly
inline infix fun Int.of(ip: IItemProvider) = ItemStack(ip, this)

@InlineOnly
inline operator fun ItemStack.plusAssign(v: Int) = grow(v)

@InlineOnly
inline operator fun ItemStack.minusAssign(v: Int) = grow(-v)