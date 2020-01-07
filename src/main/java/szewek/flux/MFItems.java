package szewek.flux;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.item.MetalItem;
import szewek.flux.item.MFToolItem;
import szewek.flux.item.GiftItem;
import szewek.flux.util.Metal;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static szewek.flux.MCFlux.MODID;

public final class MFItems {
	public static final ItemGroup MF_ITEMS = new ItemGroup("flux.items") {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(FLUXGEN);
		}
	};

	public static final Map<Metal, MetalItem>
			GRITS = metalMap("grit", Metal::all),
			DUSTS = metalMap("dust", Metal::all),
			INGOTS = metalMap("ingot", Metal::nonVanilla);

	public static final MFToolItem MFTOOL = create(MFToolItem::new, "mftool", new Item.Properties().maxStackSize(1));
	public static final GiftItem GIFT = create(GiftItem::new, "gift", new Item.Properties().maxStackSize(1));
	public static final Item MACHINE_BASE = create(Item::new, "machine_base", new Item.Properties());
	public static final BlockItem
			FLUXGEN = fromBlock(MFBlocks.FLUXGEN, "fluxgen"),
			GRINDING_MILL = fromBlock(MFBlocks.GRINDING_MILL, "grinding_mill"),
			ALLOY_CASTER = fromBlock(MFBlocks.ALLOY_CASTER, "alloy_caster"),
			WASHER = fromBlock(MFBlocks.WASHER, "washer"),
			COMPACTOR = fromBlock(MFBlocks.COMPACTOR, "compactor");

	static void register(final IForgeRegistry<Item> reg) {
		GRITS.values().forEach(reg::register);
		DUSTS.values().forEach(reg::register);
		INGOTS.values().forEach(reg::register);
		MFBlocks.ORES.forEach((name, b) -> reg.register(fromBlock(b, name.name+"_ore")));
		MFBlocks.METAL_BLOCKS.forEach((name, b) -> reg.register(fromBlock(b, name.name+"_block")));
		reg.registerAll(
				MFTOOL, GIFT, MACHINE_BASE,
				FLUXGEN, GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR
		);
	}

	private static <T extends Item> T create(Function<Item.Properties, T> factory, String name, Item.Properties props) {
		T item = factory.apply(props.group(MF_ITEMS));
		item.setRegistryName(MODID, name);
		return item;
	}

	private static Map<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
		Map<Metal, MetalItem> m = new EnumMap<>(Metal.class);
		for (Metal metal : Metal.values()) {
			if (filter.test(metal)) {
				m.put(metal, create(MetalItem::new, metal.name + '_' + type, new Item.Properties()).withMetal(metal));
			}
		}
		return m;
	}

	private static BlockItem fromBlock(Block b, String name) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(MF_ITEMS));
		item.setRegistryName(MODID, name);
		return item;
	}
}
