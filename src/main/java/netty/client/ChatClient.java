package netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import netty.message.*;
import netty.protocol.MessageCodec;
import netty.protocol.ProtocolLengthDecoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodec messageCodec = new MessageCodec();
        CountDownLatch WAIT_LOGIN = new CountDownLatch(1);
        AtomicBoolean rspResult = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new ProtocolLengthDecoder());
                            nioSocketChannel.pipeline().addLast(messageCodec);
                            nioSocketChannel.pipeline().addLast(new IdleStateHandler(0,3,0));
                            nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.WRITER_IDLE) {
                                            log.debug("已经 3s 没有向服务器发送消息了");
                                            ctx.writeAndFlush(new PingMessage());
                                        }
                                    }
                                }
                            });
                            nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    new Thread(() -> {
                                        try {
                                            Scanner scanner = new Scanner(System.in);
                                            System.out.println("请输入用户名");
                                            String userName = scanner.nextLine();
                                            System.out.println("请输入密码");
                                            String password = scanner.nextLine();
                                            LoginRequestMessage req = new LoginRequestMessage(userName, password);
                                            log.debug("发送的login对象：{}", req);
                                            ctx.writeAndFlush(req);
                                            WAIT_LOGIN.await();
                                            // 返回失败结束任务
                                            if (!rspResult.get()) {
                                                ctx.channel().close();
                                            }
                                            while (true) {
                                                commandGuide();
                                                Scanner input = new Scanner(System.in);
                                                String command = input.nextLine();
                                                Message message = buildRequestMessage(command, userName);
                                                if (message != null) {
                                                    if (message.getMessageType() == Message.QuitMessage) {
                                                        ctx.channel().close();
                                                        return;
                                                    } else {
                                                        ctx.writeAndFlush(message);
                                                    }
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).start();
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.debug("msg: {}", msg);
                                    if (msg instanceof LoginResponseMessage) {
                                        LoginResponseMessage loginRsp = (LoginResponseMessage) msg;
                                        rspResult.set(loginRsp.isSuccess());
                                        WAIT_LOGIN.countDown();
                                    } else if (msg instanceof ChatRequestMessage) {
                                        ChatRequestMessage chatRsp = (ChatRequestMessage) msg;
                                        System.out.println(chatRsp.getFrom()+":"+chatRsp.getContent());
                                    } else if (msg instanceof GroupCreateResponseMessage) {
                                        GroupCreateResponseMessage createRsp = (GroupCreateResponseMessage) msg;
                                        System.out.println(createRsp.getReason());
                                    } else if (msg instanceof GroupChatResponseMessage) {
                                        GroupChatResponseMessage groupChatRsp = (GroupChatResponseMessage) msg;
                                        System.out.println("群聊消息【"+ groupChatRsp.getFrom()+"】:"+groupChatRsp.getContent());
                                    }
                                }
                            });
                        }
                    });
            // 同步阻塞直到和服务端建立连接
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            // 同步阻塞直到和服务器关闭连接
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.debug("Chat Client failed {0}", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    private static void commandGuide() {
        System.out.println("==================================");
        System.out.println("send [username] [content]");
        System.out.println("gsend [group name] [content]");
        System.out.println("gcreate [group name] [m1,m2,m3...]");
        System.out.println("gmembers [group name]");
        System.out.println("gjoin [group name]");
        System.out.println("gquit [group name]");
        System.out.println("quit");
        System.out.println("==================================");
    }

    private static Message buildRequestMessage(String command, String userName) {
        String[] commands = command.split(" ");
        Message message = null;
        switch (commands[0]) {
            case "send":
                message = new ChatRequestMessage(userName, commands[1], commands[2]);
                break;
            case "gsend":
                message = new GroupChatRequestMessage(userName, commands[1], commands[2]);
                break;
            case "gcreate":
                HashSet<String> members = new HashSet<>(Arrays.asList(commands[2].split(",")));
                message = new GroupCreateRequestMessage(commands[1], members);
                break;
            case "gmembers":
                message = new GroupMembersRequestMessage(commands[1]);
                break;
            case "gjoin":
                message = new GroupJoinRequestMessage(userName, commands[1]);
                break;
            case "gquit":
                message = new GroupQuitRequestMessage(userName, commands[1]);
                break;
            case "quit":
                message = new QuitMessage();
                break;
            default:
                System.out.println("command is wrong, please input again");
        }
        return message;
    }
}
