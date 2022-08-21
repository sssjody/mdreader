package netty.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.message.ChatRequestMessage;
import netty.message.ChatResponseMessage;
import netty.session.SessionFactory;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage chatRequestMessage)
            throws Exception {
        Channel channel = SessionFactory.getSession().getChannel(chatRequestMessage.getTo());
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(chatRequestMessage.getFrom(),
                    chatRequestMessage.getContent()));
        } else {
            channelHandlerContext.writeAndFlush(new ChatResponseMessage(false, "发送消息的用户不存在或者不在线"));
        }
    }
}
