package szewek.fl;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import szewek.fl.finance.FinanceCapabilities;
import szewek.fl.network.FluxAnalytics;
import szewek.fl.network.FluxAnalytics2;
import szewek.fl.network.NetCommon;
import szewek.fl.recipe.CountedIngredient;
import szewek.fl.signal.SignalCapability;

/**
 * Main mod class
 */
@Mod(FL.ID)
public final class FL {
	public static final String ID = "fl";

	public FL() {
		MinecraftForge.EVENT_BUS.register(Events.class);
		NetCommon.init();
		FluxAnalytics2.init();
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void setup(final FMLCommonSetupEvent e) {
			IModInfo modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
			String modVersion = modInfo.getVersion().toString();
			NetCommon.updateVersion(modVersion);
			NetCommon.putEvent("fl/setup", "fl:setup");
			SignalCapability.register();
			FinanceCapabilities.register();
			CraftingHelper.register(new ResourceLocation(ID, "counted"), CountedIngredient.Serializer.INSTANCE);
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	final static class ClientEvents {
		@SubscribeEvent
		public static void setupClient(final FMLClientSetupEvent e) {
			FluxAnalytics2.userEvent();
		}
	}

	static class Events {
		@SubscribeEvent
		public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent e) {
			NetCommon.putEvent(e.getPlayer(), "fl/login", "fl:login");
		}
		@SubscribeEvent
		public static void playerLogout(final PlayerEvent.PlayerLoggedOutEvent e) {
			NetCommon.putEvent(e.getPlayer(), "fl/logout", "fl:logout");
		}
	}
}
