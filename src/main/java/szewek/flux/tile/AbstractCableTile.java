package szewek.flux.tile;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractCableTile<T, S extends AbstractCableTile.AbstractSide<T>> extends TileEntity implements ITickableTileEntity {
	private int cooldown;
	private final Capability<T> cap;
	protected final AtomicInteger sideFlag = new AtomicInteger();
	@SuppressWarnings({"unchecked"})
	protected final S[] sides = (S[]) new AbstractSide[6];

	public AbstractCableTile(TileEntityType<?> type, Capability<T> cap) {
		super(type);
		this.cap = cap;
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote) {
			if (--cooldown > 0) {
				return;
			}
			cooldown = 4;
			byte sf = (byte) (sideFlag.getAndSet(0) ^ 63);
			int i = 0;
			final Direction[] dirs = Direction.values();
			while (i < 6 && sf != 0) {
				if ((sf & 1) != 0) {
					updateSide(dirs[i]);
				}
				sf >>= 1;
				i++;
			}
		}
	}

	public LazyOptional<T> getSide(Direction dir) {
		return sides[dir.getIndex()].lazy;
	}

	protected abstract void updateSide(Direction dir);

	@Nonnull
	@Override
	public <X> LazyOptional<X> getCapability(@Nonnull Capability<X> cap, @Nullable Direction side) {
		if (!removed && cap == this.cap && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else {
			return super.getCapability(cap, side);
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (S s : sides) {
			s.lazy.invalidate();
		}
	}

	public static abstract class AbstractSide<T> implements NonNullSupplier<T> {
		private final byte bit;
		private final AtomicInteger sideFlag;
		protected final LazyOptional<T> lazy = LazyOptional.of(this);

		AbstractSide(int i, AtomicInteger sf) {
			bit = (byte) (1 << i);
			sideFlag = sf;
		}

		void update() {
			sideFlag.set(sideFlag.intValue() | bit);
		}
	}
}
