package szewek.flux;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import szewek.flux.energy.FurnaceEnergy;
import szewek.flux.item.MetalItem;
import szewek.flux.util.Metal;
import szewek.flux.util.gift.GiftData;
import szewek.flux.util.gift.Gifts;

import java.util.Calendar;

@Mod(MCFlux.MODID)
public final class MCFlux {
	static final String MODID = "flux";

	public MCFlux() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
	}

	public static ResourceLocation location(final String name) {
		return new ResourceLocation(MODID, name);
	}

	private void setup(final FMLCommonSetupEvent event) {
		ForgeRegistries.BIOMES.getValues().forEach(biome -> {
			final Biome.Category cat = biome.getCategory();
			if (cat != Biome.Category.NETHER && cat != Biome.Category.THEEND) {

				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Feature.ORE.func_225566_b_(new OreFeatureConfig(
								OreFeatureConfig.FillerBlockType.NATURAL_STONE,
								MFBlocks.ORES.get(Metal.COPPER).getDefaultState(),
								7
						)).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(new CountRangeConfig(20, 0, 0, 96)))
				);
				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Feature.ORE.func_225566_b_(new OreFeatureConfig(
								OreFeatureConfig.FillerBlockType.NATURAL_STONE,
								MFBlocks.ORES.get(Metal.TIN).getDefaultState(),
								7
						)).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(new CountRangeConfig(20, 0, 0, 72)))
				);
			}
		});
	}

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	@OnlyIn(Dist.CLIENT)
	public static final class ClientEvents {
		@SubscribeEvent
		public static void setupClient(final FMLClientSetupEvent event) {
			Minecraft mc = event.getMinecraftSupplier().get();
			mc.getItemColors().register(Gifts::colorByGift, MFItems.GIFT);

			ItemColors ic = mc.getItemColors();
			ic.register(Metal::gritColors, MFItems.GRITS.values().toArray(new MetalItem[0]));
			ic.register(Metal::itemColors, MFItems.DUSTS.values().toArray(new MetalItem[0]));
			ic.register(Metal::ingotColors, MFItems.INGOTS.values().toArray(new MetalItem[0]));
		}
	}


	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static final class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRE) {
			MFBlocks.register(blockRE.getRegistry());
		}

		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRE) {
			MFItems.register(itemRE.getRegistry());
		}

		@SubscribeEvent
		public static void onTilesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileRE) {
			MFTiles.register(tileRE.getRegistry());
		}

		@SubscribeEvent
		public static void onContainersRegistry(final RegistryEvent.Register<ContainerType<?>> containerRE) {
			MFContainers.register(containerRE.getRegistry());
		}

		@SubscribeEvent
		public static void onRecipesRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> recipeRE) {
			MFRecipes.register(recipeRE.getRegistry());
		}

		@SubscribeEvent
		public static <T extends IForgeRegistryEntry<T>> void fixMissingMappings(final RegistryEvent.MissingMappings<T> mm) {
			System.out.println("FIXING MAPPINGS...");
			mm.getAllMappings().forEach(m -> {
				ResourceLocation key = m.key;
				if (key.getNamespace().equals("mcflux")) {
					T o = m.registry.getValue(new ResourceLocation(MODID, key.getPath()));
					if (o != null) m.remap(o);
				}
			});
		}
	}

	@Mod.EventBusSubscriber
	public static final class CapabilityEvents {
		private static final ResourceLocation FURNACE_CAP = new ResourceLocation(MODID, "furnace_energy");

		@SubscribeEvent
		public static void wrapTile(final AttachCapabilitiesEvent<TileEntity> e) {
			TileEntity te = e.getObject();
			if (te instanceof AbstractFurnaceTileEntity) {
				e.addCapability(FURNACE_CAP, new FurnaceEnergy((AbstractFurnaceTileEntity) te));
			}
		}
	}

	@Mod.EventBusSubscriber
	public static final class Events {

		@SubscribeEvent
		public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent pe) {
			PlayerEntity player = pe.getPlayer();
			if (!player.world.isRemote) {
				CompoundNBT data = player.getPersistentData();
				int lastXDay = data.getInt("lastXDay");
				int lastXYear = data.getInt("lastXYear");
				Calendar calendar = Calendar.getInstance();
				int xday = (1 + calendar.get(Calendar.MONTH)) * 32 + calendar.get(Calendar.DAY_OF_MONTH);
				int xyear = calendar.get(Calendar.YEAR);
				if (lastXYear < xyear)
					lastXDay = 0;
				if (lastXDay < xday) {
					GiftData gd = Gifts.get(xday);
					if (gd != null) {
						data.putInt("lastXDay", xday);
						data.putInt("lastXYear", xyear);
						CompoundNBT itemTag = new CompoundNBT();
						itemTag.putInt("xDay", xday);
						ItemStack giftStack = new ItemStack(MFItems.GIFT, 1);
						giftStack.setTag(itemTag);
						ItemHandlerHelper.giveItemToPlayer(player, giftStack, -1);
					}
				}
			}
		}
	}
}
