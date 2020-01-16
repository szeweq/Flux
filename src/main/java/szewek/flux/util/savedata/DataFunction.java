package szewek.flux.util.savedata;

import net.minecraft.nbt.CompoundNBT;

interface DataFunction {
	void read(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException;
	void write(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException;
}
