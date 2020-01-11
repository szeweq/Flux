package szewek.flux

import net.minecraft.block.Block
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.AbstractFurnaceTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.placement.CountRangeConfig
import net.minecraft.world.gen.placement.Placement
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.RegistryEvent.MissingMappings
import net.minecraftforge.event.RegistryEvent.Register
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.VersionChecker
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.registries.ForgeRegistries
import szewek.flux.FBlocks.register
import szewek.flux.FContainers.register
import szewek.flux.FItems.register
import szewek.flux.FRecipes.register
import szewek.flux.FTiles.register
import szewek.flux.FluxMod
import szewek.flux.energy.FurnaceEnergy
import szewek.flux.util.MappingFixer
import szewek.flux.util.Metal
import szewek.flux.util.gift.Gifts
import szewek.flux.util.gift.Gifts.get
import szewek.fluxkt.KtModLoadingContext
import java.util.*
import java.util.function.Consumer

@Mod(FluxMod.MODID)
object FluxMod {
    const val MODID = "flux"
    var modInfo: IModInfo? = null

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    object CommonEvents {
        @SubscribeEvent
        fun setup(event: FMLCommonSetupEvent) {
            modInfo = ModLoadingContext.get().activeContainer.modInfo

            ForgeRegistries.BIOMES.values.forEach(Consumer { biome: Biome ->
                val cat = biome.category
                if (cat != Biome.Category.NETHER && cat != Biome.Category.THEEND) {
                    biome.addFeature(
                            GenerationStage.Decoration.UNDERGROUND_ORES,
                            Feature.ORE.func_225566_b_(OreFeatureConfig(
                                    OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                    FBlocks.ORES[Metal.COPPER]!!.defaultState,
                                    7
                            )).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(CountRangeConfig(20, 0, 0, 96)))
                    )
                    biome.addFeature(
                            GenerationStage.Decoration.UNDERGROUND_ORES,
                            Feature.ORE.func_225566_b_(OreFeatureConfig(
                                    OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                    FBlocks.ORES[Metal.TIN]!!.defaultState,
                                    7
                            )).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(CountRangeConfig(20, 0, 0, 72)))
                    )
                }
            })

        }
    }


    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientEvents {
        @SubscribeEvent
        fun setupClient(event: FMLClientSetupEvent) {
            val mc = event.minecraftSupplier.get()
            val ic = mc.itemColors
            ic.register(IItemColor(Gifts::colorByGift), FItems.GIFT)
            ic.register(IItemColor(Metal.Companion::gritColors), *FItems.GRITS.values.toTypedArray())
            ic.register(IItemColor(Metal.Companion::itemColors), *FItems.DUSTS.values.toTypedArray())
            ic.register(IItemColor(Metal.Companion::ingotColors), *FItems.INGOTS.values.toTypedArray())
        }
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    object RegistryEvents {
        @SubscribeEvent
        fun onBlocksRegistry(blockRE: Register<Block?>) {
            register(blockRE.registry)
        }

        @SubscribeEvent
        fun onItemsRegistry(itemRE: Register<Item?>) {
            register(itemRE.registry)
        }

        @SubscribeEvent
        fun onTilesRegistry(tileRE: Register<TileEntityType<*>?>) {
            register(tileRE.registry)
        }

        @SubscribeEvent
        fun onContainersRegistry(containerRE: Register<ContainerType<*>?>) {
            register(containerRE.registry)
        }

        @SubscribeEvent
        fun onRecipesRegistry(recipeRE: Register<IRecipeSerializer<*>?>) {
            register(recipeRE.registry)
        }

        @SubscribeEvent
        fun missingBlockMappings(mm: MissingMappings<Block?>) {
            mm.allMappings.forEach(MappingFixer::fixMapping)
        }

        @SubscribeEvent
        fun missingItemMappings(mm: MissingMappings<Item?>) {
            mm.allMappings.forEach(MappingFixer::fixMapping)
        }
    }

    @Mod.EventBusSubscriber
    object CapabilityEvents {
        private val FURNACE_CAP = ResourceLocation(MODID, "furnace_energy")
        @SubscribeEvent
        fun wrapTile(e: AttachCapabilitiesEvent<TileEntity?>) {
            val te = e.getObject()
            if (te is AbstractFurnaceTileEntity) {
                e.addCapability(FURNACE_CAP, FurnaceEnergy((te as AbstractFurnaceTileEntity?)!!))
            }
        }
    }

    @Mod.EventBusSubscriber
    object Events {
        @SubscribeEvent
        fun onPlayerLogin(pe: PlayerLoggedInEvent) {
            val player = pe.player
            if (!player.world.isRemote) {
                val ver = VersionChecker.getResult(modInfo)
                if (ver.target != null && (ver.status == VersionChecker.Status.OUTDATED || ver.status == VersionChecker.Status.BETA_OUTDATED)) {
                    player.sendMessage(TranslationTextComponent("flux.update", ver.target.toString()))
                }
                val data = player.persistentData
                var lastXDay = data.getInt("lastXDay")
                val lastXYear = data.getInt("lastXYear")
                val calendar = Calendar.getInstance()
                val xday = (1 + calendar[Calendar.MONTH]) * 32 + calendar[Calendar.DAY_OF_MONTH]
                val xyear = calendar[Calendar.YEAR]
                if (lastXYear < xyear) lastXDay = 0
                if (lastXDay < xday) {
                    val gd = get(xday)
                    if (gd != null) {
                        data.putInt("lastXDay", xday)
                        data.putInt("lastXYear", xyear)
                        val itemTag = CompoundNBT()
                        itemTag.putInt("xDay", xday)
                        val giftStack = ItemStack(FItems.GIFT, 1)
                        giftStack.tag = itemTag
                        ItemHandlerHelper.giveItemToPlayer(player, giftStack, -1)
                    }
                }
            }
        }
    }

    fun location(name: String): ResourceLocation {
        return ResourceLocation(MODID, name)
    }
}
