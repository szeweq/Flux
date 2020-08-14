package szewek.flux;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.flux.energy.FurnaceEnergy;
import szewek.flux.network.FluxPackets;
import szewek.flux.signal.MinecartSignals;
import szewek.flux.data.FluxData;
import szewek.flux.util.Gifts;
import szewek.flux.util.metals.Metal;
import szewek.flux.util.metals.Metals;

@Mod(Flux.MODID)
public final class Flux {
	public static final String MODID = "flux";
	private static IModInfo modInfo;

	public Flux() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FluxCfg.commonSpec);
		modEventBus.register(FluxCfg.class);
		modEventBus.addListener(Flux::setup);
		modEventBus.register(F.class);
	}

	private static void setup(final FMLCommonSetupEvent e) {
		modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
		FluxPackets.init();

		if (!FluxCfg.COMMON.disableOres.get()) {
			for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
				Biome.Category cat = biome.getCategory();
				if (cat != Biome.Category.NETHER && cat != Biome.Category.THEEND) {
					addOreGen(biome, Metals.COPPER, new CountRangeConfig(20, 0, 0, 96));
					addOreGen(biome, Metals.TIN, new CountRangeConfig(20, 0, 0, 72));
				}
			}
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	final static class ClientEvents {
		@SubscribeEvent
		public static void setupClient(final FMLClientSetupEvent e) {
			F.client(e.getMinecraftSupplier().get());
		}
	}

	@Mod.EventBusSubscriber
	final static class Events {
		private static final ResourceLocation FURNACE_CAP = new ResourceLocation(MODID, "furnace_energy");
		private static final ResourceLocation CART_CAP = new ResourceLocation(MODID, "minecart_signal");

		@SubscribeEvent
		public static void wrapTile(final AttachCapabilitiesEvent<TileEntity> e) {
			TileEntity te = e.getObject();
			if (te instanceof AbstractFurnaceTileEntity) {
				e.addCapability(FURNACE_CAP, new FurnaceEnergy((AbstractFurnaceTileEntity) te));
			}
		}

		@SubscribeEvent
		public static void wrapEntity(final AttachCapabilitiesEvent<Entity> e) {
			Entity ent = e.getObject();
			if (ent instanceof AbstractMinecartEntity) {
				MinecartSignals minecartSignals = new MinecartSignals();
				e.addCapability(CART_CAP, minecartSignals);
				e.addListener(minecartSignals::invalidate);
			}
		}

		@SubscribeEvent
		public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent pe) {
			PlayerEntity player = pe.getPlayer();
			if (!player.world.isRemote) {
				VersionChecker.CheckResult ver = VersionChecker.getResult(modInfo);
				if (ver.target != null && (ver.status == VersionChecker.Status.OUTDATED || ver.status == VersionChecker.Status.BETA_OUTDATED)) {
					player.sendMessage(new TranslationTextComponent("flux.update", ver.target.toString()), Util.DUMMY_UUID);
				}
				Gifts.makeGiftsForPlayer((ServerPlayerEntity) player);
			}
		}

		@SubscribeEvent
		public static void reloadData(final AddReloadListenerEvent e) {
			FluxData.addReloadListeners(e);
		}
	}

	private static void addOreGen(Biome biome, Metal metal, CountRangeConfig cfg) {
		biome.addFeature(
				GenerationStage.Decoration.UNDERGROUND_ORES,
				Feature.ORE.withConfiguration(new OreFeatureConfig(
						OreFeatureConfig.FillerBlockType.NATURAL_STONE,
						F.B.ORES.get(metal).getDefaultState(),
						7
				)).withPlacement(Placement.COUNT_RANGE.configure(cfg))
		);
	}
}
