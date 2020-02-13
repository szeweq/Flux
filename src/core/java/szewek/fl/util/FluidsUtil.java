package szewek.fl.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class FluidsUtil {
	public static void saveAllFluids(CompoundNBT tag, List<FluidStack> fluids, boolean saveEmpty) {
		ListNBT fluidNBT = new ListNBT();
		for (int i = 0; i < fluids.size(); i++) {
			FluidStack fs = fluids.get(i);
			if (fs != null && !fs.isEmpty()) {
				CompoundNBT nbt = new CompoundNBT();
				nbt.putByte("Slot", (byte) i);
				fs.writeToNBT(nbt);
				fluidNBT.add(nbt);
			}
		}

		if (!fluidNBT.isEmpty() || saveEmpty) {
			tag.put("Fluids", fluidNBT);
		}
	}

	public static void loadAllFluids(CompoundNBT tag, List<FluidStack> fluids) {
		ListNBT fluidNBT = tag.getList("Fluids", 10);
		for (int i = 0; i < fluidNBT.size(); i++) {
			CompoundNBT nbt = fluidNBT.getCompound(i);
			int j = nbt.getByte("Slot") & 255;
			if (j >= 0 && j < fluids.size()) {
				fluids.set(j, FluidStack.loadFluidStackFromNBT(nbt));
			}
		}
	}
}
