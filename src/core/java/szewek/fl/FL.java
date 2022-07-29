package szewek.fl;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import szewek.fl.finance.FinanceCapabilities;
import szewek.fl.recipe.CountedIngredient;
import szewek.fl.signal.SignalCapability;

/**
 * Main mod class
 */
@Mod(FL.ID)
public final class FL {
	public static final String ID = "fl";

	public FL() {}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void setup(final FMLCommonSetupEvent e) {
			SignalCapability.register();
			FinanceCapabilities.register();
			CraftingHelper.register(new ResourceLocation(ID, "counted"), CountedIngredient.Serializer.INSTANCE);
		}
	}
}
