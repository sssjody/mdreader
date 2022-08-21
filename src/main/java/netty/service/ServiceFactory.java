package netty.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceFactory {
    private static Properties properties;
    private static Map<Class<?>, Object> classToImplMap = new ConcurrentHashMap<>();

    static {
        try {
            InputStream in = ServiceFactory.class.getResourceAsStream("/applicationContext.properties");
            properties = new Properties();
            properties.load(in);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if (name.endsWith("Service")) {
                    Class<?> interfaceClazz = Class.forName(name);
                    Object interfaceImplClazz = Class.forName(properties.getProperty(name)).newInstance();
                    classToImplMap.put(interfaceClazz, interfaceImplClazz);
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getInterfaceImpl(Class<?> interfaceClazz) {
        return classToImplMap.get(interfaceClazz);
    }
}
