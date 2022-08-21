package netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import netty.message.RpcRequestMessage;
import netty.protocol.MessageCodec;
import netty.protocol.ProtocolLengthDecoder;
import netty.server.handler.RpcResponseMessageHandler;

public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                              ch.pipeline()
                                      .addLast(new ProtocolLengthDecoder())
                                      .addLast(new MessageCodec())
                                      .addLast(new LoggingHandler())
                                      .addLast(new RpcResponseMessageHandler())
                                      .addLast(new ChannelInboundHandlerAdapter(){
                                          @Override
                                          public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                              RpcRequestMessage message = new RpcRequestMessage(
                                                      1,
                                                      "netty.service.HelloService",
                                                      "sayHello",
                                                      String.class,
                                                      new Class[]{String.class},
                                                      new Object[]{"张三"}
                                              );
                                              ctx.writeAndFlush(message);
                                          }
                                      });
                        }
                    }).connect("localhost", 8081);
            channelFuture.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
