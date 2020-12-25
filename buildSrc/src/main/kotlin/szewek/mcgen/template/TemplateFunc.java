package szewek.mcgen.template;

import com.google.gson.JsonElement;
import szewek.mcgen.util.JsonCreator;
import szewek.mcgen.util.JsonFileWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface TemplateFunc {
    void process(JsonElement o, JsonFileWriter out);

    static TemplateFunc byName(String name) throws NoSuchMethodException {
        Method method = Templates.class.getDeclaredMethod(name, JsonElement.class, JsonFileWriter.class);
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
