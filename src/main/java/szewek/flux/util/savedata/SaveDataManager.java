package szewek.flux.util.savedata;

import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveDataManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Class<?>, FieldHolder[]> classCache = new HashMap<>();

	private static FieldHolder[] createCache(Class<?> cl) {
		List<FieldHolder> holders = new ArrayList<>();
		Field[] flds = cl.getFields();
		for (Field f : flds) {
			Data ann = f.getAnnotation(Data.class);
			if (ann != null) {
				f.setAccessible(true);
				String name = ann.value();
				DataFunction df = null;
				Class<?> ft = f.getType();
				if (ft.equals(int.class) || ft.equals(Integer.class)) {
					df = INT;
				} else if (ft.equals(boolean.class) || ft.equals(Boolean.class)) {
					df = BOOLEAN;
				}
				holders.add(new FieldHolder(f, name, df));
			}
		}
		return holders.toArray(new FieldHolder[0]);
	}

	public static void read(Object it, CompoundNBT compound) {
		long ns = System.nanoTime();
		FieldHolder[] holders = classCache.computeIfAbsent(it.getClass(), SaveDataManager::createCache);
		for (FieldHolder fh : holders)
			try {
				fh.read(it, compound);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		LOGGER.info("Read took {} ns", System.nanoTime()-ns);
	}
	public static void write(Object it, CompoundNBT compound) {
		long ns = System.nanoTime();
		FieldHolder[] holders = classCache.computeIfAbsent(it.getClass(), SaveDataManager::createCache);
		for (FieldHolder fh : holders)
			try {
				fh.write(it, compound);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		LOGGER.info("Write took {} ns", System.nanoTime()-ns);
	}

	private static final DataFunction INT = new DataFunction() {
		@Override
		public void read(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException {
			compound.putInt(fh.name, fh.field.getInt(o));
		}

		@Override
		public void write(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException {
			fh.field.setInt(o, compound.getInt(fh.name));
		}
	};
	private static final DataFunction BOOLEAN = new DataFunction() {
		@Override
		public void read(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException {
			compound.putBoolean(fh.name, fh.field.getBoolean(o));
		}

		@Override
		public void write(FieldHolder fh, Object o, CompoundNBT compound) throws IllegalAccessException {
			fh.field.setBoolean(o, compound.getBoolean(fh.name));
		}
	};
}
