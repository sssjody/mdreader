package netty.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.message.RpcRequestMessage;
import netty.message.RpcResponseMessage;
import netty.service.ServiceFactory;

import java.lang.reflect.Method;

@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequestMessage rpcRequestMessage) {
        RpcResponseMessage rspMessage = new RpcResponseMessage();;
        try {
            Object interfaceImpl = ServiceFactory.getInterfaceImpl(Class.forName(rpcRequestMessage.getInterfaceName()));
            Method method = interfaceImpl.getClass().getMethod(rpcRequestMessage.getMethodName(),
                    rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(interfaceImpl, rpcRequestMessage.getParameterValue());
            rspMessage.setReturnValue(invoke);
        } catch (Exception e) {
            rspMessage.setExceptionValue(e);
        }
        channelHandlerContext.writeAndFlush(rspMessage);
    }
}
