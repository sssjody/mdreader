package netty.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import netty.protocol.ClassCodec;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public interface Serialize {
    /**
     * 序列化算法
     */
    <T> byte[] serialize(T object);

    /**
     * 反序列化算法
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    enum Algorithm implements Serialize{

        JDK {
            @Override
            public <T> byte[] serialize(T object) {
                return SerializationUtils.serialize((Serializable) object);
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return SerializationUtils.deserialize(bytes);
            }
        },

        JSON {
            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder().
                        registerTypeAdapter(Class.class, new ClassCodec()).create();
                return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder().
                        registerTypeAdapter(Class.class, new ClassCodec()).create();
                return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
            }
        }
    }
}
