package szewek.flux.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxAdhesiveItem extends Item {
	private static final ITextComponent WORKS_UNDERWATER = new TranslationTextComponent("flux.works_underwater");

	public FluxAdhesiveItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(WORKS_UNDERWATER);
	}
}
