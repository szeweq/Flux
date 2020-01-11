package szewek.ktutils

import net.minecraft.util.ResourceLocation
import kotlin.internal.InlineOnly

@InlineOnly
inline infix fun String.at(path: String) = ResourceLocation(this, path)