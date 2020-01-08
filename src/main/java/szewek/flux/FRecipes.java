package szewek.flux;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.recipe.*;

import static szewek.flux.FluxMod.MODID;

public final class FRecipes {
	public static IRecipeType<GrindingRecipe> GRINDING = addType("grinding");
	public static IRecipeType<AlloyingRecipe> ALLOYING = addType("alloying");
	public static IRecipeType<WashingRecipe> WASHING = addType("washing");
	public static IRecipeType<CompactingRecipe> COMPACTING = addType("compacting");

	public static MachineRecipeSerializer<GrindingRecipe> GRINDING_SERIALIZER = serializer(GrindingRecipe::new, 200);
	public static MachineRecipeSerializer<AlloyingRecipe> ALLOYING_SERIALIZER = serializer(AlloyingRecipe::new, 200);
	public static MachineRecipeSerializer<WashingRecipe> WASHING_SERIALIZER = serializer(WashingRecipe::new, 200);
	public static MachineRecipeSerializer<CompactingRecipe> COMPACTING_SERIALIZER = serializer(CompactingRecipe::new, 200);

	static void register(final IForgeRegistry<IRecipeSerializer<?>> reg) {
		reg.registerAll(
				GRINDING_SERIALIZER.setRegistryName(MODID, "grinding"),
				ALLOYING_SERIALIZER.setRegistryName(MODID, "alloying"),
				WASHING_SERIALIZER.setRegistryName(MODID, "washing"),
				COMPACTING_SERIALIZER.setRegistryName(MODID, "compacting")
		);
	}

	private static <T extends IRecipe<?>> IRecipeType<T> addType(final String key) {
		return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new IRecipeType<T>() {
			@Override
			public String toString() {
				return key;
			}
		});
	}

	private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(MachineRecipeSerializer.IFactory<T> factory, int process) {
		return new MachineRecipeSerializer<>(factory, process);
	}
}
