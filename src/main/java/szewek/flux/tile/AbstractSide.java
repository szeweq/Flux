package szewek.flux.tile;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractSide<T> implements NonNullSupplier<T> {
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
