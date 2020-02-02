package szewek.flux.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import szewek.flux.F;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class ChipUpgradeTrade implements VillagerTrades.ITrade {
	private final int min, max;

	public ChipUpgradeTrade(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	@Override
	public MerchantOffer getOffer(Entity trader, Random rand) {
		int energy = MathHelper.nextInt(rand, min, max);
		int speed = MathHelper.nextInt(rand, min, max);
		ItemStack stack = new ItemStack(F.I.CHIP);
		CompoundNBT tag = stack.getOrCreateTag();
		if (energy != 0) tag.putByte("energy", (byte) energy);
		if (speed != 0) tag.putByte("speed", (byte) speed);
		return new MerchantOffer(new ItemStack(Items.EMERALD, Math.max(1, (max-min)/2)), new ItemStack(F.I.CHIP), stack, 12, max-min, 0.2f);
	}
}
