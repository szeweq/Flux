package szewek.flux.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.tile.EnergyCableTile;

import javax.annotation.Nullable;

public class EnergyCache {
	@SuppressWarnings("unchecked")
	private final LazyOptional<IEnergyStorage>[] cacheArray = (LazyOptional<IEnergyStorage>[]) new LazyOptional[6];
	private final TileEntity tile;

	public EnergyCache(TileEntity te) {
		tile = te;
	}

	@Nullable
	public IEnergyStorage getCached(Direction dir) {
		final int d = dir.ordinal();
		LazyOptional<IEnergyStorage> lazy = cacheArray[d];
		if (lazy == null) {
			World w = tile.getWorld();
			assert w != null;
			TileEntity te = w.getTileEntity(tile.getPos().offset(dir));
			if (te == null) return null;
			if (te instanceof EnergyCableTile)
				lazy = ((EnergyCableTile) te).getSide(dir.getOpposite());
			else
				lazy = te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
			if (lazy.isPresent()) {
				lazy.addListener(l -> cacheArray[d] = null);
				cacheArray[d] = lazy;
			}
		}
		return lazy.orElse(null);
	}

	public void clear() {
		for (int i = 0; i < 6; i++) cacheArray[i] = null;
	}
}
