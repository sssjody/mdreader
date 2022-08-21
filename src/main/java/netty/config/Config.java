package netty.config;

import netty.serialize.Serialize;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class Config {

    private static Properties properties;

    static {
        try ( InputStream inputStream = Config.class.getResourceAsStream("/applicationContext.properties")){
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Serialize.Algorithm getSerializeAlgorithm() {
        String algorithm = properties.getProperty("serialize.algorithm");
        return Optional.of(algorithm)
                .map(Serialize.Algorithm::valueOf)
                .orElse(Serialize.Algorithm.JDK);
    }
}
