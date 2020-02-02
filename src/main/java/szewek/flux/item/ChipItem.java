package szewek.flux.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ChipItem extends Item {
	public ChipItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		final CompoundNBT tag = stack.getTag();
		if (tag != null) {
			addTooltip(tag, tooltip, "speed", false);
			addTooltip(tag, tooltip, "energy", true);
		}
	}

	public int countValue(final CompoundNBT tag, String name, int v) {
		int x = MathHelper.clamp(tag.getByte(name) * 10, -90, 1000);
		return v * (100 + x) / 100;
	}

	private void addTooltip(final CompoundNBT tag, List<ITextComponent> tooltip, String name, boolean reversed) {
		final int v = MathHelper.clamp(tag.getByte(name) * 10, -90, 1000);
		if (v != 0) {
			String s = (v > 0 ? "+" : "") + v + " %";
			boolean positive = reversed ? v < 0 : v > 0;
			tooltip.add(
					new TranslationTextComponent("flux.chip." + name, s)
							.applyTextStyle(TextFormatting.GRAY)
							.appendText(": ")
							.appendSibling(new StringTextComponent(s)
							.applyTextStyle(positive ? TextFormatting.GREEN: TextFormatting.RED)
					)
			);
		}
	}
}
