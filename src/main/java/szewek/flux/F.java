package szewek.flux;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import szewek.fl.recipe.RecipeCompat;
import szewek.fl.type.FluxContainerType;
import szewek.fl.type.FluxRecipeType;
import szewek.fl.type.FluxTileType;
import szewek.fl.util.FluxItemTier;
import szewek.flux.block.*;
import szewek.flux.container.*;
import szewek.flux.gui.FluxGenScreen;
import szewek.flux.gui.MachineScreen;
import szewek.flux.gui.SignalControllerScreen;
import szewek.flux.item.ChipItem;
import szewek.flux.item.FluxAdhesiveItem;
import szewek.flux.item.GiftItem;
import szewek.flux.item.MetalItem;
import szewek.flux.recipe.*;
import szewek.flux.tile.*;
import szewek.flux.util.ChipUpgradeTrade;
import szewek.flux.util.Gifts;
import szewek.flux.util.Toolset;
import szewek.flux.util.metals.Metal;
import szewek.flux.util.metals.Metals;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static szewek.flux.Flux.MODID;

public final class F {
	public static final ItemGroup FLUX_GROUP = new ItemGroup("flux.items") {
		@Override public ItemStack createIcon() {
			return new ItemStack(B.FLUXGEN);
		}
	};

	@SubscribeEvent
	public static void blocks(final RegistryEvent.Register<Block> re) {
		final IForgeRegistry<Block> reg = re.getRegistry();
		registerAll(reg, B.ORES.values());
		registerAll(reg, B.METAL_BLOCKS.values());
		reg.registerAll(
				B.FLUXGEN, B.ENERGY_CABLE, B.SIGNAL_CABLE,
				B.DIGGER, B.FARMER, B.BUTCHER, B.MOB_POUNDER, B.ITEM_ABSORBER,
				B.RR_TABLE, B.ONLINE_MARKET,
				B.GRINDING_MILL, B.ALLOY_CASTER, B.WASHER, B.COMPACTOR,
				B.INTERACTOR_RAIL, B.SIGNAL_CONTROLLER, B.COPIER
		);
	}

	@SubscribeEvent
	public static void items(final RegistryEvent.Register<Item> re) {
		final IForgeRegistry<Item> reg = re.getRegistry();
		registerAll(reg, I.GRITS.values());
		registerAll(reg, I.DUSTS.values());
		registerAll(reg, I.INGOTS.values());
		registerAll(reg, I.NUGGETS.values());
		registerAll(reg, I.GEARS.values());
		registerAll(reg, I.PLATES.values());
		for (FluxOreBlock ore : B.ORES.values()) {
			reg.register(fromBlock(ore));
		}
		for (MetalBlock mb : B.METAL_BLOCKS.values()) {
			reg.register(fromBlock(mb));
		}
		reg.registerAll(
				I.GIFT, I.MACHINE_BASE, I.CHIP,
				I.SEAL, I.GLUE, I.PASTE,
				fromBlock(B.FLUXGEN),
				fromBlock(B.GRINDING_MILL),
				fromBlock(B.ALLOY_CASTER),
				fromBlock(B.WASHER),
				fromBlock(B.COMPACTOR),
				fromBlock(B.ENERGY_CABLE),
				fromBlock(B.SIGNAL_CABLE),
				fromBlock(B.DIGGER),
				fromBlock(B.FARMER),
				fromBlock(B.BUTCHER),
				fromBlock(B.MOB_POUNDER),
				fromBlock(B.ITEM_ABSORBER),
				fromBlock(B.RR_TABLE),
				fromBlock(B.ONLINE_MARKET),
				fromBlock(B.INTERACTOR_RAIL),
				fromBlock(B.SIGNAL_CONTROLLER),
				fromBlock(B.COPIER)
		);
		I.BRONZE_TOOLS.registerTools(reg);
		I.STEEL_TOOLS.registerTools(reg);
	}

	@SubscribeEvent
	public static void tiles(final RegistryEvent.Register<TileEntityType<?>> re) {
		re.getRegistry().registerAll(
				T.FLUXGEN, T.ENERGY_CABLE, T.SIGNAL_CABLE, T.DIGGER, T.FARMER, T.BUTCHER, T.MOB_POUNDER, T.ITEM_ABSORBER,
				T.GRINDING_MILL, T.ALLOY_CASTER, T.WASHER, T.COMPACTOR, T.RR_TABLE, T.ONLINE_MARKET,
				T.INTERACTOR_RAIL, T.SIGNAL_CONTROLLER, T.COPIER
		);
	}

	@SubscribeEvent
	public static void containers(final RegistryEvent.Register<ContainerType<?>> re) {
		re.getRegistry().registerAll(
				C.FLUXGEN, C.GRINDING_MILL, C.ALLOY_CASTER, C.WASHER, C.COMPACTOR,
				C.SIGNAL_CONTROLLER, C.COPIER
		);
	}

	@SubscribeEvent
	public static void recipes(final RegistryEvent.Register<IRecipeSerializer<?>> re) {
		re.getRegistry().registerAll(
				R.GRINDING.serializer,
				R.ALLOYING.serializer,
				R.WASHING.serializer,
				R.COMPACTING.serializer,
				CopyingRecipe.SERIALIZER
		);
		@SuppressWarnings("unchecked")
		final List<String> blacklist = (List<String>) FluxCfg.COMMON.blacklistCompatRecipes.get();
		final Predicate<String> filterBlacklist = s -> !blacklist.contains(s);

		recipeCompat(R.GRINDING, filterBlacklist,
				"pattysmorestuff:crushing",
				"silents_mechanisms:crushing",
				"usefulmachinery:crushing"
		);
		recipeCompat(R.ALLOYING, filterBlacklist,
				"blue_power:alloy_smelting"
		);
		recipeCompat(R.COMPACTING, filterBlacklist, "wtbw_machines:compressing");
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
	static void client(final Minecraft mc) {
		final Item[] arr = new Item[0];
		final ItemColors ic = mc.getItemColors();
		ic.register(Gifts::colorByGift, I.GIFT);
		ic.register(Metals::gritColors, I.GRITS.values().toArray(arr));
		ic.register(Metals::itemColors, I.DUSTS.values().toArray(arr));
		ic.register(Metals::ingotColors, I.INGOTS.values().toArray(arr));
		ic.register(Metals::itemColors, I.NUGGETS.values().toArray(arr));
		ic.register(Metals::itemColors, I.GEARS.values().toArray(arr));
		ic.register(Metals::itemColors, I.PLATES.values().toArray(arr));
		I.BRONZE_TOOLS.registerToolColors(Metals.BRONZE, ic);
		I.STEEL_TOOLS.registerToolColors(Metals.STEEL, ic);

		RenderTypeLookup.setRenderLayer(B.INTERACTOR_RAIL, RenderType.getCutout());

		ScreenManager.registerFactory(C.FLUXGEN, FluxGenScreen::new);
		ScreenManager.registerFactory(C.SIGNAL_CONTROLLER, SignalControllerScreen::new);
		ScreenManager.registerFactory(C.GRINDING_MILL, MachineScreen.make("grindable", "grinding_mill"));
		ScreenManager.registerFactory(C.ALLOY_CASTER, MachineScreen.make("alloyable", "alloy_caster"));
		ScreenManager.registerFactory(C.WASHER, MachineScreen.make("washable", "washer"));
		ScreenManager.registerFactory(C.COMPACTOR, MachineScreen.make("compactable", "compactor"));
		ScreenManager.registerFactory(C.COPIER, MachineScreen.make("copyable", "copier"));
	}

	public static final class B {
		public static final Map<Metal, FluxOreBlock> ORES = makeOres();
		public static final Map<Metal, MetalBlock> METAL_BLOCKS = makeBlocks();
		public static final FluxGenBlock FLUXGEN = named(new FluxGenBlock(), "fluxgen");
		public static final AbstractCableBlock
				ENERGY_CABLE = named(new EnergyCableBlock(), "energy_cable"),
				SIGNAL_CABLE = named(new SignalCableBlock(), "signal_cable");
		public static final RRTableBlock RR_TABLE = named(new RRTableBlock(), "rrtable");
		public static final OnlineMarketBlock ONLINE_MARKET = named(new OnlineMarketBlock(), "online_market");
		public static final InteractorRailBlock INTERACTOR_RAIL = named(new InteractorRailBlock(), "interactor_rail");
		public static final SignalControllerBlock SIGNAL_CONTROLLER = named(new SignalControllerBlock(), "signal_controller");
		public static final ActiveTileBlock
				DIGGER = named(new ActiveTileBlock(), "digger"),
				FARMER = named(new ActiveTileBlock(), "farmer"),
				BUTCHER = named(new ActiveTileBlock(), "butcher"),
				MOB_POUNDER = named(new ActiveTileBlock(), "mob_pounder"),
				ITEM_ABSORBER = named(new ActiveTileBlock(), "item_absorber");
		public static final MachineBlock
				GRINDING_MILL = named(new MachineBlock(), "grinding_mill"),
				ALLOY_CASTER = named(new MachineBlock(), "alloy_caster"),
				WASHER = named(new MachineBlock(), "washer"),
				COMPACTOR = named(new MachineBlock(), "compactor"),
				COPIER = named(new MachineBlock(), "copier");
	}

	public static final class I {
		public static final Map<Metal, MetalItem>
				GRITS = metalMap("grit", Metal::nonAlloy),
				DUSTS = metalMap("dust", null),
				INGOTS = metalMap("ingot", Metal::nonVanilla),
				NUGGETS = metalMap("nugget", Metal::nonVanilla),
				GEARS = metalMap("gear", null),
				PLATES = metalMap("plate", null);
		public static final GiftItem GIFT = item(GiftItem::new, "gift", new Item.Properties().maxStackSize(1));
		public static final Item MACHINE_BASE = item(Item::new, "machine_base", new Item.Properties());
		public static final ChipItem CHIP = item(ChipItem::new, "chip", new Item.Properties());
		public static final FluxAdhesiveItem
				SEAL = item(FluxAdhesiveItem::new, "seal", new Item.Properties()),
				GLUE = item(FluxAdhesiveItem::new, "glue", new Item.Properties()),
				PASTE = item(FluxAdhesiveItem::new, "paste", new Item.Properties());

		public static final FluxItemTier
				BRONZE_TIER,
				STEEL_TIER;
		public static final Toolset
				BRONZE_TOOLS,
				STEEL_TOOLS;

		static {
			FluxItemTier.Builder b = new FluxItemTier.Builder();
			BRONZE_TIER = b.setHarvestLevel(2)
					.setMaxUses(500)
					.setEfficiency(7f)
					.setAttackDamage(2.5f)
					.setEnchantability(20)
					.setTagName("ingots/bronze")
					.setMaterial(INGOTS.get(Metals.BRONZE))
					.build();
			STEEL_TIER = b.setHarvestLevel(3)
					.setMaxUses(1500)
					.setEfficiency(8.5f)
					.setAttackDamage(3f)
					.setEnchantability(22)
					.setTagName("ingots/steel")
					.setMaterial(INGOTS.get(Metals.STEEL))
					.build();
			BRONZE_TOOLS = new Toolset(BRONZE_TIER, "bronze");
			STEEL_TOOLS = new Toolset(STEEL_TIER, "steel");
		}
	}

	public static final class T {
		public static final TileEntityType<FluxGenTile> FLUXGEN;
		public static final TileEntityType<EnergyCableTile> ENERGY_CABLE;
		public static final TileEntityType<SignalCableTile> SIGNAL_CABLE;
		public static final TileEntityType<DiggerTile> DIGGER;
		public static final TileEntityType<FarmerTile> FARMER;
		public static final TileEntityType<ButcherTile> BUTCHER;
		public static final TileEntityType<MobPounderTile> MOB_POUNDER;
		public static final TileEntityType<ItemAbsorberTile> ITEM_ABSORBER;
		public static final TileEntityType<RRTableTile> RR_TABLE;
		public static final TileEntityType<OnlineMarketTile> ONLINE_MARKET;
		public static final TileEntityType<InteractorRailTile> INTERACTOR_RAIL;
		public static final TileEntityType<SignalControllerTile> SIGNAL_CONTROLLER;
		public static final TileEntityType<CopierTile> COPIER;
		public static final FluxTileType<?>
				GRINDING_MILL,
				ALLOY_CASTER,
				WASHER,
				COMPACTOR;

		static {
			FLUXGEN = tile(FluxGenTile::new, B.FLUXGEN);
			ENERGY_CABLE = tile(EnergyCableTile::new, B.ENERGY_CABLE);
			SIGNAL_CABLE = tile(SignalCableTile::new, B.SIGNAL_CABLE);
			DIGGER = tile(DiggerTile::new, B.DIGGER);
			FARMER = tile(FarmerTile::new, B.FARMER);
			BUTCHER = tile(ButcherTile::new, B.BUTCHER);
			MOB_POUNDER = tile(MobPounderTile::new, B.MOB_POUNDER);
			ITEM_ABSORBER = tile(ItemAbsorberTile::new, B.ITEM_ABSORBER);
			RR_TABLE = tile(RRTableTile::new, B.RR_TABLE);
			ONLINE_MARKET = tile(OnlineMarketTile::new, B.ONLINE_MARKET);
			INTERACTOR_RAIL = tile(InteractorRailTile::new, B.INTERACTOR_RAIL);
			SIGNAL_CONTROLLER = tile(SignalControllerTile::new, B.SIGNAL_CONTROLLER);
			COPIER = tile(CopierTile::new, B.COPIER);
			GRINDING_MILL = tile(Machine2For1Tile.make(R.GRINDING, C.GRINDING_MILL, "grinding_mill"), B.GRINDING_MILL);
			ALLOY_CASTER = tile(Machine2For1Tile.make(R.ALLOYING, C.ALLOY_CASTER, "alloy_caster"), B.ALLOY_CASTER);
			WASHER = tile(Machine2For1Tile.make(R.WASHING, C.WASHER, "washer"), B.WASHER);
			COMPACTOR = tile(Machine2For1Tile.make(R.COMPACTING, C.COMPACTOR, "compactor"), B.COMPACTOR);
		}
	}

	public static final class C {
		public static final ContainerType<FluxGenContainer> FLUXGEN;
		public static final ContainerType<SignalControllerContainer> SIGNAL_CONTROLLER;
		public static final ContainerType<CopierContainer> COPIER;
		public static final FluxContainerType<Machine2For1Container>
				GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR;

		static {
			FLUXGEN = container(FluxGenContainer::new, "fluxgen");
			SIGNAL_CONTROLLER = container(SignalControllerContainer::new, "signal_controller");
			COPIER = container(CopierContainer::new, "copier");
			GRINDING_MILL = containerFlux(Machine2For1Container.make(R.GRINDING), "grinding_mill");
			ALLOY_CASTER = containerFlux(Machine2For1Container.make(R.ALLOYING), "alloy_caster");
			WASHER = containerFlux(Machine2For1Container.make(R.WASHING), "washer");
			COMPACTOR = containerFlux(Machine2For1Container.make(R.COMPACTING), "compactor");
		}
	}

	public static final class R {
		public static final FluxRecipeType<GrindingRecipe> GRINDING = recipe("grinding", serializer(GrindingRecipe::new, "grinding"));
		public static final FluxRecipeType<AlloyingRecipe> ALLOYING = recipe("alloying", serializer(AlloyingRecipe::new, "alloying"));
		public static final FluxRecipeType<WashingRecipe> WASHING = recipe("washing", serializer(WashingRecipe::new, "washing"));
		public static final FluxRecipeType<CompactingRecipe> COMPACTING = recipe("compacting", serializer(CompactingRecipe::new, "compacting"));
		public static final FluxRecipeType<CopyingRecipe> COPYING = recipe("copying", CopyingRecipe.SERIALIZER);
	}

	public static final class V {
		public static final PointOfInterestType
				FLUX_ENGINEER_POI = poi("flux_engineer", B.FLUXGEN);
		public static final VillagerProfession FLUX_ENGINEER = new VillagerProfession("flux:flux_engineer", FLUX_ENGINEER_POI, ImmutableSet.of(), ImmutableSet.of(), null);
	}

	public static final class Tags {
		public static final Tag<Block> DIGGER_SKIP = blockTag("digger_skip");
	}

	private static <T extends IForgeRegistryEntry<T>> void registerAll(IForgeRegistry<T> reg, Iterable<? extends T> iter) {
		for (T t : iter) {
			reg.register(t);
		}
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

	@SuppressWarnings("ConstantConditions")
	private static BlockItem fromBlock(Block b) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(FLUX_GROUP));
		item.setRegistryName(b.getRegistryName());
		return item;
	}

	private static <T extends ForgeRegistryEntry<? super T>> T named(T t, String name) {
		t.setRegistryName(MODID, name);
		return t;
	}

	private static Map<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
		Map<Metal, MetalItem> m = new HashMap<>();
		Item.Properties props = new Item.Properties();
		for (Metal met : Metals.all()) {
			if (filter == null || filter.test(met)) {
				m.put(met, item(p -> new MetalItem(p, met), met.metalName + '_' + type, props));
			}
		}
		return m;
	}

	@SuppressWarnings("ConstantConditions")
	private static <T extends TileEntity> TileEntityType<T> tile(Supplier<T> f, Block b) {
		TileEntityType<T> type = new TileEntityType<>(f, Collections.singleton(b), null);
		type.setRegistryName(b.getRegistryName());
		return type;
	}

	@SuppressWarnings("ConstantConditions")
	private static <T extends TileEntity> FluxTileType<T> tile(Function<FluxTileType<T>, T> f, Block b) {
		FluxTileType<T> type = new FluxTileType<>(f, Collections.singleton(b), null);
		type.setRegistryName(b.getRegistryName());
		return type;
	}

	private static <C extends Container> ContainerType<C> container(IContainerFactory<C> factory, String name) {
		ContainerType<C> ct = new ContainerType<>(factory);
		ct.setRegistryName(MODID, name);
		return ct;
	}

	private static <C extends AbstractMachineContainer> FluxContainerType<C> containerFlux(FluxContainerType.IContainerBuilder<C> cb, String name) {
		FluxContainerType<C> ct = new FluxContainerType<>(cb);
		ct.setRegistryName(MODID, name);
		return ct;
	}

	private static <T extends IRecipe<?>> FluxRecipeType<T> recipe(String key, IRecipeSerializer<T> ser) {
		return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new FluxRecipeType<>(key, ser));
	}

	private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(BiFunction<ResourceLocation, MachineRecipeSerializer.Builder, T> factory, String key) {
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

	private static void recipeCompat(IRecipeType<?> rtype, Predicate<String> filter, String... compats) {
		RecipeCompat.registerCompatRecipeTypes(rtype, Arrays.stream(compats).filter(filter).collect(Collectors.toSet()));
	}

	private static Tag<Block> blockTag(String name) {
		return new BlockTags.Wrapper(new ResourceLocation(MODID, name));
	}

	private F() {}
}
