package szewek.flux;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.fl.type.FluxRecipeType;
import szewek.fl.type.FluxTileType;
import szewek.flux.block.*;
import szewek.flux.container.AbstractMachineContainer;
import szewek.fl.type.FluxContainerType;
import szewek.flux.container.FluxGenContainer;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.gui.FluxGenScreen;
import szewek.flux.gui.MachineScreen;
import szewek.flux.item.*;
import szewek.flux.recipe.*;
import szewek.flux.tile.*;
import szewek.flux.util.ChipUpgradeTrade;
import szewek.fl.util.FluxItemTier;
import szewek.flux.util.Toolset;
import szewek.flux.util.metals.Metal;
import szewek.flux.util.metals.Metals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static szewek.flux.FluxMod.FLUX_GROUP;
import static szewek.flux.FluxMod.MODID;

public final class F {
	@SubscribeEvent
	public static void blocks(final RegistryEvent.Register<Block> re) {
		final IForgeRegistry<Block> reg = re.getRegistry();
		for (FluxOreBlock ore : B.ORES.values()) reg.register(ore);
		for (MetalBlock metalBlock : B.METAL_BLOCKS.values()) reg.register(metalBlock);
		reg.registerAll(
				B.FLUXGEN.setRegistryName(MODID, "fluxgen"),
				B.ENERGY_CABLE.setRegistryName(MODID, "energy_cable"),
				B.DIGGER.setRegistryName(MODID, "digger"),
				B.FARMER.setRegistryName(MODID, "farmer"),
				B.BUTCHER.setRegistryName(MODID, "butcher"),
				B.MOB_POUNDER.setRegistryName(MODID, "mob_pounder"),
				B.ITEM_ABSORBER.setRegistryName(MODID, "item_absorber"),
				B.GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				B.ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				B.WASHER.setRegistryName(MODID, "washer"),
				B.COMPACTOR.setRegistryName(MODID, "compactor")
		);
	}

	@SubscribeEvent
	public static void items(final RegistryEvent.Register<Item> re) {
		final IForgeRegistry<Item> reg = re.getRegistry();
		for (MetalItem grit : I.GRITS.values()) reg.register(grit);
		for (MetalItem dust : I.DUSTS.values()) reg.register(dust);
		for (MetalItem ingot : I.INGOTS.values()) reg.register(ingot);
		B.ORES.forEach((name, b) -> reg.register(fromBlock(b, name.metalName + "_ore")));
		B.METAL_BLOCKS.forEach((name, b) -> reg.register(fromBlock(b, name.metalName + "_block")));
		reg.registerAll(
				I.FLUXTOOL, I.GIFT, I.MACHINE_BASE, I.CHIP,
				I.SEAL, I.GLUE, I.PASTE,
				fromBlock(B.FLUXGEN, "fluxgen"),
				fromBlock(B.GRINDING_MILL, "grinding_mill"),
				fromBlock(B.ALLOY_CASTER, "alloy_caster"),
				fromBlock(B.WASHER, "washer"),
				fromBlock(B.COMPACTOR, "compactor"),
				fromBlock(B.ENERGY_CABLE, "energy_cable"),
				fromBlock(B.DIGGER, "digger"),
				fromBlock(B.FARMER, "farmer"),
				fromBlock(B.BUTCHER, "butcher"),
				fromBlock(B.MOB_POUNDER, "mob_pounder"),
				fromBlock(B.ITEM_ABSORBER, "item_absorber")
		);
		I.BRONZE_TOOLS.registerTools(reg);
		I.STEEL_TOOLS.registerTools(reg);
	}

	@SubscribeEvent
	public static void tiles(final RegistryEvent.Register<TileEntityType<?>> re) {
		re.getRegistry().registerAll(
				T.FLUXGEN, T.ENERGY_CABLE, T.DIGGER, T.FARMER, T.BUTCHER, T.MOB_POUNDER, T.ITEM_ABSORBER,
				T.GRINDING_MILL, T.ALLOY_CASTER, T.WASHER, T.COMPACTOR
		);
	}

	@SubscribeEvent
	public static void containers(final RegistryEvent.Register<ContainerType<?>> re) {
		re.getRegistry().registerAll(
				C.FLUXGEN.setRegistryName(MODID, "fluxgen"),
				C.GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				C.ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				C.WASHER.setRegistryName(MODID, "washer"),
				C.COMPACTOR.setRegistryName(MODID, "compactor")
		);
	}

	@SubscribeEvent
	public static void recipes(final RegistryEvent.Register<IRecipeSerializer<?>> re) {
		re.getRegistry().registerAll(
				R.GRINDING.serializer,
				R.ALLOYING.serializer,
				R.WASHING.serializer,
				R.COMPACTING.serializer
		);
		CraftingHelper.register(new ResourceLocation(MODID, "counted"), CountedIngredient.Serializer.INSTANCE);
	}

	@SubscribeEvent
	public static void professions(final RegistryEvent.Register<VillagerProfession> re) {
		re.getRegistry().register(V.FLUX_ENGINEER.setRegistryName(MODID, "flux_engineer"));
		Int2ObjectMap<VillagerTrades.ITrade[]> lvlTrades = new Int2ObjectOpenHashMap<>();
		lvlTrades.put(1, new VillagerTrades.ITrade[]{
				new VillagerTrades.EmeraldForItemsTrade(I.INGOTS.get(Metals.COPPER), 6, 10, 4),
				new ChipUpgradeTrade(-1, 5)
		});
		lvlTrades.put(2, new VillagerTrades.ITrade[]{
				new VillagerTrades.EmeraldForItemsTrade(I.INGOTS.get(Metals.TIN), 4, 8, 4),
				new ChipUpgradeTrade(-3, 10)
		});
		lvlTrades.put(3, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-5, 20)
		});
		lvlTrades.put(4, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-9, 50)
		});
		lvlTrades.put(5, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-9, 100)
		});
		VillagerTrades.VILLAGER_DEFAULT_TRADES.put(V.FLUX_ENGINEER, lvlTrades);
	}

	@SubscribeEvent
	public static void pointsOfInterest(final RegistryEvent.Register<PointOfInterestType> re) {
		re.getRegistry().register(V.FLUX_ENGINEER_POI);
	}

	@OnlyIn(Dist.CLIENT)
	static void screens() {
		ScreenManager.registerFactory(C.FLUXGEN, FluxGenScreen::new);
		ScreenManager.registerFactory(C.GRINDING_MILL, MachineScreen.make("grindable", "grinding_mill"));
		ScreenManager.registerFactory(C.ALLOY_CASTER, MachineScreen.make("alloyable", "alloy_caster"));
		ScreenManager.registerFactory(C.WASHER, MachineScreen.make("washable", "washer"));
		ScreenManager.registerFactory(C.COMPACTOR, MachineScreen.make("compactable", "compactor"));
	}

	public static final class B {
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
	}

	public static final class I {
		public static final Map<Metal, MetalItem>
				GRITS = metalMap("grit", Metal::nonAlloy),
				DUSTS = metalMap("dust", null),
				INGOTS = metalMap("ingot", Metal::nonVanilla);
		public static final FluxToolItem FLUXTOOL = item(FluxToolItem::new, "mftool", new Item.Properties().maxStackSize(1));
		public static final GiftItem GIFT = item(GiftItem::new, "gift", new Item.Properties().maxStackSize(1));
		public static final Item MACHINE_BASE = item(Item::new, "machine_base", new Item.Properties());
		public static final ChipItem CHIP = item(ChipItem::new, "chip", new Item.Properties());
		public static final FluxAdhesiveItem SEAL = item(FluxAdhesiveItem::new, "seal", new Item.Properties());
		public static final FluxAdhesiveItem GLUE = item(FluxAdhesiveItem::new, "glue", new Item.Properties());
		public static final FluxAdhesiveItem PASTE = item(FluxAdhesiveItem::new, "paste", new Item.Properties());

		public static final FluxItemTier
				BRONZE_TIER = new FluxItemTier(2, 500, 7f, 2.5f, 20, "ingots/bronze"),
				STEEL_TIER = new FluxItemTier(3, 1500, 8.5f, 3f, 22, "ingots/steel");
		public static final Toolset
				BRONZE_TOOLS = new Toolset(BRONZE_TIER, "bronze"),
				STEEL_TOOLS = new Toolset(STEEL_TIER, "steel");

	}

	public static final class T {
		public static final TileEntityType<FluxGenTile> FLUXGEN;
		public static final TileEntityType<EnergyCableTile> ENERGY_CABLE;
		public static final TileEntityType<DiggerTile> DIGGER;
		public static final TileEntityType<FarmerTile> FARMER;
		public static final TileEntityType<ButcherTile> BUTCHER;
		public static final TileEntityType<MobPounderTile> MOB_POUNDER;
		public static final TileEntityType<ItemAbsorberTile> ITEM_ABSORBER;
		public static final FluxTileType<?> GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR;

		static {
			FLUXGEN = tile(FluxGenTile::new, "fluxgen", B.FLUXGEN);
			ENERGY_CABLE = tile(EnergyCableTile::new, "energy_cable", B.ENERGY_CABLE);
			DIGGER = tile(DiggerTile::new, "digger", B.DIGGER);
			FARMER = tile(FarmerTile::new, "farmer", B.FARMER);
			BUTCHER = tile(ButcherTile::new, "butcher", B.BUTCHER);
			MOB_POUNDER = tile(MobPounderTile::new, "mob_pounder", B.MOB_POUNDER);
			ITEM_ABSORBER = tile(ItemAbsorberTile::new, "item_absorber", B.ITEM_ABSORBER);
			GRINDING_MILL = tile(Machine2For1Tile.make(R.GRINDING, C.GRINDING_MILL, "grinding_mill"), "grinding_mill", B.GRINDING_MILL);
			ALLOY_CASTER = tile(Machine2For1Tile.make(R.ALLOYING, C.ALLOY_CASTER, "alloy_caster"), "alloy_caster", B.ALLOY_CASTER);
			WASHER = tile(Machine2For1Tile.make(R.WASHING, C.WASHER, "washer"), "washer", B.WASHER);
			COMPACTOR = tile(Machine2For1Tile.make(R.COMPACTING, C.COMPACTOR, "compactor"), "compactor", B.COMPACTOR);
		}
	}

	public static final class C {
		public static final ContainerType<FluxGenContainer> FLUXGEN;
		public static final FluxContainerType<Machine2For1Container>
				GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR;

		static {
			FLUXGEN = container(FluxGenContainer::new);
			GRINDING_MILL = containerFlux(Machine2For1Container.make(R.GRINDING));
			ALLOY_CASTER = containerFlux(Machine2For1Container.make(R.ALLOYING));
			WASHER = containerFlux(Machine2For1Container.make(R.WASHING));
			COMPACTOR = containerFlux(Machine2For1Container.make(R.COMPACTING));
		}
	}

	public static final class R {
		public static FluxRecipeType<GrindingRecipe> GRINDING = recipe("grinding", serializer(GrindingRecipe::new, "grinding"));
		public static FluxRecipeType<AlloyingRecipe> ALLOYING = recipe("alloying", serializer(AlloyingRecipe::new, "alloying"));
		public static FluxRecipeType<WashingRecipe> WASHING = recipe("washing", serializer(WashingRecipe::new, "washing"));
		public static FluxRecipeType<CompactingRecipe> COMPACTING = recipe("compacting", serializer(CompactingRecipe::new, "compacting"));
	}

	public static final class V {
		public static final PointOfInterestType FLUX_ENGINEER_POI = poi("flux_engineer", B.FLUXGEN);
		public static final VillagerProfession FLUX_ENGINEER = new VillagerProfession("flux:flux_engineer", FLUX_ENGINEER_POI, ImmutableSet.of(), ImmutableSet.of(), null);
	}

	private static Map<Metal, FluxOreBlock> makeOres() {
		Map<Metal, FluxOreBlock> m = new HashMap<>();
		for (Metal metal : Metals.all()) {
			if (metal.notVanillaOrAlloy()) {
				FluxOreBlock b = new FluxOreBlock(metal);
				b.setRegistryName("flux", metal.metalName + "_ore");
				m.put(metal, b);
			}
		}
		return m;
	}

	private static Map<Metal, MetalBlock> makeBlocks() {
		Map<Metal, MetalBlock> m = new HashMap<>();
		for (Metal metal : Metals.all()) {
			if (metal.nonVanilla()) {
				MetalBlock b = new MetalBlock(metal);
				b.setRegistryName("flux", metal.metalName + "_block");
				m.put(metal, b);
			}
		}
		return m;
	}

	private static <T extends Item> T item(Function<Item.Properties, T> f, String name, Item.Properties props) {
		T item = f.apply(props.group(FLUX_GROUP));
		item.setRegistryName(MODID, name);
		return item;
	}

	private static BlockItem fromBlock(Block b, String name) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(FLUX_GROUP));
		item.setRegistryName(FluxMod.MODID, name);
		return item;
	}

	private static Map<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
		Map<Metal, MetalItem> m = new HashMap<>();
		Item.Properties props = new Item.Properties();
		for (Metal met : Metals.all()) {
			if (filter == null || filter.test(met)) {
				m.put(met, item(MetalItem::new, met.metalName + '_' + type, props).withMetal(met));
			}
		}
		return m;
	}

	private static <T extends TileEntity> TileEntityType<T> tile(Supplier<T> f, String name, Block b) {
		TileEntityType<T> type = new TileEntityType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <T extends TileEntity> FluxTileType<T> tile(Function<FluxTileType<T>, T> f, String name, Block b) {
		FluxTileType<T> type = new FluxTileType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <C extends Container> ContainerType<C> container(IContainerFactory<C> factory) {
		return new ContainerType<>(factory);
	}

	private static <C extends AbstractMachineContainer> FluxContainerType<C> containerFlux(FluxContainerType.IContainerBuilder<C> cb) {
		return new FluxContainerType<>(cb);
	}

	private static <T extends IRecipe<?>> FluxRecipeType<T> recipe(String key, IRecipeSerializer<T> ser) {
		return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new FluxRecipeType<>(key, ser));
	}

	private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(MachineRecipeSerializer.IFactory<T> factory, String key) {
		MachineRecipeSerializer<T> mrs = new MachineRecipeSerializer<>(factory, 200);
		mrs.setRegistryName(MODID, key);
		return mrs;
	}

	private static PointOfInterestType poi(String name, Block b) {
		return PointOfInterestType.func_221052_a(
				new PointOfInterestType(MODID + ":" + name, ImmutableSet.copyOf(b.getStateContainer().getValidStates()), 1, 1)
				.setRegistryName(MODID, name)
		);
	}
}
