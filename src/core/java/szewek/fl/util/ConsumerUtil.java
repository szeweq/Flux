package szewek.fl.util;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public final class ConsumerUtil {
    private ConsumerUtil() {}

    public static <T> void forEachStaticField(Class<?> cl, Class<T> typ, Consumer<T> fn) {
        for (Field f : cl.getDeclaredFields()) {
            if (!typ.isAssignableFrom(f.getType()) || !Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            try {
                fn.accept(typ.cast(f.get(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addPlayerSlotsAt(PlayerInventory pinv, int x, int y, Consumer<Slot> fn) {
        int xBase = x;
        int i;

        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                fn.accept(new Slot(pinv, 9 * i + j + 9, x, y));
                x += 18;
            }
            x = xBase;
            y += 18;
        }

        y += 4;
        for(i = 0; i < 9; ++i) {
            fn.accept(new Slot(pinv, i, x, y));
            x += 18;
        }
    }
}
