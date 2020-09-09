package szewek.flux.tile.part;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public abstract class EnergyPart extends SelfLazy<IEnergyStorage> implements IEnergyStorage {
    protected final int max;
    protected int stored;

    protected EnergyPart(int max) {
        this.max = max;
    }

    public final int getEnergy16Bit(boolean low) {
        int v = low ? stored & 0xFFFF : stored >> 16;
        return v;
    }

    public final void setEnergy16Bit(boolean low, int v) {
        stored = low ? (stored & 0xFFFF0000) + v : (stored & 0xFFFF) + (v << 16);
    }

    public void readNBT(CompoundNBT nbt) {
        stored = MathHelper.clamp(nbt.getInt("E"), 0, max);
    }

    public void writeNBT(CompoundNBT nbt) {
        nbt.putInt("E", stored);
    }

    @Override
    public final int getEnergyStored() {
        return stored;
    }

    @Override
    public final int getMaxEnergyStored() {
        return max;
    }

    @Nonnull
    @Override
    public final IEnergyStorage get() {
        return this;
    }
}
