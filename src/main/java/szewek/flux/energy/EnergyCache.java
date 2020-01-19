package szewek.flux.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.tile.EnergyCableTile;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class EnergyCache {
	private final EnumMap<Direction, LazyOptional<IEnergyStorage>> cache = new EnumMap<>(Direction.class);

	@Nullable
	public IEnergyStorage getCached(Direction dir, World world, BlockPos pos) {
		LazyOptional<IEnergyStorage> lazy = cache.get(dir);
		if (lazy == null) {
			assert world != null;
			TileEntity te = world.getTileEntity(pos.offset(dir));
			if (te == null) return null;
			if (te instanceof EnergyCableTile)
				lazy = ((EnergyCableTile) te).getLazySide(dir.getOpposite());
			else
				lazy = te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
			if (lazy.isPresent()) {
				lazy.addListener(l -> cache.remove(dir));
				cache.put(dir, lazy);
			}
		}
		return lazy.orElse(null);
	}
}
