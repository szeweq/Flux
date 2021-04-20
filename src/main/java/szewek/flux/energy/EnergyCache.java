package szewek.flux.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.fl.util.SideCached;
import szewek.flux.tile.cable.EnergyCableTile;

public final class EnergyCache extends SideCached<IEnergyStorage> {
	public EnergyCache(final TileEntity tile) {
		super(dir -> {
			World w = tile.getLevel();
			assert w != null;
			TileEntity te = w.getBlockEntity(tile.getBlockPos().relative(dir));
			if (te == null) {
				return LazyOptional.empty();
			}
			if (te instanceof EnergyCableTile) {
				return ((EnergyCableTile) te).getSide(dir.getOpposite());
			}
			return te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
		});
	}
}
