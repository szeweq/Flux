package szewek.flux.util.savedata;

import net.minecraft.nbt.CompoundNBT;

import java.lang.reflect.Field;

final class FieldHolder {
	final Field field;
	final String name;
	private final DataFunction df;

	FieldHolder(Field field, String name, DataFunction df) {
		this.field = field;
		this.name = name;
		this.df = df;
	}

	void read(Object o, CompoundNBT compound) throws IllegalAccessException {
		if (df != null) {
			df.read(this, o, compound);
		}
	}
	void write(Object o, CompoundNBT compound) throws IllegalAccessException {
		if (df != null) {
			df.write(this, o, compound);
		}
	}
}
