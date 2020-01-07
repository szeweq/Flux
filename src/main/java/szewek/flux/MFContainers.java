package szewek.flux;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.container.*;
import szewek.flux.gui.*;

import static szewek.flux.MCFlux.MODID;

public final class MFContainers {
	public static final ContainerType<FluxGenContainer> FLUXGEN = create(FluxGenContainer::new, FluxGenScreen::new);
	public static final ContainerType<GrindingMillContainer> GRINDING_MILL = create(GrindingMillContainer::new, MachineScreen.make("grindable", "grinding_mill"));
	public static final ContainerType<AlloyCasterContainer> ALLOY_CASTER = create(AlloyCasterContainer::new, MachineScreen.make("alloyable", "alloy_caster"));
	public static final ContainerType<WasherContainer> WASHER = create(WasherContainer::new, MachineScreen.make("washable", "washer"));
	public static final ContainerType<CompactorContainer> COMPACTOR = create(CompactorContainer::new, MachineScreen.make("compactable", "compactor"));

	static void register(final IForgeRegistry<ContainerType<?>> reg) {
		reg.registerAll(
				FLUXGEN.setRegistryName(MODID, "fluxgen"),
				GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				WASHER.setRegistryName(MODID, "washer"),
				COMPACTOR.setRegistryName(MODID, "compactor")
		);
	}

	private static <M extends Container, U extends Screen & IHasContainer<M>> ContainerType<M> create(IContainerFactory<M> factory, ScreenManager.IScreenFactory<M, U> screenFactory) {
		ContainerType<M> cont = new ContainerType<>(factory);
		if (screenFactory != null) ScreenManager.registerFactory(cont, screenFactory);
		return cont;
	}
}
