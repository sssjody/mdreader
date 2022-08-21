package netty.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.message.GroupCreateRequestMessage;
import netty.message.GroupCreateResponseMessage;
import netty.session.Group;
import netty.session.GroupSessionFactory;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        String groupName = groupCreateRequestMessage.getGroupName();
        Set<String> members = groupCreateRequestMessage.getMembers();

        Group group = GroupSessionFactory.getGroupSession().createGroup(groupName, members);
        if (group == null) {
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            membersChannel.forEach(channel -> channel.writeAndFlush(new GroupCreateResponseMessage(true,
                    "你已被加入" + groupName + "群聊")));
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(true, groupName + "已经创建成功"));
        } else {
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(false, group + "已经存在"));
        }
    }
}
