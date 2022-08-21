import netty.config.Config;
import netty.message.RpcRequestMessage;
import netty.serialize.Serialize;
import org.junit.Test;

public class TestConfig {
    @Test
    public void testGetSerializeAlgorithm() {
        Serialize.Algorithm algorithm = Config.getSerializeAlgorithm();
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "netty.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );
        byte[] bytes = algorithm.serialize(message);
        System.out.println(algorithm.deserialize(RpcRequestMessage.class, bytes));
    }
}
