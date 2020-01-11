package szewek.ktutils

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import kotlin.internal.InlineOnly

@InlineOnly
inline fun blockProps(mat: Material, f: Block.Properties.() -> Block.Properties) = Block.Properties.create(mat).f()