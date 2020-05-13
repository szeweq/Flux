package szewek.flux.util;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.*;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.fl.util.FluxItemTier;
import szewek.flux.Flux;
import szewek.flux.util.metals.Metal;

public class Toolset {
	public final FluxItemTier tier;
	public final SwordItem sword;
	public final ShovelItem shovel;
	public final PickaxeItem pickaxe;
	public final AxeItem axe;
	public final HoeItem hoe;

	public Toolset(FluxItemTier tier, String name) {
		this.tier = tier;
		sword = new SwordItem(tier, 3, -2.4f, new Item.Properties().group(ItemGroup.COMBAT));
		shovel = new ShovelItem(tier, 1.5f, -3.0f, new Item.Properties().group(ItemGroup.TOOLS));
		pickaxe = new PickaxeItem(tier, 1, -2.8f, new Item.Properties().group(ItemGroup.TOOLS));
		axe = new AxeItem(tier, 6, -3.1f, new Item.Properties().group(ItemGroup.TOOLS));
		hoe = new HoeItem(tier, Math.max(tier.getHarvestLevel()-4, 0), new Item.Properties().group(ItemGroup.TOOLS));

		sword.setRegistryName(Flux.MODID, name + "_sword");
		shovel.setRegistryName(Flux.MODID, name + "_shovel");
		pickaxe.setRegistryName(Flux.MODID, name + "_pickaxe");
		axe.setRegistryName(Flux.MODID, name + "_axe");
		hoe.setRegistryName(Flux.MODID, name + "_hoe");
	}

	public Item[] allTools() {
		return new Item[] {sword, shovel, pickaxe, axe, hoe};
	}

	public void registerTools(IForgeRegistry<Item> reg) {
		reg.registerAll(allTools());
	}

	public void registerToolColors(Metal metal, ItemColors ic) {
		final int mColor = metal.color;
		ic.register((stack, layer) -> layer == 0 ? mColor : 0xffffff, allTools());
	}
}
