package szewek.flux;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.block.*;
import szewek.flux.container.*;
import szewek.flux.gui.FluxGenScreen;
import szewek.flux.gui.MachineScreen;
import szewek.flux.item.FluxToolItem;
import szewek.flux.item.GiftItem;
import szewek.flux.item.MetalItem;
import szewek.flux.recipe.*;
import szewek.flux.tile.*;
import szewek.flux.util.Metal;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static szewek.flux.FluxMod.FLUX_GROUP;
import static szewek.flux.FluxMod.MODID;

public final class F {

	public static final class Blocks {
		public static final Map<Metal, FluxOreBlock> ORES = makeOres();
		public static final Map<Metal, MetalBlock> METAL_BLOCKS = makeBlocks();
		public static final FluxGenBlock FLUXGEN = new FluxGenBlock(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(1f).sound(SoundType.METAL)
		);
		public static final EnergyCableBlock ENERGY_CABLE = new EnergyCableBlock(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(0.3f)
		);
		public static final ActiveTileBlock
				DIGGER = new ActiveTileBlock(),
				FARMER = new ActiveTileBlock(),
				BUTCHER = new ActiveTileBlock(),
				MOB_POUNDER = new ActiveTileBlock(),
				ITEM_ABSORBER = new ActiveTileBlock();
		public static final MachineBlock
				GRINDING_MILL = new MachineBlock(),
				ALLOY_CASTER = new MachineBlock(),
				WASHER = new MachineBlock(),
				COMPACTOR = new MachineBlock();

		public static void register(final IForgeRegistry<Block> reg) {
			ORES.values().forEach(reg::register);
			METAL_BLOCKS.values().forEach(reg::register);
			reg.registerAll(
					FLUXGEN.setRegistryName(MODID, "fluxgen"),
					ENERGY_CABLE.setRegistryName(MODID, "energy_cable"),
					DIGGER.setRegistryName(MODID, "digger"),
					FARMER.setRegistryName(MODID, "farmer"),
					BUTCHER.setRegistryName(MODID, "butcher"),
					MOB_POUNDER.setRegistryName(MODID, "mob_pounder"),
					ITEM_ABSORBER.setRegistryName(MODID, "item_absorber"),
					GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
					ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
					WASHER.setRegistryName(MODID, "washer"),
					COMPACTOR.setRegistryName(MODID, "compactor")
			);
		}

		private static Map<Metal, FluxOreBlock> makeOres() {
			Map<Metal, FluxOreBlock> m = new EnumMap<>(Metal.class);
			Metal[] var4 = Metal.values();
			for (Metal metal : var4) {
				if (metal.notVanillaOrAlloy()) {
					FluxOreBlock b = new FluxOreBlock(metal);
					b.setRegistryName("flux", metal.metalName + "_ore");
					m.put(metal, b);
				}
			}
			return m;
		}

		private static Map<Metal, MetalBlock> makeBlocks() {
			Map<Metal, MetalBlock> m = new EnumMap<>(Metal.class);
			Metal[] var4 = Metal.values();
			for (Metal metal : var4) {
				if (metal.nonVanilla()) {
					MetalBlock b = new MetalBlock(metal);
					b.setRegistryName("flux", metal.metalName + "_block");
					m.put(metal, b);
				}
			}
			return m;
		}
	}

	public static final class Items {
		public static final EnumMap<Metal, MetalItem>
				GRITS = metalMap("grit", Metal::nonAlloy),
				DUSTS = metalMap("dust", Metal::all),
				INGOTS = metalMap("ingot", Metal::nonVanilla);
		public static final FluxToolItem FLUXTOOL = create(FluxToolItem::new, "mftool", new Item.Properties().maxStackSize(1));
		public static final GiftItem GIFT = create(GiftItem::new, "gift", new Item.Properties().maxStackSize(1));
		public static final Item MACHINE_BASE = create(Item::new, "machine_base", new Item.Properties());

		public static void register(final IForgeRegistry<Item> reg) {
			GRITS.values().forEach(reg::register);
			DUSTS.values().forEach(reg::register);
			INGOTS.values().forEach(reg::register);
			F.Blocks.ORES.forEach((name, b) -> reg.register(fromBlock(b, name.metalName + "_ore")));
			F.Blocks.METAL_BLOCKS.forEach((name, b) -> reg.register(fromBlock(b, name.metalName + "_block")));
			reg.registerAll(
					FLUXTOOL, GIFT, MACHINE_BASE,
					fromBlock(Blocks.FLUXGEN, "fluxgen"),
					fromBlock(Blocks.GRINDING_MILL, "grinding_mill"),
					fromBlock(Blocks.ALLOY_CASTER, "alloy_caster"),
					fromBlock(Blocks.WASHER, "washer"),
					fromBlock(Blocks.COMPACTOR, "compactor"),
					fromBlock(Blocks.ENERGY_CABLE, "energy_cable"),
					fromBlock(Blocks.DIGGER, "digger"),
					fromBlock(Blocks.FARMER, "farmer"),
					fromBlock(Blocks.BUTCHER, "butcher"),
					fromBlock(Blocks.MOB_POUNDER, "mob_pounder"),
					fromBlock(Blocks.ITEM_ABSORBER, "item_absorber")
			);
		}
	}

	private static <T extends Item> T create(Function<Item.Properties, T> f, String name, Item.Properties props) {
		T item = f.apply(props.group(FLUX_GROUP));
		item.setRegistryName(MODID, name);
		return item;
	}

	private static BlockItem fromBlock(Block b, String name) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(FLUX_GROUP));
		item.setRegistryName(FluxMod.MODID, name);
		return item;
	}

	private static EnumMap<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
		EnumMap<Metal, MetalItem> m = new EnumMap<>(Metal.class);
		Item.Properties props = new Item.Properties();
		for (Metal met : Metal.values()) {
			if (filter.test(met)) {
				m.put(met, create(MetalItem::new, met.metalName + '_' + type, props).withMetal(met));
			}
		}
		return m;
	}

	public static final class Tiles {
		public static final TileEntityType<FluxGenTile> FLUXGEN;
		public static final TileEntityType<EnergyCableTile> ENERGY_CABLE;
		public static final TileEntityType<DiggerTile> DIGGER;
		public static final TileEntityType<FarmerTile> FARMER;
		public static final TileEntityType<ButcherTile> BUTCHER;
		public static final TileEntityType<MobPounderTile> MOB_POUNDER;
		public static final TileEntityType<ItemAbsorberTile> ITEM_ABSORBER;
		public static final FluxTileType<?> GRINDING_MILL;
		public static final FluxTileType<?> ALLOY_CASTER;
		public static final FluxTileType<?> WASHER;
		public static final FluxTileType<?> COMPACTOR;

		public static void register(final IForgeRegistry<TileEntityType<?>> reg) {
			reg.registerAll(
					FLUXGEN, ENERGY_CABLE, DIGGER, FARMER, BUTCHER, MOB_POUNDER, ITEM_ABSORBER,
					GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR
			);
		}

		static {
			FLUXGEN = create(FluxGenTile::new, "fluxgen", Blocks.FLUXGEN);
			ENERGY_CABLE = create(EnergyCableTile::new, "energy_cable", Blocks.ENERGY_CABLE);
			DIGGER = create(DiggerTile::new, "digger", Blocks.DIGGER);
			FARMER = create(FarmerTile::new, "farmer", Blocks.FARMER);
			BUTCHER = create(ButcherTile::new, "butcher", Blocks.BUTCHER);
			MOB_POUNDER = create(MobPounderTile::new, "mob_pounder", Blocks.MOB_POUNDER);
			ITEM_ABSORBER = create(ItemAbsorberTile::new, "item_absorber", Blocks.ITEM_ABSORBER);
			GRINDING_MILL = create(Machine2For1Tile.make(Recipes.GRINDING, GrindingMillContainer::new, "grinding_mill"), "grinding_mill", Blocks.GRINDING_MILL);
			ALLOY_CASTER = create(Machine2For1Tile.make(Recipes.ALLOYING, AlloyCasterContainer::new, "alloy_caster"), "alloy_caster", Blocks.ALLOY_CASTER);
			WASHER = create(Machine2For1Tile.make(Recipes.WASHING, WasherContainer::new, "washer"), "washer", Blocks.WASHER);
			COMPACTOR = create(Machine2For1Tile.make(Recipes.COMPACTING, CompactorContainer::new, "compactor"), "compactor", Blocks.COMPACTOR);
		}
	}

	private static <T extends TileEntity> TileEntityType<T> create(Supplier<T> f, String name, Block b) {
		TileEntityType<T> type = new TileEntityType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <T extends TileEntity> FluxTileType<T> create(Function<FluxTileType<T>, T> f, String name, Block b) {
		FluxTileType<T> type = new FluxTileType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	public static final class Containers {
		public static final ContainerType<FluxGenContainer> FLUXGEN;
		public static final ContainerType<GrindingMillContainer> GRINDING_MILL;
		public static final ContainerType<AlloyCasterContainer> ALLOY_CASTER;
		public static final ContainerType<WasherContainer> WASHER;
		public static final ContainerType<CompactorContainer> COMPACTOR;

		public static void register(final IForgeRegistry<ContainerType<?>> reg) {
			reg.registerAll(
					FLUXGEN.setRegistryName(MODID, "fluxgen"),
					GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
					ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
					WASHER.setRegistryName(MODID, "washer"),
					COMPACTOR.setRegistryName(MODID, "compactor")
			);
		}

		static {
			FLUXGEN = F.create(FluxGenContainer::new, FluxGenScreen::new);
			GRINDING_MILL = create(GrindingMillContainer::new, "grindable", "grinding_mill");
			ALLOY_CASTER = create(AlloyCasterContainer::new, "alloyable", "alloy_caster");
			WASHER = create(WasherContainer::new, "washable", "washer");
			COMPACTOR = create(CompactorContainer::new, "compactable", "compactor");
		}
	}

	private static <C extends Container, S extends Screen & IHasContainer<C>> ContainerType<C> create(IContainerFactory<C> factory, ScreenManager.IScreenFactory<C, S> screenFactory) {
		ContainerType<C> cont = new ContainerType<>(factory);
		if (screenFactory != null) {
			ScreenManager.registerFactory(cont, screenFactory);
		}
		return cont;
	}

	private static <C extends AbstractMachineContainer> ContainerType<C> create(IContainerFactory<C> cf, String showType, String title) {
		ContainerType<C> cont = new ContainerType<>(cf);
		ScreenManager.registerFactory(cont, MachineScreen.make(showType, title));
		return cont;
	}

	public static final class Recipes {
		public static FluxRecipeType<GrindingRecipe> GRINDING = type("grinding");
		public static FluxRecipeType<AlloyingRecipe> ALLOYING = type("alloying");
		public static FluxRecipeType<WashingRecipe> WASHING = type("washing");
		public static FluxRecipeType<CompactingRecipe> COMPACTING = type("compacting");
		public static MachineRecipeSerializer<GrindingRecipe> GRINDING_SERIALIZER = serializer(GrindingRecipe::new, "grinding");
		public static MachineRecipeSerializer<AlloyingRecipe> ALLOYING_SERIALIZER = serializer(AlloyingRecipe::new, "alloying");
		public static MachineRecipeSerializer<WashingRecipe> WASHING_SERIALIZER = serializer(WashingRecipe::new, "washing");
		public static MachineRecipeSerializer<CompactingRecipe> COMPACTING_SERIALIZER = serializer(CompactingRecipe::new, "compacting");

		public static void register(final IForgeRegistry<IRecipeSerializer<?>> reg) {
			reg.registerAll(
					GRINDING_SERIALIZER,
					ALLOYING_SERIALIZER,
					WASHING_SERIALIZER,
					COMPACTING_SERIALIZER
			);
		}

		private static <T extends IRecipe<?>> FluxRecipeType<T> type(String key) {
			return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new FluxRecipeType<>(key));
		}

		private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(MachineRecipeSerializer.IFactory<T> factory, String key) {
			MachineRecipeSerializer<T> mrs = new MachineRecipeSerializer<>(factory, 200);
			mrs.setRegistryName(MODID, key);
			return mrs;
		}
	}

	public static final class Villagers {
		public static final PointOfInterestType FLUX_ENGINEER_POI = poi("flux:flux_engineer", Blocks.FLUXGEN);
		public static final VillagerProfession FLUX_ENGINEER = new VillagerProfession("flux:flux_engineer", FLUX_ENGINEER_POI, ImmutableSet.of(), ImmutableSet.of(), null);

		public static void register(final IForgeRegistry<VillagerProfession> reg) {
			reg.register(FLUX_ENGINEER.setRegistryName(MODID, "flux_engineer"));
			VillagerTrades.VILLAGER_DEFAULT_TRADES.put(FLUX_ENGINEER, new Int2ObjectOpenHashMap<>(ImmutableMap.of(
					1, new VillagerTrades.ITrade[]{
							new VillagerTrades.EmeraldForItemsTrade(Items.INGOTS.get(Metal.COPPER), 6, 10, 4)
					},
					2, new VillagerTrades.ITrade[]{
							new VillagerTrades.EmeraldForItemsTrade(Items.INGOTS.get(Metal.TIN), 4, 8, 4)
					}
			)));
		}

		public static void registerPOI(final IForgeRegistry<PointOfInterestType> reg) {
			reg.register(FLUX_ENGINEER_POI.setRegistryName(MODID, "flux_engineer"));
		}

		private static PointOfInterestType poi(String name, Block b) {
			return PointOfInterestType.func_221052_a(new PointOfInterestType(name, ImmutableSet.copyOf(b.getStateContainer().getValidStates()), 1, 1));
		}
	}
}
