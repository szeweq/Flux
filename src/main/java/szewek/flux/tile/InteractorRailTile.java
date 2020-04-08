package szewek.flux.tile;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import szewek.flux.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class InteractorRailTile extends TileEntity {
	private final Set<AbstractMinecartEntity> minecarts = new HashSet<>();

	public InteractorRailTile() {
		super(F.T.INTERACTOR_RAIL);
	}

	public void setMinecarts(Collection<AbstractMinecartEntity> col) {
		minecarts.clear();
		minecarts.addAll(col);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		minecarts.removeIf(cart -> !cart.isAlive());
		for (AbstractMinecartEntity minecart : minecarts) {
			LazyOptional<T> lazyOpt = minecart.getCapability(cap);
			if (lazyOpt.isPresent()) {
				return lazyOpt;
			}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
		super.remove();
		minecarts.clear();
	}
}
