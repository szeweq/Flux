package szewek.fl.util;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Set;

public final class ValueToIDMap<T> {
	private final Object2IntMap<T> toID = new Object2IntLinkedOpenHashMap<>();

	public int get(T t) {
		if (toID.containsKey(t)) return toID.getInt(t);
		int v = toID.size();
		toID.put(t, v);
		return v;
	}

	public Set<T> values() {
		return toID.keySet();
	}
}
