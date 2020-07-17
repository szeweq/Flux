package szewek.flux.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public final class JavaUtils {
	private JavaUtils() {}

	public static <T> void forEachStaticField(Class<?> cl, Class<T> typ, Consumer<T> fn) {
		for (Field f : cl.getDeclaredFields()) {
			if (typ.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
				try {
					fn.accept(typ.cast(f.get(null)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> void forEachValue(Map<?, ? extends T> map, Consumer<T> fn) {
		final Collection<? extends T> vals = map.values();
		for (T t : vals) {
			fn.accept(t);
		}
	}
}
