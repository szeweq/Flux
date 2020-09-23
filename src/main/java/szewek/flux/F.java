package szewek.flux;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
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
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
import szewek.fl.util.ConsumerUtil;
import szewek.fl.util.FluxItemTier;
import szewek.flux.block.*;
import szewek.flux.container.*;
import szewek.flux.data.Gifts;
import szewek.flux.gui.FluxGenScreen;
import szewek.flux.gui.MachineScreen;
import szewek.flux.gui.SignalControllerScreen;
import szewek.flux.item.ChipItem;
import szewek.flux.item.FluxAdhesiveItem;
import szewek.flux.item.GiftItem;
import szewek.flux.item.MetalItem;
import szewek.flux.recipe.*;
import szewek.flux.tile.*;
import szewek.flux.tile.cable.EnergyCableTile;
import szewek.flux.tile.cable.SignalCableTile;
import szewek.flux.util.ChipUpgradeTrade;
import szewek.flux.util.Toolset;
import szewek.flux.util.metals.Metal;
import szewek.flux.util.metals.Metals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import static szewek.flux.Flux.MODID;

public final class F {
	public static final ItemGroup
			FLUX_BLOCKS = new ItemGroup("flux.blocks") {
				@Override
				public ItemStack createIcon() {
					return new ItemStack(B.FLUXGEN);
				}
			},
			FLUX_ITEMS = new ItemGroup("flux.items") {
				@Override public ItemStack createIcon() {
					return new ItemStack(I.CHIP);
				}
			};

	@SubscribeEvent
	public static void blocks(final RegistryEvent.Register<Block> re) {
		final IForgeRegistry<Block> reg = re.getRegistry();
		registerMapValues(B.ORES, reg);
		registerMapValues(B.METAL_BLOCKS, reg);
		registerFromClass(B.class, reg);
	}

	@SubscribeEvent
	public static void items(final RegistryEvent.Register<Item> re) {
		final IForgeRegistry<Item> reg = re.getRegistry();
		final BiConsumer<Metal, Block> regFromBlock = (m, b) -> reg.register(fromBlock(b));
		registerMapValues(I.GRITS, reg);
		registerMapValues(I.DUSTS, reg);
		registerMapValues(I.INGOTS, reg);
		registerMapValues(I.NUGGETS, reg);
		registerMapValues(I.GEARS, reg);
		registerMapValues(I.PLATES, reg);
		B.ORES.forEach(regFromBlock);
		B.METAL_BLOCKS.forEach(regFromBlock);
		registerFromClass(I.class, reg);
		ConsumerUtil.forEachStaticField(B.class, Block.class, b -> reg.register(fromBlock(b)));
		ConsumerUtil.forEachStaticField(I.class, Toolset.class, toolset -> toolset.registerTools(reg));
	}

	@SubscribeEvent
	public static void tiles(final RegistryEvent.Register<TileEntityType<?>> re) {
		registerFromClass(T.class, re.getRegistry());
	}

	@SubscribeEvent
	public static void containers(final RegistryEvent.Register<ContainerType<?>> re) {
		registerFromClass(C.class, re.getRegistry());
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
		re.getRegistry().register(V.FLUX_ENGINEER);
		Int2ObjectMap<VillagerTrades.ITrade[]> lvlTrades = new Int2ObjectOpenHashMap<>(5);
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

		ic.register(Gifts::giftColors, I.GIFT);
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
		ScreenManager.registerFactory(C.ONLINE_MARKET, MerchantScreen::new);
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

		private static Map<Metal, FluxOreBlock> makeOres() {
			ImmutableMap.Builder<Metal, FluxOreBlock> mb = new ImmutableMap.Builder<>();
			for (Metal metal : Metals.all()) {
				if (metal.notVanillaOrAlloy()) {
					FluxOreBlock b = new FluxOreBlock(metal);
					b.setRegistryName("flux", metal.metalName + "_ore");
					mb.put(metal, b);
				}
			}
			return mb.build();
		}

		private static Map<Metal, MetalBlock> makeBlocks() {
			ImmutableMap.Builder<Metal, MetalBlock> mb = new ImmutableMap.Builder<>();
			for (Metal metal : Metals.all()) {
				if (metal.nonVanilla()) {
					MetalBlock b = new MetalBlock(metal);
					b.setRegistryName("flux", metal.metalName + "_block");
					mb.put(metal, b);
				}
			}
			return mb.build();
		}
	}

	@SuppressWarnings("unused")
	public static final class I {
		public static final Map<Metal, MetalItem>
				GRITS = metalMap("grit", (m) -> m.nonAlloy() && m != Metals.NETHERITE),
				DUSTS = metalMap("dust", null),
				INGOTS = metalMap("ingot", Metal::nonVanilla),
				NUGGETS = metalMap("nugget", Metal::nonVanilla),
				GEARS = metalMap("gear", null),
				PLATES = metalMap("plate", null);
		public static final GiftItem GIFT = named(new GiftItem(props().maxStackSize(1)), "gift");
		public static final Item MACHINE_BASE = named(new Item(props()), "machine_base");
		public static final ChipItem CHIP = named(new ChipItem(props()), "chip");
		public static final FluxAdhesiveItem
				SEAL = named(new FluxAdhesiveItem(props()), "seal"),
				GLUE = named(new FluxAdhesiveItem(props()), "glue"),
				PASTE = named(new FluxAdhesiveItem(props()), "paste");

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
					.setTag("ingots/bronze")
					.setMaterial(INGOTS.get(Metals.BRONZE))
					.build();
			STEEL_TIER = b.setHarvestLevel(3)
					.setMaxUses(1500)
					.setEfficiency(8.5f)
					.setAttackDamage(3f)
					.setEnchantability(22)
					.setTag("ingots/steel")
					.setMaterial(INGOTS.get(Metals.STEEL))
					.build();
			BRONZE_TOOLS = new Toolset(BRONZE_TIER, "bronze");
			STEEL_TOOLS = new Toolset(STEEL_TIER, "steel");
		}

		private static Map<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
			ImmutableMap.Builder<Metal, MetalItem> mb = new ImmutableMap.Builder<>();
			Item.Properties p = props();
			for (Metal met : Metals.all()) {
				if (filter == null || filter.test(met)) {
					mb.put(met, named(new MetalItem(p, met), met.metalName + '_' + type));
				}
			}
			return mb.build();
		}

		private static Item.Properties props() {
			return new Item.Properties().group(FLUX_ITEMS);
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

		@SuppressWarnings("ConstantConditions")
		private static <T extends TileEntity> TileEntityType<T> tile(Supplier<T> f, Block b) {
			TileEntityType<T> type = new TileEntityType<>(f, Collections.singleton(b), null);
			type.setRegistryName(b.getRegistryName());
			return type;
		}

		@SuppressWarnings("ConstantConditions")
		private static <T extends TileEntity> FluxTileType<T> tile(Function<TileEntityType<T>, T> f, Block b) {
			FluxTileType<T> type = new FluxTileType<>(f, Collections.singleton(b), null);
			type.setRegistryName(b.getRegistryName());
			return type;
		}
	}

	public static final class C {
		public static final ContainerType<FluxGenContainer> FLUXGEN;
		public static final ContainerType<SignalControllerContainer> SIGNAL_CONTROLLER;
		public static final ContainerType<CopierContainer> COPIER;
		public static final ContainerType<OnlineMarketContainer> ONLINE_MARKET;
		public static final FluxContainerType<Machine2For1Container>
				GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR;

		static {
			FLUXGEN = container(FluxGenContainer::new, "fluxgen");
			SIGNAL_CONTROLLER = container(SignalControllerContainer::new, "signal_controller");
			COPIER = container(CopierContainer::new, "copier");
			ONLINE_MARKET = container(OnlineMarketContainer::new, "online_market");
			GRINDING_MILL = containerFlux(Machine2For1Container.make(R.GRINDING), "grinding_mill");
			ALLOY_CASTER = containerFlux(Machine2For1Container.make(R.ALLOYING), "alloy_caster");
			WASHER = containerFlux(Machine2For1Container.make(R.WASHING), "washer");
			COMPACTOR = containerFlux(Machine2For1Container.make(R.COMPACTING), "compactor");
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
	}

	public static final class R {
		public static final FluxRecipeType<GrindingRecipe> GRINDING = recipe("grinding", serializer(GrindingRecipe::new, "grinding"));
		public static final FluxRecipeType<AlloyingRecipe> ALLOYING = recipe("alloying", serializer(AlloyingRecipe::new, "alloying"));
		public static final FluxRecipeType<WashingRecipe> WASHING = recipe("washing", serializer(WashingRecipe::new, "washing"));
		public static final FluxRecipeType<CompactingRecipe> COMPACTING = recipe("compacting", serializer(CompactingRecipe::new, "compacting"));
		public static final FluxRecipeType<CopyingRecipe> COPYING = recipe("copying", CopyingRecipe.SERIALIZER);

		private static <T extends IRecipe<?>> FluxRecipeType<T> recipe(String key, IRecipeSerializer<T> ser) {
			return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new FluxRecipeType<>(key, ser));
		}

		private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(BiFunction<ResourceLocation, MachineRecipeSerializer.Builder, T> factory, String key) {
			MachineRecipeSerializer<T> mrs = new MachineRecipeSerializer<>(factory, 200);
			mrs.setRegistryName(MODID, key);
			return mrs;
		}
	}

	public static final class V {
		public static final PointOfInterestType
				FLUX_ENGINEER_POI = poi("flux_engineer", B.FLUXGEN);
		public static final VillagerProfession FLUX_ENGINEER = job("flux_engineer", FLUX_ENGINEER_POI, null);

		private static PointOfInterestType poi(String name, Block b) {
			return PointOfInterestType.registerBlockStates(
					new PointOfInterestType(MODID + ":" + name, ImmutableSet.copyOf(b.getStateContainer().getValidStates()), 1, 1)
							.setRegistryName(MODID, name)
			);
		}

		private static VillagerProfession job(String name, PointOfInterestType poi, SoundEvent sound) {
			return new VillagerProfession(MODID + ':' + name, poi, ImmutableSet.of(), ImmutableSet.of(), sound)
					.setRegistryName(MODID, name);
		}
	}

	public static final class Tags {
		public static final ITag.INamedTag<Block> DIGGER_SKIP = BlockTags.makeWrapperTag(MODID + ":digger_skip");
		public static final ITag.INamedTag<Item> MARKET_ACCEPT = ItemTags.makeWrapperTag(MODID + ":market_accept");
	}

	private static <T extends IForgeRegistryEntry<T>> void registerMapValues(Map<?, ? extends T> map, IForgeRegistry<T> reg) {
		map.values().forEach(reg::register);
	}

	private static <T extends IForgeRegistryEntry<T>> void registerFromClass(Class<?> cl, IForgeRegistry<T> reg) {
		ConsumerUtil.forEachStaticField(cl, reg.getRegistrySuperType(), reg::register);
	}

	@SuppressWarnings("ConstantConditions")
	private static BlockItem fromBlock(Block b) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(FLUX_BLOCKS));
		item.setRegistryName(b.getRegistryName());
		return item;
	}

	private static <T extends ForgeRegistryEntry<? super T>> T named(T t, String name) {
		t.setRegistryName(MODID, name);
		return t;
	}

	private static void recipeCompat(IRecipeType<?> rtype, Predicate<String> filter, String... compats) {
		List<String> compatList = new ArrayList<>(compats.length);
		for (String compat : compats) {
			if (filter.test(compat)) {
				compatList.add(compat);
			}
		}
		RecipeCompat.registerCompatRecipeTypes(rtype, compatList);
	}

	private F() {}
}
