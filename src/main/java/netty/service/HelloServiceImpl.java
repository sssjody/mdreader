package netty.service;

public class HelloServiceImpl implements HelloService{

    @Override
    public String sayHello(String name) {
        return name + "你好";
    }
}
