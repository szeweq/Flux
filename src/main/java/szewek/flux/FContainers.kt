package szewek.flux

import net.minecraft.client.gui.IHasContainer
import net.minecraft.client.gui.ScreenManager
import net.minecraft.client.gui.ScreenManager.IScreenFactory
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.IContainerFactory
import net.minecraftforge.registries.IForgeRegistry
import szewek.flux.container.*
import szewek.flux.gui.FluxGenScreen
import szewek.flux.gui.MachineScreen

object FContainers {
    @JvmField val FLUXGEN = create(::FluxGenContainer, IScreenFactory<FluxGenContainer, FluxGenScreen> { screenContainer, inv, titleIn -> FluxGenScreen(screenContainer, inv, titleIn) })
    @JvmField val GRINDING_MILL = create(::GrindingMillContainer, "grindable", "grinding_mill")
    @JvmField val ALLOY_CASTER = create(::AlloyCasterContainer, "alloyable", "alloy_caster")
    @JvmField val WASHER = create(::WasherContainer, "washable", "washer")
    @JvmField val COMPACTOR = create(::CompactorContainer, "compactable", "compactor")

    @JvmStatic
    fun register(reg: IForgeRegistry<ContainerType<*>?>) {
        reg.registerAll(
                FLUXGEN.setRegistryName(FluxMod.MODID, "fluxgen"),
                GRINDING_MILL.setRegistryName(FluxMod.MODID, "grinding_mill"),
                ALLOY_CASTER.setRegistryName(FluxMod.MODID, "alloy_caster"),
                WASHER.setRegistryName(FluxMod.MODID, "washer"),
                COMPACTOR.setRegistryName(FluxMod.MODID, "compactor")
        )
    }

    private fun <M : Container, U> create(factory: (Int, PlayerInventory, PacketBuffer) -> M, screenFactory: IScreenFactory<M, U>?): ContainerType<M> where U : Screen, U : IHasContainer<M> {
        val cont = ContainerType(IContainerFactory<M>(factory))
        if (screenFactory != null) ScreenManager.registerFactory(cont, screenFactory)
        return cont
    }

    private fun <M : AbstractMachineContainer, U : MachineScreen<M>> create(factory: (Int, PlayerInventory, PacketBuffer) -> M, showType: String, title: String): ContainerType<M> {
        val cont = ContainerType(IContainerFactory(factory))
        ScreenManager.registerFactory(cont, MachineScreen.make<M>(showType, title))
        return cont
    }
}