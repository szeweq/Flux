package szewek.flux.tile.cable;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import szewek.flux.tile.part.SelfLazy;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractSide<T> extends SelfLazy<T> {
	private final byte bit;
	private final AtomicInteger sideFlag;

	AbstractSide(int i, AtomicInteger sf) {
		bit = (byte) (1 << i);
		sideFlag = sf;
	}

	void update() {
		sideFlag.set(sideFlag.intValue() | bit);
	}
}
