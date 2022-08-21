package netty.protocol;

import com.google.gson.*;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

    // 反序列化
    @SneakyThrows
    @Override
    public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String clazz = jsonElement.getAsString();
        return Class.forName(clazz);
    }

    // 序列化
    @Override
    public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
        // 将 Class 变为 json
        return new JsonPrimitive(aClass.getName());
    }
}
