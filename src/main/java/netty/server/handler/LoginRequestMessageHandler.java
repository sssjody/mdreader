package netty.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.message.LoginRequestMessage;
import netty.message.LoginResponseMessage;
import netty.service.UserServiceFactory;
import netty.session.SessionFactory;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                LoginRequestMessage loginRequestMessage) throws Exception {
        String username = loginRequestMessage.getUsername();
        String password = loginRequestMessage.getPassword();
        boolean loginResult = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage rsp = loginResult ?
                new LoginResponseMessage(true, "login " + "success") :
                new LoginResponseMessage(false, "login failed");
        if (loginResult) {
            SessionFactory.getSession().bind(ctx.channel(), username);
        }
        ctx.writeAndFlush(rsp);
    }
}
