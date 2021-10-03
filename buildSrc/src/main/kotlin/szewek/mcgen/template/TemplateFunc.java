package szewek.mcgen.template;

import szewek.mcgen.util.JsonFileWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public interface TemplateFunc {
    void process(Object o, JsonFileWriter out);

    static TemplateFunc byName(String name) throws NoSuchMethodException {
        Method method = Templates.class.getDeclaredMethod(name, Object.class, JsonFileWriter.class);
        method.setAccessible(true);
        return (o, out) -> {
            try {
                method.invoke(null, o, out);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };
    }
}
