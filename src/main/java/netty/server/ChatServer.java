package netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import netty.protocol.MessageCodec;
import netty.protocol.ProtocolLengthDecoder;
import netty.server.handler.*;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        NioEventLoopGroup boss = new NioEventLoopGroup(1);

        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodec messageCodec = new MessageCodec();
        LoginRequestMessageHandler LOGIN_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_HANDLER = new ChatRequestMessageHandler();
        GroupCreateMessageHandler GROUP_CREATE_HANDLER = new GroupCreateMessageHandler();
        GroupChatMessageHandler GROUP_CHAT_HANDLER = new GroupChatMessageHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new IdleStateHandler(5, 0, 0));
                            nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.READER_IDLE) {
                                            log.debug("已经 5s 没有接收到客户端的消息，关闭客户端");
                                            ctx.channel().close();
                                        }
                                    }
                                }
                            });
                            nioSocketChannel.pipeline().addLast(new ProtocolLengthDecoder());
                            nioSocketChannel.pipeline().addLast(loggingHandler);
                            nioSocketChannel.pipeline().addLast(messageCodec);
                            nioSocketChannel.pipeline().addLast(LOGIN_HANDLER);
                            nioSocketChannel.pipeline().addLast(CHAT_HANDLER);
                            nioSocketChannel.pipeline().addLast(GROUP_CREATE_HANDLER);
                            nioSocketChannel.pipeline().addLast(GROUP_CHAT_HANDLER);
                            // 用来监听客户端正常或者异常关闭
                            nioSocketChannel.pipeline().addLast(QUIT_HANDLER);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.debug("Chat Server Failed, {0}", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

}
