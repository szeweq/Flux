package szewek.flux.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.fl.util.SideCached;
import szewek.flux.tile.EnergyCableTile;

import java.util.function.Function;

public class EnergyCache extends SideCached<IEnergyStorage> {

	public EnergyCache(TileEntity te) {
		super(getFromWorld(te));
	}

	private static Function<Direction, LazyOptional<IEnergyStorage>> getFromWorld(final TileEntity tile) {
		return dir -> {
			World w = tile.getWorld();
			assert w != null;
			TileEntity te = w.getTileEntity(tile.getPos().offset(dir));
			if (te == null) {
				return LazyOptional.empty();
			}
			if (te instanceof EnergyCableTile) {
				return ((EnergyCableTile) te).getSide(dir.getOpposite());
			}
			return te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
		};
	}
}
