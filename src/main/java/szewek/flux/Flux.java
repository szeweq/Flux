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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import szewek.flux.data.FluxData;
import szewek.flux.data.FluxGenValues;
import szewek.flux.data.Gifts;
import szewek.flux.energy.FurnaceEnergy;
import szewek.flux.network.FluxPackets;
import szewek.flux.signal.MinecartSignals;

@Mod(Flux.MODID)
public final class Flux {
	public static final String MODID = "flux";
	private static IModInfo modInfo;

	public Flux() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		FluxCfg.addConfig(ModLoadingContext.get());
		modEventBus.register(FluxCfg.class);
		modEventBus.addListener(Flux::setup);
		modEventBus.register(F.class);
	}

	private static void setup(final FMLCommonSetupEvent e) {
		modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
		FluxPackets.init();

		F.features();
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
		private static final ResourceLocation FURNACE_CAP = F.loc("furnace_energy");
		private static final ResourceLocation CART_CAP = F.loc("minecart_signal");

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

		@SubscribeEvent
		public static void updateTags(final TagsUpdatedEvent e) {
			FluxGenValues.updateValues();
		}

		@SubscribeEvent
		public static void loadBiomes(final BiomeLoadingEvent e) {
			if (!FluxCfg.COMMON.disableOres.get()) {
				Biome.Category bcat = e.getCategory();
				if (bcat != Biome.Category.THEEND && bcat != Biome.Category.NETHER) {
					e.getGeneration()
							.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, F.W.COPPER_ORES)
							.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, F.W.TIN_ORES);
				}
			}
		}
	}
}
