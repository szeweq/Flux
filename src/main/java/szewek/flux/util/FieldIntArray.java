package szewek.flux.util;

import net.minecraft.util.IIntArray;

import java.lang.reflect.Field;

public class FieldIntArray implements IIntArray {
	private final Object obj;
	private final Field[] fields;

	public FieldIntArray(Object obj, Field[] fields) {
		this.obj = obj;
		this.fields = fields;
	}

	public static FieldIntArray of(Object obj, String[] names) {
		return of(obj, names, null);
	}

	public static FieldIntArray of(Object obj, String[] names, Extended ext) {
		final Field[] fields = new Field[names.length];
		Class<?> cl = obj.getClass();
		for (int i = 0; i < names.length; i++) {
			Field f;
			try {
				f = cl.getDeclaredField(names[i]);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("Wrong fields list", e);
			}
			f.setAccessible(true);
			fields[i] = f;
		}
		return ext != null ? new Hybrid(obj, fields, ext) : new FieldIntArray(obj, fields);
	}

	@Override
	public int get(int index) {
		try {
			return fields[index].getInt(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void set(int index, int value) {
		try {
			fields[index].setInt(obj, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
		return fields.length;
	}

	public interface Extended extends IIntArray {
		int translate(int index);
	}

	static class Hybrid extends FieldIntArray {
		private final Extended ext;

		public Hybrid(Object obj, Field[] fields, Extended ext) {
			super(obj, fields);
			this.ext = ext;
		}

		@Override
		public int get(int index) {
			int idx = ext.translate(index);
			if (idx < 0) {
				return ext.get(index);
			}
			return super.get(idx);
		}

		@Override
		public void set(int index, int value) {
			int idx = ext.translate(index);
			if (idx < 0) {
				ext.set(index, value);
				return;
			}
			super.set(idx, value);
		}

		@Override
		public int getCount() {
			return ext.getCount();
		}
	}
}
