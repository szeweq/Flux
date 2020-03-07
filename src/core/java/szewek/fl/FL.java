package szewek.fl;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import szewek.fl.network.FluxPlus;
import szewek.fl.recipe.CountedIngredient;

/**
 * Main mod class
 */
@Mod(FL.ID)
public final class FL {
	public static final String ID = "fl";

	public FL() {
		MinecraftForge.EVENT_BUS.register(Events.class);
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void setup(final FMLCommonSetupEvent e) {
			FluxPlus.putAction("start");
			CraftingHelper.register(new ResourceLocation(ID, "counted"), CountedIngredient.Serializer.INSTANCE);
		}
	}

	static class Events {
		@SubscribeEvent
		public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent e) {
			FluxPlus.putAction("login");
		}

		@SubscribeEvent
		public static void stop(final FMLServerStoppedEvent e) {
			FluxPlus.putAction("exit");
		}
	}
}
