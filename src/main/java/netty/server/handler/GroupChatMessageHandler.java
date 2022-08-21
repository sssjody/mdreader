package netty.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.message.GroupChatRequestMessage;
import netty.message.GroupChatResponseMessage;
import netty.session.GroupSessionFactory;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        String groupName = groupChatRequestMessage.getGroupName();
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        membersChannel.forEach(channel -> channel.writeAndFlush(
                new GroupChatResponseMessage(groupChatRequestMessage.getFrom(), groupChatRequestMessage.getContent())));
    }
}
